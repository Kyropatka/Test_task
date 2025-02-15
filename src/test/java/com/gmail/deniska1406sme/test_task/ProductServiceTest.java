package com.gmail.deniska1406sme.test_task;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    private ProductService productService;
    private final Logger logger = LoggerFactory.getLogger(ProductServiceTest.class);
    private ValueOperations<String, String> valueOperations;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        productService = new ProductService(stringRedisTemplate);
    }

    @Test
    public void testGetProductNames_ValidCSV() throws IOException {
        File csvFile = tempDir.resolve("products.csv").toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("productId", "productName"))) {

            csvPrinter.printRecord("1", "BigSuper Company");
            csvPrinter.printRecord("2", "RichNew Company");
            csvPrinter.flush();
        }

        Map<Long, String> productNames = productService.getProductNames(csvFile);

        assertEquals(2, productNames.size());
        assertEquals("BigSuper Company", productNames.get(1L));
        assertEquals("RichNew Company", productNames.get(2L));
    }

    @Test
    void testGetProductNames_InvalidProductId() throws IOException {
        File csvFile = tempDir.resolve("invalid_products.csv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("productId", "productName"))) {

            csvPrinter.printRecord("abc", "InvalidItem");
        }

        Logger mockLogger = mock(Logger.class);
        Map<Long, String> productNames = productService.getProductNames(csvFile);
        assertTrue(productNames.isEmpty(), "Incorrect productId should be ignored");
    }

    @Test
    void testGetProductNames_EmptyFile() throws IOException {
        File emptyFile = tempDir.resolve("empty.csv").toFile();
        emptyFile.createNewFile();

        Map<Long, String> productNames = productService.getProductNames(emptyFile);
        assertTrue(productNames.isEmpty(), "Empty file, should return an empty map");
    }

    @Test
    void testSaveProductNamesInRedis() {
        Map<Long, String> productNames = Map.of(1L, "BigSuper Company", 2L, "RichNew Company");

        productService.saveProductNamesInRedis(productNames);

        verify(valueOperations, times(1)).set("1", "BigSuper Company");
        verify(valueOperations, times(1)).set("2", "RichNew Company");
        verifyNoMoreInteractions(valueOperations);
    }

    @Test
    void testSaveProductNamesInRedis_EmptyMap() {
        productService.saveProductNamesInRedis(Map.of());

        verifyNoInteractions(valueOperations);
    }
}
