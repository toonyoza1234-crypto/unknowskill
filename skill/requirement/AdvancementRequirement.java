package daripher.skilltree.skill.requirement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTSkillRequirements;
import daripher.skilltree.mixin.ClientAdvancementsAccessor;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class AdvancementRequirement implements SkillRequirement<AdvancementRequirement> {
   private ResourceLocation advancementId;

   public AdvancementRequirement(ResourceLocation advancementId) {
      this.advancementId = advancementId;
   }

   public boolean test(Player player) {
      if (player.level().isClientSide) {
         LocalPlayer localPlayer = (LocalPlayer)player;
         ClientAdvancements advancements = localPlayer.connection.getAdvancements();
         ClientAdvancementsAccessor advancementsAccessor = (ClientAdvancementsAccessor)advancements;
         Advancement advancement = advancements.getAdvancements().get(this.advancementId);
         AdvancementProgress progress = advancementsAccessor.getProgress().get(advancement);
         return progress == null ? false : progress.getPercent() >= 1.0F;
      } else {
         ServerPlayer serverPlayer = (ServerPlayer)player;
         MinecraftServer server = serverPlayer.level().getServer();
         if (server == null) {
            return false;
         } else {
            ServerAdvancementManager advancementManager = server.getAdvancements();
            PlayerAdvancements advancements = serverPlayer.getAdvancements();
            Advancement advancement = advancementManager.getAdvancement(this.advancementId);
            return advancement == null ? false : advancements.getOrStartProgress(advancement).getPercent() >= 1.0F;
         }
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String advancementPath = this.advancementId.getPath().replaceAll("/", ".");
      String advancamentDescriptionId = "advancements.%s.title".formatted(advancementPath);
      Component advancementTooltip = Component.translatable(advancamentDescriptionId).withStyle(Style.EMPTY.withColor(16766815));
      return Component.translatable(this.getDescriptionId(), new Object[]{advancementTooltip});
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<AdvancementRequirement> consumer) {
      LocalPlayer localPlayer = Minecraft.getInstance().player;
      Objects.requireNonNull(localPlayer);
      ClientAdvancements advancements = localPlayer.connection.getAdvancements();
      editor.addLabel(0, 0, "Advancement ID", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      List<ResourceLocation> advancementIds = advancements.getAdvancements().getAllAdvancements().stream().<ResourceLocation>map(Advancement::getId).toList();
      editor.addSelectionMenu(0, 0, 200, advancementIds)
         .setValue(this.getAdvancementId())
         .setElementNameGetter(v -> Component.literal(v.toString()))
         .setResponder(v -> this.selectAdvancementId(consumer, v));
      editor.increaseHeight(19);
   }

   private void selectAdvancementId(Consumer<AdvancementRequirement> consumer, ResourceLocation id) {
      this.setAdvancementId(id);
      consumer.accept(this);
   }

   public void setAdvancementId(ResourceLocation advancementId) {
      this.advancementId = advancementId;
   }

   public AdvancementRequirement copy() {
      return new AdvancementRequirement(this.advancementId);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AdvancementRequirement that = (AdvancementRequirement)o;
         return Objects.equals(this.advancementId, that.advancementId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.advancementId);
   }

   public ResourceLocation getAdvancementId() {
      return this.advancementId;
   }

   @Override
   public SkillRequirement.Serializer getSerializer() {
      return (SkillRequirement.Serializer)PSTSkillRequirements.ADVANCEMENT.get();
   }

   public static class Serializer implements SkillRequirement.Serializer {
      public SkillRequirement<?> deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation id = new ResourceLocation(json.get("advancement").getAsString());
         return new AdvancementRequirement(id);
      }

      public void serialize(JsonObject json, SkillRequirement<?> requirement) {
         if (requirement instanceof AdvancementRequirement aRequirement) {
            json.addProperty("advancement", aRequirement.advancementId.toString());
         }
      }

      public SkillRequirement<?> deserialize(CompoundTag tag) {
         ResourceLocation id = new ResourceLocation(tag.getString("advancement"));
         return new AdvancementRequirement(id);
      }

      public CompoundTag serialize(SkillRequirement<?> requirement) {
         CompoundTag tag = new CompoundTag();
         if (requirement instanceof AdvancementRequirement aRequirement) {
            tag.putString("advancement", aRequirement.advancementId.toString());
         }

         return tag;
      }

      public SkillRequirement<?> deserialize(FriendlyByteBuf buf) {
         ResourceLocation id = new ResourceLocation(buf.readUtf());
         return new AdvancementRequirement(id);
      }

      public void serialize(FriendlyByteBuf buf, SkillRequirement<?> requirement) {
         if (requirement instanceof AdvancementRequirement aRequirement) {
            buf.writeUtf(aRequirement.advancementId.toString());
         }
      }

      @Override
      public SkillRequirement<?> createDefaultInstance() {
         return new AdvancementRequirement(new ResourceLocation("minecraft:adventure/hero_of_the_village"));
      }
   }
}
