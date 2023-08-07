package xyz.apex.minecraft.itemresistance.fabric.entrypoint;

import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@ApiStatus.Internal
public final class ItemResistanceModInitializer implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ItemResistance.INSTANCE.bootstrap();
    }
}
