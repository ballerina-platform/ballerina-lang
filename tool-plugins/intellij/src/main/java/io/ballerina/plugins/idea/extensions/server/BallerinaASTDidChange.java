/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.plugins.idea.extensions.server;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

/**
 * Represents an AST change notification sent from client to server.
 */
public class BallerinaASTDidChange {

    JsonObject ast;

    VersionedTextDocumentIdentifier textDocumentIdentifier;

    public JsonObject getAst() {
        return ast;
    }

    public void setAst(JsonObject ast) {
        this.ast = ast;
    }

    public VersionedTextDocumentIdentifier getTextDocumentIdentifier() {
        return textDocumentIdentifier;
    }

    public void setTextDocumentIdentifier(VersionedTextDocumentIdentifier textDocumentIdentifier) {
        this.textDocumentIdentifier = textDocumentIdentifier;
    }
}
