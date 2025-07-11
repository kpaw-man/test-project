package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.PersonPatternMatcher;

import java.util.List;
import java.util.Set;

@Component
public class ActorHandler extends BasePropertyHandler {
    private final PersonPatternMatcher personPatternMatcher;

    public ActorHandler(ObjectMapper objectMapper, PersonPatternMatcher personPatternMatcher) {
        super(objectMapper);
        this.personPatternMatcher = personPatternMatcher;
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.CONTRIBUTOR, DCTerms.CREATOR, DCTerms.PUBLISHER);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        handlePersonOrThingEntities(metadataProperty, values, builder, rootBuilder, personPatternMatcher);
    }
}