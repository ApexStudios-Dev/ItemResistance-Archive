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

import xyz.apex.forge.commonality.Mods;
import xyz.apex.forge.itemresistance.ItemResistance;

import javax.annotation.Nullable;

@GameTestHolder(Mods.ITEM_RESISTANCE)
@PrefixGameTestTemplate(false)
public final class ExplosionTests
{
	/*
		Validates that our tags work as intended
		Blows up tnt near a dropped bedrock item
		Will succeed only if the bedrock item was blown up

		Bedrock during tests has our force explode tag, which should tell the system that bedrock should explode everytime
		rather than trying to resist the explosion using its explosion resistance property
	 */
	@GameTest(template = "barrier_platform", batch = "explosionTests")
	public static void validateBedrockExplodes(GameTestHelper helper)
	{
		test(helper, new BlockPos(1, 2, 1), Blocks.BEDROCK, ItemResistance.FORCE_EXPLODE, true);
	}

	/*
		Validates that our tags work as intended
		Blows up tnt near a dropped stone item
		Will succeed only if the stone item survived the explosion

		Stone during tests has our force resist tag, which should tell the system that stone should resist the explosion everytime
		rather than trying to resist the explosion using its explosion resistance property
	 */
	@GameTest(template = "barrier_platform", batch = "explosionTests")
	public static void validateStoneResists(GameTestHelper helper)
	{
		test(helper, new BlockPos(1, 2, 1), Blocks.STONE, ItemResistance.FORCE_RESIST, false);
	}

	/*
		Validates that the mod works as a whole
		Blows up tnt near a dropped dirt item
		Will succeed only if the dirt item was blown up

		Dirt does not have any of out tags, this will validate that the explosion resistance is taken into account
	 */
	@GameTest(template = "barrier_platform", batch = "explosionTests")
	public static void validateDirtResists(GameTestHelper helper)
	{
		test(helper, new BlockPos(1, 2, 1), Blocks.DIRT, null, true);
	}

	/*
		Validates that the mod works as a whole
		Blows up tnt near a dropped obsidian item
		Will succeed only if the obsidian item survived the explosion

		Obsidian does not have any of out tags, this will validate that the explosion resistance is taken into account
	 */
	@GameTest(template = "barrier_platform", batch = "explosionTests")
	public static void validateObsididanExplodes(GameTestHelper helper)
	{
		test(helper, new BlockPos(1, 2, 1), Blocks.OBSIDIAN, null, false);
	}

	/*
		Logic behind the tests

		[If required tag not null] Fails if fails to obtain instance of ItemTag manager from ForgeRegistries, which should never happen
		[If required tag not null] Fails if specified block as item is not tagged with specified tag

		Drops block as item at given position
		Spawns detonated tnt entity 1 block above given position with fuse time of 40 ticks
		Waits 45 ticks
		Checks to see if specified block survived the explosion or was blown up
		Succeeds if mustBlowUp was true and block exploded or mustBlowUp was false and block survived
	 */
	private static void test(GameTestHelper helper, BlockPos pos, Block block, @Nullable TagKey<Item> requiredTag, boolean mustBlowUp)
	{
		if(requiredTag != null)
		{
			var itemTags = ForgeRegistries.ITEMS.tags();

			if(itemTags == null)
				throw new GameTestAssertException("ForgeRegistries.ITEMS.tags() resulted in NULL, This *SHOULD* never happen!");
			if(!itemTags.getTag(requiredTag).contains(block.asItem()))
				throw new GameTestAssertException("%s is not tagged with required ItemTag: %s".formatted(block.getName().getString(), requiredTag.location()));
		}

		var droppedItem = helper.spawnItem(block.asItem(), pos.getX(), pos.getY(), pos.getZ());
		droppedItem.setUnlimitedLifetime();
		droppedItem.setNeverPickUp();

		var tnt = helper.spawn(EntityType.TNT, pos.above());

		var delta = helper.getLevel().random.nextDouble() * (double) ((float) Math.PI * 2F);
		tnt.setDeltaMovement(-Math.sin(delta) * .02D, .2F, -Math.cos(delta) * .02D);
		tnt.setFuse(40);

		helper.runAfterDelay(45L, () -> helper.succeedIf(() -> {
			if(mustBlowUp)
				helper.assertEntityNotPresent(EntityType.ITEM, pos);
			else
				helper.assertEntityPresent(EntityType.ITEM, pos);
		}));
	}
}