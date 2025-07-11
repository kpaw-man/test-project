package pl.psnc.pbirecordsuploader.model.metadata;


public enum ContentProperties implements MetadataProperty {

    THUMBNAILS("thumbnailUrl", ""), // empty uri, takes default from rohub
    URL("url", ""), // empty uri, takes default from rohub
    CONTENT_URL("contentUrl", ""); // empty uri, takes default from rohub


    private final String key;
    private final String uri;

    ContentProperties(String key, String uri) {
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