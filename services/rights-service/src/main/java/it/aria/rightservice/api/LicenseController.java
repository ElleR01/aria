package it.aria.rightservice.api;

import it.aria.rightservice.domain.License;
import it.aria.rightservice.repo.LicenseRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/licenses")
public class LicenseController {

  private final LicenseRepository repo;

  public LicenseController(LicenseRepository repo) {
    this.repo = repo;
  }

  public record CreateLicenseRequest(UUID trackId, UUID rightsHolderId, boolean streamingAllowed) {}

  public record LicenseResponse(UUID licenseId, UUID trackId, UUID rightsHolderId, boolean streamingAllowed) {
    static LicenseResponse from(License l) {
      return new LicenseResponse(l.getLicenseId(), l.getTrackId(), l.getRightsHolderId(), l.isStreamingAllowed());
    }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public LicenseResponse create(@Valid @RequestBody CreateLicenseRequest req) {
    if (req.trackId() == null) throw new IllegalArgumentException("trackId is required");
    if (repo.existsByTrackId(req.trackId())) throw new IllegalStateException("License already exists for this track");
    var rhId = (req.rightsHolderId() != null) ? req.rightsHolderId() : UUID.fromString("22222222-2222-2222-2222-222222222222");
    var license = new License(req.trackId(), rhId, req.streamingAllowed());
    return LicenseResponse.from(repo.save(license));
  }

  @GetMapping("/by-track/{trackId}")
  public LicenseResponse getByTrack(@PathVariable UUID trackId) {
    var license = repo.findByTrackId(trackId).orElseThrow();
    return LicenseResponse.from(license);
  }

  @GetMapping("/exists/{trackId}")
  public boolean exists(@PathVariable UUID trackId) {
    return repo.existsByTrackId(trackId);
  }
}
