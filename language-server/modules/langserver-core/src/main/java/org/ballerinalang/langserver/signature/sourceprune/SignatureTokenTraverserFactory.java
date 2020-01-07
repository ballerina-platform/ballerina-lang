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
package org.ballerinalang.langserver.signature.sourceprune;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.sourceprune.SourcePruneContext;
import org.ballerinalang.langserver.sourceprune.TokenTraverser;
import org.ballerinalang.langserver.sourceprune.TokenTraverserFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for providing token traversers and related artifacts for 'completions' service operation.
 *
 * @since 1.1.1
 */
public class SignatureTokenTraverserFactory implements TokenTraverserFactory {
    private final SourcePruneContext sourcePruneCtx;
    private final boolean pruneTokens;
    private final TokenStream tokenStream;

    private static final List<Integer> BLOCK_REMOVE_KW_TERMINALS;
    private static final List<Integer> LHS_TRAVERSE_TERMINALS;
    private static final List<Integer> RHS_TRAVERSE_TERMINALS;

    static {
        BLOCK_REMOVE_KW_TERMINALS = Arrays.asList(
                BallerinaParser.SERVICE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.MATCH,
                BallerinaParser.FOREACH, BallerinaParser.WORKER
        );
        LHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE, BallerinaParser.SEMICOLON,
                BallerinaParser.COMMA, BallerinaParser.LEFT_PARENTHESIS, BallerinaParser.RIGHT_PARENTHESIS,
                BallerinaParser.LT, BallerinaParser.RETURNS, BallerinaParser.TRANSACTION,
                BallerinaParser.LEFT_CLOSED_RECORD_DELIMITER, BallerinaParser.LEFT_BRACKET,
                BallerinaParser.XMLTemplateText
        );
        RHS_TRAVERSE_TERMINALS = Arrays.asList(
                BallerinaParser.COMMA, BallerinaParser.SEMICOLON, BallerinaParser.DocumentationLineStart,
                BallerinaParser.AT, BallerinaParser.LEFT_BRACE, BallerinaParser.RIGHT_BRACE,
                BallerinaParser.RIGHT_PARENTHESIS, BallerinaParser.IMPORT, BallerinaParser.GT,
                BallerinaParser.XMLNS, BallerinaParser.SERVICE, BallerinaParser.PUBLIC, BallerinaParser.PRIVATE,
                BallerinaParser.REMOTE, BallerinaParser.FUNCTION, BallerinaParser.TYPE, BallerinaParser.ANNOTATION,
                BallerinaParser.CONST, BallerinaParser.RIGHT_BRACKET, BallerinaParser.RIGHT_CLOSED_RECORD_DELIMITER,
                BallerinaParser.RESOURCE, BallerinaParser.LISTENER, BallerinaParser.MATCH, BallerinaParser.IF,
                BallerinaParser.WHILE, BallerinaParser.FOREACH, BallerinaParser.BREAK, BallerinaParser.BREAK,
                BallerinaParser.FORK, BallerinaParser.THROW, BallerinaParser.TRANSACTION, BallerinaParser.WORKER
        );
    }

    public SignatureTokenTraverserFactory(Path filePath, WorkspaceDocumentManager documentManager,
                                          SourcePruneContext sourcePruneCtx) throws WorkspaceDocumentException {
        this.sourcePruneCtx = sourcePruneCtx;

        // Read file content
        String documentContent = documentManager.getFileContent(filePath);

        // Execute Ballerina Parser
        BallerinaParser parser = CommonUtil.prepareParser(documentContent);
        parser.compilationUnit();

        // If the number of errors are zero, then traverse the tokens without pruning the erroneous tokens with spaces
        this.pruneTokens = parser.getNumberOfSyntaxErrors() > 0;
        this.tokenStream = parser.getTokenStream();
    }

    @Override
    public TokenTraverser createLHSTokenTraverser() {
        return new LHSSignatureTokenTraverser(this.sourcePruneCtx, this.pruneTokens);
    }

    @Override
    public TokenTraverser createRHSTokenTraverser() {
        return new RHSSignatureTokenTraverser(this.sourcePruneCtx, this.pruneTokens);
    }

    @Override
    public SourcePruneContext getSourcePruneCtx() {
        return sourcePruneCtx;
    }

    @Override
    public TokenStream getTokenStream() {
        return tokenStream;
    }

    @Override
    public List<Integer> getBlockRemoveTerminals() {
        return BLOCK_REMOVE_KW_TERMINALS;
    }

    @Override
    public List<Integer> getLHSTraverseTerminals() {
        return LHS_TRAVERSE_TERMINALS;
    }

    @Override
    public List<Integer> getRHSTraverseTerminals() {
        return RHS_TRAVERSE_TERMINALS;
    }
}
