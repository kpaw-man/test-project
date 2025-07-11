package pl.psnc.pbirecordsuploader.exceptions;

public class PbiUploaderException extends Exception {
    public PbiUploaderException(String message) {
        super(message);
    }

    public PbiUploaderException(String message, Exception e) {
        super(message, e);
    }

}