package it.aria.royaltiesservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_plays")
public class ProcessedPlay {

  @Id
  private UUID playId;

  @Column(nullable = false)
  private UUID trackId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected ProcessedPlay() {}

  public ProcessedPlay(UUID playId, UUID trackId) {
    this.playId = playId;
    this.trackId = trackId;
    this.createdAt = Instant.now();
  }

  public UUID getPlayId() { return playId; }
}
