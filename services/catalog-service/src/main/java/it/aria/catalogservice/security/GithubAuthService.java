package it.aria.catalogservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GithubAuthService {

  private final WebClient webClient;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;

  public GithubAuthService(
      @Value("${aria.github.clientId}") String clientId,
      @Value("${aria.github.clientSecret}") String clientSecret,
      @Value("${aria.github.redirectUri}") String redirectUri
  ) {
    this.webClient = WebClient.builder().build();
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
  }

  public String buildAuthorizeUrl() {
    return "https://github.com/login/oauth/authorize"
        + "?client_id=" + clientId
        + "&redirect_uri=" + redirectUri
        + "&scope=read:user user:email";
  }

  public String exchangeCodeForAccessToken(String code) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", clientId);
    form.add("client_secret", clientSecret);
    form.add("code", code);
    form.add("redirect_uri", redirectUri);

    Map<String, Object> response = webClient.post()
        .uri("https://github.com/login/oauth/access_token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(form)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (response == null || response.get("access_token") == null) {
      throw new IllegalArgumentException("GitHub access token not returned");
    }

    return response.get("access_token").toString();
  }

  public GithubUser fetchUser(String accessToken) {
    Map<String, Object> user = webClient.get()
        .uri("https://api.github.com/user")
        .headers(h -> h.setBearerAuth(accessToken))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (user == null) {
      throw new IllegalArgumentException("GitHub user not returned");
    }

    String login = user.get("login") != null ? user.get("login").toString() : null;
    String email = user.get("email") != null ? user.get("email").toString() : null;

    if (email == null || email.isBlank()) {
      List<Map<String, Object>> emails = webClient.get()
          .uri("https://api.github.com/user/emails")
          .headers(h -> h.setBearerAuth(accessToken))
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(List.class)
          .block();

      if (emails != null) {
        for (Map<String, Object> e : emails) {
          Boolean primary = (Boolean) e.get("primary");
          Boolean verified = (Boolean) e.get("verified");
          if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
            email = e.get("email").toString();
            break;
          }
        }
        if ((email == null || email.isBlank()) && !emails.isEmpty()) {
          email = emails.get(0).get("email").toString();
        }
      }
    }

    return new GithubUser(login, email);
  }

  public record GithubUser(String login, String email) {}
}