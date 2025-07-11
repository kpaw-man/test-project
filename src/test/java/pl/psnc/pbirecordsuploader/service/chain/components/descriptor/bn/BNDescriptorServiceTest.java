package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.BNSuggestionResult;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorValidatorService;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BNDescriptorServiceTest {

    @Mock
    private BNDescriptorApiClient apiClient;

    @Mock
    private BNDescriptorMapper mapper;

    @Mock
    private DescriptorValidatorService validatorService;

    private BNDescriptorService descriptorService;

    @BeforeEach
    void setUp() {
        descriptorService = new BNDescriptorService(apiClient, mapper, validatorService);
    }

    @Test
    void shouldReturnDescriptorWhenFoundAndValid() throws DescriptorValidatonException, DescriptorException,
            DescriptorApiException {
        String value = "testValue";
        BNSuggestionResult apiResult = mock(BNSuggestionResult.class);
        Descriptor expectedDescriptor = new Descriptor("http://example.com/id123", "Test Descriptor");

        when(apiClient.fetch("testValue")).thenReturn(apiResult);
        when(mapper.mapToDescriptor(apiResult)).thenReturn(Optional.of(expectedDescriptor));
        doNothing().when(validatorService).validateDescriptor(expectedDescriptor);

        Descriptor result = descriptorService.applyDescriptor(value);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("http://example.com/id123");
        assertThat(result.getName()).isEqualTo("Test Descriptor");
        verify(apiClient).fetch("testValue");
        verify(mapper).mapToDescriptor(apiResult);
        verify(validatorService).validateDescriptor(expectedDescriptor);
    }

    @Test
    void shouldThrowExceptionWhenDescriptorNotFound() throws DescriptorValidatonException, DescriptorApiException {
        String value = "testValue";
        when(apiClient.fetch("testValue")).thenReturn(null);

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor not found for value: testValue");

        verify(apiClient).fetch("testValue");
        verify(mapper, never()).mapToDescriptor(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenMappingFails() throws DescriptorValidatonException, DescriptorApiException {
        String value = "testValue";
        BNSuggestionResult apiResult = mock(BNSuggestionResult.class);

        when(apiClient.fetch("testValue")).thenReturn(apiResult);
        when(mapper.mapToDescriptor(apiResult)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor not found for value: testValue");

        verify(apiClient).fetch("testValue");
        verify(mapper).mapToDescriptor(apiResult);
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenValidationFails() throws DescriptorValidatonException, DescriptorApiException {
        String value = "testValue";
        BNSuggestionResult apiResult = mock(BNSuggestionResult.class);
        Descriptor descriptor = new Descriptor("http://example.com/id123", "Test Descriptor");

        when(apiClient.fetch("testValue")).thenReturn(apiResult);
        when(mapper.mapToDescriptor(apiResult)).thenReturn(Optional.of(descriptor));
        doThrow(new DescriptorValidatonException("Validation failed")).when(validatorService).validateDescriptor(descriptor);

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor validation failed: Validation failed");

        verify(apiClient).fetch("testValue");
        verify(mapper).mapToDescriptor(apiResult);
        verify(validatorService).validateDescriptor(descriptor);
    }

    @Test
    void shouldThrowExceptionWhenInputIsEmpty() throws DescriptorValidatonException, DescriptorApiException {
        String emptyValue = null;

        assertThatThrownBy(() -> descriptorService.applyDescriptor(emptyValue))
                .hasMessageContaining("Descriptor not found for value: null");

        verify(apiClient, never()).fetch(any());
        verify(mapper, never()).mapToDescriptor(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenInputIsNull() throws DescriptorValidatonException, DescriptorApiException {
        assertThatThrownBy(() -> descriptorService.applyDescriptor(null))
                .hasMessageContaining("Descriptor not found for value: null");

        verify(apiClient, never()).fetch(any());
        verify(mapper, never()).mapToDescriptor(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldFindDescriptorWhenInputIsValid() throws DescriptorApiException {
        String value = "testValue";
        BNSuggestionResult apiResult = mock(BNSuggestionResult.class);
        Descriptor expectedDescriptor = new Descriptor("http://example.com/id123", "Test Descriptor");

        when(apiClient.fetch("testValue")).thenReturn(apiResult);
        when(mapper.mapToDescriptor(apiResult)).thenReturn(Optional.of(expectedDescriptor));

        Optional<Descriptor> result = descriptorService.findDescriptor(value);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("http://example.com/id123");
        assertThat(result.get().getName()).isEqualTo("Test Descriptor");
        verify(apiClient).fetch("testValue");
        verify(mapper).mapToDescriptor(apiResult);
    }

    @Test
    void shouldReturnEmptyWhenFindDescriptorInputIsEmpty() throws DescriptorApiException {
        String emptyValue = null;
        Optional<Descriptor> result = descriptorService.findDescriptor(emptyValue);

        assertThat(result).isEmpty();
        verify(apiClient, never()).fetch(any());
        verify(mapper, never()).mapToDescriptor(any());
    }

    @Test
    void shouldReturnEmptyWhenFindDescriptorInputIsNull() throws DescriptorApiException {
        Optional<Descriptor> result = descriptorService.findDescriptor(null);

        assertThat(result).isEmpty();
        verify(apiClient, never()).fetch(any());
        verify(mapper, never()).mapToDescriptor(any());
    }
}