package pl.psnc.pbirecordsuploader.service.chain.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.SendForwardException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import static org.mockito.Mockito.*;

class SendForwardHandlerTest {

    private MockWebServer mockWebServer;
    private SendForwardHandler handler;
    private ChainJobEntity mockChainJobEntity;
    ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() throws IOException {
        // Start the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Create a WebClient instance pointing to the MockWebServer
        WebClient.Builder builder = WebClient.builder().baseUrl(mockWebServer.url("/").toString());

        // Initialize the SendForwardHandler with the mock WebClient
        handler = new SendForwardHandler(objectMapper,builder);

        // Mock a ChainJobEntity for testing
        mockChainJobEntity = mock(ChainJobEntity.class);
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn("test-content".getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
//    @Test
//    void testProcess_SuccessfulUpload() throws PbiUploaderException {
//        // Arrange: Mock the response from the server
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody("Upload successful"));
//
//        // Act: Call the process method
//        boolean result = handler.process(mockChainJobEntity);
//
//        // Assert: Verify the success behavior
//        assertTrue(result);
//        verify(mockChainJobEntity, times(2)).getDaceId();
//        verify(mockChainJobEntity, times(1)).getRoCrate();
//    }

    @Test
    void testProcess_FileContentMissing() {
        // Arrange: Set the RO-Crate content to null
        when(mockChainJobEntity.getRoCrate()).thenReturn(null);

        // Act & Assert: Ensure the process method throws the expected exception
        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(mockChainJobEntity));

        // Validate exception message and cause
        assertEquals("Upload terminated, for daceID test-dace-id", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(SendForwardException.class, exception.getCause());
        assertEquals(
                "RO-Crate byte array is missing or empty for DACE ID: test-dace-id",
                exception.getCause().getMessage()
        );
    }

    @Test
    void testProcess_UploadFails_Exception() {
        // Arrange: Simulate an exception during file upload
        when(mockChainJobEntity.getRoCrate()).thenThrow(new RuntimeException("Simulated exception"));

        // Act & Assert: Ensure the process method throws the expected exception
        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(mockChainJobEntity));

        // Validate exception message and cause
        assertEquals("Upload terminated, for daceID test-dace-id", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Simulated exception", exception.getCause().getMessage());
    }
}