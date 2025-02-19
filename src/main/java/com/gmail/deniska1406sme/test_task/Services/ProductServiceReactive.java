package com.gmail.deniska1406sme.test_task.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ProductServiceReactive {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceReactive.class);
    private final StringRedisTemplate stringRedisTemplate;

    public ProductServiceReactive(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Mono<Map<Long, String>> getProductNameReactive(File file) {
        return Flux.using(
                        () -> Files.lines(file.toPath(), StandardCharsets.UTF_8),
                        lines -> Flux.fromStream(lines)
                                .skip(1)
                                .map(line -> line.split(","))
                                .flatMap(parts -> {
                                    try {
                                        Long productId = Long.parseLong(parts[0].trim());
                                        String productName = parts[1].trim();
                                        return Mono.just(new AbstractMap.SimpleEntry<>(productId, productName));
                                    } catch (NumberFormatException e) {
                                        logger.warn("Incorrect productId in file CSV: {}", parts[0].trim());
                                        return Mono.empty();
                                    }
                                }),
                        Stream::close
                )
                .subscribeOn(Schedulers.boundedElastic())
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public Mono<Void> saveProductNamesInRedisReactive(Map<Long, String> productNames) {
        return Flux.fromIterable(productNames.entrySet())
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(entry ->
                        Mono.fromRunnable(() -> {
                            stringRedisTemplate.opsForValue().set(String.valueOf(entry.getKey()), entry.getValue());
                        })
                )
                .sequential()
                .then()
                .doOnTerminate(() -> logger.info("Saved product names into Redis. Number of product names: {}", productNames.size()));
    }

    public Mono<Void> loadAndSaveProductReactive(File file) {
        return getProductNameReactive(file)
                .flatMap(this::saveProductNamesInRedisReactive);
    }
}
