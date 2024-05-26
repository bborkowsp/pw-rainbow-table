package org.example;

public class ReductionFunction {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private static final int KEY_LENGTH = 8;

    public String reduceHash(String hash, int index) {
        return mapHashToCharset(hash, index);
    }

    private String mapHashToCharset(String hash, int index) {
        StringBuilder reducedKey = new StringBuilder(KEY_LENGTH);

        for (int i = 0; i < KEY_LENGTH; i++) {
            int hashIndex = (index + i) % hash.length();
            char hashChar = hash.charAt(hashIndex);
            int charsetIndex = (CHARSET.indexOf(hashChar) + index + i) % CHARSET.length();
            reducedKey.append(CHARSET.charAt(charsetIndex));
        }

        return reducedKey.toString();
    }
}