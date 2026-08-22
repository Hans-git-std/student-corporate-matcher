package com.matcher.platform.security;

import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.email:admin@studentmatcher.com}")
    private String masterAdminEmail;

    @Value("${app.security.admin.master-password:Admin@RootMaster2026!}")
    private String masterAdminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (masterAdminEmail == null || masterAdminEmail.isBlank()) {
            log.warn("Master admin email not configured. Skipping admin account seeding.");
            return;
        }

        String normalizedAdminEmail = masterAdminEmail.trim().toLowerCase();
        Optional<User> existingAdmin = userRepository.findByEmail(normalizedAdminEmail);
        String encodedPassword = passwordEncoder.encode(masterAdminPassword);

        if (existingAdmin.isEmpty()) {
            User adminUser = User.builder()
                    .email(normalizedAdminEmail)
                    .passwordHash(encodedPassword)
                    .role(RoleType.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(adminUser);
            log.info("==========================================================");
            log.info(" [SECURITY SEED] Master Admin account provisioned: {}", normalizedAdminEmail);
            log.info(" [ZERO-TRUST] 2-Step Login (Password + OTP) active for Admin.");
            log.info("==========================================================");
        } else {
            User admin = existingAdmin.get();
            admin.setRole(RoleType.ROLE_ADMIN);
            admin.setPasswordHash(encodedPassword);
            userRepository.save(admin);
            log.info("Master Admin account verified & synchronized: {}", normalizedAdminEmail);
        }
    }
}
