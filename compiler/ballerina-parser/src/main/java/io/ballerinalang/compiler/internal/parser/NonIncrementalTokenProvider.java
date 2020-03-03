/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.text.TextDocument;

import java.util.ArrayList;
import java.util.List;

public class NonIncrementalTokenProvider extends NodeAndTokenProvider {

    private final BallerinaLexer lexer;

    /**
     * The current token that the parser is looking at.
     */
    private STToken currentToken;

    /**
     * The lexed token count.
     */
    // TODO the following fields are required to implement peek() method
    private int tokenCount;

    /**
     * The index of the currentToken in tokens list.
     */
    private int currentTokenIndex;

    // TODO Can we find a better alternative than a list.
    private final List<STToken> tokens;

    public NonIncrementalTokenProvider(TextDocument textDocument) {
        this.lexer = new BallerinaLexer(textDocument.getCharacterReader());
        this.tokens = new ArrayList<>();
    }

    @Override
    public STToken getCurrentToken() {
        if (currentToken != null) {
            return currentToken;
        }

        lexNewToken();
        return currentToken;
    }

    @Override
    public STToken consumeToken(SyntaxKind expectedKind) {
        if (currentToken == null) {
            lexNewToken();
        }

        STToken foundToken = currentToken;
        if (currentToken.kind == expectedKind) {
            currentToken = null;
            return foundToken;
        }

        // Create a missing token and report an error
        return createMissingToken(expectedKind, currentToken.kind, true);
    }

    @Override
    public STToken peekToken(int noOfTokens) {
        throw new UnsupportedOperationException();
    }

    @Override
    public STNode getCurrentNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public STNode consumeNode() {
        throw new UnsupportedOperationException();
    }

    private void lexNewToken() {
        currentToken = lexer.lexSyntaxToken();
        tokens.add(currentToken);
        tokenCount++;
    }
}
