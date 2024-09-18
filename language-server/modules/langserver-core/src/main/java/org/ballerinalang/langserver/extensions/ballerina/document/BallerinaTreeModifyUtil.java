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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.diagramutil.JSONGenerationException;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.extensions.ballerina.document.visitor.UnusedSymbolsVisitor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public final class BallerinaTreeModifyUtil {

    private static final String DELETE = "delete";
    private static final String IMPORT = "import";

    private BallerinaTreeModifyUtil() {
    }

    private static final Map<String, String> typeMapping = new HashMap<String, String>() {{
        put("DELETE", "");
        put("INSERT", "$STATEMENT");
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
            mapping = mapping.replaceAll("\\$" + key, Matcher.quoteReplacement(value));
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
            Map<String, ImportDeclarationNode> unusedImports, TextDocument textDocument) {
        List<TextEdit> edits = new ArrayList<>();
        unusedImports.forEach((key, value) -> {
            LinePosition startLinePos = LinePosition.from(value.lineRange()
                            .startLine().line(),
                    value.lineRange().startLine().offset());
            LinePosition endLinePos = LinePosition.from(value.lineRange().endLine().line(),
                    value.lineRange().endLine().offset());
            int startOffset = textDocument.textPositionFrom(startLinePos);
            int endOffset = textDocument.textPositionFrom(endLinePos);
            edits.add(TextEdit.from(
                    TextRange.from(startOffset,
                            endOffset - startOffset), ""));
        });
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


    public static JsonElement modifyTree(ASTModification[] astModifications, Path compilationPath,
                                         WorkspaceManager workspaceManager)
            throws Exception {
        Optional<SyntaxTree> oldSyntaxTree = workspaceManager.syntaxTree(compilationPath);
        if (oldSyntaxTree.isEmpty()) {
            throw new JSONGenerationException("Modification error");
        }

        Map<LineRange, ASTModification> deleteRange = new HashMap<>();
        for (ASTModification astModification : astModifications) {
            if (DELETE.equalsIgnoreCase(astModification.getType())) {
                LinePosition startLine = LinePosition.from(astModification.getStartLine(),
                        astModification.getStartColumn());
                LinePosition endLine = LinePosition.from(astModification.getEndLine(),
                        astModification.getEndColumn());
                LineRange lineRange = LineRange.from(null, startLine, endLine);
                deleteRange.put(lineRange, astModification);
            }
        }

        Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(compilationPath);
        Optional<Document> srcFile = workspaceManager.document(compilationPath);
        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            throw new JSONGenerationException("Modification error");
        }
        UnusedSymbolsVisitor unusedSymbolsVisitor =
                new UnusedSymbolsVisitor(srcFile.get(), semanticModel.get(), deleteRange);
        unusedSymbolsVisitor.visit((ModulePartNode) oldSyntaxTree.get().rootNode());

        TextDocument oldTextDocument = oldSyntaxTree.get().textDocument();

        List<TextEdit> edits = new ArrayList<>();
        List<ASTModification> importModifications = Arrays.stream(astModifications)
                .filter(ASTModification::isImport)
                .toList();
        for (ASTModification importModification : importModifications) {
            if (importExist(unusedSymbolsVisitor, importModification)) {
                continue;
            }
            TextEdit edit = constructEdit(unusedSymbolsVisitor, oldTextDocument, importModification);
            if (edit != null) {
                edits.add(edit);
            }
        }

        edits.addAll(BallerinaTreeModifyUtil.getUnusedImportRanges(unusedSymbolsVisitor.getUnusedImports(),
                oldTextDocument));

        for (ASTModification astModification : astModifications) {
            if (!astModification.isImport()) {
                TextEdit edit = constructEdit(unusedSymbolsVisitor, oldTextDocument, astModification);
                if (edit != null) {
                    edits.add(edit);
                }
            }
        }

        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits.toArray(
                new TextEdit[0]));
        TextDocument newTextDocument = oldTextDocument.apply(textDocumentChange);

        // Use formatter to format the source document.
        SyntaxTree newSyntaxTree = SyntaxTree.from(newTextDocument);
        newSyntaxTree = Formatter.format(newSyntaxTree);

        SemanticModel newSemanticModel = updateWorkspaceDocument(compilationPath, newSyntaxTree.toSourceCode(),
                                                                 workspaceManager);

        Optional<Document> formattedSrcFile = workspaceManager.document(compilationPath);
        if (formattedSrcFile.isEmpty()) {
            throw new JSONGenerationException("Modification error");
        }

        JsonElement syntaxTreeJson = DiagramUtil.getSyntaxTreeJSON(formattedSrcFile.get(), newSemanticModel);
        JsonObject jsonTreeWithSource = new JsonObject();
        jsonTreeWithSource.add("tree", syntaxTreeJson);
        jsonTreeWithSource.addProperty("source", formattedSrcFile.get().syntaxTree().toSourceCode());

        return jsonTreeWithSource;
    }

    private static SemanticModel updateWorkspaceDocument(Path compilationPath, String content,
                                                         WorkspaceManager workspaceManager)
            throws WorkspaceDocumentException {
        // TODO: Find a better way to get the semantic model for new TextDocument.
        Optional<Document> document = workspaceManager.document(compilationPath);
        if (document.isEmpty()) {
            throw new WorkspaceDocumentException("Document does not exist in path: " + compilationPath.toString());
        }
        // Update file
        Document updatedDoc = document.get().modify().withContent(content).apply();
        // Update project instance

        PackageCompilation packageCompilation = updatedDoc.module().packageInstance().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(updatedDoc.module().moduleId());
        return semanticModel;
    }

    private static boolean importExist(UnusedSymbolsVisitor unusedSymbolsVisitor, ASTModification astModification) {
        String importValue = BallerinaTreeModifyUtil.getImport(astModification.getConfig());
        return importValue != null && (unusedSymbolsVisitor.getUsedImports().containsKey(importValue)
                || unusedSymbolsVisitor.getUnusedImports().containsKey(importValue));
    }

    private static TextEdit constructEdit(
            UnusedSymbolsVisitor unusedSymbolsVisitor, TextDocument oldTextDocument,
            ASTModification astModification) {
        String mapping = BallerinaTreeModifyUtil.resolveMapping(astModification.getType(),
                astModification.getConfig() == null ? new JsonObject() : astModification.getConfig());
        if (mapping != null) {
            boolean doEdit = false;
            if (DELETE.equals(astModification.getType())) {
                if (unusedSymbolsVisitor.toBeDeletedRanges().contains(astModification)) {
                    doEdit = true;
                }
            } else {
                doEdit = true;
            }
            if (doEdit) {
                LinePosition startLinePos = LinePosition.from(astModification.getStartLine(),
                        astModification.getStartColumn());
                LinePosition endLinePos = LinePosition.from(astModification.getEndLine(),
                        astModification.getEndColumn());
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
