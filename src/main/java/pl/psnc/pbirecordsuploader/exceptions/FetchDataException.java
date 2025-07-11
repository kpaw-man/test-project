package pl.psnc.pbirecordsuploader.exceptions;

public  class FetchDataException extends Exception {
    public FetchDataException(String message) {
        super(message);
    }

    public FetchDataException(String message, Throwable cause) {
        super(message, cause);
    }
}