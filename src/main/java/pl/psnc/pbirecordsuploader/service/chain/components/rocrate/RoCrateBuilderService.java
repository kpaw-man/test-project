package pl.psnc.pbirecordsuploader.service.chain.components.rocrate;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.model.ExtractedXmlContent;
import pl.psnc.pbirecordsuploader.model.HdtOntology;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.PropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.PropertyHandlerRegistry;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoCrateBuilderService {
    private static final String RO_CRATE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS+00:00";

    private final PropertyHandlerRegistry handlerRegistry;
    private final ObjectProvider<ResourcesPropertyContextHandler> contextHandlerProvider;

    public RoCrate buildRoCrate(ExtractedXmlContent extractedContent) throws ConvertException {
        try {
            RoCrate.RoCrateBuilder builder = new RoCrate.RoCrateBuilder();
            ResourcesPropertyContextHandler resourcesPropertyContextHandler = contextHandlerProvider.getObject();
            RootDataEntity.RootDataEntityBuilder rootBuilder = createInitialRootDataEntity();

            builder.addValuePairToContext(HdtOntology.HDT.getKey(), HdtOntology.HDT_URI.getKey());

            processProperties(extractedContent.properties(), builder, rootBuilder,resourcesPropertyContextHandler);
            resourcesPropertyContextHandler.applyTo(rootBuilder);

            RoCrate roCrate = builder.build();
            roCrate.setRootDataEntity(rootBuilder.build());
            return roCrate;
        } catch (Exception e) {
            throw new ConvertException("Failed to build RO-Crate", e);
        }
    }

    private RootDataEntity.RootDataEntityBuilder createInitialRootDataEntity() {
        return new RootDataEntity.RootDataEntityBuilder()
                .addType(HdtOntology.HC2_HERITAGE_DIGITAL_TWIN.getKey())
                .addProperty("name", "Automatic RO ingestion")
                .addProperty("datePublished", getCurrentFormattedDate())
                .addProperty("encodingFormat", "application/ld+json");
    }

    private void processProperties(Map<String, List<String>> properties, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder,ResourcesPropertyContextHandler contextHandler) throws ConvertException {

        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            MetadataProperty metadataProperty = MetadataPropertyResolver.fromKey(key).orElse(null);
            if (metadataProperty == null) {
                log.warn("Unknown property key: {}", key);
                continue;
            }
            PropertyHandler handler = handlerRegistry.getHandler(metadataProperty).orElse(null);
            if (handler == null) {
                log.warn("No handler found for MetadataProperty: {}", metadataProperty.key());
                continue;
            }
            try {
                handler.handle(metadataProperty, values, builder, rootBuilder, contextHandler);
            } catch (Exception e) {
                log.error("Error handling MetadataProperty {}: {}", metadataProperty.key(), e.getMessage(), e);
                throw new ConvertException("Failed to process MetadataProperty: " + metadataProperty.key(), e);
            }
        }
    }

    private String getCurrentFormattedDate() {
        return DateTimeFormatter.ofPattern(RO_CRATE_DATE_FORMAT).withZone(ZoneOffset.UTC).format(Instant.now());
    }
}