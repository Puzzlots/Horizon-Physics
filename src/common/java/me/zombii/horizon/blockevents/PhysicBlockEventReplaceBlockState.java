package me.zombii.horizon.blockevents;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.IReadBlockPosition;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEvents;
import finalforeach.cosmicreach.gameevents.blockevents.ScheduledBlockTrigger;
import finalforeach.cosmicreach.gameevents.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.util.SingleBlockEventArgs;

import java.util.HashMap;

@ActionId(
        id = "base:replace_block_state"
)
public class PhysicBlockEventReplaceBlockState implements IBlockAction {
    private static final Pool<BlockPosition> POSITION_POOL = Pools.get(BlockPosition.class);
    public String blockStateId;
    public int xOff;
    public int yOff;
    public int zOff;
    public String[] copiedParams;
    public String srcDirectionParam;

    public void act(BlockEventArgs args) {
        Zone zone = args.zone;
        BlockState srcBlockState = args.srcBlockState;
        IReadBlockPosition sourcePos = args.blockPos;
        BlockState blockState;
        if ("self".equals(this.blockStateId)) {
            blockState = srcBlockState;
        } else {
            blockState = BlockState.getInstance(this.blockStateId, MissingBlockStateResult.MISSING_OBJECT);
        }

        if (this.copiedParams != null) {
            for(String param : this.copiedParams) {
                blockState = blockState.getVariantWithParam(param, srcBlockState.getParam(param));
            }
        }

        BlockPosition bp = sourcePos.getOffsetBlockPos(POSITION_POOL, zone, this.xOff, this.yOff, this.zOff);
        BlockPosition bpCur = bp;
        if (this.srcDirectionParam != null) {
            Direction direction = Direction.fromStr(srcBlockState.getParam(this.srcDirectionParam));
            bpCur = bp.getOffsetBlockPos(bp, zone, direction);
        } else {
            if (xOff + yOff + zOff == 0 && args instanceof SingleBlockEventArgs args1) {
                args1.getCube().setState(blockState);
                POSITION_POOL.free(bp);
                return;
            }
        }

        if (bpCur != null) {
            BlockSetter.get().replaceBlock(zone, blockState, bpCur);
        }

        POSITION_POOL.free(bp);
    }
}
