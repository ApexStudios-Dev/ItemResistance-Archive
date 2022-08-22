package xyz.apex.forge.itemresistance.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import xyz.apex.forge.itemresistance.ItemResistance;

@Mixin(Explosion.class)
public abstract class ExplosionMixin
{
	// register shadow fields/methods, mixin fills them in with correct values when needed
	@Shadow @Final private Level level;
	@Shadow @Final private double x;
	@Shadow @Final private double y;
	@Shadow @Final private double z;
	@Shadow public abstract void clearToBlow();

	// register mixin injection to replace default explosion logic
	@Inject(
			method = "finalizeExplosion",
			at = @At("HEAD"),
			cancellable = true
	)
	private void finalizeExplosion(boolean isClient, CallbackInfo ci)
	{
		// check if gamerule is enabled
		if(level.getGameRules().getBoolean(ItemResistance.GAMERULE_EXPLOSIONS_DISABLED))
		{
			// clear out the block pos's to blow up
			clearToBlow();

			// do default effects, sound & particles
			if(level.isClientSide)
				level.playLocalSound(x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4F, (1F + (level.random.nextFloat() - level.random.nextFloat()) * .2F) * .7F, false);
			if(isClient)
				level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 1D, 0D, 0D);

			// cancel the method call
			// dont continue on and invoke the rest of finalizeExplosion
			ci.cancel();
		}
	}
}