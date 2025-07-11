package pl.psnc.pbirecordsuploader.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.psnc.pbirecordsuploader.tracking.Traceable;

import java.util.Map;
import java.util.UUID;


@Data
@RequiredArgsConstructor
public class UploadRequest implements Traceable {

    private String daceId;
    private Map<String, Object> metadata;

    @Override
    public  UUID getTrackId() {
        if (metadata != null && metadata.containsKey(Traceable.DEFAULT_TRACE_KEY)) {
            return UUID.fromString(metadata.get(Traceable.DEFAULT_TRACE_KEY).toString());
        }
        throw new IllegalStateException("Metadata does not contain a valid trace key");
    }
}

