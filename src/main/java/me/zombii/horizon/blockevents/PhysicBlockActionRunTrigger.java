package me.zombii.horizon.blockevents;

import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blockevents.ScheduledTrigger;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.util.SingleBlockEventArgs;

@ActionId(
        id = "base:run_trigger"
)
public class PhysicBlockActionRunTrigger implements IBlockAction {
    public String triggerId;
    public String blockEventId;
    public int xOff;
    public int yOff;
    public int zOff;
    public int tickDelay = 0;
    public boolean addToQueue = false;
    public boolean createSubqueue = false;
    public boolean useSrcBlockEvents = false;
    public Boolean updateSrcBlockState;

    public void act(BlockEventArgs args) {
        Zone zone = args.zone;
        BlockPosition sourcePosition = args.blockPos;
        BlockPosition targetPosition = sourcePosition.getOffsetBlockPos(zone, this.xOff, this.yOff, this.zOff);
        if (targetPosition != null) {
            BlockEventTrigger[] triggers = null;
            boolean shouldQueue = this.tickDelay > 0 || this.addToQueue;
            if (!shouldQueue) {
                BlockEventTrigger[] targetEvents;
                if (this.blockEventId == null) {
                    if (this.useSrcBlockEvents && args.srcBlockState != null) {
                        targetEvents = args.srcBlockState.getTrigger(this.triggerId);
                    } else {
                        if (xOff + yOff + zOff == 0) {
                            targetEvents = args.srcBlockState.getTrigger(this.triggerId);
                        } else {
                            targetEvents = targetPosition.getBlockState().getTrigger(this.triggerId);
                        }
                    }
                } else {
                    targetEvents = BlockEvents.getInstance(this.blockEventId).getTriggers(this.triggerId);
                }

                if (targetEvents == null) {
                    return;
                }

                triggers = targetEvents;
            }

            if (args instanceof SingleBlockEventArgs singleBlockEventArgs && xOff + yOff + zOff == 0) {
                SingleBlockEventArgs newArgs = new SingleBlockEventArgs();
                newArgs.setCube(singleBlockEventArgs.getCube());
                newArgs.zone = zone;
                newArgs.blockPos = targetPosition;
                boolean doUpdateSrc = this.updateSrcBlockState != null ? this.updateSrcBlockState : !targetPosition.equals(sourcePosition);
                if (doUpdateSrc) {
                    newArgs.srcBlockState = singleBlockEventArgs.getCube().state.get();
                } else {
                    newArgs.srcBlockState = args.srcBlockState;
                }

                newArgs.setSrcIdentity(args.getSrcIdentity());
                newArgs.srcPlayer = args.srcPlayer;
                if (!this.createSubqueue) {
                    args.shareQueue(newArgs);
                }

                if (shouldQueue) {
                    int runTimestamp = zone.currentZoneTick + this.tickDelay;
                    ScheduledTrigger queuedTrigger = ScheduledTrigger.POOL.obtain();
                    queuedTrigger.set(runTimestamp, this.triggerId, newArgs, this.createSubqueue);
                    if (this.addToQueue) {
                        args.addQueuedTrigger(queuedTrigger);
                    } else {
                        zone.eventQueue.add(queuedTrigger);
                    }
                } else {
                    newArgs.run(triggers);
                    if (this.createSubqueue) {
                        newArgs.runScheduledTriggers();
                    }
                }

                return;
            }
            BlockEventArgs newArgs = BlockEventArgs.POOL.obtain();
            newArgs.zone = zone;
            newArgs.blockPos = targetPosition;
            boolean doUpdateSrc = this.updateSrcBlockState != null ? this.updateSrcBlockState : !targetPosition.equals(sourcePosition);
            if (doUpdateSrc) {
                newArgs.srcBlockState = targetPosition.getBlockState();
            } else {
                newArgs.srcBlockState = args.srcBlockState;
            }

            newArgs.setSrcIdentity(args.getSrcIdentity());
            newArgs.srcPlayer = args.srcPlayer;
            if (!this.createSubqueue) {
                args.shareQueue(newArgs);
            }

            if (shouldQueue) {
                int runTimestamp = zone.currentZoneTick + this.tickDelay;
                ScheduledTrigger queuedTrigger = ScheduledTrigger.POOL.obtain();
                queuedTrigger.set(runTimestamp, this.triggerId, newArgs, this.createSubqueue);
                if (this.addToQueue) {
                    args.addQueuedTrigger(queuedTrigger);
                } else {
                    zone.eventQueue.add(queuedTrigger);
                }
            } else {
                newArgs.run(triggers);
                if (this.createSubqueue) {
                    newArgs.runScheduledTriggers();
                }

                BlockEventArgs.POOL.free(newArgs);
            }

        }
    }
}
