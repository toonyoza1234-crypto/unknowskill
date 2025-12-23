package daripher.skilltree.compat.attributeslib;

import com.mojang.blaze3d.vertex.PoseStack;
import daripher.skilltree.skill.PassiveSkill;
import dev.shadowsoffire.attributeslib.client.ModifierSource;
import java.util.Comparator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SkillModifierSource extends ModifierSource<PassiveSkill> {
   public SkillModifierSource(PassiveSkill skill) {
      super(AttributesLibCompatibility.SKILL_MODIFIER_TYPE, Comparator.comparing(PassiveSkill::getId), skill);
   }

   public void render(GuiGraphics graphics, Font font, int x, int y) {
      float scale = 0.5F;
      PoseStack stack = graphics.pose();
      stack.pushPose();
      stack.scale(scale, scale, 1.0F);
      stack.translate((float)x / scale, (float)y / scale, 0.0F);
      graphics.blit(((PassiveSkill)this.data).getIconTexture(), 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
      stack.popPose();
   }
}
