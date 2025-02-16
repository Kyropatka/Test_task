package com.gmail.deniska1406sme.test_task.Services;

import com.gmail.deniska1406sme.test_task.Model.Trade;
import com.gmail.deniska1406sme.test_task.Parsers.TradeParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private final StringRedisTemplate stringRedisTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public TradeService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public CompletableFuture<File> enrichTradeAndGenerateCsvAsync(File tradeFile, File enrichFile, TradeParser parser) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<Long, String> localCache = new ConcurrentHashMap<>();
            try (InputStream inputStream = new FileInputStream(tradeFile);
                 BufferedWriter writer = new BufferedWriter(new FileWriter(enrichFile, StandardCharsets.UTF_8));
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("date", "productName", "currency", "price"))) {

                List<Trade> rawTrades = parser.parseTrades(inputStream);

                List<List<Object>> records = rawTrades.parallelStream()
                        .map(trade -> {
                            String date = trade.getDate();
                            Long productId = Long.parseLong(trade.getProductName());
                            String productName = localCache.computeIfAbsent(productId, id -> {
                                String name = stringRedisTemplate.opsForValue().get(String.valueOf(id));
                                if (name == null || name.isEmpty()) {
                                    logger.warn("Missing match for productId: {}", id);
                                    return "Missing Product Name";
                                }
                                return name;
                            });
                            return Arrays.<Object>asList(date, productName, trade.getCurrency(), trade.getPrice());
                        })
                        .toList();

                records.forEach(record -> {
                    try {
                        csvPrinter.printRecord(record);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                csvPrinter.flush();
            } catch (IOException e) {
                logger.error("Error processing trade file", e);
                throw new RuntimeException(e);
            }
            return enrichFile;
        }, executor);
    }
}
