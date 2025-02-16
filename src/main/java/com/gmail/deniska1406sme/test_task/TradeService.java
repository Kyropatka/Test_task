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
import java.util.ArrayList;
import java.util.List;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private final TradeParser tradeParser;
    private final TradeEnricher tradeEnricher;

    public TradeService(TradeParser tradeParser, TradeEnricher tradeEnricher) {
        this.tradeParser = tradeParser;
        this.tradeEnricher = tradeEnricher;
    }

    public File enrichTradeAndGenerateCsv(File tradeFile, File enrichFile) throws IOException {

        List<Trade> rawTrade = tradeParser.parseTrades(tradeFile);
        List<Trade> enrichedTrade = tradeEnricher.enrichTrades(rawTrade);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(enrichFile, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("date", "productName", "currency", "price"))) {

            for (Trade trade : enrichedTrade) {
                csvPrinter.printRecord(trade.getDate(), trade.getProductName(), trade.getCurrency(), trade.getPrice());
            }
            csvPrinter.flush();
        }
        return enrichFile;
    }
}
