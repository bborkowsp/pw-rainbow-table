package org.example;

public class ReductionFunction {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private static final int CHARSET_LENGTH = CHARSET.length();
    private static final int KEY_LENGTH = 8;

    public String reduceHash(byte[] hash) {
        StringBuilder reducedKey = new StringBuilder(KEY_LENGTH);

        int sum = 0;
        for (byte b : hash) {
            sum += b & 0xFF;
        }

        // mapowanie sumy na znaki z zestawu dozwolonych znaków
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = (sum + i) % CHARSET_LENGTH;
            reducedKey.append(CHARSET.charAt(index));
        }

        return reducedKey.toString();
    }

}