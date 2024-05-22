package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RainbowTable {
    private static final int DES_KEY_LENGTH = 8;
    private final String[][] table;
    private final String plainText;
    private final int chainLength;
    private final int numberOfChains;
    private int currentChainRow = -1;
    private Des des;
    private ReductionFunction reductionFunction;

    public RainbowTable(String plainText, int chainLength, int numberOfChains) {
        this.plainText = plainText;
        this.chainLength = chainLength;
        this.numberOfChains = numberOfChains;

        table = new String[numberOfChains][2];
        des = new Des();
        reductionFunction = new ReductionFunction();
    }

    public void generateChain(String startPassword) {
        Main.log("Generating rainbow table for password: " + startPassword);
        startPassword = new String(adjustKeyLength(startPassword.getBytes()));
        byte[] currentKey = startPassword.getBytes();
        byte[] currentHash = new byte[0];
        Main.logWithoutBreakingTheLine("START > " + new String(currentKey));
        for (int k = 0; k < chainLength; k++) {
            try {
                currentHash = des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Main.logWithoutBreakingTheLine(" ----hash----> " + new String(currentHash));
            currentKey = reductionFunction.reduceHash(currentHash).getBytes();
            if (k < chainLength - 1) {
                Main.logWithoutBreakingTheLine(" ----reduction----> " + new String(currentKey));
            }
        }
        Main.logWithoutBreakingTheLine(" > END\n");

        currentChainRow++;
        table[currentChainRow][0] = startPassword;
        table[currentChainRow][1] = new String(currentHash);
    }

    public void crackKey(byte[] cipher) {
        byte[] currentHash = cipher;
        byte[] currentKey;

        for (int i = chainLength - 1; i >= 0; i--) {
            Main.log("Searching for key in chains with hash " + new String(currentHash));
            for (String[] entry : table) {
                Main.log("Checking entry: " + entry[0] + " with final hash " + entry[1] + " with current hash " + new String(currentHash));
                if (entry[1].equalsIgnoreCase(new String(currentHash))) {
                    Main.log("Key found in entry beginning from password " + entry[0] + " with hash " + entry[1]);
                    String startPassword = entry[0];
                    currentKey = startPassword.getBytes();

                    for (int j = 0; j < chainLength + 1; j++) {
                        try {
                            currentHash = des.cipherPassword(plainText, new String(currentKey));
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
                }
            }
            Main.log("Key not found in the current chains");
            currentKey = reductionFunction.reduceHash(currentHash).getBytes();
            Main.logWithoutBreakingTheLine(new String(currentHash) + " ----reduction----> " + new String(currentKey));

            try {
                currentHash = des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Main.log(" ----hash----> " + new String(currentHash));
        }
        System.out.println("\nKey not found in the rainbow table chains");
    }

    public void printTable() {
        Main.log("Current table:");
        for (String[] entry : table) {
            Main.log(entry[0] + " -> " + entry[1]);
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
