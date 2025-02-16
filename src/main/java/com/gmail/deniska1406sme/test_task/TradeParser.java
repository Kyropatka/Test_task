package com.gmail.deniska1406sme.test_task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradeParser {

    private static final Logger logger = LoggerFactory.getLogger(TradeParser.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<Trade> parseTrades(File tradeFile) throws IOException {
        List<Trade> rawTrades = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tradeFile, StandardCharsets.UTF_8));
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : parser) {
                String date = record.get("date").trim();
                try {
                    LocalDate.parse(date, formatter);//throw exception if date invalid
                } catch (DateTimeParseException e) {
                    logger.error("Invalid date format: {}.", record.get("date"));
                    continue;//not include trade
                }

                String productId = record.get("productId").trim();
                String currency = record.get("currency").trim();
                Double price = Double.parseDouble(record.get("price").trim());

                rawTrades.add(new Trade(date, productId, currency, price));
            }
        } catch (IOException e) {
            logger.error("Error reading trade file", e);
        }
        return rawTrades;
    }
}
