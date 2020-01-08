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
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.util.TokensUtil.searchTokenAtCursor;

/**
 * Source Pruner utility class which used to prune the invalid sources.
 *
 * @since 0.995.0
 */
public class SourcePruner {
    /**
     * Prune source.
     *
     * @param lsContext LS Context
     * @param traverserFactory Token Traverser Factory
     * @throws SourcePruneException  when source prune fails
     */
    public static void pruneSource(LSContext lsContext,
                                   TokenTraverserFactory traverserFactory) throws SourcePruneException {
        TokenStream tokenStream = traverserFactory.getTokenStream();
        Position cursorPosition = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        if (cursorPosition == null) {
            throw new SourcePruneException("Cursor position cannot be null!");
        }

        // Process tokens
        List<Token> tokenList = new ArrayList<>(((CommonTokenStream) tokenStream).getTokens());
        Optional<Token> tokenAtCursor = searchTokenAtCursor(tokenList, cursorPosition.getLine(),
                                                            cursorPosition.getCharacter(), false);
        if (tokenAtCursor.isPresent() && tokenAtCursor.get().getText().startsWith("//")) {
            lsContext.put(DocumentServiceKeys.TERMINATE_OPERATION_KEY, true);
            return;
        }
        tokenAtCursor.ifPresent(token ->
                lsContext.put(SourcePruneKeys.CURSOR_TOKEN_INDEX_KEY, tokenList.indexOf(token)));
        lsContext.put(SourcePruneKeys.TOKEN_LIST_KEY, tokenList);

        // Validate cursor position
        int tokenIndex = tokenAtCursor.map(Token::getTokenIndex).orElse(-1);
        if (tokenIndex < 0 || tokenIndex >= tokenStream.size()) {
            return;
        }

        SourcePruneContext sourcePruneCtx = traverserFactory.getSourcePruneCtx();
        sourcePruneCtx.put(SourcePruneKeys.LHS_TRAVERSE_TERMINALS_KEY, traverserFactory.getLHSTraverseTerminals());
        sourcePruneCtx.put(SourcePruneKeys.RHS_TRAVERSE_TERMINALS_KEY, traverserFactory.getRHSTraverseTerminals());
        sourcePruneCtx.put(SourcePruneKeys.BLOCK_REMOVE_KW_TERMINALS_KEY, traverserFactory.getBlockRemoveTerminals());

        // Execute source pruning
        List<CommonToken> lhsTokens = traverserFactory.createLHSTokenTraverser().traverse(tokenStream, tokenIndex);
        List<CommonToken> rhsTokens = traverserFactory.createRHSTokenTraverser().traverse(tokenStream, tokenIndex + 1);
        List<CommonToken> lhsDefaultTokens = lhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        List<CommonToken> rhsDefaultTokens = rhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        List<Integer> lhsDefaultTokenTypes = lhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        List<Integer> rhsDefaultTokenTypes = rhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        lsContext.put(CompletionKeys.LHS_TOKENS_KEY, lhsTokens);
        lsContext.put(CompletionKeys.LHS_DEFAULT_TOKENS_KEY, lhsDefaultTokens);
        lsContext.put(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY, lhsDefaultTokenTypes);
        lsContext.put(CompletionKeys.RHS_TOKENS_KEY, rhsTokens);
        lsContext.put(CompletionKeys.RHS_DEFAULT_TOKENS_KEY, rhsDefaultTokens);
        lsContext.put(CompletionKeys.RHS_DEFAULT_TOKEN_TYPES_KEY, rhsDefaultTokenTypes);
        lsContext.put(CompletionKeys.FORCE_REMOVED_STATEMENT_WITH_PARENTHESIS_KEY,
                      sourcePruneCtx.get(SourcePruneKeys.FORCE_CAPTURED_STATEMENT_WITH_PARENTHESIS_KEY));
    }

    public static SourcePruneContext newContext() {
        SourcePruneContext context = new SourcePruneContext(LSContextOperation.SOURCE_PRUNER);
        context.put(SourcePruneKeys.GT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_BRACKET_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_BRACKET_COUNT_KEY, 0);
        return context;
    }
}
