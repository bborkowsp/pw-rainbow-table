package org.example;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final String FILE_PATH = "src/main/resources/test.txt";
    public static final String PLAIN_TEXT = "000000000 000000000 000000000 000000000 000000000 000000000 1234";
    public static Integer CHAIN_LENGTH = 3;
    private static final String KEY = "11111111";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Boolean PARALLEL_MODE = true;
    private static String INPUT_FILE_PATH;
    private static String OUTPUT_FILE_PATH;
    public static Boolean DEBUG = false;


    public static void main(String[] args) {
        parseArguments(args);

        LoggerUtil.log(true, "Starting...");

        Long generatingStartTime = null, generatingEndTime = null;
        Long crackingStartTime, crackingEndTime;
        Des des = new Des();
        ReductionFunction reductionFunction = new ReductionFunction();
        String cipher = des.getCipher(PLAIN_TEXT, KEY);

        LoggerUtil.log(false, "Ciphered " + PLAIN_TEXT + " with key " + KEY + " is equal to " + HashLoggerUtil.getHashSubstitute(cipher));
        LoggerUtil.log(false, "Reduction function for the returned cipher is " + reductionFunction.reduceHash(cipher, 1));

        RainbowTable rainbowTable;

        if (INPUT_FILE_PATH != null) {
            LoggerUtil.log(true, "Loading rainbow table from file at " + INPUT_FILE_PATH);
            rainbowTable = FileUtil.readRainbowTable(INPUT_FILE_PATH);
            LoggerUtil.log(true, "Rainbow table has been successfully loaded");
        } else {
            LoggerUtil.log(true, "Loading commonly used passwords...");
            List<String> commonlyUsedPasswords = FileUtil.getCommonlyUsedPasswords(FILE_PATH);
            LoggerUtil.log(true, "Loaded " + commonlyUsedPasswords.size() + " passwords");

            LoggerUtil.log(true, "Initializing rainbow table structure...");
            rainbowTable = new RainbowTable(PLAIN_TEXT, CHAIN_LENGTH, commonlyUsedPasswords.size(), des, reductionFunction);

            LoggerUtil.log(true, "Generating chains in " + (PARALLEL_MODE ? "parallel" : "sequential") + " mode...");
            generatingStartTime = System.currentTimeMillis();
            if (PARALLEL_MODE) {
                runParallel(rainbowTable, commonlyUsedPasswords);
            } else {
                runSequential(rainbowTable, commonlyUsedPasswords);
            }
            generatingEndTime = System.currentTimeMillis();

            LoggerUtil.log(true, "Chains have been generated!");

            if (OUTPUT_FILE_PATH != null) {
                LoggerUtil.log(true, "Saving rainbow table to file at " + OUTPUT_FILE_PATH);
                FileUtil.saveRainbowTable(OUTPUT_FILE_PATH, rainbowTable);
                LoggerUtil.log(true, "Rainbow table has been successfully saved");
            }
        }

        rainbowTable.printTable();

        LoggerUtil.log(true, "Cracking...");
        crackingStartTime = System.currentTimeMillis();
        if (PARALLEL_MODE) {
            rainbowTable.crackKeyParallel(cipher);
        } else {
            rainbowTable.crackKeySequential(cipher);
        }
        crackingEndTime = System.currentTimeMillis();

        if (generatingStartTime != null) {
            LoggerUtil.log(true, "\nGenerating has been completed in " + (generatingEndTime - generatingStartTime) + " ms");
        } else {
            LoggerUtil.log(true, "");
        }
        LoggerUtil.log(true, "Cracking has been completed in " + (crackingEndTime - crackingStartTime) + " ms");
    }

    public static void parseArguments(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        boolean debug, sequential, parallel, input, output, chainLength;

        try {
            cmd = parser.parse(options, args);

            debug = cmd.hasOption("d");
            if (debug) {
                DEBUG = true;
            }

            sequential = cmd.hasOption("s");
            parallel = cmd.hasOption("p");

            if (sequential && parallel) {
                throw new ParseException("Cannot run the app in both sequential and parallel mode. Please choose one.");
            } else {
                PARALLEL_MODE = !sequential;
            }

            input = cmd.hasOption("i");
            output = cmd.hasOption("o");

            if (input && output) {
                throw new ParseException("Cannot provide both input and output file paths. Please choose one.");
            }

            chainLength = cmd.hasOption("c");
            if (output && !chainLength) {
                throw new ParseException("You need to provide chain length when using -o option");
            }

            if (input) INPUT_FILE_PATH = cmd.getOptionValue("i");
            if (output) OUTPUT_FILE_PATH = cmd.getOptionValue("o");
            if (chainLength) CHAIN_LENGTH = Integer.parseInt(cmd.getOptionValue("c"));

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("main application", options);

            System.exit(1);
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("s", "sequential", false, "sequential mode");
        options.addOption("p", "parallel", false, "parallel mode");
        options.addOption("d", "debug", false, "debug mode");

        Option inputOption = new Option("i", "input", true, "input path to load the rainbow table");
        inputOption.setRequired(false);
        options.addOption(inputOption);

        Option outputOption = new Option("o", "output", true, "output path to store the rainbow table");
        outputOption.setRequired(false);
        options.addOption(outputOption);

        Option chainLengthOption = new Option("c", "chain-length", true, "chain length required for -o option");
        chainLengthOption.setRequired(false);
        options.addOption(chainLengthOption);

        return options;
    }

    private static void runParallel(RainbowTable rainbowTable, List<String> commonlyUsedPasswords) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

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