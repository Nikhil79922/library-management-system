package com.nikkssy.library_management.services.helper.otp;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final int OTP_LENGTH = 6;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {

        int otp = secureRandom.nextInt(1_000_000);

        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}