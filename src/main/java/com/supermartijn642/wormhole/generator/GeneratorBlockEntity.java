package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.energycell.EnergyHolder;
import com.supermartijn642.wormhole.portal.IPortalGroupEntity;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class GeneratorBlockEntity extends BaseBlockEntity implements TickableBlockEntity, EnergyHolder {

    private static final int BLOCKS_PER_TICK = 5;

    protected int energy;
    protected final int energyCapacity;
    private final int energyRange;
    private final int energyTransferLimit;

    private final Set<BlockPos> portalBlocks = new LinkedHashSet<>();
    private final Set<BlockPos> energyBlocks = new LinkedHashSet<>();

    private int searchX, searchY, searchZ;

    public GeneratorBlockEntity(BaseBlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int energyCapacity, int energyRange, int energyTransferLimit){
        super(blockEntityType, pos, state);
        this.energyCapacity = energyCapacity;
        this.energyRange = energyRange;
        this.energyTransferLimit = energyTransferLimit;
        this.searchX = this.searchY = this.searchZ = -energyRange;
    }

    @Override
    public void update(){
        // find blocks with the energy capability
        for(int i = 0; i < BLOCKS_PER_TICK; i++){
            BlockPos pos = this.worldPosition.offset(this.searchX, this.searchY, this.searchZ);

            if(!pos.equals(this.worldPosition)){
                BlockEntity entity = this.level.getBlockEntity(pos);
                if(entity instanceof IPortalGroupEntity && ((IPortalGroupEntity)entity).hasGroup()){
                    this.portalBlocks.add(pos);
                    this.energyBlocks.remove(pos);
                }else{
                    boolean isEnergyHolder = entity instanceof EnergyHolder;
                    if(!isEnergyHolder && entity != null && CommonUtils.isModLoaded("team_reborn_energy")){
                        BlockState state = entity.getBlockState();
                        for(Direction side : Direction.values()){
                            EnergyStorage storage = EnergyStorage.SIDED.find(this.level, pos, state, entity, side);
                            if(storage != null && storage.supportsInsertion()){
                                isEnergyHolder = true;
                                break;
                            }
                        }
                    }
                    if(isEnergyHolder)
                        this.energyBlocks.add(pos);
                    else
                        this.energyBlocks.remove(pos);
                    this.portalBlocks.remove(pos);
                }
            }

            this.searchX++;
            if(this.searchX > this.energyRange){
                this.searchX = -this.energyRange;
                this.searchZ++;
                if(this.searchZ > this.energyRange){
                    this.searchZ = -this.energyRange;
                    this.searchY++;
                    if(this.searchY > this.energyRange)
                        this.searchY = -this.energyRange;
                }
            }
        }

        if(this.energy <= 0)
            return;

        // transfer energy
        int toTransfer = Math.min(this.energyTransferLimit, this.energy);
        Set<BlockPos> toRemove = new HashSet<>();
        Iterator<BlockPos> iterator = this.portalBlocks.iterator();
        while(iterator.hasNext()){
            BlockPos pos = iterator.next();
            BlockEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof IPortalGroupEntity && ((IPortalGroupEntity)entity).hasGroup()){
                PortalGroup group = ((IPortalGroupEntity)entity).getGroup();
                int transferred = group.receiveEnergy(toTransfer, false);
                toTransfer -= transferred;
                this.energy -= transferred;
                if(this.energy == 0)
                    return;
            }else
                toRemove.add(pos);
        }
        this.portalBlocks.removeAll(toRemove);
        toRemove.clear();
        iterator = this.energyBlocks.iterator();
        while(iterator.hasNext()){
            BlockPos pos = iterator.next();
            BlockEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof EnergyHolder){
                int transferred = ((EnergyHolder)entity).receiveEnergy(toTransfer, false);
                toTransfer -= transferred;
                this.energy -= transferred;
                if(this.energy == 0)
                    return;
            }else{
                boolean isEnergyHolder = false;
                if(entity != null && CommonUtils.isModLoaded("team_reborn_energy")){
                    BlockState state = entity.getBlockState();
                    for(Direction side : Direction.values()){
                        EnergyStorage storage = EnergyStorage.SIDED.find(this.level, pos, state, entity, side);
                        if(storage != null && storage.supportsInsertion()){
                            try(Transaction transaction = Transaction.openOuter()){
                                int transferred = (int)storage.insert(toTransfer, transaction);
                                toTransfer -= transferred;
                                this.energy -= transferred;
                                if(this.energy == 0)
                                    return;
                            }
                            isEnergyHolder = true;
                            break;
                        }
                    }
                }
                if(!isEnergyHolder)
                    toRemove.add(pos);
            }
        }
        this.energyBlocks.removeAll(toRemove);
    }

    public Set<BlockPos> getChargingPortalBlocks(){
        return this.portalBlocks;
    }

    public Set<BlockPos> getChargingEnergyBlocks(){
        return this.energyBlocks;
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag data = new CompoundTag();
        data.putInt("energy", this.energy);
        data.putInt("searchX", this.searchX);
        data.putInt("searchY", this.searchY);
        data.putInt("searchZ", this.searchZ);
        return data;
    }

    @Override
    protected void readData(CompoundTag tag){
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;
        this.searchX = tag.contains("searchX") ? Math.min(Math.max(tag.getInt("searchX"), -this.energyRange), this.energyRange) : 0;
        this.searchY = tag.contains("searchY") ? Math.min(Math.max(tag.getInt("searchY"), -this.energyRange), this.energyRange) : 0;
        this.searchZ = tag.contains("searchZ") ? Math.min(Math.max(tag.getInt("searchZ"), -this.energyRange), this.energyRange) : 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
        return Math.min(Math.min(this.energy, this.energyTransferLimit), maxExtract);
    }

    @Override
    public int getEnergyStored(){
        return this.energy;
    }

    @Override
    public void setEnergyStored(int energy){
        this.energy = energy;
    }

    @Override
    public int getMaxEnergyStored(){
        return this.energyCapacity;
    }

    @Override
    public boolean canExtract(){
        return true;
    }

    @Override
    public boolean canReceive(){
        return false;
    }
}
