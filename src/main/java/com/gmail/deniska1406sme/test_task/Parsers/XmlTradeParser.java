package com.gmail.deniska1406sme.test_task.Parsers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gmail.deniska1406sme.test_task.Model.Trade;
import com.gmail.deniska1406sme.test_task.Model.TradeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class XmlTradeParser implements TradeParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlTradeParser.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        TradeWrapper wrapper = xmlMapper.readValue(inputStream, TradeWrapper.class);
        return wrapper.getTrades().stream()
                .filter(trade -> {
                    try {
                        LocalDate.parse(trade.getDate(), formatter);
                        return true;
                    } catch (DateTimeParseException e) {
                        logger.error("Invalid date format: {}", trade.getDate());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Flux<Trade> parseTradesFlux(InputStream inputStream) throws IOException {
        TradeWrapper wrapper = xmlMapper.readValue(inputStream, TradeWrapper.class);
        if (wrapper.getTrades() == null) {
            logger.error("No trades found in XML");
            return Flux.empty();
        }
        return Flux.fromIterable(wrapper.getTrades())
                .filter(trade -> {
                    try {
                        LocalDate.parse(trade.getDate(), formatter);
                        return true;
                    }catch (DateTimeParseException e) {
                        logger.error("Invalid date format: {}", trade.getDate());
                        return false;
                    }
                });
    }
}
