package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.BNSuggestionResult;
import pl.psnc.pbirecordsuploader.model.BNResponse;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.BaseApiClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class BNDescriptorApiClient extends BaseApiClient<BNSuggestionResult> {

    private final String suggestEndpoint;
    private final int resultLimit;

    public BNDescriptorApiClient(@Qualifier("bNDescriptorAPIWebClient") WebClient.Builder builder,
            @Value("${BN-descriptor-api.model}") String languageModel,
            @Value("${BN-descriptor-api.suggest-endpoint}") String suggestPath,
            @Value("${BN-descriptor-api.timeout:${api.timeout}}") long apiTimeout,
            @Value("${BN-descriptor-api.result-limit}") int resultLimit) {
        super(builder.build(), apiTimeout);
        this.suggestEndpoint = languageModel + suggestPath;
        this.resultLimit = resultLimit;
    }

    @Override
    @Cacheable(value = "bnDescriptor", key = "#input", unless = "#result == null")
    public BNSuggestionResult fetch(String input) throws DescriptorApiException {
        if (input == null || input.trim().isEmpty()) {
            log.warn("Empty input provided to BNDescriptorApiClient");
            return null;
        }
        return fetchFromApi(input);
    }

    @Override
    protected BNSuggestionResult fetchFromApi(String input) throws DescriptorApiException {
        try {
            return webClient.post().uri(suggestEndpoint)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(Map.of("text", input, "limit", resultLimit))
                    .retrieve()
                    .bodyToMono(BNResponse.class)
                    .flatMap(this::extractSuggestion)
                    .doOnError(WebClientResponseException.class,
                            e -> log.error("BN API error {} for input '{}': {}",
                                    e.getStatusCode(), input, e.getResponseBodyAsString()))
                    .doOnError(e -> log.error("BN request error for '{}': {}", input, e.getMessage()))
                    .block();
        } catch (Exception e) {
            throw handleApiError(e);
        }
    }

    private Mono<BNSuggestionResult> extractSuggestion(BNResponse response) {
        try {
            BNSuggestionResult suggestionResult = parseSuggestion(response);
            if (suggestionResult == null) {
                return Mono.error(new DescriptorApiException("BNSuggestion cannot be null!"));
            }
            return Mono.just(suggestionResult);
        } catch (DescriptorApiException e) {
            return Mono.error(e);
        }
    }

    private BNSuggestionResult parseSuggestion(BNResponse response) throws DescriptorApiException {
        try {
            BNResponse.AnnifResult annifResult = response.getAnnifResult();
            if (annifResult != null && !annifResult.getResults().isEmpty()) {
                BNResponse.Suggestion suggestion = annifResult.getResults().get(0);
                return new BNSuggestionResult(suggestion.getLabel(), suggestion.getUri());
            }
        } catch (Exception e) {
            log.error("Failed to parse BN suggestion response: {}", response, e);
            throw new DescriptorApiException("Failed to parse BN suggestion response", e);
        }
        return null;
    }
}