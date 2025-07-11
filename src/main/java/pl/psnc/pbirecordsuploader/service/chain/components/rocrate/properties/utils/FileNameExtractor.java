package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class FileNameExtractor {
    public String extract(String url) {
        try {
            String path = URI.create(url).getPath();
            if (isValidPath(path)) {
                int lastSlash = path.lastIndexOf('/');
                if (isValidSlashPosition(lastSlash, path)) {
                    return path.substring(lastSlash + 1);
                }
            }
        } catch (IllegalArgumentException e) {
            log.debug("Invalid URL for filename extraction: '{}'", url);
        }
        return url;
    }

    private boolean isValidPath(String path) {
        return path != null && !path.isEmpty();
    }

    private boolean isValidSlashPosition(int lastSlash, String path) {
        return lastSlash >= 0 && lastSlash < path.length() - 1;
    }
}