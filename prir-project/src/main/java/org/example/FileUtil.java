package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final String UNIQUE_STRING_SEPARATOR = "\\$\\$\\$END_OF_HASH\\$\\$\\$";

    public static RainbowTable readRainbowTable(String filePath) {
        List<String[]> table = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            reader.close();
            String[] entries = content.toString().split(UNIQUE_STRING_SEPARATOR);
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] row = entry.trim().split(" ", 2);
                    if (row.length == 2) {
                        table.add(row);
                    } else {
                        logger.error("Error parsing entry: " + entry);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Des des = new Des();
        ReductionFunction reductionFunction = new ReductionFunction();

        RainbowTable rainbowTable = new RainbowTable(Main.PLAIN_TEXT, Main.CHAIN_LENGTH, table.size(), des, reductionFunction);
        String[][] tableArray = new String[table.size()][2];
        for (int i = 0; i < table.size(); i++) {
            tableArray[i] = table.get(i);
        }
        rainbowTable.setTable(tableArray);
        return rainbowTable;
    }


    public static void saveRainbowTable(String filePath, RainbowTable rainbowTable) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (String[] row : rainbowTable.getTable()) {
                writer.write(row[0] + " " + row[1]);
                writer.write("$$$END_OF_HASH$$$");
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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