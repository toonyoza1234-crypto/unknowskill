package daripher.skilltree.init;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PSTCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "skilltree");
   public static final MutableComponent TAB_TITLE = Component.translatable("itemGroup.skilltree");
   public static final Supplier<ItemStack> TAB_ICON_STACK = () -> new ItemStack((ItemLike)PSTItems.AMNESIA_SCROLL.get());

   private static void collectModItems(Output output) {
      PSTItems.REGISTRY.getEntries().stream().map(RegistryObject::get).forEach(output::accept);
   }

   static {
      REGISTRY.register(
         "skilltree", () -> CreativeModeTab.builder().title(TAB_TITLE).icon(TAB_ICON_STACK).displayItems((params, output) -> collectModItems(output)).build()
      );
   }
}
