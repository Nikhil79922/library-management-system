import pypandoc, os, textwrap

md = r"""# Email & Phone Verification — Today's Implementation Notes

## 1. Objective

Implement email and phone verification for local registration and phone verification for Google-authenticated users.

The common rule is:

- A user must have a verified email.
- A user must have a verified phone number before receiving normal application access and refresh tokens.
- Password reset can establish email ownership, but it must never verify the phone number.

---

## 2. User verification fields

The `Users` entity uses separate verification flags:

```java
private Boolean emailVerified;
private Boolean phoneVerified;
```

For a new local registration:

```text
emailVerified = false
phoneVerified = false
```

For a newly created Google user:

```text
emailVerified = true
phoneVerified = false
```

Google's verified email claim (`email_verified`) is checked in `CustomOidcUserService`. Google does not necessarily provide a phone number, so the phone-related fields may initially be `null`.

---

## 3. Local registration flow

### Step 1 — Signup

Endpoint:

```http
POST /api/auth/signup
```

The backend:

1. Creates the user.
2. Encodes the password.
3. Creates the `LOCAL` provider mapping.
4. Generates and stores an email OTP in Redis.
5. Sends the email verification OTP.
6. Returns `RegistrationResponse`.

No access token or refresh token is issued at signup.

Example request (adjust fields to match `RegisterRequest`):

```bash
curl --location 'http://localhost:8081/api/auth/signup' \
--header 'Content-Type: application/json' \
--data-raw '{
    "fullName": "Nikhil Singh",
    "username": "nikkssy",
    "email": "nikkssy.dev@gmail.com",
    "password": "YourPassword@123",
    "country": "India",
    "countryCode": "+91",
    "countrySortCode": "IN",
    "phone": "9876543210"
}'
```

### Step 2 — Verify email OTP

Endpoint:

```http
POST /api/auth/otp/email/verify
```

Example:

```bash
curl --location 'http://localhost:8081/api/auth/otp/email/verify' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com",
    "otp": "123456"
}'
```

On successful verification:

```text
emailVerified = true
phoneVerified = false
```

No normal access or refresh token is issued yet.

### Step 3 — Send phone OTP

Endpoint:

```http
POST /api/auth/otp/phone/send
```

Example:

```bash
curl --location 'http://localhost:8081/api/auth/otp/phone/send' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com"
}'
```

The backend checks that the email has already been verified, then generates and stores the phone OTP. In the current development setup, the phone OTP may be logged to the application console; console logging must be disabled in production and replaced with an SMS provider.

### Step 4 — Verify phone OTP

Endpoint:

```http
POST /api/auth/otp/phone/verify
```

Example:

```bash
curl --location 'http://localhost:8081/api/auth/otp/phone/verify' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com",
    "otp": "123456"
}' \
--cookie-jar cookies.txt
```

On successful verification:

1. The OTP is verified.
2. `phoneVerified` is set to `true`.
3. An access JWT is generated.
4. A refresh token is created and persisted.
5. The controller sets the refresh token as an HttpOnly cookie.
6. The response contains the `AuthResponse`.

The local registration flow is now complete.

---

## 4. Google registration flow

### Step 1 — Start Google OAuth2 login

Open this URL in a browser:

```text
http://localhost:8081/oauth2/authorization/google
```

Complete Google authentication.

The `CustomOidcUserService`:

1. Loads the Google OIDC user.
2. Reads Google's `sub`, email and full name.
3. Requires a verified Google email.
4. Finds an existing Google provider mapping by `sub`.
5. Otherwise resolves an existing application user by verified email or creates a Google user.
6. Creates the Google provider mapping.
7. Rejects disabled accounts.

For a new Google user, the phone fields may be `null` and `phoneVerified` is `false`.

### Step 2 — Google success handler

`GoogleAuthSuccessHandler` checks whether the application user is enabled.

If the phone is not verified:

```java
if (!user.isPhoneVerified()) {
    String redirectUrl = UriComponentsBuilder
            .fromUriString(frontendBaseUrl)
            .path("/oauth2/complete-registration")
            .build()
            .toUriString();

    response.sendRedirect(redirectUrl);
    return;
}
```

The `return` is important: it prevents access-token and refresh-token generation for an unverified phone.

If the phone is already verified, the handler follows the normal Google login flow and issues tokens.

### Step 3 — Register phone details

The request DTO is:

```java
public record RegisterPhoneRequest(

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Country calling code is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{0,3}$",
                message = "Invalid country calling code"
        )
        String countryCode,

        @NotBlank(message = "Country sort code is required")
        @Size(min = 2, max = 3)
        String countrySortCode,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{6,15}$",
                message = "Invalid phone number"
        )
        String phone
) {}
```

Example phone details:

```json
{
    "country": "India",
    "countryCode": "+91",
    "countrySortCode": "IN",
    "phone": "9876543210"
}
```

The service interface method:

```java
RegistrationResponse registerPhone(
        String googleSub,
        RegisterPhoneRequest request
);
```

The service should:

1. Resolve the Google user using the authenticated Google `sub` and the `GOOGLE` provider mapping.
2. Check that the account is enabled.
3. Check that the phone is not already verified.
4. Check phone-number uniqueness.
5. Save country, country code, country sort code and phone number.
6. Generate and store the phone OTP.
7. Return `RegistrationResponse` with `phoneVerified = false`.

No normal access or refresh token is issued by this operation.

The controller uses the authenticated OIDC principal:

```java
@PostMapping("/phone/register")
public ResponseEntity<SuccessResponse<RegistrationResponse>> registerPhone(
        @Valid @RequestBody RegisterPhoneRequest request,
        @AuthenticationPrincipal OidcUser oidcUser
) {
    if (oidcUser == null) {
        throw new OAuth2AuthenticationException(
                "Google authentication required"
        );
    }

    RegistrationResponse result = authService.registerPhone(
            oidcUser.getSubject(),
            request
    );

    // Return the standard SuccessResponse.
}
```

The endpoint must be protected by the authenticated Google session. Do not accept a frontend-supplied email or Google `sub` as proof of identity.

### Step 4 — Verify Google phone OTP

The intended Google verification operation must:

1. Require the authenticated Google session.
2. Resolve the application user from the authenticated OIDC `sub`.
3. Verify the phone OTP for that user's saved phone number.
4. Set `phoneVerified = true`.
5. Generate the access JWT.
6. Create and persist the refresh token.
7. Return `AuthResponse` and set the refresh token as an HttpOnly cookie.
8. Invalidate the pending Google registration session after successful completion.

**Implementation status note:** The conversation established this intended Google phone-verification flow, but the shared `AuthController` still showed only the local `/otp/phone/verify` endpoint. A separate Google session-based verification endpoint still needs to be added and tested.

---

## 5. AuthService interface methods

The relevant methods are:

```java
RegistrationResponse register(RegisterRequest request);

RegistrationResponse verifyEmailOtp(String email, String otp);

RegistrationResponse sendPhoneOtp(String email);

AuthResult verifyPhoneOtp(String email, String otp);

RegistrationResponse registerPhone(
        String googleSub,
        RegisterPhoneRequest request
);
```

The Google phone-verification method should be added separately, using the authenticated Google identity rather than trusting an email from the request.

---

## 6. Local login restriction

The local login service must reject users whose registration is incomplete before issuing tokens:

```java
if (!Boolean.TRUE.equals(user.getEmailVerified()) ||
        !Boolean.TRUE.equals(user.getPhoneVerified())) {

    throw new AccessDeniedException(
            "Please complete your email and phone verification before logging in."
    );
}
```

Only after this check should the service generate the access token and refresh token.

The refresh-token service must also enforce registration eligibility so an old refresh token cannot bypass verification requirements. Apply the local verification requirement to `LOCAL` users without unintentionally blocking Google users whose phone-verification policy is different.

---

## 7. Password reset and email verification

The agreed behaviour is:

- A valid password-reset token proves control of the registered email address.
- After successful reset-token validation, set `emailVerified = true`.
- Do not change `phoneVerified`.
- A Google-only user may receive a `LOCAL` provider mapping after successfully setting a password, if that is the intended account-linking policy.
- Revoke all active refresh tokens after a password change.
- Send a password-changed notification.

The relevant update in `resetPassword()` is:

```java
user.setPassword(encodedPassword);
user.setPasswordChangedAt(Instant.now());
user.setEmailVerified(true);
```

The reset token must be expiring and single-use. Token consumption should be part of the successful reset operation.

---

## 8. Spring Security filter-chain separation

Two filter chains were planned.

### Order 1 — OAuth2/session chain

This chain handles:

```text
/oauth2/**
/login/oauth2/**
/api/auth/phone/register
/api/auth/phone/verify
```

It uses:

```java
SessionCreationPolicy.IF_REQUIRED
```

The Google phone endpoints require an authenticated OIDC session.

### Order 2 — Stateless JWT API chain

This chain handles the local registration and normal API endpoints.

It uses:

```java
SessionCreationPolicy.STATELESS
```

Public endpoints include:

```text
/api/auth/signup
/api/auth/login
/api/auth/refresh-token
/api/auth/logout
/api/auth/forgot-password
/api/auth/reset-password
/api/auth/otp/email/verify
/api/auth/otp/phone/send
/api/auth/otp/phone/verify
```

Other `/api/**` routes require authentication, except admin routes which require the ADMIN role.

**Important route separation:** The local phone OTP verification endpoint is `/api/auth/otp/phone/verify`. The Google session-based phone verification endpoint is intended to be `/api/auth/phone/verify`.

Keep CSRF protection enabled for the session-based OAuth2 chain. The frontend must send the CSRF token for POST requests. The stateless JWT API chain can retain its existing CSRF policy, subject to the application's cookie-based authentication design.

---

## 9. CORS and browser session

Do not combine:

```java
allowCredentials = true
allowedOrigins = "*"
```

Use the exact frontend origin when allowing credentials.

For browser calls to the Google session-based phone endpoints, send cookies:

```javascript
axios.post(
    `${API_URL}/api/auth/phone/register`,
    phoneDetails,
    {
        withCredentials: true
    }
);
```

If the frontend and backend are cross-site, configure the session cookie appropriately (`SameSite=None; Secure` over HTTPS) and ensure CSRF is handled.

---

## 10. Postman / cURL testing

Set the base URL:

```bash
BASE_URL="http://localhost:8081/api/auth"
```

### Local flow

```bash
# 1. Signup
curl --location "$BASE_URL/signup" \
--header 'Content-Type: application/json' \
--data-raw '{
    "fullName": "Nikhil Singh",
    "username": "nikkssy",
    "email": "nikkssy.dev@gmail.com",
    "password": "YourPassword@123",
    "country": "India",
    "countryCode": "+91",
    "countrySortCode": "IN",
    "phone": "9876543210"
}'

# 2. Verify email OTP
curl --location "$BASE_URL/otp/email/verify" \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com",
    "otp": "123456"
}'

# 3. Send phone OTP
curl --location "$BASE_URL/otp/phone/send" \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com"
}'

# 4. Verify phone OTP
curl --location "$BASE_URL/otp/phone/verify" \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "nikkssy.dev@gmail.com",
    "otp": "123456"
}' \
--cookie-jar cookies.txt
```

### Google flow

Start Google OAuth in a browser:

```text
http://localhost:8081/oauth2/authorization/google
```

After authentication, use the browser-established backend session cookie and CSRF token when calling:

```bash
curl --location 'http://localhost:8081/api/auth/phone/register' \
--header 'Content-Type: application/json' \
--header 'X-XSRF-TOKEN: YOUR_CSRF_TOKEN' \
--cookie 'JSESSIONID=YOUR_GOOGLE_SESSION_ID' \
--cookie 'XSRF-TOKEN=YOUR_CSRF_TOKEN' \
--data-raw '{
    "country": "India",
    "countryCode": "+91",
    "countrySortCode": "IN",
    "phone": "9876543210"
}'
```

The Google phone-verification cURL should be added after its dedicated controller endpoint and request DTO are implemented.

---

## 11. Final verification rules

| Flow / operation | Email verified | Phone verified | Normal tokens |
|---|---:|---:|---|
| Local signup | false | false | No |
| Local email OTP success | true | false | No |
| Local phone OTP success | true | true | Yes |
| New Google registration | true | false | No |
| Google phone details saved | true | false | No |
| Google phone OTP success | true | true | Yes |
| Password reset success | true | Unchanged | No automatic issuance |

---

## 12. Remaining implementation and testing checklist

- [ ] Confirm the exact fields and validation rules in `RegisterRequest`.
- [ ] Confirm the phone uniqueness query matches the `Users` entity fields.
- [ ] Ensure phone numbers are normalized consistently (prefer E.164 for identity/uniqueness).
- [ ] Add the dedicated Google phone-verification request DTO and controller endpoint.
- [ ] Ensure Google phone verification resolves the user through the authenticated OIDC `sub`.
- [ ] Issue access and refresh tokens only after successful Google phone OTP verification.
- [ ] Invalidate the pending Google session after successful completion.
- [ ] Enforce verification eligibility in refresh-token rotation.
- [ ] Ensure password-reset tokens are single-use and expire.
- [ ] Disable development OTP console logging in production.
- [ ] Add rate limiting for OTP generation and verification.
- [ ] Test both registration paths end to end.

## Important security note

An email address or Google `sub` supplied in a request body is not proof of identity. Google phone registration and verification must derive the identity from the authenticated OIDC session. Local OTP endpoints must also ensure the requester is authorized to act on the account associated with the supplied email.
"""

out = "/mnt/data/email_phone_verification_changes.md"
pypandoc.convert_text(md, "md", format="md", outputfile=out, extra_args=["--standalone"])
print(out, os.path.getsize(out))
