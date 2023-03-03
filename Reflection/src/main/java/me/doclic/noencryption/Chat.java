package me.doclic.noencryption;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Chat {
    public static void sendChat(Player player, String message) {
        if (NoEncryption.usesKyoriChat()) {
            player.sendMessage(
                    (net.kyori.adventure.text.Component) compileComponent(message)
            );
        } else {
            player.sendMessage(
                    (String) compileComponent(message)
            );
        }
    }

    public static void sendChat(CommandSender sender, String message) {
        if (sender instanceof Player)
            sendChat((Player) sender, message);
        else
            sender.sendMessage(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', message)
            );
    }

    public static Object compileComponent(String message) {
        if (NoEncryption.usesKyoriChat()) {
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy('&').deserialize(message.replace("\\n", "\n"));
        } else {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', message.replace("\\n", "\n"));
        }
    }
}
