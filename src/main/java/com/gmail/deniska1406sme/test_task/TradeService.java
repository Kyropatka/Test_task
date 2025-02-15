package com.gmail.deniska1406sme.test_task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final StringRedisTemplate stringRedisTemplate;

    public TradeService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public File enrichTradeAndGenerateCsv(File tradeFile, File enrichFile) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(tradeFile, StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
             BufferedWriter writer = new BufferedWriter(new FileWriter(enrichFile, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("date", "productName", "currency", "price"))) {

            for (CSVRecord record : parser) {
                String date = record.get("date").trim();
                try {
                    LocalDate.parse(date, DATE_FORMATTER); //throw exception if date invalid
                }catch (DateTimeParseException e) {
                    logger.error("Invalid date format: {}.", record.get("date"));
                    continue;//not include trade
                }

                Long productId = Long.parseLong(record.get("productId").trim());
                String productName = stringRedisTemplate.opsForValue().get(String.valueOf(productId));

                if (productName == null || productName.isEmpty()) {
                    productName = "Missing Product Name";
                    logger.warn("Missing match");
                }

                String currency = record.get("currency").trim();
                Double price = Double.parseDouble(record.get("price").trim());

                csvPrinter.printRecord(date, productName, currency, price);
            }
            csvPrinter.flush();
        }
        return enrichFile;
    }
}
