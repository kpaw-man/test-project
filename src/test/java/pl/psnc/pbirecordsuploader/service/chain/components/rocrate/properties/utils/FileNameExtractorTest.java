package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class FileNameExtractorTest {

    private FileNameExtractor fileNameExtractor;

    @BeforeEach
    void setUp() {
        fileNameExtractor = new FileNameExtractor();
    }

    @Test
    void shouldExtractFilenameFromValidUrl() {
        String url = "https://example.com/documents/report.pdf";
        String result = fileNameExtractor.extract(url);
        assertThat(result).isEqualTo("report.pdf");
    }

    @Test
    void shouldExtractFilenameWithComplexPath() {
        String url = "https://api.example.com/v1/files/archive/data.tar.gz";
        String result = fileNameExtractor.extract(url);
        assertThat(result).isEqualTo("data.tar.gz");
    }

    @Test
    void shouldReturnOriginalUrlWhenPathEndsWithSlash() {
        String url = "https://example.com/documents/";
        String result = fileNameExtractor.extract(url);
        assertThat(result).isEqualTo(url);
    }

    @Test
    void shouldReturnOriginalUrlForInvalidInput() {
        String invalidUrl = "not-a-valid-url";
        String result = fileNameExtractor.extract(invalidUrl);
        assertThat(result).isEqualTo(invalidUrl);
    }

    @Test
    void shouldHandleUrlWithQueryParameters() {
        String url = "https://example.com/files/document.pdf?version=1";
        String result = fileNameExtractor.extract(url);
        assertThat(result).isEqualTo("document.pdf");
    }
}