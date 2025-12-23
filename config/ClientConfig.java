package daripher.skilltree.config;

import daripher.skilltree.skill.PassiveSkill;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent.Loading;

@EventBusSubscriber(
   modid = "skilltree",
   bus = Bus.MOD
)
public class ClientConfig {
   public static final ForgeConfigSpec SPEC = ClientConfig.BUILDER.build();
   private static final Builder BUILDER = new Builder();
   private static final ConfigValue<List<? extends String>> FAVORITE_SKILLS = BUILDER.defineList(
      "favorite_skills", new ArrayList(), ClientConfig::isValidSkillId
   );
   private static final ConfigValue<? extends String> FAVORITE_COLOR_HEX = BUILDER.define("favorite_color_hex", "#42B0FF", ClientConfig::isValidHexColor);
   private static final ConfigValue<Boolean> SKILL_TREE_BACKGROUND_PARALLAX = BUILDER.define("skill_tree_background_parallax", true);
   public static Set<ResourceLocation> favorite_skills;
   public static int favorite_color;
   public static boolean favorite_color_is_rainbow;
   public static boolean skill_tree_background_parallax;

   private static boolean isValidSkillId(Object o) {
      if (o instanceof String s && ResourceLocation.isValidResourceLocation(s)) {
         return true;
      }

      return false;
   }

   private static boolean isValidHexColor(Object o) {
      if (o instanceof String s) {
         if (s.equals("rainbow")) {
            return true;
         } else {
            try {
               Integer.decode(s);
               return true;
            } catch (NumberFormatException var3) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @SubscribeEvent
   static void load(Loading event) {
      if (event.getConfig().getSpec() == SPEC) {
         favorite_skills = ((List)FAVORITE_SKILLS.get()).stream().<ResourceLocation>map(ResourceLocation::new).collect(Collectors.toSet());
         favorite_color_is_rainbow = ((String)FAVORITE_COLOR_HEX.get()).equals("rainbow");
         skill_tree_background_parallax = (Boolean)SKILL_TREE_BACKGROUND_PARALLAX.get();
         if (!favorite_color_is_rainbow) {
            favorite_color = Integer.decode((String)FAVORITE_COLOR_HEX.get());
         }
      }
   }

   public static void toggleFavoriteSkill(PassiveSkill skill) {
      if (favorite_skills.contains(skill.getId())) {
         favorite_skills.remove(skill.getId());
      } else {
         favorite_skills.add(skill.getId());
      }

      FAVORITE_SKILLS.set((List)favorite_skills.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
   }
}
