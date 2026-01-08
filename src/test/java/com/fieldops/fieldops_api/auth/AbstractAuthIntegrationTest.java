package com.fieldops.fieldops_api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractAuthIntegrationTest {

  @Autowired protected WebApplicationContext context;

  @Autowired protected UserRepository userRepository;

  @Autowired protected PasswordEncoder passwordEncoder;

  protected MockMvc mockMvc;
  protected final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void baseSetUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    userRepository.deleteAll();
  }

  protected User createUser(
      String email, String rawPassword, String fullName, String role, boolean active) {
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setFullName(fullName);
    user.setRole(role);
    user.setActive(active);
    user.setVersion(1);
    user.setChangeVersion(1L);
    user.setCreatedAt(OffsetDateTime.now());
    user.setUpdatedAt(OffsetDateTime.now());
    return userRepository.save(user);
  }

  protected String loginAndGetToken(String email, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "email", email,
                                "password", password))))
            .andReturn();

    String json = result.getResponse().getContentAsString();
    if (result.getResponse().getStatus() != 200) {
      throw new IllegalStateException(
          "Login failed with status " + result.getResponse().getStatus() + ": " + json);
    }
    Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
    Object token = map.get("accessToken");
    assertThat(token).isInstanceOf(String.class);
    return (String) token;
  }

  protected UUID assertAndExtractUserIdFromLogin(MvcResult loginResult) throws Exception {
    String json = loginResult.getResponse().getContentAsString();
    Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
    @SuppressWarnings("unchecked")
    Map<String, Object> userMap = (Map<String, Object>) map.get("user");
    assertThat(userMap.get("id")).isInstanceOf(String.class);
    return UUID.fromString((String) userMap.get("id"));
  }
}
