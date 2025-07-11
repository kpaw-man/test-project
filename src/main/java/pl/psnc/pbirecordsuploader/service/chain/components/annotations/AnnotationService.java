package pl.psnc.pbirecordsuploader.service.chain.components.annotations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Slf4j
@Service
public class AnnotationService {

    private final WebClient webClient;
    private final String annotationUriTemplate;

    public AnnotationService(@Qualifier("rohubWebClient") WebClient.Builder builder,
            @Value("${rohub.api.annotation-endpoint:ros/{rosId}/annotations/}") String annotationUriTemplate) {
        this.webClient = builder.build();
        this.annotationUriTemplate = annotationUriTemplate;
    }

    public void postAnnotation(String rosId, String value, String propertyUri) throws PbiUploaderException {
        var payload = new AnnotationPayload(List.of(new AnnotationSpec(value, propertyUri)), rosId);

        try {
            webClient.post()
                    .uri(annotationUriTemplate, rosId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(); 

            log.info("Posted annotation: [rosId: {}, property: {}, value: {}]", rosId, propertyUri, value);
        } catch (Exception e) {
            log.error("Failed to post annotation: [rosId: {}, property: {}, value: {}], error: {}",
                    rosId, propertyUri, value, e.getMessage(), e);
            throw new PbiUploaderException("Failed to post annotation", e);
        }
    }

    private record AnnotationSpec(String value, String property) {}
    private record AnnotationPayload(List<AnnotationSpec> body_specification_json, String ro) {}
}