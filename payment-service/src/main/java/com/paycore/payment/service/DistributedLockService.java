package com.paycore.payment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;

    @Value("${paycore.lock.ttl-seconds:30}")
    private long lockTtlSeconds;

    public String acquire(String resourceKey) {
        String lockKey = "lock:" + resourceKey;
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, token, Duration.ofSeconds(lockTtlSeconds));
        if (Boolean.TRUE.equals(acquired)) {
            return token;
        }
        Counter.builder("redis_lock_acquisition_failures")
                .tag("service", "payment-service")
                .register(meterRegistry)
                .increment();
        return null;
    }

    public void release(String resourceKey, String token) {
        String lockKey = "lock:" + resourceKey;
        String current = redisTemplate.opsForValue().get(lockKey);
        if (token != null && token.equals(current)) {
            redisTemplate.delete(lockKey);
        }
    }
}
