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
package org.ballerinalang.langserver.sourceprune;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Source Pruner utility class which used to prune the invalid sources.
 *
 * @since 0.995.0
 */
public class SourcePruner {
    private static final List<Integer> LHS_TRAVERSE_TERMINALS;
    private static final List<Integer> RHS_TRAVERSE_TERMINALS;
    private static final List<Integer> DEFINITION_KW_TERMINALS;
    
    static {
        LHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, BallerinaParser.SEMICOLON,
                BallerinaParser.COMMA, BallerinaParser.LEFT_PARENTHESIS, BallerinaParser.RIGHT_PARENTHESIS,
                BallerinaParser.LT
        );
        RHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.SEMICOLON, BallerinaParser.COMMA, BallerinaParser.DocumentationLineStart,
                BallerinaParser.AT, BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, 
                BallerinaParser.RIGHT_PARENTHESIS, BallerinaParser.IMPORT, BallerinaParser.GT,
                BallerinaParser.XMLNS, BallerinaParser.SERVICE, BallerinaParser.PUBLIC, BallerinaParser.PRIVATE,
                BallerinaParser.REMOTE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.ANNOTATION,
                BallerinaParser.CONST
        );
        DEFINITION_KW_TERMINALS = Arrays.asList(
                BallerinaParser.SERVICE, BallerinaParser.FUNCTION, BallerinaParser.TYPE
        );
    }
    /**
     * Search the token at a given cursor position.
     *
     * @param tokenList List of tokens in the current compilation unit's source
     * @param cLine     Cursor line
     * @param cCol      cursor column
     * @return {@link Optional<Token>}
     */
    public static Optional<Token> searchTokenAtCursor(List<Token> tokenList, int cLine, int cCol) {
        if (tokenList.isEmpty()) {
            return Optional.empty();
        }
        int tokenIndex = tokenList.size() / 2;

        TokenPosition position = locateCursorAtToken(tokenList.get(tokenIndex), cLine, cCol);

        switch (position) {
            case ON:
                // found the token and return it
                return Optional.ofNullable(tokenList.get(tokenIndex));
            case LEFT:
                return searchTokenAtCursor(tokenList.subList(0, tokenIndex), cLine, cCol);
            default:
                return searchTokenAtCursor(tokenList.subList(tokenIndex + 1, tokenList.size()), cLine, cCol);
        }
    }

    public static void pruneSource(TokenStream tokenStream, int tokenIndex) {
        if (tokenIndex < 0 || tokenIndex >= tokenStream.size()) {
            return;
        }
        LHSTokenTraverser lhsTokenTraverser = new LHSTokenTraverser(LHS_TRAVERSE_TERMINALS, DEFINITION_KW_TERMINALS);
        lhsTokenTraverser.traverseLHS(tokenStream, tokenIndex);
        int lhsLTTokenCount = lhsTokenTraverser.getLessThanSymbolCount();
        new RHSTokenTraverser(RHS_TRAVERSE_TERMINALS, lhsTokenTraverser.isRemoveDefinition(), lhsLTTokenCount)
                .traverseRHS(tokenStream, tokenIndex);
    }

    private static TokenPosition locateCursorAtToken(Token token, int cLine, int cCol) {
        int tokenLine = token.getLine() - 1;
        int tokenStartCol = token.getCharPositionInLine();
        int tokenEndCol = tokenStartCol +
                ((token.getText().equals("\r\n") || token.getText().equals("\n")) ? 0 : token.getText().length());
        
        /*
        Token which is considered as the token at cursor is the token immediate before the cursor,
         where its end column is cursor column 
         */
        if (tokenLine == cLine && tokenStartCol < cCol && tokenEndCol >= cCol && token.getType() != BallerinaParser.NEW_LINE) {
            return TokenPosition.ON;
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol > tokenEndCol)) {
            return TokenPosition.RIGHT;
        } else {
            return TokenPosition.LEFT;
        }
    }

    private enum TokenPosition {
        LEFT,
        ON,
        RIGHT
    }
}
