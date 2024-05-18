package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String FILE_PATH = "src/main/resources/test.txt"; //"src/main/resources/10-million-password-list-top-10000.txt";
    public static final String PLAIN_TEXT = "00000000";
    private static final Integer CHAIN_LENGTH = 3;

    public static void main(String[] args) {
        String key = "12345678"; // mozna tez uzyc qrstuvwx bo to jeden ze zredukowanych kluczy pochodzacy od hasla 12345678

        Des des = new Des();
        byte[] cipher = new byte[0];
        try {
            cipher = des.cipherPassword(PLAIN_TEXT, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Starting... Ciphering " + PLAIN_TEXT + " with key " + key + " so it will be " + new String(cipher));
        System.out.println("Reduction function for returned hash is " + new ReductionFunction().reduceHash(cipher));

        List<String> commonUsedPasswords = getCommonUsedPasswords();
        System.out.println("Loaded " + commonUsedPasswords.size() + " passwords");

        RainbowTable rainbowTable = new RainbowTable(CHAIN_LENGTH, commonUsedPasswords.size());

        System.out.println("Generating chains...");
        commonUsedPasswords.forEach(rainbowTable::generateRainbowTable);
        System.out.println("Chains have been generated!");

        rainbowTable.printTable();

        System.out.println("Cracking...");
        rainbowTable.crackKey(cipher);
    }

    private static List<String> getCommonUsedPasswords() {
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
}