package com.nikkssy.library_management.entity;

import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_users_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Column(nullable = false, length = 20)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    @Column(nullable = false, length = 254)
    private String email;

    @Size(max = 255, message = "Password must not exceed 255 characters")
    @Column(length = 255)
    private String password;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Size(max = 10, message = "Country code must not exceed 10 characters")
    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(length = 100)
    private String country;

    @Size(max = 10, message = "Country sort code must not exceed 10 characters")
    @Column(name = "country_sort_code", length = 10)
    private String countrySortCode;

    @NotNull(message = "Authentication provider is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @NotNull(message = "Account enabled status is required")
    @Column(nullable = false)
    private Boolean enabled = true;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
