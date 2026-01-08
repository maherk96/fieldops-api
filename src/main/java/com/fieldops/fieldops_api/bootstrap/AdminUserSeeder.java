package com.fieldops.fieldops_api.bootstrap;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test", "ci"})
public class AdminUserSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserSeeder(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    userRepository
        .findByEmail("admin@fieldops.local")
        .ifPresentOrElse(
            user -> {}, // already exists
            this::createAdminUser
        );
  }

  private void createAdminUser() {
    User admin = new User();
    admin.setEmail("admin@fieldops.local");
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFullName("System Admin");
    admin.setRole("ADMIN");
    admin.setActive(true);
    admin.setVersion(1);
    admin.setChangeVersion(1L);
    admin.setCreatedAt(OffsetDateTime.now());
    admin.setUpdatedAt(OffsetDateTime.now());

    userRepository.save(admin);
  }
}