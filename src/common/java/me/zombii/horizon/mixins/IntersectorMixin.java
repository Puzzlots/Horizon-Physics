package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Intersector.class)
public abstract class IntersectorMixin {

    @Shadow
    public static boolean intersectRayOrientedBounds(Ray ray, OrientedBoundingBox obb, Vector3 intersection) {
        return false;
    }

    @Shadow @Final private static Vector3 v2;

    @Unique
    private static boolean horizonPhysics$intersectRayBounds(Ray ray, BoundingBox box, Vector3 intersection) {
        if (box.contains(ray.origin)) {
            if (intersection != null) {
                intersection.set(ray.origin);
            }

            return true;
        } else {
            float lowest = 0.0F;
            boolean hit = false;
            float t;
            if (ray.origin.x <= box.min.x && ray.direction.x > 0.0F) {
                t = (box.min.x - ray.origin.x) / ray.direction.x;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.y >= box.min.y && v2.y <= box.max.y && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (ray.origin.x >= box.max.x && ray.direction.x < 0.0F) {
                t = (box.max.x - ray.origin.x) / ray.direction.x;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.y >= box.min.y && v2.y <= box.max.y && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (ray.origin.y <= box.min.y && ray.direction.y > 0.0F) {
                t = (box.min.y - ray.origin.y) / ray.direction.y;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.x >= box.min.x && v2.x <= box.max.x && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (ray.origin.y >= box.max.y && ray.direction.y < 0.0F) {
                t = (box.max.y - ray.origin.y) / ray.direction.y;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.x >= box.min.x && v2.x <= box.max.x && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (ray.origin.z <= box.min.z && ray.direction.z > 0.0F) {
                t = (box.min.z - ray.origin.z) / ray.direction.z;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.x >= box.min.x && v2.x <= box.max.x && v2.y >= box.min.y && v2.y <= box.max.y && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (ray.origin.z >= box.max.z && ray.direction.z < 0.0F) {
                t = (box.max.z - ray.origin.z) / ray.direction.z;
                if (t >= 0.0F) {
                    v2.set(ray.direction).scl(t).add(ray.origin);
                    if (v2.x >= box.min.x && v2.x <= box.max.x && v2.y >= box.min.y && v2.y <= box.max.y && (!hit || t < lowest)) {
                        hit = true;
                        lowest = t;
                    }
                }
            }

            if (hit && intersection != null) {
                intersection.set(ray.direction).scl(lowest).add(ray.origin);
                if (intersection.x < box.min.x) {
                    intersection.x = box.min.x;
                } else if (intersection.x > box.max.x) {
                    intersection.x = box.max.x;
                }

                if (intersection.y < box.min.y) {
                    intersection.y = box.min.y;
                } else if (intersection.y > box.max.y) {
                    intersection.y = box.max.y;
                }

                if (intersection.z < box.min.z) {
                    intersection.z = box.min.z;
                } else if (intersection.z > box.max.z) {
                    intersection.z = box.max.z;
                }
            }

            return hit;
        }
    }

    @Inject(method = "intersectRayBounds", at = @At("HEAD"), cancellable = true)
    private static void intersectRayBounds0(Ray ray, BoundingBox box, Vector3 intersection, CallbackInfoReturnable<Boolean> cir) {
        if (((ExtendedBoundingBox) box).hasInnerBounds()) {
            cir.setReturnValue(intersectRayOrientedBounds(ray, ((ExtendedBoundingBox) box).getInnerBounds(), intersection));
            ((ExtendedBoundingBox) box).setInnerBounds(null);
            return;
        }
        cir.setReturnValue(horizonPhysics$intersectRayBounds(ray, box, intersection));
    }

}
