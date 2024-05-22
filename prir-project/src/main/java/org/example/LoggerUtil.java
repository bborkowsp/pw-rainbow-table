package org.example;

public class LoggerUtil {


    public static void log(String message) {
        System.out.println(message);
    }

    public static void logWithoutPrintingNewLine(String message) {
        System.out.print(message);
    }

}
