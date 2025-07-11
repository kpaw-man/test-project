package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Component
public class RelationHandler extends BasePropertyHandler {
    public RelationHandler(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.RELATION);
    }
    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        List<String> filteredValues = values.stream().filter(value -> !hasFileExtension(value)).toList();

        if (!filteredValues.isEmpty()) {
            addArrayProperty(metadataProperty, filteredValues, builder, rootBuilder);
        }
    }

    private boolean hasFileExtension(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return false;
            }
            int lastDot = path.lastIndexOf('.');
            int lastSlash = path.lastIndexOf('/');

            return lastDot > lastSlash && lastDot < path.length() - 1;
        } catch (URISyntaxException ignored) {
            return false;
        }
    }
}