package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static RainbowTable readRainbowTable(String filePath) {
//        List<String> rainbowTable = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                rainbowTable.add(line);
//            }
//        } catch (IOException e) {
//            logger.error("Error while reading file", e);
//        }
        return null;
    }

    public static void saveRainbowTable(String filePath, RainbowTable rainbowTable) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            for (String[] row : rainbowTable.getTable()) {
//                writer.write(row[0] + " " + row[1]);
//                writer.newLine();
//            }
//        } catch (IOException e) {
//            logger.error("Error while writing to file", e);
//        }
    }

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
