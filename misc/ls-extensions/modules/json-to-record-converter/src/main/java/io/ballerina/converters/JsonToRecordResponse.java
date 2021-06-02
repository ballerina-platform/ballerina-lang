package io.ballerina.converters;

/**
 * Response format for JsonToBalRecord endpoint.
 */
public class JsonToRecordResponse {
    private String codeBlock;

    public String getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(String codeBlock) {
        this.codeBlock = codeBlock;
    }
}
