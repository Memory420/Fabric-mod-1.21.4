package com.memory.item.custom;

import com.google.common.collect.ImmutableMap;
import com.memory.TutorialMod;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RakeItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(TutorialMod.MOD_ID);
    private static final Map<Block, BlockState> HOE_MAP =
            ImmutableMap.of(
                    Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState(),
                    Blocks.DIRT, Blocks.FARMLAND.getDefaultState(),
                    Blocks.COARSE_DIRT, Blocks.DIRT.getDefaultState(),
                    Blocks.DIRT_PATH, Blocks.FARMLAND.getDefaultState(),
                    Blocks.ROOTED_DIRT, Blocks.DIRT.getDefaultState()
            );

    public RakeItem(Settings settings) {
        super(settings.maxDamage(250));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        int timesUsed = 0;
        if (HOE_MAP.containsKey(clickedBlock)) {
            if (!world.isClient) {
                BlockPos clickedPos = context.getBlockPos();
                boolean isBlockAboveIsAir = world.getBlockState(context.getBlockPos().up()).isAir();
                if (isBlockAboveIsAir) {
                    ((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                            clickedPos.getX() + 0.5, clickedPos.getY() + 1, clickedPos.getZ() + 0.5,
                            20,
                            0.3, 0.2, 0.3,
                            0.1
                    );
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos blockPos = context.getBlockPos().add(x, 0, z);
                            boolean isLocalBlockAboveIsAir = world.getBlockState(blockPos.up()).isAir();
                            if (isLocalBlockAboveIsAir) {
                                if (HOE_MAP.containsKey(world.getBlockState(blockPos).getBlock())) {
                                    world.setBlockState(blockPos, HOE_MAP.get(world.getBlockState(blockPos).getBlock()));
                                    timesUsed++;
                                }
                            }
                        }
                    }
                    LOGGER.info("Block above is air👍");
                } else {
                    LOGGER.warn("Block above isn't air!");
                    return ActionResult.FAIL;
                }

                context.getStack().damage(timesUsed, ((ServerWorld) world), ((ServerPlayerEntity) context.getPlayer()),
                        item -> context.getPlayer().sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                world.playSound(null, context.getBlockPos(), SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.tutorialmod.iron_rake.tooltip"));
        } else {
            tooltip.add(Text.translatable("tooltip.tutorialmod.iron_rake"));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public boolean canBeEnchantedWith(ItemStack stack, RegistryEntry<Enchantment> enchantment, EnchantingContext context) {
        return context == EnchantingContext.PRIMARY
                ? enchantment.value().isPrimaryItem(stack)
                : enchantment.value().isAcceptableItem(stack);
    }
}
