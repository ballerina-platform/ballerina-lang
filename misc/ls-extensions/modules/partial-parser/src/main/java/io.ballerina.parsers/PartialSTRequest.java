/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.parsers;

/**
 * Request format for PartialParser endpoint.
 *
 * @since 2.0.0
 */
public class PartialSTRequest {

    private String codeSnippet;
    private STModification stModification;

    public PartialSTRequest(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public PartialSTRequest(String codeSnippet, STModification stModification) {
        this.codeSnippet = codeSnippet;
        this.stModification = stModification;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public STModification getStModification() {
        return stModification;
    }

    public void setStModification(STModification stModification) {
        this.stModification = stModification;
    }
}
