package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * Java representation for OpenApi request body.
 */
public class OpenApiRequestBodyType {

    private Boolean required;
    private String ref;
    private String type;
    private List<OpenApiSchemaType> contentList;

    public List<OpenApiSchemaType> getContentList() {
        return contentList;
    }

    public void setContentList(List<OpenApiSchemaType> contentList) {
        this.contentList = contentList;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getRequestBodyReference() {
        return ref;
    }

    public void setRequestBodyReference(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
