package me.doclic.noencryption;

import io.netty.channel.*;
import me.doclic.noencryption.compatibility.Compatibility;
import me.doclic.noencryption.config.ConfigurationHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin (PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final Channel channel;
        try {
            channel = Compatibility.COMPATIBLE_PLAYER.getChannel(player);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            NoEncryption.logger().severe("An error occurred while retrieving the player's channel for injection!");
            throw new RuntimeException(ex);
        }
        final ChannelPipeline pipeline = channel.pipeline();
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
            pipeline.addLast(NoEncryption.playerHandlerName, handler);
        } else {
            pipeline.addBefore("packet_handler", NoEncryption.playerHandlerName, handler);
        }

        if (ConfigurationHandler.Config.getLoginProtectionMessage() != null) {
            if (!ConfigurationHandler.Config.getLoginProtectionMessage().trim().equals("")) {
                Chat.sendChat(player, ConfigurationHandler.Config.getLoginProtectionMessage());
            }
        }

        NoEncryption.addPlayerChannel(channel);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        final Channel channel;
        final Channel serverChannel = NoEncryption.serverChannels.get(player.getUniqueId());

        try {
            channel = Compatibility.COMPATIBLE_PLAYER.getChannel(player);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            NoEncryption.logger().severe("An error occurred while retrieving the player's channel for injection!");
            throw new RuntimeException(ex);
        }

        try {
            channel.eventLoop().submit(() -> channel.pipeline().remove(NoEncryption.playerHandlerName));

            serverChannel.eventLoop().submit(() -> serverChannel.pipeline().remove("noencryption_serverlevel"));
            NoEncryption.serverChannels.remove(player.getUniqueId());
        } catch (NullPointerException ex) {
            NoEncryption.logger().warning("Could not remove the packet handler for " + player.getName() + " (" + player.getUniqueId() + ")");
            ex.printStackTrace();
        }
    }
}