package me.ashenguard.api.messenger;

import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.api.utils.Reflections;
import me.ashenguard.exceptions.NullValue;
import me.ashenguard.lib.events.ActionBarMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class ActionBarMessage_API extends ActionBarMessage{
    private static Class<?> craftPlayerClass;

    private static Constructor<?> chatComponentTextConstructor;
    private static Constructor<?> packetConstructor;

    private static Method craftPlayerHandleMethod;
    private static Method sendPacketMethod;

    private static Field playerConnectionField;

    private static Object chatMessageType;
    private static boolean initialized = false;

    private static void initialize() throws Throwable {
        craftPlayerClass = Reflections.getClass("entity.CraftPlayer");
        Class<?> packetPlayOutChatClass = Reflections.getClass("PacketPlayOutChat");
        Class<?> packetClass = Reflections.getClass("Packet");
        Class<?> chatComponentTextClass = Reflections.getClass("ChatComponentText");
        Class<?> iChatBaseComponentClass = Reflections.getClass("IChatBaseComponent");
        Class<?> chatMessageTypeClass = Reflections.getClass("ChatMessageType");
        Class<?> entityPlayerClass = Reflections.getClass("EntityPlayer");
        Class<?> playerConnectionClass = Reflections.getClass("PlayerConnection");

        NullValue.check("CraftPlayerClass", craftPlayerClass);
        NullValue.check("PacketPlayOutChatClass", packetPlayOutChatClass);
        NullValue.check("PacketClass", packetClass);
        NullValue.check("ChatComponentTextClass", chatComponentTextClass);
        NullValue.check("IChatBaseComponentClass", iChatBaseComponentClass);
        NullValue.check("ChatMessageTypeClass", chatMessageTypeClass);
        NullValue.check("EntityPlayerClass", entityPlayerClass);
        NullValue.check("PlayerConnectionClass", playerConnectionClass);

        chatMessageType = null;
        Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
        for (Object obj : chatMessageTypes) {
            if (obj.toString().equals("GAME_INFO")) {
                chatMessageType = obj;
            }
        }

        chatComponentTextConstructor = chatComponentTextClass.getConstructor(String.class);
        try {
            packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass);
        } catch (Throwable ignored) {
            packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass, UUID.class);
        }
        craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
        playerConnectionField = entityPlayerClass.getDeclaredField("playerConnection");
        sendPacketMethod = playerConnectionClass.getDeclaredMethod("sendPacket", packetClass);

        NullValue.check("ChatComponentTextConstructor", chatComponentTextConstructor);
        NullValue.check("PacketConstructor", packetConstructor);
        NullValue.check("CraftPlayerHandleMethod", craftPlayerHandleMethod);
        NullValue.check("PlayerConnectionField", playerConnectionField);
        NullValue.check("SendPacketMethod", sendPacketMethod);

        initialized = true;
    }

    public void sendActionBarMessage(SpigotPlugin plugin, Player player, String message) {
        if (!player.isOnline()) return;

        ActionBarMessageEvent event = new ActionBarMessageEvent(player, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getMessage() == null) return;

        try {
            if (!initialized) initialize();

            Object craftPlayer = craftPlayerClass.cast(player);
            Object chatComponentText = chatComponentTextConstructor.newInstance(message);
            Object packet;
            try {
                packet = packetConstructor.newInstance(chatComponentText, chatMessageType);
            } catch (Throwable ignored) {
                packet = packetConstructor.newInstance(chatComponentText, chatMessageType, player.getUniqueId());
            }
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Throwable throwable) {
            plugin.messenger.send(player, message);
        }
    }
}
