package pl.psnc.pbirecordsuploader.service.chain.components.researcharea;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.SemanticAnalyzerResponse;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.BaseApiClient;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class ResearchAreaSanitizerApi extends BaseApiClient<String> {

    private static final String MATCH_ENDPOINT = "/match";
    private static final String INPUT_TEXT_PARAM = "input_text";

    public ResearchAreaSanitizerApi(
            @Qualifier("semanticAnalyzerResearchareasWebClient") WebClient.Builder builder,
            @Value("${semantic-analyzer-researchareas-api.timeout:${api.timeout}}") long apiTimeout) {
        super(builder.build(), apiTimeout);
    }

    @Override
    protected String fetchFromApi(String inputText) throws DescriptorApiException {
        try {
            SemanticAnalyzerResponse response = callSemanticAnalyzerApi(inputText);
            return extractBestMatch(response, inputText);
        } catch (DescriptorApiException e) {
            throw e;
        } catch (Exception e) {
            throw handleApiError(e);
        }
    }

    private SemanticAnalyzerResponse callSemanticAnalyzerApi(String inputText) {
        return webClient.post()
                .uri(MATCH_ENDPOINT)
                .bodyValue(createRequestBody(inputText))
                .retrieve()
                .bodyToMono(SemanticAnalyzerResponse.class)
                .block(Duration.ofSeconds(apiTimeout));
    }

    private Map<String, Object> createRequestBody(String inputText) {
        return Map.of(INPUT_TEXT_PARAM, inputText);
    }

    private String extractBestMatch(SemanticAnalyzerResponse response, String inputText)
            throws DescriptorApiException {
        if (response == null) {
            throw new DescriptorApiException(
                    String.format("No response received for input: '%s'", inputText));
        }
        String bestMatch = response.best_match();
        if (bestMatch == null || bestMatch.isBlank()) {
            throw new DescriptorApiException(
                    String.format("No valid match returned for input: '%s'", inputText));
        }
        return bestMatch;
    }
}