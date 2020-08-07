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

import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.TextDocument;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.nio.file.Path;
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
     * @throws WorkspaceDocumentException while retrieving the syntax tree from the document manager
     */
    public static Token findTokenAtPosition(LSContext context, Position position)
            throws WorkspaceDocumentException, TokenOrSymbolNotFoundException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (!filePath.isPresent()) {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " does not exists.");
        }
        SyntaxTree syntaxTree = docManager.getTree(filePath.get());
        TextDocument textDocument = syntaxTree.textDocument();

        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        Token tokenAtPosition =
                ((ModulePartNode) syntaxTree.rootNode()).findToken(
                        txtPos);

        if (tokenAtPosition == null) {
            throw new TokenOrSymbolNotFoundException("Couldn't find a valid identifier token at position!");
        }

        int tokenType;
        //TODO: Remove this, added for the backward compatibility of code
        NonTerminalNode nodeAtCursor = getNodeAtCursor(tokenAtPosition);
        switch (nodeAtCursor.kind()) {
            case COLON_TOKEN:
                tokenType = BallerinaParser.COLON;
                break;
            case RIGHT_ARROW_TOKEN:
                tokenType = BallerinaParser.RARROW;
                break;
            case DOT_TOKEN:
                tokenType = BallerinaParser.DOT;
                break;
            default:
                tokenType = -1;
                break;
        }
        context.put(NodeContextKeys.INVOCATION_TOKEN_TYPE_KEY, tokenType);
        return tokenAtPosition;
    }

    private static NonTerminalNode getNodeAtCursor(io.ballerinalang.compiler.syntax.tree.Token tokenAtCursor) {
        NonTerminalNode parent = tokenAtCursor.parent();

        while (!(parent instanceof ModuleMemberDeclarationNode) && !(parent instanceof StatementNode)) {
            parent = parent.parent();
        }

        return parent;
    }
}
