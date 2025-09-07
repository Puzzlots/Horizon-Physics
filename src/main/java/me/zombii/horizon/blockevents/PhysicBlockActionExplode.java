//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.zombii.horizon.blockevents;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.IDamageSource;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.particles.GameParticleSystem;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.threading.PhysicsThread;

@ActionId(
    id = "base:explode"
)
public class PhysicBlockActionExplode implements IBlockAction {
    public String blockStateId = "base:air[default]";
    public int xOff;
    public int yOff;
    public int zOff;
    public float radius;
    public boolean dropsItems = true;

    public void act(BlockEventArgs args) {
        Zone zone = args.zone;
        BlockState srcBlockState = args.srcBlockState;
        BlockPosition sourcePos = args.blockPos;
        float radiusSq = this.radius * this.radius;
        Queue<BlockPosition> explodeQueue = new Queue();

        for(float i = -this.radius; i <= this.radius; ++i) {
            for(float j = -this.radius; j <= this.radius; ++j) {
                for(float k = -this.radius; k <= this.radius; ++k) {
                    float workingRadiusSq = Vector3.len2(i, j, k);
                    if (workingRadiusSq <= radiusSq) {
                        BlockPosition pos = sourcePos.getOffsetBlockPos(zone, (int)((float)this.xOff + i), (int)((float)this.yOff + j), (int)((float)this.zOff + k));
                        if (pos != null) {
                            explodeQueue.addLast(pos);
                        }
                    }
                }
            }
        }

        double r2 = (radius + 3) * (radius + 3);
        PhysicsThread.INSTANCE.space.getRigidBodyList().forEach((r) -> {

            double centerX = (xOff + sourcePos.getGlobalX());
            double centerY = (yOff + sourcePos.getGlobalY());
            double centerZ = (zOff + sourcePos.getGlobalZ());

            double itemX = r.getPhysicsLocation(new Vector3f()).x;
            double itemY = r.getPhysicsLocation(new Vector3f()).y;
            double itemZ = r.getPhysicsLocation(new Vector3f()).z;

            double dx = itemX - (xOff + sourcePos.getGlobalX());
            double dy = itemY - (yOff + sourcePos.getGlobalY());
            double dz = itemZ - (zOff + sourcePos.getGlobalZ());

            double dist2 = dx * dx + dy * dy + dz * dz;
            if (dist2 <= r2) {
                Vector3f dir = new Vector3f();
                dir.x = (float) (itemX - centerX);
                dir.y = (float) (itemY - centerY);
                dir.z = (float) (itemZ - centerZ);

                dir.normalize();

                float strength = 10 * radius;

                dir.mult(strength ,dir);

                r.applyCentralImpulse(dir);
            }
        });

        BlockState blockState;
        if ("self".equals(this.blockStateId)) {
            blockState = srcBlockState;
        } else {
            blockState = BlockState.getInstance(this.blockStateId, MissingBlockStateResult.MISSING_OBJECT);
        }

        BlockEventArgs newArgs = new BlockEventArgs();
        Array<ItemStack> droppedItemStacks = null;
        if (this.dropsItems) {
            droppedItemStacks = new Array<>();
        }

        for(BlockPosition explodedPos : explodeQueue) {
            if (!explodedPos.equals(sourcePos)) {
                BlockState explodedBlock = explodedPos.getBlockState();
                if (explodedBlock != null) {
                    if (this.dropsItems && explodedBlock.canDrop) {
                        Item item = explodedBlock.getDroppedItem();
                        boolean added = false;

                        if (droppedItemStacks != null) {
                            for (ItemStack i : droppedItemStacks) {
                                if (i.getItem() == item && i.amount < i.stackLimit) {
                                    ++i.amount;
                                    added = true;
                                    break;
                                }
                            }

                            if (!added) {
                                droppedItemStacks.add(new ItemStack(item));
                            }
                        }
                    }

                    BlockEventTrigger[] explode = explodedBlock.getTrigger("onExplode");
                    if (explode != null) {
                        for(BlockEventTrigger e : explode) {
                            newArgs.blockPos = explodedPos;
                            newArgs.srcBlockState = explodedBlock;
                            newArgs.zone = zone;
                            args.shareQueue(newArgs);
                            newArgs.setSrcIdentity(args.getSrcIdentity());
                            e.act(newArgs);
                        }

                        newArgs.runScheduledTriggers();
                    }
                }
            }
        }

        float cx = args.blockPos.getCenterX();
        float cy = args.blockPos.getCenterY();
        float cz = args.blockPos.getCenterZ();
        ArrayUtils.forEach(zone.getAllEntities(), (ex) -> {
            if (!(ex instanceof ItemEntity)) {
                float dst2 = ex.position.dst2(cx, cy, cz);
                if (dst2 < radiusSq * 2.0F) {
                    float d = Math.max(dst2, 0.1F);
                    float damage = 50.0F / d;
                    ex.hit((IDamageSource)null, damage);
                    float vx = 7.0F * Math.signum(ex.position.x - cx) / d;
                    float vy = 7.0F / d;
                    float vz = 7.0F * Math.signum(ex.position.z - cz) / d;
                    ex.velocity.add(vx, vy, vz);
                }

            }
        });
        if (this.dropsItems) {

            for (ItemStack i : droppedItemStacks) {
                ItemEntity entity = i.spawnItemEntityAt(args.blockPos);
                entity.velocity.setToRandomDirection().scl(7.0F).add(0.0F, 7.0F, 0.0F);
            }
        }

        BlockSetter.get().replaceBlocks(zone, blockState, explodeQueue);
        GameParticleSystem system = GameParticleSystem.explosionParticleSystem.copy();
        system.setOrigin((float)(sourcePos.getGlobalX() + this.xOff), (float)(sourcePos.getGlobalY() + this.yOff), (float)(sourcePos.getGlobalZ() + this.zOff));
        system.broadcastOrAdd(zone);
    }
}
