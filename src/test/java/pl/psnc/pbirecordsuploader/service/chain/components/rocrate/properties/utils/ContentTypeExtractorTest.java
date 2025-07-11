package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContentTypeExtractorTest {
    private MockWebServer mockWebServer;
    private ContentTypeExtractor contentTypeExtractor;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        httpClient = HttpClient.newHttpClient();
        contentTypeExtractor = new ContentTypeExtractor(httpClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    @Test
    void shouldExtractContentTypeFromSuccessfulResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/pdf"));

        String url = mockWebServer.url("/document.pdf").toString();

        Optional<String> result = contentTypeExtractor.extract(url);

        assertThat(result).contains("application/pdf");
    }

    @Test
    void shouldCleanContentTypeByRemovingCharsetParameter() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/html; charset=UTF-8"));

        String url = mockWebServer.url("/page.html").toString();

        Optional<String> result = contentTypeExtractor.extract(url);

        assertThat(result).contains("text/html");
    }

    @Test
    void shouldReturnEmptyForNonSuccessfulResponse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        String url = mockWebServer.url("/not-found").toString();

        Optional<String> result = contentTypeExtractor.extract(url);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoContentTypeHeader() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        String url = mockWebServer.url("/no-content-type").toString();

        Optional<String> result = contentTypeExtractor.extract(url);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInvalidUrl() {
        Optional<String> result = contentTypeExtractor.extract("not-a-valid-url");
        assertThat(result).isEmpty();
    }
}