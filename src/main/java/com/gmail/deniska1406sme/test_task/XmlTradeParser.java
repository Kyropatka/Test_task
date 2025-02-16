package com.gmail.deniska1406sme.test_task;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class XmlTradeParser implements TradeParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlTradeParser.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        TradeWrapper wrapper = xmlMapper.readValue(inputStream, TradeWrapper.class);
        List<Trade> validTrades = new ArrayList<>();

        for(Trade trade : wrapper.getTrades()) {
            try {
                LocalDate.parse(trade.getDate(), formatter);
                validTrades.add(trade);
            }catch (DateTimeParseException e){
                logger.error("Invalid date format: {}", trade.getDate());
            }
        }
        return validTrades;
    }
}
