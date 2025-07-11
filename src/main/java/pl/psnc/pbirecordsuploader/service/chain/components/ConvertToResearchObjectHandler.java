package pl.psnc.pbirecordsuploader.service.chain.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.RocrateService;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConvertToResearchObjectHandler extends Handler {

    private final RocrateService rocrateService;

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        try {
            log.debug("Converting record to ResearchObject...");
            var roCrate = rocrateService.createFromDace(chainJobEntity.getExtractedXmlContent());
            if (!rocrateService.validateCrate(roCrate)) {
                throw new PbiUploaderException("Failed to create a valid RO-Crate");
            }
            byte[] roCrateBytes = rocrateService.serializeToZip(roCrate);
            chainJobEntity.setRoCrate(roCrateBytes);
            return handleSuccess(chainJobEntity, ChainJobStatus.CONVERTED_TO_DIRTY_RO);
        } catch (Exception e) {
            handleFailure(chainJobEntity, e);
        }
        return false;
    }
}