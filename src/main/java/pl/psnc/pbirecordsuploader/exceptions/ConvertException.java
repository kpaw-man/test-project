package pl.psnc.pbirecordsuploader.exceptions;

public class ConvertException extends Exception {
    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}