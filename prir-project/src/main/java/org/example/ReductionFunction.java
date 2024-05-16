package org.example;

public class ReductionFunction {

    public String reduceHash(byte[] passwordHash) {
        StringBuilder scaledCharacters = new StringBuilder();
        for (byte b : passwordHash) {
            int scaledByte = (b % 95) + 32;
            char scaledChar = (char) scaledByte;
            scaledCharacters.append(scaledChar);
        }
        return scaledCharacters.toString();
    }
}