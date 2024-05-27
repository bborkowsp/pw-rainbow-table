package org.example.utils;

import org.example.core.RainbowTable;
import org.example.core.ReductionFunction;
import org.example.crypto.Des;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.config.AppConfig.CHAIN_LENGTH;
import static org.example.config.AppConfig.PLAIN_TEXT;


public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static RainbowTable readInputFile(String filePath) {
        List<String[]> table = parseInputFile(filePath);
        return createRainbowTable(table);
    }

    public static void saveOutputFile(String filePath, RainbowTable rainbowTable) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(CHAIN_LENGTH.toString());
            writer.newLine();
            for (String[] row : rainbowTable.getTable()) {
                writer.write(row[0] + " " + row[1]);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing to file: " + filePath, e);
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
            logger.error("Error while reading file: " + filePath, e);
        }
        return commonUsedPasswords;
    }

    private static List<String[]> parseInputFile(String filePath) {
        List<String[]> table = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            CHAIN_LENGTH = readChainLength(reader);
            table = readRainbowTable(reader);
        } catch (IOException e) {
            logger.error("Error while reading file: " + filePath, e);
        }
        return table;
    }

    private static RainbowTable createRainbowTable(List<String[]> table) {
        Des des = new Des();
        ReductionFunction reductionFunction = new ReductionFunction();

        RainbowTable rainbowTable = new RainbowTable(
                PLAIN_TEXT,
                CHAIN_LENGTH,
                table.size(),
                des,
                reductionFunction
        );
        rainbowTable.setTable(table.toArray(new String[0][2]));
        return rainbowTable;
    }

    private static List<String[]> readRainbowTable(BufferedReader reader) throws IOException {
        List<String[]> table = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(" ");
            if (row.length != 2) {
                throw new IOException("Invalid row format: " + line);
            }
            table.add(row);
        }
        return table;
    }

    private static Integer readChainLength(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid chain length format: " + line, e);
            }
        } else {
            throw new IOException("Chain length not found in the file.");
        }
    }


}