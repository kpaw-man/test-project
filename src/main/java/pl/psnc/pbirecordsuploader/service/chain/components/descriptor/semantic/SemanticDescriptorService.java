package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.BaseDescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorValidatorService;

import java.util.Optional;

@Slf4j
@Service
public class SemanticDescriptorService extends BaseDescriptorService {

    private final KeyDictionary keyDictionary;
    private final SemanticAnalyzerApiClient semanticAnalyzerApiClient;

    public SemanticDescriptorService(KeyDictionary dictionary, SemanticAnalyzerApiClient semanticAnalyzerApiClient,
            DescriptorValidatorService validatorService) {
        super(validatorService);
        this.keyDictionary = dictionary;
        this.semanticAnalyzerApiClient = semanticAnalyzerApiClient;
    }

    @Override
    public Optional<Descriptor> findDescriptor(String value) throws DescriptorException, DescriptorApiException {
        if (value == null) {
            log.warn("Empty input values");
            return Optional.empty();
        }
        String bestMatch = semanticAnalyzerApiClient.fetch(value);
        if (bestMatch == null) {
            throw new DescriptorException(String.format("Best match not found: descriptor '%s' does not exist", value));
        }
        return mapToDescriptor(bestMatch);
    }

    private Optional<Descriptor> mapToDescriptor(String bestMatch) {
        return Optional.ofNullable(keyDictionary.getMappedId(bestMatch))
                .map(mappedId -> new Descriptor(mappedId, bestMatch));
    }
}