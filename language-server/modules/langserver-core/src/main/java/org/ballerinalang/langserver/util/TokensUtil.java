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
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
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
     * @param context
     * @param tokenList List of tokens in the current compilation unit's source
     * @param cLine     Cursor line
     * @param cCol      cursor column
     * @return {@link Optional}
     */
    public static Optional<Token> searchTokenAtCursor(LSContext context,
                                                      List<Token> tokenList, int cLine, int cCol,
                                                      boolean identifiersOnly, boolean offsetAndTryNextBest) {
        if (tokenList.isEmpty()) {
            return Optional.empty();
        }
        int tokenIndex = tokenList.size() / 2;

        TokensUtil.TokenPosition position = locateTokenAtCursor(tokenList.get(tokenIndex), cLine, cCol,
                                                                identifiersOnly, offsetAndTryNextBest);

        switch (position) {
            case THIS_TOKEN:
                // found the token and return it
                return Optional.ofNullable(tokenList.get(tokenIndex));
            case NEXT_TOKEN:
                // Should only consider when offsetAndTryNextBest is set
                if (!offsetAndTryNextBest) {
                    return Optional.empty();
                }
                // Loop and find a matching token
                Token token = tokenList.get(tokenIndex + 1);
                while (token != null && token.getType() != BallerinaParser.Identifier) {
                    tokenIndex++;
                    token = (tokenList.size() > tokenIndex) ? tokenList.get(tokenIndex) : null;
                }
                if (token != null) {
                    // Offset cursor position to allow SymbolRefVisitor to mark this as cursor symbol.
                    TextDocumentPositionParams positionParams = context.get(DocumentServiceKeys.POSITION_KEY);
                    positionParams.setPosition(new Position(token.getLine() - 1, token.getCharPositionInLine()));
                    context.put(DocumentServiceKeys.POSITION_KEY, positionParams);
                }
                return Optional.ofNullable(token);
            case LEFT_SIDE:
                return searchTokenAtCursor(context, tokenList.subList(0, tokenIndex), cLine, cCol, identifiersOnly,
                                           offsetAndTryNextBest);
            default:
                return searchTokenAtCursor(context, tokenList.subList(tokenIndex + 1, tokenList.size()), cLine, cCol,
                                           identifiersOnly, offsetAndTryNextBest);
        }
    }

    private static TokenPosition locateTokenAtCursor(Token token, int cLine, int cCol, boolean identifiersOnly,
                                                     boolean tryNextBest) {
        int tokenLine = token.getLine() - 1;
        int tokenStartCol = token.getCharPositionInLine();
        int tokenEndCol = tokenStartCol +
                ((token.getText().equals("\r\n") || token.getText().equals("\n")) ? 0 : token.getText().length());

        if (identifiersOnly) {
            return locateIdentifierTokenAtCursor(token, cLine, cCol, tokenLine, tokenStartCol, tokenEndCol,
                                                 tryNextBest);
        }
        
        /*
        Token which is considered as the token at cursor is the token immediate before the cursor,
         where its end column is cursor column 
         */
        if (tokenLine == cLine && tokenStartCol < cCol && tokenEndCol >= cCol
                && token.getType() != BallerinaParser.NEW_LINE) {
            return TokenPosition.THIS_TOKEN;
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol > tokenEndCol)) {
            return TokenPosition.RIGHT_SIDE;
        } else {
            return TokenPosition.LEFT_SIDE;
        }
    }

    private static TokenPosition locateIdentifierTokenAtCursor(Token token, int cLine, int cCol, int tokenLine,
                                                               int tokenStartCol, int tokenEndCol,
                                                               boolean tryNextBest) {
        // NOTE: handles 'new' symbol specifically
        boolean isValid = token.getType() == BallerinaParser.Identifier ||
                CommonKeys.NEW_KEYWORD_KEY.equals(token.getText());
        if (tokenLine == cLine && tokenStartCol <= cCol && tokenEndCol >= cCol) {
            return (isValid) ? TokenPosition.THIS_TOKEN :
                    ((tryNextBest) ? TokenPosition.NEXT_TOKEN : TokenPosition.LEFT_SIDE);
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol >= tokenEndCol)) {
            return TokenPosition.RIGHT_SIDE;
        } else {
            return TokenPosition.LEFT_SIDE;
        }
    }

    private enum TokenPosition {
        THIS_TOKEN,
        NEXT_TOKEN,
        LEFT_SIDE,
        RIGHT_SIDE
    }
}
