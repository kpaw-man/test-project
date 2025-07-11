package pl.psnc.pbirecordsuploader.service.chain.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.ValidationException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * ValidateFieldsHandler handles validation of XML fields.
 * <p>
 * NOTE: This handler might not be necessary and is under review for future removal.
 * Ensure that dependent functionality is covered before removing.
 * @Deprecated right now function is not utilised, and probably should not be used anymore
 */
@Slf4j
@Component
@Deprecated(since = "29.05.2025")
public class ValidateFieldsHandler extends Handler {

    private static final String NAMESPACE_PREFIX = "dace";

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        log.debug("ValidateFieldsHandler handling...");
        try {
            String pbiBody = chainJobEntity.getPbiBody();
            if (pbiBody == null || pbiBody.isBlank()) {
                throw new ValidationException("Pbi body cannot be empty");
            }
            ValidationResult validationResult = validateXmlFields(pbiBody);
            if (!validationResult.isValid()) {
                throw new ValidationException("XML validation failed cause: " + validationResult.getErrorMessage());
            }
        } catch (Exception e) {
            handleFailure(chainJobEntity, e);
        }
        return handleSuccess(chainJobEntity, ChainJobStatus.VALIDATED_FIELDS_FROM_SOURCE);
    }

    private ValidationResult validateXmlFields(String xmlContent) {
        try {
            Document document = parseXml(xmlContent);
            XPath xPath = createXPathWithNamespaceContext();

            List<String> missingFields = RequiredFields.getFields(NAMESPACE_PREFIX).stream()
                    .filter(field -> !evaluateFieldExistence(xPath, document, field.xPathExpression()))
                    .map(RequiredField::name).toList();
            if (!missingFields.isEmpty()) {
                return ValidationResult.invalid("Missing fields: " + String.join(", ", missingFields));
            }
            return ValidationResult.valid();
        } catch (Exception e) {
            return ValidationResult.invalid("Validation error: " + e.getMessage());
        }
    }

    private Document parseXml(String xmlContent) throws ValidationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ValidationException("Error parsing XML content", e);
        }
    }

    private XPath createXPathWithNamespaceContext() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CustomNamespaceContext());
        return xPath;
    }

    private boolean evaluateFieldExistence(XPath xPath, Document document, String xPathExpression) {
        try {
            return (Boolean) xPath.evaluate(xPathExpression, document, XPathConstants.BOOLEAN);
        } catch (Exception e) {
            log.warn("Error evaluating XPath expression: {}", xPathExpression, e);
            return false;
        }
    }

    private static class RequiredFields {
        private static final Map<String, String> FIELDS = new LinkedHashMap<>();

        static {
            FIELDS.put("Creator", "count(//%s:creator) > 0");
            FIELDS.put("Title", "count(//%s:title) > 0");
            FIELDS.put("Description", "count(//%s:description) > 0");
            FIELDS.put("Subject", "count(//%s:subject) > 0");
            FIELDS.put("Rights", "count(//%s:rights) > 0");
            FIELDS.put("Relation", "count(//%s:relation) > 0");
            FIELDS.put("Identifier", "count(//%s:identifier) > 0");
        }

        public static List<RequiredField> getFields(String namespacePrefix) {
            return FIELDS.entrySet().stream()
                    .map(entry -> new RequiredField(entry.getKey(), String.format(entry.getValue(), namespacePrefix)))
                    .toList();
        }
    }

    private record RequiredField(String name, String xPathExpression) { }

    @Getter
    @AllArgsConstructor
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }

    private static class CustomNamespaceContext implements NamespaceContext {
        private static final Map<String, String> NAMESPACES = Map.of("dc", "http://purl.org/dc/elements/1.1/",
                NAMESPACE_PREFIX, "https://bs.katowice.pl/bsa/", "terms", "http://purl.org/dc/terms/");

        @Override
        public String getNamespaceURI(String prefix) {
            return NAMESPACES.getOrDefault(prefix, null);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return NAMESPACES.entrySet().stream().filter(entry -> entry.getValue().equals(namespaceURI))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return NAMESPACES.entrySet().stream().filter(entry -> entry.getValue().equals(namespaceURI))
                    .map(Map.Entry::getKey).iterator();
        }
    }
}