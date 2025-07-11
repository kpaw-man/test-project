package pl.psnc.pbirecordsuploader.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.psnc.pbirecordsuploader.exceptions.AccessTokenException;

import java.time.Instant;

@Service
@Slf4j
public class AccessTokenManager {
    private static final String TOKEN_PATH = "/realms/{realm}/protocol/openid-connect/token";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final int TOKEN_EXPIRY_BUFFER_MS = 10_000;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private String accessToken;
    private long expirationTime;
    private WebClient keycloakClient;
    private JwtDecoder jwtDecoder;

    @PostConstruct
    public void init() {
        keycloakClient = WebClient.builder().baseUrl(keycloakServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();

        String issuerUri = keycloakServerUrl + "/realms/" + realm;
        this.jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
    }

    /**
     * Get a valid bearer token or create a new one if the current token is expired
     *
     * @return A valid bearer token with "Bearer " prefix
     * @throws AccessTokenException if token cannot be retrieved or validated
     */
    public String getOrCreateBearerToken() throws AccessTokenException {
        if (isTokenExpired()) {
            fetchNewToken();
        } else if (accessToken != null) {
            validateJwtSignature(accessToken);
        }
        return "Bearer " + accessToken;
    }

    private boolean isTokenExpired() {
        return accessToken == null || System.currentTimeMillis() + TOKEN_EXPIRY_BUFFER_MS > expirationTime;
    }

    private void fetchNewToken() throws AccessTokenException {
        log.debug("Fetching new Keycloak access token");

        try {
            TokenResponse tokenResponse = keycloakClient.post().uri(TOKEN_PATH, realm)
                    .body(BodyInserters.fromFormData(createClientCredentialsRequestBody())).retrieve()
                    .bodyToMono(TokenResponse.class).block();
            if (tokenResponse == null) {
                throw new AccessTokenException("Failed to get token: Keycloak returned null response");
            }

            this.accessToken = tokenResponse.getAccessToken();
            setExpirationTimeFromToken();
            log.debug("Token successfully fetched, expires at: {}", Instant.ofEpochMilli(expirationTime).toString());
        } catch (WebClientResponseException e) {
            String message = String.format("HTTP %s - %s", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("Error fetching access token: {}", message, e);
            throw new AccessTokenException("Failed to fetch Keycloak token: " + message, e);
        } catch (Exception e) {
            if (e instanceof AccessTokenException accessTokenException) {
                throw accessTokenException;
            }
            log.error("Unexpected error fetching access token", e);
            throw new AccessTokenException("Unexpected error while fetching Keycloak token", e);
        }
    }

    private void setExpirationTimeFromToken() throws AccessTokenException {
        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            Object expValue = jwt.getClaim("exp");
            if (expValue == null) {
                throw new AccessTokenException("Token is missing expiration claim");
            }
            this.expirationTime = ((Instant) expValue).toEpochMilli();
        } catch (JwtException e) {
            throw new AccessTokenException("Invalid JWT: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, String> createClientCredentialsRequestBody() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        return formData;
    }

    private void validateJwtSignature(String jwtToken) throws AccessTokenException {
        try {
            jwtDecoder.decode(jwtToken);
            log.trace("JWT signature validated successfully.");
        } catch (JwtException e) {
            throw new AccessTokenException("Invalid JWT signature: " + e.getMessage(), e);
        }
    }

    @Data
    static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private int expiresIn;
    }
}