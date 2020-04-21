/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonObject;

/**
 * Pojo to modify AST.
 *
 * @since 1.3.0
 */
public class ASTModification {
    private int startOffset;
    private int endOffset;
    private String type;
    private JsonObject config;

    public ASTModification() {
    }

    public ASTModification(int startOffset, int endOffset, String type, JsonObject config) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.type = type;
        this.config = config;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject getConfig() {
        return config;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }
}
