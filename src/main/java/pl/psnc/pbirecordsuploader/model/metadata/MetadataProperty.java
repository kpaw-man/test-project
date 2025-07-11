package pl.psnc.pbirecordsuploader.model.metadata;

import java.util.Arrays;
import java.util.Optional;

public interface MetadataProperty {
    String key();
    String uri();

    static <T extends Enum<T> & MetadataProperty> Optional<MetadataProperty> fromKey(Class<T> enumClass, String key) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.key().equals(key))
                .findFirst()
                .map(e -> e);
    }
}