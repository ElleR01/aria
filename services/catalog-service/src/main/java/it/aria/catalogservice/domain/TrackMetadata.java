package it.aria.catalogservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class TrackMetadata {

  @NotBlank
  private String title;

  private String description;
  private String genre;

  // seconds
  private Integer durationSec;

  protected TrackMetadata() {}

  public TrackMetadata(String title, String description, String genre, Integer durationSec) {
    this.title = title;
    this.description = description;
    this.genre = genre;
    this.durationSec = durationSec;
  }

  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public String getGenre() { return genre; }
  public Integer getDurationSec() { return durationSec; }
}
