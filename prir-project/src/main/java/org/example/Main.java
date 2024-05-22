package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    //    private static final String FILE_PATH = "src/main/resources/most-common-8-chars-passwords.txt";
    private static final String FILE_PATH = "src/main/resources/test.txt";
    private static final String PLAIN_TEXT = "000000000 000000000 000000000 000000000 000000000 000000000 1234";
    private static final Integer CHAIN_LENGTH = 2;
    private static final String KEY = "superman";
    private static final Boolean PARALLEL_MODE = false;

    public static void main(String[] args) {
        log("Starting...");

        byte[] cipher = getCipher(KEY);

        log("Ciphered " + PLAIN_TEXT + " with key " + KEY + " is equal to " + new String(cipher));
        log("Reduction function for the returned cipher is " + new ReductionFunction().reduceHash(cipher));

        log("Loading commonly used passwords...");
        List<String> commonlyUsedPasswords = getCommonlyUsedPasswords();
        log("Loaded " + commonlyUsedPasswords.size() + " passwords");

        log("Initializing rainbow table structure...");
        RainbowTable rainbowTable = new RainbowTable(PLAIN_TEXT, CHAIN_LENGTH, commonlyUsedPasswords.size());

        log("Generating chains...");
        if (PARALLEL_MODE) {
            runParallel(rainbowTable, commonlyUsedPasswords);
        } else {
            runSequential(rainbowTable, commonlyUsedPasswords);
        }

        log("Chains have been generated!");
        rainbowTable.printTable();

        log("Cracking...");
        rainbowTable.crackKey(cipher);
    }

    public static void log(String message) {
        System.out.println(message);
    }

    public static void logWithoutBreakingTheLine(String message) {
        System.out.print(message);
    }

    private static byte[] getCipher(String key) {
        Des des = new Des();
        byte[] cipher = new byte[0];
        try {
            cipher = des.cipherPassword(PLAIN_TEXT, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }

    private static List<String> getCommonlyUsedPasswords() {
        List<String> commonUsedPasswords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commonUsedPasswords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commonUsedPasswords;
    }

    private static void runParallel(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (String password : commonlyUsedPasswords) {
            executor.submit(() -> rainbowTable.generateChain(password));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runSequential(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        commonlyUsedPasswords.forEach(rainbowTable::generateChain);
    }
}