package pl.psnc.pbirecordsuploader.service.chain;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import java.util.Objects;


@Setter
@Slf4j
public abstract class Handler {
    private Handler next;

    public void handle(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        if (process(chainJobEntity) && Objects.nonNull(next)) {
            next.handle(chainJobEntity);
        }
    }

    protected boolean handleSuccess(ChainJobEntity chainJobEntity, ChainJobStatus chainJobStatus) {
        chainJobEntity.setChainJobStatus(chainJobStatus);
        if (ChainJobStatus.DONE.equals(chainJobStatus)) {
            log.info("Processing completed for daceID {}", chainJobEntity.getDaceId());
        }
        return true;
    }

    protected void handleFailure(ChainJobEntity chainJobEntity, String errorMessage, Exception exception) throws
            PbiUploaderException {
        String errorDetails = (Objects.nonNull(exception)) ? String.valueOf(exception.getCause()) : "No details available";
        log.error("{}: {}. Processing terminated for daceId {}. Latest completed stage: {}",
                errorMessage, errorDetails, chainJobEntity.getDaceId(), chainJobEntity.getChainJobStatus());
        chainJobEntity.setChainJobStatus(ChainJobStatus.FAILED);
        if(exception == null)
            throw new PbiUploaderException("Upload terminated, for daceID " + chainJobEntity.getDaceId() + " " + errorMessage);

        throw new PbiUploaderException("Upload terminated, for daceID " + chainJobEntity.getDaceId(), exception);
    }


    protected void handleFailure(ChainJobEntity chainJobEntity, String errorMessage) throws PbiUploaderException {
         handleFailure(chainJobEntity, errorMessage, null);
    }

    protected void handleFailure(ChainJobEntity chainJobEntity, Exception e) throws PbiUploaderException {
        handleFailure(chainJobEntity, e.getMessage(), e);
    }

    protected abstract boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException  ;
}