package com.lewla.deathevolution;

import net.minecraftforge.common.config.Config;

@Config(modid = DeathEvolution.MODID, name = "deathevolution")
public final class ModConfig {
    private ModConfig() { }

    @Config.Comment("If true, the mod turns on the world's keepInventory gamerule when a player dies. The gamerule stays enabled until you change it manually.")
    public static boolean keepInventoryOnDeath = true;

    @Config.Comment("Maximum attribute bonus used to avoid non-finite values breaking the game at astronomically high death counts. The death counter itself has no configured cap.")
    public static double maximumAttributeBonus = 1.0E12D;

    @Config.Comment("Maximum damage multiplier used by the combat event, to avoid Infinity/NaN damage values.")
    public static double maximumDamageMultiplier = 1.0E30D;

    @Config.Comment("Maximum resistance scaling input used by the combat event.")
    public static double maximumResistanceScaling = 1.0E30D;

    @Config.Comment("Maximum health bonus added for each death.")
    public static double healthPerDeath = 2.0D;

    @Config.Comment("Maximum attack-damage bonus added for each death.")
    public static double attackPerDeath = 1.0D;

    @Config.Comment("Armor bonus added for each death.")
    public static double armorPerDeath = 1.0D;

    @Config.Comment("Armor-toughness bonus added for each death.")
    public static double toughnessPerDeath = 0.5D;

    @Config.Comment("Movement-speed multiplier bonus added for each death. Operation 2 means 0.01 adds about 1% to the base speed.")
    public static double speedPerDeath = 0.01D;

    @Config.Comment("Extra damage multiplier added for each death (0.25 means +25% per death before normal armor/effects are calculated).")
    public static double damageMultiplierPerDeath = 0.25D;

    @Config.Comment("Damage received is divided by (1 + deaths * this value), in addition to armor/toughness. 0.05 means 100 deaths divides incoming damage by 6.")
    public static double damageResistancePerDeath = 0.05D;

    // ---- v1.1.0: Luck Evolution ----

    @Config.Comment("Luck attribute bonus added for each death. Luck improves Loot Table results that support luck. Note: Minecraft clamps the Luck attribute to 1024.")
    public static double luckPerDeath = 1.0D;

    // ---- v1.1.0: Reflection Evolution ----

    @Config.Comment("Master switch for reflection. If false, neither Reflection nor Anti-Skill reflects any damage.")
    public static boolean reflectionEnabled = true;

    @Config.Comment("Fraction of damage reflected per death. 0.10 means +10% per death (10 deaths = 100% of the hit sent back).")
    public static double reflectPercentPerDeath = 0.10D;

    @Config.Comment("Safety cap for the total reflect multiplier (1.0 = 100% of the hit). Prevents Infinity/NaN at astronomically high death counts.")
    public static double maximumReflectMultiplier = 1.0E30D;

    @Config.Comment("If true, reflected damage ignores the attacker's armor. If false, the attacker's armor applies normally.")
    public static boolean reflectBypassesArmor = true;

    @Config.Comment("If true, reflection is based on the damage before the player's Death Evolution resistance is applied. If false, it uses the reduced damage the player actually takes.")
    public static boolean reflectUsesRawDamage = true;

    @Config.Comment("Entity registry names that never receive reflected damage, e.g. minecraft:ender_dragon. One entry per line.")
    public static String[] reflectEntityBlacklist = new String[0];

    // ---- v1.1.0: Anti-Skill ----

    @Config.Comment("Also reflect non-melee, non-projectile damage that names an attacker (magic, explosions, unblockable/absolute damage and modded skill damage sent through Forge's damage system). Skills that only apply effects without dealing damage are NOT detected.")
    public static boolean antiSkillEnabled = true;

    @Config.Comment("Extra multiplier applied to reflected skill damage on top of the normal reflect percent (1.0 = same as normal reflection).")
    public static double antiSkillReflectMultiplier = 1.0D;

    @Config.Comment("Extra damage type names (DamageSource.getDamageType) always treated as skill damage. Use this for modded skills whose damage type you know. One entry per line.")
    public static String[] antiSkillExtraDamageTypes = new String[0];
}
