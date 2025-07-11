package pl.psnc.pbirecordsuploader.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.AuthorizationHeaderException;
import pl.psnc.pbirecordsuploader.utils.AccessTokenManager;
import pl.psnc.pbirecordsuploader.utils.WebClientFilter;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final int MAX_RESPONSE_TIMEOUT_SECONDS = 30;

    private final AccessTokenManager accessTokenManager;
    private final String rohubApiEndpoint;
    private final String storageManagerBaseUrl;
    private final String rocrateValidatorLocation;
    private final String semanticAnalyzerApiUrl;
    private final String semanticAnalyzerResearchareasApiUrl;
    private final String bnDescriptorApiUrl;
    private final String bnDescriptorValidatorUrl;

    public WebClientConfig(AccessTokenManager accessTokenManager,
            @Value("${pbi.rohub.api.endpoint}") String rohubApiEndpoint,
            @Value("${storage-manager.base-url}") String storageManagerBaseUrl,
            @Value("${rocrate.validation.location}") String rocrateValidatorLocation,
            @Value("${semantic-analyzer-api.url}") String semanticAnalyzerApiUrl,
            @Value("${semantic-analyzer-researchareas-api.url}") String semanticAnalyzerResearchareasApiUrl,
            @Value("${BN-descriptor-api.url}") String bnDescriptorApiUrl,
            @Value("${BN-descriptor-api.validator.url}")String bnDescriptorValidatorUrl) {
        this.accessTokenManager = accessTokenManager;
        this.rohubApiEndpoint = validateUrl(rohubApiEndpoint, "Rohub API endpoint");
        this.storageManagerBaseUrl = validateUrl(storageManagerBaseUrl, "Storage Manager base URL");
        this.rocrateValidatorLocation = validateUrl(rocrateValidatorLocation, "RO-Crate Validator location");
        this.semanticAnalyzerApiUrl = semanticAnalyzerApiUrl;
        this.semanticAnalyzerResearchareasApiUrl = semanticAnalyzerResearchareasApiUrl;
        this.bnDescriptorApiUrl = bnDescriptorApiUrl;
        this.bnDescriptorValidatorUrl = bnDescriptorValidatorUrl;
    }

    @Bean
    public WebClient.Builder rohubWebClient() {
        return createBaseWebClient(rohubApiEndpoint)
                .filter((request, next) -> {
                    try {
                        return next.exchange(ClientRequest.from(request)
                                .header(HttpHeaders.AUTHORIZATION, accessTokenManager.getOrCreateBearerToken())
                                .build());
                    } catch (Exception e) {
                        throw new AuthorizationHeaderException("Failed to add authorization header", e);
                    }
                });

    }

    @Bean
    public WebClient.Builder storageManagerWebClient() {
        return createBaseWebClient(storageManagerBaseUrl);
    }

    @Bean
    public WebClient.Builder  validatorApiWebClient() {
        return createBaseWebClient(rocrateValidatorLocation)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    @Bean
    public WebClient.Builder semanticAnalyzerWebClient() {
        return createBaseWebClient(semanticAnalyzerApiUrl);
    }

    @Bean
    public WebClient.Builder semanticAnalyzerResearchareasWebClient() {
        return createBaseWebClient(semanticAnalyzerResearchareasApiUrl);
    }

    @Bean
    public WebClient.Builder bNDescriptorAPIWebClient() {
        return createBaseWebClient(bnDescriptorApiUrl);
    }

    @Bean
    public WebClient.Builder bNDescriptorValidatorWebClient() {
        return createBaseWebClient(bnDescriptorValidatorUrl);
    }

    private WebClient.Builder createBaseWebClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(MAX_RESPONSE_TIMEOUT_SECONDS))))
                .filter(WebClientFilter.logRequest())
                .filter(WebClientFilter.logResponse());
    }

    private String validateUrl(String url, String propertyName) {
        Assert.hasText(url, propertyName + " must not be null or empty.");
        return url;
    }


}