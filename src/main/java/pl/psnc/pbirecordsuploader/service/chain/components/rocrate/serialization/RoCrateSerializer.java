package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoCrateSerializer {
    private final ObjectMapper objectMapper;

    /**
     * Serialize a RO-Crate object to a ZIP file byte array.
     *
     * @param roCrate The RO-Crate object to serialize
     * @return A byte array containing the zipped RO-Crate
     * @throws ConvertException If serialization fails
     */
    public byte[] serializeToZip(RoCrate roCrate) throws ConvertException {
        Path tempFile = null;
        try {
            if (log.isDebugEnabled()) {
                printCrate(roCrate);
            }
            tempFile = Files.createTempFile("ro-crate-", ".zip");
            new ZipWriter().save(roCrate, tempFile.toString());
            byte[] zipBytes = Files.readAllBytes(tempFile);
            log.debug("RO-Crate saved as byte array, size: {} bytes", zipBytes.length);
            return zipBytes;
        } catch (IOException e) {
            throw new ConvertException("Failed to serialize RO-Crate to ZIP format", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary file: {}", tempFile, e);
                }
            }
        }
    }

    private void printCrate(RoCrate roCrate) {
        try {
            String jsonMetadata = roCrate.getJsonMetadata();
            JsonNode jsonNode = objectMapper.readTree(jsonMetadata);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            log.debug("RO-Crate JSON-LD content:\n{}", prettyJson);
        } catch (IOException e) {
            log.warn("Failed to pretty-print RO-Crate JSON metadata", e);
        }
    }
}