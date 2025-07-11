package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;


@Slf4j
public abstract class BaseApiClient<T> {
    protected final WebClient webClient;
    protected final long apiTimeout;

    protected BaseApiClient(WebClient webClient, long apiTimeout) {
        this.webClient = webClient;
        this.apiTimeout = apiTimeout;
    }

    /**
     * Fetches result from cache or API - now uses Spring Cache annotations
     * This method should be overridden in concrete classes to add @Cacheable
     */
    public T fetch(String input) throws DescriptorApiException {
        if (input == null || input.trim().isEmpty()) {
            log.warn("Empty input provided to API client");
            return null;
        }

        return fetchFromApi(input);
    }

    protected DescriptorApiException handleApiError(Exception e) {
        return new DescriptorApiException("API request failed", e);
    }

    protected abstract T fetchFromApi(String input) throws DescriptorApiException;
}