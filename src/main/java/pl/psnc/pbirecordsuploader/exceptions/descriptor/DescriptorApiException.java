package pl.psnc.pbirecordsuploader.exceptions.descriptor;


public class DescriptorApiException extends Exception {

    public DescriptorApiException(String message) {
        super(message);
    }

    public DescriptorApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
