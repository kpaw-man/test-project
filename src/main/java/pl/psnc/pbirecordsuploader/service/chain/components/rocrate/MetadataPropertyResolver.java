package pl.psnc.pbirecordsuploader.service.chain.components.rocrate;

import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;

import java.util.Optional;
import java.util.stream.Stream;


public final class MetadataPropertyResolver {

    private MetadataPropertyResolver() {
    }

    public static Optional<MetadataProperty> fromKey(String key) {
        return Stream.of(MetadataProperty.fromKey(DCTerms.class, key),
                        MetadataProperty.fromKey(ContentProperties.class, key))
                .flatMap(Optional::stream)
                .map(MetadataProperty.class::cast).findFirst();
    }
}