package pl.psnc.pbirecordsuploader.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UploadRequestUnknown extends UploadRequest{
    private String content;

    public UploadRequestUnknown(String content) {
        this.content = content;
    }
}
