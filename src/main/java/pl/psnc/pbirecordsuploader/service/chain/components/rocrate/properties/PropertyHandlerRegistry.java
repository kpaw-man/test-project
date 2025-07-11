package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties;

import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;

import java.util.*;

@Component
public class PropertyHandlerRegistry {
    private final Map<MetadataProperty, PropertyHandler> handlers = new HashMap<>();

    public PropertyHandlerRegistry(List<PropertyHandler> handlerBeans) {
        handlerBeans.forEach(this::registerHandler);
    }

    public Optional<PropertyHandler> getHandler(MetadataProperty metadataProperty) {
        return Optional.ofNullable(handlers.get(metadataProperty));
    }

    private void registerHandler(PropertyHandler handler) {
        for (MetadataProperty term : handler.getSupportedTerms()) {
            handlers.put(term, handler);
        }
    }
}