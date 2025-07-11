package pl.psnc.pbirecordsuploader.service.chain.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.domain.RoFailedValidationResponse;
import pl.psnc.pbirecordsuploader.exceptions.HTTPException;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.ValidationException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatusCode;

@Slf4j
@Component
public class ValidateResearchObjectHandler extends Handler {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String severity;

    public ValidateResearchObjectHandler(@Qualifier("validatorApiWebClient")WebClient.Builder webClientBuilder,
            @Value("${rocrate.validation.severity}") String severity,ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.severity = severity;
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        var daceId = chainJobEntity.getDaceId();
        log.debug("Starting validation for DACE ID: {}", daceId);

        try {
            var response = requestValidation(chainJobEntity);
            if (isValidationSuccessful(response)) {
                return handleSuccess(chainJobEntity, ChainJobStatus.VALIDATED_RO);
            }

            var failedValidationResult = objectMapper.writeValueAsString((parseResponse(response)));
            handleFailure(chainJobEntity, "Validation failed: " + failedValidationResult);
        } catch (Exception e) {
            handleFailure(chainJobEntity, "Error during validation: " + e.getMessage());
        }
        return false;
    }

    private boolean isValidationSuccessful(String response) {
        return JsonPath.read(response, "$.success");
    }

    private RoFailedValidationResponse.ValidationResponseData parseResponse(String response) throws
            ValidationException {
        try {
            List<Object> rawIssues = JsonPath.read(response, "$.issues[*]");
            List<RoFailedValidationResponse.Issue> issues = new ArrayList<>();
            for (Object rawIssue : rawIssues) {
                if (rawIssue != null) {
                    issues.add(convertToIssue(rawIssue));
                }
            }
            return new RoFailedValidationResponse.ValidationResponseData(false, issues);
        } catch (Exception e) {
            throw new ValidationException("Failed to parse validation response.", e);
        }
    }
    private RoFailedValidationResponse.Issue convertToIssue(Object rawIssue) throws ValidationException {
        try {
            return objectMapper.convertValue(rawIssue, RoFailedValidationResponse.Issue.class);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Error converting validation issue to the expected format", e);
        }
    }

    private String requestValidation(ChainJobEntity chainJobEntity) throws ValidationException {
        var roLocalBytes = chainJobEntity.getRoCrate();
        var daceId = chainJobEntity.getDaceId();

        validateRoCrate(roLocalBytes, daceId);

        var formData = createMultipartData(daceId, roLocalBytes);
        return sendValidationRequest(formData);
    }

    private void validateRoCrate(byte[] roCrate, String daceId) throws ValidationException {
        if (roCrate == null || roCrate.length == 0) {
            var message = "RO-Crate byte array is missing or empty for DACE ID: " + daceId;
            throw new ValidationException(message);
        }
    }

    private MultiValueMap<String, Object> createMultipartData(String daceId, byte[] roCrate) {
        Resource roCrateResource = new ByteArrayResource(roCrate) {
            @Override
            public String getFilename() {
                return "ro-crate.zip";
            }
        };

        var formData = new LinkedMultiValueMap<String, Object>();
        formData.add("dace_id", daceId);
        formData.add("file", roCrateResource);

        if (severity != null) {
            formData.add("severity", severity);
        }

        return formData;
    }

    private String sendValidationRequest(MultiValueMap<String, Object> formData) {
        return Objects.requireNonNull(webClient.post()
                        .uri("/validate")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(formData))
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new HTTPException(response.statusCode().value(), "Client error: " + body))))
                        .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new HTTPException(response.statusCode().value(), "Server error: " + body))))
                        .toEntity(String.class)
                        .block())
                .getBody();
    }

}