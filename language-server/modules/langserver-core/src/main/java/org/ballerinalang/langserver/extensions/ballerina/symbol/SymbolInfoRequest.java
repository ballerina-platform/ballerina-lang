/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
/**
 * Represents the request for getSymbol endpoint of BallerinaSymbolService.
 *
 * @since 2201.1.0
 */
public class SymbolInfoRequest {
    private TextDocumentIdentifier textDocumentIdentifier;
    private Position position;


    public TextDocumentIdentifier getDocumentIdentifier() {
        return textDocumentIdentifier;
    }

    public Position getPosition() {
        return position;
    }

    public Position setPosition(Position position) {
        return this.position = position;
    }

    public TextDocumentIdentifier setTextDocumentIdentifier(TextDocumentIdentifier textDocumentIdentifier) {
        return this.textDocumentIdentifier = textDocumentIdentifier;
    }
}
