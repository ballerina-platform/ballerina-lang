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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.FormattingVisitorEntry;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.UnusedNodeVisitor;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public class BallerinaTriggerModifyUtil {

//    private static final Logger logger = LoggerFactory.getLogger(BallerinaDocumentServiceImpl.class);

    private static final String MAIN = "main";
    private static final String SERVICE = "service";
    public static final String EMPTY_STRING = "";

    private BallerinaTriggerModifyUtil() {
    }


    public static LSContext modifyTrigger(String type, JsonObject config, String fileUri, Path compilationPath,
                                          WorkspaceDocumentManager documentManager)
            throws CompilationFailedException, WorkspaceDocumentException, IOException, JSONGenerationException {
        LSContext astContext = new DocumentOperationContext
                .DocumentOperationContextBuilder(LSContextOperation.DOC_SERVICE_AST)
                .withCommonParams(null, fileUri, documentManager)
                .build();
        LSModuleCompiler.getBLangPackage(astContext, documentManager, LSCustomErrorStrategy.class,
                false, false, false);
        BLangPackage oldTree = astContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        String fileName = compilationPath.toFile().getName();

        TextDocument oldTextDocument = documentManager.getTree(compilationPath).textDocument();

        List<TextEdit> edits =
                BallerinaTriggerModifyUtil.createTriggerEdits(oldTextDocument, oldTree, type.toUpperCase(), config);

        //perform edits
        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits.toArray(
                new TextEdit[0]));
        TextDocument newTextDoc = oldTextDocument.apply(textDocumentChange);
        documentManager.updateFile(compilationPath, Collections
                .singletonList(new TextDocumentContentChangeEvent(newTextDoc.toString())));

        //remove unused imports
        LSModuleCompiler.getBLangPackage(astContext, documentManager, LSCustomErrorStrategy.class,
                                         false, false, false);
        BLangPackage updatedTree = astContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);

        UnusedNodeVisitor unusedNodeVisitor = new UnusedNodeVisitor(fileName, new HashMap<>());
        updatedTree.accept(unusedNodeVisitor);
        if (!unusedNodeVisitor.unusedImports().isEmpty()) {
            TextDocument updatedTextDocument = TextDocuments.from(newTextDoc.toString());
            edits = BallerinaTreeModifyUtil.getUnusedImportRanges(
                    unusedNodeVisitor.unusedImports(), updatedTextDocument);
            textDocumentChange = TextDocumentChange.from(edits.toArray(
                    new TextEdit[0]));
            newTextDoc = newTextDoc.apply(textDocumentChange);
            documentManager.updateFile(compilationPath, Collections
                    .singletonList(new TextDocumentContentChangeEvent(newTextDoc.toString())));
        }

        //Format bal file code
        JsonObject jsonAST = TextDocumentFormatUtil.getAST(compilationPath, documentManager, astContext);
        JsonObject model = jsonAST.getAsJsonObject("model");
        FormattingSourceGen.build(model, "CompilationUnit");
        FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
        formattingUtil.accept(model);

        newTextDoc = TextDocuments.from(FormattingSourceGen.getSourceOf(model));
        documentManager.updateFile(compilationPath, Collections
                .singletonList(new TextDocumentContentChangeEvent(newTextDoc.toString())));

        astContext.put(BallerinaDocumentServiceImpl.UPDATED_SOURCE, newTextDoc.toString());
        return astContext;
    }

    private static List<TextEdit> createTriggerEdits(TextDocument oldTextDocument,
                                                     BLangPackage oldTree,
                                                     String type, JsonObject config) {
        List<TextEdit> edits = new ArrayList<>();
        Gson gson = new Gson();
        if (oldTree.getFunctions().isEmpty() && oldTree.getServices().isEmpty()) {
            //insert new
            if (MAIN.equalsIgnoreCase(type)) {
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_START",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_END",
                        1, 1, 1, 1));
            } else if (SERVICE.equalsIgnoreCase(type)) {
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument,
                        gson.fromJson("{\"TYPE\":\"ballerina/http\"}",
                                JsonObject.class), "IMPORT",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_START",
                        1, 1, 1, 1));
                edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_END",
                        1, 1, 1, 1));
            }
        } else {
            Optional<BLangFunction> mainFunction = oldTree.getFunctions().stream().
                    filter(function -> function.getName().getValue().equals("main")).findFirst();
            if (mainFunction.isPresent()) {
                //replace main
                if (MAIN.equalsIgnoreCase(type)) {
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_START_MODIFY",
                            mainFunction.get().getPosition().getStartLine() - 1,
                            mainFunction.get().getPosition().getStartColumn(),
                            mainFunction.get().getBody().getPosition().getStartLine(),
                            mainFunction.get().getBody().getPosition().getStartColumn() + 1));
                } else if (SERVICE.equalsIgnoreCase(type)) {
                    Optional<BLangImportPackage> httpImport = oldTree.getImports().stream().
                            filter(aImport -> aImport.getOrgName().getValue().equalsIgnoreCase("ballerina")
                                    && aImport.getPackageName().size() == 1 &&
                                    aImport.getPackageName().get(0).getValue().equalsIgnoreCase("http")).
                            findFirst();
                    if (!httpImport.isPresent()) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument,
                                gson.fromJson("{\"TYPE\":\"ballerina/http\"}",
                                        JsonObject.class), "IMPORT",
                                1, 1, 1, 1));
                    }
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_START",
                            mainFunction.get().getPosition().getStartLine(),
                            mainFunction.get().getPosition().getStartColumn(),
                            mainFunction.get().getBody().getPosition().getStartLine(),
                            mainFunction.get().getBody().getPosition().getStartColumn() + 1));
                    edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "SERVICE_END",
                            mainFunction.get().getBody().getPosition().getEndLine(),
                            mainFunction.get().getBody().getPosition().getEndColumn() - 1,
                            mainFunction.get().getPosition().getEndLine(),
                            mainFunction.get().getPosition().getEndColumn()));
                }
            } else {
                Optional<BLangService> service = oldTree.getServices().stream().findFirst();
                if (service.isPresent()) {
                    //replace service
                    if (MAIN.equalsIgnoreCase(type)) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_START",
                                service.get().getPosition().getStartLine(),
                                service.get().getPosition().getStartColumn(),
                                service.get().getResources().get(0).getBody().getPosition().getStartLine(),
                                service.get().getResources().get(0).getBody().getPosition().
                                        getStartColumn() + 1));
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(oldTextDocument, config, "MAIN_END",
                                service.get().getResources().get(0).getBody().getPosition().getEndLine(),
                                service.get().getResources().get(0).getBody().getPosition().
                                        getEndColumn() - 1,
                                service.get().getPosition().getEndLine(),
                                service.get().getPosition().getEndColumn()));
                    } else if (SERVICE.equalsIgnoreCase(type)) {
                        edits.add(BallerinaTreeModifyUtil.createTextEdit(
                                oldTextDocument,
                                config,
                                "SERVICE_START_MODIFY",
                                service.get().annAttachments.get(0).getPosition().getStartLine(),
                                service.get().annAttachments.get(0).getPosition().getStartColumn(),
                                service.get().getResources().get(0).getBody().getPosition().getStartLine(),
                                service.get().getResources().get(0).getBody().getPosition().getStartColumn() + 1));
                    }
                } else {
                    throw new RuntimeException("Trigger function not found for replacement!");
                }
            }
        }
        return edits;
    }

}
