package com.fieldops.fieldops_api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
  private long jwtExpiration;

  public String generateToken(UUID userId, String role, String email) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role)
        .claim("email", email)
        .issuedAt(new Date(now))
        .expiration(new Date(now + jwtExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  public UUID extractUserId(String token) {
    String subject = extractClaim(token, Claims::getSubject);
    return UUID.fromString(subject);
  }

  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  public String extractEmail(String token) {
    return extractClaim(token, claims -> claims.get("email", String.class));
  }

  public boolean isTokenValid(String token) {
    try {
      // Parsing with verification validates structure and signature
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSigningKey() {
    byte[] decoded;
    try {
      decoded = Decoders.BASE64.decode(secretKey);
    } catch (IllegalArgumentException ex) {
      decoded = null;
    }

    byte[] keyBytes =
        (decoded != null && decoded.length >= 32)
            ? decoded
            : secretKey.getBytes(StandardCharsets.UTF_8);

    if (keyBytes.length < 32) { // 256-bit minimum for HS256
      throw new IllegalStateException(
          "JWT secret key is too short; require at least 256 bits. Set a stronger JWT_SECRET.");
    }

    return Keys.hmacShaKeyFor(keyBytes);
  }

  public long getExpirationInSeconds() {
    return jwtExpiration / 1000;
  }
}
