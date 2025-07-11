package pl.psnc.pbirecordsuploader.exceptions;

public class SendForwardException extends Exception {
    public SendForwardException(String message) {
        super(message);
    }

    public SendForwardException(String message, Throwable cause) {
        super(message, cause);
    }
}