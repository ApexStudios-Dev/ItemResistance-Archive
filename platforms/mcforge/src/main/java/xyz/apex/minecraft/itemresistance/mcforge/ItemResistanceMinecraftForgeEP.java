package xyz.apex.minecraft.itemresistance.mcforge;

import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@ApiStatus.Internal
@Mod(ItemResistance.ID)
public final class ItemResistanceMinecraftForgeEP
{
    public ItemResistanceMinecraftForgeEP()
    {
        ItemResistance.INSTANCE.bootstrap();
    }
}
