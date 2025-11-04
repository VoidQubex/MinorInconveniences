package io.github.VoidQubex.minor_inconveniences.mixin;

import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class EntityMixin extends Entity implements Attackable {
    public EntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if ((Entity) this instanceof VexEntity) return;

        if ((Entity) this instanceof IronGolemEntity) {
            for (int i = 0; i < 50; i++) {
                VexEntity vex = new VexEntity(EntityType.VEX, this.getWorld());
                vex.setPos(this.getX(), this.getY() + 0.5, this.getZ());

                vex.setCustomName(Text.of("Ghost of " + this.getName().getString()));
                vex.setUuid(UUID.randomUUID());
                this.getWorld().spawnEntity(vex);
            }
        } else {
            VexEntity vex = new VexEntity(EntityType.VEX, this.getWorld());
            vex.setPos(this.getX(), this.getY() + 0.5, this.getZ());

            vex.setCustomName(Text.of("Ghost of " + this.getName().getString()));
            vex.setUuid(UUID.randomUUID());
            this.getWorld().spawnEntity(vex);
        }
    }
}
