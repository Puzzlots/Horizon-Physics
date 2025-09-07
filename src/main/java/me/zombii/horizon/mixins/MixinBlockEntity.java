package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.Cube;
import me.zombii.horizon.entity.api.PhysicEntityBoundBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements PhysicEntityBoundBlockEntity {

    transient Cube cube;

    @Override
    public Cube getEntity() {
        return cube;
    }

    @Override
    public void setEntity(Cube cube) {
        this.cube = cube;
    }

    @Shadow
    private transient Zone zone;
    @Shadow
    private transient int x;
    @Shadow
    private transient int y;
    @Shadow
    private transient int z;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockState getBlockState() {
        if (cube != null) return cube.state.get();
        return this.zone.getBlockState(this.x, this.y, this.z);
    }

}
