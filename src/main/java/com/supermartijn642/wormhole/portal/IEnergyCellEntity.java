package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.energycell.EnergyHolder;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface IEnergyCellEntity extends EnergyHolder {

    @Override
    default int receiveEnergy(int maxReceive, boolean simulate){
        return this.receiveEnergy(maxReceive, simulate, false);
    }

    /**
     * {@link IEnergyStorage#receiveEnergy(int, boolean)}
     */
    int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup);

    @Override
    default int extractEnergy(int maxExtract, boolean simulate){
        return this.extractEnergy(maxExtract, simulate, false);
    }

    /**
     * {@link IEnergyStorage#extractEnergy(int, boolean)}
     */
    int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup);

    @Override
    default int getEnergyStored(){
        return this.getEnergyStored(false);
    }

    /**
     * {@link IEnergyStorage#getEnergyStored()}
     */
    int getEnergyStored(boolean fromGroup);

    @Override
    void setEnergyStored(int energy);

    @Override
    default int getMaxEnergyStored(){
        return this.getMaxEnergyStored(false);
    }

    /**
     * {@link IEnergyStorage#getMaxEnergyStored()}
     */
    int getMaxEnergyStored(boolean fromGroup);
}
