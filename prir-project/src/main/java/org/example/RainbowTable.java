package org.example;

import java.util.ArrayList;
import java.util.List;

public class RainbowTable {
    private final List<List<String>> table = new ArrayList<>();
    private final Integer chainLength;
    private int currentChainRow = 0;
    private final Des des = new Des();
    private final ReductionFunction reductionFunction = new ReductionFunction();

    public RainbowTable(Integer chainLength) {
        this.chainLength = chainLength;
    }


    void generateRainbowTable(String password) {
        table.get(this.currentChainRow).add(password);
        this.currentChainRow++;
        generateChain(password);
    }

    private void generateChain(String password) {
        String lastPassword = "";
        try {
            lastPassword = makeSequenceOfHashingAndReduction(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        table.get(this.currentChainRow).add(lastPassword);
    }

    private String makeSequenceOfHashingAndReduction(String password) throws Exception {
        int sequenceNumber = 0;
        byte[] passwordHash = des.cipherPassword("batman", password);
        String passwordReduced = reductionFunction.reduceHash(passwordHash);
        for (int i = 1; i < this.chainLength; i++) {
            passwordHash = des.cipherPassword("batman", passwordReduced);
            passwordReduced = reductionFunction.reduceHash(passwordHash);
        }
        return passwordReduced;
    }


    public void crackKey(byte[] cipher) {
        
    }
}
