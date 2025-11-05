package io.github.VoidQubex.minor_inconveniences;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;

import java.util.*;

public class MICallbacks {
    public static void registerBlockHitCallback() {
        Random random = new Random();

        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, direction) -> {
            if (!world.isClient) {
                if (random.nextDouble() < 0.01) {
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
        Map<ServerPlayerEntity, List<Double>> playerWeights = new HashMap<>();
        final int creeperInterval = 5 * 20 * 60;
        final int[] creeperTick = {0};

        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            creeperTick[0]--;
            for (ServerPlayerEntity playerEntity : serverWorld.getPlayers()) {
                playerWeights.putIfAbsent(playerEntity, new ArrayList<>());
                List<Double> weight = playerWeights.get(playerEntity);

                double totalWeight = 0;
                if (playerEntity.interactionManager.getGameMode() == GameMode.SURVIVAL) {
                    for (int i = 0; i < playerEntity.getInventory().size(); i++) {
                        ItemStack stack = playerEntity.getInventory().getStack(i);
                        if (!stack.isEmpty()) totalWeight += playerEntity.getInventory().getStack(i).getCount();
                    }

                    weight.clear();
                    weight.add(totalWeight);

                    changeAttribute(playerEntity, EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, totalWeight == 0 ? 1 : totalWeight * 0.1);
                    changeAttribute(playerEntity, EntityAttributes.GENERIC_BURNING_TIME, totalWeight == 0 ? 1 : totalWeight);

                    // Good luck
                    if (creeperTick[0] <= 0) {
                        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, serverWorld);
                        creeper.setPos(playerEntity.getX(), playerEntity.getY() + 20, playerEntity.getZ());
                        creeper.limitFallDistance();

                        serverWorld.spawnEntity(creeper);
                        creeperTick[0] = creeperInterval;
                    }

                    if (MinecraftClient.getInstance().player == null) return;
                    if (MinecraftClient.getInstance().player.input == null) return;

                    boolean moving = MinecraftClient.getInstance().player.input.movementForward != 0 || MinecraftClient.getInstance().player.input.movementSideways != 0;
                    if (moving) {
                        if (playerEntity.getRandom().nextDouble() > 0.001 || playerEntity.isSneaking() || !playerEntity.isSprinting())
                            return;
                        playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 255, true, false, false));

                        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(20, 40, 20);
                        TitleS2CPacket titleS2CPacket = new TitleS2CPacket(Text.of("You tripped"));
                        SubtitleS2CPacket subtitleS2CPacket = new SubtitleS2CPacket(Text.of("Be more careful next time"));

                        playerEntity.networkHandler.sendPacket(fadePacket);
                        playerEntity.networkHandler.sendPacket(titleS2CPacket);
                        playerEntity.networkHandler.sendPacket(subtitleS2CPacket);

                        playerEntity.getInventory().dropAll();
                    }
                }

                if (playerEntity.isSneaking() && !playerEntity.isCreative() && playerEntity.isOnGround()) {
                    if (creeperTick[0] % 4 == 0) {
                        playerEntity.sendMessage(Text.of("Timer: " + creeperTick[0]), false);
                        playerEntity.sendMessage(Text.of("Weight: " + totalWeight), false);
                    }
                    playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 2, true, false));

                    MinecraftClient.getInstance().execute(() -> {
                        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                        if (clientPlayer == null) return;

                        clientPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 2, true, false));
                    });
                }

            }
        });
    }

    private static void changeAttribute(PlayerEntity target, RegistryEntry<EntityAttribute> attribute, double value) {
        getAttributeInstance(target, attribute).setBaseValue(value);
    }

    private static EntityAttributeInstance getAttributeInstance(Entity entity, RegistryEntry<EntityAttribute> attribute) {
        return getLivingEntity(entity).getAttributes().getCustomInstance(attribute);
    }

    private static LivingEntity getLivingEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) return livingEntity;
        else return null;
    }
}