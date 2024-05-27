package org.example.utils;

import static org.example.config.AppConfig.DEBUG;

public class LoggerUtil {

    public static void log(boolean logAlways, String message) {
        if (!logAlways && !DEBUG) {
            return;
        }
        System.out.println(message);
    }
}
