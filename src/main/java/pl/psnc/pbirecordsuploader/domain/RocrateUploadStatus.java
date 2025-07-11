package pl.psnc.pbirecordsuploader.domain;

import lombok.Data;

@Data
public class RocrateUploadStatus {
    private String jobIdentifier;
    private String jobStatus;
    private String jobProgress;
    private String rosId;

    public static RocrateUploadStatus of(String jobIdentifier) {
        RocrateUploadStatus rocrateUploadStatus = new RocrateUploadStatus();
        rocrateUploadStatus.setJobIdentifier(jobIdentifier);
        return rocrateUploadStatus;
    }
}