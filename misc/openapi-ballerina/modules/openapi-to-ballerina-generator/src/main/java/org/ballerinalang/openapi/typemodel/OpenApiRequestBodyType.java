package org.ballerinalang.openapi.typemodel;

import java.util.Map;

public class OpenApiRequestBodyType {

    private Boolean required;
    private String $ref;
    private String type;
    private Map<String, OpenApiSchemaType> contentList;

    public Map<String, OpenApiSchemaType> getContentList() {
        return contentList;
    }

    public void setContentList(Map<String, OpenApiSchemaType> contentList) {
        this.contentList = contentList;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String get$ref() {
        return $ref;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
