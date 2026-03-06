package it.aria.rightservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "licenses", uniqueConstraints = @UniqueConstraint(columnNames = {"trackId"}))
public class License {

  @Id
  @GeneratedValue
  private UUID licenseId;

  @Column(nullable = false)
  private UUID trackId;

  @Column(nullable = false)
  private UUID rightsHolderId;

  @Column(nullable = false)
  private boolean streamingAllowed;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected License() {}

  public License(UUID trackId, UUID rightsHolderId, boolean streamingAllowed) {
    this.trackId = trackId;
    this.rightsHolderId = rightsHolderId;
    this.streamingAllowed = streamingAllowed;
    this.createdAt = Instant.now();
  }

  public UUID getLicenseId() { return licenseId; }
  public UUID getTrackId() { return trackId; }
  public UUID getRightsHolderId() { return rightsHolderId; }
  public boolean isStreamingAllowed() { return streamingAllowed; }
  public Instant getCreatedAt() { return createdAt; }
}
