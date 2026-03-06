package it.aria.catalogservice.repo;

import it.aria.catalogservice.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
  List<Track> findByArtistId(UUID artistId);
}
