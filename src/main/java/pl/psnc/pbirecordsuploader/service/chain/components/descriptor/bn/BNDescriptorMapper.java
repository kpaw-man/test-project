package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.BNSuggestionResult;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BNDescriptorMapper {
    private final String bnDescriptorUri;

    public BNDescriptorMapper(@Value("${BN-descriptor-api.descriptor-details-uri}") String bnDescriptorUri) {
        this.bnDescriptorUri = bnDescriptorUri;
    }

    public Optional<Descriptor> mapToDescriptor(BNSuggestionResult result) {
        if (result == null || result.uri() == null || result.label() == null) {
            log.warn("Skipping mapping: missing label or URI in suggestion result: {}", result);
            return Optional.empty();
        }

        return Optional.of(result.uri())
                .flatMap(this::extractIdFromUri)
                .map(id -> new Descriptor(bnDescriptorUri + id, result.label()));
    }

    private Optional<String> extractIdFromUri(String uriString) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uriString), StandardCharsets.UTF_8);
            return params.stream()
                    .filter(p -> "id".equals(p.getName()))
                    .map(NameValuePair::getValue)
                    .findFirst();
        } catch (Exception e) {
            log.error("Failed to parse URI '{}'", uriString, e);
            return Optional.empty();
        }
    }
}