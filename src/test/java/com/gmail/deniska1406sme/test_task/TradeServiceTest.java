package com.gmail.deniska1406sme.test_task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TradeServiceTest {

    private TradeService tradeService;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    public void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        tradeService = new TradeService(stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testEnrichTradeAndGenerateCsv() throws IOException {

        File tradeFile = new File("src/test/resources/test_trade.csv");
        File enrichFile = new File("src/test/resources/test_enrich.csv");

        when(stringRedisTemplate.opsForValue().get("1")).thenReturn("VeryBig Company");
        when(stringRedisTemplate.opsForValue().get("2")).thenReturn("");

        File result = tradeService.enrichTradeAndGenerateCsv(tradeFile, enrichFile);

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

        verify(stringRedisTemplate, atLeast(2)).opsForValue();
        verify(valueOperations, atLeast(2)).get(anyString());
    }

    @Test
    void testInvalidDateFormat() throws IOException {

        File tradeFile = new File("src/test/resources/test_invalid_date_trade.csv");
        File enrichFile = new File("src/test/resources/enriched_invalid_date_trade.csv");

        when(stringRedisTemplate.opsForValue().get("2")).thenReturn("VeryBig Company");

        File result = tradeService.enrichTradeAndGenerateCsv(tradeFile, enrichFile);

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
}
