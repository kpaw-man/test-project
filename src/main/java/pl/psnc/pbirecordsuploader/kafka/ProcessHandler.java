package pl.psnc.pbirecordsuploader.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.domain.UploadRequest;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.service.UploaderService;

import static pl.psnc.pbirecordsuploader.configuration.PbiRecordsUploaderAppConfig.KAFKA_LISTENER_CONTAINER_ID;


@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessHandler {
    private final UploaderService uploaderService;

    @KafkaListener(id = KAFKA_LISTENER_CONTAINER_ID, topics = "#{pbiRecordsUploaderAppConfig.listeningTopic}",
            groupId = "#{pbiRecordsUploaderAppConfig.groupId}")
    public void consume(@Payload UploadRequest uploadRequest) throws PbiUploaderException {
            uploaderService.perform(uploadRequest);
    }
}