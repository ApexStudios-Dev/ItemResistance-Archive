package xyz.apex.minecraft.itemresistance.neoforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.neoforge.lib.EventBusHelper;
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

        EventBusHelper.addListener(MinecraftForge.EVENT_BUS, ExplosionEvent.Detonate.class, event -> ItemResistance.onExplosionDetonate(
                event.getLevel(),
                event.getExplosion(),
                event.getAffectedEntities()
        ));
    }
}
