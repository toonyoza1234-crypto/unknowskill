package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRecipeTypes;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public class RecipeUnlockBonus implements SkillBonus<RecipeUnlockBonus> {
   @Nonnull
   private ResourceLocation recipeId;

   public RecipeUnlockBonus(@Nonnull ResourceLocation recipeId) {
      this.recipeId = recipeId;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.RECIPE_UNLOCK.get();
   }

   public RecipeUnlockBonus copy() {
      return new RecipeUnlockBonus(this.recipeId);
   }

   public RecipeUnlockBonus multiply(double multiplier) {
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof RecipeUnlockBonus otherBonus ? Objects.equals(otherBonus.recipeId, this.recipeId) : false;
   }

   @Override
   public SkillBonus<RecipeUnlockBonus> merge(SkillBonus<?> other) {
      return this;
   }

   @Override
   public MutableComponent getTooltip() {
      Component recipeTooltip = TooltipHelper.getRecipeTooltip(this.recipeId);
      Style recipeTooltipStyle = TooltipHelper.getItemBonusStyle();
      Component var4 = Component.literal(recipeTooltip.getString()).withStyle(recipeTooltipStyle);
      MutableComponent tooltip = Component.translatable(this.getDescriptionId(), new Object[]{var4});
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return true;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<RecipeUnlockBonus> consumer) {
      editor.addLabel(0, 0, "Recipe ID", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      ClientLevel clientLevel = Minecraft.getInstance().level;
      Objects.requireNonNull(clientLevel);
      RecipeManager recipesManager = clientLevel.getRecipeManager();
      List<ResourceLocation> artisanRecipes = recipesManager.getAllRecipesFor(PSTRecipeTypes.WORKBENCH).stream().<ResourceLocation>map(Recipe::getId).toList();
      editor.addSelectionMenu(0, 0, 200, artisanRecipes).setValue(this.recipeId).setResponder(id -> this.selectRecipeId(editor, consumer, id));
      editor.increaseHeight(19);
   }

   private void selectRecipeId(SkillTreeEditor editor, Consumer<RecipeUnlockBonus> consumer, ResourceLocation id) {
      this.setRecipeId(id);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   public void setRecipeId(@Nonnull ResourceLocation id) {
      this.recipeId = id;
   }

   @Nonnull
   public ResourceLocation getRecipeId() {
      return this.recipeId;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         RecipeUnlockBonus that = (RecipeUnlockBonus)obj;
         return Objects.equals(this.recipeId, that.recipeId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.recipeId);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public RecipeUnlockBonus deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation recipeId = new ResourceLocation(json.get("recipe_id").getAsString());
         return new RecipeUnlockBonus(recipeId);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof RecipeUnlockBonus aBonus) {
            json.addProperty("recipe_id", aBonus.recipeId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public RecipeUnlockBonus deserialize(CompoundTag tag) {
         ResourceLocation recipeId = new ResourceLocation(tag.getString("recipe_id"));
         return new RecipeUnlockBonus(recipeId);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof RecipeUnlockBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putString("recipe_id", aBonus.recipeId.toString());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public RecipeUnlockBonus deserialize(FriendlyByteBuf buf) {
         ResourceLocation recipeId = new ResourceLocation(buf.readUtf());
         return new RecipeUnlockBonus(recipeId);
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof RecipeUnlockBonus aBonus) {
            buf.writeUtf(aBonus.recipeId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new RecipeUnlockBonus(new ResourceLocation("unknown_recipe"));
      }
   }
}
