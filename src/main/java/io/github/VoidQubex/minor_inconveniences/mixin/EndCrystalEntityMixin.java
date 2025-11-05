package io.github.VoidQubex.minor_inconveniences.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin extends Entity {
    @Shadow
    protected abstract void crystalDestroyed(DamageSource source);

    public EndCrystalEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();

        if (this.isInvulnerableTo(source)) {
            cir.setReturnValue(false);
        } else if (source.getAttacker() instanceof EnderDragonEntity) {
            cir.setReturnValue(false);
        } else {
            if (!this.isRemoved() && !this.getWorld().isClient) {
                this.remove(RemovalReason.KILLED);
                if (!source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                    DamageSource damageSource = source.getAttacker() != null ? this.getDamageSources().explosion(this, source.getAttacker()) : null;
                    this.getWorld().createExplosion(this, damageSource, null, this.getX(), this.getY(), this.getZ(), 6.0F, false, World.ExplosionSourceType.BLOCK);

                    for (int i = 0; i < 2; i++) {
                        WitherEntity wither = new WitherEntity(EntityType.WITHER, this.getWorld());
                        wither.setPos(this.getX(), this.getY() + 0.5, this.getZ());

                        wither.setUuid(UUID.randomUUID());
                        this.getWorld().spawnEntity(wither);
                    }
                }

                this.crystalDestroyed(source);
            }

            cir.setReturnValue(true);
        }
    }
}