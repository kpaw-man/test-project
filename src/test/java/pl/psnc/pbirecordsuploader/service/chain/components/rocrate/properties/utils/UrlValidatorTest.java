package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UrlValidatorTest {

    private UrlValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new UrlValidator();
    }

    @Test
    @DisplayName("Should return true for valid HTTP URL")
    void shouldReturnTrueForValidHttpUrl() {
        String url = "http://example.com/path/file.pdf";
        boolean result = urlValidator.isValid(url);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for valid HTTPS URL")
    void shouldReturnTrueForValidHttpsUrl() {
        String url = "https://secure.example.com/api/data";
        boolean result = urlValidator.isValid(url);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for URL without scheme")
    void shouldReturnFalseForUrlWithoutScheme() {
        String url = "example.com/path/file.pdf";
        boolean result = urlValidator.isValid(url);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for non-HTTP schemes")
    void shouldReturnFalseForNonHttpSchemes() {
        String url = "ftp://example.com/file.txt";
        boolean result = urlValidator.isValid(url);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for invalid URL format")
    void shouldReturnFalseForInvalidUrlFormat() {
        String url = "not-a-valid-url";
        boolean result = urlValidator.isValid(url);
        assertThat(result).isFalse();
    }
}