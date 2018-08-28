/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.parser;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.SourceGen;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.TreeUtil;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.formatting.FormattingUtil;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

public class BallerinaParserServiceImpl implements BallerinaParserService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaParserService.class);

    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;

    public BallerinaParserServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public CompletableFuture<ParseContentReply> parseContent(ParseContentRequest request) {
        ParseContentReply reply = new ParseContentReply();
        try {
            reply.setModel(TreeUtil.getTreeForContent(request.getContent()));
            reply.setParseSuccess(true);
        } catch (LSCompilerException | JSONGenerationException e) {
            reply.setParseSuccess(false);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<ASTModifiedReply> astModified(ASTModifiedRequest request) {
        ASTModifiedReply reply = new ASTModifiedReply();
        String fileUri = request.getTextDocumentIdentifier().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            // calculate range to replace
            String fileContent = documentManager.getFileContent(compilationPath);
            String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            int totalLines = contentComponents.length;
            Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));

            // generate source for the new ast.
            JsonObject ast = request.getAst();
            SourceGen sourceGen = new SourceGen(0);
            sourceGen.build(ast, null, "CompilationUnit");
            String textEditContent = sourceGen.getSourceOf(ast, false, false);

            // create text edit
            TextEdit textEdit = new TextEdit(range, textEditContent);
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit(request.getTextDocumentIdentifier(),
                    Collections.singletonList(textEdit));
            workspaceEdit.setDocumentChanges(Collections.singletonList(textDocumentEdit));
            applyWorkspaceEditParams.setEdit(workspaceEdit);

            // update the document
            ballerinaLanguageServer.getClient().applyEdit(applyWorkspaceEditParams);
            reply.setContent(textEditContent);
        } catch (Exception e) {
            if (CommonUtil.LS_DEBUG_ENABLED) {
                String msg = e.getMessage();
                logger.error("Error while tree modification source gen" + ((msg != null) ? ": " + msg : ""), e);
            }
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }
}
