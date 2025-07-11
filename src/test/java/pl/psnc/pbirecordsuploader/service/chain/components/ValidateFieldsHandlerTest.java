package pl.psnc.pbirecordsuploader.service.chain.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
import pl.psnc.pbirecordsuploader.domain.ChainJobStatus;
import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
import pl.psnc.pbirecordsuploader.exceptions.ValidationException;

import static org.mockito.Mockito.*;


class ValidateFieldsHandlerTest {

    private final ValidateFieldsHandler handler = new ValidateFieldsHandler();

    @Test
    void testProcess_ValidXml() throws PbiUploaderException {
        ChainJobEntity mockEntity = mock(ChainJobEntity.class);
        when(mockEntity.getPbiBody()).thenReturn("""
                    <dace:pbi xmlns:dc="http://purl.org/dc/elements/1.1/"
                              xmlns:dace="https://bs.katowice.pl/bsa/"
                              xmlns:terms="http://purl.org/dc/terms/">
                        <dace:dc>
                            <dace:title>Valid Title</dace:title>
                            <dace:creator>Valid Creator</dace:creator>
                            <dace:subject>Valid Subject</dace:subject>
                            <dace:description>Valid Description</dace:description>
                            <dace:rights>Valid Rights</dace:rights>
                            <dace:relation>Valid Relation</dace:relation>
                            <dace:identifier>Valid Identifier</dace:identifier>
                        </dace:dc>
                    </dace:pbi>
                """);

        boolean result = handler.process(mockEntity);

        assertTrue(result);
        verify(mockEntity, times(1)).setChainJobStatus(ChainJobStatus.VALIDATED_FIELDS_FROM_SOURCE);
    }

    @Test
    void testProcess_InvalidXml_MissingField() {
        ChainJobEntity mockEntity = mock(ChainJobEntity.class);
        when(mockEntity.getPbiBody()).thenReturn("""
                    <dace:pbi xmlns:dc="http://purl.org/dc/elements/1.1/"
                              xmlns:dace="https://bs.katowice.pl/bsa/"
                              xmlns:terms="http://purl.org/dc/terms/">
                        <dace:dc>
                            <dace:title>Valid Title</dace:title>
                        </dace:dc>
                    </dace:pbi>
                """);

        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(mockEntity));

        assertInstanceOf(ValidationException.class, exception.getCause());
        assertEquals(
                "XML validation failed cause: Missing fields: Creator, Description, Subject, Rights, Relation, Identifier",
                exception.getCause().getMessage());

        verify(mockEntity, times(1)).setChainJobStatus(any());
    }

    @Test
    void testProcess_EmptyDaceBody() {
        ChainJobEntity mockEntity = mock(ChainJobEntity.class);
        when(mockEntity.getPbiBody()).thenReturn("");


        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(mockEntity));

        assertInstanceOf(ValidationException.class, exception.getCause());
        assertEquals("Pbi body cannot be empty", exception.getCause().getMessage());

        verify(mockEntity, times(1)).setChainJobStatus(any());
    }

    @Test
    void testProcess_NullDaceBody() {
        ChainJobEntity mockEntity = mock(ChainJobEntity.class);
        when(mockEntity.getDaceBody()).thenReturn(null);

        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(mockEntity));

        assertInstanceOf(ValidationException.class, exception.getCause());
        assertEquals("Pbi body cannot be empty", exception.getCause().getMessage());

        verify(mockEntity, times(1)).setChainJobStatus(any());
    }
}
