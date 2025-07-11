package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import lombok.extern.slf4j.Slf4j;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.util.Optional;

@Slf4j
public abstract class BaseDescriptorService implements DescriptorService {

    private final DescriptorValidatorService validatorService;

    protected BaseDescriptorService(DescriptorValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public final Descriptor applyDescriptor(String value) throws DescriptorException, DescriptorApiException {
        Optional<Descriptor> optionalDescriptor = findDescriptor(value);
        if (optionalDescriptor.isPresent()) {
            try {
                Descriptor descriptor = optionalDescriptor.get();
                validatorService.validateDescriptor(descriptor);
                return descriptor;
            } catch (DescriptorValidatonException e) {
                throw new DescriptorException("Descriptor validation failed: " + e.getMessage(), e);
            }
        }
        throw new DescriptorException("Descriptor not found for value: " + value);
    }

    /**
     * Finds a descriptor based on the provided input values without validation.
     * This method is for internal use by the base class.
     *
     * @param value single input to analyze
     * @return A Descriptor object or empty Optional if no match found
     */
    protected abstract Optional<Descriptor> findDescriptor(String value) throws DescriptorException,
            DescriptorApiException;
}