package org.example;

import java.util.ArrayList;
import java.util.List;

public class RainbowTable {
    private static final int DES_KEY_LENGTH = 8;
    private final String[][] table;
    private final int chainLength;
    private final int numberOfChains;
    private int currentChainRow = -1;
    private final Des des = new Des();
    private final ReductionFunction reductionFunction = new ReductionFunction();

    public RainbowTable(int chainLength, int numberOfChains) {
        this.chainLength = chainLength;
        this.numberOfChains = numberOfChains;

        table = new String[numberOfChains][2];
    }

    void generateRainbowTable(String startPassword) {
        System.out.println("Generating rainbow table for password: " + startPassword);

        startPassword = new String(adjustKeyLength(startPassword.getBytes()));
        byte[] currentKey = startPassword.getBytes();
        byte[] currentHash = new byte[0];

        System.out.print("START > " + new String(currentKey));

        for (int k = 0; k < chainLength; k++) {
            try {
                currentHash = des.cipherPassword(Main.PLAIN_TEXT, new String(currentKey));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            System.out.print(" ----hash----> " + new String(currentHash));

            currentKey = reductionFunction.reduceHash(currentHash).getBytes();

            if (k < chainLength - 1) {
                System.out.print(" ----reduction----> " + new String(currentKey));
            }
        }
        System.out.print(" > END\n");

        currentChainRow++;
        table[currentChainRow][0] = startPassword;
        table[currentChainRow][1] = new String(currentHash);
    }

    public void crackKey(byte[] cipher) {
        byte[] currentHash = cipher;
        byte[] currentKey;

        for (int i = chainLength - 1; i >= 0; i--) {
            System.out.println("Searching for key in chains with hash " + new String(currentHash));

            for (String[] entry : table) {
                System.out.println("Checking entry: " + entry[0] + " with final hash " + entry[1] + " with current hash " + new String(currentHash));
                if (entry[1].equalsIgnoreCase(new String(currentHash))) {
                    System.out.println("Key found in entry beginning from password " + entry[0] + " with hash " + entry[1]);

                    String startPassword = entry[0];
                    currentKey = startPassword.getBytes();

                    for (int j = 0; j < chainLength; j++) {
                        try {
                            currentHash = des.cipherPassword(Main.PLAIN_TEXT, new String(currentKey));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        if (new String(currentHash).equals(new String(cipher))) {
                            System.out.println("\nKey cracked: " + new String(currentKey));
                            return;
                        }

                        currentKey = reductionFunction.reduceHash(currentHash).getBytes();
                    }

                    return;
                }
            }

            System.out.println("Key not found in the current chains");

            currentKey = reductionFunction.reduceHash(currentHash).getBytes();

            System.out.print(new String(currentHash) + " ----reduction----> " + new String(currentKey));

            try {
                currentHash = des.cipherPassword(Main.PLAIN_TEXT, new String(currentKey));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            System.out.println(" ----hash----> " + new String(currentHash));
        }

        System.out.println("\nKey not found in the rainbow table chains");
    }

    public void printTable() {
        System.out.println("Current table:");
        for (String[] entry : table) {
            System.out.println(entry[0] + " -> " + entry[1]);
        }
    }

    // zeby upewnic sie ze dlugosc klucza wynosi 8 (wymagane dla algorytmu DES)
    public static byte[] adjustKeyLength(byte[] key) {
        byte[] adjustedKey = new byte[DES_KEY_LENGTH];
        for (int i = 0; i < DES_KEY_LENGTH; i++) {
            adjustedKey[i] = i < key.length ? key[i] : 0;
        }
        return adjustedKey;
    }

}
