package pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.Resource;

import java.io.IOException;

@Component
@Slf4j
public class KeyDictionary {
    private final Map<String, String> keywords = new LinkedHashMap<>();
    private final ObjectMapper objectMapper;
    private final String keywordMappingsPath;
    private final String keywordMappingsClasspath;

    public KeyDictionary(ObjectMapper objectMapper,
            @Value("${keywords.mappings.dctype.file-path:}") String keywordMappingsPath,
            @Value("${keywords.mappings.dctype.classpath:}") String keywordMappingsClasspath) {
        this.objectMapper = objectMapper;
        this.keywordMappingsPath = keywordMappingsPath;
        this.keywordMappingsClasspath = keywordMappingsClasspath;
    }

    @PostConstruct
    public void initialize() {
        loadKeywordMappings();
    }

    private void loadKeywordMappings() {
        if (keywordMappingsPath != null && !keywordMappingsPath.isBlank()) {
            try {
                File mappingsFile = new File(keywordMappingsPath);
                if (mappingsFile.exists()) {
                    Map<String, String> loadedKeywords = objectMapper.readValue(mappingsFile, new TypeReference<>() {
                    });
                    keywords.putAll(loadedKeywords);
                    log.info("Loaded {} keyword mappings from file: {}", keywords.size(), keywordMappingsPath);
                    return;
                } else {
                    log.warn("Keyword mappings file not found at: {}", keywordMappingsPath);
                }
            } catch (IOException e) {
                log.error("Error reading keyword mappings file at {}: {}", keywordMappingsPath, e.getMessage(), e);
                throw new IllegalStateException("Failed to load keyword mappings from file path", e);
            }
        }

        // Fallback: load from classpath
        if (keywordMappingsClasspath != null && !keywordMappingsClasspath.isBlank()) {
            try {
                Resource resource = new ClassPathResource(keywordMappingsClasspath);
                if (resource.exists()) {
                    Map<String, String> loadedKeywords = objectMapper.readValue(resource.getInputStream(),
                            new TypeReference<>() {
                            });
                    keywords.putAll(loadedKeywords);
                    log.info("Loaded {} keyword mappings from classpath: {}", keywords.size(),
                            keywordMappingsClasspath);
                } else {
                    log.error("Classpath resource not found: {}", keywordMappingsClasspath);
                    throw new IllegalStateException("Keyword mappings classpath resource not found");
                }
            } catch (IOException e) {
                log.error("Error reading keyword mappings from classpath {}: {}", keywordMappingsClasspath,
                        e.getMessage(), e);
                throw new IllegalStateException("Failed to load keyword mappings from classpath", e);
            }
        } else {
            log.error("No keyword mapping source provided (file path or classpath)");
            throw new IllegalStateException("Missing keyword mapping configuration");
        }
    }

    public String getMappedId(String keyword) {
        return keywords.get(keyword);
    }
}