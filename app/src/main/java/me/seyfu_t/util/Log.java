package me.seyfu_t.util;

import java.util.logging.Level;

import me.seyfu_t.App;

public class Log {

    public static void debug(String msg, Object... args) {
        if (App.LOG_LEVEL == Level.FINE)
            System.out.println("[DEBUG] " + String.format(msg, args));
    }

    public static void debug(Object msg) {
        if (App.LOG_LEVEL == Level.FINE)
            System.out.println("[DEBUG] " + msg);
    }

    public static void debug(String prefix, Object msg) {
        if (App.LOG_LEVEL == Level.FINE)
            System.out.println("[DEBUG] " + prefix + " " + msg);
    }

    public static void info(String msg, Object... args) {
        if (App.LOG_LEVEL == Level.INFO)
            System.out.println("[INFO] " + String.format(msg, args));
    }

    public static void info(Object msg) {
        if (App.LOG_LEVEL == Level.INFO)
            System.out.println("[INFO] " + msg);
    }

    public static void info(String prefix, Object msg) {
        if (App.LOG_LEVEL == Level.INFO)
            System.out.println("[INFO] " + prefix + " " + msg);
    }

}
