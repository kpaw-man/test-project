package pl.psnc.pbirecordsuploader.model.researcharea;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchArea {
    private String identifier;
    private String name;
    private String description;
    private String term;
    @JsonProperty("parent_research_area")
    private String parentResearchArea;
    @JsonProperty("api_link")
    private String apiLink;
}