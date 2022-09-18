package me.doclic.noencryption.compatibility;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Compatibility {

    public static final CompatiblePlayer COMPATIBLE_PLAYER;
    public static final CompatiblePacketListener COMPATIBLE_PACKET_LISTENER;

    public static final String PLUGIN_COMPATIBLE_VERSION;
    public static final boolean SERVER_COMPATIBLE;
    public static final String SERVER_VERSION;

    static {

        String minecraftVersion;

        PLUGIN_COMPATIBLE_VERSION = "1.19.1-R0.1-SNAPSHOT";

        try {

            minecraftVersion = Bukkit.getBukkitVersion();


        } catch (ArrayIndexOutOfBoundsException exception) {
            minecraftVersion = null;
        }

        SERVER_VERSION = minecraftVersion;

        Bukkit.getLogger().info("Your server is running version " + minecraftVersion);

        if (minecraftVersion != null && minecraftVersion.equals(PLUGIN_COMPATIBLE_VERSION)) {

            COMPATIBLE_PLAYER = instantiate(CompatiblePlayer.class);
            COMPATIBLE_PACKET_LISTENER = instantiate(CompatiblePacketListener.class);

            SERVER_COMPATIBLE = true;

        } else {

            COMPATIBLE_PLAYER = null;
            COMPATIBLE_PACKET_LISTENER = null;

            SERVER_COMPATIBLE = false;

        }

    }

    private static <T> T instantiate(Class<T> clazz) {

        if (clazz == null) return null;

        try {
            final Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

    }

}
