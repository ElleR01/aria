package it.aria.rightservice.repo;

import it.aria.rightservice.domain.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
  Optional<License> findByTrackId(UUID trackId);
  boolean existsByTrackId(UUID trackId);
}
