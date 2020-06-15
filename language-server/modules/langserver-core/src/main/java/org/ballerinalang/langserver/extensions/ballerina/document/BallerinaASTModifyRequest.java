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

import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public class BallerinaASTModifyRequest {

    private TextDocumentIdentifier documentIdentifier;
    private ASTModification[] astModifications;

    public BallerinaASTModifyRequest() {
    }

    public BallerinaASTModifyRequest(TextDocumentIdentifier documentIdentifier,
                                     ASTModification[] astModifications) {
        this.documentIdentifier = documentIdentifier;
        this.astModifications = astModifications;
    }

    public ASTModification[] getAstModifications() {
        return astModifications;
    }

    public void setAstModifications(ASTModification[] astModifications) {
        this.astModifications = astModifications;
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

}
