package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class XmlExtractorTest {

    @InjectMocks
    private XmlExtractor xmlExtractor;


    @Test
    void testParseDocument_validXml_shouldReturnDocument() {
        String xml = "<root><item>value1</item><item>value2</item></root>";
        assertDoesNotThrow(() -> {
            Document doc = xmlExtractor.parseDocument(xml);
            assertNotNull(doc.getDocumentElement());
            assertEquals("root", doc.getDocumentElement().getNodeName());
        });
    }

    @Test
    void testParseDocument_invalidXml_shouldThrowConvertException() {
        String invalidXml = "<root><item>missing end tag";
        assertThrows(ConvertException.class, () -> xmlExtractor.parseDocument(invalidXml));
    }

    @Test
    void testExtractValues_validXPath_shouldReturnValues() throws ConvertException {
        String xml = "<root><item>value1</item><item>value2</item></root>";
        Document doc = xmlExtractor.parseDocument(xml);
        List<String> values = xmlExtractor.extractValues(doc, "/root/item");

        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    @Test
    void testExtractValues_invalidXPath_shouldThrowConvertException() throws ConvertException {
        String xml = "<root><item>value1</item></root>";
        Document doc = xmlExtractor.parseDocument(xml);

        assertThrows(ConvertException.class, () -> xmlExtractor.extractValues(doc, "//*["));
    }
}