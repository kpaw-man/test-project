package pl.psnc.pbirecordsuploader.service.chain.components;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.FetchDataException;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;

@Slf4j
@Component
public class FetchDataHandler extends Handler {
    private static final String SCHEMA_ID = "dace";
    private static final String RECORDS_ENDPOINT = "/records";
    private static final String RECORD_BODY_PATH = "$.records[0].recordBody.body";
    private final WebClient.Builder webClientBuilder;

    public FetchDataHandler(@Qualifier("storageManagerWebClient") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        String daceId = chainJobEntity.getDaceId();
        log.debug("Fetching data from remote API for daceId: {}", daceId);

        try {
            String fetchedRecord = fetchRecord(daceId);
            if (fetchedRecord == null || fetchedRecord.isEmpty()) {
                throw new FetchDataException("No data returned during fetch for daceId: " + daceId);
            }

            String extractedBody = extractBody(fetchedRecord);
            chainJobEntity.setDaceBody(extractedBody);

            return handleSuccess(chainJobEntity, ChainJobStatus.FETCHED_FROM_SOURCE);
        } catch (Exception e) {
             handleFailure(chainJobEntity,  e);
        }
        return false;
    }

    private String fetchRecord(String daceId) {
            return webClientBuilder.build().get()
                    .uri(uriBuilder -> uriBuilder.path(RECORDS_ENDPOINT).queryParam("daceId", daceId)
                            .queryParam("schemaId", SCHEMA_ID).build()).retrieve()
                    .onStatus(status ->
                            !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                            .map(body -> new FetchDataException("Failed to fetch record: " + body)))
                    .bodyToMono(String.class)
                    .block();
    }

    private String extractBody(String fetchedRecord) {
            return JsonPath.parse(fetchedRecord).read(RECORD_BODY_PATH, String.class);
    }
}