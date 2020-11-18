/*
  Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.ballerinalang.langserver.util;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.Position;

import java.util.Optional;


/**
 * Token capturing utils.
 *
 * @since 1.0
 */
public class TokensUtil {
    private TokensUtil() {
    }

    /**
     * Find the token at position.
     *
     * @return Token at position
     */
    public static Token findTokenAtPosition(DocumentServiceContext context, Position position)
            throws TokenOrSymbolNotFoundException {
        Optional<Document> document = context.workspace().document(context.filePath());
        if (document.isEmpty()) {
            throw new TokenOrSymbolNotFoundException("Couldn't find a valid document!");
        }
        TextDocument textDocument = document.get().textDocument();

        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        Token tokenAtPosition = ((ModulePartNode) document.get().syntaxTree().rootNode()).findToken(txtPos);

        if (tokenAtPosition == null) {
            throw new TokenOrSymbolNotFoundException("Couldn't find a valid identifier token at position!");
        }

        return tokenAtPosition;
    }
}
