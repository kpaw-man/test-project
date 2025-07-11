package pl.psnc.pbirecordsuploader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.UploadRequest;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.service.chain.*;
import pl.psnc.pbirecordsuploader.tracking.annotations.Track;

@Service
@Slf4j
public class UploaderService {

    private final ChainManager chainManager;

    public UploaderService(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    @Track
    public void perform(UploadRequest uploadRequest) throws PbiUploaderException {
        log.info("Processing started for daceID {} ", uploadRequest.getDaceId());
        String daceId = uploadRequest.getDaceId();
        ChainJobEntity chainJobEntity = ChainJobEntity.from(daceId);
        chainManager.execute(chainJobEntity);
    }
}


