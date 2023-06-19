package stanislaw.wojcik.githubapiconsumer.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import stanislaw.wojcik.githubapiconsumer.entity.ErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler {


    @ExceptionHandler(InvalidHeaderException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidHeaderException() {
        return buildResponseEntity(406, "Invalid headers.");
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException() {
        return buildResponseEntity(404, "User not found.");
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(final int statusCode, final String message) {
        return ResponseEntity
                .status(statusCode)
                .headers(getHeaders())
                .body(new ErrorResponse(statusCode, message));
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        return headers;
    }

}
