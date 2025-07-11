package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.model.BNSuggestionResult;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BNDescriptorMapperTest {

    private static final String DESCRIPTOR_URI = "http://example.com/descriptor/";
    private BNDescriptorMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BNDescriptorMapper(DESCRIPTOR_URI);
    }

    @Test
    void shouldMapToDescriptorSuccessfully() {
        String uri = "http://test.com/suggestion?id=12345";
        String label = "Test Label";
        BNSuggestionResult result = new BNSuggestionResult( label,uri);

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isPresent();
        assertThat(descriptor.get().getId()).isEqualTo(DESCRIPTOR_URI + "12345");
        assertThat(descriptor.get().getName()).isEqualTo(label);
    }

    @Test
    void shouldHandleUriWithoutIdParameter() {
        String uri = "http://test.com/suggestion?other=value";
        String label = "Test Label";
        BNSuggestionResult result = new BNSuggestionResult(uri, label);

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isEmpty();
    }

    @Test
    void shouldHandleInvalidUri() {
        String uri = "invalid:uri";
        String label = "Test Label";
        BNSuggestionResult result = new BNSuggestionResult(uri, label);

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isEmpty();
    }

    @Test
    void shouldHandleNullResult() {
        Optional<Descriptor> descriptor = mapper.mapToDescriptor(null);

        assertThat(descriptor).isEmpty();
    }

    @Test
    void shouldHandleNullUri() {
        BNSuggestionResult result = new BNSuggestionResult(null, "Test Label");

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isEmpty();
    }

    @Test
    void shouldHandleNullLabel() {
        BNSuggestionResult result = new BNSuggestionResult("http://test.com/suggestion?id=12345", null);

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isEmpty();
    }

    @Test
    void shouldHandleEmptyIdValue() {
        String uri = "http://test.com/suggestion?id=";
        String label = "Test Label";
        BNSuggestionResult result = new BNSuggestionResult(label,uri);

        Optional<Descriptor> descriptor = mapper.mapToDescriptor(result);

        assertThat(descriptor).isPresent();
        assertThat(descriptor.get().getId()).isEqualTo(DESCRIPTOR_URI);
        assertThat(descriptor.get().getName()).isEqualTo(label);
    }
}