package org.example.core;

public class ReductionFunction {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private static final int KEY_LENGTH = 8;

    public String reduceHash(String hash, int index) {
        return mapHashToCharset(hash, index);
    }

    private String mapHashToCharset(String hash, int index) {
        StringBuilder reducedKey = new StringBuilder(KEY_LENGTH);

        for (int i = 0; i < KEY_LENGTH; i++) {
            int hashIndex = getHashIndex(hash.length(), index, i);
            char hashChar = hash.charAt(hashIndex);
            int charsetIndex = getCharsetIndex(hashChar, index, i);
            reducedKey.append(CHARSET.charAt(charsetIndex));
        }

        return reducedKey.toString();
    }

    private int getCharsetIndex(char hashChar, int index, int i) {
        return (CHARSET.indexOf(hashChar) + index + i) % CHARSET.length();
    }

    private int getHashIndex(int hashLength, int index, int i) {
        return (index + i) % hashLength;
    }
}