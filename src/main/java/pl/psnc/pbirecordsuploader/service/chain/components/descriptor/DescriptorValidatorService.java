package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.Descriptor;

@Component
public class DescriptorValidatorService {

    private final WebClient webClient;

    public DescriptorValidatorService(@Qualifier("bNDescriptorValidatorWebClient") WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public void validateDescriptor(Descriptor descriptor) throws DescriptorValidatonException {
        String id = extractIdFromDescriptorUrl(descriptor.getId());
        if (!checkResourceExists(id)) {
            throw new DescriptorValidatonException("Descriptor of id " + id + " does not exist");
        }
    }

    public String extractIdFromDescriptorUrl(String url) throws DescriptorValidatonException {
        if (url == null || !url.contains("/")) {
            throw new DescriptorValidatonException("Invalid descriptor URL: " + url);
        }
        String id = url.substring(url.lastIndexOf('/') + 1);
        if (id.isEmpty()) {
            throw new DescriptorValidatonException("Empty ID extracted from URL: " + url);
        }
        return id;
    }

    private boolean checkResourceExists(String id) throws DescriptorValidatonException {
        try {
            webClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new DescriptorValidatonException("Validation failed for ID: " + id, e);
        }
    }
}