package me.doclic.noencryption.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.util.Crypt.SaltSignaturePair;

import java.util.Optional;

public class CompatiblePacketListener {
    public Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { return packet; }

    public Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final Optional<Component> unsignedContent = clientboundPlayerChatPacket.unsignedContent();

            InternalMetrics.insertChart(new Metrics.SingleLineChart("strippedMessages", () -> 1));

            // recreate a new packet
            return new ClientboundPlayerChatPacket(
                    unsignedContent.orElse(clientboundPlayerChatPacket.signedContent()), // use unsigned content if available, this is the signed content field
                    unsignedContent, // unsigned content field
                    clientboundPlayerChatPacket.typeId(),
                    clientboundPlayerChatPacket.sender(),
                    clientboundPlayerChatPacket.timeStamp(),
                    new SaltSignaturePair(0, new byte[0])); // salt signature field
            }

        return packet;
    }
}