/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Represents a request for a Ballerina Syntax API Quote.
 *
 * @since 2.0.0
 */
public class SyntaxApiCallsRequest {

    private TextDocumentIdentifier documentIdentifier;
    private boolean ignoreMinutiae;

    public SyntaxApiCallsRequest() {
    }

    public SyntaxApiCallsRequest(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
        this.ignoreMinutiae = false;
    }

    public SyntaxApiCallsRequest(TextDocumentIdentifier documentIdentifier, boolean ignoreMinutiae) {
        this.documentIdentifier = documentIdentifier;
        this.ignoreMinutiae = ignoreMinutiae;
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public boolean getIgnoreMinutiae() {
        return ignoreMinutiae;
    }

    public void setIgnoreMinutiae(boolean ignoreMinutiae) {
        this.ignoreMinutiae = ignoreMinutiae;
    }
}
