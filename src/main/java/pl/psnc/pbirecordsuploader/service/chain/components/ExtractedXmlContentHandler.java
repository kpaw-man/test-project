package pl.psnc.pbirecordsuploader.service.chain.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;
import pl.psnc.pbirecordsuploader.service.chain.Handler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml.XmlContentExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml.XmlExtractor;

@Slf4j
@Component
public class ExtractedXmlContentHandler extends Handler {
    private final XmlExtractor xmlExtractor;
    private final XmlContentExtractor xmlContentExtractor;

    public ExtractedXmlContentHandler(XmlExtractor xmlExtractor, XmlContentExtractor xmlContentExtractor) {
        this.xmlExtractor = xmlExtractor;
        this.xmlContentExtractor = xmlContentExtractor;
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        try {
            log.debug("Handling extracting data");
            String daceBody = chainJobEntity.getDaceBody();
            validateInput(daceBody);

            Document document = xmlExtractor.parseDocument(daceBody);
            ExtractedXmlContent extractedContent = xmlContentExtractor.extractContent(document);
            chainJobEntity.setExtractedXmlContent(extractedContent);
            return handleSuccess(chainJobEntity, ChainJobStatus.EXTRACTED_XML_DATA);

        } catch (Exception e) {
            handleFailure(chainJobEntity,e.getMessage());
        }
        return false;
    }
    private void validateInput(String daceXml) throws ConvertException {
        if (daceXml == null || daceXml.isBlank()) {
            throw new ConvertException("DACE XML content is null or empty");
        }
    }
}