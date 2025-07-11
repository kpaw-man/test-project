package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.SemanticAnalyzerResponse;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.BaseApiClient;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class SemanticAnalyzerApiClient extends BaseApiClient<String> {

    public static final String MATCH_ENDPOINT = "/match";

    public SemanticAnalyzerApiClient(
            @Qualifier("semanticAnalyzerWebClient") WebClient.Builder builder,
            @Value("${semantic-analyzer-api.timeout:${api.timeout}}") long apiTimeout) {
        super(builder.build(), apiTimeout);
    }

    @Override
    @Cacheable(value = "semanticAnalyzer", key = "#input", unless = "#result == null")
    public String fetch(String input) throws DescriptorApiException {
        if (input == null || input.trim().isEmpty()) {
            log.warn("Empty input provided to BNDescriptorApiClient");
            return null;
        }
        return fetchFromApi(input);
    }

    @Override
    protected String fetchFromApi(String inputText) throws DescriptorApiException {
        try {
            SemanticAnalyzerResponse response = webClient.post()
                    .uri(MATCH_ENDPOINT)
                    .bodyValue(Map.of("input_text", inputText))
                    .retrieve()
                    .bodyToMono(SemanticAnalyzerResponse.class)
                    .block(Duration.ofSeconds(apiTimeout));

            if (response == null || response.best_match() == null || response.best_match().isBlank()) {
                throw new DescriptorApiException(String.format("No valid match returned for input: '%s'", inputText));
            }
            return response.best_match();
        } catch (Exception e) {
            throw handleApiError(e);
        }
    }
}