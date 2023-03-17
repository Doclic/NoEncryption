package me.doclic.noencryption.commands;

import me.doclic.noencryption.Chat;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.updates.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            Chat.sendChat(sender, "&c[NoEncryption] &7Help Menu");

            // Send help options depending on if they are console or not
            if (sender instanceof ConsoleCommandSender) {
                Chat.sendChat(sender, "&7 suppressnotices &8-&7 Suppress startup config notices");
                Chat.sendChat(sender, "&7 checkforupdates &8-&7 Queries the GitHub for an updated version of NoEncryption");
            } else {
                if (sender.hasPermission("noencryption.command.suppressnotices"))
                    Chat.sendChat(sender, "&7 checkforupdates &8-&7 Queries the GitHub for an updated version of NoEncryption");
            }

            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "suppressnotices" -> {
                    if (!sender.hasPermission("noencryption.command.suppressnotices")) {
                        Chat.sendChat(sender, "&c[NoEncryption] You do not have permission to run this command!");
                        return true;
                    }
                    if (!(sender instanceof ConsoleCommandSender)) {
                        Chat.sendChat(sender, "&c[NoEncryption] You can not run this command as a player!");
                        return true;
                    }
                    if (ConfigurationHandler.Notices.suppressNotices())
                        Chat.sendChat(sender, "&c[NoEncryption] &7Start up config messages suppressed");
                    else
                        Chat.sendChat(sender, "&c[NoEncryption] There are no active config messages to suppress");
                }
                case "checkforupdates" -> {
                    if (!sender.hasPermission("noencryption.command.suppressnotices")) {
                        Chat.sendChat(sender, "&c[NoEncryption] You do not have permission to run this command!");
                        return true;
                    }
                    Chat.sendChat(sender, "&c[NoEncryption] &7Checking for updates...");
                    UpdateChecker.check(
                            () -> {
                                Chat.sendChat(sender, "&c[NoEncryption] &7There is a new update available. You can download it here:");
                                Chat.sendChat(sender, UpdateChecker.updateUrl.toString());
                            },
                            () -> {
                                Chat.sendChat(sender, "&c[NoEncryption] &7You are on the latest stable NoEncryption build");
                            },
                            () -> {
                                Chat.sendChat(sender, "&c[NoEncryption] There was an error checking for a new update.");
                                Chat.sendChat(sender, "If you are a server administrator, check your server's internet connection,");
                                Chat.sendChat(sender, "Or check the GitHub Status Page for API Requests issues: ");
                                Chat.sendChat(sender, "https://www.githubstatus.com/");
                            }
                    );
                }
                default ->
                        Chat.sendChat(sender, "&c[NoEncryption] Invalid argument \"" + ChatColor.stripColor(args[0]) + "\"!");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Makes it easier to iterate through, and less code
            Chat.sendChat(sender, "&c[NoEncryption] No argument provided!");
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length == 0) {
                return Collections.emptyList();
            } else if (args.length == 1) {
                if (sender instanceof ConsoleCommandSender)
                    return TabUtils.match(args[0], Arrays.asList(
                            "suppressnotices",
                            "checkforupdates"
                    ));
                else {
                    List<String> returnStrings = new ArrayList<>();
                    // Can swap for above for new commands that players can run
                    if (sender.hasPermission("noencryption.command.suppressnotices"))
                        returnStrings.add("checkforupdates");

                    return TabUtils.match(args[0], returnStrings);
                }
            } else {
                return Collections.emptyList();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Just makes it easier to code
            return Collections.emptyList();
        }
    }
}
