package daripher.skilltree.data.generation;

import daripher.skilltree.init.PSTBlocks;
import java.util.function.Function;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class PSTBlockStatesProvider extends BlockStateProvider {
   public PSTBlockStatesProvider(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
      super(dataGenerator.getPackOutput(), "skilltree", existingFileHelper);
   }

   protected void registerStatesAndModels() {
      this.simpleBlockState(PSTBlocks.WORKBENCH, this::orientableModelWithBottom);
   }

   private void simpleBlockState(RegistryObject<Block> blockRegistryObject, Function<ResourceLocation, ModelFile> modelFileProvider) {
      ResourceLocation blockId = blockRegistryObject.getId();
      ModelFile modelFile = modelFileProvider.apply(blockId);
      ConfiguredModel configuredModel = new ConfiguredModel(modelFile);
      this.getVariantBuilder((Block)blockRegistryObject.get()).partialState().setModels(new ConfiguredModel[]{configuredModel});
      this.itemModels().withExistingParent(blockId.toString(), this.modLoc("block/" + blockId.getPath()));
   }

   private ModelFile orientableModelWithBottom(ResourceLocation blockId) {
      return this.models()
         .orientableWithBottom(
            blockId.toString(), subTexture(blockId, "side"), subTexture(blockId, "front"), subTexture(blockId, "bottom"), subTexture(blockId, "top")
         );
   }

   @NotNull
   private static ResourceLocation subTexture(ResourceLocation blockId, String name) {
      return blockId.withPath("block/" + blockId.getPath() + "_" + name);
   }
}
