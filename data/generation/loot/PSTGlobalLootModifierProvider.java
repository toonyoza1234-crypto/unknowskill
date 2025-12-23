package daripher.skilltree.data.generation.loot;

import daripher.skilltree.loot.modifier.SkillBonusesModifier;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class PSTGlobalLootModifierProvider extends GlobalLootModifierProvider {
   public PSTGlobalLootModifierProvider(DataGenerator generator) {
      super(generator.getPackOutput(), "skilltree");
   }

   protected void start() {
      this.add("skill_bonuses", new SkillBonusesModifier());
   }
}
