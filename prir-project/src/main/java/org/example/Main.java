package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String FILE_PATH = "src/main/resources/10-million-password-list-top-10000.txt";
    private static final Integer CHAIN_LENGTH = 1000;


    public static void main(String[] args) {
        Des des = new Des();
        byte[] cipher = new byte[0];
        try {
            cipher = des.cipherPassword("qwerty", "01234567");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RainbowTable rainbowTable = new RainbowTable(CHAIN_LENGTH);
        List<String> commonUsedPasswords = getCommonUsedPasswords();
        commonUsedPasswords.forEach(rainbowTable::generateRainbowTable);
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