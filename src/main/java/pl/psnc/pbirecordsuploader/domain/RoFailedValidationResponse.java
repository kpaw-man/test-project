package pl.psnc.pbirecordsuploader.domain;

import java.util.List;

public class RoFailedValidationResponse {

    public record Issue(String severity, String check, String message) { }

    public record ValidationResponseData(boolean success, List<Issue> issues) { }
}
