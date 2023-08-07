package xyz.apex.minecraft.itemresistance.neoforge;

import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@ApiStatus.Internal
@Mod(ItemResistance.ID)
public final class ItemResistanceForgeEP
{
    public ItemResistanceForgeEP()
    {
        ItemResistance.INSTANCE.bootstrap();
    }
}
