package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.BNSuggestionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BNDescriptorApiClientTest {

    private MockWebServer mockWebServer;

    private BNDescriptorApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        WebClient.Builder builder = WebClient.builder();
        apiClient = new BNDescriptorApiClient(
                builder,
                baseUrl.substring(0, baseUrl.length() - 1),
                "/suggest",
                5000L,
                100
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnParsedSuggestionWhenResponseIsValid() throws InterruptedException, DescriptorApiException {
        String json = """
            {
              "annif_result": {
                "results": [
                  {
                    "label": "Test Label",
                    "uri": "http://example.com/test"
                  }
                ]
              }
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        BNSuggestionResult result = apiClient.fetch("input text");

        assertThat(result).isNotNull();
        assertThat(result.label()).isEqualTo("Test Label");
        assertThat(result.uri()).isEqualTo("http://example.com/test");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/suggest");
        assertThat(request.getBody().readUtf8()).contains("input text");
    }

    @Test
    void shouldThrowForEmptyResults(){
        String json = """
            {
              "annif_result": {
                "results": []
              }
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
    void shouldThrowDescriptorApiExceptionForServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThatThrownBy(() -> apiClient.fetch("input text"))
                .isInstanceOf(DescriptorApiException.class)
                .hasMessageContaining("API request failed");
    }
}