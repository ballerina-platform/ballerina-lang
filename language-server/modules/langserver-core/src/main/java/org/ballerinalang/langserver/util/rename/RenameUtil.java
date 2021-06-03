/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util.rename;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A utility to handle renaming related operations.
 *
 * @since 2.0.0
 */
public class RenameUtil {

    private RenameUtil() {
    }

    /**
     * Check if the provided position is valid for renaming. If valid, returns a valid range. Empty otherwise.
     *
     * @param context Reference context
     * @return A range if position is valid for rename
     */
    public static Optional<Range> prepareRename(ReferencesContext context) {
        fillTokenInfoAtCursor(context);

        // Token at cursor is checked at cursor position and cursor position - 1 column due to left associativity.
        //      Ex: int val<cursor>;
        // Here, the token at cursor will be ";", but user expects "val". To achieve this, we try col-1.
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, context.getCursorPosition())
                .filter(token -> token instanceof IdentifierToken)
                .or(() -> {
                    Position originalPos = context.getCursorPosition();
                    Position newPos = new Position(originalPos.getLine(), originalPos.getCharacter() - 1);
                    return TokensUtil.findTokenAtPosition(context, newPos);
                })
                .orElse(null);
        // Check if token at cursor is an identifier
        if (!(tokenAtCursor instanceof IdentifierToken) || CommonUtil.isKeyword(tokenAtCursor.text())) {
            return Optional.empty();
        }
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            return Optional.empty();
        }

        Range cursorPosRange = new Range(context.getCursorPosition(), context.getCursorPosition());
        NonTerminalNode nodeAtCursor = CommonUtil.findNode(cursorPosRange, document.get().syntaxTree());

        if (onImportDeclarationNode(context, nodeAtCursor)) {
            return Optional.empty();
        }

        return Optional.of(CommonUtil.toRange(tokenAtCursor.lineRange()));
    }
    
    /**
     * Process a rename request and returns the text edits required to be made to complete the rename.
     *
     * @param context Context
     * @param newName Assigned identifier after renaming
     * @return Text edits for that rename operation
     */
    public static Map<String, List<TextEdit>> rename(ReferencesContext context, String newName) {
        fillTokenInfoAtCursor(context);

        if (!CommonUtil.isValidIdentifier(newName)) {
            throw new UserErrorException("Invalid identifier provided");
        }

        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            return Collections.emptyMap();
        }

        Range cursorPosRange = new Range(context.getCursorPosition(), context.getCursorPosition());
        NonTerminalNode nodeAtCursor = CommonUtil.findNode(cursorPosRange, document.get().syntaxTree());

        // For clients that doesn't support prepare rename, we do this check here as well
        if (onImportDeclarationNode(context, nodeAtCursor)) {
            return Collections.emptyMap();
        }
        if (QNameReferenceUtil.onModulePrefix(context, nodeAtCursor)) {
            return handleQNameReferenceRename(context, document.get(), nodeAtCursor, newName);
        }
        if (onImportPrefixNode(context, nodeAtCursor)) {
            return handleImportPrefixRename(context, document.get(), nodeAtCursor, newName);
        }

        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);

        Map<String, List<TextEdit>> changes = new HashMap<>();
        locationMap.forEach((module, locations) ->
                locations.forEach(location -> {
                    String uri = ReferencesUtil.getUriFromLocation(module, location);
                    List<TextEdit> textEdits = changes.computeIfAbsent(uri, k -> new ArrayList<>());
                    textEdits.add(new TextEdit(ReferencesUtil.getRange(location), newName));
                }));
        return changes;
    }

    private static boolean onImportPrefixNode(ReferencesContext context, NonTerminalNode node) {
        if (node.kind() != SyntaxKind.IMPORT_PREFIX) {
            return false;
        }

        ImportPrefixNode importPrefixNode = (ImportPrefixNode) node;
        int cursor = context.getCursorPositionInTree();
        return importPrefixNode.prefix().textRange().startOffset() <= cursor &&
                cursor <= importPrefixNode.prefix().textRange().endOffset();
    }

    private static boolean onImportDeclarationNode(ReferencesContext context, NonTerminalNode node) {
        while (node != null && node.kind() != SyntaxKind.IMPORT_DECLARATION) {
            node = node.parent();
        }

        if (node == null) {
            return false;
        }

        ImportDeclarationNode importDeclarationNode = (ImportDeclarationNode) node;
        int cursor = context.getCursorPositionInTree();
        SeparatedNodeList<IdentifierToken> moduleNames = importDeclarationNode.moduleName();
        int startOffset;
        if (importDeclarationNode.orgName().isPresent()) {
            startOffset = importDeclarationNode.orgName().get().textRange().startOffset();
        } else if (!moduleNames.isEmpty()) {
            startOffset = moduleNames.get(0).textRange().startOffset();
        } else {
            return false;
        }

        return !moduleNames.isEmpty() && startOffset <= cursor &&
                cursor <= moduleNames.get(moduleNames.size() - 1).textRange().endOffset();
    }

    private static Map<String, List<TextEdit>> handleQNameReferenceRename(ReferencesContext context,
                                                                          Document document,
                                                                          NonTerminalNode nodeAtCursor,
                                                                          String newName) {
        QualifiedNameReferenceNode qNameRefNode = (QualifiedNameReferenceNode) nodeAtCursor;
        String moduleOrAlias = qNameRefNode.modulePrefix().text();

        ModulePartNode modulePartNode = document.syntaxTree().rootNode();
        Optional<ImportDeclarationNode> importDeclarationNode = modulePartNode.imports().stream()
                .filter(importDeclaration -> {
                    CodeActionModuleId moduleId = CodeActionModuleId.from(importDeclaration);
                    if (!StringUtils.isEmpty(moduleId.modulePrefix())) {
                        return moduleId.modulePrefix().equals(moduleOrAlias);
                    }

                    // TODO Should address cases like import project1.mymod1; import project1.mod1;
                    return moduleId.moduleName().endsWith(moduleOrAlias);
                })
                .findFirst();

        if (importDeclarationNode.isEmpty()) {
            return Collections.emptyMap();
        }

        return handleImportDeclarationRename(context, document, importDeclarationNode.get(), newName);
    }

    private static Map<String, List<TextEdit>> handleImportDeclarationRename(ReferencesContext context,
                                                                             Document document,
                                                                             ImportDeclarationNode importDeclaration,
                                                                             String newName) {
        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);
        Map<String, List<TextEdit>> changes = new HashMap<>();

        // Filter location map for refs in same doc
        locationMap.entrySet().stream()
                .filter(moduleListEntry -> moduleListEntry.getKey().moduleId().equals(document.documentId().moduleId()))
                .forEach(moduleLocations -> {
                    Module module = moduleLocations.getKey();
                    List<Location> locations = moduleLocations.getValue();
                    locations.forEach(location -> {
                        String fileUri = ReferencesUtil.getUriFromLocation(module, location);
                        // If within the same file
                        if (!context.fileUri().equals(fileUri)) {
                            return;
                        }
                        // If location is within import declaration node
                        if (CommonUtil.isWithinLineRange(location.lineRange(), importDeclaration.lineRange()) &&
                                importDeclaration.prefix().isEmpty()) {
                            // If there's no prefix, we have to add " as $newName" to the import
                            SeparatedNodeList<IdentifierToken> moduleNames = importDeclaration.moduleName();
                            LinePosition endPos = moduleNames.get(moduleNames.size() - 1).lineRange().endLine();
                            Range range = new Range(CommonUtil.toPosition(endPos), CommonUtil.toPosition(endPos));
                            List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                            textEdits.add(new TextEdit(range, " as " + newName));
                        } else {
                            List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                            textEdits.add(new TextEdit(ReferencesUtil.getRange(location), newName));
                        }
                    });
                });
        return changes;
    }

    private static Map<String, List<TextEdit>> handleImportPrefixRename(ReferencesContext context,
                                                                        Document document,
                                                                        NonTerminalNode nodeAtCursor,
                                                                        String newName) {
        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);

        Map<String, List<TextEdit>> changes = new HashMap<>();
        // Filter location map for refs in same doc
        locationMap.entrySet().stream()
                .filter(moduleListEntry -> moduleListEntry.getKey().moduleId().equals(document.documentId().moduleId()))
                .forEach(moduleLocations -> {
                    Module module = moduleLocations.getKey();
                    List<Location> locations = moduleLocations.getValue();
                    locations.forEach(location -> {
                        String fileUri = ReferencesUtil.getUriFromLocation(module, location);
                        // If within the same file
                        if (!context.fileUri().equals(fileUri)) {
                            return;
                        }

                        List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                        textEdits.add(new TextEdit(ReferencesUtil.getRange(location), newName));
                    });
                });
        return changes;
    }

    private static void fillTokenInfoAtCursor(ReferencesContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
    }
}
