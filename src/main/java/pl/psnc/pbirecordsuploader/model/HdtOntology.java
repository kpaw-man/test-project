package pl.psnc.pbirecordsuploader.model;

import lombok.Getter;

@Getter
public enum HdtOntology {

    HDT("hdt"),
    HDT_URI("https://hdt/1.0.0/"),
    HP10_IS_TOLD_BY("https://hdt/1.0.0/HP10_is_told_by"),
    HP10_TELLS_ABOUT("https://hdt/1.0.0/HP10_tells_about"),
    HP1_HAS_DIGITAL_TWIN("https://hdt/1.0.0/HP1_has_digital_twin"),
    HP1_IS_DIGITAL_TWIN_OF("https://hdt/1.0.0/HP1_is_digital_twin_of"),
    HP2_HAS_STORY("https://hdt/1.0.0/HP2_has_story"),
    HP2_IS_STORY_ABOUT("https://hdt/1.0.0/HP2_is_story_about"),
    HP3_HAS_DIGITAL_TWIN_COMPONENT("https://hdt/1.0.0/HP3_has_digital_twin_component"),
    HP3_IS_DIGITAL_TWIN_COMPONENT_OF("https://hdt/1.0.0/HP3_is_digital_twin_component_of"),
    HP4_IS_NARRATED_THROUGH("https://hdt/1.0.0/HP4_is_narrated_through"),
    HP4_NARRATES("https://hdt/1.0.0/HP4_narrates"),
    HP5_HAS_INTANGIBLE_ASPECT("https://hdt/1.0.0/HP5_has_intangible_aspect"),
    HP5_IS_INTANGIBLE_ASPECT_OF("https://hdt/1.0.0/HP5_is_intangible_aspect_of"),
    HP6_EVENT_IS_MANIFESTATION_OF("https://hdt/1.0.0/HP6_event_is_manifestation_of"),
    HP6_HAS_MANIFESTATION_EVENT("https://hdt/1.0.0/HP6_has_manifestation_event"),
    HP7_IS_MANIFESTATION_OF("https://hdt/1.0.0/HP7_is_manifestation_of"),
    HP7_IS_MANIFESTED_BY("https://hdt/1.0.0/HP7_is_manifested_by"),
    HP8_DOCUMENT_USED_FOR_NARRATION("https://hdt/1.0.0/HP8_document_used_for_narration"),
    HP8_IS_NARRATED_IN_DOCUMENT("https://hdt/1.0.0/HP8_is_narrated_in_document"),
    HP9_HAS_VISUAL_REPRESENTATION("https://hdt/1.0.0/HP9_has_visual_representation"),
    HP9_IS_VISUAL_REPRESENTATION_OF("https://hdt/1.0.0/HP9_is_visual_representation_of"),
    HDT_ENTITY("https://hdt/1.0.0/HDT_Entity"),
    HC1_HERITAGE_ENTITY("https://hdt/1.0.0/HC1_Heritage_Entity"),
    HC2_HERITAGE_DIGITAL_TWIN("https://hdt/1.0.0/HC2_Heritage_Digital_Twin"),
    HC3_TANGIBLE_ASPECT("https://hdt/1.0.0/HC3_Tangible_Aspect"),
    HC4_INTANGIBLE_ASPECT("https://hdt/1.0.0/HC4_Intangible_Aspect"),
    HC5_DIGITAL_REPRESENTATION("https://hdt/1.0.0/HC5_Digital_Representation"),
    HC6_DIGITAL_HERITAGE_DOCUMENT("https://hdt/1.0.0/HC6_Digital_Heritage_Document"),
    HC7_DIGITAL_VISUAL_OBJECT("https://hdt/1.0.0/HC7_Digital_Visual_Object"),
    HC8_3D_MODEL("https://hdt/1.0.0/HC8_3D_Model");

    private final String key;

    HdtOntology(String key) {
        this.key = key;
    }
}
