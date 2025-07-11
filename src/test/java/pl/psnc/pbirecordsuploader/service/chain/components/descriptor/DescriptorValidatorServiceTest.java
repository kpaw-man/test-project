package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;
import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class DescriptorValidatorServiceTest {

    private MockWebServer mockWebServer;
    private DescriptorValidatorService validatorService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString());

        validatorService = new DescriptorValidatorService(builder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldPassValidationWhenResourceExists() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        Descriptor descriptor = new Descriptor("http://example.com/abc123", "Test");

        assertThatCode(() -> validatorService.validateDescriptor(descriptor)).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        Descriptor descriptor = new Descriptor("http://example.com/xyz", "Missing");

        assertThatThrownBy(() -> validatorService.validateDescriptor(descriptor))
                .isInstanceOf(DescriptorValidatonException.class)
                .hasMessageContaining("Descriptor of id xyz does not exist");
    }

    @Test
    void shouldThrowOnInvalidDescriptorUrl() {
        Descriptor bad = new Descriptor("invalid-url", "Bad");

        assertThatThrownBy(() -> validatorService.validateDescriptor(bad))
                .isInstanceOf(DescriptorValidatonException.class)
                .hasMessageContaining("Invalid descriptor URL");
    }

    @Test
    void shouldThrowOnServerError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Descriptor descriptor = new Descriptor("http://example.com/zzz999", "ServerError");

        assertThatThrownBy(() -> validatorService.validateDescriptor(descriptor))
                .hasMessageContaining("Validation failed for ID: zzz999");
    }

    @Test
    void shouldExtractIdCorrectlyFromUrl() throws DescriptorValidatonException {
        String url = "http://example.com/some/path/descriptorId42";
        String extracted = validatorService.extractIdFromDescriptorUrl(url);
        assertThat(extracted).isEqualTo("descriptorId42");
    }

    @Test
    void shouldThrowOnNullOrBadUrlInExtractId() {
        assertThatThrownBy(() -> validatorService.extractIdFromDescriptorUrl(null))
                .isInstanceOf(DescriptorValidatonException.class);
        assertThatThrownBy(() -> validatorService.extractIdFromDescriptorUrl("no-slash"))
                .isInstanceOf(DescriptorValidatonException.class);
    }
    @Test
    void shouldThrowOnEmptyIdInUrl() {
        String url = "http://example.com/some/path/";
        assertThatThrownBy(() -> validatorService.extractIdFromDescriptorUrl(url))
                .isInstanceOf(DescriptorValidatonException.class)
                .hasMessageContaining("Empty ID extracted from URL");
    }
}