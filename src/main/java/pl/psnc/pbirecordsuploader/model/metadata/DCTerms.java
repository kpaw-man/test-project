package pl.psnc.pbirecordsuploader.model.metadata;


public enum DCTerms implements MetadataProperty{
    TITLE("title", "http://purl.org/dc/terms/title"),
    CREATOR("creator", "http://purl.org/dc/terms/creator"),
    CONTRIBUTOR("contributor", "http://purl.org/dc/terms/contributor"),
    IDENTIFIER("identifier", "http://purl.org/dc/terms/identifier"),
    SUBJECT("subject", "http://purl.org/dc/terms/subject"),
    RELATION("relation", "http://purl.org/dc/terms/relation"),
    FORMAT("format", "http://purl.org/dc/terms/format"),
    SOURCE("source", "http://purl.org/dc/terms/source"),
    TYPE("type", "http://purl.org/dc/terms/type"),
    SPATIAL("spatial", "http://purl.org/dc/terms/spatial"),
    RIGHTS("rights", "http://purl.org/dc/terms/rights"),
    CREATED("created", "http://purl.org/dc/terms/created"),
    ISSUED("issued", "http://purl.org/dc/terms/issued"),
    DESCRIPTION("description", "http://purl.org/dc/terms/description"),
    LICENSE("license", "http://purl.org/dc/terms/license"),
    LANGUAGE("language", "http://purl.org/dc/terms/language"),
    DATE("date", "http://purl.org/dc/terms/date"),
    ACCESS_RIGHTS("accessRights", "http://purl.org/dc/terms/accessRights"),
    COVERAGE("coverage", "http://purl.org/dc/terms/coverage"),
    PUBLISHER("publisher", "http://purl.org/dc/terms/publisher"),
    ABSTRACT("abstract", "http://purl.org/dc/terms/abstract"),
    ACCRUAL_METHOD("accrualMethod", "http://purl.org/dc/terms/accrualMethod"),
    ACCRUAL_PERIODICITY("accrualPeriodicity", "http://purl.org/dc/terms/accrualPeriodicity"),
    ACCRUAL_POLICY("accrualPolicy", "http://purl.org/dc/terms/accrualPolicy"),
    ALTERNATIVE("alternative", "http://purl.org/dc/terms/alternative"),
    AUDIENCE("audience", "http://purl.org/dc/terms/audience"),
    AVAILABLE("available", "http://purl.org/dc/terms/available"),
    BIBLIOGRAPHIC_CITATION("bibliographicCitation", "http://purl.org/dc/terms/bibliographicCitation"),
    CONFORMS_TO("conformsTo", "http://purl.org/dc/terms/conformsTo"),
    COVERAGE_TEMPORAL("temporal", "http://purl.org/dc/terms/temporal"),
    DATE_ACCEPTED("dateAccepted", "http://purl.org/dc/terms/dateAccepted"),
    DATE_COPYRIGHTED("dateCopyrighted", "http://purl.org/dc/terms/dateCopyrighted"),
    DATE_SUBMITTED("dateSubmitted", "http://purl.org/dc/terms/dateSubmitted"),
    EDUCATION_LEVEL("educationLevel", "http://purl.org/dc/terms/educationLevel"),
    EXTENT("extent", "http://purl.org/dc/terms/extent"),
    HAS_FORMAT("hasFormat", "http://purl.org/dc/terms/hasFormat"),
    HAS_PART("hasPart", "http://purl.org/dc/terms/hasPart"),
    HAS_VERSION("hasVersion", "http://purl.org/dc/terms/hasVersion"),
    INSTRUCTIONAL_METHOD("instructionalMethod", "http://purl.org/dc/terms/instructionalMethod"),
    IS_FORMAT_OF("isFormatOf", "http://purl.org/dc/terms/isFormatOf"),
    IS_PART_OF("isPartOf", "http://purl.org/dc/terms/isPartOf"),
    IS_REFERENCED_BY("isReferencedBy", "http://purl.org/dc/terms/isReferencedBy"),
    IS_REPLACED_BY("isReplacedBy", "http://purl.org/dc/terms/isReplacedBy"),
    IS_REQUIRED_BY("isRequiredBy", "http://purl.org/dc/terms/isRequiredBy"),
    ISSUED_DATE("issued", "http://purl.org/dc/terms/issued"),
    IS_VERSION_OF("isVersionOf", "http://purl.org/dc/terms/isVersionOf"),
    MEDIATOR("mediator", "http://purl.org/dc/terms/mediator"),
    MEDIUM("medium", "http://purl.org/dc/terms/medium"),
    MODIFIED("modified", "http://purl.org/dc/terms/modified"),
    PROVENANCE("provenance", "http://purl.org/dc/terms/provenance"),
    REFERENCES("references", "http://purl.org/dc/terms/references"),
    REPLACES("replaces", "http://purl.org/dc/terms/replaces"),
    REQUIRES("requires", "http://purl.org/dc/terms/requires"),
    RIGHTS_HOLDER("rightsHolder", "http://purl.org/dc/terms/rightsHolder"),
    TABLE_OF_CONTENTS("tableOfContents", "http://purl.org/dc/terms/tableOfContents"),
    VALID("valid", "http://purl.org/dc/terms/valid");

    private final String key;
    private final String uri;

    DCTerms(String key, String uri) {
        this.key = key;
        this.uri = uri;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String uri() {
        return uri;
    }
}