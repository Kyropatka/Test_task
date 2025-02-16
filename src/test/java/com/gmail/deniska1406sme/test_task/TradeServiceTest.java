package com.gmail.deniska1406sme.test_task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TradeServiceTest {

    private TradeService tradeService;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;
    private CsvTradeParser csvTradeParser;
    private JsonTradeParser jsonTradeParser;
    private XmlTradeParser xmlTradeParser;

    @BeforeEach
    public void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        csvTradeParser = new CsvTradeParser();
        jsonTradeParser = new JsonTradeParser();
        xmlTradeParser = new XmlTradeParser();
        tradeService = new TradeService(stringRedisTemplate);
    }

    @Test
    void testEnrichTradeAndGenerateCsvAsync() throws IOException {

        File tradeFile = new File("src/test/resources/test_trade.csv");
        File enrichFile = new File("src/test/resources/test_enrich.csv");

        when(valueOperations.get("1")).thenReturn("VeryBig Company");
        when(valueOperations.get("2")).thenReturn("");

        CompletableFuture<File> future = tradeService.enrichTradeAndGenerateCsvAsync(tradeFile, enrichFile, csvTradeParser);
        File result = future.join();

        assertTrue(result.exists());
        assertCsvContent(result);

        verify(stringRedisTemplate, atLeast(2)).opsForValue();
        verify(valueOperations, atLeast(2)).get(anyString());
    }

    @Test
    void testEnrichTradeAndGenerateJsonAsync() throws IOException {
        File tradeFile = new File("src/test/resources/test_trade.json");
        File enrichFile = new File("src/test/resources/test_enrich_json.csv");

        when(valueOperations.get("1")).thenReturn("VeryBig Company");

        CompletableFuture<File> future = tradeService.enrichTradeAndGenerateCsvAsync(tradeFile, enrichFile, jsonTradeParser);
        File result = future.join();

        assertTrue(result.exists());
        assertCsvContent(result);
    }

    @Test
    void testEnrichTradeAndGenerateXmlAsync() throws IOException {
        File tradeFile = new File("src/test/resources/test_trade.xml");
        File enrichFile = new File("src/test/resources/test_enrich_xml.csv");

        when(valueOperations.get("1")).thenReturn("VeryBig Company");

        CompletableFuture<File> future = tradeService.enrichTradeAndGenerateCsvAsync(tradeFile, enrichFile, xmlTradeParser);
        File result = future.join();

        assertTrue(result.exists());
        assertCsvContent(result);
    }


    @Test
    void testInvalidDateFormatAsync() throws IOException {

        File tradeFile = new File("src/test/resources/test_invalid_date_trade.csv");
        File enrichFile = new File("src/test/resources/enriched_invalid_date_trade.csv");

        when(valueOperations.get("2")).thenReturn("VeryBig Company");

        CompletableFuture<File> future = tradeService.enrichTradeAndGenerateCsvAsync(tradeFile, enrichFile, csvTradeParser);
        File result = future.join();

        assertTrue(result.exists());

        try (BufferedReader br = new BufferedReader(new FileReader(result, StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount == 1) {
                    assertTrue(line.contains("date"));
                }
                if (lineCount == 2) {
                    assertTrue(line.contains("VeryBig Company"));
                    assertTrue(line.contains("USD"));
                    assertTrue(line.contains("20.1"));
                }
            }
        }
    }

    private void assertCsvContent(File result) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(result, StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount == 1) {
                    assertTrue(line.contains("date"));
                }
                if (lineCount == 2) {
                    assertTrue(line.contains("VeryBig Company"));
                    assertTrue(line.contains("EUR"));
                    assertTrue(line.contains("10"));
                }
                if (lineCount == 3) {
                    assertTrue(line.contains("Missing Product Name"));
                    assertTrue(line.contains("USD"));
                    assertTrue(line.contains("20.1"));
                }
            }
        }
    }
}
