package net.dianacraft.alliances;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Events {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(Events::onServerStarting);
    }

    private static void onServerStarting(MinecraftServer server) {
        Main.server = server;
    }
}