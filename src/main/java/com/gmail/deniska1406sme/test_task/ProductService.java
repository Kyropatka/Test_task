package com.gmail.deniska1406sme.test_task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final StringRedisTemplate stringRedisTemplate;

    public ProductService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Map<Long, String> getProductNames(File file) throws IOException {

        Map<Long, String> productNames = new ConcurrentHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                try {
                    Long productId = Long.parseLong(record.get("productId").trim());
                    String productName = record.get("productName").trim();
                    productNames.put(productId, productName);
                } catch (NumberFormatException e) {
                    logger.warn("Incorrect productId in file CSV: {}", record.get("productId").trim());
                }
            }
        } catch (IOException e) {
            logger.error("Error reading file product.csv", e);
        }

        return productNames;
    }

    public void saveProductNamesInRedis(Map<Long, String> productNames) {

        try {
            for (Map.Entry<Long, String> entry : productNames.entrySet()) {
                stringRedisTemplate.opsForValue().set(String.valueOf(entry.getKey()), entry.getValue());
            }
            logger.info("Saved product names into Redis. Number of product names: {}", productNames.size());
        } catch (Exception e) {
            logger.error("Error saving product names into Redis", e);
        }
    }
}
