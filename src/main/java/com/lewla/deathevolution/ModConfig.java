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
}
