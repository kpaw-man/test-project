package pl.psnc.pbirecordsuploader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class Descriptor {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("schema:name")
    private String name;

    public Descriptor(String id, String name) {
        this.id = id;
        this.name = name;
    }
}