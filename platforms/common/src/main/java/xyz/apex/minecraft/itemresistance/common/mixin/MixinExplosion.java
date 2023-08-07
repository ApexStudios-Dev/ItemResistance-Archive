package xyz.apex.minecraft.itemresistance.common.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private Level level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow public abstract void clearToBlow();

    @Inject(
            method = "finalizeExplosion",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ItemResistance$finalizeExplosion(boolean spawnParticles, CallbackInfo ci)
    {
        if(level.getGameRules().getBoolean(ItemResistance.GAMERULE_EXPLOSIONS_DISABLED))
        {
            clearToBlow();

            if(level.isClientSide)
                level.playLocalSound(x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4F, (1F + (level.random.nextFloat() - level.random.nextFloat()) * .2F) * .7F, false);
            if(spawnParticles)
                level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 1D, 0D, 0D);

            ci.cancel();
        }
    }
}
