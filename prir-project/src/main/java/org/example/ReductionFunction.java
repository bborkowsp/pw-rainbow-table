package org.example;

public class ReductionFunction {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private static final int KEY_LENGTH = 8;

    public String reduceHash(byte[] hash, int index) {
        int sum = calculateSumOfBytesInHash(hash);
        return mapSumToCharset(sum);
    }

    private String mapSumToCharset(int sum) {
        StringBuilder reducedKey = new StringBuilder(KEY_LENGTH);

        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = (sum + i) % CHARSET.length();
            reducedKey.append(CHARSET.charAt(index));
        }
        return reducedKey.toString();
    }

    private int calculateSumOfBytesInHash(byte[] hash) {
        int sum = 0;
        for (byte b : hash) {
            sum += b & 0xFF;
        }
        return sum;
    }

}