package com.fieldops.fieldops_api.config;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes test data for local development profile.
 *
 * Creates multiple organizations with users.
 *
 * Runs ONLY under the "local" profile.
 */
@Component
@Profile("local")
@Order(1)
public class LocalDataInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(LocalDataInitializer.class);
  private static final String SEPARATOR = "=".repeat(80);
  private static final String SUB_SEPARATOR = "-".repeat(80);

  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private int organizationsCreated = 0;
  private int organizationsSkipped = 0;
  private int usersCreated = 0;
  private int usersSkipped = 0;

  /** LOCAL ONLY: store plaintext credentials for visibility */
  private record TestCredential(
          String subdomain,
          String email,
          String password,
          String role,
          boolean active
  ) {}

  private final Set<TestCredential> testCredentials = new HashSet<>();

  public LocalDataInitializer(
          final OrganizationRepository organizationRepository,
          final UserRepository userRepository,
          final PasswordEncoder passwordEncoder) {
    this.organizationRepository = organizationRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(final String... args) {
    log.info(SEPARATOR);
    log.info("LOCAL DEVELOPMENT DATA INITIALIZER STARTED");
    log.info(SEPARATOR);

    final long startTime = System.currentTimeMillis();

    initializeAcmeCorporation();
    initializeTechStartInc();
    initializeGlobalServices();
    initializeQuickFixLLC();

    final long duration = System.currentTimeMillis() - startTime;

    log.info(SUB_SEPARATOR);
    printSummary(duration);
    log.info(SEPARATOR);
    log.info("LOCAL DEVELOPMENT DATA INITIALIZATION COMPLETE");
    log.info(SEPARATOR);
  }

  private void initializeAcmeCorporation() {
    log.info("Initializing organization: ACME CORPORATION");

    final Organization acme = createOrGetOrganization(
            "Acme Corporation",
            "acme",
            "ACTIVE",
            "PRO",
            100,
            1000,
            null
    );

    createUserIfNotExists(acme, "admin@acme.com", "admin123", "John Admin", "ADMIN", true);
    createUserIfNotExists(acme, "sarah.admin@acme.com", "admin123", "Sarah Admin", "ADMIN", true);
    createUserIfNotExists(acme, "engineer@acme.com", "engineer123", "Bob Engineer", "ENGINEER", true);
    createUserIfNotExists(acme, "jane.engineer@acme.com", "engineer123", "Jane Engineer", "ENGINEER", true);
    createUserIfNotExists(acme, "dispatcher@acme.com", "dispatcher123", "Alice Dispatcher", "DISPATCHER", true);
  }

  private void initializeTechStartInc() {
    log.info("Initializing organization: TECHSTART INC");

    final Organization techstart = createOrGetOrganization(
            "TechStart Inc",
            "techstart",
            "TRIAL",
            "BASIC",
            20,
            200,
            OffsetDateTime.now().plusDays(14)
    );

    createUserIfNotExists(techstart, "admin@techstart.com", "admin123", "Emma Founder", "ADMIN", true);
    createUserIfNotExists(techstart, "lead@techstart.com", "engineer123", "David Lead", "ENGINEER", true);
    createUserIfNotExists(techstart, "dispatch@techstart.com", "dispatcher123", "Mark Dispatch", "DISPATCHER", true);
  }

  private void initializeGlobalServices() {
    log.info("Initializing organization: GLOBAL SERVICES");

    final Organization global = createOrGetOrganization(
            "Global Services Ltd",
            "globalservices",
            "ACTIVE",
            "ENTERPRISE",
            500,
            5000,
            null
    );

    createUserIfNotExists(global, "admin@globalservices.com", "admin123", "Robert Admin", "ADMIN", true);
    createUserIfNotExists(global, "engineer1@globalservices.com", "engineer123", "Carlos Engineer", "ENGINEER", true);
    createUserIfNotExists(global, "inactive@globalservices.com", "engineer123", "Inactive User", "ENGINEER", false);
  }

  private void initializeQuickFixLLC() {
    log.info("Initializing organization: QUICKFIX LLC");

    final Organization quickfix = createOrGetOrganization(
            "QuickFix LLC",
            "quickfix",
            "ACTIVE",
            "BASIC",
            10,
            100,
            null
    );

    createUserIfNotExists(quickfix, "owner@quickfix.com", "admin123", "Sam Owner", "ADMIN", true);
    createUserIfNotExists(quickfix, "tech@quickfix.com", "engineer123", "Pat Technician", "ENGINEER", true);
  }

  private Organization createOrGetOrganization(
          final String name,
          final String subdomain,
          final String subscriptionStatus,
          final String subscriptionPlan,
          final int maxEngineers,
          final int maxWorkOrdersPerMonth,
          final OffsetDateTime trialEndsAt) {

    final Optional<Organization> existing = organizationRepository.findBySubdomain(subdomain);

    if (existing.isPresent()) {
      log.debug("Organization '{}' already exists (ID={})", name, existing.get().getId());
      organizationsSkipped++;
      return existing.get();
    }

    log.info("Creating organization: {} ({})", name, subdomain);

    final Organization org = new Organization();
    org.setName(name);
    org.setSubdomain(subdomain);
    org.setSettings("{}");
    org.setSubscriptionStatus(subscriptionStatus);
    org.setSubscriptionPlan(subscriptionPlan);
    org.setMaxEngineers(maxEngineers);
    org.setMaxWorkOrdersPerMonth(maxWorkOrdersPerMonth);
    org.setTrialEndsAt(trialEndsAt);

    final Organization saved = organizationRepository.save(org);
    organizationsCreated++;
    return saved;
  }

  private void createUserIfNotExists(
          final Organization organization,
          final String email,
          final String plainPassword,
          final String fullName,
          final String role,
          final boolean active) {

    // Always add to credentials list for summary printing
    testCredentials.add(
            new TestCredential(
                    organization.getSubdomain(),
                    email,
                    plainPassword,
                    role,
                    active
            )
    );

    final Optional<User> existingUser =
            userRepository.findByOrganizationIdAndEmail(organization.getId(), email);

    if (existingUser.isPresent()) {
      log.debug("User {} already exists in org {}, skipping",
              email, organization.getSubdomain());
      usersSkipped++;
      return;
    }

    log.info("Creating user {} [{}] in org {}", email, role, organization.getSubdomain());

    final User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(plainPassword));
    user.setFullName(fullName);
    user.setRole(role);
    user.setActive(active);
    user.setOrganization(organization);
    user.setChangeVersion(0L);
    user.setVersion(1);
    user.setUpdatedAt(OffsetDateTime.now());

    userRepository.save(user);
    usersCreated++;
  }

  private void printSummary(final long durationMs) {
    log.info("INITIALIZATION SUMMARY");
    log.info(SUB_SEPARATOR);
    log.info("Organizations: created={}, skipped={}", organizationsCreated, organizationsSkipped);
    log.info("Users: created={}, skipped={}", usersCreated, usersSkipped);
    log.info("Execution time: {} ms", durationMs);

    log.info("");
    log.info("TEST USERS (LOCAL ONLY)");
    log.info(SUB_SEPARATOR);

    testCredentials.stream()
            .sorted((a, b) -> {
              int orgCompare = a.subdomain().compareTo(b.subdomain());
              return orgCompare != 0 ? orgCompare : a.email().compareTo(b.email());
            })
            .forEach(c ->
                    log.info("[{}] {} / {} / {} / {}",
                            c.subdomain(),
                            c.email(),
                            c.password(),
                            c.role(),
                            c.active() ? "ACTIVE" : "INACTIVE"
                    )
            );
  }
}