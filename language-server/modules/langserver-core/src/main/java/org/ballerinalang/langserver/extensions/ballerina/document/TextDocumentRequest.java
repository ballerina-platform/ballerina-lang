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

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Represents a request for a Ballerina Syntax Tree.
 *
 * @since 1.3.0
 */
public class TextDocumentRequest {

    private TextDocumentIdentifier textDocument;
    private Position position;

    public TextDocumentRequest() {
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return textDocument;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier textDocument) {
        this.textDocument = textDocument;
    }

}
