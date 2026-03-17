package it.aria.catalogservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = auth.substring(7);

try {
  String username = jwt.extractUsername(token);
  UUID artistId = jwt.extractArtistId(token);
  String role = jwt.extractRole(token);

  var authentication = new UsernamePasswordAuthenticationToken(
      username,
      null,
      List.of(new SimpleGrantedAuthority("ROLE_" + role))
  );

  authentication.setDetails(artistId);

  SecurityContextHolder.getContext().setAuthentication(authentication);
} catch (Exception e) {
  SecurityContextHolder.clearContext();
  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  response.setContentType("application/json");
  response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
  return;
}

    chain.doFilter(request, response);
  }

  @Override
protected boolean shouldNotFilter(HttpServletRequest request) {
  String path = request.getServletPath();
  return path.startsWith("/auth") || path.equals("/ping");
}

}