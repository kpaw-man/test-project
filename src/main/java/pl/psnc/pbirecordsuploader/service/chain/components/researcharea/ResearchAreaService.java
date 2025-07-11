package pl.psnc.pbirecordsuploader.service.chain.components.researcharea;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.model.researcharea.ResearchArea;
import pl.psnc.pbirecordsuploader.model.researcharea.ResearchAreaResponse;
import pl.psnc.pbirecordsuploader.model.researcharea.ResearchAreaResult;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ResearchAreaService {

    private static final String RESEARCH_AREAS_PATH = "/research-areas/";
    private static final String ACCEPT_HEADER = "application/json";
    private static final String SEARCH_PARAM = "search";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;

    public ResearchAreaService(@Qualifier("rohubWebClient") WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public ResearchAreaResult getFirstTermValue(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            log.debug("Search term is null or blank, returning null");
            return null;
        }

        return getTermValues(searchTerm).stream()
                .findFirst()
                .map(firstTerm -> createResearchAreaResult(searchTerm, firstTerm))
                .orElse(null);
    }

    private ResearchAreaResult createResearchAreaResult(String searchTerm, String firstTerm) {
        log.debug("First term value for '{}': {}", searchTerm, firstTerm);
        return new ResearchAreaResult(searchTerm, firstTerm);
    }

    private List<String> getTermValues(String searchTerm) {
        ResearchAreaResponse response = searchResearchAreas(searchTerm);
        if (response == null) {
            log.debug("No response for term '{}'", searchTerm);
            return Collections.emptyList();
        }
        List<String> terms = extractTermsFromResponse(response);
        if (terms.isEmpty()) {
            log.debug("Empty result for term '{}'", searchTerm);
        }
        return terms;
    }

    private ResearchAreaResponse searchResearchAreas(String searchTerm) {
        log.debug("Searching research areas with term: {}", searchTerm);
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(RESEARCH_AREAS_PATH)
                            .queryParam(SEARCH_PARAM, searchTerm)
                            .build())
                    .header("Accept", ACCEPT_HEADER)
                    .retrieve()
                    .bodyToMono(ResearchAreaResponse.class)
                    .timeout(DEFAULT_TIMEOUT)
                    .block();
        } catch (Exception e) {
            log.error("Error searching research areas with term '{}': {}",
                    searchTerm, e.getMessage(), e);
            return null;
        }
    }

    private List<String> extractTermsFromResponse(ResearchAreaResponse response) {
        return Optional.ofNullable(response)
                .map(ResearchAreaResponse::getResults)
                .orElse(Collections.emptyList())
                .stream()
                .map(ResearchArea::getTerm)
                .filter(this::isValidTerm)
                .toList();
    }

    private boolean isValidTerm(String term) {
        return term != null && !term.isBlank();
    }
}