package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.compatibility.CompatiblePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.util.Crypt.SaltSignaturePair;

import java.util.Optional;

public class CompatiblePacketListener_v1_19_R1 implements CompatiblePacketListener {

    @Override
    public Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { return packet; }

    @Override
    public Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final Optional<Component> unsignedContent = clientboundPlayerChatPacket.unsignedContent();

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
