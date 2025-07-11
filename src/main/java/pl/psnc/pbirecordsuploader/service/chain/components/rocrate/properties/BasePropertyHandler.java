package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import lombok.RequiredArgsConstructor;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.PersonPatternMatcher;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BasePropertyHandler implements PropertyHandler {
    public static final String PERSON_ENTITY_TYPE = "Person";
    public static final String GENERAL_ENTITY_TYPE = "Thing";
    protected final ObjectMapper mapper;

    protected void addContextualReference(MetadataProperty metadataProperty, RoCrate.RoCrateBuilder builder) {
        if (metadataProperty.uri() != null && !metadataProperty.uri().isEmpty())
            builder.addValuePairToContext(metadataProperty.key(), metadataProperty.uri());
    }

    protected void addSimpleProperty(MetadataProperty metadataProperty, List<String> values,
            RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder) {
        addContextualReference(metadataProperty, builder);
        values.forEach(value -> rootBuilder.addProperty(metadataProperty.key(), value));
    }

    protected void addArrayProperty(MetadataProperty metadataProperty, List<String> values,
            RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder) {
        ArrayNode domainIds = createArrayFromValues(values.stream().toList());
        addContextualReference(metadataProperty, builder);
        rootBuilder.addProperty(metadataProperty.key(), domainIds);
    }

    protected ArrayNode createArrayFromValues(List<String> values) {
        ArrayNode arrayNode = mapper.createArrayNode();
        values.forEach(arrayNode::add);
        return arrayNode;
    }

    protected void handlePersonOrThingEntities(MetadataProperty metadataProperty, List<String> values,
            RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder,
            PersonPatternMatcher personPatternMatcher) {
        addContextualReference(metadataProperty, builder);
        ArrayNode entityArray = mapper.createArrayNode();

        for (String value : values) {

            String actorPrefix = "https://dariah-pbi.org/actor-id/";
            UUID  randomUUID = UUID.randomUUID();
            String actorURI = actorPrefix + randomUUID;


            String entityType = personPatternMatcher.isPersonName(value) ? PERSON_ENTITY_TYPE : GENERAL_ENTITY_TYPE;

            builder.addContextualEntity(createContextualEntityActor(actorURI,entityType,value));

            ObjectNode entityRef = mapper.createObjectNode();
            entityRef.put("@id", actorURI);
            entityArray.add(entityRef);
        }
        rootBuilder.addProperty(metadataProperty.key(), entityArray);
    }

    protected ContextualEntity createContextualEntityActor(String id, String type, String name) {
        return new ContextualEntity.ContextualEntityBuilder()
                .setId(id)
                .addTypes(List.of(type,"prov:Agent"))
                .addProperty("name", name)
                .addProperty("schema:additionalType", "External Agent")
                .build();
    }

    protected ContextualEntity createContextualEntity(String id, String type, String name) {
        return new ContextualEntity.ContextualEntityBuilder()
                .setId(id)
                .addType(type)
                .addProperty("name", name)
                .build();
    }
}