package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.util.List;

/**
 * Common interface for descriptor services that can find and apply a descriptor
 * based on input values.
 */
public interface DescriptorService {
    Descriptor applyDescriptor(String value) throws DescriptorException, DescriptorApiException;
}