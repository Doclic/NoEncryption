package me.doclic.noencryption.compatibility;

import io.netty.channel.Channel;
import me.doclic.noencryption.NoEncryption;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSInterface {
    private NMSInterface() { }

    // Player
    private static Method getPlayerHandleMethod = null;
    private static Field playerConnectionField = null;
    private static Field networkManagerField = null;
    private static Field nettyChannelField = null;

    // Server
    private static Method getServerHandleMethod = null;

    public static Field getField(Class<?> clazz, String name) {
        try {
            Field field;
            try { field = clazz.getDeclaredField(name); }
            catch(NoSuchFieldException e) { field = clazz.getField(name); }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            NoEncryption.logger().severe("Couldn't find field " + clazz.getName() + "#" + name);
            NoEncryption.logger().severe("Please create an issue on at");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/issues");

            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            Method method;
            try { method = clazz.getDeclaredMethod(name, params); }
            catch(NoSuchMethodException e) { method = clazz.getMethod(name, params); }
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            NoEncryption.logger().severe("Couldn't find method " + clazz.getName() + "#" + name);
            NoEncryption.logger().severe("Please create an issue on at");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/issues");

            throw new RuntimeException(e);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... params) {
        try {
            Constructor<T> constructor;
            try { constructor = clazz.getDeclaredConstructor(params); }
            catch(NoSuchMethodException e) { constructor = clazz.getConstructor(params); }
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            NoEncryption.logger().severe("Couldn't find constructor for " + clazz.getName());
            NoEncryption.logger().severe("Please create an issue on at");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/issues");

            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            NoEncryption.logger().severe("Couldn't find class " + name);
            NoEncryption.logger().severe("Please create an issue on at");
            NoEncryption.logger().severe("https://github.com/Doclic/NoEncryption/issues");

            throw new RuntimeException(e);
        }
    }


    public static Object getNMSServer() throws InvocationTargetException, IllegalAccessException { return getHandle(Bukkit.getServer()); }
    public static Object getHandle(Server server) throws InvocationTargetException, IllegalAccessException {
        if(getServerHandleMethod == null) getServerHandleMethod = getMethod(server.getClass(), "getServer");
        return getServerHandleMethod.invoke(server);
    }

    public static Object getHandle(Player player) throws InvocationTargetException, IllegalAccessException {
        if(getPlayerHandleMethod == null) getPlayerHandleMethod = getMethod(player.getClass(), "getHandle");
        return getPlayerHandleMethod.invoke(player);
    }

    public static Object getPlayerConnection(Object nmsPlayer) throws IllegalAccessException {
        if(playerConnectionField == null) playerConnectionField = getField(nmsPlayer.getClass(), "b");
        return playerConnectionField.get(nmsPlayer);
    }

    public static Object getNetworkManager(Object playerConnection) throws IllegalAccessException {
        if(networkManagerField == null) networkManagerField = getField(playerConnection.getClass(), "b");
        return networkManagerField.get(playerConnection);
    }

    public static Channel getNettyChannel(Object networkManager) throws IllegalAccessException, ClassCastException {
        if(nettyChannelField == null) nettyChannelField = getField(networkManager.getClass(), "m");
        return (Channel) nettyChannelField.get(networkManager);
    }

    public static Channel getNettyChannel(Player player) throws InvocationTargetException, IllegalAccessException, ClassCastException {
        return getNettyChannel(getNetworkManager(getPlayerConnection(getHandle(player))));
    }
}
