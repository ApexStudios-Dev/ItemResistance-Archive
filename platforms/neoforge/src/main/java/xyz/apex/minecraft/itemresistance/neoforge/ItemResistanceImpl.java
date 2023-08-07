package xyz.apex.minecraft.itemresistance.neoforge;

import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.neoforge.lib.EventBuses;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@ApiStatus.Internal
public final class ItemResistanceImpl implements ItemResistance
{
    @Override
    public void bootstrap()
    {
        ItemResistance.super.bootstrap();
        EventBuses.registerForJavaFML();
    }
}
