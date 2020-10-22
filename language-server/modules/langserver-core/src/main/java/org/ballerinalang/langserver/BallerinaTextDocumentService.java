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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.langserver.codeaction.CodeActionRouter;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codelenses.CodeLensUtil;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderHolder;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.ballerinalang.langserver.compiler.LSCompilerCache;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.completions.exceptions.CompletionContextNotSupportedException;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.extensions.ballerina.semantichighlighter.HighlightingFailedException;
import org.ballerinalang.langserver.extensions.ballerina.semantichighlighter.SemanticHighlightProvider;
import org.ballerinalang.langserver.hover.HoverUtil;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.ballerinalang.langserver.util.Debouncer;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.definition.DefinitionUtil;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;
import static org.ballerinalang.langserver.compiler.LSClientLogger.notifyUser;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;
import static org.ballerinalang.langserver.implementation.GotoImplementationUtil.getImplementationLocation;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {
    // indicates the frequency to send diagnostics to server upon document did change
    private static final int DIAG_PUSH_DEBOUNCE_DELAY = 750;
    private final BallerinaLanguageServer languageServer;
    private final WorkspaceDocumentManager docManager;
    private final DiagnosticsHelper diagnosticsHelper;
    private LSClientCapabilities clientCapabilities;
    private boolean enableStdlibDefinition = true;

    private final Debouncer diagPushDebouncer;

    BallerinaTextDocumentService(LSGlobalContext globalContext) {
        this.languageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.docManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.diagnosticsHelper = globalContext.get(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY);
        LSClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            this.enableStdlibDefinition = newConfig.getGoToDefinition().isEnableStdlib();
        });
        this.diagPushDebouncer = new Debouncer(DIAG_PUSH_DEBOUNCE_DELAY);
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
        final List<CompletionItem> completions = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            Optional<Path> completionPath = CommonUtil.getPathFromURI(fileUri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (completionPath.isEmpty() || CommonUtil.isCachedExternalSource(fileUri)) {
                return Either.forLeft(completions);
            }

            Path compilationPath = getUntitledFilePath(completionPath.toString()).orElse(completionPath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);

            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_COMPLETION)
                    .withCommonParams(position, fileUri, docManager)
                    .withCompletionParams(clientCapabilities.getTextDocCapabilities().getCompletion())
                    .build();

            try {
                LSModuleCompiler.getBLangPackage(context, docManager, false, false);
                // Fill the current file imports
                context.put(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY, CommonUtil.getCurrentFileImports(context));
                CompletionUtil.resolveSymbols(context);
                completions.addAll(CompletionUtil.getCompletionItems(context));
            } catch (CompletionContextNotSupportedException e) {
                // Ignore the exception
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation 'text/completion' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
            } finally {
                lock.ifPresent(Lock::unlock);
            }
            return Either.forLeft(completions);
        });
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (CommonUtil.isCachedExternalSource(fileUri)) {
                return null;
            }
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_HOVER)
                    .withCommonParams(position, fileUri, docManager)
                    .withHoverParams()
                    .build();
            Hover hover;
            try {
                hover = ReferencesUtil.getHover(context, position.getPosition());
            } catch (TokenOrSymbolNotFoundException e) {
                hover = HoverUtil.getDefaultHoverObject();
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

            Path compilationPath = getUntitledFilePath(sigFilePath.toString()).orElse(sigFilePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_SIGNATURE)
                    .withCommonParams(position, uri, docManager)
                    .withSignatureParams(clientCapabilities.getTextDocCapabilities().getSignatureHelp())
                    .build();
            try {
                // Prune the source and compile
                BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
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

                SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage,
                        context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
                Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
                String filePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
                context.put(CommonKeys.VISIBLE_SYMBOLS_KEY, semanticModel
                        .visibleSymbols(filePath, LinePosition.from(cursor.getLine(), cursor.getCharacter())));

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
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition
            (TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_DEFINITION)
                    .withCommonParams(position, fileUri, docManager)
                    .withDefinitionParams(fileUri)
                    .withStdLibDefinitionParam(this.enableStdlibDefinition)
                    .build();
            try {
                return Either.forLeft(DefinitionUtil.getDefinition(context, position.getPosition()));
            } catch (UserErrorException e) {
                notifyUser("Goto Definition", e);
                return Either.forLeft(new ArrayList<>());
            } catch (Throwable e) {
                String msg = "Operation 'text/definition' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
                return Either.forLeft(new ArrayList<>());
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

            TextDocumentPositionParams pos = new TextDocumentPositionParams(params.getTextDocument(),
                    params.getPosition());
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_REFERENCES)
                    .withCommonParams(pos, fileUri, docManager)
                    .withReferencesParams()
                    .build();
            try {
                boolean includeDeclaration = params.getContext().isIncludeDeclaration();
                return ReferencesUtil.getReferences(context, includeDeclaration, params.getPosition());
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
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
            TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>>
    documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> docSymbolFilePath = CommonUtil.getPathFromURI(fileUri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (!docSymbolFilePath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
                return new ArrayList<>();
            }
            Path compilationPath = getUntitledFilePath(docSymbolFilePath.toString()).orElse(docSymbolFilePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);
            try {
                LSContext context = new DocumentServiceOperationContext
                        .ServiceOperationContextBuilder(LSContextOperation.TXT_DOC_SYMBOL)
                        .withDocumentSymbolParams(fileUri)
                        .build();
                BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
                Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                        .filter(cUnit -> (fileUri.endsWith(cUnit.getName())))
                        .findFirst();

                documentCUnit.ifPresent(cUnit -> {
                    SymbolFindingVisitor visitor = new SymbolFindingVisitor(context);
                    cUnit.accept(visitor);
                });

                return context.get(DocumentServiceKeys.SYMBOL_LIST_KEY);
            } catch (UserErrorException e) {
                notifyUser("Document Symbols", e);
                return new ArrayList<>();
            } catch (Throwable e) {
                String msg = "Operation 'text/documentSymbol' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return new ArrayList<>();
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CodeAction> actions = new ArrayList<>();
            TextDocumentIdentifier identifier = params.getTextDocument();
            String fileUri = identifier.getUri();
            Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);

            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (!filePath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
                return new ArrayList<>();
            }

            Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);

            int line = params.getRange().getStart().getLine();
            int col = params.getRange().getStart().getCharacter();
            TextDocumentPositionParams positionParams = new TextDocumentPositionParams(params.getTextDocument(),
                    new Position(line, col));

            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_CODE_ACTION)
                    .withCommonParams(positionParams, fileUri, docManager)
                    .withCodeActionParams(params.getRange().getStart())
                    .build();
            try {
                // Compile and get Top level node
                CodeActionNodeType nodeType = CodeActionUtil.topLevelNodeInLine(context, identifier, line, docManager);
                List<Diagnostic> rangeDiagnostics = params.getContext().getDiagnostics();
                List<Diagnostic> allDiagnostics = context.get(CodeActionKeys.DIAGNOSTICS_KEY);

                // Add code actions
                actions = CodeActionRouter.getBallerinaCodeActions(nodeType, context, rangeDiagnostics, allDiagnostics);
            } catch (UserErrorException e) {
                notifyUser("Code Action", e);
            } catch (Throwable e) {
                String msg = "Operation 'text/codeAction' failed!";
                Range range = params.getRange();
                logError(msg, e, params.getTextDocument(), range.getStart(), range.getEnd());
            } finally {
                lock.ifPresent(Lock::unlock);
            }
            return actions.stream().map(
                    (Function<CodeAction, Either<Command, CodeAction>>) Either::forRight).collect(Collectors.toList());
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
            if (!docSymbolFilePath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
                return new ArrayList<>();
            }

            Path compilationPath = getUntitledFilePath(docSymbolFilePath.toString()).orElse(docSymbolFilePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);

            LSContext codeLensContext = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_CODE_LENS)
                    .withCommonParams(null, fileUri, docManager)
                    .build();

            try {
                lenses = CodeLensUtil.getCodeLenses(codeLensContext);
                docManager.setCodeLenses(compilationPath, lenses);
                return lenses;
            } catch (UserErrorException e) {
                notifyUser("Code Lens", e);
                // Source compilation failed, serve from cache
                return docManager.getCodeLenses(compilationPath);
            } catch (Throwable e) {
                String msg = "Operation 'text/codeLens' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                // Source compilation failed, serve from cache
                return docManager.getCodeLenses(compilationPath);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
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
            if (!formattingFilePath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
                return Collections.singletonList(textEdit);
            }
            Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);
            try {
                CommonUtil.getPathFromURI(fileUri);
                SyntaxTree syntaxTree = docManager.getTree(formattingFilePath.get());
                String formattedSource = Formatter.format(syntaxTree).toSourceCode();

                LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
                Range range = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                textEdit = new TextEdit(range, formattedSource);
                return Collections.singletonList(textEdit);
            } catch (UserErrorException e) {
                notifyUser("Formatting", e);
                return Collections.singletonList(textEdit);
            } catch (Throwable e) {
                String msg = "Operation 'text/formatting' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return Collections.singletonList(textEdit);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    /**
     * The document range formatting request is sent from the client to the
     * server to format a given range in a document.
     *
     * Registration Options: TextDocumentRegistrationOptions
     */
    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return CompletableFuture.supplyAsync(() -> {
            TextEdit textEdit = new TextEdit();
            String fileUri = params.getTextDocument().getUri();
            Optional<Path> formattingFilePath = CommonUtil.getPathFromURI(fileUri);
            // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
            if (!formattingFilePath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
                return Collections.singletonList(textEdit);
            }
            Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath.get());
            Optional<Lock> lock = docManager.lockFile(compilationPath);
            try {
                CommonUtil.getPathFromURI(fileUri);
                SyntaxTree syntaxTree = docManager.getTree(formattingFilePath.get());
                Range range = params.getRange();
                LinePosition startPos = LinePosition.from(range.getStart().getLine(), range.getStart().getCharacter());
                LinePosition endPos = LinePosition.from(range.getEnd().getLine(), range.getEnd().getCharacter());

                LineRange lineRange = LineRange.from(syntaxTree.filePath(), startPos, endPos);
                SyntaxTree formattedTree = Formatter.format(syntaxTree, lineRange);

                LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
                Range updateRange = new Range(new Position(0, 0), new Position(eofPos.line() + 1, eofPos.offset()));
                textEdit = new TextEdit(updateRange, formattedTree.toSourceCode());
                return Collections.singletonList(textEdit);
            } catch (UserErrorException e) {
                notifyUser("Formatting", e);
                return Collections.singletonList(textEdit);
            } catch (Throwable e) {
                String msg = "Operation 'text/formatting' failed!";
                logError(msg, e, params.getTextDocument(), (Position) null);
                return Collections.singletonList(textEdit);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();

            // Note: If the source is a cached stdlib source, then return early and ignore
            if (CommonUtil.isCachedExternalSource(fileUri)) {
                return null;
            }
            Position position = params.getPosition();
            TextDocumentPositionParams pos = new TextDocumentPositionParams(params.getTextDocument(), position);
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_RENAME)
                    .withCommonParams(pos, fileUri, docManager)
                    .withRenameParams()
                    .build();

            try {
                return ReferencesUtil.getRenameWorkspaceEdits(context, params.getNewName(), position);
            } catch (UserErrorException e) {
                notifyUser("Rename", e);
                return null;
            } catch (Throwable e) {
                String msg = "Operation 'text/rename' failed!";
                logError(msg, e, params.getTextDocument(), params.getPosition());
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation
            (TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();

            // Note: If the source is a cached stdlib source, then return early and ignore
            if (CommonUtil.isCachedExternalSource(fileUri)) {
                return null;
            }
            List<Location> implementationLocations = new ArrayList<>();
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_IMPL)
                    .withCommonParams(position, fileUri, docManager)
                    .build();
            LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(fileUri);
            Path implementationPath = lsDocument.getPath();
            Path compilationPath = getUntitledFilePath(implementationPath.toString()).orElse(implementationPath);
            Optional<Lock> lock = docManager.lockFile(compilationPath);

            try {
                BLangPackage bLangPkg = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
                List<Location> locations = getImplementationLocation(bLangPkg, context, position.getPosition(),
                        lsDocument.getProjectRoot());
                implementationLocations.addAll(locations);
            } catch (UserErrorException e) {
                notifyUser("Goto Implementation", e);
                return null;
            } catch (Throwable e) {
                String msg = "Operation 'text/implementation' failed!";
                logError(msg, e, position.getTextDocument(), position.getPosition());
            } finally {
                lock.ifPresent(Lock::unlock);
            }

            return Either.forLeft(implementationLocations);
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String docUri = params.getTextDocument().getUri();
        Path compilationPath;
        try {
            compilationPath = Paths.get(new URL(docUri).toURI());
        } catch (URISyntaxException | MalformedURLException e) {
            compilationPath = null;
        }
        if (compilationPath != null) {
            String content = params.getTextDocument().getText();
            // TODO: check the untitled file path issue
            Optional<Lock> lock = docManager.lockFile(compilationPath);
            try {
                docManager.openFile(Paths.get(new URL(docUri).toURI()), content);
                LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_OPEN.getName() + "' {fileUri: '" +
                        compilationPath + "'} updated}");
                ExtendedLanguageClient client = this.languageServer.getClient();
                LSContext context = new DocumentServiceOperationContext
                        .ServiceOperationContextBuilder(LSContextOperation.TXT_DID_OPEN)
                        .withCommonParams(null, docUri, docManager)
                        .withStdLibDefinitionParam(this.enableStdlibDefinition)
                        .build();
                String fileUri = context.get(DocumentServiceKeys.FILE_URI_KEY);

                /*
                In order to support definition within the standard libraries, we cache the standard library content 
                at this stage for the cached sources. We ignore this particular step at any other operation including
                didChange.
                 */
                if (CommonUtil.isCachedExternalSource(fileUri)) {
                    context.put(DocumentServiceKeys.IS_CACHE_SUPPORTED, true);
                    context.put(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED, true);
                    LSModuleCompiler.getBLangPackages(context, docManager, false, true, true);
                    // Populate the Standard Library Cache
                    CommonUtil.updateStdLibCache(context);
                    // Note: If the source is a cached stdlib source then return early and ignore sending diagnostics
                    return;
                }

                LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(fileUri);
                diagnosticsHelper.compileAndSendDiagnostics(client, context, lsDocument, docManager);
                if (clientCapabilities.getExperimentalCapabilities().isSemanticSyntaxEnabled()) {
                    SemanticHighlightProvider.sendHighlights(client, context, docManager);
                }
                /*
                For the non-cached sources we send the diagnostics and then update the standard lib cache
                 */
                CommonUtil.updateStdLibCache(context);
            } catch (CompilationFailedException e) {
                String msg = "Computing 'diagnostics' failed!";
                TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
                logError(msg, e, identifier, (Position) null);
            } catch (HighlightingFailedException e) {
                String msg = "Semantic highlighting failed!";
                TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
                logError(msg, e, identifier, (Position) null);
            } catch (Throwable e) {
                String msg = "Operation 'text/didOpen' failed!";
                TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
                logError(msg, e, identifier, (Position) null);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String fileUri = params.getTextDocument().getUri();
        Optional<Path> changedPath = CommonUtil.getPathFromURI(fileUri);
        // Note: If the source is a cached stdlib source or path does not exist, then return early and ignore
        if (!changedPath.isPresent() || CommonUtil.isCachedExternalSource(fileUri)) {
            return;
        }
        Path compilationPath = getUntitledFilePath(changedPath.toString()).orElse(changedPath.get());
        Optional<Lock> lock = docManager.lockFile(compilationPath);
        try {
            // Update content
            docManager.updateFile(compilationPath, params.getContentChanges());
            LSClientLogger.logTrace("Operation '" + LSContextOperation.TXT_DID_CHANGE.getName() + "' {fileUri: '" +
                    compilationPath + "'} updated}");

            // Schedule diagnostics
            ExtendedLanguageClient client = this.languageServer.getClient();
            this.diagPushDebouncer.call(compilationPath, () -> {
                // Need to lock since debouncer triggers later
                Optional<Lock> nLock = docManager.lockFile(compilationPath);
                try {
                    LSContext context = new DocumentServiceOperationContext
                            .ServiceOperationContextBuilder(LSContextOperation.DIAGNOSTICS)
                            .withStdLibDefinitionParam(this.enableStdlibDefinition)
                            .withCommonParams(null, fileUri, docManager)
                            .build();
                    String fileURI = params.getTextDocument().getUri();

                    LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(fileURI);
                    diagnosticsHelper.compileAndSendDiagnostics(client, context, lsDocument, docManager);
                    if (clientCapabilities.getExperimentalCapabilities().isSemanticSyntaxEnabled()) {
                        SemanticHighlightProvider.sendHighlights(client, context, docManager);
                    }
                    // Clear current cache upon successful compilation
                    // If the compiler fails, still we'll have the cached entry(marked as outdated)
                    LSCompilerCache.clear(context, lsDocument.getProjectRoot());
                    CommonUtil.updateStdLibCache(context);
                } catch (CompilationFailedException e) {
                    String msg = "Computing 'diagnostics' failed!";
                    logError(msg, e, params.getTextDocument(), (Position) null);
                } catch (HighlightingFailedException e) {
                    String msg = "Semantic highlighting failed!";
                    TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
                    logError(msg, e, identifier, (Position) null);
                } catch (Throwable e) {
                    String msg = "Operation 'text/didChange' failed!";
                    TextDocumentIdentifier identifier = new TextDocumentIdentifier(params.getTextDocument().getUri());
                    logError(msg, e, identifier, (Position) null);
                } finally {
                    nLock.ifPresent(Lock::unlock);
                }
            });
        } catch (Throwable e) {
            String msg = "Operation 'text/didChange' failed!";
            logError(msg, e, params.getTextDocument(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String docUri = params.getTextDocument().getUri();
        Optional<Path> closedPath = CommonUtil.getPathFromURI(docUri);

        if (!closedPath.isPresent()) {
            return;
        }

        try {
            Path compilationPath = getUntitledFilePath(closedPath.toString()).orElse(closedPath.get());
            this.docManager.closeFile(compilationPath);
        } catch (Throwable e) {
            String msg = "Operation 'text/didClose' failed!";
            logError(msg, e, params.getTextDocument(), (Position) null);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }
}
