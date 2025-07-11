package pl.psnc.pbirecordsuploader.exceptions.descriptor;

public class DescriptorValidatonException extends Exception {
    public DescriptorValidatonException(String message) {
        super(message);
    }

    public DescriptorValidatonException(String message, Throwable cause) {
        super(message, cause);
    }
}