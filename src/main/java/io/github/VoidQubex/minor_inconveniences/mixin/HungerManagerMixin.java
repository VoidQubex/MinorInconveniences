package io.github.VoidQubex.minor_inconveniences.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private float exhaustion;

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void youDareClampMyHungerValues(float exhaustion, CallbackInfo ci) {
        ci.cancel();

        this.exhaustion += exhaustion;
    }
}