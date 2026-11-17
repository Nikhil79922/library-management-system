package com.nikkssy.library_management.infra;

import java.time.Duration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisConnectionTest implements ApplicationRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {

        redisTemplate.opsForValue().set(
                "library:redis:test",
                "connected",
                Duration.ofMinutes(1)
        );

        String result = redisTemplate.opsForValue()
                .get("library:redis:test");

        log.info("Redis connection test: {}", result);
    }
}