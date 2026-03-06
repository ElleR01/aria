package it.aria.catalogservice.api;

import it.aria.catalogservice.domain.Track;
import it.aria.catalogservice.domain.TrackMetadata;
import it.aria.catalogservice.infra.RightsClient;
import it.aria.catalogservice.infra.RoyaltiesClient;
import it.aria.catalogservice.repo.TrackRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Value;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tracks")
public class TrackController {

  private final TrackRepository repo;
  private final RightsClient rightsClient;
  private final RoyaltiesClient royaltiesClient;
  private final MinioClient minio;
  private final String bucket;

  public TrackController(
    TrackRepository repo,
    RightsClient rightsClient,
    RoyaltiesClient royaltiesClient,
    MinioClient minio,
    @Value("${aria.minio.bucket}") String bucket
) {
  this.repo = repo;
  this.rightsClient = rightsClient;
  this.royaltiesClient = royaltiesClient;
  this.minio = minio;
  this.bucket = bucket;
}

  // ===== DTOs =====
  public record CreateTrackRequest(
      @NotBlank String title,
      String description,
      String genre,
      Integer durationSec
  ) {}

  public record TrackResponse(
      UUID trackId,
      UUID artistId,
      String title,
      String description,
      String genre,
      Integer durationSec,
      String status
  ) {
    static TrackResponse from(Track t) {
      return new TrackResponse(
          t.getTrackId(),
          t.getArtistId(),
          t.getMetadata().getTitle(),
          t.getMetadata().getDescription(),
          t.getMetadata().getGenre(),
          t.getMetadata().getDurationSec(),
          t.getStatus().name()
      );
    }
  }

  // ===== Helpers =====
  private UUID currentArtistId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    Object details = auth.getDetails();
    if (details == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "artistId missing from token");
    if (!(details instanceof UUID)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid auth details");
    return (UUID) details;
  }

  private Track mustBelongToArtist(UUID trackId) {
    UUID artistId = currentArtistId();
    var track = repo.findById(trackId).orElseThrow();

    if (!artistId.equals(track.getArtistId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your track");
    }
    return track;
  }

  // ===== Endpoints =====

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TrackResponse create(@Valid @RequestBody CreateTrackRequest req) {
    UUID artistId = currentArtistId();

    var metadata = new TrackMetadata(req.title(), req.description(), req.genre(), req.durationSec());
    var track = new Track(artistId, metadata);
    return TrackResponse.from(repo.save(track));
  }

  @GetMapping
  public List<TrackResponse> list() {
    UUID artistId = currentArtistId();
    return repo.findByArtistId(artistId).stream().map(TrackResponse::from).toList();
  }

  @PutMapping("/{trackId}/submit")
  public TrackResponse submit(@PathVariable UUID trackId) {
    var track = mustBelongToArtist(trackId);

    if (track.getStatus().name().equals("SUBMITTED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Track already submitted");
    }

    // audio must be present
    if (track.getStorageKey() == null || track.getStorageKey().isBlank()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot submit: audio not uploaded");
    }

    track.submit();
    return TrackResponse.from(repo.save(track));
  }

  /**
   * Publish classico: richiede che la licenza ESISTA già.
   */
  @PutMapping("/{trackId}/publish")
  public TrackResponse publish(@PathVariable UUID trackId) {
    var track = mustBelongToArtist(trackId);

    if (track.getStatus().name().equals("PUBLISHED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Track already published");
    }

    if (!track.getStatus().name().equals("SUBMITTED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot publish: track is not submitted");
    }

    boolean exists = rightsClient.licenseExists(trackId);
    if (!exists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot publish: license not found");
    }

    track.publish();
    var saved = repo.save(track);
    royaltiesClient.initRoyaltyAccount(saved.getTrackId(), saved.getArtistId());
    return TrackResponse.from(saved);
  }

  /**
   * ✅ Publish "comodo" per frontend:
   * - Se la licenza non esiste, la crea lui chiamando Rights-service (server-to-server)
   * - Poi pubblica.
   *
   * Così il FE NON deve chiamare 8082 (niente CORS).
   */
  @PutMapping("/{trackId}/publish-with-license")
  public TrackResponse publishWithLicense(@PathVariable UUID trackId) {
    var track = mustBelongToArtist(trackId);

    if (track.getStatus().name().equals("PUBLISHED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Track already published");
    }

    if (!track.getStatus().name().equals("SUBMITTED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot publish: track is not submitted");
    }

    boolean exists = rightsClient.licenseExists(trackId);
    if (!exists) {
      // per prototipo: usiamo artistId come rightsHolderId
      rightsClient.createLicense(track.getTrackId(), track.getArtistId(), true);
    }

    track.publish();
    var saved = repo.save(track);
    royaltiesClient.initRoyaltyAccount(saved.getTrackId(), saved.getArtistId());
    return TrackResponse.from(saved);
  }

  @PostMapping("/{trackId}/play")
  public String play(@PathVariable UUID trackId, @RequestHeader("Idempotency-Key") UUID playId) {
    var track = mustBelongToArtist(trackId);

    if (!track.getStatus().name().equals("PUBLISHED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot play: track not published");
    }

    royaltiesClient.addStream(trackId, playId, 1);
    return "OK";
  }

@DeleteMapping("/{trackId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteDraft(@PathVariable UUID trackId) {
  var auth = SecurityContextHolder.getContext().getAuthentication();
  UUID artistId = (UUID) auth.getDetails(); // messo dal JwtAuthFilter

  var track = repo.findById(trackId).orElseThrow();

  // ownership
  if (!track.getArtistId().equals(artistId)) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your track");
  }

  // solo DRAFT
  if (!track.getStatus().name().equals("DRAFT")) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete: track is not DRAFT");
  }

  // elimina audio da MinIO (se presente)
  String key = track.getStorageKey();
  if (key != null && !key.isBlank()) {
    try {
      minio.removeObject(
          RemoveObjectArgs.builder()
              .bucket(bucket)
              .object(key)
              .build()
      );
    } catch (ErrorResponseException e) {
      // se l'oggetto non esiste, non blocchiamo l'eliminazione del DB
      if (!"NoSuchKey".equals(e.errorResponse().code())) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "MinIO delete failed: " + e.errorResponse().code());
      }
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "MinIO delete failed");
    }
  }

  repo.delete(track);
}

@GetMapping("/{trackId}/stream")
public ResponseEntity<InputStreamResource> stream(@PathVariable UUID trackId) {
  var track = repo.findById(trackId).orElseThrow();

  // solo proprietario (demo)
  var auth = SecurityContextHolder.getContext().getAuthentication();
  UUID artistId = (UUID) auth.getDetails();
  if (artistId == null || !artistId.equals(track.getArtistId())) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your track");
  }

  if (track.getStorageKey() == null || track.getStorageKey().isBlank()) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio not uploaded");
  }

  try {
    var is = minio.getObject(
        GetObjectArgs.builder()
            .bucket(bucket)
            .object(track.getStorageKey())
            .build()
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDisposition(ContentDisposition.inline().build());
    headers.setCacheControl(CacheControl.noStore());

    return new ResponseEntity<>(new InputStreamResource(is), headers, HttpStatus.OK);
  } catch (Exception e) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio not found on storage");
  }
}

}