package pl.psnc.pbirecordsuploader.service.chain.components.researcharea;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CachedResearchAreaSanitizer {

    private final ResearchAreaSanitizerApi delegate;

    @Cacheable(value = "researchAreaSanitizer", key = "#input", unless = "#result == null")
    public String sanitize(String input) throws DescriptorApiException {
        if (input == null || input.isBlank()) {
            log.debug("Input is null or blank, returning null");
            return null;
        }
        try {
            return delegate.fetch(input);
        } catch (DescriptorApiException e) {
            log.warn("Failed to sanitize input '{}': {}", input, e.getMessage());
            throw e;
        }
    }
}