/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonElement;

/**
 * Represents a Ballerina Syntax Tree response.
 *
 * @since 2201.3.0
 */
public class BallerinaSyntaxTreeResponse {

    private JsonElement syntaxTree;

    private String source;

    private String defFilePath;

    private boolean parseSuccess;

    public JsonElement getSyntaxTree() {
        return syntaxTree;
    }

    public void setSyntaxTree(JsonElement syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isParseSuccess() {
        return parseSuccess;
    }

    public void setParseSuccess(boolean parseSuccess) {
        this.parseSuccess = parseSuccess;
    }

    public void setDefFilePath(String defFilePath) {
        this.defFilePath = defFilePath;
    }

    public String getDefFilePath() {
        return defFilePath;
    }

}
