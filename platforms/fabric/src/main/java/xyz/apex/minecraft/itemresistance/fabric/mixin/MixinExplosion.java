package xyz.apex.minecraft.itemresistance.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

import java.util.List;
import java.util.Set;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private Level level;

    @Inject(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V",
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void ItemResistance$explode(CallbackInfo ci, Set<BlockPos> blockPositions, int i, float diameter, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, List<Entity> entities)
    {
        var explosions = (Explosion) (Object) this;
        ItemResistance.onExplosionDetonate(level, explosions, entities);
    }
}
