package it.aria.catalogservice.repo;

import it.aria.catalogservice.domain.ArtistUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtistUserRepository extends JpaRepository<ArtistUser, UUID> {
  Optional<ArtistUser> findByUsername(String username);
  boolean existsByUsername(String username);
}