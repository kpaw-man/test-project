package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PersonPatternMatcher {
    private final Pattern personPattern;

    public PersonPatternMatcher() {
        this.personPattern = Pattern.compile(
                "^([\\p{Lu}][\\p{L}]+(?:[-\\s][\\p{Lu}][\\p{L}]+){0,2}|[\\p{Lu}][\\p{L}]+,\\s[\\p{Lu}][\\p{L}]+(?:\\s[\\p{Lu}][\\p{L}]+)*)(?:\\s*\\(\\d{4}(?:[./-]\\d{2}(?:[./-]\\d{2})?)?\\s*(?:[-–]\\s*(?:\\d{4}(?:[./-]\\d{2}(?:[./-]\\d{2})?)?)?)?\\))?(?:\\.\\s*)?(?:\\s*\\([^)]+\\))?$"
        );
    }

    public boolean isPersonName(String value) {
        return personPattern.matcher(value).matches();
    }
}