package com.fieldops.fieldops_api.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom UserDetails implementation that includes organization context.
 *
 * <p>This class extends the standard Spring Security UserDetails to include:
 *
 * <ul>
 *   <li>User ID
 *   <li>Organization ID (for tenant isolation)
 *   <li>Role-based authorities
 * </ul>
 */
@Getter
public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final String email;
  private final String password;
  private final UUID organizationId;
  private final String role;
  private final boolean active;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(
      final UUID id,
      final String email,
      final String password,
      final UUID organizationId,
      final String role,
      final boolean active) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.organizationId = organizationId;
    this.role = role;
    this.active = active;
    this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
