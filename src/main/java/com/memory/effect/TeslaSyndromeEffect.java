package com.memory.effect;

import net.minecraft.client.font.UnihexFont;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class TeslaSyndromeEffect extends StatusEffect {
    protected TeslaSyndromeEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    private static final int DEFAULT_COOLDOWN = 60;
    private static final double MINIMUM_CHANCE = 0.01;
    private static final double WET_PENALTY = 0.1;
    private static final double BASE_LIGHTNING_CHANCE = 0.05;
    private static final double ON_OPEN_AIR_PENALTY = 0.1;
    private static final double RAIN_PENALTY = 0.05;
    private static final double THUNDER_PENALTY = 0.1;
    private static final double MIN_HEIGHT_PENALTY = 0.02;
    private static final double MAX_HEIGHT_PENALTY = 0.5;
    private static final double IRON_HELMET_PENALTY = 0.2;
    private static final double GOLD_HELMET_PENALTY = 0.4;
    private static final double LEATHER_HELMET_BONUS = -0.05;
    private static final double DIAMOND_HELMET_BONUS = -0.2;
    private static final double CHAINMAIL_HELMET_PENALTY = 0.15;
    private static final double NETHERITE_HELMET_BONUS = -0.3;

    private static final int[] COOLDOWN_LEVELS = {50, 40, 30, 25, 20, 15, 10, 5, 4, 3, 2, 1};

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return true;
        }
        if (entity.age % getCooldown(amplifier) == 0) {
            if (shouldLightingStrike(world, entity, amplifier)) {
                summonLighting(world, entity);
            }
        }

        return super.applyUpdateEffect(world, entity, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
    private static boolean shouldLightingStrike(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.getPos().getY() < 20) {
            return false;
        }
        double chance = BASE_LIGHTNING_CHANCE;
        if (entity.isSubmergedInWater()){
            return false;
        }
        if (world.isSkyVisible(entity.getBlockPos())){
            chance += ON_OPEN_AIR_PENALTY;
        }
        if (entity.isWet()){
            chance += WET_PENALTY;
        }
        if (world.hasRain(entity.getBlockPos())){
            chance += RAIN_PENALTY;
        }
        if (world.isThundering()){
            chance += THUNDER_PENALTY;
        }

        Item helmet = entity.getEquippedStack(EquipmentSlot.HEAD).getItem();
        if (helmet == Items.LEATHER_HELMET){
            chance += LEATHER_HELMET_BONUS;
        } else if (helmet == Items.GOLDEN_HELMET){
            chance += GOLD_HELMET_PENALTY;
        } else if (helmet == Items.IRON_HELMET){
            chance += IRON_HELMET_PENALTY;
        } else if (helmet == Items.DIAMOND_HELMET){
            chance += DIAMOND_HELMET_BONUS;
        } else if (helmet == Items.NETHERITE_HELMET){
            chance += NETHERITE_HELMET_BONUS;
            chance = chance / 2;
        } else if (helmet == Items.CHAINMAIL_HELMET){
            chance += CHAINMAIL_HELMET_PENALTY;
        }
        chance += getChanceByHeight(entity);

        if (chance < MINIMUM_CHANCE){
            chance = MINIMUM_CHANCE;
        }
        chance = truncateToTwoDecimals(chance);
        if (entity instanceof PlayerEntity player){
            player.sendMessage(Text.literal(String.valueOf(chance) + " " + getCooldown(amplifier)), false);
        }
        return world.getRandom().nextDouble() < chance;
    }

    private static void summonLighting(ServerWorld world, LivingEntity entity) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(entity.getBlockPos().toCenterPos());
        world.spawnEntity(lightning);
    }
    private static double getChanceByHeight(LivingEntity entity) {
        double height = entity.getPos().getY();
        if (height < 20) return 0;
        return getLightningChanceByHeight(height);
    }


    private static double getLightningChanceByHeight(double y) {
        double minY = 40;
        double maxY = 320;

        if (y <= minY) return MIN_HEIGHT_PENALTY;
        if (y >= maxY) return MAX_HEIGHT_PENALTY;

        return MIN_HEIGHT_PENALTY + (MAX_HEIGHT_PENALTY - MIN_HEIGHT_PENALTY) * ((y - minY) / (maxY - minY));
    }

    private static int getCooldown(int amplifier) {
        if (amplifier == 0){
            return DEFAULT_COOLDOWN;
        }
        if (amplifier >= COOLDOWN_LEVELS.length){
            return 1;
        }
        return COOLDOWN_LEVELS[amplifier + 1];
    }
    private static double truncateToTwoDecimals(double value) {
        return Math.floor(value * 100) / 100;
    }
}
