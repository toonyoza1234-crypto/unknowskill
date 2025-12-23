package daripher.skilltree.skill.bonus.predicate.effect;

public enum EffectType {
   NEUTRAL,
   HARMFUL,
   BENEFICIAL,
   ANY;

   public String getName() {
      return this.name().toLowerCase();
   }

   public static EffectType fromName(String name) {
      return valueOf(name.toUpperCase());
   }

   public String getDescriptionId() {
      return "effect_type." + this.getName();
   }
}
