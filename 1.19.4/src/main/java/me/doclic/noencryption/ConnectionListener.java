package me.doclic.noencryption;

import io.netty.channel.*;
import me.doclic.noencryption.compatibility.Compatibility;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ConnectionListener {
    public static BukkitTask start() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(NoEncryption.plugin(), ConnectionListener::runCatch, 1, 1);
    }

    public static void stop(BukkitTask task) {
        if (task != null)
            task.cancel();
    }

    private static void runCatch() {
        Compatibility.COMPATIBLE_PLAYER.getServerConnections().forEach(connection -> {
            Channel channel = connection.channel;
            ChannelPipeline pipeline = channel.pipeline();

            if (pipeline.get(NoEncryption.serverHandlerName) == null) {
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
                    pipeline.addLast(NoEncryption.serverHandlerName, handler);
                } else {
                    pipeline.addBefore("packet_handler", NoEncryption.serverHandlerName, handler);
                }

                NoEncryption.addServerChannel(channel);
            }
        });
    }
}
