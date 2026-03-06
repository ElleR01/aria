package it.aria.catalogservice.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class RoyaltiesClient {

  private final WebClient webClient;

  public RoyaltiesClient(@Value("${aria.royalties.baseUrl}") String baseUrl) {
    this.webClient = WebClient.builder().baseUrl(baseUrl).build();
  }

  record InitRequest(UUID trackId, UUID artistId) {}

  public void initRoyaltyAccount(UUID trackId, UUID artistId) {
    webClient.post()
        .uri("/royalties/init")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new InitRequest(trackId, artistId))
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

public void addStream(UUID trackId, UUID playId, long payoutCents) {
  webClient.post()
      .uri(uriBuilder -> uriBuilder
          .path("/royalties/{trackId}/stream")
          .queryParam("playId", playId)
          .queryParam("payoutCents", payoutCents)
          .build(trackId))
      .retrieve()
      .bodyToMono(String.class)
      .block();
}

}
