package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.compatibility.CompatiblePacketListener;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.Optional;
import java.util.UUID;

public class CompatiblePacketListener_v1_19_R1 implements CompatiblePacketListener {

    @Override
    public Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { return packet; }

    @Override
    public Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final Optional<Component> unsignedContent = clientboundPlayerChatPacket.message().unsignedContent();
            final ChatMessageContent signedContent = clientboundPlayerChatPacket.message().signedContent();
            final SignedMessageBody signedBody = clientboundPlayerChatPacket.message().signedBody();
            final ChatType.BoundNetwork chatType = clientboundPlayerChatPacket.chatType();

            // recreate a new packet
            return new ClientboundPlayerChatPacket(
                    new PlayerChatMessage(
                            new SignedMessageHeader(
                                    new MessageSignature(new byte[0]),
                                    new UUID(0,0)),
                            new MessageSignature(new byte[0]),
                            new SignedMessageBody(
                                    new ChatMessageContent(
                                            signedContent.plain(),
                                            signedContent.decorated()),
                                    signedBody.timeStamp(),
                                    0,
                                    signedBody.lastSeen()),
                            unsignedContent,
                            new FilterMask(0)
                    ),
                    chatType);
        }

        if (packet instanceof final ClientboundSystemChatPacket clientboundSystemChatPacket) {
            // recreate a new packet
            return new ClientboundSystemChatPacket(
                    ComponentSerializer.parse(clientboundSystemChatPacket.content()),
                    clientboundSystemChatPacket.overlay());
        }

        if (packet instanceof final ClientboundPlayerChatHeaderPacket clientboundPlayerChatHeaderPacket) {
            // recreate a new packet
            return new ClientboundPlayerChatHeaderPacket(
                    new SignedMessageHeader(
                            new MessageSignature(new byte[0]),
                            new UUID(0,0)),
                    new MessageSignature(new byte[0]),
                    clientboundPlayerChatHeaderPacket.bodyDigest()
            );
        }

        return packet;

    }

}