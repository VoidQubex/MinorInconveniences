package io.github.VoidQubex.minor_inconveniences.mixin;

import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class EntityMixin extends Entity implements Attackable {
    @Shadow
    public abstract float getMaxHealth();

    public EntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if ((Entity) this instanceof VexEntity) return;

        for (int i = 0; i < (int) this.getMaxHealth() / 2; i++) {
            VexEntity vex = new VexEntity(EntityType.VEX, this.getWorld());
            vex.setPos(this.getX(), this.getY() + 0.5, this.getZ());

            vex.setCustomName(Text.of("Ghost of " + this.getName().getString()));
            vex.setUuid(UUID.randomUUID());
            this.getWorld().spawnEntity(vex);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onYourDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) this instanceof CreeperEntity creeperEntity) {
            creeperEntity.ignite();
        }

        if ((Entity) this instanceof SpiderEntity spiderEntity) {
            World world = this.getWorld();

            for (int i = 0; i < 1; i++) {
                SpiderEntity spider = new SpiderEntity(EntityType.SPIDER, world);
                spider.setPos(spiderEntity.getX(), spiderEntity.getY() + 1, spiderEntity.getZ());

                spider.setUuid(UUID.randomUUID());
                world.spawnEntity(spider);
            }
        }
    }
}