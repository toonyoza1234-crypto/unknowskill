package daripher.skilltree.data.reloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.data.serializers.SkillBonusSerializer;
import daripher.skilltree.data.serializers.SkillRequirementSerializer;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.requirement.SkillRequirement;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceLocation.Serializer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
   modid = "skilltree"
)
public class SkillsReloader extends SimpleJsonResourceReloadListener {
   public static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(ResourceLocation.class, new Serializer())
      .registerTypeAdapter(SkillBonus.class, new SkillBonusSerializer())
      .registerTypeAdapter(SkillRequirement.class, new SkillRequirementSerializer())
      .registerTypeAdapter(MutableComponent.class, new net.minecraft.network.chat.Component.Serializer())
      .setPrettyPrinting()
      .create();
   private static final Map<ResourceLocation, PassiveSkill> SKILLS = new HashMap<>();

   public SkillsReloader() {
      super(GSON, "skills");
   }

   @SubscribeEvent
   public static void reloadSkills(AddReloadListenerEvent event) {
      event.addListener(new SkillsReloader());
   }

   public static Map<ResourceLocation, PassiveSkill> getSkills() {
      return SKILLS;
   }

   @Nullable
   public static PassiveSkill getSkillById(ResourceLocation id) {
      return SKILLS.get(id);
   }

   public static void loadFromByteBuf(FriendlyByteBuf buf) {
      SKILLS.clear();
      NetworkHelper.readPassiveSkills(buf).forEach(s -> SKILLS.put(s.getId(), s));
   }

   protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
      SKILLS.clear();
      map.forEach(this::readSkill);
   }

   protected void readSkill(ResourceLocation id, JsonElement json) {
      try {
         PassiveSkill skill = (PassiveSkill)GSON.fromJson(json, PassiveSkill.class);
         SKILLS.put(skill.getId(), skill);
      } catch (Exception var4) {
         SkillTreeMod.LOGGER.error("Couldn't load passive skill {}", id);
         var4.printStackTrace();
      }
   }
}
