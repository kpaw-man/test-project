package pl.psnc.pbirecordsuploader.model;

import java.util.List;
import java.util.Map;

public record ExtractedXmlContent(Map<String, List<String>> properties) { }