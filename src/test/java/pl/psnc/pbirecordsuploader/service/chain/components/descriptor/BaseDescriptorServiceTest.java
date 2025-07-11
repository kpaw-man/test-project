package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.Descriptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseDescriptorServiceTest {

    @Mock
    private DescriptorValidatorService validatorService;

    private TestBaseDescriptorService descriptorService;

    @BeforeEach
    void setUp() {
        descriptorService = new TestBaseDescriptorService(validatorService);
    }

    @Test
    void shouldReturnDescriptorWhenFoundAndValid() throws DescriptorValidatonException, DescriptorException,
            DescriptorApiException {
        String value = "testValue";
        Descriptor expectedDescriptor = new Descriptor("http://example.com/id123", "Test Descriptor");

        descriptorService.setMockDescriptor(Optional.of(expectedDescriptor));
        doNothing().when(validatorService).validateDescriptor(expectedDescriptor);

        Descriptor result = descriptorService.applyDescriptor(value);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("http://example.com/id123");
        assertThat(result.getName()).isEqualTo("Test Descriptor");
        verify(validatorService).validateDescriptor(expectedDescriptor);
    }

    @Test
    void shouldThrowExceptionWhenDescriptorNotFound() throws DescriptorValidatonException {
        String value = "testValue";
        descriptorService.setMockDescriptor(Optional.empty());

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor not found for value: testValue");

        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenValidationFails() throws DescriptorValidatonException {
        String value = "testValue";
        Descriptor descriptor = new Descriptor("http://example.com/id123", "Test Descriptor");

        descriptorService.setMockDescriptor(Optional.of(descriptor));
        doThrow(new IllegalStateException("Validation failed")).when(validatorService).validateDescriptor(descriptor);

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Validation failed");

        verify(validatorService).validateDescriptor(descriptor);
    }


    @Setter
    private static class TestBaseDescriptorService extends BaseDescriptorService {
        private Optional<Descriptor> mockDescriptor;

        public TestBaseDescriptorService(DescriptorValidatorService validatorService) {
            super(validatorService);
            this.mockDescriptor = Optional.empty();
        }

        @Override
        protected Optional<Descriptor> findDescriptor(String value) {
            return mockDescriptor;
        }
    }
}