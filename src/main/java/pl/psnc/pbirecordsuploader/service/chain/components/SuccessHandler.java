package pl.psnc.pbirecordsuploader.service.chain.components;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.service.chain.Handler;

@Slf4j
@Component
public class SuccessHandler extends Handler {
    @Override
    protected boolean process(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        try {
            log.debug("Handling final stage bf process...");
            return handleSuccess(chainJobEntity, ChainJobStatus.DONE);

        } catch (Exception e) {
             handleFailure(chainJobEntity,e.getMessage());
        }
        return false;
    }
}