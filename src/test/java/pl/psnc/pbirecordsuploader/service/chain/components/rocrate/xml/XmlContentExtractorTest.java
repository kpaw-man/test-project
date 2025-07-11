package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.anyString;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class XmlContentExtractorTest {

    @Mock
    private Document mockDocument;

    @Mock
    private XmlExtractor xmlExtractor;

    @InjectMocks
    private XmlContentExtractor xmlContentExtractor;


    @Test
    void testExtractContent_SuccessfulExtraction() throws Exception {
        when(xmlExtractor.extractValues(eq(mockDocument), anyString())).thenAnswer(invocation -> {
            String xpath = invocation.getArgument(1);
            if (xpath.contains("title")) {
                return List.of("Test Title");
            }
            if (xpath.contains("creator")) {
                return List.of("Author A");
            }
            return List.of();
        });

        ExtractedXmlContent result = xmlContentExtractor.extractContent(mockDocument);
        assertNotNull(result);
        assertEquals(List.of("Test Title"), result.properties().get("title"));
        assertEquals(List.of("Author A"), result.properties().get("creator"));

        assertFalse(result.properties().containsKey("rights"));
    }

    @Test
    void testExtractContent_WithEmptyResults() throws Exception {
        when(xmlExtractor.extractValues(eq(mockDocument), anyString())).thenReturn(List.of());

        ExtractedXmlContent result = xmlContentExtractor.extractContent(mockDocument);

        assertNotNull(result);
        assertTrue(result.properties().isEmpty());
    }

    @Test
    void testExtractContent_ThrowsConvertException_OnCommonPropertyError() throws Exception {
        when(xmlExtractor.extractValues(mockDocument, "//*[local-name()='title']")).thenThrow(
                new RuntimeException("XPath failure"));

        Exception exception = assertThrows(ConvertException.class, () -> xmlContentExtractor.extractContent(mockDocument));

        assertTrue(exception.getMessage().contains("Failed to extract property: title"));
    }

    @Test
    void testExtractContent_ThrowsConvertException_OnDescriptorError() throws Exception {
        for (String property : List.of("title", "created", "creator", "issued", "contributor", "identifier",
                "description", "subject", "relation", "format", "source", "type", "spatial", "license", "rights")) {
            String xpath = "//*[local-name()='" + property + "']";
            when(xmlExtractor.extractValues(mockDocument, xpath)).thenReturn(List.of());
        }

        when(xmlExtractor.extractValues(mockDocument,
                "//*[local-name()='type']")).thenThrow(
                new RuntimeException("XPath failure"));

        ConvertException exception = assertThrows(ConvertException.class, () -> xmlContentExtractor.extractContent(mockDocument));
        assertTrue(exception.getMessage().contains("Failed to extract property: type"));
    }
}