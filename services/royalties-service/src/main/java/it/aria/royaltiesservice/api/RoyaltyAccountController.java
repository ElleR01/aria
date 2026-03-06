package it.aria.royaltiesservice.api;

import it.aria.royaltiesservice.domain.RoyaltyAccount;
import it.aria.royaltiesservice.repo.RoyaltyAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import it.aria.royaltiesservice.repo.ProcessedPlayRepository;
import it.aria.royaltiesservice.domain.ProcessedPlay;

import java.util.UUID;

@RestController
@RequestMapping("/royalties")
public class RoyaltyAccountController {

  private final RoyaltyAccountRepository repo;
  private final ProcessedPlayRepository playRepo;

  public RoyaltyAccountController(RoyaltyAccountRepository repo, ProcessedPlayRepository playRepo) {
    this.repo = repo;
    this.playRepo = playRepo;
  }

  public record InitRequest(UUID trackId, UUID artistId) {}
  public record AccountResponse(UUID trackId, UUID artistId, long totalStreams, long totalAmountCents) {
    static AccountResponse from(RoyaltyAccount a) {
      return new AccountResponse(a.getTrackId(), a.getArtistId(), a.getTotalStreams(), a.getTotalAmountCents());
    }
  }

  @PostMapping("/init")
  @ResponseStatus(HttpStatus.CREATED)
  public AccountResponse init(@Valid @RequestBody InitRequest req) {
    if (req.trackId() == null || req.artistId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId and artistId are required");
    }
    if (repo.existsByTrackId(req.trackId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Royalty account already exists");
    }
    var acc = repo.save(new RoyaltyAccount(req.trackId(), req.artistId()));
    return AccountResponse.from(acc);
  }

  @GetMapping("/{trackId}")
  public AccountResponse get(@PathVariable UUID trackId) {
    var acc = repo.findByTrackId(trackId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Royalty account not found"));
    return AccountResponse.from(acc);
  }

  // demo endpoint: simulate a stream
  @PostMapping("/{trackId}/stream")
public AccountResponse addStream(@PathVariable UUID trackId,
                                 @RequestParam UUID playId,
                                 @RequestParam(defaultValue = "1") long payoutCents) {

  // idempotenza: se playId già visto, NON incrementare
  if (playRepo.existsById(playId)) {
    var acc = repo.findByTrackId(trackId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Royalty account not found"));
    return AccountResponse.from(acc);
  }

  var acc = repo.findByTrackId(trackId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Royalty account not found"));

  playRepo.save(new ProcessedPlay(playId, trackId)); // “marca” come processato
  acc.addStream(payoutCents);

  return AccountResponse.from(repo.save(acc));
}

}
