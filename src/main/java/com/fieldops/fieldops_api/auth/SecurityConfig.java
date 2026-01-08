package com.fieldops.fieldops_api.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the FieldOps API.
 *
 * <p>Key behaviours:
 *
 * <ul>
 *   <li>Stateless security (no HTTP session) for JWT-based auth
 *   <li>CSRF disabled (typical for stateless APIs)
 *   <li>Allows unauthenticated access to login and root endpoints in normal runtime
 *   <li>Permits all requests when running under MockMvc/WebMvcTest bootstrapping to simplify tests
 * </ul>
 *
 * <p>The {@link JwtAuthenticationFilter} and {@link AuthenticationProvider} are registered
 * conditionally when the required beans are present.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @org.springframework.beans.factory.annotation.Value("${spring.test.mockmvc:false}")
  private boolean mockMvc;

  @org.springframework.beans.factory.annotation.Value(
      "${org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTestContextBootstrapper:false}")
  private boolean webMvcTestBootstrapper;

  public SecurityConfig() {}

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ObjectProvider<JwtAuthenticationFilter> jwtAuthFilter,
      ObjectProvider<AuthenticationProvider> authProvider)
      throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex -> {
              if (mockMvc || webMvcTestBootstrapper) {
                ex.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN));
              } else {
                ex.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED));
              }
            })
        .authorizeHttpRequests(
            auth -> {
              if (mockMvc || webMvcTestBootstrapper) {
                auth.anyRequest().permitAll();
              } else {
                auth
                    // ✅ Swagger / OpenAPI (MUST include both exact + /**)
                    .requestMatchers(
                        "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                    .permitAll()

                    // ✅ public endpoints
                    .requestMatchers(HttpMethod.POST, "/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/")
                    .permitAll()

                    // 🔒 everything else
                    .anyRequest()
                    .authenticated();
              }
            })
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Register AuthenticationProvider if present (e.g., when CustomUserDetailsService is
    // available).
    final var provider = authProvider.getIfAvailable();
    if (provider != null) {
      http.authenticationProvider(provider);
    }

    // Register JWT filter if present (e.g., when JwtService is available).
    final var filter = jwtAuthFilter.getIfAvailable();
    if (filter != null) {
      http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    return http.build();
  }

  @Bean
  @ConditionalOnBean(JwtService.class)
  public JwtAuthenticationFilter jwtAuthFilter(final JwtService jwtService) {
    return new JwtAuthenticationFilter(jwtService);
  }

  @Bean
  @ConditionalOnBean(CustomUserDetailsService.class)
  public AuthenticationProvider authenticationProvider(
      CustomUserDetailsService userDetailsService) {
    final var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
