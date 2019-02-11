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
import org.ballerinalang.langserver.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProvider;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderFactory;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerCache;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageCache;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionCustomErrorStrategy;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.definition.util.DefinitionUtil;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.formatting.FormattingSourceGen;
import org.ballerinalang.langserver.formatting.FormattingVisitorEntry;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.langserver.implementation.GotoImplementationCustomErrorStratergy;
import org.ballerinalang.langserver.implementation.GotoImplementationUtil;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.references.util.ReferenceUtil;
import org.ballerinalang.langserver.rename.RenameUtil;
import org.ballerinalang.langserver.signature.SignatureHelpUtil;
import org.ballerinalang.langserver.signature.SignatureKeys;
import org.ballerinalang.langserver.signature.SignatureTreeVisitor;
import org.ballerinalang.langserver.symbols.SymbolFindingVisitor;
import org.ballerinalang.langserver.util.Debouncer;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
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
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipError;

import static org.ballerinalang.langserver.command.CommandUtil.getCommandForNodeType;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TEST_DIR_NAME;

/**
 * Text document service implementation for ballerina.
 */
class BallerinaTextDocumentService implements TextDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaTextDocumentService.class);

    // indicates the frequency to send diagnostics to server upon document did change
    private static final int DIAG_PUSH_DEBOUNCE_DELAY = 500;
    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;
    private final LSCompiler lsCompiler;
    private final DiagnosticsHelper diagnosticsHelper;
    private final LSIndexImpl lsIndex;
    private TextDocumentClientCapabilities clientCapabilities;

    private final Debouncer diagPushDebouncer;

    BallerinaTextDocumentService(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.diagnosticsHelper = globalContext.get(LSGlobalContextKeys.DIAGNOSTIC_HELPER_KEY);
        this.lsIndex = globalContext.get(LSGlobalContextKeys.LS_INDEX_KEY);
        this.diagPushDebouncer = new Debouncer(DIAG_PUSH_DEBOUNCE_DELAY);
        this.lsCompiler = new LSCompiler(documentManager);
    }

    /**
     * Set the Text Document Capabilities.
     *
     * @param clientCapabilities    Client's Text Document Capabilities
     */
    void setClientCapabilities(TextDocumentClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        final List<CompletionItem> completions = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            LSServiceOperationContext context = new LSServiceOperationContext();
            Path completionPath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(completionPath.toString()).orElse(completionPath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            context.put(DocumentServiceKeys.POSITION_KEY, position);
            context.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            context.put(CompletionKeys.DOC_MANAGER_KEY, documentManager);
            context.put(CompletionKeys.CLIENT_CAPABILITIES_KEY, this.clientCapabilities.getCompletion());
            context.put(LSGlobalContextKeys.LS_INDEX_KEY, this.lsIndex);

            try {
                BLangPackage bLangPackage = lsCompiler.getBLangPackage(context, documentManager, false,
                                                                        CompletionCustomErrorStrategy.class,
                                                                        false);
                context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, bLangPackage.symbol.getName().getValue());
                context.put(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY, bLangPackage.packageID);
                context.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
                CompletionUtil.resolveSymbols(context);
                CompletionSubRuleParser.parse(context);
                completions.addAll(CompletionUtil.getCompletionItems(context));
            } catch (Exception | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while resolving symbols" + ((msg != null) ? ": " + msg : ""), e);
                }
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
            LSServiceOperationContext hoverContext = new LSServiceOperationContext();
            Path hoverFilePath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(hoverFilePath.toString()).orElse(hoverFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            Hover hover;
            hoverContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            hoverContext.put(DocumentServiceKeys.POSITION_KEY, position);
            try {
                BLangPackage currentBLangPackage = lsCompiler.getBLangPackage(hoverContext, documentManager, false,
                                                                               LSCustomErrorStrategy.class, false);
                hoverContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                 currentBLangPackage.symbol.getName().getValue());
                hover = HoverUtil.getHoverContent(hoverContext, currentBLangPackage);
            } catch (Exception | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving hover content" + ((msg != null) ? ": " + msg : ""), e);
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
            Path sigFilePath = new LSDocument(uri).getPath();
            Path compilationPath = getUntitledFilePath(sigFilePath.toString()).orElse(sigFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            try {
                String fileContent = this.documentManager.getFileContent(compilationPath);
                LSServiceOperationContext signatureContext = new LSServiceOperationContext();
                SignatureHelpUtil.captureCallableItemInfo(position.getPosition(), fileContent, signatureContext);
                signatureContext.put(DocumentServiceKeys.POSITION_KEY, position);
                signatureContext.put(DocumentServiceKeys.FILE_URI_KEY, uri);
                signatureContext.put(SignatureKeys.SIGNATURE_HELP_CAPABILITIES_KEY,
                        this.clientCapabilities.getSignatureHelp());
                SignatureHelp signatureHelp;
                BLangPackage bLangPackage = lsCompiler.getBLangPackage(signatureContext, documentManager, false,
                                                                        LSCustomErrorStrategy.class, false);
                signatureContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
                signatureContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, 
                        bLangPackage.packageID.getName().getValue());
                SignatureTreeVisitor signatureTreeVisitor = new SignatureTreeVisitor(signatureContext);
                bLangPackage.accept(signatureTreeVisitor);
                signatureHelp = SignatureHelpUtil.getFunctionSignatureHelp(signatureContext);
                return signatureHelp;
            } catch (Exception | ZipError | AssertionError e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving signature help" + ((msg != null) ? ": " + msg : ""), e);
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
            Path defFilePath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(defFilePath.toString()).orElse(defFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            List<Location> contents;
            try {
                LSServiceOperationContext definitionContext = new LSServiceOperationContext();
                definitionContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                definitionContext.put(DocumentServiceKeys.POSITION_KEY, position);
                BLangPackage currentBLangPackage = lsCompiler.getBLangPackage(definitionContext, documentManager, false,
                                                                               LSCustomErrorStrategy.class, false);
                definitionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                      currentBLangPackage.symbol.getName().getValue());
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(definitionContext);
                currentBLangPackage.accept(positionTreeVisitor);
                contents = DefinitionUtil.getDefinitionPosition(definitionContext);
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving definition" + ((msg != null) ? ": " + msg : ""), e);
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
            LSDocument document = new LSDocument(fileUri);
            Path refFilePath = document.getPath();
            Path compilationPath = getUntitledFilePath(refFilePath.toString()).orElse(refFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            List<Location> contents = new ArrayList<>();
            try {
                LSServiceOperationContext referenceContext = new LSServiceOperationContext();
                referenceContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                referenceContext.put(DocumentServiceKeys.POSITION_KEY, params);
                List<BLangPackage> bLangPackages = lsCompiler.getBLangPackages(referenceContext, documentManager, false,
                                                                               LSCustomErrorStrategy.class, true);
                // Get the current package from multiple.
                BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages, fileUri);
                if (currentBLangPackage == null) {
                    // fail quietly
                    return contents;
                }
                referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                     currentBLangPackage.symbol.getName().getValue());

                // Calculate position for the current package.
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(referenceContext);
                currentBLangPackage.accept(positionTreeVisitor);

                // Add all compiled package IDs
                List<PackageID> packageIds = new ArrayList<>();
                for (BLangPackage bLangPackage : bLangPackages) {
                    packageIds.add(bLangPackage.packageID);
                }
                referenceContext.put(NodeContextKeys.REFERENCE_PKG_IDS_KEY, packageIds);
                referenceContext.put(NodeContextKeys.REFERENCE_RESULTS_KEY, contents);

                // Run reference visitor for all the packages in project folder.
                for (BLangPackage bLangPackage : bLangPackages) {
                    referenceContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                         bLangPackage.symbol.getName().getValue());
                    ReferenceUtil.findReferences(referenceContext, bLangPackage);
                }

                return contents;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving references" + ((msg != null) ? ": " + msg : ""), e);
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
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>>
    documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = params.getTextDocument().getUri();
            Path docSymbolFilePath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(docSymbolFilePath.toString()).orElse(docSymbolFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            List<Either<SymbolInformation, DocumentSymbol>> symbols = new ArrayList<>();
            try {
                LSServiceOperationContext symbolsContext = new LSServiceOperationContext();
                symbolsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                symbolsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, symbols);
                BLangPackage bLangPackage = lsCompiler.getBLangPackage(symbolsContext, documentManager, false,
                                                                        LSCustomErrorStrategy.class, false);
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
                    LOGGER.error("Error while retrieving document symbols" + ((msg != null) ? ": " + msg : ""), e);
                }
                return symbols;
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Either<Command, CodeAction>> commands = new ArrayList<>();
            TextDocumentIdentifier identifier = params.getTextDocument();
            String fileUri = identifier.getUri();
            try {
                int line = params.getRange().getStart().getLine();
                LSDocument document = new LSDocument(fileUri);
                List<Diagnostic> diagnostics = params.getContext().getDiagnostics();

                String topLevelNodeType = CommonUtil.topLevelNodeInLine(identifier, line, documentManager);

                // Add create test commands
                String innerDirName = LSCompilerUtil.getCurrentModulePath(document.getPath())
                        .relativize(document.getPath())
                        .toString().split(Pattern.quote(File.separator))[0];
                String moduleName = document.getSourceRootPath()
                        .relativize(LSCompilerUtil.getCurrentModulePath(document.getPath())).toString();
                if (topLevelNodeType != null && diagnostics.isEmpty() && document.hasProjectRepo() &&
                        !TEST_DIR_NAME.equals(innerDirName) && !moduleName.isEmpty() &&
                        !moduleName.endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)) {
                    /*
                    Test generation suggested only when no code diagnosis exists, inside a bal project,
                    inside a module, not inside /tests folder
                     */
                    commands.addAll(CommandUtil.getTestGenerationCommand(topLevelNodeType, fileUri, params,
                                                                         documentManager, lsCompiler));
                }

                // Add commands base on node diagnostics
                if (!diagnostics.isEmpty()) {
                    diagnostics.forEach(diagnostic -> {
                        if (line == diagnostic.getRange().getStart().getLine()) {
                            commands.addAll(CommandUtil.getCommandsByDiagnostic(diagnostic, params));
                        }
                    });
                }

                // Add commands base on node type
                if (topLevelNodeType != null) {
                    commands.addAll(getCommandForNodeType(topLevelNodeType, fileUri, line));
                }
                return commands;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving code actions" + ((msg != null) ? ": " + msg : ""), e);
                }
                return commands;
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CodeLens> lenses = new ArrayList<>();

            if (!LSCodeLensesProviderFactory.getInstance().isEnabled()) {
                // Disabled ballerina codeLens feature
                clientCapabilities.setCodeLens(null);
                // Skip code lenses if codeLens disabled
                return lenses;
            }

            String fileUri = params.getTextDocument().getUri();
            Path docSymbolFilePath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(docSymbolFilePath.toString()).orElse(docSymbolFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            try {
                LSServiceOperationContext codeLensContext = new LSServiceOperationContext();
                codeLensContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                BLangPackage bLangPackage = lsCompiler.getBLangPackage(codeLensContext, documentManager, true,
                                                                       LSCustomErrorStrategy.class, false);
                Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                        .filter(cUnit -> (fileUri.endsWith(cUnit.getName())))
                        .findFirst();

                CompilerContext compilerContext = codeLensContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
                final List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = new ArrayList<>();
                if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
                    CollectDiagnosticListener listener =
                            (CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class);
                    diagnostics.addAll(listener.getDiagnostics());
                    listener.clearAll();
                }

                codeLensContext.put(CodeLensesProviderKeys.BLANG_PACKAGE_KEY, bLangPackage);
                codeLensContext.put(CodeLensesProviderKeys.FILE_URI_KEY, fileUri);
                codeLensContext.put(CodeLensesProviderKeys.DIAGNOSTIC_KEY, diagnostics);

                documentCUnit.ifPresent(cUnit -> {
                    codeLensContext.put(CodeLensesProviderKeys.COMPILATION_UNIT_KEY, cUnit);

                    List<LSCodeLensesProvider> providers = LSCodeLensesProviderFactory.getInstance().getProviders();
                    for (LSCodeLensesProvider provider : providers) {
                        try {
                            lenses.addAll(provider.getLenses(codeLensContext));
                        } catch (LSCodeLensesProviderException e) {
                            LOGGER.error("Error while retrieving lenses from: " + provider.getName());
                        }
                    }
                });
                return lenses;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while retrieving code lenses " + ((msg != null) ? ": " + msg : ""), e);
                }
                return lenses;
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
            String textEditContent;
            TextEdit textEdit = new TextEdit();

            String fileUri = params.getTextDocument().getUri();
            Path formattingFilePath = new LSDocument(fileUri).getPath();
            Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            try {
                LSServiceOperationContext formatContext = new LSServiceOperationContext();
                formatContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);

                // Build the given ast.
                JsonObject ast = TextDocumentFormatUtil.getAST(formattingFilePath, lsCompiler, documentManager,
                        formatContext);
                JsonObject model = ast.getAsJsonObject("model");
                FormattingSourceGen.build(model, "CompilationUnit");

                // Format the given ast.
                FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
                formattingUtil.accept(model);

                //Generate source for the ast.
                textEditContent = FormattingSourceGen.getSourceOf(model);
                Matcher matcher = Pattern.compile("\r\n|\r|\n").matcher(textEditContent);
                int totalLines = 0;
                while (matcher.find()) {
                    totalLines++;
                }

                int lastNewLineCharIndex = Math.max(textEditContent.lastIndexOf('\n'),
                        textEditContent.lastIndexOf('\r'));
                int lastCharCol = textEditContent.substring(lastNewLineCharIndex + 1).length();

                Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));
                textEdit = new TextEdit(range, textEditContent);
                return Collections.singletonList(textEdit);
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while formatting" + ((msg != null) ? ": " + msg : ""), e);
                }
                return Collections.singletonList(textEdit);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return CompletableFuture.supplyAsync(() -> {
            TextDocumentIdentifier identifier = params.getTextDocument();
            String fileUri = identifier.getUri();
            LSDocument document = new LSDocument(fileUri);
            Path renameFilePath = document.getPath();
            Path compilationPath = getUntitledFilePath(renameFilePath.toString()).orElse(renameFilePath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            try {
                LSServiceOperationContext renameContext = new LSServiceOperationContext();
                renameContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
                renameContext.put(DocumentServiceKeys.POSITION_KEY,
                                  new TextDocumentPositionParams(identifier, params.getPosition()));
                List<Location> contents = new ArrayList<>();
                List<BLangPackage> bLangPackages = lsCompiler.getBLangPackages(renameContext, documentManager, false,
                                                                               LSCustomErrorStrategy.class, true);
                // Get the current package from multiple.
                BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages, fileUri);
                if (currentBLangPackage == null) {
                    // fail quietly
                    return workspaceEdit;
                }

                renameContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                  currentBLangPackage.symbol.getName().getValue());
                renameContext.put(NodeContextKeys.REFERENCE_RESULTS_KEY, contents);

                // Run the position calculator for the current package.
                PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(renameContext);
                currentBLangPackage.accept(positionTreeVisitor);
                String replaceableSymbolName = renameContext.get(NodeContextKeys.NAME_OF_NODE_KEY);

                // Add all compiled package IDs
                List<PackageID> packageIds = new ArrayList<>();
                for (BLangPackage bLangPackage : bLangPackages) {
                    packageIds.add(bLangPackage.packageID);
                }
                renameContext.put(NodeContextKeys.REFERENCE_PKG_IDS_KEY, packageIds);

                // Run reference visitor and rename util for project folder.
                for (BLangPackage bLangPackage : bLangPackages) {
                    renameContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                                      bLangPackage.symbol.getName().getValue());

                    LSContextManager lsContextManager = LSContextManager.getInstance();
                    String sourceRoot = LSCompilerUtil.getSourceRoot(compilationPath);
                    CompilerContext context = lsContextManager.getCompilerContext(bLangPackage.packageID, sourceRoot,
                                                                                  documentManager);
                    LSPackageCache.getInstance(context).put(bLangPackage.packageID, bLangPackage);

                    ReferenceUtil.findReferences(renameContext, bLangPackage);
                }

                List<Either<TextDocumentEdit, ResourceOperation>> docChanges = RenameUtil.getRenameTextEdits(contents,
                        documentManager, params.getNewName(), replaceableSymbolName)
                        .stream()
                        .map(Either::<TextDocumentEdit, ResourceOperation>forLeft)
                        .collect(Collectors.toList());
                workspaceEdit.setDocumentChanges(docChanges);

                return workspaceEdit;
            } catch (Exception e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while renaming" + ((msg != null) ? ": " + msg : ""), e);
                }
                return workspaceEdit;
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> implementation(TextDocumentPositionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = position.getTextDocument().getUri();
            List<Location> implementationLocations = new ArrayList<>();
            LSServiceOperationContext context = new LSServiceOperationContext();
            LSDocument lsDocument = new LSDocument(fileUri);
            Path implementationPath = lsDocument.getPath();
            Path compilationPath = getUntitledFilePath(implementationPath.toString()).orElse(implementationPath);
            Optional<Lock> lock = documentManager.lockFile(compilationPath);

            context.put(DocumentServiceKeys.POSITION_KEY, position);
            context.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);

            try {
                BLangPackage bLangPackage = lsCompiler.getBLangPackage(context, documentManager, false,
                        GotoImplementationCustomErrorStratergy.class, false);
                implementationLocations.addAll(GotoImplementationUtil.getImplementationLocation(bLangPackage, context,
                        position.getPosition(), lsDocument.getSourceRoot()));
            } catch (LSCompilerException e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    LOGGER.error("Error while go to implementation" + ((msg != null) ? ": " + msg : ""), e);
                }
            } finally {
                lock.ifPresent(Lock::unlock);
            }

            return implementationLocations;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        Path openedPath = new LSDocument(params.getTextDocument().getUri()).getPath();
        if (openedPath != null) {
            String content = params.getTextDocument().getText();
            Optional<Lock> lock = Optional.empty();
            try {
                Path compilationPath = getUntitledFilePath(openedPath.toString()).orElse(openedPath);
                lock = documentManager.openFile(compilationPath, content);
                // Clear cache
                String sourceRoot = LSCompilerUtil.getSourceRoot(compilationPath);
                String moduleName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, compilationPath.toString());
                LSCompilerCache.getInstance().clearAll(sourceRoot, moduleName);
                LanguageClient client = this.ballerinaLanguageServer.getClient();
                diagnosticsHelper.compileAndSendDiagnostics(client, lsCompiler, openedPath, compilationPath);
            } catch (WorkspaceDocumentException e) {
                LOGGER.error("Error while opening file:" + openedPath.toString());
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Path changedPath = new LSDocument(params.getTextDocument().getUri()).getPath();
        if (changedPath != null) {
            String content = params.getContentChanges().get(0).getText();
            Optional<Lock> lock = Optional.empty();
            try {
                Path compilationPath = getUntitledFilePath(changedPath.toString()).orElse(changedPath);
                lock = documentManager.updateFile(compilationPath, content);
                // Clear cache
                String sourceRoot = LSCompilerUtil.getSourceRoot(compilationPath);
                String moduleName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, compilationPath.toString());
                LSCompilerCache.getInstance().clearAll(sourceRoot, moduleName);
                LanguageClient client = this.ballerinaLanguageServer.getClient();
                this.diagPushDebouncer.call(() -> {
                    diagnosticsHelper.compileAndSendDiagnostics(client, lsCompiler, changedPath, compilationPath);
                });
            } catch (WorkspaceDocumentException e) {
                LOGGER.error("Error while updating change in file:" + changedPath.toString(), e);
            } finally {
                lock.ifPresent(Lock::unlock);
            }
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        Path closedPath = new LSDocument(params.getTextDocument().getUri()).getPath();

        if (closedPath == null) {
            return;
        }

        try {
            Path compilationPath = getUntitledFilePath(closedPath.toString()).orElse(closedPath);
            // Clear cache
            String sourceRoot = LSCompilerUtil.getSourceRoot(compilationPath);
            String moduleName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, compilationPath.toString());
            LSCompilerCache.getInstance().clearAll(sourceRoot, moduleName);
            this.documentManager.closeFile(compilationPath);
        } catch (WorkspaceDocumentException e) {
            LOGGER.error("Error occurred while closing file:" + closedPath.toString(), e);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }
}
