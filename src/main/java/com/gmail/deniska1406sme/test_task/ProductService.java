package com.gmail.deniska1406sme.test_task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ProductService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public CompletableFuture<Map<Long, String>> getProductNamesAsync(File file) throws IOException {

        return CompletableFuture.supplyAsync(() -> {
            Map<Long, String> productNames = new ConcurrentHashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                productNames = StreamSupport.stream(csvParser.spliterator(), false)
                        .flatMap(record -> {
                            try {
                                Long productId = Long.parseLong(record.get("productId").trim());
                                String productName = record.get("productName").trim();
                                return Stream.of(new AbstractMap.SimpleEntry<>(productId, productName));
                            } catch (NumberFormatException e) {
                                logger.warn("Incorrect productId in file CSV: {}", record.get("productId").trim());
                                return Stream.empty();
                            }
                        })
                        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

            } catch (IOException e) {
                logger.error("Error reading file product.csv", e);
            }
            return productNames;
        }, executor);
    }

    public CompletableFuture<Void> saveProductNamesInRedisAsync(Map<Long, String> productNames) {
        return CompletableFuture.runAsync(() -> {
            try {
                productNames.entrySet().parallelStream().forEach(entry -> {
                    stringRedisTemplate.opsForValue().set(String.valueOf(entry.getKey()), entry.getValue());
                });
                logger.info("Saved product names into Redis. Number of product names: {}", productNames.size());
            } catch (Exception e) {
                logger.error("Error saving product names into Redis", e);
            }
        }, executor);
    }

    public CompletableFuture<Void> loadAndSaveProductsAsync(File file) throws IOException {
        return getProductNamesAsync(file)
                .thenCompose(this::saveProductNamesInRedisAsync);
    }
}
