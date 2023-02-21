package me.doclic.noencryption.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage;

import java.util.Optional;

public class CompatiblePacketListener {

    public Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { return packet; }

    public Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {

        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final String plainText = clientboundPlayerChatPacket.body().content();
            final Component textComponent = Component.literal(plainText);
            Optional<ChatType.Bound> chatType = clientboundPlayerChatPacket.chatType().resolve(((CraftServer) Bukkit.getServer()).getServer().registryAccess());

            InternalMetrics.insertChart(new Metrics.SingleLineChart("strippedMessages", () -> 1));

            return new ClientboundSystemChatPacket(
                    chatType.orElseThrow().decorate(textComponent),
                    false
            );
        } else if (packet instanceof final ClientboundSystemChatPacket clientboundSystemChatPacket) {
            if (clientboundSystemChatPacket.content() == null) {
                return clientboundSystemChatPacket;
            } else {
                // recreate a new packet
                return new ClientboundSystemChatPacket(
                        CraftChatMessage.fromJSONOrNull(clientboundSystemChatPacket.content()),
                        clientboundSystemChatPacket.overlay());
            }
        } else if (packet instanceof final ClientboundServerDataPacket clientboundServerDataPacket) {
            if (ConfigurationHandler.getDisableBanner()) {
                // recreate a new packet
                return new ClientboundServerDataPacket(
                        clientboundServerDataPacket.getMotd().get(),
                        clientboundServerDataPacket.getIconBase64().orElse(""),
                        true
                );
            }
        }

        return packet;

    }

}
