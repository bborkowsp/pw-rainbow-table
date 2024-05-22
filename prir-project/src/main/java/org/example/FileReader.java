package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReader {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

    public static List<String> getCommonlyUsedPasswords(String filePath) {
        List<String> commonUsedPasswords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commonUsedPasswords.add(line);
            }
        } catch (IOException e) {
            logger.error("Error while reading file", e);
        }
        return commonUsedPasswords;
    }
}
