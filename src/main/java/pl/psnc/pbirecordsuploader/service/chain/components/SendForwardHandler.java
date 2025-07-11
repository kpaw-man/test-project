package pl.psnc.pbirecordsuploader.service.chain.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.domain.RocrateUploadStatus;
import pl.psnc.pbirecordsuploader.exceptions.HTTPException;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.SendForwardException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SendForwardHandler extends Handler {

    private static final String UPLOAD_URI = "ros/upload/";
    private static final String FILE_NAME = "ro_crate.zip";
    private static final String CONTENT_TYPE = "application/x-zip-compressed";
    private static final String CRLF = "\r\n";

    // Configuration for polling
    private static final Duration DEFAULT_POLLING_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(2);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    // Make these configurable via properties
    @Value("${rohub.polling.timeout:300}") // 5 minutes in seconds
    private long pollingTimeoutSeconds;

    @Value("${rohub.polling.interval:2}") // 2 seconds
    private long pollingIntervalSeconds;

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("api/ros/([^/\\s]+)");

    public SendForwardHandler(ObjectMapper objectMapper, @Qualifier("rohubWebClient") WebClient.Builder webClientBuilder) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.build();
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        log.debug("Starting to process ChainJobEntity with ID: {}", chainJobEntity.getDaceId());

        try {
            uploadFileToRoHub(chainJobEntity);
            boolean pollSuccess = pollJobStatus(chainJobEntity.getRocrateUploadStatus());

            if (!pollSuccess) {
                throw new SendForwardException("Job polling failed or timed out for DACE ID: " + chainJobEntity.getDaceId());
            }

            return handleSuccess(chainJobEntity, ChainJobStatus.FORWARDED_RO);
        } catch (Exception e) {
            handleFailure(chainJobEntity, e);
            return false;
        }
    }

    private boolean pollJobStatus(RocrateUploadStatus status) {
        Duration timeout = Duration.ofSeconds(pollingTimeoutSeconds > 0 ? pollingTimeoutSeconds : DEFAULT_POLLING_TIMEOUT.getSeconds());
        Duration interval = Duration.ofSeconds(pollingIntervalSeconds > 0 ? pollingIntervalSeconds : DEFAULT_POLLING_INTERVAL.getSeconds());

        log.debug("Starting to poll job {} with timeout of {} seconds", status.getJobIdentifier(), timeout.getSeconds());

        try {
            Boolean result = createPollingMono(status, interval)
                    .timeout(timeout)
                    .switchIfEmpty(Mono.just(false)) // Default to false if no terminal result
                    .block();

            return result != null && result;
        } catch (Exception e) {
            log.error("Polling failed for job {}: {}", status.getJobIdentifier(), e.getMessage());
            return false;
        }
    }

    private Mono<Boolean> createPollingMono(RocrateUploadStatus status, Duration interval) {
        return Flux.interval(Duration.ZERO, interval)
                .flatMap(tick -> pollJobStatusOnce(status))
                .filter(Optional::isPresent)        // Only proceed with terminal results
                .map(Optional::get)                 // Extract the Boolean value
                .next();                           // Take the first terminal result
    }

    private Mono<Optional<Boolean>> pollJobStatusOnce(RocrateUploadStatus status) {
        return webClient.get()
                .uri("/jobs/" + status.getJobIdentifier() + "/")
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> processJobResponse(status, response))
                .onErrorResume(error -> {
                    log.warn("Failed to poll job status for {}: {}", status.getJobIdentifier(), error.getMessage());
                    return Mono.just(Optional.empty()); // Return empty to continue polling
                })
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(retrySignal ->
                                log.debug("Retrying poll for job {} (attempt {})",
                                        status.getJobIdentifier(), retrySignal.totalRetries() + 1)));
    }

    private Optional<Boolean> processJobResponse(RocrateUploadStatus status, String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String currentStatus = jsonNode.get("status").asText();
            int progress = jsonNode.get("progress").asInt();

            status.setJobStatus(currentStatus);
            status.setJobProgress(progress + "%");

            log.debug("Polled job {}: status={}, progress={}%", status.getJobIdentifier(), currentStatus, progress);

            if (isTerminalState(currentStatus)) {
                log.info("Job {} completed with status: {}", status.getJobIdentifier(), currentStatus);
                status.setRosId(rosIdentifierExtractor(jsonNode.get("results").asText()));
                return Optional.of("SUCCESS".equalsIgnoreCase(currentStatus));
            }

            return Optional.empty(); // Continue polling
        } catch (Exception e) {
            log.error("Failed to process job response for {}: {}", status.getJobIdentifier(), e.getMessage());
            throw new RuntimeException("Failed to process job response", e);
        }
    }

    private String rosIdentifierExtractor(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Matcher matcher = IDENTIFIER_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isTerminalState(String status) {
        return "SUCCESS".equalsIgnoreCase(status) ||
                "FAILURE".equalsIgnoreCase(status) ||
                "CANCELLED".equalsIgnoreCase(status);
    }

    private void uploadFileToRoHub(ChainJobEntity chainJobEntity) throws IOException, SendForwardException {
        var daceId = chainJobEntity.getDaceId();
        var fileContent = chainJobEntity.getRoCrate();

        validateFileContent(daceId, fileContent);

        var boundary = generateBoundary();
        var multipartBody = createMultipartBody(boundary, fileContent);

        String rohubJobId = sendMultipartRequest(daceId, boundary, multipartBody);
        chainJobEntity.setRocrateUploadStatus(RocrateUploadStatus.of(rohubJobId));
    }

    private void validateFileContent(String daceId, byte[] fileContent) throws SendForwardException {
        if (fileContent == null || fileContent.length == 0) {
            var errorMessage = "RO-Crate byte array is missing or empty for DACE ID: " + daceId;
            throw new SendForwardException(errorMessage);
        }
    }

    private String generateBoundary() {
        return "--------------------------" + System.currentTimeMillis();
    }

    private byte[] createMultipartBody(String boundary, byte[] fileContent) throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            var header = String.format(
                    "--%s%sContent-Disposition: form-data; name=\"file\"; filename=\"%s\"%sContent-Type: %s%s%s",
                    boundary, CRLF, FILE_NAME, CRLF, CONTENT_TYPE, CRLF, CRLF).getBytes(StandardCharsets.UTF_8);

            var footer = String.format("%s--%s--%s", CRLF, boundary, CRLF).getBytes(StandardCharsets.UTF_8);

            outputStream.write(header);
            outputStream.write(fileContent);
            outputStream.write(footer);

            return outputStream.toByteArray();
        }
    }

    private String sendMultipartRequest(String daceId, String boundary, byte[] multipartBody) {
        try {
            String response = webClient.post()
                    .uri(UPLOAD_URI)
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .bodyValue(multipartBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("(No response body)")
                                    .flatMap(body -> {
                                        log.error("Client error {} while uploading for DACE ID {}. Response: {}",
                                                clientResponse.statusCode().value(), daceId, body);
                                        return Mono.error(new HTTPException(clientResponse.statusCode().value()));
                                    }))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("(No response body)")
                                    .flatMap(body -> {
                                        log.error("Server error {} while uploading for DACE ID {}. Response: {}",
                                                clientResponse.statusCode().value(), daceId, body);
                                        return Mono.error(new HTTPException(clientResponse.statusCode().value()));
                                    }))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30)) // Upload timeout
                    .block();

            log.debug("Upload response for DACE ID {}: {}", daceId, response);
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("identifier").asText();
        } catch (Exception e) {
            log.error("Failed to upload for DACE ID {}: {}", daceId, e.getMessage(), e);
            throw new IllegalStateException("Upload failed for daceId:" + daceId + ", cause " + e);
        }
    }
}