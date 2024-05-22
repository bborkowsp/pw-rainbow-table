package org.example;

public class LoggerUtil {

    public static void log(boolean logAlways, String message) {
        if (!logAlways && !Main.DEBUG) {
            return;
        }
        System.out.println(message);
    }

}
