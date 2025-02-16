package com.gmail.deniska1406sme.test_task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeEnricher {

    private static final Logger logger = LoggerFactory.getLogger(TradeEnricher.class.getName());
    private final StringRedisTemplate stringRedisTemplate;

    public TradeEnricher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public List<Trade> enrichTrades(List<Trade> rawTrades) {
        Map<Long, String> localCache = new HashMap<>();
        List<Trade> enrichedTrades = new ArrayList<>();

        for (Trade rawTrade : rawTrades) {
            Long productId = Long.parseLong(rawTrade.getProductName());
            String productName = localCache.get(productId);
            if (productName == null) { //Local cache to reduce requests to Redis
                productName = stringRedisTemplate.opsForValue().get(String.valueOf(productId));
                localCache.put(productId, productName);
            }
            if (productName == null || productName.isEmpty()) {
                productName = "Missing Product Name";
                logger.warn("Missing match for productId: {}", productId);
            }
            enrichedTrades.add(new Trade(rawTrade.getDate(), productName, rawTrade.getCurrency(), rawTrade.getPrice()));
        }
        return enrichedTrades;
    }
}
