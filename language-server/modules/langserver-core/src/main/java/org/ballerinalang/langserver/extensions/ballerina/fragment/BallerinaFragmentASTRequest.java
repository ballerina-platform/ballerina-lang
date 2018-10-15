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
package org.ballerinalang.langserver.extensions.ballerina.fragment;

/**
 * Represents a request for parsing a code fragment.
 *
 * @since 0.981.2
 */
public class BallerinaFragmentASTRequest {

    private String enclosingScope;

    private String expectedNodeType;

    private String source;

    public String getEnclosingScope() {
        return enclosingScope;
    }

    public void setEnclosingScope(String enclosingScope) {
        this.enclosingScope = enclosingScope;
    }

    public String getExpectedNodeType() {
        return expectedNodeType;
    }

    public void setExpectedNodeType(String expectedNodeType) {
        this.expectedNodeType = expectedNodeType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
