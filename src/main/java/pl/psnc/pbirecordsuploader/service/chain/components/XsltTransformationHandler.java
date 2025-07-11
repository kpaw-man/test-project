package pl.psnc.pbirecordsuploader.service.chain.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.XsltTransformationException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class XsltTransformationHandler extends Handler {

    private final Transformer transformer;

    public XsltTransformationHandler(@Value("${xslt.filepath}") String xsltFilePath) {
        this.transformer = initializeTransformer(xsltFilePath);
    }

    private Transformer initializeTransformer(String xsltFilePath) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Source source = new StreamSource(ResourceUtils.getFile("classpath:" + xsltFilePath));
            Transformer newTransformer = factory.newTransformer(source);

            newTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            newTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return newTransformer;
        } catch (TransformerConfigurationException | FileNotFoundException e) {
            log.error("Failed to initialize XSLT transformer", e);
            throw new IllegalStateException("Failed to initialize XSLT transformer", e);
        }
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        log.debug("Starting XSLT transformation for DACE ID: {}", chainJobEntity.getDaceId());

        if (chainJobEntity.getDaceBody() == null) {
            throw new IllegalStateException("Dace body cannot be empty, transformation terminated");
        }
        try {
            String pbiBody = transform(chainJobEntity.getDaceBody(), chainJobEntity.getDaceId());
            chainJobEntity.setPbiBody(pbiBody);
            return handleSuccess(chainJobEntity, ChainJobStatus.XSLT_TRANSFORMATION);
        } catch (Exception e) {
            handleFailure(chainJobEntity, e);
            return false;
        }
    }

    private String transform(String xml, String daceId) throws XsltTransformationException {
        log.debug("Transforming record {}", daceId);

        try (InputStream xmlStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            StringWriter result = new StringWriter();
            transformer.transform(new StreamSource(xmlStream), new StreamResult(result));
            if (log.isDebugEnabled()) {
                log.debug("Transformed record for DACE ID {}: {}", daceId, result);
            }
            return result.toString();
        } catch (IOException | TransformerException e) {
            String errorMsg = String.format("Record %s transformation failed", daceId);
            log.error(errorMsg, e);
            throw new XsltTransformationException(errorMsg, e);
        }
    }
}