package daripher.skilltree.data.generation;

import daripher.skilltree.init.PSTTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSTItemTagsProvider extends ItemTagsProvider {
   public static final ResourceLocation KNIVES = new ResourceLocation("forge", "tools/knives");

   public PSTItemTagsProvider(
      DataGenerator dataGenerator, CompletableFuture<Provider> provider, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper fileHelper
   ) {
      super(dataGenerator.getPackOutput(), provider, blockTagsProvider.contentsGetter(), "skilltree", fileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
      this.tag(Items.TOOLS).addOptionalTag(KNIVES);
      this.tag(PSTTags.Items.MELEE_WEAPON).addTags(new TagKey[]{ItemTags.SWORDS, ItemTags.AXES, Items.TOOLS_TRIDENTS});
      this.tag(PSTTags.Items.RANGED_WEAPON).addTags(new TagKey[]{Items.TOOLS_BOWS, Items.TOOLS_CROSSBOWS});
      this.tag(PSTTags.Items.LEATHER_ARMOR)
         .add(
            new Item[]{
               net.minecraft.world.item.Items.LEATHER_BOOTS,
               net.minecraft.world.item.Items.LEATHER_CHESTPLATE,
               net.minecraft.world.item.Items.LEATHER_HELMET,
               net.minecraft.world.item.Items.LEATHER_LEGGINGS
            }
         );
   }
}
