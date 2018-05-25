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

import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageCache;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionCustomErrorStrategy;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.definition.util.DefinitionUtil;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.langserver.references.util.ReferenceUtil;
import org.ballerinalang.langserver.rename.RenameUtil;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.signature.SignatureTreeVisitor;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.ballerinalang.langserver.util.Debouncer;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaTextDocumentService.class);

    // indicates the frequency to send diagnostics to server upon document did change
    private static final int DIAG_PUSH_DEBOUNCE_DELAY = 500;
    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;
    private Map<String, List<Diagnostic>> lastDiagnosticMap;
    private LSGlobalContext lsGlobalContext;

    private final Debouncer diagPushDebouncer;

    BallerinaTextDocumentService(LSGlobalContext globalContext) {
        this.lsGlobalContext = globalContext;
        this.ballerinaLanguageServer = this.lsGlobalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = this.lsGlobalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.lastDiagnosticMap = new HashMap<>();
        this.diagPushDebouncer = new Debouncer(DIAG_PUSH_DEBOUNCE_DELAY);
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
    completion(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            List<CompletionItem> completions;
            LSServiceOperationContext completionContext = new LSServiceOperationContext();
            Path completionPath = CommonUtil.getPath(new LSDocument(fileUri));
            Optional<Lock> lock = documentManager.lockFile(completionPath);
            try {
                completionContext.put(DocumentServiceKeys.POSITION_KEY, position);
                completionContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                completionContext.put(CompletionKeys.DOC_MANAGER_KEY, documentManager);
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue

                BLangPackage bLangPackage = LSCompiler.getBLangPackage(completionContext, documentManager, false,
                                                                       CompletionCustomErrorStrategy.class,
                                                                       false, completionContext).get(0);
                completionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                      bLangPackage.symbol.getName().getValue());
                CompletionUtil.resolveSymbols(completionContext, bLangPackage);
            } catch (Exception | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while resolving symbols" + ((msg != null) ? ": " + msg : ""), e);
                }
                // Fallback procedure in an exception. Currently supports the match statement only
                CompletionUtil.resolveSymbols(completionContext, null);
            } finally {
                lock.ifPresent(Lock::unlock);
                completions = CompletionUtil.getCompletionItems(completionContext);
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
            LSServiceOperationContext hoverContext = new LSServiceOperationContext();
            Path hoverFilePath = CommonUtil.getPath(new LSDocument(fileUri));
            Optional<Lock> lock = documentManager.lockFile(hoverFilePath);
            Hover hover;
            hoverContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            hoverContext.put(DocumentServiceKeys.POSITION_KEY, position);
            try {
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                BLangPackage currentBLangPackage =
                        LSCompiler.getBLangPackage(hoverContext, documentManager, false,
                                                   LSCustomErrorStrategy.class, false, hoverContext).get(0);
                hoverContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                 currentBLangPackage.symbol.getName().getValue());
                hover = HoverUtil.getHoverContent(hoverContext, currentBLangPackage);
            } catch (Exception | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving hover content" + ((msg != null) ? ": " + msg : ""), e);
                }
                hover = new Hover();
                List<Either<String, MarkedString>> contents = new ArrayList<>();
                contents.add(Either.forLeft(""));
                hover.setContents(contents);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
            return hover;
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String uri = position.getTextDocument().getUri();
            Path sigFilePath = CommonUtil.getPath(new LSDocument(uri));
            Optional<Lock> lock = documentManager.lockFile(sigFilePath);
            try {
                String fileContent = this.documentManager.getFileContent(sigFilePath);
                LSServiceOperationContext signatureContext = new LSServiceOperationContext();
                SignatureHelpUtil.captureCallableItemInfo(position.getPosition(), fileContent, signatureContext);
                signatureContext.put(DocumentServiceKeys.POSITION_KEY, position);
                signatureContext.put(DocumentServiceKeys.FILE_URI_KEY, uri);
                SignatureHelp signatureHelp;
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                BLangPackage bLangPackage = LSCompiler.getBLangPackage(signatureContext, documentManager,
                        false, LSCustomErrorStrategy.class, false, signatureContext).get(0);
                signatureContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                     bLangPackage.symbol.getName().getValue());
                SignatureTreeVisitor signatureTreeVisitor = new SignatureTreeVisitor(signatureContext);
                bLangPackage.accept(signatureTreeVisitor);
                signatureHelp = SignatureHelpUtil.getFunctionSignatureHelp(signatureContext);
                return signatureHelp;
            } catch (Exception | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving signature help" + ((msg != null) ? ": " + msg : ""), e);
                }
                return new SignatureHelp();
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            Path defFilePath = CommonUtil.getPath(new LSDocument(fileUri));
            Optional<Lock> lock = documentManager.lockFile(defFilePath);
            List<Location> contents;
            try {
                LSServiceOperationContext definitionContext = new LSServiceOperationContext();
                definitionContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                definitionContext.put(DocumentServiceKeys.POSITION_KEY, position);
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                BLangPackage currentBLangPackage =
                        LSCompiler.getBLangPackage(definitionContext, documentManager, false,
                                LSCustomErrorStrategy.class, false, definitionContext).get(0);
                definitionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                      currentBLangPackage.symbol.getName().getValue());
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(definitionContext);
                currentBLangPackage.accept(positionTreeVisitor);
                contents = DefinitionUtil.getDefinitionPosition(definitionContext);
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving definition" + ((msg != null) ? ": " + msg : ""), e);
                }
                contents = new ArrayList<>();
            } finally {
                lock.ifPresent(Lock::unlock);
            }
            return contents;
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            Path refFilePath = CommonUtil.getPath(new LSDocument(fileUri));
            Optional<Lock> lock = documentManager.lockFile(refFilePath);
            List<Location> contents = new ArrayList<>();
            try {
                LSServiceOperationContext referenceContext = new LSServiceOperationContext();
                referenceContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                referenceContext.put(DocumentServiceKeys.POSITION_KEY, params);
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                List<BLangPackage> bLangPackages = LSCompiler.getBLangPackage(referenceContext, documentManager, false,
                        LSCustomErrorStrategy.class, true, referenceContext);
                // Get the current package.
                BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages, fileUri);

                referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                     currentBLangPackage.symbol.getName().getValue());

                // Calculate position for the current package.
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(referenceContext);
                currentBLangPackage.accept(positionTreeVisitor);

                // Run reference visitor for all the packages in project folder.
                for (BLangPackage bLangPackage : bLangPackages) {
                    referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                         bLangPackage.symbol.getName().getValue());
                    referenceContext.put(NodeContextKeys.REFERENCE_NODES_KEY, contents);
                    contents = ReferenceUtil.getReferences(referenceContext, bLangPackage);
                }

                return contents;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving references" + ((msg != null) ? ": " + msg : ""), e);
                }
                return contents;
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
            TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            Path docSymbolFilePath = CommonUtil.getPath(new LSDocument(fileUri));
            Optional<Lock> lock = documentManager.lockFile(docSymbolFilePath);
            List<SymbolInformation> symbols = new ArrayList<>();
            try {
                LSServiceOperationContext symbolsContext = new LSServiceOperationContext();
                symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                BLangPackage bLangPackage = LSCompiler.getBLangPackage(symbolsContext, documentManager,
                                                                       false, LSCustomErrorStrategy.class, false,
                                                                       symbolsContext).get(0);
                symbolsContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                   bLangPackage.symbol.getName().getValue());
                Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                        .filter(cUnit -> (fileUri.endsWith(cUnit.getName())))
                        .findFirst();

                documentCUnit.ifPresent(cUnit -> {
                    SymbolFindingVisitor visitor = new SymbolFindingVisitor(symbolsContext);
                    cUnit.accept(visitor);
                });

                return symbols;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving document symbols" + ((msg != null) ? ": " + msg : ""), e);
                }
                return symbols;
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Command> commands = new ArrayList<>();
            LSDocument lsDocument = new LSDocument(params.getTextDocument().getUri());
            try {
                Position start = params.getRange().getStart();
                String topLevelNodeType = CommonUtil
                        .topLevelNodeTypeInLine(params.getTextDocument(), start, documentManager);
                if (topLevelNodeType != null) {
                    commands.add(CommandUtil.getDocGenerationCommand(topLevelNodeType,
                                                                     params.getTextDocument().getUri(),
                                                                     start.getLine()));
                    commands.add(CommandUtil.getAllDocGenerationCommand(params.getTextDocument().getUri()));
                }
                if (!params.getContext().getDiagnostics().isEmpty()) {
                    LSContextManager lsContextManager = LSContextManager.getInstance();
                    String sourceRoot = LSCompiler.getSourceRoot(CommonUtil.getPath(lsDocument));
                    CompilerContext compilerContext = lsContextManager.getCompilerContext(sourceRoot);
                    LSPackageCache lsPackageCache = LSPackageCache.getInstance(compilerContext);
                    params.getContext().getDiagnostics().forEach(diagnostic -> {
                        if (start.getLine() == diagnostic.getRange().getStart().getLine()) {
                            commands.addAll(CommandUtil
                                                    .getCommandsByDiagnostic(diagnostic, params, lsPackageCache));
                        }
                    });
                }
                return commands;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while retrieving code actions" + ((msg != null) ? ": " + msg : ""), e);
                }
                return commands;
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String textEditContent = null;

            LSServiceOperationContext formatContext = new LSServiceOperationContext();
            formatContext.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());

            LSDocument document = new LSDocument(params.getTextDocument().getUri());
            String fileContent = documentManager.getFileContent(CommonUtil.getPath(document));
            String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            int totalLines = contentComponents.length;

            Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));
            // Source generation for given ast.
            JsonObject ast = TextDocumentFormatUtil.getAST(params, documentManager, formatContext);
            SourceGen sourceGen = new SourceGen(0);
            sourceGen.build(ast.getAsJsonObject("model"), null, "CompilationUnit");
            textEditContent = sourceGen.getSourceOf(ast.getAsJsonObject("model"), true, false);
            TextEdit textEdit = new TextEdit(range, textEditContent);
            return Collections.singletonList(textEdit);
        });
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return CompletableFuture.supplyAsync(() -> {
            Path renameFilePath = CommonUtil.getPath(new LSDocument(params.getTextDocument().getUri()));
            Optional<Lock> lock = documentManager.lockFile(renameFilePath);
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            try {
                LSServiceOperationContext renameContext = new LSServiceOperationContext();
                renameContext.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());
                renameContext.put(DocumentServiceKeys.POSITION_KEY,
                                  new TextDocumentPositionParams(params.getTextDocument(), params.getPosition()));
                List<Location> contents = new ArrayList<>();
                // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
                List<BLangPackage> bLangPackages = LSCompiler.getBLangPackage(renameContext, documentManager, false,
                        LSCustomErrorStrategy.class, true, renameContext);
                // Get the current package.
                BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages,
                                                                                          params.getTextDocument()
                                                                                                  .getUri());

                renameContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                  currentBLangPackage.symbol.getName().getValue());
                renameContext.put(NodeContextKeys.REFERENCE_NODES_KEY, contents);

                // Run the position calculator for the current package.
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(renameContext);
                currentBLangPackage.accept(positionTreeVisitor);
                String replaceableSymbolName = renameContext.get(NodeContextKeys.NAME_OF_NODE_KEY);

                // Run reference visitor and rename util for project folder.
                for (BLangPackage bLangPackage : bLangPackages) {
                    renameContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                      bLangPackage.symbol.getName().getValue());

                    LSContextManager lsContextManager = LSContextManager.getInstance();
                    String sourceRoot = LSCompiler.getSourceRoot(renameFilePath);
                    CompilerContext context = lsContextManager.getCompilerContext(bLangPackage.packageID, sourceRoot);
                    LSPackageCache.getInstance(context).put(bLangPackage.packageID, bLangPackage);

                    contents = ReferenceUtil.getReferences(renameContext, bLangPackage);
                }

                workspaceEdit.setDocumentChanges(RenameUtil
                                                         .getRenameTextEdits(contents, documentManager,
                                                                             params.getNewName(),
                                                                             replaceableSymbolName));
                return workspaceEdit;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while renaming" + ((msg != null) ? ": " + msg : ""), e);
                }
                return workspaceEdit;
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        Path openedPath = CommonUtil.getPath(new LSDocument(params.getTextDocument().getUri()));
        if (openedPath == null) {
            return;
        }
        String content = params.getTextDocument().getText();
        Optional<Lock> lock = documentManager.lockFile(openedPath);
        try {
            if (documentManager.isFileOpen(openedPath)) {
                documentManager.openFile(openedPath, content);
            }
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        compileAndSendDiagnostics(content, openedPath);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Path changedPath = CommonUtil.getPath(new LSDocument(params.getTextDocument().getUri()));
        if (changedPath == null) {
            return;
        }
        String content = params.getContentChanges().get(0).getText();
        Optional<Lock> lock = documentManager.lockFile(changedPath);
        try {
            if (documentManager.isFileOpen(changedPath)) {
                documentManager.updateFile(changedPath, content);
            } else {
                documentManager.openFile(changedPath, content);
            }
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        this.diagPushDebouncer.call(() -> compileAndSendDiagnostics(content, changedPath));
    }

    private void compileAndSendDiagnostics(String content, Path path) {
        BallerinaFile balFile;
        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        String tempFileId = LSCompiler.getUnsavedFileIdOrNull(path.toString());
        Path compilationPath = path;
        if (tempFileId != null) {
            compilationPath = LSCompiler.createAndGetTempFile(tempFileId);
        }
        balFile = LSCompiler.compileContent(content, compilationPath, CompilerPhase.TAINT_ANALYZE, documentManager,
                true);
        if (balFile.getDiagnostics() != null) {
            balDiagnostics = balFile.getDiagnostics();
        }
        publishDiagnostics(balDiagnostics, path);
        this.fillNewPackages(balFile.getBLangPackage());
    }

    private void publishDiagnostics(List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics, Path path) {
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
        balDiagnostics.forEach(diagnostic -> {
            Diagnostic d = new Diagnostic();
            d.setSeverity(DiagnosticSeverity.Error);
            d.setMessage(diagnostic.getMessage());
            Range r = new Range();

            int startLine = diagnostic.getPosition().getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = diagnostic.getPosition().getStartColumn() - 1;
            int endLine = diagnostic.getPosition().getEndLine() - 1;
            int endChar = diagnostic.getPosition().getEndColumn() - 1;

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            r.setStart(new Position(startLine, startChar));
            r.setEnd(new Position(endLine, endChar));
            d.setRange(r);


            String fileName = diagnostic.getPosition().getSource().getCompilationUnitName();
            Path filePath = Paths.get(path.getParent() + "", fileName);
            String fileURI = filePath.toUri().toString() + "";

            if (!diagnosticsMap.containsKey(fileURI)) {
                diagnosticsMap.put(fileURI, new ArrayList<Diagnostic>());
            }
            List<Diagnostic> clientDiagnostics = diagnosticsMap.get(fileURI);

            clientDiagnostics.add(d);
        });

        // clear previous diagnostics
        List<Diagnostic> empty = new ArrayList<Diagnostic>(0);
        for (Map.Entry<String, List<Diagnostic>> entry : lastDiagnosticMap.entrySet()) {
            if (diagnosticsMap.containsKey(entry.getKey())) {
                continue;
            }
            PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
            diagnostics.setUri(entry.getKey());
            diagnostics.setDiagnostics(empty);
            this.ballerinaLanguageServer.getClient().publishDiagnostics(diagnostics);
        }

        for (Map.Entry<String, List<Diagnostic>> entry : diagnosticsMap.entrySet()) {
            PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
            diagnostics.setUri(entry.getKey());
            diagnostics.setDiagnostics(entry.getValue());
            this.ballerinaLanguageServer.getClient().publishDiagnostics(diagnostics);
        }

        lastDiagnosticMap = diagnosticsMap;
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LSDocument document = new LSDocument(params.getTextDocument().getUri());
        Path closedPath = CommonUtil.getPath(document);
        if (closedPath == null) {
            return;
        }

        this.documentManager.closeFile(CommonUtil.getPath(document));
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    // Private methods

    private void fillNewPackages(BLangPackage bLangPackage) {
        if (bLangPackage == null) {
            return;
        }
        List<TopLevelNode> importPkgs = new ArrayList<>();
        bLangPackage.getCompilationUnits().forEach(bLangCompilationUnit -> {
            importPkgs.addAll(bLangCompilationUnit.getTopLevelNodes().stream()
                    .filter(topLevelNode -> topLevelNode instanceof ImportPackageNode)
                    .collect(Collectors.toList()));
        });
        List<BallerinaPackage> ballerinaPackages = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(ballerinaPackages::addAll);
        importPkgs.forEach(bLangImportPackage -> {
            if (bLangImportPackage instanceof BLangImportPackage) {
                BLangImportPackage pkgNode = ((BLangImportPackage) bLangImportPackage);
                if (pkgNode.symbol != null
                        && !CommonUtil.listContainsPackage(pkgNode.symbol.pkgID.toString(), ballerinaPackages)) {
                    LSPackageLoader.getHomeRepoPackages()
                            .add(new BallerinaPackage(pkgNode.symbol.pkgID.getOrgName().getValue(),
                                    pkgNode.symbol.pkgID.getName().getValue(),
                                    pkgNode.symbol.pkgID.getPackageVersion().getValue()));
                }
            }
        });
    }
}
