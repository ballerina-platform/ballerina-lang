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
import org.ballerinalang.ballerina.swagger.convertor.service.SwaggerConverterUtils;
import org.ballerinalang.compiler.CompilerPhase;
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
import org.ballerinalang.model.tree.*;
import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.model.GenSrcFile;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.eclipse.lsp4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
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
    public void swaggerDefDidChange(BallerinaASTOASChangeRequest request) {
        String fileUri = request.getDocumentIdentifier().getUri();
        Path formattingFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(formattingFilePath.toString()).orElse(formattingFilePath);
        Optional<Lock> lock = documentManager.lockFile(compilationPath);

        try {
            //Generate compilation unit for provided Open Api Sep JSON
            File tempOasJsonFile = getSwaggerFile(request.getOasDefinition());
            CodeGenerator generator = new CodeGenerator();
            List<GenSrcFile> oasSource = generator.generate(GeneratorConstants.GenType.MOCK,
                    tempOasJsonFile.getPath());
            BallerinaFile oasFile = LSCompiler.compileContent(oasSource.get(0).getContent(),
                    CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> oasFilePackage = oasFile.getBLangPackage();

            String fileContent = documentManager.getFileContent(compilationPath);
            String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            int totalLines = contentComponents.length;
            Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));

            BallerinaFile ballerinaFile = LSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
            Optional<BLangPackage> bLangPackage = ballerinaFile.getBLangPackage();

            if (bLangPackage.isPresent() && bLangPackage.get().symbol != null) {
                BLangCompilationUnit compilationUnit = bLangPackage.get().getCompilationUnits()
                        .stream().findFirst().orElse(null);
                BLangCompilationUnit oasCompilationUnit = oasFilePackage.get().getCompilationUnits()
                        .stream().findFirst().orElse(null);

                mergeAst(compilationUnit, oasCompilationUnit);
                TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>());

                // generate source for the new ast.
                JsonObject ast = TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>())
                        .getAsJsonObject();
                SourceGen sourceGen = new SourceGen(0);
                sourceGen.build(ast, null, "CompilationUnit");
                String textEditContent = sourceGen.getSourceOf(ast, false, false);

                // create text edit
                TextEdit textEdit = new TextEdit(range, textEditContent);
                WorkspaceEdit workspaceEdit = new WorkspaceEdit();
                ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
                TextDocumentEdit textDocumentEdit = new TextDocumentEdit(request.getDocumentIdentifier(),
                        Collections.singletonList(textEdit));
                workspaceEdit.setDocumentChanges(Collections.singletonList(textDocumentEdit));
                applyWorkspaceEditParams.setEdit(workspaceEdit);

                ballerinaLanguageServer.getClient().applyEdit(applyWorkspaceEditParams);
            }

        } catch (Exception ex) {
            logger.error("error: while processing service definition at converter service: " + ex.getMessage(), ex);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

    }

    /**
     * A Util method to create a temporary swagger JSON file to be used to convert into ballerina definition.
     *
     * @param oasDefinition Swagger JSON string for file creation.
     * @return Temporary file created with provided string
     * @throws IOException
     */
    private File getSwaggerFile(String oasDefinition) throws IOException {
        File oasTempFile = File.createTempFile("oasTempFile", ".json");
        BufferedWriter bw = new BufferedWriter(new FileWriter(oasTempFile));
        bw.write(oasDefinition);
        bw.close();
        return oasTempFile;
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

            if (topLevelNode instanceof ImportPackageNode) {
                if (!hasImport(ast, (ImportPackageNode) topLevelNode)) {
                    ast.getTopLevelNodes().add(0,topLevelNode);
                }
            }

            if (topLevelNode instanceof ServiceNode) {
                ServiceNode swaggerService = (ServiceNode) topLevelNode;
                List<ServiceNode> serviceList = new ArrayList<>();
                for (TopLevelNode astNode : ast.getTopLevelNodes()) {
                    if (astNode instanceof ServiceNode) {
                        ServiceNode astService = (ServiceNode) astNode;
                        if (astService.getName().getValue().equals(swaggerService.getName().getValue())) {
                            mergeServices(astService, swaggerService);
                        }
                    }
                }
            }

        });

    }

    private void mergeServices(ServiceNode originService, ServiceNode targetService) {
        mergeAnnotations(originService, targetService);
        List<ResourceNode> targetServices = new ArrayList<>();

        for (ResourceNode targetResource : targetService.getResources()) {
            boolean matched = false;
            for (ResourceNode originResource : originService.getResources()) {
                if (matchResource(originResource, targetResource)) {
                    matched = true;
                    mergeAnnotations(originResource, targetResource);
                }
            }

            if (!matched) {
                targetServices.add(targetResource);
            }
        }

        targetServices.forEach(service -> originService.addResource(service));
    }

    private void mergeAnnotations(AnnotatableNode targetNode, AnnotatableNode sourceNode) {
        for (AnnotationAttachmentNode sourceNodeAttachment : sourceNode.getAnnotationAttachments()) {

            AnnotationAttachmentNode matchedTargetNode = findAttachmentNode(targetNode, sourceNodeAttachment);

            if (matchedTargetNode != null) {
                if (sourceNodeAttachment.getExpression() instanceof BLangRecordLiteral &&
                        matchedTargetNode.getExpression() instanceof BLangRecordLiteral) {

                    BLangRecordLiteral sourceRecord = (BLangRecordLiteral) sourceNodeAttachment.getExpression();
                    BLangRecordLiteral matchedTargetRecord = (BLangRecordLiteral) matchedTargetNode.getExpression();

                    for (BLangRecordLiteral.BLangRecordKeyValue sourceKeyValue : sourceRecord.getKeyValuePairs()) {
                        int matchedKeyValuePairIndex = 0;
                        BLangRecordLiteral.BLangRecordKeyValue matchedObj = null;

                        for (BLangRecordLiteral.BLangRecordKeyValue matchedKeyValue : matchedTargetRecord.getKeyValuePairs()) {
                            if ((matchedKeyValue.key != null && matchedKeyValue.key.expr instanceof BLangSimpleVarRef)) {
                                BLangSimpleVarRef matchedKey = (BLangSimpleVarRef) matchedKeyValue.key.expr;
                                BLangSimpleVarRef sourceKey = (BLangSimpleVarRef) sourceKeyValue.key.expr;
                                if (matchedKey.variableName.getValue().equals(sourceKey.variableName.getValue())) {
                                    matchedObj = matchedKeyValue;
                                    break;
                                }
                            }
                            matchedKeyValuePairIndex++;
                        }

                        if(matchedObj != null) {
                            matchedTargetRecord.getKeyValuePairs().set(matchedKeyValuePairIndex,sourceKeyValue);
                        } else {
                            ((BLangRecordLiteral) matchedTargetNode.getExpression()).keyValuePairs.add(sourceKeyValue);
                        }

                    }
                }
            } else {
                targetNode.addAnnotationAttachment(sourceNodeAttachment);
            }

        }
    }

    private AnnotationAttachmentNode findAttachmentNode(AnnotatableNode targetNode,
                                                        AnnotationAttachmentNode sourceNodeAttachment) {
        AnnotationAttachmentNode matchedNode = null;
        for (AnnotationAttachmentNode attachmentNode : targetNode.getAnnotationAttachments()) {
            if (sourceNodeAttachment.getAnnotationName().getValue().equals(
                    attachmentNode.getAnnotationName().getValue()) && sourceNodeAttachment.getPackageAlias()
                    .getValue().equals(attachmentNode.getPackageAlias().getValue())) {
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
        if (astResource.getName().getValue().equals(swaggerResource.getName().getValue())) {
            return true;
        }
        return false;
    }

    /**
     *
     * Util method to check if given node is an existing import in current AST model.
     * @param originAst - current AST model
     * @param mergePackage - Import Node
     * @return - boolean status
     */
    private boolean hasImport(BLangCompilationUnit originAst, ImportPackageNode mergePackage) {
        boolean importFound = false;

        for (TopLevelNode originNode : originAst.getTopLevelNodes()) {
            if (originNode instanceof ImportPackageNode) {
                ImportPackageNode originPackage = (ImportPackageNode) originNode;

                if (originPackage.getOrgName().getValue().equals(mergePackage.getOrgName().getValue())) {
                    if (originPackage.getPackageName().size() == mergePackage.getPackageName().size()) {
                        List<IdentifierNode> packageNameList = originPackage.getPackageName().stream()
                                .filter(pkgName -> mergePackage.getPackageName().contains(pkgName))
                                .collect(Collectors.toList());
                        if (packageNameList.size() > 0 &&
                                packageNameList.size() == mergePackage.getPackageName().size()) {
                            importFound = true;
                        }
                    }
                }
            } else {
                break;
            }
        }

        return importFound;
    }

}
