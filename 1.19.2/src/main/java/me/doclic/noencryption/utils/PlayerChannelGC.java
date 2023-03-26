package me.doclic.noencryption.utils;

import me.doclic.noencryption.NoEncryption;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class PlayerChannelGC {
    public static BukkitTask start() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(NoEncryption.plugin(), PlayerChannelGC::runGC, 100, 100);
    }

    public static void stop(BukkitTask task) {
        if (task != null)
            task.cancel();
    }

    private static void runGC() {
        NoEncryption.activePlayerChannels.forEach(channel -> {
            if (!channel.isOpen()) {
                if (channel.pipeline().get(NoEncryption.playerHandlerName) != null) {
                    channel.pipeline().remove(NoEncryption.playerHandlerName);

                    NoEncryption.removePlayerChannel(channel);
                } else {
                    NoEncryption.removePlayerChannel(channel);;
                }
            }
        });
    }
}
