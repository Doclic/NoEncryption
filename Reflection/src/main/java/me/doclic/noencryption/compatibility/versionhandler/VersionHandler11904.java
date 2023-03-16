package me.doclic.noencryption.compatibility.versionhandler;

//import com.google.gson.JsonParseException;
import me.doclic.noencryption.compatibility.NMSInterface;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class VersionHandler11904 implements VersionHandler {
    private static final Class<?> PLAYER_CHAT_PACKET_CLASS;
    private static final Field PLAYER_CHAT_SIGNED_BODY_FIELD;
    private static final Field SIGNED_BODY_CONTENT_FIELD;
    private static final Field PLAYER_CHAT_UNSIGNED_CONTENT_FIELD;
    private static final Field PLAYER_CHAT_TYPE_FIELD;
    private static Method chatTypeResolveMethod = null;
    private static Method serverRegistryAccessMethod = null;
    private static Method chatTypeDecorateMethod = null;
//    private static final Class<?> SYS_CHAT_PACKET_CLASS;
//    private static final Field SYS_CHAT_CONTENT_FIELD;
//    private static final Field SYS_CHAT_OVERLAY_FIELD;
    private static final Constructor<?> SYS_CHAT_PACKET_CONSTRUCTOR;
    private static final Class<?> COMPONENT_CLASS;
    private static final Method COMPONENT_COPY_METHOD;
    private static final Method JSON_TO_COMPONENT_METHOD;
    private static final Method LITERAL_COMPONENT_METHOD;
    private static final Class<?> SERVER_DATA_PACKET_CLASS;
    private static final Field SERVER_DATA_MOTD_FIELD;
    private static final Field SERVER_DATA_ICON_FIELD;
    private static final Constructor<?> SERVER_DATA_PACKET_CONSTRUCTOR;
    static {
        PLAYER_CHAT_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundPlayerChatPacket");
        PLAYER_CHAT_SIGNED_BODY_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "d");
        SIGNED_BODY_CONTENT_FIELD = NMSInterface.getField(PLAYER_CHAT_SIGNED_BODY_FIELD.getType(), "a");
        PLAYER_CHAT_UNSIGNED_CONTENT_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "e");
        PLAYER_CHAT_TYPE_FIELD = NMSInterface.getField(PLAYER_CHAT_PACKET_CLASS, "g");
//        SYS_CHAT_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
//        SYS_CHAT_CONTENT_FIELD = NMSInterface.getField(SYS_CHAT_PACKET_CLASS, "a");
//        SYS_CHAT_OVERLAY_FIELD = NMSInterface.getField(SYS_CHAT_PACKET_CLASS, "b");
        final var componentClassName = "net.minecraft.network.chat.IChatBaseComponent";
        COMPONENT_CLASS = NMSInterface.getClass(componentClassName);
        SYS_CHAT_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(
                NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket"),
                COMPONENT_CLASS,
                boolean.class
        );
        COMPONENT_COPY_METHOD = NMSInterface.getMethod(COMPONENT_CLASS, "e");
        JSON_TO_COMPONENT_METHOD = NMSInterface.getMethod(NMSInterface.getClass(componentClassName + "$ChatSerializer"), "a", String.class);
        LITERAL_COMPONENT_METHOD = NMSInterface.getMethod(COMPONENT_CLASS, "b", String.class);
        SERVER_DATA_PACKET_CLASS = NMSInterface.getClass("net.minecraft.network.protocol.game.ClientboundServerDataPacket");
        SERVER_DATA_MOTD_FIELD = NMSInterface.getField(SERVER_DATA_PACKET_CLASS, "a");
        SERVER_DATA_ICON_FIELD = NMSInterface.getField(SERVER_DATA_PACKET_CLASS, "b");
        SERVER_DATA_PACKET_CONSTRUCTOR = NMSInterface.getConstructor(SERVER_DATA_PACKET_CLASS, COMPONENT_CLASS, Optional.class, boolean.class);
    }

    @Override
    public Object writePacket(Object packet) throws Exception {
        if(packet == null) return null;

        if(packet.getClass().equals(PLAYER_CHAT_PACKET_CLASS)) {
            var content = PLAYER_CHAT_UNSIGNED_CONTENT_FIELD.get(packet);
            if(content != null) content = COMPONENT_COPY_METHOD.invoke(content);
            final var nmsServer = NMSInterface.getNMSServer();

            if(serverRegistryAccessMethod == null) serverRegistryAccessMethod = NMSInterface.getMethod(nmsServer.getClass()/*.getSuperclass()*/, "aX");
            if(chatTypeResolveMethod == null) chatTypeResolveMethod = NMSInterface.getMethod(PLAYER_CHAT_TYPE_FIELD.getType(), "a", NMSInterface.getClass("net.minecraft.core.IRegistryCustom"));
            final var chatType =
                    ((Optional<?>) chatTypeResolveMethod.invoke(PLAYER_CHAT_TYPE_FIELD.get(packet), serverRegistryAccessMethod.invoke(nmsServer))).orElseThrow();

            if(chatTypeDecorateMethod == null) chatTypeDecorateMethod = NMSInterface.getMethod(chatType.getClass(), "a", COMPONENT_CLASS);

            InternalMetrics.insertChart(new Metrics.SingleLineChart("strippedMessages", () -> 1));

            return SYS_CHAT_PACKET_CONSTRUCTOR.newInstance(
                    (content == null)
                            ? chatTypeDecorateMethod.invoke(chatType,
                                    LITERAL_COMPONENT_METHOD.invoke(null,
                                            SIGNED_BODY_CONTENT_FIELD.get(
                                                    PLAYER_CHAT_SIGNED_BODY_FIELD.get(packet))))
                            : content,
                    false
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

        if (packet.getClass().equals(SERVER_DATA_PACKET_CLASS)) {
            if (ConfigurationHandler.getDisableBanner()) {
                InternalMetrics.insertChart(new Metrics.SingleLineChart("popupsBlocked", () -> 1));

                // recreate a new packet
                return SERVER_DATA_PACKET_CONSTRUCTOR.newInstance(
                        SERVER_DATA_MOTD_FIELD.get(packet),
                        ((Optional<String>) SERVER_DATA_ICON_FIELD.get(packet)).orElse(""),
                        true
                );
            }
        }

        return packet;
    }
}