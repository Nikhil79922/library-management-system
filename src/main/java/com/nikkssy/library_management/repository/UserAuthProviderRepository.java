package com.nikkssy.library_management.repository;

import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserAuthProviderRepository
         extends JpaRepository<UserAuthProvider, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<UserAuthProvider> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

}
