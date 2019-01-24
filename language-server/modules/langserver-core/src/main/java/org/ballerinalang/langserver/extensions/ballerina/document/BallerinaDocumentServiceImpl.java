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
import org.ballerinalang.ballerina.swagger.convertor.service.SwaggerConverterUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.extensions.OASGenerationException;
import org.ballerinalang.langserver.formatting.FormattingSourceGen;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.model.GenSrcFile;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getProjectDir;
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
    private final LSCompiler lsCompiler;

    public BallerinaDocumentServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        this.lsCompiler = new LSCompiler(documentManager);
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
                    .generateOAS3Definitions(fileContent, request.getBallerinaService());
            reply.setBallerinaOASJson(convertToJson(swaggerDefinition));
        } catch (Exception e) {
            reply.isIsError(true);
            logger.error("error: while processing service definition at converter service: " + e.getMessage(), e);
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
        Path sourceFilePath = new LSDocument(fileUri).getPath();
        Optional<Lock> lock = documentManager.lockFile(sourceFilePath);

        try {
            //Generate compilation unit for provided Open Api Sep JSON
            File tempOasJsonFile = getSwaggerFile(params.getOASDefinition());
            CodeGenerator generator = new CodeGenerator();
            List<GenSrcFile> oasSources = generator.generate(GeneratorConstants.GenType.MOCK,
                    tempOasJsonFile.getPath());

            Optional<GenSrcFile> oasServiceFile = oasSources.stream()
                    .filter(genSrcFile -> genSrcFile.getType().equals(GenSrcFile.GenFileType.GEN_SRC)).findAny();

            if (!oasServiceFile.isPresent()) {
                throw new OASGenerationException("OAS Service file is empty.");
            }

            //Generate ballerina file to get services
            BallerinaFile oasServiceBal = LSCompiler.compileContent(oasServiceFile.get().getContent(),
                    CompilerPhase.CODE_ANALYZE);

            Optional<BLangPackage> oasFilePackage = oasServiceBal.getBLangPackage();

            String fileContent = documentManager.getFileContent(sourceFilePath);
            String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            int totalLines = contentComponents.length;
            Range range = new Range(new Position(0, 0), new Position(totalLines, lastCharCol));

            BallerinaFile ballerinaFile = LSCompiler.compileContent(fileContent, CompilerPhase.CODE_ANALYZE);
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
                        .singletonList(Either.forLeft(textDocumentEdit)));
                applyWorkspaceEditParams.setEdit(workspaceEdit);

                ballerinaLanguageServer.getClient().applyEdit(applyWorkspaceEditParams);
            }
        } catch (Exception ex) {
            logger.error("error: while processing service definition at converter service: " + ex.getMessage(), ex);
        } finally {
            lock.ifPresent(Lock::unlock);
        }

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
        } catch (LSCompilerException | WorkspaceDocumentException e) {
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
            LSContext astContext = new LSServiceOperationContext();
            astContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            BLangPackage bLangPackage = lsCompiler.getBLangPackage(astContext, this.documentManager, false,
                    LSCustomErrorStrategy.class, false);
            astContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
            reply.setAst(getTreeForContent(astContext));
            reply.setParseSuccess(true);
        } catch (LSCompilerException | JSONGenerationException e) {
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

    @Override
    public CompletableFuture<BallerinaProject> project(BallerinaProjectParams params) {
        return CompletableFuture.supplyAsync(() -> {
            Path sourceFilePath = new LSDocument(params.getDocumentIdentifier().getUri()).getPath();
            BallerinaProject project = new BallerinaProject();
            project.setPath(getProjectDir(sourceFilePath));
            return project;
        });
    }

    private JsonElement getTreeForContent(LSContext context) throws LSCompilerException, JSONGenerationException {
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolFindVisitor symbolFindVisitor = new SymbolFindVisitor(compilerContext);

        if (bLangPackage.symbol != null) {
            symbolFindVisitor.visit(bLangPackage);
            Map<BLangNode, List<SymbolMetaInfo>> symbolMetaInfoMap = symbolFindVisitor.getVisibleSymbolsMap();
            String relativeFilePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().stream()
                    .filter(cUnit -> cUnit.getPosition().getSource().cUnitName.replace("/", CommonUtil.FILE_SEPARATOR)
                            .equals(relativeFilePath))
                    .findFirst()
                    .orElse(null);
            JsonElement jsonAST = TextDocumentFormatUtil.generateJSON(compilationUnit, new HashMap<>(),
                    symbolMetaInfoMap);
            FormattingSourceGen.build(jsonAST.getAsJsonObject(), "CompilationUnit");
            return jsonAST;
        }
        return null;
    }

    /**
     * A Util method to create a temporary swagger JSON file to be used to convert into ballerina definition.
     *
     * @param oasDefinition Swagger JSON string for file creation
     * @return Temporary file created with provided string
     * @throws IOException will throw IO Exception if file error
     */
    private File getSwaggerFile(String oasDefinition) throws IOException {
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
     * @param swaggerResource resource which needs to be checked
     * @return true if matched else false
     */
    private boolean matchResource(JsonObject astResource, JsonObject swaggerResource) {
        return astResource.getAsJsonObject("name").get("value").getAsString()
                .equals(swaggerResource.getAsJsonObject("name").get("value").getAsString());
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
