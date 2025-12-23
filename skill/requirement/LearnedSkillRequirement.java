package daripher.skilltree.skill.requirement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.init.PSTSkillRequirements;
import daripher.skilltree.skill.PassiveSkill;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class LearnedSkillRequirement implements SkillRequirement<LearnedSkillRequirement> {
   private ResourceLocation skillId;

   public LearnedSkillRequirement(ResourceLocation skillId) {
      this.skillId = skillId;
   }

   public boolean test(Player player) {
      if (!PlayerSkillsProvider.hasSkills(player)) {
         return false;
      } else {
         NonNullList<PassiveSkill> skills = PlayerSkillsProvider.get(player).getPlayerSkills();
         return skills.stream().map(PassiveSkill::getId).anyMatch(this.skillId::equals);
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component skillTitle = TooltipHelper.getSkillTitle(this.skillId).withStyle(Style.EMPTY.withColor(16766815));
      return Component.translatable(this.getDescriptionId(), new Object[]{skillTitle});
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<LearnedSkillRequirement> consumer) {
      editor.addLabel(0, 0, "Skill ID", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      Set<ResourceLocation> skillIDs = SkillsReloader.getSkills().keySet();
      editor.addSelectionMenu(0, 0, 200, skillIDs)
         .setValue(this.getSkillId())
         .setElementNameGetter(v -> Component.literal(v.toString()))
         .setResponder(v -> this.selectSkillId(consumer, v));
      editor.increaseHeight(19);
   }

   private void selectSkillId(Consumer<LearnedSkillRequirement> consumer, ResourceLocation id) {
      this.setSkillId(id);
      consumer.accept(this);
   }

   public void setSkillId(ResourceLocation skillId) {
      this.skillId = skillId;
   }

   public LearnedSkillRequirement copy() {
      return new LearnedSkillRequirement(this.skillId);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LearnedSkillRequirement that = (LearnedSkillRequirement)o;
         return Objects.equals(this.skillId, that.skillId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.skillId);
   }

   public ResourceLocation getSkillId() {
      return this.skillId;
   }

   @Override
   public SkillRequirement.Serializer getSerializer() {
      return (SkillRequirement.Serializer)PSTSkillRequirements.LEARNED_SKILL.get();
   }

   public static class Serializer implements SkillRequirement.Serializer {
      public SkillRequirement<?> deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation id = new ResourceLocation(json.get("skill_id").getAsString());
         return new LearnedSkillRequirement(id);
      }

      public void serialize(JsonObject json, SkillRequirement<?> requirement) {
         if (requirement instanceof LearnedSkillRequirement aRequirement) {
            json.addProperty("skill_id", aRequirement.skillId.toString());
         }
      }

      public SkillRequirement<?> deserialize(CompoundTag tag) {
         ResourceLocation id = new ResourceLocation(tag.getString("skill_id"));
         return new LearnedSkillRequirement(id);
      }

      public CompoundTag serialize(SkillRequirement<?> requirement) {
         CompoundTag tag = new CompoundTag();
         if (requirement instanceof LearnedSkillRequirement aRequirement) {
            tag.putString("skill_id", aRequirement.skillId.toString());
         }

         return tag;
      }

      public SkillRequirement<?> deserialize(FriendlyByteBuf buf) {
         ResourceLocation id = new ResourceLocation(buf.readUtf());
         return new LearnedSkillRequirement(id);
      }

      public void serialize(FriendlyByteBuf buf, SkillRequirement<?> requirement) {
         if (requirement instanceof LearnedSkillRequirement aRequirement) {
            buf.writeUtf(aRequirement.skillId.toString());
         }
      }

      @Override
      public SkillRequirement<?> createDefaultInstance() {
         return new LearnedSkillRequirement(new ResourceLocation("skilltree:hunter_1"));
      }
   }
}
