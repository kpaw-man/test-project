package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;

import java.util.List;
import java.util.Set;

public interface PropertyHandler {
    Set<MetadataProperty> getSupportedTerms();

    void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) throws ConvertException;
}