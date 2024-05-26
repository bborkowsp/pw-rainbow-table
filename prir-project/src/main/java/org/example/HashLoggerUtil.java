package org.example;

import java.util.HashMap;

public class HashLoggerUtil {

    private static final HashMap<String, String> generatedSubstitutes = new HashMap<>();

    public static String getHashSubstitute(byte[] hash) {
        if (hash == null) return null;
        return getHashSubstitute(new String(hash));
    }

    public static String getHashSubstitute(String hash) {
        if (generatedSubstitutes.containsKey(hash)) {
            return generatedSubstitutes.get(hash);
        }

        String newSubstitute = generateNextSubstitute();
        generatedSubstitutes.put(hash, generateNextSubstitute());
        return newSubstitute;
    }

    private static String generateNextSubstitute() {
        return "HASH_" + generatedSubstitutes.size();
    }

}
