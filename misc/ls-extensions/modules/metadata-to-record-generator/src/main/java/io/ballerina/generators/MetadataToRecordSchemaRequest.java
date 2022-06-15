package io.ballerina.generators;

/**
 *  Request format for customRecordGenerator endpoint.
 */
public class MetadataToRecordSchemaRequest {
    private String metadata;
    private String schemaSource;
    private boolean isClosed;
    private boolean isRecordTypeDesc;

    public MetadataToRecordSchemaRequest(String jsonString, String schemaSource, boolean isClosed) {
        this.metadata = jsonString;
        this.schemaSource = schemaSource;
        this.isClosed = isClosed;

    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getSchemaSource() {
        return schemaSource;
    }

    public void setSchemaSource(String schemaSource) {
        this.schemaSource = schemaSource;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isRecordTypeDesc() {
        return isRecordTypeDesc;
    }

    public void setRecordTypeDesc(boolean recordTypeDesc) {
        isRecordTypeDesc = recordTypeDesc;
    }
}
