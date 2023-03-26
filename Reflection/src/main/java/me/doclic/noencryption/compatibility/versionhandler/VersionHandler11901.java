package me.doclic.noencryption.compatibility.versionhandler;

//import com.google.gson.JsonParseException;
import me.doclic.noencryption.compatibility.NMSInterface;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class VersionHandler11901 implements VersionHandler {
    private static final Class<?> PLAYER_CHAT_PACKET_CLASS;
    private static final Field PLAYER_CHAT_MESSAGE_FIELD;
    private static final Field PLAYER_CHAT_TYPE_FIELD;
    private static final Constructor<?> PLAYER_CHAT_PACKET_CONSTRUCTOR;
    private static final Field MESSAGE_UNSIGNED_CONTENT_FIELD;
    private static final Method MESSAGE_SIGNED_CONTENT_METHOD;
    private static final Field MESSAGE_SIGNED_BODY_FIELD;
    private static final Constructor<?> MESSAGE_CONSTRUCTOR;
    private static final Constructor<?> MESSAGE_HEADER_CONSTRUCTOR;
    private static final Constructor<?> MESSAGE_SIGNATURE_CONSTRUCTOR;
    private static final Field MESSAGE_BODY_TIMESTAMP_FIELD;
    private static final Field MESSAGE_BODY_LAST_SEEN_FIELD;
    private static final Constructor<?> MESSAGE_BODY_CONSTRUCTOR;
    private static final Field MESSAGE_CONTENT_PLAIN_FIELD;
    private static final Field MESSAGE_CONTENT_DECORATED_FIELD;
    private static final Constructor<?> MESSAGE_CONTENT_CONSTRUCTOR;
    private static final Constructor<?> FILTER_MASK_CONSTRUCTOR;
//    private static final Class<?> SYS_CHAT_PACKET_CLASS;
//    private static final Field SYS_CHAT_CONTENT_FIELD;
//    private static final Field SYS_CHAT_OVERLAY_FIELD;
//    private static final Constructor<?> SYS_CHAT_PACKET_CONSTRUCTOR;
    private static final Class<?> MSG_HEADER_PACKET_CLASS;
    private static final Field MSG_HEADER_PACKET_DIGEST_FIELD;
    private static final Constructor<?> MSG_HEADER_PACKET_CONSTRUCTOR;
    private static final Method JSON_TO_COMPONENT_METHOD;
    private static final Class<?> SERVER_DATA_PACKET_CLASS;
    private static final Field SERVER_DATA_MOTD_FIELD;
    private static final Field SERVER_DATA_ICON_FIELD;
    private static final Field SERVER_DATA_PREVIEWS_CHAT_FIELD;
    private static final Constructor<?> SERVER_DATA_PACKET_CONSTRUCTOR;
    static {
        PLAYER_CHAT_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundPlayerChatPacket");
        PLAYER_CHAT_MESSAGE_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "a");
        PLAYER_CHAT_TYPE_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "b");
        final var messageClass = NMSInterface.getClass("net.minecraft.network.chat.PlayerChatMessage");
        final var messageTypeClass = NMSInterface.getClass("net.minecraft.network.chat.ChatMessageType$b");
        PLAYER_CHAT_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(PLAYER_CHAT_PACKET_CLASS, messageClass, messageTypeClass);
        MESSAGE_UNSIGNED_CONTENT_FIELD = NMSInterface.getField(messageClass, "f");
        MESSAGE_SIGNED_CONTENT_METHOD = NMSInterface.getMethod(messageClass, "b");
        MESSAGE_SIGNED_BODY_FIELD = NMSInterface.getField(messageClass, "e");
        final var headerClass = NMSInterface.getClass("net.minecraft.network.chat.SignedMessageHeader");
        final var signatureClass = NMSInterface.getClass("net.minecraft.network.chat.MessageSignature");
        final var filterMaskClass = NMSInterface.getClass("net.minecraft.network.chat.FilterMask");
        MESSAGE_CONSTRUCTOR = NMSInterface.getConstructor(messageClass,
                headerClass,
                signatureClass,
                MESSAGE_SIGNED_BODY_FIELD.getType(),
                Optional.class,
                filterMaskClass
        );
        MESSAGE_HEADER_CONSTRUCTOR = NMSInterface.getConstructor(headerClass, signatureClass, UUID.class);
        MESSAGE_SIGNATURE_CONSTRUCTOR = NMSInterface.getConstructor(signatureClass, byte[].class);
        MESSAGE_BODY_TIMESTAMP_FIELD = NMSInterface.getField(MESSAGE_SIGNED_BODY_FIELD.getType(), "c");
        MESSAGE_BODY_LAST_SEEN_FIELD = NMSInterface.getField(MESSAGE_SIGNED_BODY_FIELD.getType(), "e");
        MESSAGE_BODY_CONSTRUCTOR = NMSInterface.getConstructor(MESSAGE_SIGNED_BODY_FIELD.getType(),
                MESSAGE_SIGNED_CONTENT_METHOD.getReturnType(),
                Instant.class,
                long.class,
                MESSAGE_BODY_LAST_SEEN_FIELD.getType()
        );
        MESSAGE_CONTENT_PLAIN_FIELD = NMSInterface.getField(MESSAGE_SIGNED_CONTENT_METHOD.getReturnType(), "a");
        MESSAGE_CONTENT_DECORATED_FIELD = NMSInterface.getField(MESSAGE_SIGNED_CONTENT_METHOD.getReturnType(), "b");
        final var componentClassName = "net.minecraft.network.chat.IChatBaseComponent";
        final var componentClass = NMSInterface.getClass(componentClassName);
        MESSAGE_CONTENT_CONSTRUCTOR = NMSInterface.getConstructor(MESSAGE_SIGNED_CONTENT_METHOD.getReturnType(), String.class, componentClass);
        FILTER_MASK_CONSTRUCTOR = NMSInterface.getConstructor(NMSInterface.getClass("net.minecraft.network.chat.FilterMask"), int.class);
//        SYS_CHAT_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
//        SYS_CHAT_CONTENT_FIELD = NMSInterface.getField(SYS_CHAT_PACKET_CLASS, "a");
//        SYS_CHAT_OVERLAY_FIELD = NMSInterface.getField(SYS_CHAT_PACKET_CLASS, "b");
//        SYS_CHAT_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(SYS_CHAT_PACKET_CLASS, componentClass, boolean.class);
        MSG_HEADER_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket");
        MSG_HEADER_PACKET_DIGEST_FIELD = NMSInterface.getField(MSG_HEADER_PACKET_CLASS, "c");
        MSG_HEADER_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(MSG_HEADER_PACKET_CLASS, headerClass, signatureClass, byte[].class);
        JSON_TO_COMPONENT_METHOD = NMSInterface.getMethod(NMSInterface.getClass(componentClassName + "$ChatSerializer"), "a", String.class);
        SERVER_DATA_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundServerDataPacket");
        SERVER_DATA_MOTD_FIELD = NMSInterface.getField(SERVER_DATA_PACKET_CLASS, "a");
        SERVER_DATA_ICON_FIELD = NMSInterface.getField(SERVER_DATA_PACKET_CLASS, "b");
        SERVER_DATA_PREVIEWS_CHAT_FIELD = NMSInterface.getField(SERVER_DATA_PACKET_CLASS, "c");
        SERVER_DATA_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(SERVER_DATA_PACKET_CLASS, componentClass, String.class, boolean.class, boolean.class);
    }

    @Override
    public Object writePacket(Object packet) throws Exception {
        if(packet == null) return null;

        if(packet.getClass().equals(PLAYER_CHAT_PACKET_CLASS)) {
            final var msg = PLAYER_CHAT_MESSAGE_FIELD.get(packet);
            final var unsignedContent = MESSAGE_UNSIGNED_CONTENT_FIELD.get(msg);
            final var signedContent = MESSAGE_SIGNED_CONTENT_METHOD.invoke(msg);
            final var signedBody = MESSAGE_SIGNED_BODY_FIELD.get(msg);
            final var type = PLAYER_CHAT_TYPE_FIELD.get(packet);

            InternalMetrics.insertChart(new Metrics.SingleLineChart("strippedMessages", () -> 1));

            final var emptySignature = MESSAGE_SIGNATURE_CONSTRUCTOR.newInstance((Object) new byte[0]);
            return PLAYER_CHAT_PACKET_CONSTRUCTOR.newInstance(
                    MESSAGE_CONSTRUCTOR.newInstance(
                            MESSAGE_HEADER_CONSTRUCTOR.newInstance(
                                    emptySignature,
                                    new UUID(0, 0) // todo ConfigurationHandler.forwardUUID
                            ),
                            emptySignature,
                            MESSAGE_BODY_CONSTRUCTOR.newInstance(
                                    MESSAGE_CONTENT_CONSTRUCTOR.newInstance(
                                            MESSAGE_CONTENT_PLAIN_FIELD.get(signedContent),
                                            MESSAGE_CONTENT_DECORATED_FIELD.get(signedContent)
                                    ),
                                    MESSAGE_BODY_TIMESTAMP_FIELD.get(signedBody),
                                    0,
                                    MESSAGE_BODY_LAST_SEEN_FIELD.get(signedBody)
                            ),
                            unsignedContent,
                            FILTER_MASK_CONSTRUCTOR.newInstance(0)
                    ),
                    type
            );
        }

//        if(packet.getClass().equals(SYS_CHAT_PACKET_CLASS)) {
//            var content = SYS_CHAT_CONTENT_FIELD.get(packet);
//            if (content == null) return packet;
//            else {
//                try { content = JSON_TO_COMPONENT_METHOD.invoke(null, content); }
//                catch (JsonParseException e) { content = null; }
//                // recreate a new packet
//                return SYS_CHAT_PACKET_CONSTRUCTOR.newInstance(content, SYS_CHAT_OVERLAY_FIELD.get(packet));
//            }
//        }

        if(packet.getClass().equals(MSG_HEADER_PACKET_CLASS)) {
            return MSG_HEADER_PACKET_CONSTRUCTOR.newInstance(
                    MESSAGE_HEADER_CONSTRUCTOR.newInstance(
                            MESSAGE_SIGNATURE_CONSTRUCTOR.newInstance((Object) new byte[0]),
                            new UUID(0, 0)
                    ),
                    MSG_HEADER_PACKET_DIGEST_FIELD.get(packet)
            );
        }

        if (packet.getClass().equals(SERVER_DATA_PACKET_CLASS)) {
            if (ConfigurationHandler.Config.getDisableBanner()) {
                InternalMetrics.insertChart(new Metrics.SingleLineChart("popupsBlocked", () -> 1));

                // recreate a new packet
                return SERVER_DATA_PACKET_CONSTRUCTOR.newInstance(
                        ((Optional<?>) SERVER_DATA_MOTD_FIELD.get(packet)).orElse(null),
                        ((Optional<String>) SERVER_DATA_ICON_FIELD.get(packet)).orElse(null),
                        SERVER_DATA_PREVIEWS_CHAT_FIELD.get(packet),
                        true
                );
            }
        }

        return packet;
    }
}