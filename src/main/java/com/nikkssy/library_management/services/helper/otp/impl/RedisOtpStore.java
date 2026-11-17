
package com.nikkssy.library_management.services.helper.otp.impl;

import com.nikkssy.library_management.entity.enums.OtpPurpose;
import com.nikkssy.library_management.services.helper.otp.OtpData;
import com.nikkssy.library_management.services.helper.otp.OtpStore;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisOtpStore implements OtpStore {

    private static final String KEY_PREFIX = "auth:otp:";

    private static final String HASHED_OTP = "hashedOtp";
    private static final String ATTEMPTS = "attempts";
    private static final String EXPIRES_AT = "expiresAt";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(
            OtpPurpose purpose,
            String identifier,
            OtpData otpData,
            Duration ttl
    ) {

        String key = buildKey(purpose, identifier);

        Map<String, String> values = Map.of(
                HASHED_OTP, otpData.hashedOtp(),
                ATTEMPTS, String.valueOf(otpData.attempts()),
                EXPIRES_AT, otpData.expiresAt().toString()
        );

        redisTemplate.opsForHash().putAll(key, values);

        redisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<OtpData> find(
            OtpPurpose purpose,
            String identifier
    ) {

        String key = buildKey(purpose, identifier);

        Map<Object, Object> values =
                redisTemplate.opsForHash().entries(key);

        if (values.isEmpty()) {
            return Optional.empty();
        }

        String hashedOtp = (String) values.get(HASHED_OTP);
        int attempts = Integer.parseInt(
                values.get(ATTEMPTS).toString()
        );

        Instant expiresAt = Instant.parse(
                values.get(EXPIRES_AT).toString()
        );

        return Optional.of(
                new OtpData(hashedOtp, attempts, expiresAt)
        );
    }

    @Override
    public void delete(
            OtpPurpose purpose,
            String identifier
    ) {

        redisTemplate.delete(buildKey(purpose, identifier));
    }

    @Override
    public long incrementAttempts(
            OtpPurpose purpose,
            String identifier
    ) {

        String key = buildKey(purpose, identifier);

        Long attempts = redisTemplate.opsForHash()
                .increment(key, ATTEMPTS, 1);

        return attempts == null ? 0 : attempts;
    }

    private String buildKey(
            OtpPurpose purpose,
            String identifier
    ) {

        return KEY_PREFIX
                + purpose.name()
                + ":"
                + identifier;
    }
}
