package pl.psnc.pbirecordsuploader.service.chain.components.researcharea;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.researcharea.ResearchAreaResult;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResearchAreasExtractor {

    private final ResearchAreaService researchAreaService;
    private final CachedResearchAreaSanitizer cachedSanitizer;
    private static final String SUBJECT_FIELD = "subject";

    public List<ResearchAreaResult> fetchDomains(List<String> elements) {
        if (elements == null || elements.isEmpty()) {
            log.trace("Domain extractor omitted, field '{}' is not present or empty", SUBJECT_FIELD);
            return Collections.emptyList();
        }
        return elements.stream()
                .filter(Objects::nonNull)
                .filter(element -> !element.isBlank())
                .map(this::sanitizeElement)
                .filter(Objects::nonNull)
                .map(researchAreaService::getFirstTermValue)
                .filter(Objects::nonNull)
                .toList();
    }

    private String sanitizeElement(String input) {
        try {
            return cachedSanitizer.sanitize(input);
        } catch (DescriptorApiException e) {
            log.warn("Failed to sanitize '{}': {}", input, e.getMessage());
            return null;
        }
    }
}