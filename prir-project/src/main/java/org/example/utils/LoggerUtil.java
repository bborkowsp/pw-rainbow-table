package org.example.utils;

import org.example.Main;

public class LoggerUtil {

    public static void log(boolean logAlways, String message) {
        if (!logAlways && !Main.DEBUG) {
            return;
        }
        System.out.println(message);
    }

}
