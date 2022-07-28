package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.compatibility.CompatiblePacketListener;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

import java.util.Optional;

public class CompatiblePacketListener_v1_19_R1 implements CompatiblePacketListener {

    @Override
    public Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { return packet; }

    @Override
    public Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final Optional<Component> unsignedContent = clientboundPlayerChatPacket.message().unsignedContent();  
            final Component unsignedContentWithFallback = unsignedContent.orElse(clientboundPlayerChatPacket.message().signedContent().decorated());
            final ChatMessageContent serverCont = new ChatMessageContent(unsignedContentWithFallback.getString(), unsignedContentWithFallback);
            
			// recreate a new packet
            return new ClientboundPlayerChatPacket(PlayerChatMessage.system(serverCont), clientboundPlayerChatPacket.chatType());
        }

        return packet;

    }

}
