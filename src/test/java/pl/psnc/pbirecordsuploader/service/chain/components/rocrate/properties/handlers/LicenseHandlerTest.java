package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseHandlerTest {

    @Mock
    private RoCrate.RoCrateBuilder builder;

    @Mock
    private RootDataEntity.RootDataEntityBuilder rootBuilder;

    @Mock
    private ResourcesPropertyContextHandler resourcesPropertyContextHandler;

    private LicenseHandler licenseHandler;

    private static final String DEFAULT_LICENSE = "http://rightsstatements.org/vocab/InC/1.0/";

    @BeforeEach
    void setUp() {
        licenseHandler = new LicenseHandler(new ObjectMapper());
    }

    @Test
    void shouldUseProvidedLicense() {
        List<String> values = List.of("https://creativecommons.org/licenses/by/4.0/");

        licenseHandler.handle(DCTerms.LICENSE, values, builder, rootBuilder,resourcesPropertyContextHandler);
        verify(rootBuilder).setLicense("https://creativecommons.org/licenses/by/4.0/");
    }

    @Test
    void shouldUseDefaultLicenseWhenValuesAreEmpty() {
        licenseHandler.handle(DCTerms.LICENSE, List.of(), builder, rootBuilder,resourcesPropertyContextHandler);
        verify(rootBuilder).setLicense(DEFAULT_LICENSE);
    }

    @Test
    void shouldUseDefaultLicenseWhenValuesAreNull() {
        licenseHandler.handle(DCTerms.LICENSE, null, builder, rootBuilder,resourcesPropertyContextHandler);
        verify(rootBuilder).setLicense(DEFAULT_LICENSE);
    }
}