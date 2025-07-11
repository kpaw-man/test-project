package pl.psnc.pbirecordsuploader.service.chain.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;
import pl.psnc.pbirecordsuploader.model.HdtOntology;
import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.service.chain.Handler;
import pl.psnc.pbirecordsuploader.service.chain.components.annotations.AnnotationService;

import java.util.*;


/**
 * Responsibility of this handler is to provide to existing RO additional annotations.
 * THis might serve as post-creating stage which enhances RO.
 */
@Slf4j
@Component
public class UploadAnnotationsHandler extends Handler {
    private final AnnotationService annotationService;

    public UploadAnnotationsHandler(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        try {
            log.debug("Starting UploadAnnotationsHandler");

            ExtractedXmlContent content = chainJobEntity.getExtractedXmlContent();
            String rosId = chainJobEntity.getRocrateUploadStatus().getRosId();

            postIsDigitalTwin(content, rosId);

            return handleSuccess(chainJobEntity, ChainJobStatus.UPLOADED_ANNOTATIONS);
        } catch (Exception e) {
            log.error("Failed in UploadAnnotationsHandler: {}", e.getMessage(), e);
            handleFailure(chainJobEntity, e.getMessage());
            return false;
        }
    }

    /*
    Provides HDT information about whole RO, in this particular case it is providing annotation which binds
    RO with HP1_is_digitial_twin
     */
    private void postIsDigitalTwin(ExtractedXmlContent content, String rosId) throws PbiUploaderException {
        String digitalTwinValue = Optional.ofNullable(content.properties().get(ContentProperties.URL.key()))
                .flatMap(list -> list.stream().findFirst())
                .orElseThrow(() -> new PbiUploaderException("Missing digital twin URL"));

        annotationService.postAnnotation(rosId, digitalTwinValue, HdtOntology.HP1_IS_DIGITAL_TWIN_OF.getKey());
    }
}