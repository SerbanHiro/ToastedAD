package me.serbob.toastedad.Utils;

import org.bukkit.Bukkit;

public class Logger {
    public static void log(LogLevel level, String message) {
        if (message == null) return;

        switch (level) {
            case e:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&8[&c&lERROR&r&8] &f" + message));
                break;
            case WARNING:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&c&k&lWARNING &8» &f" + message));
                break;
            case INFO:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&x&f&f&a&d&6&1&lINFO&r &8» &f" + message));
                break;
            case s:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&a&lSUCCESS &8» &f" + message));
                break;
            case DEBUG:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&9DEBUG &8» &f"+message));
                break;
            case OUTLINE:
                Bukkit.getConsoleSender().sendMessage(ChatUtil.c("&8&l&m{message}"
                        .replace("{message}",message)));
                break;
        }
    }

    public enum LogLevel {e, WARNING, INFO, s, DEBUG, OUTLINE }
}

