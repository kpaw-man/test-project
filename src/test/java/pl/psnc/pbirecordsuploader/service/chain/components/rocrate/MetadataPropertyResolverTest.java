package pl.psnc.pbirecordsuploader.service.chain.components.rocrate;

import org.junit.jupiter.api.Test;
import pl.psnc.pbirecordsuploader.model.metadata.ContentProperties;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;
import pl.psnc.pbirecordsuploader.model.metadata.MetadataProperty;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataPropertyResolverTest {

    @Test
    void shouldResolveDCTermProperty() {
        String key = "title"; // Exists in DCTerms

        Optional<MetadataProperty> result = MetadataPropertyResolver.fromKey(key);

        assertThat(result).isPresent();
        assertThat(result.get().key()).isEqualTo("title");
        assertThat(result.get()).isInstanceOf(DCTerms.class);
    }

    @Test
    void shouldResolveContentProperty() {
        String key = "thumbnailUrl"; // Exists in ContentProperties

        Optional<MetadataProperty> result = MetadataPropertyResolver.fromKey(key);

        assertThat(result).isPresent();
        assertThat(result.get().key()).isEqualTo("thumbnailUrl");
        assertThat(result.get()).isInstanceOf(ContentProperties.class);
    }

    @Test
    void shouldReturnEmptyForUnknownKey() {
        String key = "nonexistent";

        Optional<MetadataProperty> result = MetadataPropertyResolver.fromKey(key);

        assertThat(result).isEmpty();
    }
}