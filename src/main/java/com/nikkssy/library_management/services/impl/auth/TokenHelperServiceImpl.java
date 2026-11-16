package com.nikkssy.library_management.services.impl.auth;

public interface TokenHelperServiceImpl {

    String generateSecureToken();

    String hashToken(String rawToken);
}
