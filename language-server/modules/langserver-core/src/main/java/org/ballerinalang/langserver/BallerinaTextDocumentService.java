/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.codelenses.CodeLensUtil;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderHolder;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.completions.exceptions.CompletionContextNotSupportedException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.foldingrange.FoldingRangeProvider;
import org.ballerinalang.langserver.hover.HoverUtil;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.definition.DefinitionUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.LSClientLogger.logError;
import static org.ballerinalang.langserver.LSClientLogger.notifyUser;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {
    private final BallerinaLanguageServer languageServer;
    private LSClientCapabilities clientCapabilities;
    private final WorkspaceManager workspaceManager;

    BallerinaTextDocumentService(BallerinaLanguageServer languageServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
        this.languageServer = languageServer;
    }

    /**
     * Set the client capabilities.
     *
     * @param clientCapabilities Client's Text Document Capabilities
     */
    void setClientCapabilities(LSClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            CompletionContext context = ContextBuilder.buildCompletionContext(fileUri,
                    this.workspaceManager,
                    this.clientCapabilities.getTextDocCapabilities().getCompletion(),
                    position.getPosition());
            try {
                return LangExtensionDelegator.instance().completion(position, context);
            } catch (CompletionContextNotSupportedException e) {
                // Ignore the exception
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation 'text/completion' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
            }

            return Either.forLeft(Collections.emptyList());
        });
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            HoverContext context = ContextBuilder
                    .buildHoverContext(fileUri, this.workspaceManager, position.getPosition());
            Hover hover;
            try {
                hover = HoverUtil.getHover(context);
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation 'text/hover' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
                hover = HoverUtil.getDefaultHoverObject();
            }

            return hover;
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String uri = position.getTextDocument().getUri();
            Optional<Path> sigFilePath = CommonUtil.getPathFromURI(uri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (sigFilePath.isEmpty() || CommonUtil.isCachedExternalSource(uri)) {
                return new SignatureHelp();
            }

            SignatureContext context = ContextBuilder.buildSignatureContext(uri,
                    this.workspaceManager,
                    this.clientCapabilities.getTextDocCapabilities().getSignatureHelp(),
                    position.getPosition());
            try {
                // Find token at cursor position
                Token cursorToken = TokensUtil.findTokenAtPosition(context, position.getPosition());
                int activeParamIndex = 0;
                //TODO: Once https://git.io/JJIFp fixed, can get docs directly from the node of syntaxTree
                NonTerminalNode sNode = cursorToken.parent();
                SyntaxKind sKind = (sNode != null) ? sNode.kind() : null;

                // Find invocation node
                while (sNode != null &&
                        sKind != SyntaxKind.FUNCTION_CALL &&
                        sKind != SyntaxKind.METHOD_CALL &&
                        sKind != SyntaxKind.REMOTE_METHOD_CALL_ACTION &&
                        sKind != SyntaxKind.IMPLICIT_NEW_EXPRESSION &&
                        sKind != SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
                    sNode = sNode.parent();
                    sKind = (sNode != null) ? sNode.kind() : null;
                }

                if (sNode == null) {
                    throw new Exception("Couldn't find the invocation symbol!");
                }

                // Find parameter index
                int cLine = position.getPosition().getLine();
                int cCol = position.getPosition().getCharacter();
                for (Node child : sNode.children()) {
                    int sLine = child.lineRange().startLine().line();
                    int sCol = child.lineRange().startLine().offset();
                    if ((cLine == sLine && cCol < sCol) || (cLine < sLine)) {
                        break;
                    }
                    if (child.kind() == SyntaxKind.COMMA_TOKEN) {
                        activeParamIndex++;
                    }
                }

                // Search function invocation symbol
                List<SignatureInformation> signatures = new ArrayList<>();
                Optional<SignatureInformation> signatureInfo = SignatureHelpUtil.getSignatureInformation(context);
                signatureInfo.ifPresent(signatures::add);
                SignatureHelp signatureHelp = new SignatureHelp();
                signatureHelp.setActiveParameter(activeParamIndex);
                signatureHelp.setActiveSignature(0);
                signatureHelp.setSignatures(signatures);
                return signatureHelp;
            } catch (UserErrorException e) {
                notifyUser("Signature Help", e);
                return new SignatureHelp();
            } catch (Throwable e) {
                String msg = "Operation 'text/signature' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
                return new SignatureHelp();
            }
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition
            (TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentServiceContext defContext = ContextBuilder.buildBaseContext(position.getTextDocument().getUri(),
                        this.workspaceManager,
                        LSContextOperation.TXT_DEFINITION);
                return Either.forLeft(DefinitionUtil.getDefinition(defContext, position.getPosition()));
            } catch (UserErrorException e) {
                notifyUser("Goto Definition", e);
                return Either.forLeft(Collections.emptyList());
            } catch (Throwable e) {
                String msg = "Operation 'text/definition' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
                return Either.forLeft(Collections.emptyList());
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();

            // Note: If the source is a cached stdlib source, then return early and ignore
            if (CommonUtil.isCachedExternalSource(fileUri)) {
                return null;
            }

            try {
                return new ArrayList<>();
            } catch (UserErrorException e) {
                notifyUser("Find References", e);
                return new ArrayList<>();
            } catch (Throwable e) {
                String msg = "Operation 'text/references' failed!";
                logError(msg, e, params.getTextDocument(), params.getPosition());
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>>
    documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> docSymbolFilePath = CommonUtil.getPathFromURI(fileUri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (docSymbolFilePath.isEmpty() || CommonUtil.isCachedExternalSource(fileUri)) {
                return new ArrayList<>();
            }
            try {
                return new ArrayList<>();
            } catch (UserErrorException e) {
                notifyUser("Document Symbols", e);
                return new ArrayList<>();
            } catch (Throwable e) {
                String msg = "Operation 'text/documentSymbol' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            try {
                CodeActionContext context = ContextBuilder.buildCodeActionContext(fileUri, workspaceManager, params);
                return LangExtensionDelegator.instance().codeActions(params, context).stream()
                        .map((Function<CodeAction, Either<Command, CodeAction>>) Either::forRight)
                        .collect(Collectors.toList());
            } catch (UserErrorException e) {
                notifyUser("Code Action", e);
            } catch (Throwable e) {
                String msg = "Operation 'text/codeAction' failed!";
                Range range = params.getRange();
                logError(msg, e, params.getTextDocument(), range.getStart(), range.getEnd());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CodeLens> lenses;
            if (!LSCodeLensesProviderHolder.getInstance().isEnabled()) {
                // Disabled ballerina codeLens feature
                clientCapabilities.getTextDocCapabilities().setCodeLens(null);
                // Skip code lenses if codeLens disabled
                return new ArrayList<>();
            }

            String fileUri = params.getTextDocument().getUri();
            Optional<Path> docSymbolFilePath = CommonUtil.getPathFromURI(fileUri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (docSymbolFilePath.isEmpty() || CommonUtil.isCachedExternalSource(fileUri)) {
                return new ArrayList<>();
            }

            DocumentServiceContext codeLensContext = ContextBuilder.buildBaseContext(fileUri,
                    this.workspaceManager,
                    LSContextOperation.TXT_CODE_LENS);

            try {
                lenses = CodeLensUtil.getCodeLenses(codeLensContext);
                return lenses;
            } catch (UserErrorException e) {
                notifyUser("Code Lens", e);
                // Source compilation failed, serve from cache
            } catch (Throwable e) {
                String msg = "Operation 'text/codeLens' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                // Source compilation failed, serve from cache
            }

            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return CompletableFuture.supplyAsync(() -> {
            TextEdit textEdit = new TextEdit();
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> formattingFilePath = CommonUtil.getPathFromURI(fileUri);
            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (formattingFilePath.isEmpty() || CommonUtil.isCachedExternalSource(fileUri)) {
                return Collections.singletonList(textEdit);
            }
            try {
                CommonUtil.getPathFromURI(fileUri);
                Optional<Document> document = workspaceManager.document(formattingFilePath.get());
                if (document.isEmpty()) {
                    return new ArrayList<>();
                }
                SyntaxTree syntaxTree = document.get().syntaxTree();
                String formattedSource = Formatter.format(syntaxTree).toSourceCode();

                LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
                Range range = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                textEdit = new TextEdit(range, formattedSource);
                return Collections.singletonList(textEdit);
            } catch (UserErrorException | FormatterException e) {
                notifyUser("Formatting", e);
                return Collections.singletonList(textEdit);
            } catch (Throwable e) {
                String msg = "Operation 'text/formatting' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return Collections.singletonList(textEdit);
            }
        });
    }

    /**
     * The document range formatting request is sent from the client to the
     * server to format a given range in a document.
     * <p>
     * Registration Options: TextDocumentRegistrationOptions
     */
    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return CompletableFuture.supplyAsync(() -> {
            TextEdit textEdit = new TextEdit();
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> formattingFilePath = CommonUtil.getPathFromURI(fileUri);
            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (formattingFilePath.isEmpty() || CommonUtil.isCachedExternalSource(fileUri)) {
                return Collections.singletonList(textEdit);
            }
            try {
                CommonUtil.getPathFromURI(fileUri);
                Optional<Document> document = workspaceManager.document(formattingFilePath.get());
                if (document.isEmpty()) {
                    return new ArrayList<>();
                }
                SyntaxTree syntaxTree = document.get().syntaxTree();
                Range range = params.getRange();
                LinePosition startPos = LinePosition.from(range.getStart().getLine(), range.getStart().getCharacter());
                LinePosition endPos = LinePosition.from(range.getEnd().getLine(), range.getEnd().getCharacter());

                LineRange lineRange = LineRange.from(syntaxTree.filePath(), startPos, endPos);
                SyntaxTree formattedTree = Formatter.format(syntaxTree, lineRange);

                LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
                Range updateRange = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                textEdit = new TextEdit(updateRange, formattedTree.toSourceCode());
                return Collections.singletonList(textEdit);
            } catch (UserErrorException | FormatterException e) {
                notifyUser("Formatting", e);
                return Collections.singletonList(textEdit);
            } catch (Throwable e) {
                String msg = "Operation 'text/formatting' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return Collections.singletonList(textEdit);
            }
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            DocumentServiceContext context = ContextBuilder.buildBaseContext(fileUri, this.workspaceManager,
                    LSContextOperation.TXT_DID_OPEN);
            this.workspaceManager.didOpen(context.filePath(), params);
            LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_OPEN.getName() +
                    "' {fileUri: '" + fileUri + "'} opened}");
            DiagnosticsHelper.getInstance().compileAndSendDiagnostics(this.languageServer.getClient(), context);
        } catch (Throwable e) {
            String msg = "Operation 'text/didOpen' failed!";
            TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
            logError(msg, e, identifier, (Position) null);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            // Update content
            DocumentServiceContext context = ContextBuilder.buildBaseContext(fileUri,
                    this.workspaceManager,
                    LSContextOperation.TXT_DID_CHANGE);
            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (CommonUtil.isCachedExternalSource(fileUri)) {
                // TODO: Check whether still we need this check
                return;
            }
            workspaceManager.didChange(context.filePath(), params);
            LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CHANGE.getName() +
                    "' {fileUri: '" + fileUri + "'} updated}");
            DiagnosticsHelper.getInstance().compileAndSendDiagnostics(this.languageServer.getClient(), context);
        } catch (Throwable e) {
            String msg = "Operation 'text/didChange' failed!";
            logError(msg, e, params.getTextDocument(), (Position) null);
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            DocumentServiceContext context = ContextBuilder.buildBaseContext(fileUri,
                    this.workspaceManager,
                    LSContextOperation.TXT_DID_CLOSE);
            workspaceManager.didClose(context.filePath(), params);
            LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CLOSE.getName() +
                    "' {fileUri: '" + fileUri + "'} closed}");
        } catch (Throwable e) {
            String msg = "Operation 'text/didClose' failed!";
            logError(msg, e, params.getTextDocument(), (Position) null);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    @JsonRequest
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FoldingRangeContext foldingRangeContext = ContextBuilder.buildFoldingRangeContext(
                        params.getTextDocument().getUri(),
                        this.workspaceManager,
                        this.clientCapabilities.getTextDocCapabilities().getFoldingRange().getLineFoldingOnly());
                return FoldingRangeProvider.getFoldingRange(foldingRangeContext);
            } catch (Throwable e) {
                String msg = "Operation 'text/foldingRange' failed!";
                logError(msg, e, new TextDocumentIdentifier(params.getTextDocument().getUri()), (Position) null);
                return Collections.emptyList();
            }
        });
    }
}
