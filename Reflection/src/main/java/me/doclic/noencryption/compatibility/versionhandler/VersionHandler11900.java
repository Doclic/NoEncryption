package me.doclic.noencryption.compatibility.versionhandler;

import me.doclic.noencryption.compatibility.NMSInterface;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

public class VersionHandler11900 implements VersionHandler {
    private static final Class<?> PLAYER_CHAT_PACKET_CLASS;
    private static final Field PLAYER_CHAT_SIGNED_FIELD;
    private static final Field PLAYER_CHAT_UNSIGNED_FIELD;
    private static final Field PLAYER_CHAT_TYPE_ID_FIELD;
    private static final Field PLAYER_CHAT_SENDER_FIELD;
    private static final Field PLAYER_CHAT_TIME_STAMP_FIELD;
    private static final Constructor<?> PLAYER_CHAT_PACKET_CONSTRUCTOR;
    private static final Constructor<?> SIGNATURE_CONSTRUCTOR;
    static {
        PLAYER_CHAT_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundPlayerChatPacket");
        PLAYER_CHAT_SIGNED_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "a");
        PLAYER_CHAT_UNSIGNED_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "b");
        PLAYER_CHAT_TYPE_ID_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "c");
        PLAYER_CHAT_SENDER_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "d");
        PLAYER_CHAT_TIME_STAMP_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "e");
        final var signatureClass = NMSInterface.getClass("net.minecraft.util.MinecraftEncryption$b");
        PLAYER_CHAT_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(PLAYER_CHAT_PACKET_CLASS,
                PLAYER_CHAT_SIGNED_FIELD.getType(),
                Optional.class,
                int.class,
                PLAYER_CHAT_SENDER_FIELD.getType(),
                Instant.class,
                signatureClass
        );
        SIGNATURE_CONSTRUCTOR = NMSInterface.getConstructor(signatureClass, long.class, byte[].class);
    }

    @Override
    public Object writePacket(Object packet) throws Exception {
        if(packet == null) return null;

        if(packet.getClass().equals(PLAYER_CHAT_PACKET_CLASS)) {
            final var unsignedContent = (Optional<?>) PLAYER_CHAT_UNSIGNED_FIELD.get(packet);

            InternalMetrics.insertChart(new Metrics.SingleLineChart("strippedMessages", () -> 1));

            return PLAYER_CHAT_PACKET_CONSTRUCTOR.newInstance(
                    (unsignedContent.isPresent()) ? unsignedContent.get() : PLAYER_CHAT_SIGNED_FIELD.get(packet),
                    unsignedContent,
                    PLAYER_CHAT_TYPE_ID_FIELD.get(packet),
                    PLAYER_CHAT_SENDER_FIELD.get(packet),
                    PLAYER_CHAT_TIME_STAMP_FIELD.get(packet),
                    SIGNATURE_CONSTRUCTOR.newInstance(0, new byte[0])
            );
        }

        return packet;
    }
}