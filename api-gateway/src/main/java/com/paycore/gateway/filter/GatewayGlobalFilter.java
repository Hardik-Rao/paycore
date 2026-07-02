package com.paycore.gateway.filter;

import com.paycore.common.ApiErrorResponse;
import com.paycore.common.CorrelationHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class GatewayGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;

    @Value("${paycore.rate-limit.payment:100}")
    private int paymentLimit;

    @Value("${paycore.rate-limit.admin:20}")
    private int adminLimit;

    public GatewayGlobalFilter(ReactiveStringRedisTemplate redis, ObjectMapper objectMapper,
                               @Value("${paycore.jwt.secret}") String secret) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationHeaders.CORRELATION_ID);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        final String cid = correlationId;
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing JWT token", cid);
        }
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(auth.substring(7));
        } catch (Exception e) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Invalid JWT", cid);
        }

        int limit = path.startsWith("/admin") ? adminLimit : paymentLimit;
        String rateKey = "rate:" + path + ":" + cid;
        return redis.opsForValue().increment(rateKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return redis.expire(rateKey, Duration.ofMinutes(1)).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > limit) {
                        return error(exchange, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Rate limit exceeded", cid);
                    }
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.header(CorrelationHeaders.CORRELATION_ID, cid))
                            .build();
                    return chain.filter(mutated);
                });
    }

    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String code, String message, String cid) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(
                    ApiErrorResponse.of(status.value(), code, message, cid));
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
