package me.zombii.horizon.entity;

import finalforeach.cosmicreach.blocks.Block;
import me.zombii.horizon.HorizonConstants;

public class C4 extends Cube {

    public C4() {
        super(Block.getById("base:c4").getDefaultBlockState(), HorizonConstants.MOD_ID + ":c4");
    }

}
