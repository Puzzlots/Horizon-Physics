package me.zombii.horizon.threading;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import com.jme3.bullet.PhysicsSpace;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.block.IBlockPosition;
import io.github.puzzle.cosmic.api.entity.player.IPlayer;
import io.github.puzzle.cosmic.api.item.IItemSlot;
import io.github.puzzle.cosmic.api.world.IZone;
import io.github.puzzle.cosmic.impl.ray.Raycaster;
import io.github.puzzle.cosmic.util.APISide;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.collision.AABB;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.rendering.IShapeRenderer;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.world.PhysicsZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LidarThread implements TickingRunnable {

    public static Logger LOGGER = LoggerFactory.getLogger("Horizon Lidar Thread");
    public static LidarThread INSTANCE;

    public static PauseableThread parent;
    public static final Queue<Runnable> queuedRunnables = new Queue<>();

    public LidarThread() {
        INSTANCE = this;
        parent = (PauseableThread) ThreadHelper.getThread("physics");
    }

    boolean isInitialized = false;
    public boolean shouldRun = false;

    public static void queue(Zone zone, Vector3 position, Vector3 viewDirection) {
        LidarThread.queuedRunnables.addLast(new Runnable() {
            Entity[] entities;
            final Vector3 test = new Vector3(0.03f, 0.03f, 0.03f);

            final Matrix4 matrix4f = new Matrix4();

            private Boolean onHit(Vector3 vector3) {
                for (Entity entity : entities) {
                    if (entity instanceof PlayerEntity pe) continue;

                    BoundingBox eb = entity.globalBoundingBox;

                    if (((ExtendedBoundingBox) eb).hasInnerBounds()) {
                        if (((ExtendedBoundingBox) eb).getInnerBounds().contains(vector3)) {
                            if (entity instanceof IPhysicEntity) {
                                matrix4f.idt();
                                matrix4f.set(ConversionUtil.fromJME(((IPhysicEntity) entity).getEularRotation()));
                                Vector3 v = vector3.cpy().sub(entity.position).unrotate(matrix4f);

                                if (entity instanceof IVirtualZoneEntity vz) {
                                    PhysicsZone zone1 = vz.getWorld();

                                    BlockPosition pos = BlockPosition.ofGlobal(zone, (int) vector3.x, (int) vector3.y, (int) vector3.z);
                                    if (pos.chunk == null) return false;
                                    BlockState blockState = pos.getBlockState();
                                    if (Block.AIR.getDefaultBlockState() != blockState && Block.WATER.getDefaultBlockState() != blockState) {
                                        for (AABB b : ((IBlockBoundsMaker) blockState.getModel()).getBounds((int) v.x, (int) v.y, (int) v.z)) {
                                            if (b.intersects(vector3)) return true;
                                        }
                                    }
                                }
                            } else return true;
                        }
                    } else if (eb.contains(vector3)) {
                        return true;
                    }
                }


                BlockPosition pos = BlockPosition.ofGlobal(zone, (int) vector3.x, (int) vector3.y, (int) vector3.z);
                if (pos.chunk == null) return false;
                BlockState blockState = pos.getBlockState();

                if (blockState != null) {
                    for (AABB b : ((IBlockBoundsMaker) blockState.getModel()).getBounds(pos.getGlobalX(), pos.getGlobalY(), pos.getGlobalZ())) {
                        if (b.intersects(vector3.x, vector3.y, vector3.z)) return true;
                    }
                }

                return false;
            }

            @Override
            public void run() {
                entities = zone.getAllEntities().toArray(Entity.class);

                Raycaster.RaycastContext context = new Raycaster.RaycastContext(
                        zone,
                        .5f,
                        .05f
                );
                Vector3 dir;
                context.start(position);

                float degrees = 0;
                for (int j = 0; j < 100; j++) {
                    dir = viewDirection.cpy();
                    dir.rotate(-60, 0, 1, 0);
                    dir.rotate(degrees - 50, viewDirection.x, 0, viewDirection.z);

                    degrees += 1;
                    for (float i = 0; i < 110; i++) {
                        dir.rotate(1, 0, 1, 0);
                        context.end(dir, 128);

                        Raycaster.RaycastHitResult hitResult = Raycaster.castRay(context, this::onHit);
                        try {
                            Vector3 hit = hitResult.getVector();
//                            if (onHit(hit)) {
                                hit.sub(test.cpy().scl(dir));

                                IShapeRenderer.points.add(hit);
//                            }
                        } catch (Exception e) {}
                    }
                }
                System.err.println("Finished");
            }
        });
    }

    @Override
    public void run(float delta) {
        if (shouldRun) {
            synchronized (queuedRunnables) {
                while (!queuedRunnables.isEmpty()) {
                    queuedRunnables.removeFirst().run();
                }
            }
        }
    }

    public static void pause() {
        INSTANCE.shouldRun = false;
        parent.onPause();
    }

    public static boolean started = false;

    public static void resume() {
        INSTANCE.shouldRun = true;
        parent.onResume();
    }

    public static void init() {
        parent = ThreadHelper.createTicking("lidar", new LidarThread());
    }

    public static PauseableThread start() {
        if (started) {
            INSTANCE.shouldRun = true;
            resume();
            return null;
        }
        if (parent == null) {
            throw new RuntimeException("Call `init()` on the `LidarThread` first.");
        }

        parent.start();
        INSTANCE.shouldRun = true;

        started = true;
        return parent;
    }

}
