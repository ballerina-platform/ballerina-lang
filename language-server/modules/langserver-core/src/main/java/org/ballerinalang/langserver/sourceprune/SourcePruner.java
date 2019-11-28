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
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final List<Integer> LHS_TRAVERSE_TERMINALS;
    private static final List<Integer> RHS_TRAVERSE_TERMINALS;
    private static final List<Integer> BLOCK_REMOVE_KW_TERMINALS;
    
    static {
        LHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, BallerinaParser.SEMICOLON,
                BallerinaParser.COMMA, BallerinaParser.LEFT_PARENTHESIS, BallerinaParser.RIGHT_PARENTHESIS,
                BallerinaParser.LT, BallerinaParser.RETURNS, BallerinaParser.TRANSACTION,
                BallerinaParser.LEFT_CLOSED_RECORD_DELIMITER, BallerinaParser.LEFT_BRACKET
        );
        RHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.SEMICOLON, BallerinaParser.DocumentationLineStart,
                BallerinaParser.AT, BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, 
                BallerinaParser.RIGHT_PARENTHESIS, BallerinaParser.IMPORT, BallerinaParser.GT,
                BallerinaParser.XMLNS, BallerinaParser.SERVICE, BallerinaParser.PUBLIC, BallerinaParser.PRIVATE,
                BallerinaParser.REMOTE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.ANNOTATION,
                BallerinaParser.CONST, BallerinaParser.RIGHT_BRACKET, BallerinaParser.RIGHT_CLOSED_RECORD_DELIMITER,
                BallerinaParser.RESOURCE, BallerinaParser.LISTENER, BallerinaParser.MATCH, BallerinaParser.IF,
                BallerinaParser.WHILE, BallerinaParser.FOREACH, BallerinaParser.BREAK, BallerinaParser.BREAK,
                BallerinaParser.FORK, BallerinaParser.THROW, BallerinaParser.TRANSACTION, BallerinaParser.WORKER
        );
        BLOCK_REMOVE_KW_TERMINALS = Arrays.asList(
                BallerinaParser.SERVICE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.MATCH,
                BallerinaParser.FOREACH, BallerinaParser.WORKER
        );
    }

    /**
     * Prune source.
     *
     * @param lsContext LS Context
     * @throws SourcePruneException  when source prune fails
     * @throws WorkspaceDocumentException when reading file content fails
     */
    public static void pruneSource(LSContext lsContext) throws SourcePruneException, WorkspaceDocumentException {
        Position cursorPosition = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        WorkspaceDocumentManager documentManager = lsContext.get(CommonKeys.DOC_MANAGER_KEY);
        String uri = lsContext.get(DocumentServiceKeys.FILE_URI_KEY);

        if (cursorPosition == null || documentManager == null || uri == null) {
            throw new SourcePruneException("Cursor position, docManager and fileUri cannot be null!");
        }

        // Read file content
        Path path = Paths.get(URI.create(uri));
        String documentContent = documentManager.getFileContent(path);

        // Execute Ballerina Parser
        BallerinaParser parser = CommonUtil.prepareParser(documentContent);
        parser.compilationUnit();

        // Process tokens
        TokenStream tokenStream = parser.getTokenStream();
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

        // If the number of errors are zero, then traverse the tokens without pruning the erroneous tokens with spaces
        boolean pruneTokens = parser.getNumberOfSyntaxErrors() > 0;

        // Execute source pruning
        SourcePruneContext sourcePruneCtx = getContext();
        List<CommonToken> lhsTokens = new LHSTokenTraverser(sourcePruneCtx, pruneTokens)
                .traverseLHS(tokenStream, tokenIndex);
        List<CommonToken> rhsTokens = new RHSTokenTraverser(sourcePruneCtx, pruneTokens)
                .traverseRHS(tokenStream, tokenIndex + 1);
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

        // Update document manager
        documentManager.setPrunedContent(path, tokenStream.getText());
    }
    
    private static SourcePruneContext getContext() {
        SourcePruneContext context = new SourcePruneContext(LSContextOperation.SOURCE_PRUNER);
        context.put(SourcePruneKeys.GT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LT_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_BRACE_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LEFT_BRACKET_COUNT_KEY, 0);
        context.put(SourcePruneKeys.RIGHT_BRACKET_COUNT_KEY, 0);
        context.put(SourcePruneKeys.LHS_TRAVERSE_TERMINALS_KEY, LHS_TRAVERSE_TERMINALS);
        context.put(SourcePruneKeys.RHS_TRAVERSE_TERMINALS_KEY, RHS_TRAVERSE_TERMINALS);
        context.put(SourcePruneKeys.BLOCK_REMOVE_KW_TERMINALS_KEY, BLOCK_REMOVE_KW_TERMINALS);
        
        return context;
    }
}
