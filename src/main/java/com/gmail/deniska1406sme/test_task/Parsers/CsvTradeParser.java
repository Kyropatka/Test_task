package com.gmail.deniska1406sme.test_task.Parsers;

import com.gmail.deniska1406sme.test_task.Model.Trade;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CsvTradeParser implements TradeParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvTradeParser.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            //Moving common logic into a separate method slows down execution time by 10-15 seconds.
            return StreamSupport.stream(parser.spliterator(), false)
                    .filter(record -> {
                        String date = record.get("date").trim();
                        try {
                            LocalDate.parse(date, formatter);//throw exception if date invalid
                            return true;
                        } catch (DateTimeParseException e) {
                            logger.error("Invalid date format: {}.", record.get("date"));
                            return false;//not include trade
                        }
                    })
                    .map(record -> {
                        String date = record.get("date").trim();
                        String productId = record.get("productId").trim();
                        String currency = record.get("currency").trim();
                        Double price = Double.parseDouble(record.get("price").trim());
                        return new Trade(date, productId, currency, price);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error reading trade file", e);
            throw e;
        }
    }

    @Override
    public Flux<Trade> parseTradesFlux(InputStream inputStream) throws IOException {

        return Flux.using(
                () -> CSVParser.parse(
                        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)),
                        CSVFormat.DEFAULT.withFirstRecordAsHeader()),

                //Moving common logic into a separate method slows down execution time by 10-15 seconds.
                parser -> Flux.fromStream(StreamSupport.stream(parser.spliterator(), false))
                        .filter(record -> {
                            String date = record.get("date").trim();
                            try {
                                LocalDate.parse(date, formatter);
                                return true;
                            } catch (DateTimeParseException e) {
                                logger.error("Invalid date format: {}.", record.get("date"));
                                return false;
                            }
                        })
                        .map(record -> {
                            String date = record.get("date").trim();
                            String productId = record.get("productId").trim();
                            String currency = record.get("currency").trim();
                            Double price = Double.parseDouble(record.get("price").trim());
                            return new Trade(date, productId, currency, price);
                        })
        );
    }
}
