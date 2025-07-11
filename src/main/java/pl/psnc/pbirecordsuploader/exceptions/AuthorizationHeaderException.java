package pl.psnc.pbirecordsuploader.exceptions;

public class AuthorizationHeaderException extends RuntimeException {
    public AuthorizationHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
}