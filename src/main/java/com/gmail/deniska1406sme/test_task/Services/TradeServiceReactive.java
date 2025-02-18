package com.gmail.deniska1406sme.test_task.Services;

import com.gmail.deniska1406sme.test_task.Model.Trade;
import com.gmail.deniska1406sme.test_task.Parsers.TradeParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TradeServiceReactive {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceReactive.class);
    private final StringRedisTemplate stringRedisTemplate;

    public TradeServiceReactive(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Mono<File> enrichTradeAndGenerateCsvReactive(File tradeFile, File enrichFile, TradeParser tradeParser) {

        ConcurrentHashMap<Long, String> localCache = new ConcurrentHashMap<>();

        return Mono.using(
                () -> new FileInputStream(tradeFile),
                inputStream ->
                {
                    try {
                        return tradeParser.parseTradesFlux(inputStream)
                                .flatMap(trade ->
                                        Mono.fromCallable(() -> {
                                            Long productId = Long.parseLong(trade.getProductName());
                                            String productName = localCache.computeIfAbsent(productId, id -> {
                                                String name = stringRedisTemplate.opsForValue().get(String.valueOf(id));
                                                if (name == null || name.isEmpty()) {
                                                    logger.warn("Missing match for productId: {}", id);
                                                    return "Missing Product Name";
                                                }
                                                return name;
                                            });
                                            return new Trade(trade.getDate(), productName, trade.getCurrency(), trade.getPrice());
                                        }).subscribeOn(Schedulers.boundedElastic())
                                )
                                .collectList()
                                .flatMap(enrichTrades ->
                                        Mono.fromCallable(() -> {
                                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(enrichFile,
                                                    StandardCharsets.UTF_8));

                                                 CSVPrinter csvPrinter = new CSVPrinter(writer,
                                                         CSVFormat.DEFAULT.withHeader("date", "productName", "currency", "price"))) {

                                                for (Trade trade : enrichTrades) {
                                                    csvPrinter.printRecord(
                                                            trade.getDate(),
                                                            trade.getProductName(),
                                                            trade.getCurrency(),
                                                            trade.getPrice()
                                                    );
                                                }
                                                csvPrinter.flush();
                                            }
                                            return enrichFile;
                                        }).subscribeOn(Schedulers.boundedElastic())
                                );
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                }
        );
    }
}

