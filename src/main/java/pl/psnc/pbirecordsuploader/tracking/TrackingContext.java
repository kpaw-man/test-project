package pl.psnc.pbirecordsuploader.tracking;

import org.slf4j.MDC;

import java.util.UUID;

public final class TrackingContext {
    private static final String CORRELATION_ID_NAME = "trackId";

    private TrackingContext() {
    }

    public static void setTrackId(UUID value) {
        MDC.put(CORRELATION_ID_NAME, value.toString());
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID_NAME);
    }
}