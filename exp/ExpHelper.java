package daripher.skilltree.exp;

import net.minecraft.world.entity.player.Player;

public class ExpHelper {
   public static long getPlayerExp(Player player) {
      return (long)(levelsToXP(player.experienceLevel) + Math.round(player.experienceProgress * (float)player.getXpNeededForNextLevel()));
   }

   private static int levelsToXP(int levels) {
      if (levels <= 16) {
         return (int)(Math.pow((double)levels, 2.0) + (double)(6 * levels));
      } else {
         return levels <= 31
            ? (int)(2.5 * Math.pow((double)levels, 2.0) - 40.5 * (double)levels + 360.0)
            : (int)(4.5 * Math.pow((double)levels, 2.0) - 162.5 * (double)levels + 2220.0);
      }
   }
}
