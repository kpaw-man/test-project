package pl.psnc.pbirecordsuploader.service.chain.components;

import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FetchDataHandlerTest {

    private MockWebServer mockWebServer;

    @InjectMocks
    private FetchDataHandler fetchDataHandler;

    private static final String RECORDS_RESPONSE_BODY = "{ \"records\": [ { \"recordBody\": { \"body\": \"sample body content\" } } ] }";
    private static final String INVALID_JSON_RESPONSE = "{ \"invalid\": \"json\" }";

    private ChainJobEntity chainJobEntity;

    @BeforeEach
    void setUp() throws Exception {
        // Manually starting the mock server since it's not a mocked object anymore.
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder builder = WebClient.builder().baseUrl(mockWebServer.url("/").toString());
        fetchDataHandler = new FetchDataHandler(builder);

        chainJobEntity = new ChainJobEntity();
        chainJobEntity.setDaceId("12345");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void testProcess_SuccessfulFetchAndExtraction() throws PbiUploaderException {
        mockWebServer.enqueue(
                new MockResponse().setBody(RECORDS_RESPONSE_BODY).addHeader("Content-Type", "application/json"));

        boolean result = fetchDataHandler.process(chainJobEntity);

        assertTrue(result, "Process should return true for a successful fetch and extraction");
        assertEquals("sample body content", chainJobEntity.getDaceBody(), "Extracted body should match expected value");
        assertEquals(ChainJobStatus.FETCHED_FROM_SOURCE, chainJobEntity.getChainJobStatus(),
                "Job status should be updated correctly");
    }

    @Test
    void testProcess_FetchDataException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not Found\"}")
                .addHeader("Content-Type", "application/json"));

        PbiUploaderException exception = assertThrows(
                PbiUploaderException.class,
                () -> fetchDataHandler.process(chainJobEntity),
                "Expected process to throw PbiUploaderException for a 404 error response");

        Throwable cause = exception.getCause();
        assertNotNull(cause, "Exception cause should not be null");
        assertTrue(cause.getMessage().contains("Failed to fetch record: {\"error\":\"Not Found\"}"),
                "Cause message should indicate failure to fetch record with the error details");
    }
    @Test
    void testProcess_InvalidJsonExtraction() {
        mockWebServer.enqueue(
                new MockResponse().setBody(INVALID_JSON_RESPONSE).addHeader("Content-Type", "application/json"));

        PbiUploaderException exception = assertThrows(
                PbiUploaderException.class,
                () -> fetchDataHandler.process(chainJobEntity),
                "Expected process to throw PbiUploaderException when invalid JSON is encountered");

        Throwable cause = exception.getCause();
        assertNotNull(cause, "Exception cause should not be null");
        assertInstanceOf(PathNotFoundException.class, cause, "Cause should be a PathNotFoundException");

        assertTrue(cause.getMessage().contains("Missing property in path $['records']"),
                "Cause message should indicate missing property in JSON path");
    }

}
