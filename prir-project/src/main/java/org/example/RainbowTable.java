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

    private final Des des;
    private final ReductionFunction reductionFunction;

    public RainbowTable(String plainText, int chainLength, int numberOfChains, Des des, ReductionFunction reductionFunction) {
        this.plainText = plainText;
        this.chainLength = chainLength;
        this.numberOfChains = numberOfChains;
        this.des = des;
        this.reductionFunction = reductionFunction;

        table = new String[numberOfChains][2];
    }

    public void generateChain(String startPassword) {
        LoggerUtil.log(false, "Generating rainbow table for password: " + startPassword);

        if (startPassword.length() != 8)
            startPassword = new String(adjustKeyLengthToEightBytes(startPassword.getBytes()));

        byte[] currentKey = startPassword.getBytes();
        byte[] currentHash = new byte[0];
        int currentReductionIndex;
        StringBuilder cipherChain = new StringBuilder("START > " + new String(currentKey));

        for (int i = 0; i < chainLength; i++) {
            try {
                currentHash = des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return;
            }

            cipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentHash));
            currentReductionIndex = i + 1;
            currentKey = reductionFunction.reduceHash(currentHash, currentReductionIndex).getBytes();
            if (i < chainLength - 1) {
                cipherChain.append(" ----reduction ").append(currentReductionIndex).append("----> ").append(new String(currentKey));
            }
        }

        cipherChain.append(" > END\n");
        LoggerUtil.log(false, "Generated chain: " + cipherChain);
        updateTable(startPassword, currentHash);
    }

    private void updateTable(String startPassword, byte[] currentHash) {
        synchronized (lock) {
            currentChainRow++;
            table[currentChainRow][0] = startPassword;
            table[currentChainRow][1] = new String(currentHash);
        }
    }

    public void crackKey(byte[] cipher) {
        byte[] currentHash = cipher;
        byte[] currentKey;
        int currentReductionIndex;

        StringBuilder cipherChain = new StringBuilder(HashLoggerUtil.getHashSubstitute(currentHash));

        for (int i = 0; i < chainLength; i++) {
            LoggerUtil.log(false, "Searching for key in chains with hash " + HashLoggerUtil.getHashSubstitute(currentHash));

            for (String[] chain : table) {
                LoggerUtil.log(false, "Checking chain: " + chain[0] + " with final hash " + HashLoggerUtil.getHashSubstitute(chain[1]) + " with current hash " + HashLoggerUtil.getHashSubstitute(currentHash));
                if (chain[1].equalsIgnoreCase(new String(currentHash))) {
                    LoggerUtil.log(false, "Key found in chain beginning from password " + chain[0] + " with hash " + HashLoggerUtil.getHashSubstitute(chain[1]));
                    String startPassword = chain[0];
                    currentKey = startPassword.getBytes();

                    byte[] currentChainHash;

                    for (int j = 0; j < chainLength + 1; j++) {

                        try {
                            currentChainHash = des.cipherPassword(plainText, new String(currentKey));
                        } catch (Exception e) {
                            logger.error("Error while ciphering password", e);
                            return;
                        }
                        if (new String(currentChainHash).equals(new String(cipher))) {
                            System.out.println("\nKey cracked: " + new String(currentKey));
                            return;
                        }
                        currentReductionIndex = j + 1;
                        currentKey = reductionFunction.reduceHash(currentHash, currentReductionIndex).getBytes();
                    }
                    LoggerUtil.log(false, "Key found in chain but did not match the cipher... checking next chain");
                }
            }
            LoggerUtil.log(false, "Key not found in the current chains");
            currentReductionIndex = chainLength - i - 1;
            currentKey = reductionFunction.reduceHash(currentHash, currentReductionIndex).getBytes();

            cipherChain.append(" ----reduction ").append(currentReductionIndex).append("----> ").append(new String(currentKey));

            try {
                currentHash = des.cipherPassword(plainText, new String(currentKey));
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return;
            }
            cipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentHash));
            LoggerUtil.log(false, "Current deciphering chain: " + cipherChain);
        }
        System.out.println("\nKey not found in the rainbow table chains");
    }

    public void printTable() {
        LoggerUtil.log(false, "Current table:");
        for (String[] chain : table) {
            LoggerUtil.log(false, chain[0] + " -> " + HashLoggerUtil.getHashSubstitute(chain[1]));
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
