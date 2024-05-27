package org.example;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Getter
@Setter
public class RainbowTable {
    private static final Logger logger = LoggerFactory.getLogger(RainbowTable.class);
    private static final int DES_KEY_LENGTH = 8;
    private final int chainLength;
    private final int numberOfChains;
    private String[][] table;
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

        String currentKey = startPassword;
        String currentHash = "";
        int indexOfReductionFunctionInChainSequence;
        StringBuilder cipherChain = new StringBuilder("START > " + currentKey);

        for (int i = 0; i < chainLength; i++) {
            try {
                currentHash = des.cipherPassword(plainText, currentKey);
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return;
            }

            cipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentHash));
            indexOfReductionFunctionInChainSequence = i + 1;
            currentKey = reductionFunction.reduceHash(currentHash, indexOfReductionFunctionInChainSequence);
            if (i < chainLength - 1) {
                cipherChain.append(" ----reduction ").append(indexOfReductionFunctionInChainSequence).append("----> ").append(currentKey);
            }
        }

        cipherChain.append(" > END\n");
        LoggerUtil.log(false, "Generated chain: " + cipherChain);
        updateTable(startPassword, currentHash);
    }

    private void updateTable(String startPassword, String currentHash) {
        synchronized (lock) {
            currentChainRow++;
            table[currentChainRow][0] = startPassword;
            table[currentChainRow][1] = currentHash;
        }
    }

    public void crackKeySequential(String cipher) {
        for (int i = chainLength - 1; i >= 1; i--) {
            String returned = crackKey(cipher, i);
            if (returned != null) {
                LoggerUtil.log(true, "\nKey cracked: " + returned);
                return;
            }
        }
        LoggerUtil.log(true, "\nKey not found in the rainbow table chains");
    }

    public void crackKeyParallel(String cipher) {
        ExecutorService executor = Executors.newFixedThreadPool(Main.NUMBER_OF_THREADS);
        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = chainLength - 1; i >= 1; i--) {
            final int index = i;
            tasks.add(() -> crackKey(cipher, index));
        }

        try {
            List<Future<String>> results = executor.invokeAll(tasks);
            for (Future<String> result : results) {
                try {
                    String returned = result.get();
                    if (returned != null) {
                        LoggerUtil.log(true, "\nKey cracked: " + returned);
                        executor.shutdown();
                        return;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        LoggerUtil.log(true, "\nKey not found in the rainbow table chains");
    }

    private String crackKey(String cipher, int startReductionIndex) {
        String currentHash = cipher;
        String currentKey;
        int currentReductionIndex = startReductionIndex;
        LoggerUtil.log(false, "-------------- Current reduction index: " + currentReductionIndex + "--------------");

        StringBuilder cipherChain = new StringBuilder(HashLoggerUtil.getHashSubstitute(currentHash));

        for (int i = 0; i < chainLength + 1; i++) {
            LoggerUtil.log(false, "Searching for key in chains with hash " + HashLoggerUtil.getHashSubstitute(currentHash) + " (try number " + (i + 1) + " of " + (chainLength + 1) + ")");

            for (String[] chain : table) {
                LoggerUtil.log(false, "Checking chain: " + chain[0] + " with final hash " + HashLoggerUtil.getHashSubstitute(chain[1]) + " with current hash " + HashLoggerUtil.getHashSubstitute(currentHash));
                if (chain[1].equals(currentHash)) {
                    LoggerUtil.log(false, "Key found in chain beginning from password " + chain[0] + " with hash " + HashLoggerUtil.getHashSubstitute(chain[1]));
                    String startPassword = chain[0];
                    currentKey = startPassword;

                    String currentChainHash;
                    int reductionIndex;
                    StringBuilder currentCipherChain = new StringBuilder("START > " + currentKey);

                    for (int j = 0; j < chainLength; j++) {
                        try {
                            currentChainHash = des.cipherPassword(plainText, currentKey);
                        } catch (Exception e) {
                            logger.error("Error while ciphering password", e);
                            return null;
                        }
                        currentCipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentChainHash));
                        if (currentChainHash.equalsIgnoreCase(cipher)) {
                            LoggerUtil.log(false, "Key found in chain: " + currentCipherChain.toString());
                            return currentKey;
                        }
                        reductionIndex = j + 1;
                        currentKey = reductionFunction.reduceHash(currentChainHash, reductionIndex);
                        if (j < chainLength - 1) {
                            currentCipherChain.append(" ----reduction ").append(reductionIndex).append("----> ").append(currentKey);
                        }
                    }
                    LoggerUtil.log(false, "Key found in chain but did not match the cipher: " + currentCipherChain);
                }
            }
            LoggerUtil.log(false, "Key not found in the current chains");
            if (currentReductionIndex >= chainLength) {
                break;
            }
            currentKey = reductionFunction.reduceHash(currentHash, currentReductionIndex);
            cipherChain.append(" ----reduction ").append(currentReductionIndex).append("----> ").append(currentKey);
            currentReductionIndex++;

            try {
                currentHash = des.cipherPassword(plainText, currentKey);
            } catch (Exception e) {
                logger.error("Error while ciphering password", e);
                return null;
            }
            cipherChain.append(" ----hash----> ").append(HashLoggerUtil.getHashSubstitute(currentHash));
            LoggerUtil.log(false, "Current deciphering chain: " + cipherChain);
        }
        return null;
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
