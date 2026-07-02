package com.paycore.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${paycore.idempotency.ttl-hours:24}")
    private long ttlHours;

    public <T> Optional<T> get(String key, Class<T> type) {
        String cached = redisTemplate.opsForValue().get(cacheKey(key));
        if (cached == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cached, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void put(String key, Object response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey(key), json, Duration.ofHours(ttlHours));
        } catch (Exception ignored) {
        }
    }

    private String cacheKey(String key) {
        return "idempotency:" + key;
    }
}
