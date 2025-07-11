package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class ContentTypeExtractor {
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;

    public ContentTypeExtractor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<String> extract(String url) {
        try {
            HttpRequest headRequest = createHeadRequest(url);
            HttpResponse<Void> response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());

            if (isSuccessfulResponse(response.statusCode())) {
                return response.headers()
                        .firstValue("Content-Type")
                        .map(this::cleanContentType);
            } else {
                log.warn("Non-successful response for '{}': {}", url, response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while fetching content type for '{}'", url, e);
        } catch (IOException | IllegalArgumentException e) {
            log.error("Failed to fetch content type for '{}'", url, e);
        }
        return Optional.empty();
    }

    private HttpRequest createHeadRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(HTTP_TIMEOUT)
                .build();
    }

    private boolean isSuccessfulResponse(int code) {
        return code >= 200 && code < 300;
    }

    private String cleanContentType(String type) {
        return type.split(";")[0].trim();
    }
}