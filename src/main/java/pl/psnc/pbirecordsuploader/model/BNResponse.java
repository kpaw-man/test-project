package pl.psnc.pbirecordsuploader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BNResponse {
    private String text;

    @JsonProperty("lemmatized_text")
    private String lemmatizedText;

    @JsonProperty("annif_result")
    private AnnifResult annifResult;

    @Data
    public static class AnnifResult {
        private List<Suggestion> results;
    }

    @Data
    public static class Suggestion {
        private String label;
        private String notation;
        private double score;
        private String uri;
    }
}