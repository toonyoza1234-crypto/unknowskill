package daripher.skilltree.event;

import daripher.skilltree.config.ServerConfig;
import net.minecraftforge.event.GrindstoneEvent.OnTakeItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "skilltree"
)
public class PSTEvents {
   @SubscribeEvent
   public static void applyGrindstoneExpPenalty(OnTakeItem event) {
      event.setXp((int)((double)event.getXp() * ServerConfig.grindstone_exp_multiplier));
   }
}
