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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.ExpressionNode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.ballerinalang.ballerina.swagger.convertor.service.SwaggerConverterUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.SourceGen;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.tree.*;
import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.model.GenSrcFile;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Implementation of Ballerina Document extension for Language Server.
 *
 * @since 0.981.2
 */
public class BallerinaDocumentServiceImpl implements BallerinaDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaDocumentService.class);

    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;

    public BallerinaDocumentServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public CompletableFuture<BallerinaOASResponse> swaggerDef(BallerinaOASRequest request) {
        String fileUri = request.getBallerinaDocument().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        BallerinaOASResponse reply = new BallerinaOASResponse();

        try {
            String fileContent = documentManager.getFileContent(compilationPath);
            String swaggerDefinition = SwaggerConverterUtils
                .generateSwaggerDefinitions(fileContent, request.getBallerinaService());
            reply.setBallerinaOASJson(swaggerDefinition);
        } catch (Exception e) {
            reply.isIsError(true);
            logger.error("error: while processing service definition at converter service: " + e.getMessage(), e);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaASTOASChangeResponse> astOasChange(BallerinaASTOASChangeRequest request) {
        BallerinaASTOASChangeResponse reply = new BallerinaASTOASChangeResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        try {
            String swaggerSource = request.getOasDefinition();
            File temp = File.createTempFile("tempfile", ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(swaggerSource);
            bw.close();

            String fileContent = documentManager.getFileContent(compilationPath);
            BallerinaFile ballerinaFile = LSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();
            ArrayList<String> services = new ArrayList<String>();


            CodeGenerator generator = new CodeGenerator();
            List<GenSrcFile> source = generator.generate(GeneratorConstants.GenType.MOCK, temp.getPath());

            BallerinaFile swaggerFile = LSCompiler.compileContent(source.get(0).getContent(),
                    CompilerPhase.CODE_ANALYZE);


            if (bLangPackage.isPresent() && bLangPackage.get().symbol != null) {
                BLangCompilationUnit compilationUnit = bLangPackage.get().getCompilationUnits().stream()
                        .findFirst()
                        .orElse(null);
                BLangCompilationUnit swaggerCompilationUnit = swaggerFile.getBLangPackage().get().getCompilationUnits()
                        .stream().findFirst().orElse(null);

                mergeAst(compilationUnit, swaggerCompilationUnit);
                reply.setOasAST(TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>()));
            }

        } catch (Exception ex) {
            reply.isIsError(true);
            logger.error("error: while processing service definition at converter service: " + ex.getMessage(), ex);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaServiceListResponse> serviceList(BallerinaServiceListRequest request) {
        BallerinaServiceListResponse reply = new BallerinaServiceListResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        try {
            String fileContent = documentManager.getFileContent(compilationPath);
            BallerinaFile ballerinaFile = LSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();
            ArrayList<String> services = new ArrayList<String>();

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
        } catch (LSCompilerException | WorkspaceDocumentException  e) {
            logger.error("error: while processing service definition at converter service: " + e.getMessage());
        } finally {
            lock.ifPresent(Lock::unlock);
        }

        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaASTResponse> ast(BallerinaASTRequest request) {
        BallerinaASTResponse reply = new BallerinaASTResponse();
        String fileUri = request.getDocumentIdentifier().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);
        try {
            String fileContent = documentManager.getFileContent(compilationPath);
            reply.setAst(getTreeForContent(fileContent));
            reply.setParseSuccess(true);
        } catch (LSCompilerException | JSONGenerationException | WorkspaceDocumentException  e) {
            reply.setParseSuccess(false);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }

    @Override
    public CompletableFuture<BallerinaASTDidChangeResponse> astDidChange(BallerinaASTDidChange notification) {
        BallerinaASTDidChangeResponse reply = new BallerinaASTDidChangeResponse();
        String fileUri = notification.getTextDocumentIdentifier().getUri();
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
            JsonObject ast = notification.getAst();
            SourceGen sourceGen = new SourceGen(0);
            sourceGen.build(ast, null, "CompilationUnit");
            String textEditContent = sourceGen.getSourceOf(ast, false, false);

            // create text edit
            TextEdit textEdit = new TextEdit(range, textEditContent);
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit(notification.getTextDocumentIdentifier(),
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

    private JsonElement getTreeForContent(String content) throws LSCompilerException, JSONGenerationException {
        BallerinaFile ballerinaFile = LSCompiler.compileContent(content, CompilerPhase.CODE_ANALYZE);
        Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();
        if (bLangPackage.isPresent() && bLangPackage.get().symbol != null) {
            BLangCompilationUnit compilationUnit = bLangPackage.get().getCompilationUnits().stream()
                    .findFirst()
                    .orElse(null);
            return TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>());
        }
        return null;
    }

    private void mergeAst(BLangCompilationUnit ast, BLangCompilationUnit swaggerAst) {
        swaggerAst.getTopLevelNodes().stream().forEach(topLevelNode -> {

            if(topLevelNode instanceof ImportPackageNode){
                if(!hasImport(ast, topLevelNode)){
                    ast.addTopLevelNode(topLevelNode);
                }
            }

            if(topLevelNode instanceof ServiceNode) {
                ServiceNode swaggerService = (ServiceNode) topLevelNode;
                for(TopLevelNode astNode : ast.getTopLevelNodes()) {
                    if(astNode instanceof ServiceNode) {
                        ServiceNode astService = (ServiceNode) astNode;
                        if(astService.getName().getValue().equals(swaggerService.getName().getValue())){
                            mergeServices(astService, swaggerService);
                        } else {
                            ast.addTopLevelNode(swaggerService);
                        }
                    }
                }
            }

        });

    }

    private  void mergeServices(ServiceNode targetService, ServiceNode swaggerService) {
        mergeAnnotations(targetService, swaggerService);

        for(ResourceNode swaggerResource : swaggerService.getResources()) {
            for(ResourceNode targetResource : targetService.getResources()) {
                if(matchResource(targetResource, swaggerResource)){
                    mergeAnnotations(targetResource, swaggerResource);
                } else {
                    targetService.addResource(swaggerResource);
                }
            }
        }
    }

    private void mergeAnnotations(AnnotatableNode targetNode, AnnotatableNode sourceNode){
        for(AnnotationAttachmentNode sourceNodeAttachment : sourceNode.getAnnotationAttachments()) {

            AnnotationAttachmentNode matchedTargetNode = findAttachmentNode(targetNode, sourceNodeAttachment);

            if(matchedTargetNode != null) {
                if(sourceNodeAttachment.getExpression() instanceof BLangRecordLiteral &&
                        matchedTargetNode.getExpression() instanceof BLangRecordLiteral) {

                    BLangRecordLiteral sourceRecord = (BLangRecordLiteral) sourceNodeAttachment.getExpression();
                    BLangRecordLiteral matchedTargetRecord = (BLangRecordLiteral) matchedTargetNode.getExpression();

                    for(BLangRecordLiteral.BLangRecordKeyValue sourceKeyValue : sourceRecord.getKeyValuePairs()){
                        for (BLangRecordLiteral.BLangRecordKeyValue matchedKeyValue : matchedTargetRecord.getKeyValuePairs()){
                            int matchedKeyValuePairIndex = 0;
                            if((matchedKeyValue.key!= null && matchedKeyValue.key.expr instanceof BLangSimpleVarRef)){
                                BLangSimpleVarRef matchedKey = (BLangSimpleVarRef) matchedKeyValue.key.expr;
                                BLangSimpleVarRef sourceKey = (BLangSimpleVarRef) sourceKeyValue.key.expr;

                                if(matchedKey.variableName.getValue().equals(sourceKey.variableName.getValue())) {
                                    matchedTargetRecord.getKeyValuePairs().set(matchedKeyValuePairIndex, sourceKeyValue);
                                } else {
                                    ((BLangRecordLiteral) matchedTargetNode.getExpression()).keyValuePairs.add(sourceKeyValue);
                                }
                            }
                            matchedKeyValuePairIndex++;
                        }
                    }
                }
            } else {
                targetNode.addAnnotationAttachment(sourceNodeAttachment);
            }

        }
    }

    private AnnotationAttachmentNode findAttachmentNode(AnnotatableNode targetNode, AnnotationAttachmentNode sourceNodeAttachment) {
        AnnotationAttachmentNode matchedNode = null;
        for(AnnotationAttachmentNode attachmentNode : targetNode.getAnnotationAttachments()) {
            if(sourceNodeAttachment.getAnnotationName().getValue().equals(attachmentNode.getAnnotationName().getValue()) &&
                    sourceNodeAttachment.getPackageAlias().getValue().equals(attachmentNode.getPackageAlias().getValue())) {
                matchedNode = attachmentNode;
                break;
            }
        }
        return matchedNode;
    }

    /**
     * Util method to match given resource in a service node.
     * @param astResource service node
     * @param swaggerResource resource which needs to be checked
     * @return true if matched else false
     */
    private boolean matchResource(ResourceNode astResource, ResourceNode swaggerResource) {
        if(astResource.getName().getValue().equals(swaggerResource.getName().getValue())) {
            return true;
        }
        return false;
    }

    /**
     *
     * Util method to check if given node is an existing import in current AST model
     * @param ast - current AST model
     * @param node - Import Node
     * @return - boolean status
     */
    private boolean hasImport(BLangCompilationUnit ast, TopLevelNode node) {
        boolean importFound = false;

        for(TopLevelNode topLevelNode : ast.getTopLevelNodes()) {
            if(topLevelNode instanceof  ImportPackageNode) {
                ImportPackageNode pkg = (ImportPackageNode) topLevelNode;
                ImportPackageNode swaggerNode = (ImportPackageNode) node;

                if(pkg.getOrgName().getValue().equals(swaggerNode.getOrgName().getValue())){
                    importFound = true;
                    boolean samePackage = true;
                    for(IdentifierNode packageName : pkg.getPackageName()) {
                        for (IdentifierNode swaggerpkg: swaggerNode.getPackageName()) {
                            if (!packageName.getValue().equals(swaggerpkg.getValue())) {
                                samePackage = false;
                                break;
                            }
                        }
                        if(!samePackage) {
                            importFound = false;
                            break;
                        }
                    }
                }
            }
        }

        return importFound;
    }

}
