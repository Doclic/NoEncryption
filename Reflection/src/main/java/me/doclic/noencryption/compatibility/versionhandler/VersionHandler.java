package me.doclic.noencryption.compatibility.versionhandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.Chat;
import me.doclic.noencryption.NoEncryption;
import me.doclic.noencryption.compatibility.NMSInterface;
import me.doclic.noencryption.config.ConfigurationHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.lang.reflect.InvocationTargetException;

public interface VersionHandler {
    String PACKET_HANDLER_NAME = "NoEncryption";

    default void listen(Player player) {
        try {
            NMSInterface.getNettyChannel(player).pipeline().addBefore("packet_handler", PACKET_HANDLER_NAME, new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    try {
                        msg = readPacket(msg);
                    } catch (Exception e) {
                        NoEncryption.logger().warning("An exception was thrown when modifying C2S packet");
                        NoEncryption.logger().warning("Please create an issue at");
                        NoEncryption.logger().warning("https://github.com/Doclic/NoEncryption/issues");
                        NoEncryption.logger().warning("And provide this message:");
                        e.printStackTrace();

                        // We don't send the packet to packet_handler to be safe
                        return;
                    }
                    if(msg != null) super.channelRead(ctx, msg);
                }

                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    try {
                        msg = writePacket(msg);
                    } catch (Exception e) {
                        NoEncryption.logger().warning("An exception was thrown when modifying S2C packet");
                        NoEncryption.logger().warning("Please create an issue at");
                        NoEncryption.logger().warning("https://github.com/Doclic/NoEncryption/issues");
                        NoEncryption.logger().warning("And provide this message:");
                        e.printStackTrace();

                        // We don't send the packet to packet_handler to be safe
                        return;
                    }
                    if(msg != null) super.write(ctx, msg, promise);
                }
            });
        } catch (InvocationTargetException | IllegalAccessException e) {
            NoEncryption.logger().severe("Couldn't add NE packet handler for player " + player.getName() + " (" + player.getUniqueId() + ")");
            NoEncryption.logger().severe("The player has been kicked to prevent them from being able to report others");
            NoEncryption.logger().severe("To fix this, try updating NoEncryption by downloading a JAR from");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/releases");
            NoEncryption.logger().severe("You can download the multi-version JAR which is slower but supports multiple versions");
            NoEncryption.logger().severe("or you can also download a JAR for your specific version which may fix the issue");
            NoEncryption.logger().severe("If the issue persists this might mean that your server is running on an unsupported");
            NoEncryption.logger().severe("Minecraft version, in which case you'll have to wait for the developers to update NE");
            NoEncryption.logger().severe("This NoEncryption build supports versions 1.19-1.19.3");
            NoEncryption.logger().severe("Otherwise, please create an issue on the NoEncryption GitHub at");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/issues");
            NoEncryption.logger().severe("And provide the following:");
            NoEncryption.logger().severe("Minecraft version, server implementation (such as Spigot, Paper, etc), NE version,");
            NoEncryption.logger().severe("and the stacktrace shown below");
            e.printStackTrace();

            try {
                player.kick((net.kyori.adventure.text.Component) Chat.compileComponent(ConfigurationHandler.getSafetyKickMessage()), PlayerKickEvent.Cause.PLUGIN);
            } catch (NoClassDefFoundError exception) {
                // Server doesn't support Kyori chat so kick them through the Spigot method instead
                player.kickPlayer((String) Chat.compileComponent(ConfigurationHandler.getSafetyKickMessage()));
            }
        }
    }


    default Object readPacket(Object packet) throws Exception { return packet; }
    Object writePacket(Object packet) throws Exception;
}