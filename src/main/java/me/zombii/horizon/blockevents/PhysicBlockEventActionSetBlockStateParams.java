package me.zombii.horizon.blockevents;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateMissing;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.util.SingleBlockEventArgs;

import java.util.HashMap;

@ActionId(
        id = "base:set_block_state_params"
)
public class PhysicBlockEventActionSetBlockStateParams implements IBlockAction {
    private static final Pool<BlockPosition> POSITION_POOL = Pools.get(BlockPosition.class);
    public int xOff;
    public int yOff;
    public int zOff;
    public int tickDelay = 0;
    public HashMap<String, String> params = new HashMap();

    public PhysicBlockEventActionSetBlockStateParams() {}

    public void act(BlockEventArgs args) {
        Zone zone = args.zone;
        BlockPosition sourcePos = args.blockPos;
        if (xOff + yOff + zOff == 0 && args instanceof SingleBlockEventArgs args1) {
            args1.getCube().setState(args.srcBlockState.getVariantWithParams(this.params));
            return;
        }
        BlockPosition bp = sourcePos.getOffsetBlockPos(POSITION_POOL, zone, this.xOff, this.yOff, this.zOff);
        BlockState blockState = bp.getBlockState();
        if (blockState != null) {
            blockState = blockState.getVariantWithParams(this.params);
            BlockSetter.get().replaceBlock(zone, blockState, bp);
        }

        POSITION_POOL.free(bp);
    }
}
