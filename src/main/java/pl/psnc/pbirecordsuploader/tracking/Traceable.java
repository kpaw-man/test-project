package pl.psnc.pbirecordsuploader.tracking;

import java.util.UUID;

public interface Traceable {
    String DEFAULT_TRACE_KEY = "trackId";

    UUID getTrackId();
}
