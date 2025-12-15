package net.dianacraft.alliances.util;

import net.dianacraft.alliances.Main;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.dianacraft.alliances.Main.server;

public class PlayerUtils {
    public static void sendMessage(ServerPlayer player, String prefix, String message){
        player.displayClientMessage(Component.literal(prefix + " §r" + message), false);
    }

    public static void sendAnnouncement(ServerPlayer player, String message, boolean success){
        if (success){
            sendMessage(player, "§6[Alliances]", message);
        } else {
            sendMessage(player, "§c[Alliances]", "§c"+message);
        }
    }

    public static ServerPlayer getPlayer(String player){
        if (server == null || player == null) return null;
        return server.getPlayerList().getPlayerByName(player);
    }

    public static boolean isAdmin(ServerPlayer player){
        if (player == null) return false;
        if (server == null) return false;
        return server.getPlayerList().isOp(player.nameAndId());
    }
}