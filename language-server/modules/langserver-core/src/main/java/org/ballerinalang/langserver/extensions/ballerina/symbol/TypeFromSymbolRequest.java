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
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Represents a request to get type info given for given positions of symbols.
 */
public class TypeFromSymbolRequest {
    private TextDocumentIdentifier documentIdentifier;
    private LinePosition[] positions;

    protected LinePosition[] getPositions() {
        return positions;
    }

    public void setPositions(LinePosition[] positions) {
        this.positions = positions == null ? null : positions.clone();
    }

    public TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }
}
