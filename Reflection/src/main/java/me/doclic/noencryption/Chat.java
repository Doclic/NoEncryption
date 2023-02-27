package me.doclic.noencryption;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class Chat {
    public static void sendChat(Player player, String message) {
        if (NoEncryption.usesKyoriChat()) {
            player.sendMessage(
                    (Component) compileComponent(message)
            );
        } else {
            player.sendMessage(
                    (String) compileComponent(message)
            );
        }
    }

    public static Object compileComponent(String message) {
        if (NoEncryption.usesKyoriChat()) {
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy('&').deserialize(message.replace("\\n", "\n"));
        } else {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', message.replace("\\n", "\n"));
        }
    }
}
