package daripher.skilltree.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockItem extends BlockItem {
   public ModBlockItem(RegistryObject<Block> blockRegistryObject) {
      super((Block)blockRegistryObject.get(), new Properties());
   }
}
