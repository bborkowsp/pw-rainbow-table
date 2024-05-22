package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final String FILE_PATH = "src/main/resources/test.txt";
    private static final String PLAIN_TEXT = "000000000 000000000 000000000 000000000 000000000 000000000 1234";
    private static final Integer CHAIN_LENGTH = 2;
    private static final String KEY = "creampie";
    private static final Boolean PARALLEL_MODE = true;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        LoggerUtil.log("Starting...");

        byte[] cipher = Des.getCipher(PLAIN_TEXT, KEY);

        LoggerUtil.log("Ciphered " + PLAIN_TEXT + " with key " + KEY + " is equal to " + HashLoggerUtil.getHashSubstitute(cipher));
        LoggerUtil.log("Reduction function for the returned cipher is " + ReductionFunction.reduceHash(cipher));

        LoggerUtil.log("Loading commonly used passwords...");
        List<String> commonlyUsedPasswords = FileReader.getCommonlyUsedPasswords(FILE_PATH);
        LoggerUtil.log("Loaded " + commonlyUsedPasswords.size() + " passwords");

        LoggerUtil.log("Initializing rainbow table structure...");
        RainbowTable rainbowTable = new RainbowTable(PLAIN_TEXT, CHAIN_LENGTH, commonlyUsedPasswords.size());

        LoggerUtil.log("Generating chains...");
        if (PARALLEL_MODE) {
            runParallel(rainbowTable, commonlyUsedPasswords);
        } else {
            runSequential(rainbowTable, commonlyUsedPasswords);
        }

        LoggerUtil.log("Chains have been generated!");
        rainbowTable.printTable();

        LoggerUtil.log("Cracking...");
        rainbowTable.crackKey(cipher);
    }


    private static void runParallel(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (String password : commonlyUsedPasswords) {
            executor.submit(() -> rainbowTable.generateChain(password));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.error("Error while waiting for executor to finish", e);
        }
    }

    private static void runSequential(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        commonlyUsedPasswords.forEach(rainbowTable::generateChain);
    }
}