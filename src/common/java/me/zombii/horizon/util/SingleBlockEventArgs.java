package me.zombii.horizon.util;

import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import me.zombii.horizon.entity.Cube;

public class SingleBlockEventArgs extends BlockEventArgs {

    Cube cube;

    public void setCube(Cube cube) {
        this.cube = cube;
    }

    public Cube getCube() {
        return cube;
    }
}
