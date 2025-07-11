package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorServiceFactory;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeHandlerTest {

    @Mock
    private DescriptorServiceFactory descriptorServiceFactory;
    @Mock
    private DescriptorService descriptorService;
    @Mock
    private RoCrate.RoCrateBuilder builder;
    @Mock
    private RootDataEntity.RootDataEntityBuilder rootBuilder;
    @Mock
    private Descriptor descriptor;
    @Mock
    private ResourcesPropertyContextHandler resourcesPropertyContextHandler;
    @Captor
    private ArgumentCaptor<ContextualEntity> entityCaptor;

    private TypeHandler typeHandler;

    @BeforeEach
    void setUp() {
        typeHandler = new TypeHandler(new ObjectMapper(), descriptorServiceFactory);
    }

    @Test
    void shouldHandleTypeDescriptorsCorrectly() throws Exception {
        List<String> values = List.of("dataset");

        when(descriptorServiceFactory.getDescriptorService(
                DescriptorServiceFactory.DescriptorSourceType.SEMANTIC)).thenReturn(descriptorService);
        when(descriptorService.applyDescriptor("dataset")).thenReturn(descriptor);
        when(descriptor.getId()).thenReturn("http://example.org/dataset");
        when(descriptor.getName()).thenReturn("Dataset");

        typeHandler.handle(DCTerms.TYPE, values, builder, rootBuilder,resourcesPropertyContextHandler);

        verify(builder).addValuePairToContext("type", "http://purl.org/dc/terms/type");
        verify(descriptorService).applyDescriptor("dataset");

        verify(builder).addContextualEntity(entityCaptor.capture());
        ContextualEntity addedEntity = entityCaptor.getValue();
        assertEquals("http://example.org/dataset", addedEntity.getId());
    }

    @Test
    void shouldThrowConvertExceptionOnDescriptorFailure() throws Exception {
        List<String> values = List.of("invalid-type");

        when(descriptorServiceFactory.getDescriptorService(
                DescriptorServiceFactory.DescriptorSourceType.SEMANTIC)).thenReturn(descriptorService);
        when(descriptorService.applyDescriptor("invalid-type")).thenThrow(new DescriptorApiException("Not found"));

        ConvertException exception = assertThrows(ConvertException.class, () -> typeHandler.handle(DCTerms.TYPE, values, builder, rootBuilder,resourcesPropertyContextHandler));

        assertTrue(exception.getMessage().contains("Failed to handle type property"));
    }
}