/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver;
/**
 * Enum for experimental server capabilities in language server.
 *
 * @since 1.1.0
 */
public enum Experimental {
    INTROSPECTION("introspection"),
    AST_PROVIDER("astProvider"),
    SHOW_TEXT_DOCUMENT("showTextDocument"),
    EXAMPLES_PROVIDER("examplesProvider"),
    API_EDITOR_PROVIDER("apiEditorProvider"),
    SEMANTIC_SYNTAX_HIGHLIGHTER("semanticSyntaxHighlighter"),
    SEMANTIC_SCOPES("semanticScopes");

    private final String value;

    Experimental(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
