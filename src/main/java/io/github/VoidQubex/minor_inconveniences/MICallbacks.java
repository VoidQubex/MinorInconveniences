package io.github.VoidQubex.minor_inconveniences;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import java.util.Random;

public class MICallbacks {
    public static void registerBlockHitCallback() {
        Random random = new Random();

        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, direction) -> {
            if (!world.isClient) {
                if (random.nextDouble() < 0.001) {
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                    TntEntity tntEntity = new TntEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, null);
                    tntEntity.setFuse(0);
                    world.spawnEntity(tntEntity);
                }

                if (world.getBlockState(blockPos).getBlock() instanceof DoorBlock) {
                    world.playSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 100, 1, true);
                    if (random.nextDouble() < 0.1) {
                        world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                        TntEntity tntEntity = new TntEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, null);
                        tntEntity.setFuse(0);
                        world.spawnEntity(tntEntity);
                    }
                }

                if (!player.isSpectator() && player.getMainHandStack().isEmpty()) {
                    player.damage(world.getDamageSources().flyIntoWall(), 2.0F);
                }

                return ActionResult.PASS;
            }

            if (world.getBlockState(blockPos).getBlock() instanceof DoorBlock) {
                world.playSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 100, 1, true);
            }

            if (!player.isSpectator() && player.getMainHandStack().isEmpty()) {
                player.damage(world.getDamageSources().flyIntoWall(), 1.0F);
            }

            return ActionResult.PASS;
        });
    }

    public static void registerTickEvent() {
        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            for (ServerPlayerEntity playerEntity : serverWorld.getPlayers()) {
                if (playerEntity.isSneaking() && !playerEntity.isCreative() && playerEntity.isOnGround()) {
                    serverWorld.setBlockState(playerEntity.getBlockPos().down(1), Blocks.LAVA.getDefaultState());
                }
            }
        });
    }
}
