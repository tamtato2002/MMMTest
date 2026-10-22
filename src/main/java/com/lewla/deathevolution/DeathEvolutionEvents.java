package com.lewla.deathevolution;

import java.math.BigInteger;
import java.util.Locale;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DeathEvolutionEvents {
    private static final String PROGRESS_TAG = "DeathEvolutionProgress";
    private static final String DEATHS_TAG = "Deaths";

    private static final UUID HEALTH_UUID = UUID.fromString("9f6a7152-e5d8-4bc2-a5e9-a0e5b0f20001");
    private static final UUID ATTACK_UUID = UUID.fromString("9f6a7152-e5d8-4bc2-a5e9-a0e5b0f20002");
    private static final UUID ARMOR_UUID = UUID.fromString("9f6a7152-e5d8-4bc2-a5e9-a0e5b0f20003");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("9f6a7152-e5d8-4bc2-a5e9-a0e5b0f20004");
    private static final UUID SPEED_UUID = UUID.fromString("9f6a7152-e5d8-4bc2-a5e9-a0e5b0f20005");

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote) {
            return;
        }

        BigInteger deaths = getDeaths(player).add(BigInteger.ONE);
        setDeaths(player, deaths);

        // This event runs before vanilla finishes the player's death handling.
        // Enabling the gamerule here preserves inventory and XP on this death too.
        if (ModConfig.keepInventoryOnDeath) {
            player.world.getGameRules().setOrCreateGameRule("keepInventory", "true");
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (newPlayer.world.isRemote) {
            return;
        }

        // Copy persistent progression after death and after an End/dimension clone.
        NBTTagCompound oldProgress = getProgressTag(event.getOriginal()).copy();
        setProgressTag(newPlayer, oldProgress);
        applyBonuses(newPlayer, getDeaths(newPlayer), false);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) {
            return;
        }

        BigInteger deaths = getDeaths(player);
        applyBonuses(player, deaths, true);

        if (deaths.signum() > 0) {
            double attackMultiplier = 1.0D + scaledAmount(deaths, ModConfig.damageMultiplierPerDeath,
                    ModConfig.maximumDamageMultiplier - 1.0D);
            double resistanceDivisor = getResistanceDivisor(deaths);

            player.sendMessage(new TextComponentString(
                    TextFormatting.DARK_PURPLE + "[Death Evolution] "
                    + TextFormatting.LIGHT_PURPLE + "ตายสะสม "
                    + TextFormatting.YELLOW + deaths.toString()
                    + TextFormatting.LIGHT_PURPLE + " ครั้ง"));
            player.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "โบนัสถาวร: HP +" + fmt(scaledAmount(deaths, ModConfig.healthPerDeath, ModConfig.maximumAttributeBonus))
                    + " | ATK +" + fmt(scaledAmount(deaths, ModConfig.attackPerDeath, ModConfig.maximumAttributeBonus))
                    + " | Armor +" + fmt(scaledAmount(deaths, ModConfig.armorPerDeath, ModConfig.maximumAttributeBonus))
                    + " | Toughness +" + fmt(scaledAmount(deaths, ModConfig.toughnessPerDeath, ModConfig.maximumAttributeBonus))));
            player.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "โจมตี x" + fmt(attackMultiplier)
                    + " | ดาเมจที่ได้รับ ÷" + fmt(resistanceDivisor)
                    + " | ความเร็วเพิ่มขึ้นถาวร"));
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) {
            return;
        }
        applyBonuses(player, getDeaths(player), false);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote || event.getAmount() <= 0.0F) {
            return;
        }

        // Scale damage dealt by a player, including arrows/projectiles whose true source is the player.
        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource instanceof EntityPlayer && trueSource != event.getEntityLiving()) {
            EntityPlayer attacker = (EntityPlayer) trueSource;
            BigInteger deaths = getDeaths(attacker);
            if (deaths.signum() > 0) {
                double multiplier = 1.0D + scaledAmount(deaths, ModConfig.damageMultiplierPerDeath,
                        ModConfig.maximumDamageMultiplier - 1.0D);
                event.setAmount(safeDamage(event.getAmount(), multiplier));
            }
        }

        // Permanent resistance: incoming damage is divided by a value that grows with every death.
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer victim = (EntityPlayer) event.getEntityLiving();
            BigInteger deaths = getDeaths(victim);
            if (deaths.signum() > 0) {
                double divisor = getResistanceDivisor(deaths);
                event.setAmount((float) (event.getAmount() / divisor));
            }
        }
    }

    private static void applyBonuses(EntityPlayer player, BigInteger deaths, boolean refillHealth) {
        double health = scaledAmount(deaths, ModConfig.healthPerDeath, ModConfig.maximumAttributeBonus);
        double attack = scaledAmount(deaths, ModConfig.attackPerDeath, ModConfig.maximumAttributeBonus);
        double armor = scaledAmount(deaths, ModConfig.armorPerDeath, ModConfig.maximumAttributeBonus);
        double toughness = scaledAmount(deaths, ModConfig.toughnessPerDeath, ModConfig.maximumAttributeBonus);
        double speed = scaledAmount(deaths, ModConfig.speedPerDeath, ModConfig.maximumAttributeBonus);

        setModifier(player, SharedMonsterAttributes.MAX_HEALTH, HEALTH_UUID, "Death Evolution health", health, 0);
        setModifier(player, SharedMonsterAttributes.ATTACK_DAMAGE, ATTACK_UUID, "Death Evolution attack", attack, 0);
        setModifier(player, SharedMonsterAttributes.ARMOR, ARMOR_UUID, "Death Evolution armor", armor, 0);
        setModifier(player, SharedMonsterAttributes.ARMOR_TOUGHNESS, TOUGHNESS_UUID, "Death Evolution toughness", toughness, 0);
        setModifier(player, SharedMonsterAttributes.MOVEMENT_SPEED, SPEED_UUID, "Death Evolution speed", speed, 2);

        if (refillHealth) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void setModifier(EntityPlayer player, IAttribute attribute, UUID id, String name,
                                    double amount, int operation) {
        IAttributeInstance instance = player.getEntityAttribute(attribute);
        if (instance == null) {
            // Some entity/mod combinations do not register armor attributes by default.
            instance = player.getAttributeMap().registerAttribute(attribute);
        }
        instance.removeModifier(id);
        if (amount > 0.0D && !Double.isNaN(amount) && !Double.isInfinite(amount)) {
            instance.applyModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    private static BigInteger getDeaths(EntityPlayer player) {
        NBTTagCompound progress = getProgressTag(player);
        String value = progress.getString(DEATHS_TAG);
        if (value == null || value.isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            BigInteger parsed = new BigInteger(value);
            return parsed.signum() < 0 ? BigInteger.ZERO : parsed;
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }

    private static void setDeaths(EntityPlayer player, BigInteger deaths) {
        NBTTagCompound progress = getProgressTag(player);
        progress.setString(DEATHS_TAG, deaths.toString());
    }

    private static NBTTagCompound getProgressTag(EntityPlayer player) {
        NBTTagCompound root = player.getEntityData();
        NBTTagCompound persisted = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) {
            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        NBTTagCompound progress = persisted.getCompoundTag(PROGRESS_TAG);
        if (!persisted.hasKey(PROGRESS_TAG, 10)) {
            persisted.setTag(PROGRESS_TAG, progress);
        }
        return progress;
    }

    private static void setProgressTag(EntityPlayer player, NBTTagCompound progress) {
        NBTTagCompound root = player.getEntityData();
        NBTTagCompound persisted = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persisted.setTag(PROGRESS_TAG, progress.copy());
        root.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
    }


    private static double getResistanceDivisor(BigInteger deaths) {
        // Armor and toughness also contribute to the player's effective mitigation.
        // This event-based layer works consistently for players even when a modpack
        // changes how vanilla armor attributes are interpreted.
        double perDeath = ModConfig.damageResistancePerDeath
                + Math.max(0.0D, ModConfig.armorPerDeath) * 0.01D
                + Math.max(0.0D, ModConfig.toughnessPerDeath) * 0.005D;
        return 1.0D + scaledAmount(deaths, perDeath, ModConfig.maximumResistanceScaling);
    }

    private static double scaledAmount(BigInteger deaths, double perDeath, double cap) {
        if (deaths.signum() <= 0 || perDeath <= 0.0D || cap <= 0.0D) {
            return 0.0D;
        }
        double count = deaths.doubleValue();
        if (Double.isInfinite(count) || Double.isNaN(count) || count > cap / perDeath) {
            return cap;
        }
        return Math.min(cap, count * perDeath);
    }

    private static float safeDamage(float baseDamage, double multiplier) {
        double result = (double) baseDamage * multiplier;
        if (Double.isNaN(result) || result <= 0.0D) {
            return baseDamage;
        }
        return (float) Math.min(result, 1.0E30D);
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
