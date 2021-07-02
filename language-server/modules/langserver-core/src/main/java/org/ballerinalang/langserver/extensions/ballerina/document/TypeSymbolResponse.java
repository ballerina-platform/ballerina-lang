package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonElement;

public class TypeSymbolResponse {
    private JsonElement typeData;

    public JsonElement getTypeData() {
        return typeData;
    }

    public void setTypeData(JsonElement typeData) {
        this.typeData = typeData;
    }
}
