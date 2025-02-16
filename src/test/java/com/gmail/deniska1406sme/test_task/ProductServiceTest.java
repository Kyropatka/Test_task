package com.gmail.deniska1406sme.test_task;


import com.gmail.deniska1406sme.test_task.Services.ProductService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    private ProductService productService;
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
    public void testGetProductNamesAsync_ValidCSV() throws IOException, ExecutionException, InterruptedException {
        File csvFile = tempDir.resolve("products.csv").toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("productId", "productName"))) {

            csvPrinter.printRecord("1", "BigSuper Company");
            csvPrinter.printRecord("2", "RichNew Company");
            csvPrinter.flush();
        }

        CompletableFuture<Map<Long,String>> future = productService.getProductNamesAsync(csvFile);
        Map<Long, String> productNames = future.get();

        assertEquals(2, productNames.size());
        assertEquals("BigSuper Company", productNames.get(1L));
        assertEquals("RichNew Company", productNames.get(2L));
    }

    @Test
    void testGetProductNamesAsync_InvalidProductId() throws IOException, ExecutionException, InterruptedException {
        File csvFile = tempDir.resolve("invalid_products.csv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("productId", "productName"))) {

            csvPrinter.printRecord("abc", "InvalidItem");
        }

        CompletableFuture<Map<Long, String>> future = productService.getProductNamesAsync(csvFile);
        Map<Long, String> productNames = future.get();

        assertTrue(productNames.isEmpty(), "Incorrect productId should be ignored");
    }

    @Test
    void testGetProductNamesAsync_EmptyFile() throws IOException, ExecutionException, InterruptedException {
        File emptyFile = tempDir.resolve("empty.csv").toFile();
        emptyFile.createNewFile();

        CompletableFuture<Map<Long, String>> future = productService.getProductNamesAsync(emptyFile);
        Map<Long, String> productNames = future.get();

        assertTrue(productNames.isEmpty(), "Empty file, should return an empty map");
    }

    @Test
    void testSaveProductNamesInRedisAsync() throws ExecutionException, InterruptedException {
        Map<Long, String> productNames = Map.of(1L, "BigSuper Company", 2L, "RichNew Company");

        CompletableFuture<Void> future = productService.saveProductNamesInRedisAsync(productNames);
        future.get();

        verify(valueOperations, times(1)).set("1", "BigSuper Company");
        verify(valueOperations, times(1)).set("2", "RichNew Company");
        verifyNoMoreInteractions(valueOperations);
    }

    @Test
    void testSaveProductNamesInRedisAsync_EmptyMap() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = productService.saveProductNamesInRedisAsync(Map.of());
        future.get();

        verifyNoInteractions(valueOperations);
    }

    @Test
    void testLoadProductNamesInRedisAsync() throws IOException, ExecutionException, InterruptedException {
        File csvFile = tempDir.resolve("products.csv").toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8));
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("productId", "productName"))) {

            printer.printRecord("1", "BigSuper Company");
            printer.printRecord("2", "RichNew Company");
            printer.flush();
        }

        CompletableFuture<Void> future = productService.loadAndSaveProductsAsync(csvFile);
        future.get();

        verify(valueOperations, times(1)).set("1", "BigSuper Company");
        verify(valueOperations, times(1)).set("2", "RichNew Company");
        verifyNoMoreInteractions(valueOperations);
    }
}
