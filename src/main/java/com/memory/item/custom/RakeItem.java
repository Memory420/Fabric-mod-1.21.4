package com.memory.item.custom;

import com.google.common.collect.ImmutableMap;
import com.memory.TutorialMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
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
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();

        if (HOE_MAP.containsKey(clickedBlock)) {
            if (!world.isClient) {
                boolean isBlockAboveIsAir = world.getBlockState(context.getBlockPos().up()).isAir();
                if (isBlockAboveIsAir) {
                    world.setBlockState(context.getBlockPos(), HOE_MAP.get(clickedBlock));
                    LOGGER.info("Block above is air👍");
                } else {
                    LOGGER.warn("Block above isn't air!");
                    return ActionResult.FAIL;
                }

                context.getStack().damage(1, ((ServerWorld) world), ((ServerPlayerEntity) context.getPlayer()),
                        item -> context.getPlayer().sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                world.playSound(null, context.getBlockPos(), SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS);
            }
        }
        return ActionResult.SUCCESS;
    }
}
