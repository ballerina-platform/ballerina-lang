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

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
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
    private static final List<Integer> BLOCK_REMOVE_KW_TERMINALS;
    
    static {
        LHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, BallerinaParser.SEMICOLON,
                BallerinaParser.COMMA, BallerinaParser.LEFT_PARENTHESIS, BallerinaParser.RIGHT_PARENTHESIS,
                BallerinaParser.LT, BallerinaParser.RETURNS, BallerinaParser.TRANSACTION,
                BallerinaParser.LEFT_CLOSED_RECORD_DELIMITER
        );
        RHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.SEMICOLON, BallerinaParser.DocumentationLineStart,
                BallerinaParser.AT, BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, 
                BallerinaParser.RIGHT_PARENTHESIS, BallerinaParser.IMPORT, BallerinaParser.GT,
                BallerinaParser.XMLNS, BallerinaParser.SERVICE, BallerinaParser.PUBLIC, BallerinaParser.PRIVATE,
                BallerinaParser.REMOTE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.ANNOTATION,
                BallerinaParser.CONST, BallerinaParser.RIGHT_BRACKET, BallerinaParser.RIGHT_CLOSED_RECORD_DELIMITER,
                BallerinaParser.RESOURCE, BallerinaParser.LISTENER
        );
        BLOCK_REMOVE_KW_TERMINALS = Arrays.asList(
                BallerinaParser.SERVICE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.MATCH,
                BallerinaParser.FOREACH, BallerinaParser.WORKER
        );
    }
    /**
     * Search the token at a given cursor position.
     *
     * @param tokenList List of tokens in the current compilation unit's source
     * @param cLine     Cursor line
     * @param cCol      cursor column
     * @return {@link Optional}
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

    public static void pruneSource(TokenStream tokenStream, int tokenIndex, LSContext lsContext) {
        if (tokenIndex < 0 || tokenIndex >= tokenStream.size()) {
            return;
        }
        SourcePruneContext sourcePruneCtx = getContext();
        List<CommonToken> lhsTokens = new LHSTokenTraverser(sourcePruneCtx).traverseLHS(tokenStream, tokenIndex);
        List<CommonToken> rhsTokens = new RHSTokenTraverser(sourcePruneCtx).traverseRHS(tokenStream, tokenIndex + 1);
        lsContext.put(CompletionKeys.LHS_TOKENS_KEY, lhsTokens);
        lsContext.put(CompletionKeys.RHS_TOKENS_KEY, rhsTokens);
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
        if (tokenLine == cLine && tokenStartCol < cCol && tokenEndCol >= cCol
                && token.getType() != BallerinaParser.NEW_LINE) {
            return TokenPosition.ON;
        } else if (cLine > tokenLine || (tokenLine == cLine && cCol > tokenEndCol)) {
            return TokenPosition.RIGHT;
        } else {
            return TokenPosition.LEFT;
        }
    }
    
    private static SourcePruneContext getContext() {
        SourcePruneContext context = new SourcePruneContext();
        context.put(SourcePruneKeys.GT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LHS_TRAVERSE_TERMINALS_KEY, LHS_TRAVERSE_TERMINALS);
        context.put(SourcePruneKeys.RHS_TRAVERSE_TERMINALS_KEY, RHS_TRAVERSE_TERMINALS);
        context.put(SourcePruneKeys.BLOCK_REMOVE_KW_TERMINALS_KEY, BLOCK_REMOVE_KW_TERMINALS);
        
        return context;
    }

    private enum TokenPosition {
        LEFT,
        ON,
        RIGHT
    }
}
