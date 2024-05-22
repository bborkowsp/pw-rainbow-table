package org.example;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class RainbowTable {
    private static final Logger logger = LoggerFactory.getLogger(RainbowTable.class);
    private static final int DES_KEY_LENGTH = 8;
    private final int chainLength;
    private final int numberOfChains;
    private final String[][] table;
    private final String plainText;
    private int currentChainRow = -1;
    private static final Object lock = new Object();

    public RainbowTable(String plainText, int chainLength, int numberOfChains) {
        this.plainText = plainText;
        this.chainLength = chainLength;
        this.numberOfChains = numberOfChains;

        table = new String[numberOfChains][2];
    }

    public void generateChain(String startPassword) {
        LoggerUtil.log("Generating rainbow table for password: " + startPassword);

        if (startPassword.length() != 8)
            startPassword = new String(adjustKeyLengthToEightBytes(startPassword.getBytes()));

        byte[] currentKey = startPassword.getBytes();
        byte[] currentHash = new byte[0];
        LoggerUtil.logWithoutPrintingNewLine("START > " + new String(currentKey));

        for (int i = 0; i < chainLength; i++) {
            try {
                currentHash = Des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return;
            }

            LoggerUtil.logWithoutPrintingNewLine(" ----hash----> " + HashLoggerUtil.getHashSubstitute(currentHash));
            currentKey = ReductionFunction.reduceHash(currentHash).getBytes();
            if (i < chainLength - 1) {
                LoggerUtil.logWithoutPrintingNewLine(" ----reduction----> " + new String(currentKey));
            }
        }

        LoggerUtil.logWithoutPrintingNewLine(" > END\n");
        updateTable(startPassword, currentHash);
    }

    private void updateTable(String startPassword, byte[] currentHash) {
        synchronized (lock) {
            currentChainRow++;
            System.out.println("Current chain row: " + currentChainRow);
            table[currentChainRow][0] = startPassword;
            table[currentChainRow][1] = new String(currentHash);
        }
    }

    public void crackKey(byte[] cipher) {
        byte[] currentHash = cipher;
        byte[] currentKey;

        StringBuilder cipherChain = new StringBuilder(HashLoggerUtil.getHashSubstitute(currentHash));

        for (int i = 0; i < chainLength; i++) {
            LoggerUtil.log("Searching for key in chains with hash " + HashLoggerUtil.getHashSubstitute(currentHash));

            for (String[] chain : table) {
                LoggerUtil.log("Checking chain: " + chain[0] + " with final hash " + HashLoggerUtil.getHashSubstitute(chain[1]) + " with current hash " + HashLoggerUtil.getHashSubstitute(currentHash));
                if (chain[1].equalsIgnoreCase(new String(currentHash))) {
                    LoggerUtil.log("Key found in chain beginning from password " + chain[0] + " with hash " + HashLoggerUtil.getHashSubstitute(chain[1]));
                    String startPassword = chain[0];
                    currentKey = startPassword.getBytes();

                    byte[] currentChainHash;

                    for (int j = 0; j < chainLength + 1; j++) {

                        try {
                            currentChainHash = Des.cipherPassword(plainText, new String(currentKey));
                        } catch (Exception e) {
                            logger.error("Error while ciphering password", e);
                            return;
                        }
                        if (new String(currentChainHash).equals(new String(cipher))) {
                            System.out.println("\nKey cracked: " + new String(currentKey));
                            return;
                        }
                        currentKey = ReductionFunction.reduceHash(currentHash).getBytes();
                    }
                    LoggerUtil.log("Key found in chain but did not match the cipher... checking next chain");
                }
            }
            LoggerUtil.log("Key not found in the current chains");
            currentKey = ReductionFunction.reduceHash(currentHash).getBytes();

            cipherChain.append(" ----reduction----> ").append(new String(currentKey));

            try {
                currentHash = Des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return;
            }
            cipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentHash));
            LoggerUtil.log("Current deciphering chain: " + cipherChain);
        }
        LoggerUtil.log("Key not found in the rainbow table chains");
    }

    public void printTable() {
        LoggerUtil.log("Current table:");
        for (String[] chain : table) {
            LoggerUtil.log(chain[0] + " -> " + HashLoggerUtil.getHashSubstitute(chain[1]));
        }
    }

    public static byte[] adjustKeyLengthToEightBytes(byte[] key) {
        byte[] adjustedKey = new byte[DES_KEY_LENGTH];
        for (int i = 0; i < DES_KEY_LENGTH; i++) {
            adjustedKey[i] = i < key.length ? key[i] : 0;
        }
        return adjustedKey;
    }

}
