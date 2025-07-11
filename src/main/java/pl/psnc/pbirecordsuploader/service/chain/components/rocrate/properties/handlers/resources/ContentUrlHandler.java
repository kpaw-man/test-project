package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.ContentTypeExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.UrlValidator;

import java.util.List;
import java.util.Set;

@Component
public class ContentUrlHandler extends ResourcesPropertyHandler {

    protected ContentUrlHandler(ObjectMapper objectMapper,
            UrlValidator urlValidator,
            ContentTypeExtractor contentTypeExtractor) {
        super(objectMapper, urlValidator, contentTypeExtractor);
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(ContentProperties.CONTENT_URL);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        handleResources(values, builder, context);
    }
}