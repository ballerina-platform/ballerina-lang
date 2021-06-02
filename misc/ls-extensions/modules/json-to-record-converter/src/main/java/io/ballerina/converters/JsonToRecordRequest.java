package io.ballerina.converters;

/**
 * Request format for JsonToBalRecord endpoint.
 */
public class JsonToRecordRequest {
    private String jsonString;

    public JsonToRecordRequest(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
