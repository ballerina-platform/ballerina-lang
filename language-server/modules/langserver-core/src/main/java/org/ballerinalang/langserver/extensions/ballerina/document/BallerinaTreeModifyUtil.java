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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.FormattingVisitorEntry;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.DeleteRange;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.UnusedNodeVisitor;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public class BallerinaTreeModifyUtil {

    private static final String DELETE = "delete";
    private static final String IMPORT = "import";

    private BallerinaTreeModifyUtil() {
    }

    private static final Map<String, String> typeMapping = new HashMap<String, String>() {{
        put("DELETE", "");
        put("IMPORT", "import $TYPE;\n");
        put("DECLARATION", "$TYPE $VARIABLE = new ($PARAMS);\n");
        put("REMOTE_SERVICE_CALL_CHECK", "$TYPE $VARIABLE = checkpanic $CALLER->$FUNCTION($PARAMS);\n");
        put("REMOTE_SERVICE_CALL", "$TYPE $VARIABLE = $CALLER->$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL_CHECK", "$TYPE $VARIABLE = checkpanic $CALLER.$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL", "$TYPE $VARIABLE = $CALLER.$FUNCTION($PARAMS);\n");
        put("MAIN_START", "$COMMENTpublic function main() {\n");
        put("MAIN_START_MODIFY", "$COMMENTpublic function main() {");
        put("MAIN_END", "\n}\n");
        put("SERVICE_START", "@http:ServiceConfig {\n\tbasePath: \"/\"\n}\n" +
                "service $SERVICE on new http:Listener($PORT) {\n" +
                "@http:ResourceConfig {\n\tmethods: [$METHODS],\npath: \"/$RES_PATH\"\n}\n" +
                "    resource function $RESOURCE(http:Caller caller, http:Request req) {\n\n");
        put("SERVICE_START_MODIFY", "@http:ServiceConfig {\n\tbasePath: \"/\"\n}\n" +
                "service $SERVICE on new http:Listener($PORT) {\n" +
                "@http:ResourceConfig {\n\tmethods: [$METHODS],\npath: \"/$RES_PATH\"\n}\n" +
                "    resource function $RESOURCE(http:Caller caller, http:Request req) {");
        put("SERVICE_END",
                "    }\n" +
                        "}\n");
        put("IF_STATEMENT", "if ($CONDITION) {\n" +
                "\n} else {\n\n}\n");
        put("FOREACH_STATEMENT", "foreach $TYPE $VARIABLE in $COLLECTION {\n" +
                "\n}\n");
        put("LOG_STATEMENT", "log:print$TYPE($LOG_EXPR);\n");
        put("PROPERTY_STATEMENT", "$PROPERTY\n");
        put("RESPOND", "$TYPE $VARIABLE = $CALLER->respond($EXPRESSION);\n");
        put("TYPE_GUARD_IF", "if($VARIABLE is $TYPE) {\n" +
                "$STATEMENT" +
                "\n}\n");
        put("TYPE_GUARD_ELSE_IF", "else if($VARIABLE is $TYPE) {\n" +
                "\n}\n");
        put("TYPE_GUARD_ELSE", " else {\n" +
                "\n}\n");
        put("RESPOND_WITH_CHECK", "checkpanic $CALLER->respond(<@untainted>$EXPRESSION);\n");
        put("PROPERTY_STATEMENT", "$PROPERTY\n");
        put("RETURN_STATEMENT", "return $RETURN_EXPR;\n");
        put("CHECKED_PAYLOAD_FUNCTION_INVOCATION", "$TYPE $VARIABLE = checkpanic $RESPONSE.$PAYLOAD();\n");
    }};

    public static String resolveMapping(String type, JsonObject config) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        String mapping = typeMapping.get(type.toUpperCase(Locale.getDefault()));
        if (mapping == null) {
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            String key = entry.getKey().toUpperCase(Locale.getDefault());
            String value;
            if (key.equals("PARAMS")) {
                JsonArray array = entry.getValue().getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < array.size(); i++) {
                    sb.append(array.get(i).getAsString());
                    if (i != array.size() - 1) {
                        sb.append(", ");
                    }
                }
                value = sb.toString();
            } else {
                value = entry.getValue().getAsString();
            }
            mapping = mapping.replaceAll("\\$" + key, value);
        }
        return mapping;
    }

    public static String getImport(JsonObject config) {
        JsonElement value = config.get("TYPE");
        if (value != null) {
            return value.getAsString();
        }
        return null;
    }

    public static List<TextEdit> getUnusedImportRanges(
            Collection<BLangImportPackage> unusedImports, TextDocument textDocument) {
        List<TextEdit> edits = new ArrayList<>();
        for (BLangImportPackage importPackage : unusedImports) {
            LinePosition startLinePos = LinePosition.from(importPackage.getPosition().getStartLine() - 1,
                    importPackage.getPosition().getStartColumn() - 1);
            LinePosition endLinePos = LinePosition.from(importPackage.getPosition().getEndLine() - 1,
                    importPackage.getPosition().getEndColumn() - 1);
            int startOffset = textDocument.textPositionFrom(startLinePos);
            int endOffset = textDocument.textPositionFrom(endLinePos) + 1;
            edits.add(TextEdit.from(
                    TextRange.from(startOffset,
                            endOffset - startOffset), ""));
        }
        return edits;
    }

    public static TextEdit createTextEdit(TextDocument oldTextDocument,
                                          JsonObject config,
                                          String type,
                                          int startLine,
                                          int startColumn,
                                          int endLine,
                                          int endColumn) {
        String mainStartMapping = BallerinaTreeModifyUtil.resolveMapping(type,
                config == null ? new JsonObject() : config);
        int theStartOffset = oldTextDocument.textPositionFrom(LinePosition.from(
                startLine - 1, startColumn - 1));
        int theEndOffset = oldTextDocument.textPositionFrom(LinePosition.from(
                endLine - 1, endColumn - 1));
        return TextEdit.from(
                TextRange.from(theStartOffset,
                        theEndOffset - theStartOffset), mainStartMapping);
    }


    public static LSContext modifyTree(ASTModification[] astModifications, String fileUri, Path compilationPath,
                                       WorkspaceDocumentManager documentManager)
            throws CompilationFailedException, WorkspaceDocumentException, IOException, JSONGenerationException {
        LSContext astContext = new DocumentOperationContext
                .DocumentOperationContextBuilder(LSContextOperation.DOC_SERVICE_AST)
                .withCommonParams(null, fileUri, documentManager)
                .build();
        LSModuleCompiler.getBLangPackage(astContext, documentManager, false, false);
        BLangPackage oldTree = astContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        String fileName = compilationPath.toFile().getName();

        Map<Diagnostic.DiagnosticPosition, ASTModification> deleteRange = new HashMap<>();
        for (ASTModification astModification : astModifications) {
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
        TextDocument oldTextDocument = TextDocuments.from(fileContent);

        List<TextEdit> edits = new ArrayList<>();
        List<ASTModification> importModifications = Arrays.stream(astModifications)
                .filter(astModification -> IMPORT.equalsIgnoreCase(astModification.getType()))
                .collect(Collectors.toList());
        for (ASTModification importModification : importModifications) {
            if (importExist(unusedNodeVisitor, importModification)) {
                continue;
            }
            TextEdit edit = constructEdit(unusedNodeVisitor, oldTextDocument, importModification);
            if (edit != null) {
                edits.add(edit);
            }
        }
        edits.addAll(BallerinaTreeModifyUtil.getUnusedImportRanges(unusedNodeVisitor.unusedImports(),
                        oldTextDocument));
        for (ASTModification astModification : astModifications) {
            if (!IMPORT.equalsIgnoreCase(astModification.getType())) {
                TextEdit edit = constructEdit(unusedNodeVisitor, oldTextDocument, astModification);
                if (edit != null) {
                    edits.add(edit);
                }
            }
        }

        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits.toArray(
                new TextEdit[0]));
        TextDocument newTextDocument = oldTextDocument.apply(textDocumentChange);
        documentManager.updateFile(compilationPath, Collections
                .singletonList(new TextDocumentContentChangeEvent(newTextDocument.toString())));

        //Format bal file code
        JsonObject jsonAST = TextDocumentFormatUtil.getAST(compilationPath, documentManager, astContext);
        JsonObject model = jsonAST.getAsJsonObject("model");
        FormattingSourceGen.build(model, "CompilationUnit");
        FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
        formattingUtil.accept(model);

        String formattedSource = FormattingSourceGen.getSourceOf(model);
        TextDocumentContentChangeEvent changeEvent = new TextDocumentContentChangeEvent(formattedSource);
        documentManager.updateFile(compilationPath, Collections.singletonList(changeEvent));
//        astContext.put(BallerinaDocumentServiceImpl.UPDATED_SOURCE, formattedSource);
        return astContext;
    }


    private static boolean importExist(UnusedNodeVisitor unusedNodeVisitor, ASTModification astModification) {
        String importValue = BallerinaTreeModifyUtil.getImport(astModification.getConfig());
        return importValue != null && unusedNodeVisitor.usedImports().contains(importValue);
    }

    private static TextEdit constructEdit(
            UnusedNodeVisitor unusedNodeVisitor, TextDocument oldTextDocument,
            ASTModification astModification) {
        String mapping = BallerinaTreeModifyUtil.resolveMapping(astModification.getType(),
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
                return TextEdit.from(
                        TextRange.from(startOffset,
                                endOffset - startOffset), mapping);
            }
        }
        return null;
    }

}
