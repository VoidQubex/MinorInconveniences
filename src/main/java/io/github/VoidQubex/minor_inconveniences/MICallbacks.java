package io.github.VoidQubex.minor_inconveniences;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
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

                if (!player.isSpectator() && player.getMainHandStack().isEmpty() && world.getBlockState(blockPos).getBlock().getHardness() >= 1.5f) {
                    player.damage(world.getDamageSources().flyIntoWall(), 2.0F);
                }

                return ActionResult.PASS;
            }

            if (world.getBlockState(blockPos).getBlock() instanceof DoorBlock) {
                world.playSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 100, 1, true);
            }

            if (!player.isSpectator() && player.getMainHandStack().isEmpty() && world.getBlockState(blockPos).getBlock().getHardness() >= 1.5f) {
                player.damage(world.getDamageSources().flyIntoWall(), 2.0F);
            }

            return ActionResult.PASS;
        });
    }

    public static void registerEntityHitCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if (entity instanceof EndermanEntity) {
                player.heal(-10.0f);
                player.addExhaustion(500.0f);
                if (world.isClient) {
                    player.addExhaustion(500.0f);
                    player.heal(-10.0f);
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void registerTickEvent() {
        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            for (ServerPlayerEntity playerEntity : serverWorld.getPlayers()) {
                if (playerEntity.isSneaking() && !playerEntity.isCreative() && playerEntity.isOnGround()) {
                    playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, true, false));

                    MinecraftClient.getInstance().execute(() -> {
                        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                        if (clientPlayer == null) {
                            return;
                        }

                        clientPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, true, false));
                    });
                }
            }
        });
    }

    public static void registerLoginEvent() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
                EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute == null) return;

                maxHealthAttribute.setBaseValue(2.0d);
            }
        });
    }
}