package com.nikkssy.library_management.init;

import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.Role;
import com.nikkssy.library_management.mapper.UserMapper;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.full-name}")
    private String fullName;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        Users admin = new Users();

        admin.setFullName(fullName);
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);

        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(true);

        // 1. Save Admin user
        Users savedAdmin = userRepository.save(admin);

        // 2. Create LOCAL authentication mapping
        UserAuthProvider authProvider =
                userMapper.toLocalAuthProvider(savedAdmin);

        savedAdmin.addAuthProvider(authProvider);
        userAuthProviderRepository.save(authProvider);
    }
}
