package de.uscoutz.nexus.item;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NmsHelper {

    public static void sendPacket(Player player, Object packet) {
        try {
            Object playerHandle = playerHandle(player);
            Object playerConnection = playerHandle.getClass().getField("playerConnection").get(playerHandle);
            playerConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(playerConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static Object playerHandle(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getNmsClass(String name) {
        try {
            return Class.forName(getPackage() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." +
                Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Object object, String name, Object value) {
        try {
            getField(object, name).set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Field getField(Object object, String name) {
        return getField(object.getClass(), name);
    }

    public static <T> T getFieldValue(Class<?> clazz, String fieldName, Object instance) {
        try {
            return (T) getField(clazz, fieldName).get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... types) {
        try {
            return clazz.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPackage() {
        String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return "net.minecraft.server." + ver;
    }
}
