package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.compatibility.CompatiblePacketListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Optional;

public class CompatiblePacketListener_v1_19_R1 implements CompatiblePacketListener {

    private Field signedContentField = null;
    private Field saltSignatureField = null;
    public CompatiblePacketListener_v1_19_R1() {
        try {
            signedContentField = ClientboundPlayerChatPacket.class.getDeclaredField("a");
            saltSignatureField = ClientboundPlayerChatPacket.class.getDeclaredField("f");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { }

    @Override
    public void writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {

        if (packet instanceof ClientboundPlayerChatPacket) {

            final ClientboundPlayerChatPacket clientboundPlayerChatPacket = (ClientboundPlayerChatPacket) packet;
            final Optional<IChatBaseComponent> unsignedContent = clientboundPlayerChatPacket.d();
            if (unsignedContent.isPresent())
                signedContentField.set(clientboundPlayerChatPacket, unsignedContent.get());
            saltSignatureField.set(clientboundPlayerChatPacket, null);

        }

    }

}
