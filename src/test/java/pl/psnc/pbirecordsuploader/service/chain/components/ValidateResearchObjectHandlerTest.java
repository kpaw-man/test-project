package pl.psnc.pbirecordsuploader.service.chain.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidateResearchObjectHandlerTest {

    private MockWebServer mockWebServer;
    private ValidateResearchObjectHandler handler;
    private ChainJobEntity mockChainJobEntity;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder builder = WebClient.builder().baseUrl(mockWebServer.url("/").toString());

        handler = new ValidateResearchObjectHandler(builder, "REQUIRED", new ObjectMapper());

        mockChainJobEntity = mock(ChainJobEntity.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testProcess_ValidationSuccess() throws PbiUploaderException {
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn("valid-ro-crate".getBytes());

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"success\": true}"));

        boolean result = handler.process(mockChainJobEntity);

        assertTrue(result);
        verify(mockChainJobEntity, times(2)).getDaceId();
    }

    @Test
    void testProcess_ValidationFails() {
        // Arrange: Set up mock behavior
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn("invalid-ro-crate".getBytes());

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"success\": false, \"issues\": [{\"message\": \"Invalid structure\"}]}"));

        // Act: Call the process method and expect the exception to be thrown
        PbiUploaderException thrownException = assertThrows(PbiUploaderException.class, () -> {
            handler.process(mockChainJobEntity);
        });

        // Assert: Verify that the exception message contains expected details
        assertTrue(thrownException.getMessage().contains("Upload terminated, for daceID test-dace-id"));
        assertTrue(thrownException.getMessage().contains("Validation failed:"));
    }

    @Test
    void testProcess_RoCrateMissing() {
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn(null);

        PbiUploaderException thrownException = assertThrows(PbiUploaderException.class, () -> handler.process(mockChainJobEntity));

        assertTrue(thrownException.getMessage().contains("Upload terminated, for daceID test-dace-id"));
        assertTrue(thrownException.getMessage()
                .contains("RO-Crate byte array is missing or empty for DACE ID: test-dace-id"));
    }

    @Test
    void testProcess_ClientError() {
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn("valid-ro-crate".getBytes());

        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("me so dummy"));

        PbiUploaderException thrownException = assertThrows(PbiUploaderException.class, () -> handler.process(mockChainJobEntity));

        String expectedMessage = "Upload terminated, for daceID test-dace-id Error during validation: HTTP status: 400 Reason: Client error: me so dummy";
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    void testProcess_ServerError() {
        when(mockChainJobEntity.getDaceId()).thenReturn("test-dace-id");
        when(mockChainJobEntity.getRoCrate()).thenReturn("valid-ro-crate".getBytes());

        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Server so dummy"));

        PbiUploaderException thrownException = assertThrows(PbiUploaderException.class, () -> handler.process(mockChainJobEntity));

        String expectedMessage = "Upload terminated, for daceID test-dace-id Error during validation: HTTP status: 500 Reason: Server error: Server so dummy";
        assertEquals(expectedMessage, thrownException.getMessage());
    }
}