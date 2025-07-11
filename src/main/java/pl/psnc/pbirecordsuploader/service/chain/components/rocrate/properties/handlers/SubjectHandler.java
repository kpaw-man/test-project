package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.model.researcharea.ResearchAreaResult;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorServiceFactory;
import pl.psnc.pbirecordsuploader.service.chain.components.researcharea.ResearchAreasExtractor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Component
public class SubjectHandler extends BasePropertyHandler {
    private static final String DOMAIN_FIELD = "studySubject";

    private final DescriptorServiceFactory descriptorServiceFactory;
    private final ResearchAreasExtractor researchAreasExtractor;

    public SubjectHandler(ObjectMapper mapper, DescriptorServiceFactory descriptorServiceFactory,
            ResearchAreasExtractor researchAreasExtractor) {
        super(mapper);
        this.descriptorServiceFactory = descriptorServiceFactory;
        this.researchAreasExtractor = researchAreasExtractor;
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.SUBJECT);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) throws ConvertException {
        try {
            handleSubjectAsDescriptors(metadataProperty, values, builder, rootBuilder);
            handleSubjectsAsResearchAreas(metadataProperty, values, builder, rootBuilder);
        } catch (DescriptorException | DescriptorApiException e) {
            log.error("Failed to handle descriptors for subject property", e);
            throw new ConvertException("Failed to handle descriptor-based subjects", e);
        } catch (Exception e) {
            log.error("Unexpected error while handling subject property", e);
            throw new ConvertException("Failed to handle subject property", e);
        }
    }

    private void handleSubjectAsDescriptors(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder)
            throws DescriptorException, DescriptorApiException {

        addContextualReference(metadataProperty, builder);

        DescriptorService descriptorService = descriptorServiceFactory.getDescriptorService(
                DescriptorServiceFactory.DescriptorSourceType.BN);

        List<Descriptor> descriptors = fetchDescriptors(values, descriptorService);
        ArrayNode subjectArray = createEntityReferencesArray(descriptors, Descriptor::getId,
                descriptor -> createContextualEntity(descriptor.getId(), "CategoryCode", descriptor.getName()),
                builder);

        rootBuilder.addProperty(metadataProperty.key(), subjectArray);
    }

    private void handleSubjectsAsResearchAreas(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder) {

        List<ResearchAreaResult> domains = researchAreasExtractor.fetchDomains(values);
        List<ResearchAreaResult> uniqueDomains = removeDuplicatesByCleanedValue(domains);

        // Add domain IDs as simple array
        ArrayNode domainIds = createArrayFromValues(
                uniqueDomains.stream().map(ResearchAreaResult::URI).toList());
        rootBuilder.addProperty(DOMAIN_FIELD, domainIds);

        // Add domain references with contextual entities
        ArrayNode domainRefs = createEntityReferencesArray(uniqueDomains,
                ResearchAreaResult::URI,
                domain -> createContextualEntity(domain.URI(), metadataProperty.key(), domain.cleanedVal()),
                builder);

        rootBuilder.addProperty(metadataProperty.key(), domainRefs);
    }

    private List<Descriptor> fetchDescriptors(List<String> values, DescriptorService descriptorService)
            throws DescriptorException, DescriptorApiException {

        List<Descriptor> descriptors = new ArrayList<>();
        for (String value : values) {
            Descriptor descriptor = descriptorService.applyDescriptor(value);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    private List<ResearchAreaResult> removeDuplicatesByCleanedValue(List<ResearchAreaResult> domains) {
        List<ResearchAreaResult> uniqueByCleanedVal = new ArrayList<>();
        Set<String> seenCleanedVals = new HashSet<>();

        for (ResearchAreaResult domain : domains) {
            if (seenCleanedVals.add(domain.cleanedVal())) {
                uniqueByCleanedVal.add(domain);
            }
        }
        return uniqueByCleanedVal;
    }

    private <T> ArrayNode createEntityReferencesArray(List<T> entities,
            Function<T, String> idExtractor,
            Function<T, ContextualEntity> entityCreator,
            RoCrate.RoCrateBuilder builder) {
        ArrayNode arrayNode = mapper.createArrayNode();

        for (T entity : entities) {
            String id = idExtractor.apply(entity);
            ContextualEntity contextualEntity = entityCreator.apply(entity);

            builder.addContextualEntity(contextualEntity);

            ObjectNode ref = createEntityReference(id);
            arrayNode.add(ref);
        }

        return arrayNode;
    }

    private ObjectNode createEntityReference(String id) {
        ObjectNode ref = mapper.createObjectNode();
        ref.put("@id", id);
        return ref;
    }
}