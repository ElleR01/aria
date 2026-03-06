package it.aria.catalogservice.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

  record ErrorResponse(
      Instant timestamp,
      int status,
      String error,
      String message,
      String path
  ) {}

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handle(ResponseStatusException ex,
                                             jakarta.servlet.http.HttpServletRequest req) {

    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

    var body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        ex.getReason(),          
        req.getRequestURI()
    );

    return ResponseEntity.status(status).body(body);
  }
}
