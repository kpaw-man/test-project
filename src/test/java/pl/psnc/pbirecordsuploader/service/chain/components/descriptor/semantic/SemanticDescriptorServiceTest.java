package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorApiException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorException;
import pl.psnc.pbirecordsuploader.exceptions.descriptor.DescriptorValidatonException;
import pl.psnc.pbirecordsuploader.model.Descriptor;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.DescriptorValidatorService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SemanticDescriptorServiceTest {

    @Mock
    private KeyDictionary keyDictionary;

    @Mock
    private SemanticAnalyzerApiClient semanticAnalyzerApiClient;

    @Mock
    private DescriptorValidatorService validatorService;

    private SemanticDescriptorService descriptorService;

    @BeforeEach
    void setUp() {
        descriptorService = new SemanticDescriptorService(keyDictionary, semanticAnalyzerApiClient, validatorService);
    }

    @Test
    void shouldReturnDescriptorWhenFoundAndValid() throws DescriptorValidatonException, DescriptorException,
            DescriptorApiException {
        String value = "testValue";
        String bestMatch = "bestMatchTerm";
        String mappedId = "http://example.com/id456";

        when(semanticAnalyzerApiClient.fetch("testValue")).thenReturn(bestMatch);
        when(keyDictionary.getMappedId(bestMatch)).thenReturn(mappedId);
        doNothing().when(validatorService).validateDescriptor(any(Descriptor.class));

        Descriptor result = descriptorService.applyDescriptor(value);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(mappedId);
        assertThat(result.getName()).isEqualTo(bestMatch);
        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary).getMappedId(bestMatch);
        verify(validatorService).validateDescriptor(any(Descriptor.class));
    }

    @Test
    void shouldThrowDescriptorExceptionWhenBestMatchIsNull() throws DescriptorValidatonException,
            DescriptorApiException {
        String value = "testValue";
        when(semanticAnalyzerApiClient.fetch("testValue")).thenReturn(null);

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .isInstanceOf(DescriptorException.class)
                .hasMessageContaining("Best match not found: descriptor 'testValue' does not exist");

        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary, never()).getMappedId(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenMappedIdNotFound() throws DescriptorValidatonException, DescriptorApiException {
        String value = "testValue";
        String bestMatch = "bestMatchTerm";

        when(semanticAnalyzerApiClient.fetch("testValue")).thenReturn(bestMatch);
        when(keyDictionary.getMappedId(bestMatch)).thenReturn(null);

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor not found for value: testValue");

        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary).getMappedId(bestMatch);
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenValidationFails() throws DescriptorValidatonException, DescriptorApiException {
        String value = "testValue";
        String bestMatch = "bestMatchTerm";
        String mappedId = "http://example.com/id456";

        when(semanticAnalyzerApiClient.fetch("testValue")).thenReturn(bestMatch);
        when(keyDictionary.getMappedId(bestMatch)).thenReturn(mappedId);
        doThrow(new DescriptorValidatonException("Validation failed")).when(validatorService).validateDescriptor(any(
                Descriptor.class));

        assertThatThrownBy(() -> descriptorService.applyDescriptor(value))
                .hasMessageContaining("Descriptor validation failed: Validation failed");

        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary).getMappedId(bestMatch);
        verify(validatorService).validateDescriptor(any(Descriptor.class));
    }

    @Test
    void shouldThrowExceptionWhenInputIsEmpty() throws DescriptorValidatonException, DescriptorApiException {
        String emptyValues = null;

        assertThatThrownBy(() -> descriptorService.applyDescriptor(emptyValues))
                .hasMessageContaining("Descriptor not found for value: null");

        verify(semanticAnalyzerApiClient, never()).fetch(any());
        verify(keyDictionary, never()).getMappedId(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldThrowExceptionWhenInputIsNull() throws DescriptorValidatonException, DescriptorApiException {
        assertThatThrownBy(() -> descriptorService.applyDescriptor(null))
                .hasMessageContaining("Descriptor not found for value: null");

        verify(semanticAnalyzerApiClient, never()).fetch(any());
        verify(keyDictionary, never()).getMappedId(any());
        verify(validatorService, never()).validateDescriptor(any());
    }

    @Test
    void shouldFindDescriptorWhenInputIsValid() throws DescriptorException, DescriptorApiException {
        String value = "testValue";
        String bestMatch = "bestMatchTerm";
        String mappedId = "http://example.com/id456";

        when(semanticAnalyzerApiClient.fetch("testValue")).thenReturn(bestMatch);
        when(keyDictionary.getMappedId(bestMatch)).thenReturn(mappedId);

        Optional<Descriptor> result = descriptorService.findDescriptor(value);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(mappedId);
        assertThat(result.get().getName()).isEqualTo(bestMatch);
        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary).getMappedId(bestMatch);
    }

    @Test
    void shouldReturnEmptyWhenFindDescriptorInputIsEmpty() throws DescriptorException, DescriptorApiException {
        String emptyValue = null;

        Optional<Descriptor> result = descriptorService.findDescriptor(emptyValue);

        assertThat(result).isEmpty();
        verify(semanticAnalyzerApiClient, never()).fetch(any());
        verify(keyDictionary, never()).getMappedId(any());
    }

    @Test
    void shouldReturnEmptyWhenFindDescriptorInputIsNull() throws DescriptorException, DescriptorApiException {
        Optional<Descriptor> result = descriptorService.findDescriptor(null);

        assertThat(result).isEmpty();
        verify(semanticAnalyzerApiClient, never()).fetch(any());
        verify(keyDictionary, never()).getMappedId(any());
    }

    @Test
    void shouldThrowDescriptorExceptionWhenApiThrowsException() throws DescriptorApiException {
        String value = "testValue";
        when(semanticAnalyzerApiClient.fetch("testValue")).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> descriptorService.findDescriptor(value))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("API error");

        verify(semanticAnalyzerApiClient).fetch("testValue");
        verify(keyDictionary, never()).getMappedId(any());
    }
}