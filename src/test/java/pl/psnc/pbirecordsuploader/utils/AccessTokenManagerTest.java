package pl.psnc.pbirecordsuploader.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.psnc.pbirecordsuploader.exceptions.AccessTokenException;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccessTokenManagerTest {

    @InjectMocks
    private AccessTokenManager accessTokenManager;

    @Mock
    private WebClient keycloakClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private JwtDecoder jwtDecoder;


    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(accessTokenManager, "keycloakServerUrl", "http://keycloak.example.com");
        ReflectionTestUtils.setField(accessTokenManager, "realm", "test-realm");
        ReflectionTestUtils.setField(accessTokenManager, "clientId", "test-client");
        ReflectionTestUtils.setField(accessTokenManager, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(accessTokenManager, "keycloakClient", keycloakClient);
        ReflectionTestUtils.setField(accessTokenManager, "jwtDecoder", jwtDecoder);


    }

    @Test
    void testGetOrCreateBearerToken_NewToken() throws Exception {
        String mockToken = "mockedAccessToken";
        Instant expiry = Instant.now().plusSeconds(60);

        AccessTokenManager.TokenResponse mockResponse = new AccessTokenManager.TokenResponse();
        mockResponse.setAccessToken(mockToken);
        mockResponse.setExpiresIn(60);
        mockResponse.setTokenType("bearer");

        when(responseSpec.bodyToMono(AccessTokenManager.TokenResponse.class)).thenReturn(Mono.just(mockResponse));
        when(keycloakClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Jwt mockJwt = mock(Jwt.class);
        when(jwtDecoder.decode(mockToken)).thenReturn(mockJwt);
        when(mockJwt.getClaim("exp")).thenReturn(expiry);

        String bearerToken = accessTokenManager.getOrCreateBearerToken();

        assertTrue(bearerToken.startsWith("Bearer "));
        assertEquals("Bearer " + mockResponse.getAccessToken(), bearerToken);

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodyUriSpec).uri(uriCaptor.capture(), eq("test-realm"));
        assertEquals("/realms/{realm}/protocol/openid-connect/token", uriCaptor.getValue());

        ArgumentCaptor<BodyInserters.FormInserter<String>> formCaptor = ArgumentCaptor.forClass(
                BodyInserters.FormInserter.class);
        verify(requestBodySpec).body(formCaptor.capture());
    }


    @Test
    void testReuseValidToken() throws Exception {
        String validToken = "validJwtToken";
        long futureExpirationTime = System.currentTimeMillis() + 120_000; // Token expires in 2 minutes

        ReflectionTestUtils.setField(accessTokenManager, "accessToken", validToken);
        ReflectionTestUtils.setField(accessTokenManager, "expirationTime", futureExpirationTime);

        Jwt mockJwt = mock(Jwt.class);
        when(jwtDecoder.decode(validToken)).thenReturn(mockJwt);

        String bearerToken = accessTokenManager.getOrCreateBearerToken();

        assertEquals("Bearer " + validToken, bearerToken);
        verify(keycloakClient, never()).post();
    }

    @Test
    void testGetOrCreateBearerToken_HandleWebClientResponseException() {
        WebClientResponseException mockException = mock(WebClientResponseException.class);
        when(mockException.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.UNAUTHORIZED);
        when(mockException.getResponseBodyAsString()).thenReturn("Unauthorized");
        when(mockException.getMessage()).thenReturn("401 Unauthorized");

        when(keycloakClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AccessTokenManager.TokenResponse.class)).thenReturn(Mono.error(mockException));

        AccessTokenException exception = assertThrows(AccessTokenException.class,
                () -> accessTokenManager.getOrCreateBearerToken());

        assertTrue(exception.getMessage().contains("Failed to fetch Keycloak token"));
        assertEquals(mockException, exception.getCause());
    }
}