package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonPatternMatcherTest {

    private PersonPatternMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new PersonPatternMatcher();
    }

    @Nested
    @DisplayName("Should return true for valid person names")
    class ValidPersonNames {

        @Test
        void shouldMatchSimpleName() {
            assertTrue(matcher.isPersonName("John Smith"));
        }

        @Test
        void shouldMatchNameWithHyphen() {
            assertTrue(matcher.isPersonName("Mary-Jane Doe"));
        }

        @Test
        void shouldMatchNameWithMiddleName() {
            assertTrue(matcher.isPersonName("John Michael Smith"));
        }

        @Test
        void shouldMatchNameWithComma() {
            assertTrue(matcher.isPersonName("Smith, John"));
        }

        @Test
        void shouldMatchNameWithDates() {
            assertTrue(matcher.isPersonName("John Smith (1980-2020)"));
        }

        @Test
        void shouldMatchNameWithDotAndSuffix() {
            assertTrue(matcher.isPersonName("Smith, John. (researcher)"));
        }

        @Test
        void shouldMatchNameWithDateAndSuffix() {
            assertTrue(matcher.isPersonName("Smith, John (1980-2020). (editor)"));
        }

        @Test
        void shouldMatchUnicodeNames() {
            assertTrue(matcher.isPersonName("Żółć, Jan"));
        }
    }

    @Nested
    @DisplayName("Should return false for invalid person names")
    class InvalidPersonNames {

        @Test
        void shouldNotMatchLowerCaseName() {
            assertFalse(matcher.isPersonName("john smith"));
        }

        @Test
        void shouldNotMatchRandomText() {
            assertFalse(matcher.isPersonName("Dataset 2023. v1"));
        }

        @Test
        void shouldNotMatchUrl() {
            assertFalse(matcher.isPersonName("http://example.com/JohnSmith"));
        }

        @Test
        void shouldNotMatchEmptyString() {
            assertFalse(matcher.isPersonName(""));
        }

        @Test
        void shouldNotMatchNullInput() {
            assertThrows(NullPointerException.class, () -> matcher.isPersonName(null));
        }
    }
}