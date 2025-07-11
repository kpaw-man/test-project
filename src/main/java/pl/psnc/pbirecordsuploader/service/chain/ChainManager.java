package pl.psnc.pbirecordsuploader.service.chain;

import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;

import java.util.Objects;


public class ChainManager {
    private Handler firstHandler;
    private Handler lastHandler;

    public static ChainManager createChain(Handler... handlers) {
        ChainManager chainManager = new ChainManager();
        for (Handler handler : handlers) {
            chainManager.addHandler(handler);
        }
        return chainManager;
    }

    public void execute(ChainJobEntity chainJobEntity) throws PbiUploaderException {
        if (Objects.nonNull(firstHandler)) {
            firstHandler.handle(chainJobEntity);
        }
    }

    private void addHandler(Handler handler) {
        if (firstHandler == null) {
            firstHandler = handler;
        } else {
            lastHandler.setNext(handler);
        }
        lastHandler = handler;
    }
}