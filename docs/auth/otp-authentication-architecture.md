# OTP Authentication Architecture

## 1. Purpose

The application supports passwordless authentication using one-time
passwords (OTPs) sent to a user's registered email address or phone
number.

OTP login is implemented alongside the existing password-based login.
Both OTP channels reuse the application's existing OTP infrastructure
and authentication-token issuance flow.

## 2. High-level architecture

``` text
                         Client
                           |
              +------------+------------+
              |                         |
        Email OTP flow             Phone OTP flow
              |                         |
        AuthController              AuthController
              |                         |
              +------------+------------+
                           |
                      AuthService
                           |
             +-------------+-------------+
             |                           |
        UserRepository                OtpService
             |                           |
       Account lookup             OTP generation
       Eligibility check          OTP verification
                                       |
                                   OtpGenerator
                                       |
                                    OtpStore
                                       |
                                  RedisOtpStore
                                       |
                                     Redis

After successful OTP verification:

AuthService
    |
    +--> JwtService
    |
    +--> RefreshTokenService
    |
    +--> AuthResultMapper
    |
AuthController
    |
    +--> AuthCookieManagement
    |
Client receives access token and HttpOnly refresh-token cookie
```

## 3. Main components and responsibilities

### 3.1 AuthController

**Package:** `com.nikkssy.library_management.controllers`

Responsibilities:

-   Exposes the email and phone OTP login endpoints.
-   Accepts and validates request DTOs.
-   Delegates business operations to `AuthServiceImpl`.
-   Returns the standard `SuccessResponse`.
-   On successful verification, creates the refresh-token cookie using
    `AuthCookieManagement`.
-   Returns the authentication response containing the access token.

### 3.2 AuthServiceImpl

**Package:** `com.nikkssy.library_management.services.impl.auth`

Responsibilities:

-   Finds users by email or phone number.
-   Validates whether the account is eligible for OTP login.
-   Requests OTP generation and delivery.
-   Verifies submitted OTPs.
-   Coordinates access-token and refresh-token issuance after successful
    verification.

The service exposes these operations:

``` java
void sendEmailLoginOtp(EmailOtpLoginRequest request);

void sendPhoneLoginOtp(PhoneOtpLoginRequest request);

AuthResult verifyEmailLoginOtp(VerifyEmailOtpLoginRequest request);

AuthResult verifyPhoneLoginOtp(VerifyPhoneOtpLoginRequest request);
```

### 3.3 OtpService

**Package:** `com.nikkssy.library_management.services.helper.otp`

Responsibilities:

-   Generates OTPs through `OtpGenerator`.
-   Hashes OTPs using HMAC-SHA256.
-   Stores OTP data through `OtpStore`.
-   Sends email OTPs through `EmailServiceImpl`.
-   Logs phone OTPs during development.
-   Verifies submitted OTPs.
-   Tracks failed attempts.
-   Deletes OTP records after successful verification.

The existing OTP service is shared by registration and login. A separate
OTP subsystem is not created.

### 3.4 OtpGenerator

Generates the six-digit OTP used by the OTP service.

### 3.5 OtpStore and RedisOtpStore

`OtpStore` defines the OTP storage operations. `RedisOtpStore`
implements those operations using Redis.

The OTP data contains:

``` java
public record OtpData(
        String hashedOtp,
        int attempts,
        Instant expiresAt
) {}
```

OTP records are stored with a purpose- and identifier-specific key.

### 3.6 EmailServiceImpl and SmtpEmailService

The email service abstraction is used by `OtpService` to send login OTP
emails.

`SmtpEmailService` renders the `login-otp` Thymeleaf template and sends
the email asynchronously using the existing email executor.

Phone delivery currently uses development console logging. A real SMS
provider is not yet integrated.

### 3.7 Token and cookie components

After successful OTP verification:

-   `JwtService` generates the access token.
-   `RefreshTokenServiceImpl` creates and persists the refresh token.
-   `AuthResultMapper` builds the `AuthResult`.
-   `AuthCookieManagement` creates the HttpOnly refresh-token cookie.
-   `AuthController` sends the cookie in the `Set-Cookie` response
    header.

Both email and phone OTP login use the same token-issuance helper.

## 4. OTP purpose isolation

The `OtpPurpose` enum separates registration OTPs from login OTPs:

``` java
public enum OtpPurpose {
    REGISTRATION_EMAIL,
    REGISTRATION_PHONE,
    LOGIN_EMAIL,
    LOGIN_PHONE
}
```

This separation ensures an OTP created for one purpose cannot be used
for another purpose.

## 5. OTP storage and verification model

The existing OTP service uses the following configuration:

  Property                        Current behavior
  ------------------------------- -------------------------------
  OTP length                      6 digits
  OTP lifetime                    5 minutes
  Maximum verification attempts   3
  Hash algorithm                  HMAC-SHA256
  Storage                         Redis
  Comparison                      Constant-time hash comparison
  Successful verification         OTP record deleted

The HMAC input is composed of:

``` text
purpose:identifier:otp
```

The HMAC secret is configured through:

``` properties
app.security.otp-hmac-secret=...
```

The raw OTP is not stored in Redis; the hashed OTP is stored instead.

## 6. Email OTP architecture

``` text
Client
  |
  | email
  v
AuthController
  |
  v
AuthServiceImpl
  |
  +--> UserRepository: find user by email
  |
  +--> Validate account eligibility
  |
  v
OtpService
  |
  +--> OtpGenerator
  |
  +--> HMAC-SHA256 hash
  |
  +--> OtpStore / RedisOtpStore
  |
  +--> EmailServiceImpl
          |
          v
      SmtpEmailService
          |
          +--> EmailTemplateService
          |
          +--> login-otp.html
          |
          v
       SMTP provider
```

For verification, the submitted email and OTP are passed to the service.
The service retrieves the user, validates account eligibility, verifies
the OTP using `LOGIN_EMAIL`, and issues tokens after successful
verification.

## 7. Phone OTP architecture

``` text
Client
  |
  | countryCode + phoneNumber
  v
AuthController
  |
  v
AuthServiceImpl
  |
  +--> UserRepository: find user by country code and phone
  |
  +--> Validate account eligibility
  |
  v
OtpService
  |
  +--> OtpGenerator
  |
  +--> HMAC-SHA256 hash
  |
  +--> OtpStore / RedisOtpStore
  |
  +--> Development log
```

The phone identifier used by the OTP service is:

``` text
countryCode:phone
```

For verification, the service retrieves the user, validates account
eligibility, verifies the OTP using `LOGIN_PHONE`, and issues tokens
after successful verification.

Phone OTP delivery is currently a development implementation.
SMS-provider integration is a future task.

## 8. Account eligibility

Before sending or verifying a login OTP, the service checks that the
account:

-   Is enabled.
-   Has a verified email address.
-   Has a verified phone number.

The eligibility check is centralized in
`validateOtpLoginEligibility(Users user)`.

Registration verification flags are not changed during login. OTP login
authenticates an already-verified account; it does not complete
registration.

## 9. Security configuration

The four OTP login endpoints must be accessible without an existing
authenticated session or JWT because they establish authentication.

The stateless API security chain permits the four OTP login paths.
Account eligibility and OTP verification are enforced by the
authentication service before tokens are issued.

## 10. Important production-hardening considerations

The current architecture has the following follow-up requirements before
production:

1.  **Atomic OTP verification and consumption:** Current OTP
    verification uses separate Redis read, compare, and delete
    operations. Use an atomic Redis operation or Lua script to prevent
    concurrent requests from consuming the same OTP.
2.  **OTP request rate limiting:** Add resend cooldowns and
    per-account/IP limits.
3.  **SMS delivery:** Replace development phone OTP logging with an SMS
    provider.
4.  **Email delivery observability:** The asynchronous email sender
    currently logs delivery failures. Add suitable delivery tracking and
    retry/resend handling.
5.  **Security testing:** Test expired, invalid, reused, and
    maximum-attempt OTPs, as well as disabled and unverified accounts.

## 11. Architecture summary

The OTP authentication architecture reuses the existing Redis-backed OTP
infrastructure for both registration and login, with separate OTP
purposes for each operation.

Email and phone login converge on the same authentication service and
token-issuance path. After successful OTP verification, the application
issues an access token and persists a refresh token, which is returned
to the client through the existing HttpOnly cookie mechanism.
