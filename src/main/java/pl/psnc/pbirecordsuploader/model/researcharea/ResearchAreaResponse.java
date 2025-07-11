package pl.psnc.pbirecordsuploader.model.researcharea;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchAreaResponse {
    private Integer count;
    private String next;
    private String previous;
    private List<ResearchArea> results;
}