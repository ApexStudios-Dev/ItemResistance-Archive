package xyz.apex.minecraft.itemresistance.common;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.helper.TagHelper;
import xyz.apex.minecraft.apexcore.common.lib.hook.GameRuleHooks;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;

import java.util.List;

@ApiStatus.NonExtendable
public interface ItemResistance
{
    Logger LOGGER = LogManager.getLogger();
    String ID = "itemresistance";

    ItemResistance INSTANCE = Services.singleton(ItemResistance.class);
    GameRules.Key<GameRules.BooleanValue> GAMERULE_EXPLOSIONS_DISABLED = GameRuleHooks.get().registerBoolean(ID, "explosionsDisabled", GameRules.Category.MISC, false);
    TagKey<Item> BLACK_LIST = TagHelper.itemTag(ID, "black_list");

    default void bootstrap()
    {
        registerGenerators();
    }

    private void registerGenerators()
    {
        var descriptionKey = "pack.%s.description".formatted(ID);

        ProviderTypes.LANGUAGES.addListener(ID, (provider, lookup) -> provider
                .enUS()
                    .add(descriptionKey, "ItemResistance")
                    .add(GAMERULE_EXPLOSIONS_DISABLED, "Explosions Disabled")
                    .add("%s.description".formatted(GAMERULE_EXPLOSIONS_DISABLED.getDescriptionId()), "Controls whether Blocks may drop from explosions or not.")
                .end()
        );

        ProviderTypes.registerDefaultMcMetaGenerator(ID, Component.translatable(descriptionKey));

        ProviderTypes.ITEM_TAGS.addListener(ID, (provider, lookup) -> provider.tag(BLACK_LIST));
    }

    static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities)
    {
        var itr = entities.iterator();

        while(itr.hasNext())
        {
            if(!(itr.next() instanceof ItemEntity entity))
                continue;

            var stack = entity.getItem();

            if(stack.is(BLACK_LIST))
                continue;

            var block = Block.byItem(stack.getItem());

            if(block == Blocks.AIR)
                continue;

            var pos = entity.blockPosition();
            var blockState = getBlockState(stack, block);
            var fluidState = level.getFluidState(pos);
            var resistance = explosion.damageCalculator.getBlockExplosionResistance(explosion, level, pos, blockState, fluidState);
            var power = explosion.radius * (.7F + level.random.nextFloat() * .6F);

            if(resistance.isPresent())
                power -= (resistance.get().floatValue() + .3F) * .3F;
            if(power <= 0F || !explosion.damageCalculator.shouldBlockExplode(explosion, level, pos, blockState, power))
                itr.remove();
        }
    }

    private static BlockState getBlockState(ItemStack stack, Block block)
    {
        var blockState = block.defaultBlockState();
        var blockStateTag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);

        if(blockStateTag != null)
        {
            var stateDefinition = block.getStateDefinition();

            for(var key : blockStateTag.getAllKeys())
            {
                var property = stateDefinition.getProperty(key);

                if(property == null)
                    continue;

                var value = blockStateTag.get(key).getAsString();
                blockState = updateBlockState(blockState, property, value);
            }
        }

        return blockState;
    }

    private static <T extends Comparable<T>> BlockState updateBlockState(BlockState blockState, Property<T> property, String valueIdentifier)
    {
        return property.getValue(valueIdentifier).map(value -> blockState.setValue(property, value)).orElse(blockState);
    }
}
