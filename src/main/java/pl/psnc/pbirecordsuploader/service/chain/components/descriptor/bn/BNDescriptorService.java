package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.BaseDescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorValidatorService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BNDescriptorService extends BaseDescriptorService {
    private final BNDescriptorApiClient apiClient;
    private final BNDescriptorMapper mapper;

    public BNDescriptorService(BNDescriptorApiClient apiClient, BNDescriptorMapper mapper,
            DescriptorValidatorService validatorService) {
        super(validatorService);
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    @Override
    public Optional<Descriptor> findDescriptor(String value) throws DescriptorApiException {
        if (value == null ) {
            return Optional.empty();
        }
        return Optional.ofNullable(apiClient.fetch(value)).flatMap(mapper::mapToDescriptor);
    }
}