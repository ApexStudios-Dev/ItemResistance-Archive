package xyz.apex.forge.itemresistance;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import xyz.apex.forge.commonality.Mods;
import xyz.apex.forge.commonality.tags.ItemTags;
import xyz.apex.forge.commonality.trust.TrustManager;

@Mod(Mods.ITEM_RESISTANCE)
public final class ItemResistance
{
	// translation key for text shown when explosion is denied
	public static final String TXT_EXPlOSION_DISABLED = "txt.%s.explosions_disabled".formatted(Mods.ITEM_RESISTANCE);
	// tag to mark blocks as always exploding
	// if bedrock has this tag, tnt can blow it up in item form
	public static final TagKey<Item> FORCE_EXPLODE = ItemTags.tag(Mods.ITEM_RESISTANCE, "force_explode");
	// tag to mark blocks always resisting
	// if dirt has this tag, tnt can not blow it up in item form
	public static final TagKey<Item> FORCE_RESIST = ItemTags.tag(Mods.ITEM_RESISTANCE, "force_resist");
	// register new gamerule that disables explosions
	// defaults to false (do not deny explosions)
	// if set to true explosions are denied
	public static final GameRules.Key<GameRules.BooleanValue> GAMERULE_EXPLOSIONS_DISABLED = GameRules.register("%s:explosionsDisabled".formatted(Mods.ITEM_RESISTANCE), GameRules.Category.DROPS, GameRules.BooleanValue.create(false));

	public ItemResistance()
	{
		// ensure downloaded from trusted source
		TrustManager.throwIfUntrusted(Mods.ITEM_RESISTANCE);
		// register event listener for detonate events
		MinecraftForge.EVENT_BUS.addListener(this::onExplosionDetonate);
		// register event listener for data generation
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onGatherData);
	}

	private void onGatherData(GatherDataEvent event)
	{
		var generator = event.getGenerator();
		var fileHelper = event.getExistingFileHelper();
		var output = generator.getPackOutput();
		var lookupProvider = event.getLookupProvider();

		var includeClient = event.includeClient();
		var includeServer = event.includeServer();

		var blockTagsProvider = generator.addProvider(includeServer, new BlockTagsProvider(output, lookupProvider, Mods.ITEM_RESISTANCE, fileHelper) {
			@Override
			protected void addTags(HolderLookup.Provider provider)
			{
				// NOOP : Dont create vanilla block tags
			}
		});

		// register block tags generator
		generator.addProvider(includeServer, new ItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), Mods.ITEM_RESISTANCE, event.getExistingFileHelper()) {
			@Override
			protected void addTags(HolderLookup.Provider provider)
			{
				// generate empty tags
				tag(FORCE_EXPLODE);
				tag(FORCE_RESIST);
			}
		});

		// register language generator (en_us)
		generator.addProvider(includeClient, new LanguageProvider(output, Mods.ITEM_RESISTANCE, "en_us") {
			@Override
			protected void addTranslations()
			{
				add(TXT_EXPlOSION_DISABLED, "Explosions have been disabled via the GameRule: %s");
				add(GAMERULE_EXPLOSIONS_DISABLED.getDescriptionId(), "ExplosionsDisabled");
			}
		});
	}

	public static float getExplosionSize(Explosion explosion)
	{
		// explosion size is private final field inside of
		// net.minecraft.world.Explosion
		// we use reflection to obtain the value of this field
		// ObfuscationReflectionHelper since Minecraft code is obfuscated outside of development
		// (maps human readable name to obfuscated name)
		Float explosionSize = ObfuscationReflectionHelper.getPrivateValue(Explosion.class, explosion, "f_46017_");
		// getPrivateValue() is marked nullable
		// simple null check to default to 0F
		return explosionSize == null ? 0F : explosionSize;
	}

	private void onExplosionDetonate(ExplosionEvent.Detonate event)
	{
		var world = event.getLevel();
		var explosion = event.getExplosion();
		var explosionSize = getExplosionSize(explosion);
		var itr = event.getAffectedEntities().iterator();

		// iterate over all entities
		while(itr.hasNext())
		{
			var entity = itr.next();

			// check if entity is dropped item stack
			if(entity instanceof ItemEntity itemEntity)
			{
				var stack = itemEntity.getItem();

				// if block marked as always resisting
				// keep the exploded item
				if(stack.is(FORCE_RESIST))
				{
					itr.remove();
					continue;
				}

				// if block marked as always exploding
				// explode the item
				if(stack.is(FORCE_EXPLODE))
					continue;

				var item = stack.getItem();

				// check if dropped item is a block
				if(item instanceof BlockItem blockItem)
				{
					var block = blockItem.getBlock();

					// if we get here, block does not have our custom tags
					// check for block resistance instead
					// we keep item if block would not normally blow up
					var resistance = block.getExplosionResistance();

					// calculate explosion size
					// same calculation as in
					// net.minecraft.world.Explosion#doExplosionA:L148
					var f = explosionSize * (.7F + world.random.nextFloat() * .6F);

					// decrement explosion size
					// by blocks explosion resistance amount
					// same calculation as in
					// same calculation as in net.minecraft.world.Explosion#doExplosionA:L159
					if(resistance > 0F)
						f -= (resistance + .3F) * .3f;

					// if explosion size is less than 0
					// that means this block can not be exploded
					// remove entity from list
					// of entities to be exploded
					if(f <= 0F)
						itr.remove();
				}
			}
		}
	}
}
