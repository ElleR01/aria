package it.aria.catalogservice.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

  private final GoogleIdTokenVerifier verifier;

  public GoogleTokenVerifierService(@Value("${aria.google.clientId}") String clientId) {
    this.verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(),
        GsonFactory.getDefaultInstance()
    )
        .setAudience(Collections.singletonList(clientId))
        .build();
  }

  public GoogleIdToken.Payload verify(String idTokenString) {
    try {
      GoogleIdToken idToken = verifier.verify(idTokenString);

      if (idToken == null) {
        throw new IllegalArgumentException("invalid google token");
      }

      return idToken.getPayload();
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalArgumentException("google token verification failed");
    }
  }
}