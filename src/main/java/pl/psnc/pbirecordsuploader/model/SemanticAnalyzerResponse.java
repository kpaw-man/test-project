package pl.psnc.pbirecordsuploader.model;

import java.util.List;

public record SemanticAnalyzerResponse(String best_match,
                                       double similarity_score,
                                       List<List<Object>> top_matches) {
}