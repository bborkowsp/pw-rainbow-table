package org.example;

import org.example.cli.CommandLineArgumentParser;
import org.example.core.RainbowTable;
import org.example.core.ReductionFunction;
import org.example.crypto.Des;
import org.example.utils.FileUtil;
import org.example.utils.HashLoggerUtil;
import org.example.utils.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.example.config.AppConfig.*;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        CommandLineArgumentParser.parseArguments(args);

        LoggerUtil.log(true, "Starting...");
        if (PARALLEL_MODE) {
            LoggerUtil.log(true, "Parallel mode will run on " + NUMBER_OF_THREADS + " threads");
        }

        boolean generateInParallelMode = PARALLEL_MODE;
        boolean crackInParallelMode = PARALLEL_MODE && !RUN_CRACKING_ONLY_IN_SEQUENTIAL_MODE;

        Long generatingStartTime = null, generatingEndTime = null;
        long crackingStartTime, crackingEndTime;
        Des des = new Des();
        ReductionFunction reductionFunction = new ReductionFunction();
        String cipher;
        if (CIPHER_TO_CRACK == null) {
            cipher = des.getCipher(PLAIN_TEXT, KEY);
            LoggerUtil.log(true, "Using key \"" + KEY + "\" and plain text \"" + PLAIN_TEXT + "\" to test the rainbow table");
        } else {
            cipher = CIPHER_TO_CRACK;
            LoggerUtil.log(true, "Using cipher \"" + CIPHER_TO_CRACK + "\" to test the rainbow table");
        }

        LoggerUtil.log(false, "Ciphered " + PLAIN_TEXT + " with key " + KEY + " is equal to " + HashLoggerUtil.getHashSubstitute(cipher));
        LoggerUtil.log(false, "Reduction function for the returned cipher is " + reductionFunction.reduceHash(cipher, 1));

        RainbowTable rainbowTable;

        if (TABLES_INPUT_FILE_PATH != null) {
            LoggerUtil.log(true, "Loading rainbow table from file at " + TABLES_INPUT_FILE_PATH);
            rainbowTable = FileUtil.readInputFile(TABLES_INPUT_FILE_PATH);
            LoggerUtil.log(true, "Rainbow table has been successfully loaded");
        } else {
            LoggerUtil.log(true, "Loading commonly used passwords...");
            List<String> commonlyUsedPasswords = FileUtil.getCommonlyUsedPasswords(PASSWORDS_INPUT_FILE_PATH);
            if (commonlyUsedPasswords.isEmpty()) {
                logger.error("No commonly used passwords found in the file: " + PASSWORDS_INPUT_FILE_PATH);
                System.exit(1);
            }
            LoggerUtil.log(true, "Loaded " + commonlyUsedPasswords.size() + " passwords");

            LoggerUtil.log(true, "Initializing rainbow table structure (chain length is set to " + CHAIN_LENGTH + ")...");
            rainbowTable = new RainbowTable(PLAIN_TEXT, CHAIN_LENGTH, commonlyUsedPasswords.size(), des, reductionFunction);

            LoggerUtil.log(true, "Generating chains in " + (PARALLEL_MODE ? "parallel" : "sequential") + " mode...");
            generatingStartTime = System.currentTimeMillis();
            if (generateInParallelMode) {
                runParallel(rainbowTable, commonlyUsedPasswords);
            } else {
                runSequential(rainbowTable, commonlyUsedPasswords);
            }
            generatingEndTime = System.currentTimeMillis();

            LoggerUtil.log(true, "Chains have been generated!");

            if (TABLES_OUTPUT_FILE_PATH != null) {
                LoggerUtil.log(true, "Saving rainbow table to file at " + TABLES_OUTPUT_FILE_PATH);
                FileUtil.saveOutputFile(TABLES_OUTPUT_FILE_PATH, rainbowTable);
                LoggerUtil.log(true, "Rainbow table has been successfully saved");
            }
        }

        rainbowTable.printTable();

        LoggerUtil.log(true, "Cracking...");
        crackingStartTime = System.currentTimeMillis();
        if (crackInParallelMode) {
            rainbowTable.crackKeyParallel(cipher);
        } else {
            rainbowTable.crackKeySequential(cipher);
        }
        crackingEndTime = System.currentTimeMillis();

        if (generatingStartTime != null) {
            LoggerUtil.log(true, "\nGenerating has been completed in " + (generateInParallelMode ? "parallel" : "sequential") + " mode in " + (generatingEndTime - generatingStartTime) + " ms");
        } else {
            LoggerUtil.log(true, "");
        }
        LoggerUtil.log(true, "Cracking has been completed in " + (crackInParallelMode ? "parallel" : "sequential") + " mode in " + (crackingEndTime - crackingStartTime) + " ms");
    }


    private static void runParallel(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for (String password : commonlyUsedPasswords) {
            executor.submit(() -> rainbowTable.generateChain(password));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                logger.error("Timeout occurred while waiting for executor to finish");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error while waiting for executor to finish", e);
            executor.shutdownNow();
        }
    }

    private static void runSequential(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        commonlyUsedPasswords.forEach(rainbowTable::generateChain);
    }
}