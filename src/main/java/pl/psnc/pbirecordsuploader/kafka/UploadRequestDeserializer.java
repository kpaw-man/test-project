package pl.psnc.pbirecordsuploader.kafka;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import pl.psnc.pbirecordsuploader.domain.UploadRequest;
import pl.psnc.pbirecordsuploader.domain.UploadRequestUnknown;

import java.util.Map;

public class UploadRequestDeserializer implements Deserializer<UploadRequest> {
    private final JsonDeserializer<UploadRequest> jsonDeserializer;

    public UploadRequestDeserializer() {
        this.jsonDeserializer = new JsonDeserializer<>(UploadRequest.class);
    }

    @Override
    public void configure(Map configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public UploadRequest deserialize(String s, byte[] bytes) {
        try {
            return jsonDeserializer.deserialize(s, bytes);
        } catch (Exception e) {
            return new UploadRequestUnknown(new String(bytes));
        }
    }
}
