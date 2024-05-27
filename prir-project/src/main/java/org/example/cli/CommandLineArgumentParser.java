package org.example.cli;

import org.apache.commons.cli.*;

import static org.example.config.AppConfig.*;

public class CommandLineArgumentParser {
    
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

            if (input) TABLES_INPUT_FILE_PATH = cmd.getOptionValue("i");
            if (output) TABLES_OUTPUT_FILE_PATH = cmd.getOptionValue("o");
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
                    throw new ParseException("Number of threads cannot be greater than the number of available " + "processors on the current machine. Your machine has " + Runtime.getRuntime().availableProcessors() + " processors.");
                }
            } else {
                NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
            }

            if (cmd.hasOption("passwords")) {
                PASSWORDS_INPUT_FILE_PATH = cmd.getOptionValue("passwords");
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

        Option numberOfThreadsOption = new Option("n", "number-of-threads", true, "number of threads to use in the parallel mode, default is equal to max number of threads on " + "the current machine (" + Runtime.getRuntime().availableProcessors() + ")");
        numberOfThreadsOption.setRequired(false);
        options.addOption(numberOfThreadsOption);

        Option passwordsInputOption = new Option("passwords", "passwords-input", true, "input path to load the commonly used 8-characters passwords, by default a sample list of 500 passwords is used");
        passwordsInputOption.setRequired(false);
        options.addOption(passwordsInputOption);

        return options;
    }
}
