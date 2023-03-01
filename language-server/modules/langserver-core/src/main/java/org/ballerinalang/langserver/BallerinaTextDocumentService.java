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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Module;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.codelenses.CodeLensUtil;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderHolder;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.BallerinaDefinitionContext;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.DocumentSymbolContext;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.PrepareRenameContext;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.commons.RenameContext;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.definition.DefinitionUtil;
import org.ballerinalang.langserver.documentsymbol.DocumentSymbolUtil;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.foldingrange.FoldingRangeProvider;
import org.ballerinalang.langserver.hover.HoverUtil;
import org.ballerinalang.langserver.references.ReferencesUtil;
import org.ballerinalang.langserver.rename.RenameUtil;
import org.ballerinalang.langserver.semantictokens.SemanticTokensUtils;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManagerProxy;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
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
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {

    private final BallerinaLanguageServer languageServer;
    private LSClientCapabilities clientCapabilities;
    private final BallerinaWorkspaceManagerProxy workspaceManagerProxy;
    private final LanguageServerContext serverContext;
    private final LSClientLogger clientLogger;

    BallerinaTextDocumentService(BallerinaLanguageServer languageServer,
                                 BallerinaWorkspaceManagerProxy workspaceManagerProxy,
                                 LanguageServerContext serverContext) {
        this.workspaceManagerProxy = workspaceManagerProxy;
        this.languageServer = languageServer;
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(this.serverContext);
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
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String fileUri = position.getTextDocument().getUri();
            CompletionContext context = ContextBuilder.buildCompletionContext(fileUri,
                    this.workspaceManagerProxy.get(fileUri),
                    this.clientCapabilities.getTextDocCapabilities().getCompletion(),
                    this.serverContext,
                    position.getPosition());
            try {
                return LangExtensionDelegator.instance()
                        .completion(position, context, this.serverContext, cancelChecker);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation 'text/completion' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_COMPLETION, msg, e, position.getTextDocument(),
                        position.getPosition());
            }

            return Either.forLeft(Collections.emptyList());
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                HoverContext context = ContextBuilder.buildHoverContext(
                        PathUtil.convertUriSchemeFromBala(params.getTextDocument().getUri()),
                        this.workspaceManagerProxy.get(),
                        this.serverContext, params.getPosition(),
                        cancelChecker);
                return HoverUtil.getHover(context);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation 'text/hover' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_HOVER, msg, e, params.getTextDocument(),
                        params.getPosition());
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String uri = params.getTextDocument().getUri();
            Optional<Path> sigFilePath = PathUtil.getPathFromURI(uri);

            // Note: If the path does not exist, then return early and ignore
            if (sigFilePath.isEmpty()) {
                return new SignatureHelp();
            }

            SignatureContext context = ContextBuilder.buildSignatureContext(uri,
                    this.workspaceManagerProxy.get(uri),
                    this.clientCapabilities.getTextDocCapabilities().getSignatureHelp(),
                    this.serverContext,
                    params.getPosition(),
                    cancelChecker);
            try {
                // Find token at cursor position
                return SignatureHelpUtil.getSignatureHelp(context);
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Signature Help", e);
            } catch (CancellationException ignore) {
                // ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/signature' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_SIGNATURE, msg, e, params.getTextDocument(),
                        params.getPosition());
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition
            (DefinitionParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                BallerinaDefinitionContext defContext = ContextBuilder.buildDefinitionContext(
                        PathUtil.convertUriSchemeFromBala(params.getTextDocument().getUri()),
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        params.getPosition(),
                        cancelChecker);
                return Either.forLeft(DefinitionUtil.getDefinition(defContext, params.getPosition()));
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Goto Definition", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation Exception
            } catch (Throwable e) {
                String msg = "Operation 'text/definition' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_DEFINITION, msg, e, params.getTextDocument(),
                        params.getPosition());
            }

            return Either.forLeft(Collections.emptyList());
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                String fileUri = params.getTextDocument().getUri();
                ReferencesContext context = ContextBuilder.buildReferencesContext(
                        PathUtil.convertUriSchemeFromBala(fileUri),
                        this.workspaceManagerProxy.get(fileUri),
                        this.serverContext,
                        params.getPosition(),
                        cancelChecker);

                Map<Module, List<io.ballerina.tools.diagnostics.Location>> referencesMap =
                        ReferencesUtil.getReferences(context);

                context.checkCancelled();
                List<Location> references = new ArrayList<>();
                referencesMap.forEach((module, locations) ->
                        locations.forEach(location -> {
                            Path filePath = PathUtil.getPathFromLocation(module, location);
                            String uri = filePath.toUri().toString();
                            // If path is readonly, change the URI scheme
                            if (PathUtil.isWriteProtectedPath(filePath)) {
                                try {
                                    uri = PathUtil.getBalaUriForPath(serverContext, filePath);
                                } catch (URISyntaxException e) {
                                    this.clientLogger.logError(LSContextOperation.TXT_REFERENCES,
                                            "Failed to convert path to bala URI", e,
                                            params.getTextDocument(), params.getPosition());
                                }
                            }
                            references.add(new Location(uri, PathUtil.getRange(location)));
                        }));

                return references;
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Find References", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/references' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_REFERENCES, msg, e, params.getTextDocument(),
                        params.getPosition());
            }

            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>>
    documentSymbol(DocumentSymbolParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> docSymbolFilePath = PathUtil.getPathFromURI(fileUri);

            // Note: If the path does not exist, then return early and ignore
            if (docSymbolFilePath.isEmpty()) {
                return new ArrayList<>();
            }
            try {
                DocumentSymbolContext context = ContextBuilder.buildDocumentSymbolContext(params,
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        this.clientCapabilities);
                return DocumentSymbolUtil.documentSymbols(context);
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Document Symbols", e);
                return new ArrayList<>();
            } catch (Throwable e) {
                String msg = "Operation 'text/documentSymbol' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_DOC_SYMBOL, msg, e, params.getTextDocument(),
                        (Position) null);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String fileUri = params.getTextDocument().getUri();
            try {
                CodeActionContext context = ContextBuilder.buildCodeActionContext(fileUri,
                        this.workspaceManagerProxy.get(fileUri),
                        this.serverContext,
                        params,
                        cancelChecker);
                return LangExtensionDelegator.instance().codeActions(params, context, this.serverContext).stream()
                        .map((Function<CodeAction, Either<Command, CodeAction>>) Either::forRight)
                        .collect(Collectors.toList());
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Code Action", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/codeAction' failed!";
                Range range = params.getRange();
                this.clientLogger.logError(LSContextOperation.TXT_CODE_ACTION, msg, e, params.getTextDocument(),
                        range.getStart(), range.getEnd());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction codeAction) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                ResolvableCodeAction resolvableCodeAction = ResolvableCodeAction.from(codeAction);
                if (resolvableCodeAction.getData() == null || resolvableCodeAction.getData().getFileUri() == null) {
                   // Probably not a resolvable code action. Can be the client sending resolve request for a
                   // scenario where code action's text edit is empty
                    this.clientLogger.logWarning("Invalid resolvable code action received: " + codeAction.getTitle());
                    return codeAction;
                }
                String fileUri = resolvableCodeAction.getData().getFileUri();
                CodeActionResolveContext resolveContext = ContextBuilder.buildCodeActionResolveContext(
                        fileUri,
                        workspaceManagerProxy.get(fileUri),
                        this.serverContext,
                        cancelChecker);
                return LangExtensionDelegator.instance().resolveCodeAction(resolvableCodeAction, resolveContext);
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Resolve Code Action", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/resolveCodeAction' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_RESOLVE_CODE_ACTION, msg, e, null,
                        (Position) null);
            }

            return codeAction;
        });
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            if (!LSCodeLensesProviderHolder.getInstance(this.serverContext).isEnabled()) {
                // Disabled ballerina codeLens feature
                clientCapabilities.getTextDocCapabilities().setCodeLens(null);
                // Skip code lenses if codeLens disabled
                return Collections.emptyList();
            }

            String fileUri = params.getTextDocument().getUri();
            Optional<Path> docSymbolFilePath = PathUtil.getPathFromURI(fileUri);

            // Note: If the path does not exist, then return early and ignore
            if (docSymbolFilePath.isEmpty()) {
                return Collections.emptyList();
            }

            DocumentServiceContext codeLensContext = ContextBuilder.buildDocumentServiceContext(
                    fileUri,
                    this.workspaceManagerProxy.get(),
                    LSContextOperation.TXT_CODE_LENS, this.serverContext,
                    cancelChecker);
            try {
                return CodeLensUtil.getCodeLenses(codeLensContext, params.getTextDocument());
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Code Lens", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/codeLens' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_CODE_LENS, msg, e, params.getTextDocument(),
                        (Position) null);
            }

            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String fileUri = params.getTextDocument().getUri();
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(fileUri,
                    this.workspaceManagerProxy.get(),
                    LSContextOperation.TXT_FORMATTING,
                    this.serverContext,
                    cancelChecker);
            try {
                Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
                if (syntaxTree.isEmpty()) {
                    return Collections.emptyList();
                }
                String formattedSource = Formatter.format(syntaxTree.get()).toSourceCode();
                LinePosition eofPos = syntaxTree.get().rootNode().lineRange().endLine();
                Range range = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                TextEdit textEdit = new TextEdit(range, formattedSource);
                return Collections.singletonList(textEdit);
            } catch (UserErrorException | FormatterException e) {
                this.clientLogger.notifyUser("Formatting", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/formatting' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_FORMATTING, msg, e, params.getTextDocument(),
                        (Position) null);
            }

            return Collections.emptyList();
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
        return CompletableFutures.computeAsync((cancelChecker) -> {
            String fileUri = params.getTextDocument().getUri();
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(fileUri,
                    this.workspaceManagerProxy.get(),
                    LSContextOperation.TXT_FORMATTING,
                    this.serverContext,
                    cancelChecker);
            try {
                Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
                if (syntaxTree.isEmpty()) {
                    return Collections.emptyList();
                }
                Range range = params.getRange();
                LinePosition startPos = LinePosition.from(range.getStart().getLine(), range.getStart().getCharacter());
                LinePosition endPos = LinePosition.from(range.getEnd().getLine(), range.getEnd().getCharacter());

                LineRange lineRange = LineRange.from(syntaxTree.get().filePath(), startPos, endPos);
                SyntaxTree formattedTree = Formatter.format(syntaxTree.get(), lineRange);

                LinePosition eofPos = syntaxTree.get().rootNode().lineRange().endLine();
                Range updateRange = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                TextEdit textEdit = new TextEdit(updateRange, formattedTree.toSourceCode());

                return Collections.singletonList(textEdit);
            } catch (UserErrorException | FormatterException e) {
                this.clientLogger.notifyUser("Formatting", e);
            } catch (CancellationException ignore) {
                // ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/rangeFormatting' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_RANGE_FORMATTING, msg, e, params.getTextDocument(),
                        (Position) null);
            }

            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>>
    prepareRename(PrepareRenameParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                String fileUri = params.getTextDocument().getUri();
                PrepareRenameContext context = ContextBuilder.buildPrepareRenameContext(
                        fileUri,
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        params.getPosition(),
                        cancelChecker);
                Optional<Range> range = RenameUtil.prepareRename(context);
                if (range.isPresent()) {
                    return Either3.forFirst(range.get());
                }
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Rename", e);
            } catch (CancellationException ignore) {
                // ignore the cancellation exception
            } catch (Throwable t) {
                String msg = "Operation 'text/prepareRename' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_PREPARE_RENAME, msg, t, params.getTextDocument(),
                        params.getPosition());
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                RenameContext context = ContextBuilder.buildRenameContext(params,
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        this.clientCapabilities,
                        cancelChecker);

                return RenameUtil.rename(context);
            } catch (UserErrorException e) {
                this.clientLogger.notifyUser("Rename", e);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/rename' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_RENAME, msg, e, params.getTextDocument(),
                        params.getPosition());
            }
            return null;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(
                    PathUtil.convertUriSchemeFromBala(fileUri),
                    this.workspaceManagerProxy.get(fileUri),
                    LSContextOperation.TXT_DID_OPEN, this.serverContext);
            this.workspaceManagerProxy.didOpen(params);
            this.clientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_OPEN.getName() +
                    "' {fileUri: '" + fileUri + "'} opened");
            EventSyncPubSubHolder.getInstance(this.serverContext)
                    .getPublisher(EventKind.PROJECT_UPDATE)
                    .publish(this.languageServer.getClient(), this.serverContext, context);
        } catch (Throwable e) {
            String msg = "Operation 'text/didOpen' failed!";
            TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
            this.clientLogger.logError(LSContextOperation.TXT_DID_OPEN, msg, e, identifier, (Position) null);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            // Update content
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(
                    PathUtil.convertUriSchemeFromBala(fileUri),
                    this.workspaceManagerProxy.get(fileUri),
                    LSContextOperation.TXT_DID_CHANGE,
                    this.serverContext);
            this.workspaceManagerProxy.didChange(params);
            this.clientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CHANGE.getName() +
                    "' {fileUri: '" + fileUri + "'} updated");
            EventSyncPubSubHolder.getInstance(this.serverContext)
                    .getPublisher(EventKind.PROJECT_UPDATE)
                    .publish(this.languageServer.getClient(), this.serverContext, context);
        } catch (Throwable e) {
            String msg = "Operation 'text/didChange' failed!";
            this.clientLogger.logError(LSContextOperation.TXT_DID_CHANGE, msg, e, params.getTextDocument(),
                    (Position) null);
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        try {
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(
                    PathUtil.convertUriSchemeFromBala(fileUri),
                    this.workspaceManagerProxy.get(fileUri),
                    LSContextOperation.TXT_DID_CLOSE,
                    this.serverContext);
            this.workspaceManagerProxy.didClose(params);
            this.clientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CLOSE.getName() +
                    "' {fileUri: '" + fileUri + "'} closed");
        } catch (Throwable e) {
            String msg = "Operation 'text/didClose' failed!";
            this.clientLogger.logError(LSContextOperation.TXT_DID_CLOSE, msg, e, params.getTextDocument(),
                    (Position) null);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                boolean lineFoldingOnly = this.clientCapabilities.getTextDocCapabilities().getFoldingRange() != null &&
                        Boolean.TRUE.equals(this.clientCapabilities.getTextDocCapabilities()
                                .getFoldingRange().getLineFoldingOnly());
                FoldingRangeContext context = ContextBuilder.buildFoldingRangeContext(
                        params.getTextDocument().getUri(),
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        lineFoldingOnly,
                        cancelChecker);
                return FoldingRangeProvider.getFoldingRange(context);
            } catch (CancellationException ignore) {
                // Ignore the cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'text/foldingRange' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_FOLDING_RANGE, msg, e,
                        new TextDocumentIdentifier(params.getTextDocument().getUri()),
                        (Position) null);
            }

            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                SemanticTokensContext context = ContextBuilder.buildSemanticTokensContext(
                        params.getTextDocument().getUri(),
                        this.workspaceManagerProxy.get(),
                        this.serverContext,
                        cancelChecker);

                return SemanticTokensUtils.getSemanticTokens(context);
            } catch (CancellationException ignore) {
                // Ignore cancellation exception
            } catch (Throwable e) {
                String msg = "Operation 'textDocument/semanticTokens/full' failed!";
                this.clientLogger.logError(LSContextOperation.TXT_SEMANTIC_TOKENS_FULL, msg, e,
                        new TextDocumentIdentifier(params.getTextDocument().getUri()),
                        (Position) null);
            }

            return new SemanticTokens(new ArrayList<>());
        });
    }
}
