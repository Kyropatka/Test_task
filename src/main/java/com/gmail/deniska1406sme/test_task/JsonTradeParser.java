package com.gmail.deniska1406sme.test_task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class JsonTradeParser implements TradeParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonTradeParser.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        List<Trade> rawTrades = new ArrayList<>();
        JsonNode root = mapper.readTree(inputStream);
        if (root.isArray()) {
            for (JsonNode tradeNode : root) {
                String date = tradeNode.get("date").asText().trim();

                try{
                    LocalDate.parse(date, formatter);
                }catch (DateTimeParseException e){
                    logger.error("Invalid date format: {}", date);
                    continue;
                }

                String productId = tradeNode.get("productId").asText().trim();
                String currency = tradeNode.get("currency").asText().trim();
                Double price = tradeNode.get("price").asDouble();
                rawTrades.add(new Trade(date, productId, currency, price));
            }
        }
        return rawTrades;
    }
}
