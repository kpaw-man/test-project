package pl.psnc.pbirecordsuploader.exceptions;

public class XsltTransformationException extends Exception {
    public XsltTransformationException(String message) {
        super(message);
    }

    public XsltTransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}