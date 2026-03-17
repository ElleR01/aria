package it.aria.catalogservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expMinutes;

  public JwtService(
      @Value("${aria.jwt.secret}") String secret,
      @Value("${aria.jwt.expMinutes}") long expMinutes
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expMinutes = expMinutes;
  }

  // AGGIORNATO: aggiunto ROLE
  public String generateToken(String username, UUID artistId, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMinutes * 60);

    return Jwts.builder()
        .subject(username)
        .claim("artistId", artistId.toString())
        .claim("role", role) // 👈 NUOVO
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public UUID extractArtistId(String token) {
    var claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return UUID.fromString(claims.get("artistId", String.class));
  }

  // NUOVO: estrazione ruolo
  public String extractRole(String token) {
    var claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.get("role", String.class);
  }
}