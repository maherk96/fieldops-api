package com.fieldops.fieldops_api.bootstrap;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default administrator user when the application starts.
 *
 * <p>This runner is only active for {@code local}, {@code test}, and {@code ci} profiles. If an
 * admin user with the configured email already exists, no action is taken.
 *
 * <p>Intended for non-production environments to simplify local development and automated testing.
 */
@Component
@Profile({"local", "test", "ci"})
public class AdminUserSeeder implements ApplicationRunner {

  private static final String ADMIN_EMAIL = "admin@fieldops.local";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates a new {@link AdminUserSeeder}.
   *
   * @param userRepository repository used to look up and persist users
   * @param passwordEncoder encoder used to securely hash the admin password
   */
  public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Executed automatically on application startup.
   *
   * <p>Checks for the existence of the default admin user and creates one if it does not already
   * exist.
   *
   * @param args application startup arguments
   */
  @Override
  public void run(ApplicationArguments args) {
    userRepository
        .findByEmail(ADMIN_EMAIL)
        .ifPresentOrElse(
            user -> {
              // Admin user already exists – no action required
            },
            this::createAdminUser);
  }

  /**
   * Creates and persists the default administrator user.
   *
   * <p>The password is securely encoded and all required audit and versioning fields are
   * initialised.
   */
  private void createAdminUser() {
    var admin = new User();
    admin.setEmail(ADMIN_EMAIL);
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFullName("System Admin");
    admin.setRole("ADMIN");
    admin.setActive(true);
    admin.setVersion(1);
    admin.setChangeVersion(1L);
    userRepository.save(admin);
  }
}
