package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import lombok.extern.slf4j.Slf4j;
import pl.psnc.pbirecordsuploader.model.HdtOntology;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.ContentTypeExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils.UrlValidator;


import java.util.List;


@Slf4j
public abstract class ResourcesPropertyHandler extends BasePropertyHandler {
    private static final String FILE_TYPE = "File";
    private static final String SKETCH_TYPE = "http://purl.org/wf4ever/roterms#Sketch";
    private static final String RESOURCE_TYPE = "http://purl.org/wf4ever/wf4ever#Resource";

    private final UrlValidator urlValidator;
    private final ContentTypeExtractor contentTypeExtractor;

    protected ResourcesPropertyHandler(ObjectMapper objectMapper, UrlValidator urlValidator,
            ContentTypeExtractor contentTypeExtractor) {
        super(objectMapper);
        this.urlValidator = urlValidator;
        this.contentTypeExtractor = contentTypeExtractor;
    }

    protected void handleResources(List<String> values, RoCrate.RoCrateBuilder builder,
            ResourcesPropertyContextHandler context) {
        processResources(values, builder, context, ResourceType.FILE);
    }

    protected void handleThumbnailResources(List<String> values, RoCrate.RoCrateBuilder builder,
            ResourcesPropertyContextHandler context) {
        processResources(values, builder, context, ResourceType.THUMBNAIL);
    }

    private void processResources(List<String> values, RoCrate.RoCrateBuilder builder,
            ResourcesPropertyContextHandler context, ResourceType resourceType) {
        if (isEmptyOrNull(values)) {
            return;
        }

        values.stream()
                .filter(urlValidator::isValid)
                .forEach(url -> processResource(url, builder, context, resourceType));
    }

    private void processResource(String url, RoCrate.RoCrateBuilder builder,
            ResourcesPropertyContextHandler context, ResourceType resourceType) {
        ObjectNode fileRef = createFileReference(url);
        ContextualEntity fileEntity = createEntity(url, resourceType);

        context.addHasPartReference(fileRef);
        builder.addContextualEntity(fileEntity);
    }

    private boolean isEmptyOrNull(List<String> values) {
        return values == null || values.isEmpty();
    }

    private ObjectNode createFileReference(String url) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("@id", url);
        return objectNode;
    }

    private ContextualEntity createEntity(String url, ResourceType resourceType) {
        ContextualEntity.ContextualEntityBuilder builder = new ContextualEntity.ContextualEntityBuilder()
                .setId(url)
                .addProperty("name", url)
                .addProperty("contentUrl", url);

        if (resourceType == ResourceType.FILE) {
            builder.addTypes(List.of(FILE_TYPE, HdtOntology.HC5_DIGITAL_REPRESENTATION.getKey()));
        } else if (resourceType == ResourceType.THUMBNAIL) {
            builder.addTypes(List.of(RESOURCE_TYPE,FILE_TYPE, SKETCH_TYPE, HdtOntology.HC5_DIGITAL_REPRESENTATION.getKey()));
        }

        contentTypeExtractor.extract(url)
                .ifPresent(type -> builder.addProperty("encodingFormat", type));

        return builder.build();
    }

    private enum ResourceType {
        FILE, THUMBNAIL
    }
}