package xyz.apex.minecraft.itemresistance.fabric.entrypoint;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ApexDataProvider;
import xyz.apex.minecraft.itemresistance.common.ItemResistance;

@ApiStatus.Internal
public final class ItemResistanceDataGeneratorEntrypoint implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator)
    {
        ApexDataProvider.register(ItemResistance.ID, func -> generator.createPack().addProvider(func::apply));
    }
}
