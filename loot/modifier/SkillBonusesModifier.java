package daripher.skilltree.loot.modifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.player.LootDuplicationBonus;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class SkillBonusesModifier extends LootModifier {
   public static final Supplier<Codec<SkillBonusesModifier>> CODEC = Suppliers.memoize(
      () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SkillBonusesModifier::new))
   );

   public SkillBonusesModifier(LootItemCondition... conditionsIn) {
      super(conditionsIn);
   }

   @NotNull
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext) {
      for (LootItemCondition condition : this.conditions) {
         if (!condition.test(lootContext)) {
            return generatedLoot;
         }
      }

      Player player = null;
      float lootMultiplier = 0.0F;

      for (LootDuplicationBonus.LootType lootType : LootDuplicationBonus.LootType.values()) {
         if (lootType.canAffect(lootContext)) {
            player = (Player)lootContext.getParam(lootType.getPlayerLootContextParam());
            lootMultiplier = getLootMultiplier(player, lootType);
         }
      }

      if (player == null) {
         return generatedLoot;
      } else if (lootMultiplier == 0.0F) {
         return generatedLoot;
      } else {
         RandomSource random = lootContext.getRandom();
         ObjectArrayList<ItemStack> newLoot = new ObjectArrayList();
         int copies = (int)lootMultiplier;
         lootMultiplier -= (float)copies;
         copies++;
         ObjectListIterator var21 = generatedLoot.iterator();

         while (var21.hasNext()) {
            ItemStack stack = (ItemStack)var21.next();
            int itemCopies = copies;
            if (random.nextFloat() < lootMultiplier) {
               itemCopies = copies + 1;
            }

            for (int i = 0; i < itemCopies; i++) {
               newLoot.add(stack.copy());
            }
         }

         return newLoot;
      }
   }

   private static float getLootMultiplier(Player player, LootDuplicationBonus.LootType lootType) {
      RandomSource random = player.getRandom();
      Map<Float, Float> multipliers = getLootMultipliers(player, lootType);
      float multiplier = 0.0F;

      for (Entry<Float, Float> entry : multipliers.entrySet()) {
         float chance;
         for (chance = entry.getValue(); chance > 1.0F; chance--) {
            multiplier += entry.getKey();
         }

         if (random.nextFloat() < chance) {
            multiplier += entry.getKey();
         }
      }

      return multiplier;
   }

   @Nonnull
   private static Map<Float, Float> getLootMultipliers(Player player, LootDuplicationBonus.LootType lootType) {
      Map<Float, Float> multipliers = new HashMap<>();

      for (LootDuplicationBonus bonus : SkillBonusHandler.getSkillBonuses(player, LootDuplicationBonus.class)) {
         if (bonus.getLootType() == lootType) {
            float chance = bonus.getChance() + multipliers.getOrDefault(bonus.getMultiplier(), 0.0F);
            multipliers.put(bonus.getMultiplier(), chance);
         }
      }

      return multipliers;
   }

   public Codec<? extends IGlobalLootModifier> codec() {
      return CODEC.get();
   }
}
