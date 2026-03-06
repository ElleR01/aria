package it.aria.catalogservice.api;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import it.aria.catalogservice.repo.TrackRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/tracks")
public class AudioUploadController {

  private final TrackRepository repo;
  private final MinioClient minio;
  private final String bucket;

  public AudioUploadController(
      TrackRepository repo,
      MinioClient minio,
      @Value("${aria.minio.bucket}") String bucket
  ) {
    this.repo = repo;
    this.minio = minio;
    this.bucket = bucket;
  }

  @PostMapping("/{trackId}/audio")
  @ResponseStatus(HttpStatus.OK)
  public String uploadAudio(@PathVariable UUID trackId, @RequestParam("file") MultipartFile file) throws Exception {
    var track = repo.findById(trackId).orElseThrow();

 // se la traccia è publish, l'audio non può essere modificato
 if (track.getStatus().name().equals("PUBLISHED")) {
    throw new ResponseStatusException(HttpStatus.CONFLICT,
        "Cannot upload audio: track already published");
  }

    String originalName = file.getOriginalFilename();
    if (originalName == null || originalName.isBlank()) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "File must have a valid name");
    }
    String objectName = "tracks/" + trackId + "/" + originalName;

    minio.putObject(
        PutObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .contentType(file.getContentType())
            .stream(file.getInputStream(), file.getSize(), -1)
            .build()
    );

    track.attachStorageKey(objectName);
    repo.save(track);

    return "Uploaded: " + objectName;
  }
}
