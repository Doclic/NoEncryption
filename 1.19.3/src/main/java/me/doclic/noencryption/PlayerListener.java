package me.doclic.noencryption;

import io.netty.channel.*;
import me.doclic.noencryption.compatibility.Compatibility;
import me.doclic.noencryption.config.ConfigurationHandler;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    public static void startConnectionListenTimer() {
        NoEncryption.timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(NoEncryption.plugin(), () -> {
            Compatibility.COMPATIBLE_PLAYER.getServerConnections().forEach(connection -> {
                Channel channel = connection.channel;
                ChannelPipeline pipeline = channel.pipeline();

                if (pipeline.get("noencryption") == null) {
                    final ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                        @Override
                        public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                            Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.readPacket(channelHandlerContext, packet);
                            super.channelRead(channelHandlerContext, newPacket);
                        }

                        @Override
                        public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
                            this.handleConnectionChannel(packet);

                            Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.writePacket(channelHandlerContext, packet, promise, false);
                            super.write(channelHandlerContext, newPacket, promise);
                        }

                        public void handleConnectionChannel(Object packet) {
                            if (packet instanceof ClientboundGameProfilePacket clientboundGameProfilePacket) {
                                NoEncryption.serverChannels.put(clientboundGameProfilePacket.getGameProfile().getId(), channel);
                            }
                        }
                    };

                    if (pipeline.get("packet_handler") == null) {
                        pipeline.addLast("noencryption", handler);
                    } else {
                        pipeline.addBefore("packet_handler", "noencryption", handler);
                    }
                }
            });
        }, 1, 1);

        NoEncryption.testerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(NoEncryption.plugin(), () -> {
            NoEncryption.serverChannels.forEach((uuid, channel) -> {
                if (Bukkit.getPlayer(uuid) == null) {
                    channel.eventLoop().submit(() -> channel.pipeline().remove("noencryption"));
                    NoEncryption.serverChannels.remove(uuid);
                }
            });
        }, 20, 20);
    }

    public static void stopConnectionListenTimer() {
        NoEncryption.timerTask.cancel();
        NoEncryption.testerTask.cancel();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin (PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final ChannelPipeline pipeline = Compatibility.COMPATIBLE_PLAYER.getChannel(player).pipeline();
        final ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.readPacket(channelHandlerContext, packet);
                super.channelRead(channelHandlerContext, newPacket);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
                Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.writePacket(channelHandlerContext, packet, promise, true);
                super.write(channelHandlerContext, newPacket, promise);
            }
        };

        if (pipeline.get("packet_handler") == null) {
            pipeline.addLast(player.getUniqueId().toString(), handler);
        } else {
            pipeline.addBefore("packet_handler", player.getUniqueId().toString(), handler);
        }

        if (ConfigurationHandler.getLoginProtectionMessage() != null) {
            if (!ConfigurationHandler.getLoginProtectionMessage().trim().equals("")) {
                Chat.sendChat(player, ConfigurationHandler.getLoginProtectionMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        final Channel channel = Compatibility.COMPATIBLE_PLAYER.getChannel(player);
        channel.eventLoop().submit(() -> channel.pipeline().remove(player.getUniqueId().toString()));

        Channel serverChannel = NoEncryption.serverChannels.get(player.getUniqueId());
        serverChannel.eventLoop().submit(() -> serverChannel.pipeline().remove("noencryption"));
        NoEncryption.serverChannels.remove(player.getUniqueId());
    }
}