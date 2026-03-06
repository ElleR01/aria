package it.aria.catalogservice.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import it.aria.catalogservice.domain.ArtistUser;
import it.aria.catalogservice.repo.ArtistUserRepository;
import it.aria.catalogservice.security.JwtService;
import it.aria.catalogservice.security.GoogleTokenVerifierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final ArtistUserRepository repo;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final GoogleTokenVerifierService googleVerifier;

  public AuthController(
      ArtistUserRepository repo,
      PasswordEncoder encoder,
      JwtService jwt,
      GoogleTokenVerifierService googleVerifier
  ) {
    this.repo = repo;
    this.encoder = encoder;
    this.jwt = jwt;
    this.googleVerifier = googleVerifier;
  }

  public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}
  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
  public record GoogleAuthRequest(@NotBlank String idToken) {}
  public record AuthResponse(String accessToken, UUID artistId) {}

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
    if (repo.existsByUsername(req.username())) {
      throw new IllegalStateException("username already exists");
    }

    UUID artistId = UUID.randomUUID();
    String hash = encoder.encode(req.password());
    repo.save(new ArtistUser(req.username(), hash, artistId));

    return new AuthResponse(jwt.generateToken(req.username(), artistId), artistId);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    var user = repo.findByUsername(req.username())
        .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

    if (!encoder.matches(req.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("invalid credentials");
    }

    return new AuthResponse(jwt.generateToken(user.getUsername(), user.getArtistId()), user.getArtistId());
  }

  @PostMapping("/google")
  public AuthResponse googleLogin(@Valid @RequestBody GoogleAuthRequest req) {
    GoogleIdToken.Payload payload = googleVerifier.verify(req.idToken());

    String email = payload.getEmail();
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("google account without email");
    }

    String username = email;

    var existing = repo.findByUsername(username);

    ArtistUser user;
    if (existing.isPresent()) {
      user = existing.get();
    } else {
      UUID artistId = UUID.randomUUID();

      // password fittizia, tanto per login Google non serve davvero
      String randomHash = encoder.encode(UUID.randomUUID().toString());

      user = new ArtistUser(username, randomHash, artistId);
      repo.save(user);
    }

    return new AuthResponse(
        jwt.generateToken(user.getUsername(), user.getArtistId()),
        user.getArtistId()
    );
  }
}