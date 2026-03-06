package it.aria.catalogservice.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "artist_users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_artist_users_username", columnNames = "username")
})
public class ArtistUser {

  @Id
  @GeneratedValue
  private UUID userId;

  @Column(nullable = false, length = 80)
  private String username;

  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false)
  private UUID artistId;

  protected ArtistUser() {}

  public ArtistUser(String username, String passwordHash, UUID artistId) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.artistId = artistId;
  }

  public UUID getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
  public UUID getArtistId() { return artistId; }
}