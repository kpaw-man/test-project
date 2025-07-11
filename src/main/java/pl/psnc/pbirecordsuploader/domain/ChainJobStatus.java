package pl.psnc.pbirecordsuploader.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChainJobStatus {
    NEW(0),
    FETCHED_FROM_SOURCE(15),
    XSLT_TRANSFORMATION(20),
    EXTRACTED_XML_DATA(25),
    VALIDATED_FIELDS_FROM_SOURCE(30),
    CONVERTED_TO_DIRTY_RO(45),
    VALIDATED_RO(60),
    FORWARDED_RO(75),
    UPLOADED_ANNOTATIONS(85),
    DONE(100),
    FAILED(-1);

    private final int progress;

    @Override
    public String toString() {
        return this.name();
    }
}

