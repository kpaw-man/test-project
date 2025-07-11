package pl.psnc.pbirecordsuploader.exceptions;

public class HTTPException extends Exception {
    private final int status;

    public HTTPException(int status) {
        this(status, "");
    }

    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }

    @Override
    public String toString() {
        return "HTTP status: " + status + " Reason: " + getMessage();
    }
}
