/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.formatting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Util class for formatting.
 */
public class FormattingVisitorEntry {
    private FormattingVisitor visitor;

    public FormattingVisitorEntry() {
        this.visitor = new FormattingVisitor();
    }

    /**
     * visit given json object and format.
     *
     * @param node ballerina node as a json object
     */
    public void accept(JsonObject node) {
        visitor.beginVisit(node);

        for (Map.Entry<String, JsonElement> child : node.entrySet()) {
            if (!child.getKey().equals("parent") && !child.getKey().equals("position") &&
                    !child.getKey().equals("ws")) {
                if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject().has("kind")) {
                    if (!(child.getValue().getAsJsonObject().has("skipFormatting") &&
                            child.getValue().getAsJsonObject().get("skipFormatting").getAsBoolean())) {
                        accept(child.getValue().getAsJsonObject());
                    }
                } else if (child.getValue().isJsonArray()) {
                    for (int i = 0; i < child.getValue().getAsJsonArray().size(); i++) {
                        JsonElement childItem = child.getValue().getAsJsonArray().get(i);
                        if (childItem.isJsonObject() && childItem.getAsJsonObject().has("kind")) {
                            if (!(childItem.getAsJsonObject().has("skipFormatting") &&
                                    childItem.getAsJsonObject().get("skipFormatting").getAsBoolean())) {
                                accept(childItem.getAsJsonObject());
                            }
                        }
                    }
                }
            }
        }

        visitor.endVisit(node);
    }
}
