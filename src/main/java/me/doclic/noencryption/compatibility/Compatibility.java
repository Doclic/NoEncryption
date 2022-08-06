package me.doclic.noencryption.compatibility;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Compatibility {

    public static final CompatiblePlayer COMPATIBLE_PLAYER;
    public static final CompatiblePacketListener COMPATIBLE_PACKET_LISTENER;

    public static final boolean SERVER_COMPATIBLE;
    public static final String SERVER_VERSION;

    public static final String COMPATIBLE_VERSION;
    public static final String BUKKIT_VERSION;

    static {

        COMPATIBLE_VERSION = "1.19.2-R0.1-SNAPSHOT";

        String minecraftVersion;
        String bukkitVersion;

        try {

            minecraftVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]; // Gets the server version
            bukkitVersion = Bukkit.getBukkitVersion(); // Gets the Bukkit version


        } catch (ArrayIndexOutOfBoundsException exception) {
            minecraftVersion = null;
            bukkitVersion = null;
        }

        SERVER_VERSION = minecraftVersion;
        BUKKIT_VERSION = bukkitVersion;

        Bukkit.getLogger().info("Your server is running version " + bukkitVersion);

        if (minecraftVersion != null) {

            final String versionPackage = getVersionPackage(minecraftVersion);

            COMPATIBLE_PLAYER = instantiate(getCompatibleClass(CompatiblePlayer.class, minecraftVersion, versionPackage));
            COMPATIBLE_PACKET_LISTENER = instantiate(getCompatibleClass(CompatiblePacketListener.class, minecraftVersion, versionPackage));

            SERVER_COMPATIBLE = true;

        } else {

            COMPATIBLE_PLAYER = null;
            COMPATIBLE_PACKET_LISTENER = null;

            SERVER_COMPATIBLE = false;

        }

    }

    private static String getVersionPackage(String minecraftVersion) {

        return Compatibility.class.getPackage().getName() + "." + minecraftVersion;

    }

    private static <T> Class<? extends T> getCompatibleClass(Class<T> clazz, String minecraftVersion, String versionPackage) {

        try {
            final Class<?> compatibleClass = Class.forName(versionPackage + "." + clazz.getSimpleName() + "_" + minecraftVersion);
            if (compatibleClass.getSuperclass() != clazz && !Arrays.asList(compatibleClass.getInterfaces()).contains(clazz)) {
                return null;
            }
            else
                //noinspection unchecked
                return (Class<? extends T>) compatibleClass;
        } catch (ClassNotFoundException e) {
            return null;
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
