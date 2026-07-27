package com.stockpilot.inventory.config;

import com.stockpilot.inventory.entity.Role;
import com.stockpilot.inventory.entity.User;
import com.stockpilot.inventory.enums.RoleName;
import com.stockpilot.inventory.repository.RoleRepository;
import com.stockpilot.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.platform.admin-email}")
    private String adminEmail;

    @Value("${app.platform.admin-password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Seed roles
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.name().replace("_", " "))
                        .build());
                log.info("Created role: {}", roleName);
            }
        }

        // Seed platform admin
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(RoleName.PLATFORM_ADMIN)
                    .orElseThrow(() -> new RuntimeException("PLATFORM_ADMIN role not found"));

            User admin = User.builder()
                    .firstName("Platform")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole))
                    .active(true)
                    .emailVerified(true)
                    .build();

            userRepository.save(admin);
            log.info("Platform admin created: {}", adminEmail);
        }
    }
}
