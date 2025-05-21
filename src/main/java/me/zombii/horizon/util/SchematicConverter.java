package me.zombii.horizon.util;

import me.zombii.horizon.world.VirtualWorld;

public class SchematicConverter {

//    public static VirtualWorld structureMapFromSchematic(Schematic schematic) {
//        StructureWorld world = new StructureWorld();
//
//        Structure structure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "0"));
//
//        Vec3i lastChunkCoord = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        Vec3i currentChunkCoord;
//        int length = cubize(schematic.length);
//        int height = cubize(schematic.height);
//        int width = cubize(schematic.width);
//
//        for (int x = 0; x < length; x++) {
//            for (int y = 0; y < height; y++) {
//                for (int z = 0; z < width; z++) {
//                    BlockState state;
//                    try {
//                        state = schematic.getBlockState(
//                                x,
//                                y,
//                                z
//                        );
//                    } catch (Exception ignore) {
//                        state = BlockState.getInstance("base:air[default]");
//                    }
//
//                    int cx = Math.floorDiv(x, 16);
//                    int cy = Math.floorDiv(y, 16);
//                    int cz = Math.floorDiv(z, 16);
//                    currentChunkCoord = new Vec3i(cx, cy, cz);
//
//                    int lx = x - (cx * 16);
//                    int ly = y - (cy * 16);
//                    int lz = z - (cz * 16);
//
//                    if (lx == 15 && ly == 15 && lz == 15) {
////                    if (lastChunkCoord != currentChunkCoord) {
//                        world.putChunkAt(new Vec3i(cx, cy ,cz), structure);
//                        structure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "1"));
//                        lastChunkCoord = currentChunkCoord;
//                    } else {
////                        int nlx = x >= schematic.length ? schematic.length - (cx * 16) : lx;
////                        int nly = y >= schematic.height ? schematic.height - (cy * 16) : ly;
////                        int nlz = z >= schematic.width ? schematic.width - (cz * 16) : lz;
//                        structure.setBlockState(state, lx, ly, lz);
//                    }
//                }
//            }
//        }

//        return null;
//    }

}
