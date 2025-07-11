package pl.psnc.pbirecordsuploader.domain;

import lombok.Data;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;

import java.util.UUID;

@Component
@Data
public class ChainJobEntity {
    private UUID id;
    private String daceId;
    private String daceBody;
    private ExtractedXmlContent extractedXmlContent;
    private String pbiBody;
    private ChainJobStatus chainJobStatus;
    private byte[] roCrate;
    private RocrateUploadStatus rocrateUploadStatus;

    public static ChainJobEntity from(String daceId) {
        ChainJobEntity chainJobEntity = new ChainJobEntity();
        chainJobEntity.setChainJobStatus(ChainJobStatus.NEW);
        chainJobEntity.setId(UUID.randomUUID());
        chainJobEntity.setDaceId(daceId);
        return chainJobEntity;
    }
}