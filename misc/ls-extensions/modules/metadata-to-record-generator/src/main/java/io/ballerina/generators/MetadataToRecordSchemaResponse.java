package io.ballerina.generators;

/**
 * Response format for salesforceSObjectSchemaToRecord endpoint.
 **/
public class MetadataToRecordSchemaResponse {
    private String codeBlock;

    public String getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(String codeBlock) {
        this.codeBlock = codeBlock;
    }
}

