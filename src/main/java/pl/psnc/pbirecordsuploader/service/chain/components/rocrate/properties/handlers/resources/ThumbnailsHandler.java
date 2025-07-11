package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.ContentTypeExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.FileNameExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.UrlValidator;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ThumbnailsHandler extends ResourcesPropertyHandler {
    protected ThumbnailsHandler(ObjectMapper objectMapper,
            UrlValidator urlValidator,
            ContentTypeExtractor contentTypeExtractor) {
        super(objectMapper, urlValidator, contentTypeExtractor);
    }
    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(ContentProperties.THUMBNAILS);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        handleThumbnailResources(values, builder, context);
    }
}