package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.serialization;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoCrateSerializerTest {

    @Mock
    RoCrate roCrate = mock(RoCrate.class);

    @InjectMocks
    RoCrateSerializer serializer;

    @Test
    void testSerializeToZip_success(@TempDir Path tempDir) throws Exception {
        Path mockTempFile = tempDir.resolve("ro-crate.zip");
        Files.writeString(mockTempFile, "fake zip content");

        try (MockedConstruction<ZipWriter> mockedZipWriter = mockConstruction(ZipWriter.class,
                (mock, context) -> doNothing().when(mock).save(any(RoCrate.class), eq(mockTempFile.toString())));
                var filesMock = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            filesMock.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(mockTempFile);
            filesMock.when(() -> Files.readAllBytes(mockTempFile)).thenReturn("fake zip content".getBytes());
            filesMock.when(() -> Files.deleteIfExists(mockTempFile)).thenReturn(true);

            byte[] result = serializer.serializeToZip(roCrate);

            assertNotNull(result);
            assertEquals("fake zip content", new String(result));

            ZipWriter zipWriterMock = mockedZipWriter.constructed().get(0);
            verify(zipWriterMock).save(roCrate, mockTempFile.toString());
        }
    }


    @Test
    void testSerializeToZip_shouldThrowConvertExceptionOnIoFailure() {
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile(anyString(), anyString()))
                    .thenThrow(new IOException("Disk full"));

            ConvertException ex = assertThrows(ConvertException.class, () -> serializer.serializeToZip(roCrate));
            assertTrue(ex.getMessage().contains("Failed to serialize RO-Crate"));
        }
    }

    @Test
    void testSerializeToZip_shouldLogWarningOnDeleteFailure(@TempDir Path tempDir) throws Exception {
        Path mockTempFile = tempDir.resolve("ro-crate.zip");
        Files.writeString(mockTempFile, "fake zip content");

        try (MockedConstruction<ZipWriter> mockedZipWriter = mockConstruction(ZipWriter.class,
                (mock, context) -> doNothing().when(mock).save(any(RoCrate.class), eq(mockTempFile.toString())));
                var filesMock = mockStatic(Files.class, CALLS_REAL_METHODS)) {

            filesMock.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(mockTempFile);
            filesMock.when(() -> Files.readAllBytes(mockTempFile)).thenReturn("fake zip content".getBytes());

            filesMock.when(() -> Files.deleteIfExists(mockTempFile)).thenThrow(new IOException("Delete failed"));

            byte[] result = serializer.serializeToZip(roCrate);

            assertNotNull(result);
            assertEquals("fake zip content", new String(result));
        }
    }
}