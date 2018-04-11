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
import org.ballerinalang.langserver.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.modal.BallerinaFile;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.LSParserUtils;
import org.ballerinalang.langserver.completions.CompletionCustomErrorStrategy;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.resolvers.TopLevelResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.definition.util.DefinitionUtil;
import org.ballerinalang.langserver.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.langserver.references.util.ReferenceUtil;
import org.ballerinalang.langserver.rename.RenameUtil;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.signature.SignatureTreeVisitor;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.ballerinalang.langserver.util.Debouncer;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
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
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {
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
            List<CompletionItem> completions;
            LSServiceOperationContext completionContext = new LSServiceOperationContext();
            completionContext.put(DocumentServiceKeys.POSITION_KEY, position);
            completionContext.put(DocumentServiceKeys.FILE_URI_KEY, position.getTextDocument().getUri());
            try {
                BLangPackage bLangPackage = TextDocumentServiceUtil.getBLangPackage(completionContext,
                        documentManager, false, CompletionCustomErrorStrategy.class, false, this.lsGlobalContext)
                        .get(0);
                completionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                        bLangPackage.symbol.getName().getValue());
                completionContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
                // Visit the package to resolve the symbols
                TreeVisitor treeVisitor = new TreeVisitor(completionContext);
                bLangPackage.accept(treeVisitor);
                BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
                if (symbolEnvNode == null) {
                    completions = CompletionItemResolver.getResolverByClass(TopLevelResolver.class)
                            .resolveItems(completionContext);
                } else {
                    completions = CompletionItemResolver.getResolverByClass(symbolEnvNode.getClass())
                            .resolveItems(completionContext);
                }
            } catch (Exception | AssertionError e) {
                completions = new ArrayList<>();
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
            LSServiceOperationContext hoverContext = new LSServiceOperationContext();
            hoverContext.put(DocumentServiceKeys.FILE_URI_KEY, position.getTextDocument().getUri());
            hoverContext.put(DocumentServiceKeys.POSITION_KEY, position);
            Hover hover;
            try {
                BLangPackage currentBLangPackage =
                        TextDocumentServiceUtil.getBLangPackage(hoverContext, documentManager, false,
                                LSCustomErrorStrategy.class, false, this.lsGlobalContext).get(0);
                hoverContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                        currentBLangPackage.symbol.getName().getValue());
                LSPackageCache.getInstance().addPackage(currentBLangPackage.packageID, currentBLangPackage);
                hover = HoverUtil.getHoverContent(hoverContext, currentBLangPackage, LSPackageCache.getInstance());
            } catch (Exception | AssertionError e) {
                hover = new Hover();
                List<Either<String, MarkedString>> contents = new ArrayList<>();
                contents.add(Either.forLeft(""));
                hover.setContents(contents);
            }
            return hover;
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String uri = position.getTextDocument().getUri();
            String fileContent = this.documentManager.getFileContent(Paths.get(URI.create(uri)));
            LSServiceOperationContext signatureContext = new LSServiceOperationContext();
            SignatureHelpUtil.captureCallableItemInfo(position.getPosition(), fileContent, signatureContext);
            signatureContext.put(DocumentServiceKeys.POSITION_KEY, position);
            signatureContext.put(DocumentServiceKeys.FILE_URI_KEY, uri);
            SignatureHelp signatureHelp;
            BLangPackage bLangPackage = TextDocumentServiceUtil.getBLangPackage(signatureContext, documentManager,
                    false, LSCustomErrorStrategy.class, false, this.lsGlobalContext).get(0);
            signatureContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                    bLangPackage.symbol.getName().getValue());
            SignatureTreeVisitor signatureTreeVisitor = new SignatureTreeVisitor(signatureContext);
            bLangPackage.accept(signatureTreeVisitor);
            signatureHelp = SignatureHelpUtil.getFunctionSignatureHelp(signatureContext);
            return signatureHelp;
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            LSServiceOperationContext definitionContext = new LSServiceOperationContext();
            definitionContext.put(DocumentServiceKeys.FILE_URI_KEY, position.getTextDocument().getUri());
            definitionContext.put(DocumentServiceKeys.POSITION_KEY, position);

            BLangPackage currentBLangPackage =
                    TextDocumentServiceUtil.getBLangPackage(definitionContext, documentManager, false,
                            LSCustomErrorStrategy.class, false, this.lsGlobalContext).get(0);
            definitionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                    currentBLangPackage.symbol.getName().getValue());
            LSPackageCache.getInstance().addPackage(currentBLangPackage.packageID, currentBLangPackage);
            List<Location> contents;
            try {
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(definitionContext);
                currentBLangPackage.accept(positionTreeVisitor);

                contents = DefinitionUtil.getDefinitionPosition(definitionContext, LSPackageCache.getInstance());
            } catch (Exception e) {
                contents = new ArrayList<>();
            }
            return contents;
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFuture.supplyAsync(() -> {
            LSServiceOperationContext referenceContext = new LSServiceOperationContext();
            referenceContext.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());
            referenceContext.put(DocumentServiceKeys.POSITION_KEY, params);
            List<Location> contents = new ArrayList<>();

            List<BLangPackage> bLangPackages = TextDocumentServiceUtil
                    .getBLangPackage(referenceContext, documentManager, false,
                            LSCustomErrorStrategy.class, true, this.lsGlobalContext);
            // Get the current package.
            BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages,
                    params.getTextDocument().getUri());

            referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                    currentBLangPackage.symbol.getName().getValue());

            // Calculate position for the current package.
            PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(referenceContext);
            currentBLangPackage.accept(positionTreeVisitor);

            // Run reference visitor for all the packages in project folder.
            for (BLangPackage bLangPackage : bLangPackages) {
                referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                        bLangPackage.symbol.getName().getValue());
                LSPackageCache.getInstance().addPackage(bLangPackage.packageID, bLangPackage);
                referenceContext.put(NodeContextKeys.REFERENCE_NODES_KEY, contents);
                contents = ReferenceUtil.getReferences(referenceContext, bLangPackage);
            }

            return contents;
        });
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
            TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        String uri = params.getTextDocument().getUri();
        List<SymbolInformation> symbols = new ArrayList<>();

        LSServiceOperationContext symbolsContext = new LSServiceOperationContext();
        symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);

        BLangPackage bLangPackage = TextDocumentServiceUtil.getBLangPackage(symbolsContext, documentManager,
                false, LSCustomErrorStrategy.class, false, this.lsGlobalContext).get(0);
        symbolsContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                bLangPackage.symbol.getName().getValue());
        Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                .filter(cUnit -> (uri.endsWith(cUnit.getName())))
                .findFirst();

        documentCUnit.ifPresent(cUnit -> {
            SymbolFindingVisitor visitor = new SymbolFindingVisitor(symbolsContext);
            cUnit.accept(visitor);
        });

        return CompletableFuture.supplyAsync(() -> symbols);
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Command> commands = new ArrayList<>();
            String topLevelNodeType = CommonUtil
                    .topLevelNodeTypeInLine(params.getTextDocument(), params.getRange().getStart(), documentManager);
            if (topLevelNodeType != null) {
                commands.add(CommandUtil.getDocGenerationCommand(topLevelNodeType,
                        params.getTextDocument().getUri(), params.getRange().getStart().getLine()));
                commands.add(CommandUtil.getAllDocGenerationCommand(params.getTextDocument().getUri()));
            } else if (!params.getContext().getDiagnostics().isEmpty()) {
                LSPackageCache lsPackageCache = LSPackageCache.getInstance();
                params.getContext().getDiagnostics().forEach(diagnostic -> {
                    commands.addAll(CommandUtil
                            .getCommandsByDiagnostic(diagnostic, params, lsPackageCache));
                });
            }
            return commands;
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
            JsonObject ast = TextDocumentFormatUtil.getAST(params, documentManager, formatContext,
                    this.lsGlobalContext);
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
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            LSServiceOperationContext renameContext = new LSServiceOperationContext();
            renameContext.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());
            renameContext.put(DocumentServiceKeys.POSITION_KEY,
                    new TextDocumentPositionParams(params.getTextDocument(), params.getPosition()));
            List<Location> contents = new ArrayList<>();

            List<BLangPackage> bLangPackages =
                    TextDocumentServiceUtil.getBLangPackage(renameContext, documentManager, false,
                            LSCustomErrorStrategy.class, true, this.lsGlobalContext);
            // Get the current package.
            BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages,
                    params.getTextDocument().getUri());

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
                LSPackageCache.getInstance().addPackage(bLangPackage.packageID, bLangPackage);

                contents = ReferenceUtil.getReferences(renameContext, bLangPackage);
            }

            workspaceEdit.setDocumentChanges(RenameUtil
                    .getRenameTextEdits(contents, documentManager, params.getNewName(), replaceableSymbolName));
            return workspaceEdit;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LSDocument document = new LSDocument(params.getTextDocument().getUri());
        Path openedPath = CommonUtil.getPath(document);
        if (openedPath == null) {
            return;
        }

        String content = params.getTextDocument().getText();
        this.documentManager.openFile(openedPath, content);

        compileAndSendDiagnostics(content, document, openedPath);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LSDocument document = new LSDocument(params.getTextDocument().getUri());
        Path changedPath = CommonUtil.getPath(document);
        if (changedPath == null) {
            return;
        }

        String content = params.getContentChanges().get(0).getText();
        this.documentManager.updateFile(changedPath, content);

        this.diagPushDebouncer.call(new Runnable() {
            @Override
            public void run() {
                compileAndSendDiagnostics(content, document, changedPath);
            }
        });
    }

    private void compileAndSendDiagnostics(String content, LSDocument document, Path path) {
        BallerinaFile balFile;
        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        String tempFileId = LSParserUtils.getUnsavedFileIdOrNull(path.toString());
        if (tempFileId == null) {
            balFile = LSParserUtils.compile(content, path, CompilerPhase.CODE_ANALYZE, false,
                                            this.lsGlobalContext);
        } else {
            balFile = LSParserUtils.compile(content, tempFileId, CompilerPhase.CODE_ANALYZE, false,
                                            this.lsGlobalContext);
        }
        if (balFile.getDiagnostics() != null) {
            balDiagnostics = balFile.getDiagnostics();
        }
        publishDiagnostics(balDiagnostics, path);
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
}
