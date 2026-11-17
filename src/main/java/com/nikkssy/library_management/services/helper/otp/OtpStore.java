package com.nikkssy.library_management.services.helper.otp;

import com.nikkssy.library_management.entity.enums.OtpPurpose;

import java.time.Duration;
import java.util.Optional;

public interface OtpStore {

    void save(
            OtpPurpose purpose,
            String identifier,
            OtpData otpData,
            Duration ttl
    );

    Optional<OtpData> find(
            OtpPurpose purpose,
            String identifier
    );

    void delete(
            OtpPurpose purpose,
            String identifier
    );

    long incrementAttempts(
            OtpPurpose purpose,
            String identifier
    );
}