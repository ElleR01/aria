package it.aria.royaltiesservice.repo;

import it.aria.royaltiesservice.domain.ProcessedPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedPlayRepository extends JpaRepository<ProcessedPlay, UUID> {}
