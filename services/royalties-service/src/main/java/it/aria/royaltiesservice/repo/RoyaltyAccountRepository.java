package it.aria.royaltiesservice.repo;

import it.aria.royaltiesservice.domain.RoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoyaltyAccountRepository extends JpaRepository<RoyaltyAccount, UUID> {
  Optional<RoyaltyAccount> findByTrackId(UUID trackId);
  boolean existsByTrackId(UUID trackId);
}
