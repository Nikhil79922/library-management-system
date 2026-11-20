package com.nikkssy.library_management.repository;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.entity.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username,
            String email
    );

    boolean existsByRole(Role role);

    Optional<Users> findByCountryCodeAndPhone(String countryCode, String phone);

    boolean existsByCountryCodeAndPhone(String countryCode, String phone);
    
    boolean existsByEmailIgnoreCase(String email);

    Optional<Users> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    Optional<Users> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);
}


