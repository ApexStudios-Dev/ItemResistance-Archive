package xyz.apex.forge.itemresistance.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import xyz.apex.forge.commonality.init.Mods;
import xyz.apex.forge.itemresistance.ItemResistance;

@GameTestHolder(Mods.ITEM_RESISTANCE)
@PrefixGameTestTemplate(false)
public final class GameTests
{
	@GameTest(template = "bedrock_platform", batch = "tag_testing")
	public static void bedrockBlownUp(GameTestHelper test)
	{
		blowUpBlock(test, new BlockPos(1, 2, 1), Blocks.BEDROCK, ItemResistance.FORCE_EXPLODE, true);
	}

	@GameTest(template = "bedrock_platform", batch = "tag_testing")
	public static void stoneSurvived(GameTestHelper test)
	{
		blowUpBlock(test, new BlockPos(1, 2, 1), Blocks.STONE, ItemResistance.FORCE_RESIST, false);
	}

	private static boolean isFor(TagKey<Item> tag, Block block)
	{
		return isFor(ForgeRegistries.ITEMS, tag, block.asItem());
	}

	private static <T extends IForgeRegistryEntry<T>> boolean isFor(IForgeRegistry<T> registry, TagKey<T> tag, T value)
	{
		var tags = registry.tags();

		if(tags == null)
			return false;

		return tags.getTag(tag).contains(value);
	}

	private static void blowUpBlock(GameTestHelper test, BlockPos pos, Block block, TagKey<Item> requiredTag, boolean mustBlowUp)
	{
		if(!isFor(requiredTag, block))
			throw new GameTestAssertException("%s is not tagged with required ItemTag: %s".formatted(block.getName().getString(), requiredTag.location()));

		test.setBlock(pos, block);

		var tnt = test.spawn(EntityType.TNT, pos.above());
		var d0 = test.getLevel().random.nextDouble() * (double) ((float) Math.PI * 2F);
		tnt.setDeltaMovement(-Math.sin(d0) * .02D, .2F, -Math.cos(d0) * .02D);
		tnt.setFuse(40);

		test.runAfterDelay(45, () ->
				test.succeedIf(() -> {
					if(mustBlowUp)
						test.assertBlock(pos, testBlock -> testBlock == block, () -> "The Block (%s) at <%s> was Blown Up!".formatted(block.getName().getString(), pos.toShortString()));
					else
						test.assertBlock(pos, testBlock -> testBlock != block, () -> "The Block (%s) at <%s> survived the TNT Explosion!".formatted(block.getName().getString(), pos.toShortString()));
				})
		);
	}
}
