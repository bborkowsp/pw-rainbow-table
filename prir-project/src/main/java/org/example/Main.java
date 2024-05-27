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
    public static String PLAIN_TEXT = "000000000 000000000 000000000 000000000 000000000 000000000 1234";
    public static Integer CHAIN_LENGTH = 100;
    private static String KEY = "password";
    private static String CIPHER_TO_CRACK = null;
    public static Integer NUMBER_OF_THREADS;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Boolean PARALLEL_MODE = true;
    private static final Boolean RUN_CRACKING_ONLY_IN_SEQUENTIAL_MODE = false;
    private static String INPUT_FILE_PATH;
    private static String OUTPUT_FILE_PATH;
    public static Boolean DEBUG = false;


    public static void main(String[] args) {
        parseArguments(args);

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
            if (generateInParallelMode) {
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
        if (crackInParallelMode) {
            rainbowTable.crackKeyParallel(cipher);
        } else {
            rainbowTable.crackKeySequential(cipher);
        }
        crackingEndTime = System.currentTimeMillis();

        if (generatingStartTime != null) {
            LoggerUtil.log(true, "\nGenerating has been completed in " + (generateInParallelMode ? "parallel" : "sequential") + " mode in " +  (generatingEndTime - generatingStartTime) + " ms");
        } else {
            LoggerUtil.log(true, "");
        }
        LoggerUtil.log(true, "Cracking has been completed in " + (crackInParallelMode ? "parallel" : "sequential") + " mode in " + (crackingEndTime - crackingStartTime) + " ms");
    }

    public static void parseArguments(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        boolean debug, sequential, parallel, input, output, chainLength, key, text, cipher;

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

            key = cmd.hasOption("k");
            text = cmd.hasOption("t");
            cipher = cmd.hasOption("cipher");

            if ((key || text) && cipher) {
                throw new ParseException("Cannot provide both key (or text) and cipher to crack. Please choose one.");
            }

            if (key) {
                KEY = cmd.getOptionValue("k");
                if (KEY.length() != 8) {
                    throw new ParseException("Key must be 8 characters long");
                }
            }
            if (text) PLAIN_TEXT = cmd.getOptionValue("t");
            if (cipher) CIPHER_TO_CRACK = cmd.getOptionValue("cipher");

            if (cmd.hasOption("n")) {
                NUMBER_OF_THREADS = Integer.parseInt(cmd.getOptionValue("n"));
                if (NUMBER_OF_THREADS > Runtime.getRuntime().availableProcessors()) {
                    throw new ParseException("Number of threads cannot be greater than the number of available "
                            + "processors on the current machine");
                }
            } else {
                NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
            }
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

        Option outputOption = new Option("o", "output", true, "output path to store the rainbow table (requires -c option)");
        outputOption.setRequired(false);
        options.addOption(outputOption);

        Option chainLengthOption = new Option("c", "chain-length", true, "chain length required for -o option, default is " + CHAIN_LENGTH);
        chainLengthOption.setRequired(false);
        options.addOption(chainLengthOption);

        Option keyOption = new Option("k", "key", true, "key used to test the rainbow table, default is \"" + KEY + "\"");
        keyOption.setRequired(false);
        options.addOption(keyOption);

        Option cipherOption = new Option("cipher", true, "cipher used to test the rainbow table, by default a key is used instead");
        cipherOption.setRequired(false);
        options.addOption(cipherOption);

        Option textOption = new Option("t", "text", true, "plain text to cipher, default is \"" + PLAIN_TEXT + "\"");
        textOption.setRequired(false);
        options.addOption(textOption);

        Option numberOfThreadsOption = new Option("n", "number-of-threads", true,
                "number of threads to use in the parallel mode, default is equal to max number of threads on "
                        + "the current machine (" + Runtime.getRuntime().availableProcessors() + ")");
        numberOfThreadsOption.setRequired(false);
        options.addOption(numberOfThreadsOption);

        return options;
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