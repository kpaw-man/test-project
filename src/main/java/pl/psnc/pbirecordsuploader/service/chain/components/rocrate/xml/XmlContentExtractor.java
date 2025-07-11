package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlContentExtractor {

    private static final List<MetadataProperty> COMMON_PROPERTIES = Stream.concat(Arrays.stream(DCTerms.values()),
            Arrays.stream(ContentProperties.values())).collect(Collectors.toList());

    private static final String LOCAL_NAME_XPATH_TEMPLATE = "//*[local-name()='%s']";
    private final XmlExtractor xmlExtractor;

    public ExtractedXmlContent extractContent(Document document) throws ConvertException {
        Map<String, List<String>> commonProps = new HashMap<>();

        for (MetadataProperty property : COMMON_PROPERTIES) {
            String xpath = String.format(LOCAL_NAME_XPATH_TEMPLATE, property.key());
            try {
                List<String> values = xmlExtractor.extractValues(document, xpath);
                if (!values.isEmpty()) {
                    commonProps.put(property.key(), values);
                }
            } catch (Exception e) {
                throw new ConvertException("Failed to extract property: " + property.key(), e);
            }
        }
        return new ExtractedXmlContent(commonProps);
    }
}