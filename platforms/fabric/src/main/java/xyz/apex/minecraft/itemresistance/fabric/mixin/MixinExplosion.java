package xyz.apex.minecraft.itemresistance.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

import java.util.List;

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
            )
    )
    private void ItemResistance$explode(CallbackInfo ci, @Local LocalRef<List<Entity>> entities)
    {
        var explosions = (Explosion) (Object) this;
        ItemResistance.onExplosionDetonate(level, explosions, entities.get());
    }
}
