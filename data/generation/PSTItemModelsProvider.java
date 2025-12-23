package daripher.skilltree.data.generation;

import daripher.skilltree.init.PSTItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PSTItemModelsProvider extends ItemModelProvider {
   public PSTItemModelsProvider(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
      super(dataGenerator.getPackOutput(), "skilltree", existingFileHelper);
   }

   protected void registerModels() {
      this.basicItem((Item)PSTItems.AMNESIA_SCROLL.get());
      this.basicItem((Item)PSTItems.WISDOM_SCROLL.get());
   }
}
