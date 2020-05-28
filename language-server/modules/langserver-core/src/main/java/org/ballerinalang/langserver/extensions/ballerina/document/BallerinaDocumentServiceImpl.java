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
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextDocuments;
import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiConverterUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.langserver.extensions.OASGenerationException;
import org.ballerinalang.langserver.extensions.VisibleEndpointVisitor;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.DeleteRange;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.UnusedNodeVisitor;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.model.GenSrcFile;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getProjectDir;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Implementation of Ballerina Document extension for Language Server.
 *
 * @since 0.981.2
 */
public class BallerinaDocumentServiceImpl implements BallerinaDocumentService {

    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;
    private static final String DELETE = "delete";
    public static final LSContext.Key<SyntaxTree> UPDATED_SYNTAX_TREE = new LSContext.Key<>();
//    private static final Logger logger = LoggerFactory.getLogger(BallerinaDocumentServiceImpl.class);

    public BallerinaDocumentServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public CompletableFuture<BallerinaOASResponse> openApiDefinition(BallerinaOASRequest request) {
        BallerinaOASResponse reply = new BallerinaOASResponse();
        String fileUri = request.getBallerinaDocument().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        try {
            String fileContent = documentManager.getFileContent(compilationPath);
            String openApiDefinition = OpenApiConverterUtils
                    .generateOAS3Definitions(fileContent, request.getBallerinaService());
            reply.setBallerinaOASJson(convertToJson(openApiDefinition));
        } catch (Throwable e) {
            reply.isIsError(true);
            String msg = "Operation 'ballerinaDocument/openApiDefinition' failed!";
            logError(msg, e, request.getBallerinaDocument(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

        return CompletableFuture.supplyAsync(() -> reply);
    }

    private static String convertToJson(String yamlString) throws IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yamlString, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    @Override
    public void apiDesignDidChange(ApiDesignDidChangeParams params) {
        String fileUri = params.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return;
        }
        Optional<Lock> lock = documentManager.lockFile(filePath.get());

        try {
            //Generate compilation unit for provided Open Api Sep JSON
            File tempOasJsonFile = getOpenApiFile(params.getOASDefinition());
            CodeGenerator generator = new CodeGenerator();
            List<GenSrcFile> oasSources = generator.generateBalSource(GeneratorConstants.GenType.GEN_SERVICE,
                    tempOasJsonFile.getPath(), "", null);

            Optional<GenSrcFile> oasServiceFile = oasSources.stream()
                    .filter(genSrcFile -> genSrcFile.getType().equals(GenSrcFile.GenFileType.GEN_SRC)).findAny();

            if (!oasServiceFile.isPresent()) {
                throw new OASGenerationException("OAS Service file is empty.");
            }

            //Generate ballerina file to get services
            BallerinaFile oasServiceBal = ExtendedLSCompiler.compileContent(oasServiceFile.get().getContent(),
                    CompilerPhase.CODE_ANALYZE);

            Optional<BLangPackage> oasFilePackage = oasServiceBal.getBLangPackage();

            String fileContent = documentManager.getFileContent(filePath.get());
            String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            int totalLines = contentComponents.length;
            Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));

            BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();

            if (bLangPackage.isPresent() && bLangPackage.get().symbol != null && oasFilePackage.isPresent()) {
                Optional<BLangCompilationUnit> compilationUnit = bLangPackage.get().getCompilationUnits()
                        .stream().findFirst();
                Optional<BLangCompilationUnit> oasCompilationUnit = oasFilePackage.get().getCompilationUnits()
                        .stream().findFirst();

                if (!oasCompilationUnit.isPresent() || !compilationUnit.isPresent()) {
                    return;
                }

                JsonObject targetAST = TextDocumentFormatUtil.generateJSON(compilationUnit.get(), new HashMap<>(),
                        new HashMap<>()).getAsJsonObject();
                FormattingSourceGen.build(targetAST, "CompilationUnit");
                JsonObject generatedAST = TextDocumentFormatUtil.generateJSON(oasCompilationUnit.get(), new HashMap<>(),
                        new HashMap<>()).getAsJsonObject();
                FormattingSourceGen.build(generatedAST, "CompilationUnit");
                mergeAst(targetAST, generatedAST);

                // generate source for the new ast.
                String textEditContent = FormattingSourceGen.getSourceOf(targetAST);

                // create text edit
                TextEdit textEdit = new TextEdit(range, textEditContent);
                ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
                TextDocumentEdit textDocumentEdit = new TextDocumentEdit(params.getDocumentIdentifier(),
                        Collections.singletonList(textEdit));
                WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections
                        .singletonList(
                                Either.forLeft(textDocumentEdit)));
                applyWorkspaceEditParams.setEdit(workspaceEdit);

                ballerinaLanguageServer.getClient().applyEdit(applyWorkspaceEditParams);
            }
        } catch (Throwable e) {
            String msg = "Operation 'ballerinaDocument/apiDesignDidChange' failed!";
            logError(msg, e, params.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

    }

    @Override
    public CompletableFuture<BallerinaServiceListResponse> serviceList(BallerinaServiceListRequest request) {
        BallerinaServiceListResponse reply = new BallerinaServiceListResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        try {
            String fileContent = documentManager.getFileContent(compilationPath);
            BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();
            ArrayList<String> services = new ArrayList<>();

            if (bLangPackage.isPresent() && bLangPackage.get().symbol != null) {
                BLangCompilationUnit compilationUnit = bLangPackage.get().getCompilationUnits().stream()
                        .findFirst()
                        .orElse(null);

                List<TopLevelNode> servicePkgs = new ArrayList<>();
                servicePkgs.addAll(compilationUnit.getTopLevelNodes().stream()
                        .filter(topLevelNode -> topLevelNode instanceof ServiceNode)
                        .collect(Collectors.toList()));

                servicePkgs.forEach(servicepkg -> {
                    if (servicepkg instanceof ServiceNode) {
                        ServiceNode pkg = ((ServiceNode) servicepkg);
                        services.add(pkg.getName().getValue());
                    }
                });
            }
            reply.setServices(services.toArray(new String[0]));
        } catch (Throwable e) {
            String msg = "Operation 'ballerinaDocument/serviceList' failed!";
            logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaASTResponse> ast(BallerinaASTRequest request) {
        BallerinaASTResponse reply = new BallerinaASTResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            LSContext astContext = new DocumentOperationContext
                    .DocumentOperationContextBuilder(LSContextOperation.DOC_SERVICE_AST)
                    .withCommonParams(null, fileUri, documentManager)
                    .build();
            LSModuleCompiler.getBLangPackage(astContext, this.documentManager, LSCustomErrorStrategy.class,
                    false, false);
            reply.setAst(getTreeForContent(astContext));
            reply.setParseSuccess(true);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/ast' failed!";
            logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTree(BallerinaSyntaxTreeRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            TextDocument doc = TextDocuments.from(documentManager.getFileContent(compilationPath));
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator();
            SyntaxTree syntaxTree = SyntaxTree.from(doc, compilationPath.toString());
            reply.setSyntaxTree(mapGenerator.transform(syntaxTree.modulePart()));
            reply.setParseSuccess(true);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTree' failed!";
            logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaSyntaxTreeResponse> syntaxTreeModify(BallerinaSyntaxTreeModifyRequest request) {
        BallerinaSyntaxTreeResponse reply = new BallerinaSyntaxTreeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();

        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            LSContext astContext = modifyTree(request.getAstModifications(), fileUri, compilationPath);
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator();
            reply.setSyntaxTree(mapGenerator.transform(astContext.get(UPDATED_SYNTAX_TREE).modulePart()));
            reply.setParseSuccess(true);
        } catch (Throwable e) {
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/syntaxTreeModify' failed!";
            logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaASTResponse> astModify(BallerinaASTModifyRequest request) {
        BallerinaASTResponse reply = new BallerinaASTResponse();
        String fileUri = request.getDocumentIdentifier().getUri();

        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            LSContext astContext = modifyTree(request.getAstModifications(), fileUri, compilationPath);
            LSModuleCompiler.getBLangPackage(astContext, this.documentManager,
                    LSCustomErrorStrategy.class, false,
                    false);
            reply.setSource(astContext.get(UPDATED_SYNTAX_TREE).toString());
            reply.setAst(getTreeForContent(astContext));
            reply.setParseSuccess(true);
        } catch (Throwable e) {
//            logger.error(e.getMessage(), e);
            reply.setParseSuccess(false);
            String msg = "Operation 'ballerinaDocument/ast' failed!";
            logError(msg, e, request.getDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    private LSContext modifyTree(ASTModification[] astModifications, String fileUri, Path compilationPath)
            throws CompilationFailedException, WorkspaceDocumentException, IOException {
        LSContext astContext = new DocumentOperationContext
                .DocumentOperationContextBuilder(LSContextOperation.DOC_SERVICE_AST)
                .withCommonParams(null, fileUri, documentManager)
                .build();
        LSModuleCompiler.getBLangPackage(astContext, this.documentManager, LSCustomErrorStrategy.class,
                false, false);
        BLangPackage oldTree = astContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        String fileName = compilationPath.toFile().getName();
        ArrayList<io.ballerinalang.compiler.text.TextEdit> edits = new ArrayList<>();

        Map<Diagnostic.DiagnosticPosition, ASTModification> deleteRange = new HashMap<>();

        for (int i = 0; i < astModifications.length; i++) {
            ASTModification astModification = astModifications[i];
            if (DELETE.equalsIgnoreCase(astModification.getType())) {
                deleteRange.put(new DeleteRange(
                        astModification.getStartLine(),
                        astModification.getEndLine(),
                        astModification.getStartColumn(),
                        astModification.getEndColumn()), astModification);
            }
        }
        UnusedNodeVisitor unusedNodeVisitor = new UnusedNodeVisitor(fileName, deleteRange);
        oldTree.accept(unusedNodeVisitor);

        String fileContent = documentManager.getFileContent(compilationPath);
        TextDocument textDocument = TextDocuments.from(fileContent);
        SyntaxTree oldSyntaxTree = SyntaxTree.from(textDocument, compilationPath.toString());
        TextDocument oldTextDocument = oldSyntaxTree.textDocument();

        for (BLangImportPackage importPackage : unusedNodeVisitor.unusedImports()) {
            LinePosition startLinePos = LinePosition.from(importPackage.getPosition().getStartLine() - 1,
                    importPackage.getPosition().getStartColumn() - 1);
            LinePosition endLinePos = LinePosition.from(importPackage.getPosition().getEndLine() - 1,
                    importPackage.getPosition().getEndColumn() - 1);
            int startOffset = oldTextDocument.textPositionFrom(startLinePos);
            int endOffset = oldTextDocument.textPositionFrom(endLinePos) + 1;
            edits.add(io.ballerinalang.compiler.text.TextEdit.from(
                    io.ballerinalang.compiler.text.TextRange.from(startOffset,
                            endOffset - startOffset), ""));
        }
        for (int i = 0; i < astModifications.length; i++) {
            ASTModification astModification = astModifications[i];
            String mapping = BallerinaSyntaxTreeModifyUtil.resolveMapping(astModification.getType(),
                    astModification.getConfig() == null ? new JsonObject() : astModification.getConfig());
            if (mapping != null) {
                boolean doEdit = false;
                if (DELETE.equals(astModification.getType())) {
                    if (unusedNodeVisitor.toBeDeletedRanges().contains(astModification)) {
                        doEdit = true;
                    }
                } else {
                    doEdit = true;
                }
                if (doEdit) {
                    LinePosition startLinePos = LinePosition.from(astModification.getStartLine() - 1,
                            astModification.getStartColumn() - 1);
                    LinePosition endLinePos = LinePosition.from(astModification.getEndLine() - 1,
                            astModification.getEndColumn() - 1);
                    int startOffset = oldTextDocument.textPositionFrom(startLinePos);
                    int endOffset = oldTextDocument.textPositionFrom(endLinePos);
                    edits.add(io.ballerinalang.compiler.text.TextEdit.from(
                            io.ballerinalang.compiler.text.TextRange.from(startOffset,
                                    endOffset - startOffset), mapping));
                }
            }
        }

        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits.toArray(
                new io.ballerinalang.compiler.text.TextEdit[0]));
        SyntaxTree updatedSyntaxTree = SyntaxTree.from(oldSyntaxTree, textDocumentChange);
//        logger.info("Updated Tree : " + updatedSyntaxTree);
        String updatedSyntaxTreeString = updatedSyntaxTree.toString();
        documentManager.updateFile(compilationPath, updatedSyntaxTreeString);
        astContext.put(UPDATED_SYNTAX_TREE, updatedSyntaxTree);

        File outputFile = compilationPath.toFile();
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(updatedSyntaxTreeString);
        }
        astContext.put(UPDATED_SYNTAX_TREE, updatedSyntaxTree);
        return astContext;
    }

    @Override
    public CompletableFuture<BallerinaASTDidChangeResponse> astDidChange(BallerinaASTDidChange notification) {
        BallerinaASTDidChangeResponse reply = new BallerinaASTDidChangeResponse();
        String fileUri = notification.getTextDocumentIdentifier().getUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            return CompletableFuture.supplyAsync(() -> reply);
        }
        Path compilationPath = getUntitledFilePath(filePath.get().toString()).orElse(filePath.get());
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
            JsonObject ast = notification.getAst();
            FormattingSourceGen.build(ast, "CompilationUnit");
            // we are reformatting entire document upon each astChange
            // until partial formatting is supported
            // FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
            // formattingUtil.accept(ast);
            String textEditContent = FormattingSourceGen.getSourceOf(ast);

            // create text edit
            TextEdit textEdit = new TextEdit(range, textEditContent);
            ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
            TextDocumentEdit txtDocumentEdit = new TextDocumentEdit(notification.getTextDocumentIdentifier(),
                    Collections.singletonList(textEdit));

            WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(txtDocumentEdit)));
            applyWorkspaceEditParams.setEdit(workspaceEdit);

            // update the document
            ballerinaLanguageServer.getClient().applyEdit(applyWorkspaceEditParams);
            reply.setContent(textEditContent);
        } catch (Throwable e) {
            String msg = "Operation 'ballerinaDocument/astDidChange' failed!";
            logError(msg, e, notification.getTextDocumentIdentifier(), (Position) null);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaProject> project(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaProject project = new BallerinaProject();
            try {
                Optional<Path> filePath = CommonUtil.getPathFromURI(params.getDocumentIdentifier().getUri());
                if (!filePath.isPresent()) {
                    return project;
                }
                project.setPath(getProjectDir(filePath.get()));
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaDocument/project' failed!";
                logError(msg, e, params.getDocumentIdentifier(), (Position) null);
            }
            return project;
        });
    }

    private JsonElement getTreeForContent(LSContext context) throws JSONGenerationException {
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);

        if (bLangPackage.symbol != null) {
            visibleEndpointVisitor.visit(bLangPackage);
            Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode = visibleEndpointVisitor.getVisibleEPsByNode();
            String relativeFilePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().stream()
                    .filter(cUnit -> cUnit.getPosition().getSource().cUnitName.replace("/", CommonUtil.FILE_SEPARATOR)
                            .equals(relativeFilePath))
                    .findFirst()
                    .orElse(null);
            JsonElement jsonAST = TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>(),
                    visibleEPsByNode);
            FormattingSourceGen.build(jsonAST.getAsJsonObject(), "CompilationUnit");
            return jsonAST;
        }
        return null;
    }

    /**
     * A Util method to create a temporary openapi JSON file to be used to convert into ballerina definition.
     *
     * @param oasDefinition OpenApi JSON string for file creation
     * @return Temporary file created with provided string
     * @throws IOException will throw IO Exception if file error
     */
    private File getOpenApiFile(String oasDefinition) throws IOException {
        File oasTempFile = File.createTempFile("oasTempFile", ".json");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oasTempFile),
                StandardCharsets.UTF_8))) {
            bw.write(oasDefinition);
        }
        return oasTempFile;
    }

    /**
     * Util method to merge updated compilation unit to the current compilation unit.
     *
     * @param targetCompUnit    target compilation unit
     * @param generatedCompUnit generated compilation unit which needs to be merged
     */
    private void mergeAst(JsonObject targetCompUnit, JsonObject generatedCompUnit) {
        generatedCompUnit.getAsJsonArray("topLevelNodes").forEach(item -> {
            JsonObject topLevelNode = item.getAsJsonObject();
            if (topLevelNode.get("kind").getAsString().equals("Import")) {
                if (!hasImport(targetCompUnit, topLevelNode)) {
                    int startPosition = FormattingSourceGen.getStartPosition(targetCompUnit, "imports", -1);
                    FormattingSourceGen.reconcileWS(topLevelNode,
                            targetCompUnit.getAsJsonArray("topLevelNodes"), targetCompUnit, startPosition);
                    targetCompUnit.getAsJsonArray("topLevelNodes").add(topLevelNode);
                }
            }

            if (topLevelNode.get("kind").getAsString().equals("Service")) {
                for (JsonElement astNode : targetCompUnit.getAsJsonArray("topLevelNodes")) {
                    JsonObject targetNode = astNode.getAsJsonObject();
                    if (targetNode.get("kind").getAsString().equals("Service")) {
                        if (targetNode.get("name").getAsJsonObject().get("value")
                                .equals(topLevelNode.get("name").getAsJsonObject().get("value"))) {
                            mergeServices(targetNode, topLevelNode, targetCompUnit);
                        }
                    }
                }
            }
        });
    }

    /**
     * Util method to merge given two service nodes.
     *
     * @param originService Origin service
     * @param targetService Target service which will get merged to origin service
     */
    private void mergeServices(JsonObject originService, JsonObject targetService, JsonObject tree) {
        mergeAnnotations(originService, targetService, tree);
        List<JsonObject> targetServices = new ArrayList<>();

        for (JsonElement targetItem : targetService.getAsJsonArray("resources")) {
            JsonObject targetResource = targetItem.getAsJsonObject();
            boolean matched = false;
            for (JsonElement originItem : originService.getAsJsonArray("resources")) {
                JsonObject originResource = originItem.getAsJsonObject();
                if (matchResource(originResource, targetResource)) {
                    matched = true;
                    mergeAnnotations(originResource, targetResource, tree);
                }
            }

            if (!matched) {
                targetResource.getAsJsonObject("body").add("statements", new JsonArray());
                targetServices.add(targetResource);
            }
        }

        targetServices.forEach(resource -> {
            int startIndex = FormattingSourceGen.getStartPosition(originService, "resources", -1);
            FormattingSourceGen.reconcileWS(resource, originService.getAsJsonArray("resources"), tree,
                    startIndex);
            originService.getAsJsonArray("resources").add(resource);
        });
    }

    /**
     * Util method to merge annotation attachments.
     *
     * @param targetNode target node
     * @param sourceNode source node which will get merged to target node
     */
    private void mergeAnnotations(JsonObject targetNode, JsonObject sourceNode, JsonObject tree) {
        JsonArray annotationAttachments = sourceNode.has("annotationAttachments")
                ? sourceNode.getAsJsonArray("annotationAttachments")
                : sourceNode.getAsJsonArray("annAttachments");
        for (JsonElement item : annotationAttachments) {
            JsonObject sourceNodeAttachment = item.getAsJsonObject();

            JsonObject matchedTargetNode = findAttachmentNode(targetNode, sourceNodeAttachment);

            if (matchedTargetNode != null) {
                if (sourceNodeAttachment.getAsJsonObject("expression").get("kind").getAsString()
                        .equals("RecordLiteralExpr") && matchedTargetNode.getAsJsonObject("expression").get("kind")
                        .getAsString().equals("RecordLiteralExpr")) {

                    JsonObject sourceRecord = sourceNodeAttachment.getAsJsonObject("expression");
                    JsonObject matchedTargetRecord = matchedTargetNode.getAsJsonObject("expression");

                    if (sourceNodeAttachment.getAsJsonObject("annotationName").get("value").getAsString()
                            .equals("MultiResourceInfo")) {
                        JsonArray sourceResourceInformations = sourceRecord.getAsJsonArray("keyValuePairs")
                                .get(0).getAsJsonObject().getAsJsonObject("value").getAsJsonArray("keyValuePairs");
                        JsonArray targetResourceInformations = matchedTargetRecord.getAsJsonArray("keyValuePairs")
                                .get(0).getAsJsonObject().getAsJsonObject("value").getAsJsonArray("keyValuePairs");

                        // Get map values of the resourceInformation map in MultiResourceInfo annotation.
                        for (JsonElement sourceResourceInfoItem : sourceResourceInformations) {
                            JsonObject sourceResourceInfo = sourceResourceInfoItem.getAsJsonObject();
                            JsonObject matchedTargetResourceInfo = null;
                            for (JsonElement targetResourceInfoItem : targetResourceInformations) {
                                JsonObject targetResourceInfo = targetResourceInfoItem.getAsJsonObject();
                                if (targetResourceInfo.has("key")
                                        && targetResourceInfo.getAsJsonObject("key").get("kind").getAsString()
                                        .equals("Literal")) {
                                    JsonObject targetResourceInfoKey = targetResourceInfo.getAsJsonObject("key");
                                    JsonObject sourceResourceInfoKey = sourceResourceInfo.getAsJsonObject("key");

                                    if (sourceResourceInfoKey.get("value").getAsString()
                                            .equals(targetResourceInfoKey.get("value").getAsString())) {
                                        matchedTargetResourceInfo = targetResourceInfo;
                                    }
                                }
                            }

                            if (matchedTargetResourceInfo != null) {
                                JsonArray sourceResourceInfoOperation = sourceResourceInfo.getAsJsonObject("value")
                                        .getAsJsonArray("keyValuePairs");
                                JsonArray targetResourceInfoOperation = matchedTargetResourceInfo
                                        .getAsJsonObject("value").getAsJsonArray("keyValuePairs");

                                for (JsonElement keyValueItem : sourceResourceInfoOperation) {
                                    JsonObject sourceKeyValue = keyValueItem.getAsJsonObject();
                                    int matchedKeyValuePairIndex = 0;
                                    JsonObject matchedObj = null;
                                    for (JsonElement matchedKeyValueItem : targetResourceInfoOperation) {
                                        JsonObject matchedKeyValue = matchedKeyValueItem.getAsJsonObject();
                                        if ((matchedKeyValue.has("key") &&
                                                matchedKeyValue.getAsJsonObject("key").get("kind").getAsString()
                                                        .equals("SimpleVariableRef"))) {
                                            JsonObject matchedKey = matchedKeyValue.getAsJsonObject("key");
                                            JsonObject sourceKey = sourceKeyValue.getAsJsonObject("key");
                                            if (matchedKey.getAsJsonObject("variableName").get("value").getAsString()
                                                    .equals(sourceKey.getAsJsonObject("variableName").get("value")
                                                            .getAsString())) {
                                                matchedObj = matchedKeyValue;
                                                break;
                                            }
                                        }
                                        matchedKeyValuePairIndex++;
                                    }

                                    if (matchedObj != null) {
                                        List<JsonObject> matchedObjWS = FormattingSourceGen.extractWS(matchedObj);
                                        int firstTokenIndex = matchedObjWS.get(0).get("i").getAsInt();
                                        targetResourceInfoOperation
                                                .remove(matchedKeyValuePairIndex);
                                        FormattingSourceGen.reconcileWS(sourceKeyValue, targetResourceInfoOperation,
                                                tree, firstTokenIndex);
                                        targetResourceInfoOperation.add(sourceKeyValue);
                                    } else {
                                        // Add new key value pair to the annotation record.
                                        FormattingSourceGen.reconcileWS(sourceKeyValue, targetResourceInfoOperation,
                                                tree, -1);
                                        targetResourceInfoOperation.add(sourceKeyValue);

                                        if (targetResourceInfoOperation.size() > 1) {
                                            // Add a new comma to separate the new key value pair.
                                            int startIndex = FormattingSourceGen.extractWS(sourceKeyValue).get(0)
                                                    .getAsJsonObject().get("i").getAsInt();
                                            FormattingSourceGen.addNewWS(matchedTargetResourceInfo
                                                    .getAsJsonObject("value"), tree, "", ",", true, startIndex);
                                        }
                                    }
                                }

                            } else {
                                FormattingSourceGen.reconcileWS(sourceResourceInfo, targetResourceInformations,
                                        tree, -1);
                                targetResourceInformations.add(sourceResourceInfo);
                            }
                        }

                    } else {
                        for (JsonElement keyValueItem : sourceRecord.getAsJsonArray("keyValuePairs")) {
                            JsonObject sourceKeyValue = keyValueItem.getAsJsonObject();
                            int matchedKeyValuePairIndex = 0;
                            JsonObject matchedObj = null;

                            for (JsonElement matchedKeyValueItem :
                                    matchedTargetRecord.getAsJsonArray("keyValuePairs")) {
                                JsonObject matchedKeyValue = matchedKeyValueItem.getAsJsonObject();
                                if ((matchedKeyValue.has("key") &&
                                        matchedKeyValue.getAsJsonObject("key").get("kind").getAsString()
                                                .equals("SimpleVariableRef"))) {
                                    JsonObject matchedKey = matchedKeyValue.getAsJsonObject("key");
                                    JsonObject sourceKey = sourceKeyValue.getAsJsonObject("key");
                                    if (matchedKey.getAsJsonObject("variableName").get("value").getAsString()
                                            .equals(sourceKey.getAsJsonObject("variableName").get("value")
                                                    .getAsString())) {
                                        matchedObj = matchedKeyValue;
                                        break;
                                    }
                                }
                                matchedKeyValuePairIndex++;
                            }

                            if (matchedObj != null) {
                                List<JsonObject> matchedObjWS = FormattingSourceGen.extractWS(matchedObj);
                                int firstTokenIndex = matchedObjWS.get(0).get("i").getAsInt();
                                matchedTargetRecord.getAsJsonArray("keyValuePairs")
                                        .remove(matchedKeyValuePairIndex);
                                FormattingSourceGen.reconcileWS(sourceKeyValue, matchedTargetRecord
                                        .getAsJsonArray("keyValuePairs"), tree, firstTokenIndex);
                                matchedTargetRecord.getAsJsonArray("keyValuePairs").add(sourceKeyValue);
                            } else {
                                // Add the new record key value pair.
                                FormattingSourceGen.reconcileWS(sourceKeyValue, matchedTargetRecord
                                        .getAsJsonArray("keyValuePairs"), tree, -1);
                                matchedTargetRecord.getAsJsonArray("keyValuePairs").add(sourceKeyValue);

                                if (matchedTargetRecord.getAsJsonArray("keyValuePairs").size() > 1) {
                                    // Add a new comma to separate the new key value pair.
                                    int startIndex = FormattingSourceGen.extractWS(sourceKeyValue).get(0)
                                            .getAsJsonObject().get("i").getAsInt();
                                    FormattingSourceGen.addNewWS(matchedTargetRecord, tree, "", ",", true, startIndex);
                                }
                            }
                        }
                    }
                }
            } else {
                int startIndex = FormattingSourceGen.getStartPosition(targetNode, "annAttachments", -1);
                JsonArray targetAnnAttachments = targetNode.has("annotationAttachments")
                        ? targetNode.getAsJsonArray("annotationAttachments")
                        : targetNode.getAsJsonArray("annAttachments");
                FormattingSourceGen.reconcileWS(sourceNodeAttachment, targetAnnAttachments, tree, startIndex);
                targetAnnAttachments.add(sourceNodeAttachment);
            }

        }
    }

    private JsonObject findAttachmentNode(JsonObject targetNode,
                                          JsonObject sourceNodeAttachment) {
        JsonObject matchedNode = null;
        JsonArray annotationAttachments = targetNode.has("annotationAttachments")
                ? targetNode.getAsJsonArray("annotationAttachments")
                : targetNode.getAsJsonArray("annAttachments");
        for (JsonElement item : annotationAttachments) {
            JsonObject attachmentNode = item.getAsJsonObject();
            if (sourceNodeAttachment.getAsJsonObject("annotationName").get("value").getAsString()
                    .equals(attachmentNode.getAsJsonObject("annotationName").get("value").getAsString())
                    && sourceNodeAttachment.getAsJsonObject("packageAlias").get("value").getAsString()
                    .equals(attachmentNode.getAsJsonObject("packageAlias").get("value").getAsString())) {
                matchedNode = attachmentNode;
                break;
            }
        }
        return matchedNode;
    }

    /**
     * Util method to match given resource in a service node.
     *
     * @param astResource     service node
     * @param openApiResource resource which needs to be checked
     * @return true if matched else false
     */
    private boolean matchResource(JsonObject astResource, JsonObject openApiResource) {
        return astResource.getAsJsonObject("name").get("value").getAsString()
                .equals(openApiResource.getAsJsonObject("name").get("value").getAsString());
    }

    /**
     * Util method to check if given node is an existing import in current AST model.
     *
     * @param originAst    - current AST model
     * @param mergePackage - Import Node
     * @return - boolean status
     */
    private boolean hasImport(JsonObject originAst, JsonObject mergePackage) {
        boolean importFound = false;

        for (JsonElement node : originAst.getAsJsonArray("topLevelNodes")) {
            JsonObject originNode = node.getAsJsonObject();
            if (importFound) {
                break;
            } else if (originNode.get("kind").getAsString().equals("Import")
                    && originNode.get("orgName").getAsJsonObject().get("value").getAsString()
                    .equals(mergePackage.get("orgName").getAsJsonObject().get("value").getAsString())
                    && originNode.getAsJsonArray("packageName").size() == mergePackage
                    .getAsJsonArray("packageName").size()) {
                JsonArray packageName = originNode.getAsJsonArray("packageName");
                for (int i = 0; i < packageName.size(); i++) {
                    JsonArray mergePackageName = mergePackage.getAsJsonArray("packageName");
                    if (mergePackageName.get(i).getAsJsonObject().get("value").getAsString()
                            .equals(packageName.get(i).getAsJsonObject().get("value").getAsString())) {
                        importFound = true;
                    } else {
                        importFound = false;
                        break;
                    }
                }
            }
        }

        return importFound;
    }

}
