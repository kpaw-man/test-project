package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.BasePropertyHandler;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.List;
import java.util.Set;

@Component
public class ArrayPropertyHandler extends BasePropertyHandler {

    public ArrayPropertyHandler(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Set<MetadataProperty> getSupportedTerms() {
        return Set.of(DCTerms.ABSTRACT, DCTerms.ACCRUAL_METHOD, DCTerms.ACCRUAL_PERIODICITY, DCTerms.ACCRUAL_POLICY,
                DCTerms.ALTERNATIVE, DCTerms.AUDIENCE, DCTerms.AVAILABLE, DCTerms.BIBLIOGRAPHIC_CITATION,
                DCTerms.CONFORMS_TO, DCTerms.COVERAGE_TEMPORAL, DCTerms.DATE_ACCEPTED, DCTerms.DATE_COPYRIGHTED,
                DCTerms.DATE_SUBMITTED, DCTerms.EDUCATION_LEVEL, DCTerms.EXTENT, DCTerms.HAS_FORMAT, DCTerms.HAS_PART,
                DCTerms.HAS_VERSION, DCTerms.INSTRUCTIONAL_METHOD, DCTerms.IS_FORMAT_OF, DCTerms.IS_PART_OF,
                DCTerms.IS_REFERENCED_BY, DCTerms.IS_REPLACED_BY, DCTerms.IS_REQUIRED_BY, DCTerms.ISSUED_DATE,
                DCTerms.IS_VERSION_OF, DCTerms.MEDIATOR, DCTerms.MEDIUM, DCTerms.MODIFIED, DCTerms.PROVENANCE,
                DCTerms.REFERENCES, DCTerms.REPLACES, DCTerms.REQUIRES, DCTerms.RIGHTS_HOLDER,
                DCTerms.TABLE_OF_CONTENTS, DCTerms.VALID, DCTerms.ACCESS_RIGHTS, DCTerms.COVERAGE, DCTerms.FORMAT,
                DCTerms.LANGUAGE, DCTerms.RIGHTS, DCTerms.SPATIAL);
    }

    @Override
    public void handle(MetadataProperty metadataProperty, List<String> values, RoCrate.RoCrateBuilder builder,
            RootDataEntity.RootDataEntityBuilder rootBuilder, ResourcesPropertyContextHandler context) {
        addArrayProperty(metadataProperty, values, builder, rootBuilder);
    }
}