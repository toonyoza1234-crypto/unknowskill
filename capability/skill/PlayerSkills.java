package daripher.skilltree.capability.skill;

import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.skill.PassiveSkill;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSkills implements IPlayerSkills {
   private static final UUID TREE_VERSION = UUID.fromString("fd21c2a9-7ab5-4a1e-b06d-ddb87b56047f");
   private final NonNullList<PassiveSkill> skills = NonNullList.create();
   private int skillPoints;
   private boolean treeReset;

   @Override
   public NonNullList<PassiveSkill> getPlayerSkills() {
      return this.skills;
   }

   @Override
   public int getSkillPoints() {
      return this.skillPoints;
   }

   @Override
   public void setSkillPoints(int skillPoints) {
      this.skillPoints = skillPoints;
   }

   @Override
   public void grantSkillPoints(int skillPoints) {
      this.skillPoints += skillPoints;
   }

   @Override
   public boolean learnSkill(@Nonnull PassiveSkill passiveSkill) {
      if (this.skillPoints == 0) {
         return false;
      } else if (this.skills.contains(passiveSkill)) {
         return false;
      } else {
         this.skillPoints--;
         return this.skills.add(passiveSkill);
      }
   }

   @Override
   public boolean isTreeReset() {
      return this.treeReset;
   }

   @Override
   public void setTreeReset(boolean reset) {
      this.treeReset = reset;
   }

   @Override
   public void resetTree(ServerPlayer player) {
      this.skillPoints = this.skillPoints + this.getPlayerSkills().size();
      this.getPlayerSkills().forEach(skill -> skill.remove(player));
      this.getPlayerSkills().clear();
   }

   public CompoundTag serializeNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("TreeVersion", TREE_VERSION);
      tag.putInt("Points", this.skillPoints);
      tag.putBoolean("TreeReset", this.treeReset);
      ListTag skillsTag = new ListTag();
      this.skills.forEach(skill -> skillsTag.add(StringTag.valueOf(skill.getId().toString())));
      tag.put("Skills", skillsTag);
      return tag;
   }

   public void deserializeNBT(CompoundTag tag) {
      this.skills.clear();
      UUID treeVersion = tag.hasUUID("TreeVersion") ? tag.getUUID("TreeVersion") : null;
      this.skillPoints = tag.getInt("Points");
      ListTag skillsTag = tag.getList("Skills", 8);
      if (!TREE_VERSION.equals(treeVersion)) {
         this.skillPoints = this.skillPoints + skillsTag.size();
         this.treeReset = true;
      } else {
         for (Tag skillTag : skillsTag) {
            ResourceLocation skillId = new ResourceLocation(skillTag.getAsString());
            PassiveSkill passiveSkill = SkillsReloader.getSkillById(skillId);
            if (passiveSkill == null || passiveSkill.isInvalid()) {
               this.skills.clear();
               this.treeReset = true;
               this.skillPoints = this.skillPoints + skillsTag.size();
               return;
            }

            this.skills.add(passiveSkill);
         }
      }
   }
}
