package md.utm.pad.bid.config;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, WebRequest request) {
        return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
    }
}
