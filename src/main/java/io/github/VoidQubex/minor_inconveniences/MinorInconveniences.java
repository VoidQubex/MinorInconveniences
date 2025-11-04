package io.github.VoidQubex.minor_inconveniences;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;

public class MinorInconveniences implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLoginConnectionEvents.INIT.register((serverLoginNetworkHandler, minecraftServer) -> {
            MICallbacks.registerBlockHitCallback();
            MICallbacks.registerEntityHitCallback();
            MICallbacks.registerTickEvent();
            MICallbacks.registerLoginEvent();
        });
    }
}
