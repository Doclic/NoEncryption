package me.doclic.noencryption;

import org.bukkit.entity.Player;

public class Chat {
    public static void sendChat(Player player, String message) {
        if (NoEncryption.usesKyoriChat()) {
            player.sendMessage(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy('&').deserialize(message)
            );
        } else {
            player.sendMessage(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', message)
            );
        }
    }
}
