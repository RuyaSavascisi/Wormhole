package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.targetcell.TargetCellType;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class WormholeItemInfoGenerator extends ItemInfoGenerator {

    public WormholeItemInfoGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(Wormhole.portal_frame, "block/portal_frame");
        this.simpleInfo(Wormhole.portal, "block/portals/portal_x_red");
        this.simpleInfo(Wormhole.portal_stabilizer, "block/portal_stabilizer_off");
        this.simpleInfo(Wormhole.basic_energy_cell, "block/energy_cells/basic_energy_cell_0");
        this.simpleInfo(Wormhole.advanced_energy_cell, "block/energy_cells/advanced_energy_cell_0");
        this.simpleInfo(Wormhole.creative_energy_cell, "block/energy_cells/creative_energy_cell");
        this.simpleInfo(Wormhole.basic_target_cell, "block/portal_frame");
        for(TargetCellType type : TargetCellType.values())
            this.simpleInfo(type.getBlock(), "block/target_cells/" + type.getRegistryName() + "_0");
        this.simpleInfo(Wormhole.coal_generator, "block/coal_generator");
        this.simpleInfo(Wormhole.target_device, "item/target_device");
        this.simpleInfo(Wormhole.advanced_target_device, "item/advanced_target_device");
    }
}
