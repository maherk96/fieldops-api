package com.fieldops.fieldops_api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class AuthIntegrationTest {

  private MockMvc mockMvc;

  @Autowired private WebApplicationContext context;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    userRepository.deleteAll();

    User user = new User();
    user.setEmail("engineer@example.com");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setFullName("John Engineer");
    user.setRole("ENGINEER");
    user.setActive(true);
    user.setVersion(1);
    user.setChangeVersion(1L);
    user.setCreatedAt(OffsetDateTime.now());
    user.setUpdatedAt(OffsetDateTime.now());

    userRepository.save(user);
  }

  @Test
  void login_and_me_flow_succeeds() throws Exception {
    // Login
    LoginRequest req = new LoginRequest();
    req.setEmail("engineer@example.com");
    req.setPassword("password123");

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

    String loginJson = loginResult.getResponse().getContentAsString();
    System.out.println("LOGIN JSON: " + loginJson);
    java.util.Map<String, Object> loginMap =
        objectMapper.readValue(
            loginJson,
            new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});

    assertThat(loginMap).isNotNull();
    String token = (String) loginMap.get("accessToken");
    assertThat(token).isNotBlank();
    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) loginMap.get("user");
    assertThat(userMap.get("email")).isEqualTo("engineer@example.com");

    // Call /auth/me with token
    MvcResult meResult =
        mockMvc
            .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

    String meJson = meResult.getResponse().getContentAsString();
    System.out.println("ME JSON: " + meJson);
    java.util.Map<String, Object> meMap =
        objectMapper.readValue(
            meJson,
            new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});

    assertThat(meMap).isNotNull();
    assertThat(meMap.get("email")).isEqualTo("engineer@example.com");
    assertThat(meMap.get("role")).isEqualTo("ENGINEER");
    assertThat(meMap.get("active")).isEqualTo(Boolean.TRUE);
  }
}
