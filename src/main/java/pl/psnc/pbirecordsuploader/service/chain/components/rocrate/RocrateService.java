package pl.psnc.pbirecordsuploader.service.chain.components.rocrate;

import edu.kit.datamanager.ro_crate.RoCrate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml.XmlContentExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml.XmlExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.serialization.RoCrateSerializer;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocrateService {
    private final RoCrateBuilderService roCrateBuilderService;
    private final RoCrateSerializer serializer;

    public RoCrate createFromDace(ExtractedXmlContent extractedContent) throws ConvertException {
        log.debug("Creating RO-Crate from DACE XML");
        try {
            return roCrateBuilderService.buildRoCrate(extractedContent);
        } catch (Exception e) {
            log.error("Failed to convert DACE XML to RO-Crate", e);
            throw new ConvertException("Failed to convert DACE XML to RO-Crate", e);
        }
    }

    public byte[] serializeToZip(RoCrate roCrate) throws ConvertException {
        log.debug("Serializing RO-Crate to ZIP");
        return serializer.serializeToZip(roCrate);
    }

    public boolean validateCrate(RoCrate roCrate) {
        log.debug("Validating RO-Crate");
        if (roCrate.getRootDataEntity() == null) {
            log.warn("RO-Crate validation failed: missing root data entity");
            return false;
        }

        if (roCrate.getJsonMetadata() == null) {
            log.warn("RO-Crate validation failed: missing JSON metadata");
            return false;
        }
        return true;
    }
}