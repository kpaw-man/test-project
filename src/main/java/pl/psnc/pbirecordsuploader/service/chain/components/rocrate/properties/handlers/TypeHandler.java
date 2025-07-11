package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorServiceFactory;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.List;
import java.util.Set;

@Component
public class TypeHandler extends BasePropertyHandler {
    private final DescriptorServiceFactory descriptorServiceFactory;

    public TypeHandler(ObjectMapper mapper, DescriptorServiceFactory descriptorServiceFactory) {
        super(mapper);
        this.descriptorServiceFactory = descriptorServiceFactory;
    }
    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.TYPE);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values,
            RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) throws ConvertException {
        try {
            addContextualReference(metadataProperty, builder);

            DescriptorService descriptorService = descriptorServiceFactory
                    .getDescriptorService(DescriptorServiceFactory.DescriptorSourceType.SEMANTIC);

            ArrayNode typeArray = createDescriptorArray(values, descriptorService, builder);
            rootBuilder.addProperty(metadataProperty.key(), typeArray);

        } catch (Exception e) {
            throw new ConvertException("Failed to handle type property", e);
        }
    }

    private ArrayNode createDescriptorArray(List<String> values,
            DescriptorService descriptorService,
            RoCrate.RoCrateBuilder builder) throws DescriptorException, DescriptorApiException {
        ArrayNode array = mapper.createArrayNode();
        for (String value : values) {
            Descriptor descriptor = descriptorService.applyDescriptor(value);

            ContextualEntity entity = createContextualEntity(descriptor.getId(), "CategoryCode", descriptor.getName());
            builder.addContextualEntity(entity);

            ObjectNode ref = mapper.createObjectNode();
            ref.put("@id", descriptor.getId());
            array.add(ref);
        }
        return array;
    }
}