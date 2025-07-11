package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ResourcesPropertyContextHandler {

    public static final String HAS_PART = "hasPart";
    private final ArrayNode resourcesArray;

    public ResourcesPropertyContextHandler(ObjectMapper objectMapper) {
        this.resourcesArray = objectMapper.createArrayNode();
    }

    public void addHasPartReference(ObjectNode reference) {
        resourcesArray.add(reference);
    }

    public void applyTo(RootDataEntity.RootDataEntityBuilder builder) {
        if (!resourcesArray.isEmpty()) {
            builder.addProperty(HAS_PART, resourcesArray);
        }
    }
}