package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;
import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.ResourcesPropertyContextHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArrayPropertyHandlerTest {

    private ArrayPropertyHandler arrayPropertyHandler;

    @Mock
    private RoCrate.RoCrateBuilder mockBuilder;

    @Mock
    private RootDataEntity.RootDataEntityBuilder mockRootBuilder;

    @Mock
    private ResourcesPropertyContextHandler mockContext;

    @Mock
    private MetadataProperty mockMetadataProperty;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        arrayPropertyHandler = new ArrayPropertyHandler(objectMapper);
    }

    @Test
    void testConstructor() {
        assertNotNull(arrayPropertyHandler);
        Set<MetadataProperty> supportedTerms = arrayPropertyHandler.getSupportedTerms();
        assertNotNull(supportedTerms);
    }

    @Test
    void testGetSupportedTerms_ReturnsCorrectTerms() {
        Set<MetadataProperty> supportedTerms = arrayPropertyHandler.getSupportedTerms();

        assertNotNull(supportedTerms);
        assertEquals(42, supportedTerms.size());

        assertTrue(supportedTerms.contains(DCTerms.ABSTRACT));
        assertTrue(supportedTerms.contains(DCTerms.ALTERNATIVE));
    }

    @Test
    void testGetSupportedTerms_ReturnsImmutableSet() {
        Set<MetadataProperty> supportedTerms = arrayPropertyHandler.getSupportedTerms();
        assertThrows(UnsupportedOperationException.class, () -> supportedTerms.add(mock(MetadataProperty.class)));
    }

    @Test
    void testHandle_WithSingleValue() {
        List<String> values = List.of("Single Value");
        String propertyKey = "testProperty";
        String propertyUri = "http://example.com/test";

        when(mockMetadataProperty.key()).thenReturn(propertyKey);
        when(mockMetadataProperty.uri()).thenReturn(propertyUri);

        arrayPropertyHandler.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder).addValuePairToContext(propertyKey, propertyUri);

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq(propertyKey), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(1, capturedArray.size());
        assertEquals("Single Value", capturedArray.get(0).asText());
    }

    @Test
    void testHandle_WithMultipleValues() {
        List<String> values = Arrays.asList("Value 1", "Value 2", "Value 3");
        String propertyKey = "multiProperty";
        String propertyUri = "http://example.com/multi";

        when(mockMetadataProperty.key()).thenReturn(propertyKey);
        when(mockMetadataProperty.uri()).thenReturn(propertyUri);

        arrayPropertyHandler.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder).addValuePairToContext(propertyKey, propertyUri);

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq(propertyKey), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(3, capturedArray.size());
        assertEquals("Value 1", capturedArray.get(0).asText());
        assertEquals("Value 2", capturedArray.get(1).asText());
        assertEquals("Value 3", capturedArray.get(2).asText());
    }

    @Test
    void testHandle_WithEmptyList() {
        List<String> values = List.of();
        String propertyKey = "emptyProperty";
        String propertyUri = "http://example.com/empty";

        when(mockMetadataProperty.key()).thenReturn(propertyKey);
        when(mockMetadataProperty.uri()).thenReturn(propertyUri);

        arrayPropertyHandler.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder).addValuePairToContext(propertyKey, propertyUri);

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq(propertyKey), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(0, capturedArray.size());
        assertTrue(capturedArray.isEmpty());
    }

    @Test
    void testHandle_WithNullUri() {
        List<String> values = List.of("Test Value");
        String propertyKey = "nullUriProperty";

        when(mockMetadataProperty.key()).thenReturn(propertyKey);
        when(mockMetadataProperty.uri()).thenReturn(null);

        arrayPropertyHandler.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder, never()).addValuePairToContext(any(), any());

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq(propertyKey), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(1, capturedArray.size());
        assertEquals("Test Value", capturedArray.get(0).asText());
    }

    @Test
    void testHandle_WithEmptyUri() {
        List<String> values = List.of("Test Value");
        String propertyKey = "emptyUriProperty";

        when(mockMetadataProperty.key()).thenReturn(propertyKey);
        when(mockMetadataProperty.uri()).thenReturn("");

        arrayPropertyHandler.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder, never()).addValuePairToContext(any(), any());

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq(propertyKey), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(1, capturedArray.size());
        assertEquals("Test Value", capturedArray.get(0).asText());
    }
    @Test
    void testHandle_VerifyObjectMapperUsage() {
        ObjectMapper spyMapper = spy(new ObjectMapper());
        ArrayPropertyHandler handlerWithSpyMapper = new ArrayPropertyHandler(spyMapper);

        List<String> values = List.of("Value1", "Value2");
        when(mockMetadataProperty.key()).thenReturn("testKey");
        when(mockMetadataProperty.uri()).thenReturn("testUri");

        handlerWithSpyMapper.handle(mockMetadataProperty, values, mockBuilder, mockRootBuilder, mockContext);

        verify(spyMapper).createArrayNode();
    }

    @Test
    void testIntegration_WithRealDCTerms() {
        // Integration test using actual DCTerms constants (assuming they exist)
        // This test verifies the handler works with real metadata properties

        List<String> abstractValues = Arrays.asList("This is an abstract", "Another abstract");

        MetadataProperty abstractProperty = mock(MetadataProperty.class);
        when(abstractProperty.key()).thenReturn("abstract");
        when(abstractProperty.uri()).thenReturn("http://purl.org/dc/terms/abstract");

        arrayPropertyHandler.handle(abstractProperty, abstractValues, mockBuilder, mockRootBuilder, mockContext);

        verify(mockBuilder).addValuePairToContext("abstract", "http://purl.org/dc/terms/abstract");

        ArgumentCaptor<ArrayNode> arrayCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(mockRootBuilder).addProperty(eq("abstract"), arrayCaptor.capture());

        ArrayNode capturedArray = arrayCaptor.getValue();
        assertEquals(2, capturedArray.size());
        assertEquals("This is an abstract", capturedArray.get(0).asText());
        assertEquals("Another abstract", capturedArray.get(1).asText());
    }
}