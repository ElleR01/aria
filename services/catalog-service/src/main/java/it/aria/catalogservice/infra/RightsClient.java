package it.aria.catalogservice.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class RightsClient {

  private final WebClient webClient;

  public RightsClient(@Value("${aria.rights.baseUrl}") String baseUrl) {
    this.webClient = WebClient.builder().baseUrl(baseUrl).build();
  }

  public boolean licenseExists(UUID trackId) {
    return Boolean.TRUE.equals(
        webClient.get()
            .uri("/licenses/exists/{trackId}", trackId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block()
    );
  }

  public void createLicense(UUID trackId, UUID rightsHolderId, boolean streamingAllowed) {
    webClient.post()
        .uri("/licenses")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new CreateLicenseRequest(trackId, rightsHolderId, streamingAllowed))
        .retrieve()
        .bodyToMono(String.class) // non ti serve il body, ma così “consuma” la risposta
        .block();
  }

  public record CreateLicenseRequest(
      UUID trackId,
      UUID rightsHolderId,
      boolean streamingAllowed
  ) {}
}