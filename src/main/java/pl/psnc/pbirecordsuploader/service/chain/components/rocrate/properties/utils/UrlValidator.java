package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties.utils;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UrlValidator  {

    public boolean isValid(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null &&
                    (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}