package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.doclic.noencryption.compatibility.CompatiblePacketListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class CompatiblePacketListener_v1_19_R1 implements CompatiblePacketListener {

    // Caching fields
    private final Field signedContentField;
    private final Field saltSignatureField;
    private final VarHandle modifiersHandle;
    public CompatiblePacketListener_v1_19_R1() {
        try {
            signedContentField = ClientboundPlayerChatPacket.class.getDeclaredField("a");
            signedContentField.setAccessible(true);
            saltSignatureField = ClientboundPlayerChatPacket.class.getDeclaredField("f");
            saltSignatureField.setAccessible(true);
            final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            modifiersHandle = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception { }

    @Override
    public void writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof final ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
            final Optional<IChatBaseComponent> unsignedContent = clientboundPlayerChatPacket.d();
            if (unsignedContent.isPresent()) {
                signedContentField.set(clientboundPlayerChatPacket, unsignedContent.get());
            }

            // applying a fix of an issue called "can't set a field, which is final!" beforehand.
            modifiersHandle.set(saltSignatureField, saltSignatureField.getModifiers() & ~Modifier.FINAL);

            saltSignatureField.set(clientboundPlayerChatPacket, null);

        }

    }

}
