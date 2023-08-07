package xyz.apex.minecraft.itemresistance.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;

@ApiStatus.NonExtendable
public interface ItemResistance
{
    Logger LOGGER = LogManager.getLogger();
    String ID = "itemresistance";

    ItemResistance INSTANCE = Services.singleton(ItemResistance.class);

    default void bootstrap()
    {
    }
}
