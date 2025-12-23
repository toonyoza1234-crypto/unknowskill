package daripher.skilltree.skill.bonus;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface SkillBonus<T extends SkillBonus<T>> extends Comparable<SkillBonus<?>> {
   default void onSkillLearned(ServerPlayer player, boolean firstTime) {
   }

   default void onSkillRemoved(ServerPlayer player) {
   }

   boolean canMerge(SkillBonus<?> var1);

   default boolean sameBonus(SkillBonus<?> other) {
      return this.canMerge(other);
   }

   SkillBonus<T> merge(SkillBonus<?> var1);

   SkillBonus<T> copy();

   T multiply(double var1);

   SkillBonus.Serializer getSerializer();

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.SKILL_BONUSES.get().getKey(this.getSerializer());
      return "skill_bonus.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   MutableComponent getTooltip();

   default void gatherInfo(Consumer<MutableComponent> consumer) {
      TooltipHelper.consumeTranslated(this.getDescriptionId() + ".info", consumer);
   }

   boolean isPositive();

   void addEditorWidgets(SkillTreeEditor var1, int var2, Consumer<T> var3);

   default int compareTo(@NotNull SkillBonus<?> o) {
      if (this.isPositive() != o.isPositive()) {
         return this.isPositive() ? -1 : 1;
      } else {
         String regex = "\\+?-?[0-9]+\\.?[0-9]?%? ";
         String as = this.getTooltip().getString().replaceAll(regex, "");
         String bs = o.getTooltip().getString().replaceAll(regex, "");
         return as.compareTo(bs);
      }
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<SkillBonus<?>> {
      SkillBonus<?> createDefaultInstance();
   }

   public static enum Target {
      PLAYER,
      ENEMY;

      public String getName() {
         return this.name().toLowerCase();
      }

      public static SkillBonus.Target fromName(String name) {
         return valueOf(name.toUpperCase());
      }
   }
}
