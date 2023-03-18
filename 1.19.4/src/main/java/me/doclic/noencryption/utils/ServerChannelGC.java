package me.doclic.noencryption.utils;

import me.doclic.noencryption.NoEncryption;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ServerChannelGC {
    public static BukkitTask start() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(NoEncryption.plugin(), ServerChannelGC::runGC, 100, 100);
    }

    public static void stop(BukkitTask task) {
        if (task != null)
            task.cancel();
    }

    private static void runGC() {
        NoEncryption.serverChannels.forEach((uuid, channel) -> {
            if (Bukkit.getPlayer(uuid) == null) {
                channel.eventLoop().submit(() -> channel.pipeline().remove(NoEncryption.serverHandlerName));
                NoEncryption.serverChannels.remove(uuid);
            }
        });

        NoEncryption.activeServerChannels.forEach(channel -> {
            if (!channel.isOpen()) {
                if (channel.pipeline().get(NoEncryption.serverHandlerName) != null) {
                    channel.pipeline().remove(NoEncryption.serverHandlerName);

                    NoEncryption.removeServerChannel(channel);
                } else {
                    NoEncryption.removeServerChannel(channel);;
                }
            }
        });
    }
}
