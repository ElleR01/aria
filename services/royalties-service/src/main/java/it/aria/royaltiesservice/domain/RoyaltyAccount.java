package it.aria.royaltiesservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "royalty_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"trackId"}))
public class RoyaltyAccount {

  @Id
  @GeneratedValue
  private UUID accountId;

  @Column(nullable = false)
  private UUID trackId;

  @Column(nullable = false)
  private UUID artistId;

  @Column(nullable = false)
  private long totalStreams;

  @Column(nullable = false)
  private long totalAmountCents;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected RoyaltyAccount() {}

  public RoyaltyAccount(UUID trackId, UUID artistId) {
    this.trackId = trackId;
    this.artistId = artistId;
    this.totalStreams = 0;
    this.totalAmountCents = 0;
    this.createdAt = Instant.now();
  }

  public UUID getAccountId() { return accountId; }
  public UUID getTrackId() { return trackId; }
  public UUID getArtistId() { return artistId; }
  public long getTotalStreams() { return totalStreams; }
  public long getTotalAmountCents() { return totalAmountCents; }
  public Instant getCreatedAt() { return createdAt; }

  public void addStream(long payoutCents) {
    this.totalStreams += 1;
    this.totalAmountCents += payoutCents;
  }
}
