
package com.nikkssy.library_management.services.helper.otp;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.OtpPurpose;
import com.nikkssy.library_management.services.impl.email.EmailServiceImpl;
import com.nikkssy.library_management.exception.IllegalArgumentException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 3;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;
    private final EmailServiceImpl emailService;

    @Value("${app.security.otp-hmac-secret}")
    private String otpHmacSecret;

    public void generateAndSendRegistrationEmailOtp(Users user) {

        String otp = generateOtp(
                OtpPurpose.REGISTRATION_EMAIL,
                user.getEmail()
        );

        emailService.sendEmailVerificationOtp(
                user.getEmail(),
                user.getFullName(),
                otp
        );
    }

    public void generateAndLogRegistrationPhoneOtp(Users user) {

        String identifier = getPhoneIdentifier(user);

        String otp = generateOtp(
                OtpPurpose.REGISTRATION_PHONE,
                identifier
        );

            log.info(
                    "Development phone verification OTP for user {}: {}",
                    user.getId(),
                    otp
            );

    }

    private String getPhoneIdentifier(Users user) {

        return user.getCountryCode() + ":" + user.getPhone();
    }

    private String generateOtp(
            OtpPurpose purpose,
            String identifier
    ) {

        String otp = otpGenerator.generate();

        String hashedOtp = hashOtp(
                purpose,
                identifier,
                otp
        );

        OtpData otpData = new OtpData(
                hashedOtp,
                0,
                Instant.now().plus(OTP_TTL)
        );

        otpStore.save(
                purpose,
                identifier,
                otpData,
                OTP_TTL
        );

        return otp;
    }

    public void verifyOtp(
            OtpPurpose purpose,
            String identifier,
            String submittedOtp
    ) {

        OtpData otpData = otpStore.find(purpose, identifier)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "OTP expired or not found"
                        )
                );

        if (otpData.attempts() >= MAX_ATTEMPTS) {
            otpStore.delete(purpose, identifier);

            throw new IllegalArgumentException(
                    "Maximum OTP attempts exceeded"
            );
        }

        String submittedHash = hashOtp(
                purpose,
                identifier,
                submittedOtp
        );

        boolean matches = MessageDigest.isEqual(
                HexFormat.of().parseHex(submittedHash),
                HexFormat.of().parseHex(otpData.hashedOtp())
        );

        if (!matches) {

            long attempts = otpStore.incrementAttempts(
                    purpose,
                    identifier
            );

            if (attempts >= MAX_ATTEMPTS) {
                otpStore.delete(purpose, identifier);
            }

            throw new IllegalArgumentException("Invalid OTP");
        }

        otpStore.delete(purpose, identifier);
    }

    private String hashOtp(
            OtpPurpose purpose,
            String identifier,
            String otp
    ) {

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec secretKey = new SecretKeySpec(
                    otpHmacSecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            mac.init(secretKey);

            String payload = purpose.name()
                    + ":"
                    + identifier
                    + ":"
                    + otp;

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash OTP",
                    e
            );
        }
    }

    public void generateAndSendLoginEmailOtp(Users user) {

        String otp = generateOtp(
                OtpPurpose.LOGIN_EMAIL,
                user.getEmail()
        );

        emailService.sendLoginOtp(
                user.getEmail(),
                user.getFullName(),
                otp
        );
    }


    public void generateAndLogLoginPhoneOtp(Users user) {

        String identifier = getPhoneIdentifier(user);

        String otp = generateOtp(
                OtpPurpose.LOGIN_PHONE,
                identifier
        );

        log.info(
                "Development phone login OTP for user {}: {}",
                user.getId(),
                otp
        );
    }
}
