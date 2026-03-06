package it.aria.catalogservice.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

  @Id
  @GeneratedValue
  private UUID trackId;

  @Column(nullable = false)
  private UUID artistId;

  @Embedded
  @Valid
  private TrackMetadata metadata;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PublishingStatus status;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  // reference to file in MinIO (we add later)
  private String storageKey;

  protected Track() {}

  public Track(UUID artistId, TrackMetadata metadata) {
    this.artistId = artistId;
    this.metadata = metadata;
    this.status = PublishingStatus.DRAFT;
    this.createdAt = Instant.now();
  }

  public UUID getTrackId() { return trackId; }
  public UUID getArtistId() { return artistId; }
  public TrackMetadata getMetadata() { return metadata; }
  public PublishingStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public String getStorageKey() { return storageKey; }

  public void submit() {
    if (status != PublishingStatus.DRAFT) {
      throw new IllegalStateException("Track must be DRAFT to be submitted");
    }
    this.status = PublishingStatus.SUBMITTED;
  }

  public void publish() {
    if (status != PublishingStatus.SUBMITTED) {
      throw new IllegalStateException("Track must be SUBMITTED to be published");
    }
    this.status = PublishingStatus.PUBLISHED;
  }

  public void attachStorageKey(String storageKey) {
    this.storageKey = storageKey;
  }
}
