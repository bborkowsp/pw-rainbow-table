package org.example.config;

public class AppConfig {
    public static final String FILE_PATH = "src/main/resources/test.txt";
    public static String INPUT_FILE_PATH;
    public static String OUTPUT_FILE_PATH;

    public static String PLAIN_TEXT = "000000000 000000000 000000000 000000000 000000000 000000000 1234";
    public static Integer CHAIN_LENGTH = 100;
    public static Integer NUMBER_OF_THREADS;

    public static String KEY = "sebastia";
    public static String CIPHER_TO_CRACK = null;

    public static Boolean PARALLEL_MODE = true;
    public static final Boolean RUN_CRACKING_ONLY_IN_SEQUENTIAL_MODE = false;
    public static Boolean DEBUG = false;
}
