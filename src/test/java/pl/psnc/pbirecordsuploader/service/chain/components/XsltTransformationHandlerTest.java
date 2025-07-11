package pl.psnc.pbirecordsuploader.service.chain.components;

import org.junit.jupiter.api.BeforeEach;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;

import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.XsltTransformationException;


import static org.junit.jupiter.api.Assertions.*;

class XsltTransformationHandlerTest {

    private static final String XSLT_FILE_PATH = "xslt/pbi-rohub.xsl";


    private XsltTransformationHandler handler;
    private ChainJobEntity testChainJobEntity;

    @BeforeEach
    void setUp() {
        // Initialize handler with test XSLT from resources
        handler = new XsltTransformationHandler(XSLT_FILE_PATH);

        // Initialize test data
        testChainJobEntity = new ChainJobEntity();
        testChainJobEntity.setDaceId("TEST-123");
        testChainJobEntity.setDaceBody("<test><data>Original Content</data></test>");
    }

    @Test
    void process_SuccessfulTransformation_ShouldReturnTrue() throws Exception {
        // Act
        boolean result = handler.process(testChainJobEntity);

        // Assert
        assertTrue(result);
        assertNotNull(testChainJobEntity.getPbiBody());
        assertFalse(testChainJobEntity.getPbiBody().isEmpty());
        assertEquals(ChainJobStatus.XSLT_TRANSFORMATION, testChainJobEntity.getChainJobStatus());
    }

    @Test
    void process_InvalidXmlInput_ShouldThrowException() {
        // Arrange
        testChainJobEntity.setDaceBody("invalid xml content");

        // Act & Assert
        PbiUploaderException exception = assertThrows(PbiUploaderException.class,
                () -> handler.process(testChainJobEntity));

        assertEquals("Upload terminated, for daceID TEST-123", exception.getMessage());
        assertInstanceOf(XsltTransformationException.class, exception.getCause());
        assertEquals("Record TEST-123 transformation failed", exception.getCause().getMessage());
        assertEquals(ChainJobStatus.FAILED, testChainJobEntity.getChainJobStatus());
    }

    @Test
    void process_NullInput_ShouldHandleGracefully()   {
        // Arrange
        testChainJobEntity.setDaceBody(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> handler.process(testChainJobEntity));
        assertEquals("Dace body cannot be empty, transformation terminated", exception.getMessage());
    }

    @Test
    void process_LargeInput_ShouldProcessSuccessfully() throws Exception {
        // Arrange
        StringBuilder largeXml = new StringBuilder("<root>");
        for (int i = 0; i < 1000; i++) {
            largeXml.append("<item>").append(i).append("</item>");
        }
        largeXml.append("</root>");
        testChainJobEntity.setDaceBody(largeXml.toString());

        // Act
        boolean result = handler.process(testChainJobEntity);

        // Assert
        assertTrue(result);
        assertNotNull(testChainJobEntity.getPbiBody());
    }

    @Test
    void constructor_InvalidXsltPath_ShouldThrowException() {
        assertThrows(IllegalStateException.class, () ->
                new XsltTransformationHandler("non-existent.xslt")
        );
    }
}