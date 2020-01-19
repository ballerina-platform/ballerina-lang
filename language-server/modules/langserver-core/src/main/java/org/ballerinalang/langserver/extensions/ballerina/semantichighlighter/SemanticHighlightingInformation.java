/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.semantichighlighter;
/**
 * Represents the data model for Semantic Highlighting Information.
 *
 * @since 1.1.0
 */
class SemanticHighlightingInformation {
    private final int line;
    private final String token;

    public SemanticHighlightingInformation(int line, String token) {
        this.line = line;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public int getLine() {
        return line;
    }
}
