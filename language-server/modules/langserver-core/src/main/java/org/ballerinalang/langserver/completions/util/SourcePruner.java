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
package org.ballerinalang.langserver.completions.util;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Source Pruner utility class which used to prune the invalid sources.
 *
 * @since 0.995.0
 */
class SourcePruner {
    private static final List<Integer> LHS_TRAVERSE_TERMINALS;
    private static final List<Integer> RHS_TRAVERSE_TERMINALS;

    static {
        LHS_TRAVERSE_TERMINALS = Arrays.asList(BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE,
                BallerinaParser.SEMICOLON, BallerinaParser.COMMA);
        RHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, BallerinaParser.SEMICOLON,
                BallerinaParser.COMMA, BallerinaParser.IMPORT, BallerinaParser.XMLNS,
                BallerinaParser.DocumentationLineStart, BallerinaParser.AT, BallerinaParser.SERVICE,
                BallerinaParser.PUBLIC, BallerinaParser.PRIVATE, BallerinaParser.REMOTE, BallerinaParser.FUNCTION,
                BallerinaParser.TYPE, BallerinaParser.ANNOTATION, BallerinaParser.CONST
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
    static Optional<Token> searchTokenAtCursor(List<Token> tokenList, int cLine, int cCol) {
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
        traverseLHS(tokenStream, tokenIndex);
        traverseRHS(tokenStream, tokenIndex);
    }

    private static void traverseLHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            if (LHS_TRAVERSE_TERMINALS.contains(token.get().getType())) {
                handleLHSTerminalTokenText(token.get());
                break;
            }
            alterTokenText(token.get());
            token = CommonUtil.getPreviousDefaultToken(tokenStream, token.get().getTokenIndex());
        }
    }

    private static void traverseRHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            if (RHS_TRAVERSE_TERMINALS.contains(token.get().getType())) {
                handleRHSTerminalTokenText(token.get());
                break;
            }
            alterTokenText(token.get());
            token = CommonUtil.getNextDefaultToken(tokenStream, token.get().getTokenIndex());
        }
    }

    private static void handleRHSTerminalTokenText(Token token) {
        if (token.getType() == BallerinaParser.SEMICOLON || token.getType() == BallerinaParser.COMMA) {
            ((CommonToken) token).setText(getNCharLengthEmptyLine(token.getText().length()));
        }
    }

    private static void handleLHSTerminalTokenText(Token token) {
        if (token.getType() == BallerinaParser.COMMA) {
            ((CommonToken) token).setText(getNCharLengthEmptyLine(token.getText().length()));
        }
    }

    private static String getNCharLengthEmptyLine(int n) {
        return String.join("", Collections.nCopies(n, " "));
    }
    
    private static void alterTokenText(Token token) {
        if (token.getType() == BallerinaParser.NEW_LINE) {
            return;
        }
        ((CommonToken) token).setText(getNCharLengthEmptyLine(token.getText().length()));
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
        if (tokenLine == cLine && tokenEndCol == cCol && token.getType() != BallerinaParser.NEW_LINE) {
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
