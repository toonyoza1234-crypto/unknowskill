package daripher.skilltree.data.generation;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class PSTBlockTagsProvider extends BlockTagsProvider {
   public PSTBlockTagsProvider(DataGenerator dataGenerator, CompletableFuture<Provider> provider, ExistingFileHelper fileHelper) {
      super(dataGenerator.getPackOutput(), provider, "skilltree", fileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
   }
}
