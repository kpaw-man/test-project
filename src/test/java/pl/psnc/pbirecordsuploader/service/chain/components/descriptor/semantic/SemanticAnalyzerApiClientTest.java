package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SemanticAnalyzerApiClientTest {

    private MockWebServer mockWebServer;
    private SemanticAnalyzerApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder builder = WebClient.builder().baseUrl(mockWebServer.url("/").toString());
        apiClient = new SemanticAnalyzerApiClient(
                builder,
                5
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnBestMatchWhenResponseIsValid() throws InterruptedException, DescriptorApiException {
        String json = """
            {
              "best_match": "Matched Keyword"
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        String result = apiClient.fetch("some input");

        assertThat(result).isEqualTo("Matched Keyword");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/match");
        assertThat(request.getBody().readUtf8()).contains("input_text");
    }

    @Test
    void shouldThrowWhenBestMatchIsMissing() {
        String json = """
        {
          "other_field": "No match here"
        }
    """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> apiClient.fetch("some input"))
                .isInstanceOf(DescriptorApiException.class)
                .hasMessageContaining("API request failed");
    }


    @Test
    void shouldThrowDescriptorApiExceptionOnServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThatThrownBy(() -> apiClient.fetch("test input"))
                .isInstanceOf(DescriptorApiException.class)
                .hasMessageContaining("API request failed");
    }

    @Test
    void shouldThrowDescriptorApiExceptionOnClientError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad Request"));

        assertThatThrownBy(() -> apiClient.fetch("bad input"))
                .isInstanceOf(DescriptorApiException.class)
                .hasMessageContaining("API request failed");
    }
}