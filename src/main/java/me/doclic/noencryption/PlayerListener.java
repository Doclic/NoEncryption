package me.doclic.noencryption;

import io.netty.channel.*;
import me.doclic.noencryption.compatibility.Compatibility;
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
        final ChannelPipeline pipeline = Compatibility.COMPATIBLE_PLAYER.getChannel(player).pipeline();
        pipeline.addBefore("packet_handler", player.getUniqueId().toString(), new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {

                Compatibility.COMPATIBLE_PACKET_LISTENER.readPacket(channelHandlerContext, packet);

                super.channelRead(channelHandlerContext, packet);

            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {

                Compatibility.COMPATIBLE_PACKET_LISTENER.writePacket(channelHandlerContext, packet, promise);

                super.write(channelHandlerContext, packet, promise);

            }

        });

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent e) {

        final Player player = e.getPlayer();
        final Channel channel = Compatibility.COMPATIBLE_PLAYER.getChannel(player);
        channel.eventLoop().submit(() -> channel.pipeline().remove(player.getUniqueId().toString()));

    }

}
