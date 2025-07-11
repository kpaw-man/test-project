package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import lombok.Getter;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.List;
import java.util.Set;

@Getter
@Component
public class SimplePropertyHandler extends BasePropertyHandler {
    public SimplePropertyHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.CREATED, DCTerms.DATE, DCTerms.IDENTIFIER, DCTerms.ISSUED, DCTerms.SOURCE,
                DCTerms.TITLE);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        addSimpleProperty(metadataProperty, values, builder, rootBuilder);
    }
}