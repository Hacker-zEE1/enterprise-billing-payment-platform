package com.shaqib.billing.security.config;

import com.shaqib.billing.security.role.Role;
import com.shaqib.billing.security.user.AppUser;
import com.shaqib.billing.security.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        if (appUserRepository.existsByEmail(adminEmail)) {
            return;
        }

        AppUser admin = new AppUser(
                UUID.randomUUID(),
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN,
                true,
                LocalDateTime.now(),
                null
        );

        appUserRepository.save(admin);
    }
}