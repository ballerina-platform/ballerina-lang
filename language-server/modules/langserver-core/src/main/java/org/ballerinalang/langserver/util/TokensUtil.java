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

import org.antlr.v4.runtime.Token;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.List;
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
     * Search the token at a given cursor position.
     *
     * @param tokenList List of tokens in the current compilation unit's source
     * @param cLine     Cursor line
     * @param cCol      cursor column
     * @return {@link Optional}
     */
    public static Optional<Token> searchTokenAtCursor(List<Token> tokenList, int cLine, int cCol,
                                                      boolean identifiersOnly) {
        if (tokenList.isEmpty()) {
            return Optional.empty();
        }
        int tokenIndex = tokenList.size() / 2;

        TokensUtil.TokenPosition position = locateTokenAtCursor(tokenList.get(tokenIndex), cLine, cCol,
                identifiersOnly);

        switch (position) {
            case ON:
                // found the token and return it
                return Optional.ofNullable(tokenList.get(tokenIndex));
            case LEFT:
                return searchTokenAtCursor(tokenList.subList(0, tokenIndex), cLine, cCol, identifiersOnly);
            default:
                return searchTokenAtCursor(tokenList.subList(tokenIndex + 1, tokenList.size()), cLine, cCol,
                        identifiersOnly);
        }
    }

    private static TokenPosition locateTokenAtCursor(Token token, int cLine, int cCol, boolean identifiersOnly) {
        int tokenLine = token.getLine() - 1;
        int tokenStartCol = token.getCharPositionInLine();
        int tokenEndCol = tokenStartCol +
                ((token.getText().equals("\r\n") || token.getText().equals("\n")) ? 0 : token.getText().length());
        
        if (identifiersOnly) {
            return locateIdentifierTokenAtCursor(token, cLine, cCol, tokenLine, tokenStartCol, tokenEndCol);
        }
        
        /*
        Token which is considered as the token at cursor is the token immediate before the cursor,
         where its end column is cursor column 
         */
        if (tokenLine == cLine && tokenStartCol < cCol && tokenEndCol >= cCol
                && token.getType() != BallerinaParser.NEW_LINE) {
            return TokenPosition.ON;
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol > tokenEndCol)) {
            return TokenPosition.RIGHT;
        } else {
            return TokenPosition.LEFT;
        }
    }
    
    private static TokenPosition locateIdentifierTokenAtCursor(Token token, int cLine, int cCol, int tokenLine,
                                                               int tokenStartCol, int tokenEndCol) {
        if (tokenLine == cLine && tokenStartCol <= cCol && tokenEndCol >= cCol
                && token.getType() == BallerinaParser.Identifier) {
            return TokenPosition.ON;
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol >= tokenEndCol)) {
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
