package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn.BNDescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic.SemanticDescriptorService;

@Component
@RequiredArgsConstructor
public class DescriptorServiceFactory {
    private final BNDescriptorService bnDescriptorService;
    private final SemanticDescriptorService semanticDescriptorService;

    /**
     * Get a descriptor service based on the source type.
     *
     * @param sourceType The source type
     * @return The appropriate descriptor service
     */
    public DescriptorService getDescriptorService(DescriptorSourceType sourceType) {
        return switch (sourceType) {
            case BN -> bnDescriptorService;
            case SEMANTIC -> semanticDescriptorService;
        };
    }

    /**
     * Descriptor source types.
     */
    public enum DescriptorSourceType {
        BN,
        SEMANTIC
    }
}