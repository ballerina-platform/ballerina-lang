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
package org.ballerinalang.langserver.rename;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
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
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.PrepareRenameContext;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.commons.RenameContext;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.contexts.BallerinaContextUtils;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.references.ReferencesUtil;
import org.eclipse.lsp4j.AnnotatedTextEdit;
import org.eclipse.lsp4j.ChangeAnnotation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
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
    public static Optional<Range> prepareRename(PrepareRenameContext context) {
        fillTokenInfoAtCursor(context);
        context.checkCancelled();
        
        // Token at cursor is checked at cursor position and cursor position - 1 column due to left associativity.
        //      Ex: int val<cursor>;
        // Here, the token at cursor will be ";", but user expects "val". To achieve this, we try col-1.
        Token tokenAtCursor = PositionUtil.findTokenAtPosition(context, context.getCursorPosition())
                .filter(token -> token instanceof IdentifierToken)
                .or(() -> {
                    Position originalPos = context.getCursorPosition();
                    Position newPos = new Position(originalPos.getLine(), originalPos.getCharacter() - 1);
                    return PositionUtil.findTokenAtPosition(context, newPos);
                })
                .orElse(null);
        // Check if token at cursor is an identifier
        if (!(tokenAtCursor instanceof IdentifierToken) || SyntaxInfo.isKeyword(tokenAtCursor.text()) 
                || isSelfClassSymbol(context)) {
            return Optional.empty();
        }
        
        // Check if symbol at cursor is empty
        Optional<Symbol> symbolAtCursor = ReferencesUtil.getSymbolAtCursor(context);
        if (symbolAtCursor.isEmpty()
                || symbolAtCursor.get().kind() == SymbolKind.RESOURCE_METHOD
                || symbolAtCursor.get().kind() == SymbolKind.PATH_PARAMETER
                || symbolAtCursor.get().kind() == SymbolKind.PATH_NAME_SEGMENT) {
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
        
        // If the node at cursor is qualified name reference with no matching import, we don't allow it to be renamed
        if (QNameRefCompletionUtil.onModulePrefix(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRefNode = (QualifiedNameReferenceNode) nodeAtCursor;
            Optional<ImportDeclarationNode> importDeclaration =
                    getImportDeclarationNodeForQNameReference(document.get(), qNameRefNode);
            if (importDeclaration.isEmpty()) {
                return Optional.empty();
            }
        }

        return Optional.of(PositionUtil.toRange(tokenAtCursor.lineRange()));
    }

    private static Map<String, List<TextEdit>> getChanges(
            RenameContext context) {

        fillTokenInfoAtCursor(context);
        String newName = context.getParams().getNewName();
        // We don't allow invalid identifiers. But allow annotated text edits for keywords.
        if (!SyntaxInfo.isIdentifier(newName) && !SyntaxInfo.isKeyword(newName)) {
            throw new UserErrorException("Invalid identifier provided");
        }

        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            return Collections.emptyMap();
        }

        Range cursorPosRange = new Range(context.getCursorPosition(), context.getCursorPosition());
        NonTerminalNode nodeAtCursor = CommonUtil.findNode(cursorPosRange, document.get().syntaxTree());

        Optional<Symbol> symbolAtCursor = ReferencesUtil.getSymbolAtCursor(context);
        // For clients that doesn't support prepare rename, we do this check here as well
        if (onImportDeclarationNode(context, nodeAtCursor)
                || (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE && isSelfClassSymbol(context)) || 
                (symbolAtCursor.isPresent() && symbolAtCursor.get().kind() == SymbolKind.RESOURCE_METHOD)) {
            return Collections.emptyMap();
        }

        if (QNameRefCompletionUtil.onModulePrefix(context, nodeAtCursor)) {
            return handleQNameReferenceRename(context, document.get(), nodeAtCursor);
        }
        if (onImportPrefixNode(context, nodeAtCursor)) {
            return handleImportPrefixRename(context, document.get(), nodeAtCursor);
        }

        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);
        Map<String, List<TextEdit>> changes = new HashMap<>();

        for (Map.Entry<Module, List<Location>> entry : locationMap.entrySet()) {
            Module module = entry.getKey();
            List<Location> locations = entry.getValue();
            for (Location location : locations) {
                String uri = PathUtil.getUriFromLocation(module, location);
                Range editRange = PathUtil.getRange(location);

                // CHeck for field binding pattern var name nodes in references
                // Ex: 
                //      type Person record { string name; int id; };
                //      Person p = {name: "name1", id: 1};
                //      var {name, id} = p;
                // Here var named name in mapping binding pattern need to be handled as a special case
                Path path = PathUtil.getPathFromLocation(module, location);
                Optional<FieldBindingPatternVarnameNode> bindingPatternVarNode = context.workspace().syntaxTree(path)
                        .map(syntaxTree -> CommonUtil.findNode(editRange, syntaxTree))
                        .flatMap(RenameUtil::getFieldBindingPatternVarNameNode);

                List<TextEdit> textEdits = changes.computeIfAbsent(uri, k -> new ArrayList<>());
                if (context.getHonorsChangeAnnotations() && SyntaxInfo.isKeyword(newName)) {
                    String escapedInsertText = CommonUtil.escapeReservedKeyword(newName);
                    String insertText = newName;
                    // Prepare insert text for both cases separately
                    if (bindingPatternVarNode.isPresent()) {
                        String currentName = bindingPatternVarNode.get().variableName().name().text();
                        escapedInsertText = currentName + ": " + escapedInsertText;
                        insertText = currentName + ": " + insertText;
                    }
                    textEdits.add(new AnnotatedTextEdit(editRange,
                            escapedInsertText, RenameChangeAnnotation.QUOTED_KEYWORD.getID()));
                    textEdits.add(new AnnotatedTextEdit(editRange,
                            insertText, RenameChangeAnnotation.UNQUOTED_KEYWORD.getID()));
                } else {
                    String insertText = newName;
                    if (bindingPatternVarNode.isPresent()) {
                        String currentName = bindingPatternVarNode.get().variableName().name().text();
                        insertText = currentName + ": " + insertText;
                    }
                    textEdits.add(new TextEdit(editRange, insertText));
                }
            }
        }
        
        return changes;
    }

    /**
     * @param context Context
     * @return {@link WorkspaceEdit} Workspace edit of changes.
     */
    public static WorkspaceEdit rename(RenameContext context) {
        Map<String, ChangeAnnotation> changeAnnotationMap = new HashMap<>();
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        Map<String, List<TextEdit>> changes = getChanges(context);
        if (context.getHonorsChangeAnnotations() && SyntaxInfo.isKeyword(context.getParams().getNewName())) {
            changeAnnotationMap.put(RenameChangeAnnotation.QUOTED_KEYWORD.getID(),
                    RenameChangeAnnotation.QUOTED_KEYWORD.getChangeAnnotation());
            changeAnnotationMap.put(RenameChangeAnnotation.UNQUOTED_KEYWORD.getID(),
                    RenameChangeAnnotation.UNQUOTED_KEYWORD.getChangeAnnotation());
            List<Either<TextDocumentEdit, ResourceOperation>> docEdits = new ArrayList<>();
            changes.entrySet().forEach(entry -> {
                TextDocumentEdit edit = new TextDocumentEdit();
                edit.setTextDocument(new VersionedTextDocumentIdentifier(entry.getKey(), null));
                edit.setEdits(entry.getValue());
                docEdits.add(Either.forLeft(edit));
            });
            workspaceEdit.setDocumentChanges(docEdits);
            workspaceEdit.setChangeAnnotations(changeAnnotationMap);
        } else {
            workspaceEdit.setChanges(changes);
        }
        return workspaceEdit;
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
    
    private static Optional<FieldBindingPatternVarnameNode> getFieldBindingPatternVarNameNode(NonTerminalNode node) {
        if (node.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE ||
                node.parent().kind() != SyntaxKind.FIELD_BINDING_PATTERN) {
            return Optional.empty();
        }

        FieldBindingPatternNode fieldBindingPatternNode = (FieldBindingPatternNode) node.parent();
        if (fieldBindingPatternNode instanceof FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
            return Optional.of(fieldBindingPatternVarnameNode);
        }
        return Optional.empty();
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

    private static Map<String, List<TextEdit>> handleQNameReferenceRename(RenameContext context,
                                                                          Document document,
                                                                          NonTerminalNode nodeAtCursor) {
        QualifiedNameReferenceNode qNameRefNode = (QualifiedNameReferenceNode) nodeAtCursor;
        Optional<ImportDeclarationNode> importDeclarationNode =
                getImportDeclarationNodeForQNameReference(document, qNameRefNode);

        if (importDeclarationNode.isEmpty()) {
            return Collections.emptyMap();
        }

        return handleImportDeclarationRename(context, document, importDeclarationNode.get());
    }

    /**
     * Given the document and a qualified name reference node, this method returns the matching import declaration
     * node.
     *
     * @param document     Document
     * @param qNameRefNode Qualified name reference node
     * @return Optional import declaration node
     */
    private static Optional<ImportDeclarationNode> getImportDeclarationNodeForQNameReference(
            Document document, QualifiedNameReferenceNode qNameRefNode) {
        String moduleOrAlias = qNameRefNode.modulePrefix().text();

        ModulePartNode modulePartNode = document.syntaxTree().rootNode();
        return modulePartNode.imports().stream()
                .filter(importDeclaration -> {
                    CodeActionModuleId moduleId = CodeActionModuleId.from(importDeclaration);
                    if (!StringUtils.isEmpty(moduleId.modulePrefix())) {
                        return moduleId.modulePrefix().equals(moduleOrAlias);
                    }

                    // TODO Should address cases like import project1.mymod1; import project1.mod1;
                    return moduleId.moduleName().endsWith(moduleOrAlias);
                })
                .findFirst();
    }

    private static Map<String, List<TextEdit>> handleImportDeclarationRename(RenameContext context,
                                                                             Document document,
                                                                             ImportDeclarationNode importDeclaration) {
        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);
        Map<String, List<TextEdit>> changes = new HashMap<>();
        String newName = context.getParams().getNewName();
        // Filter location map for refs in same doc
        locationMap.entrySet().stream()
                .filter(moduleListEntry -> moduleListEntry.getKey().moduleId().equals(document.documentId().moduleId()))
                .forEach(moduleLocations -> {
                    Module module = moduleLocations.getKey();
                    List<Location> locations = moduleLocations.getValue();
                    locations.forEach(location -> {
                        String fileUri = PathUtil.getUriFromLocation(module, location);
                        // If within the same file
                        if (!context.fileUri().equals(fileUri)) {
                            return;
                        }
                        // If location is within import declaration node
                        Range editRange = PathUtil.getRange(location);
                        if (PositionUtil.isWithinLineRange(location.lineRange(), importDeclaration.lineRange()) &&
                                importDeclaration.prefix().isEmpty()) {
                            // If there's no prefix, we have to add " as $newName" to the import
                            SeparatedNodeList<IdentifierToken> moduleNames = importDeclaration.moduleName();
                            LinePosition endPos = moduleNames.get(moduleNames.size() - 1).lineRange().endLine();
                            Range range = new Range(PositionUtil.toPosition(endPos), PositionUtil.toPosition(endPos));
                            List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                            if (context.getHonorsChangeAnnotations() && SyntaxInfo.isKeyword(newName)) {
                                String escapedNewName = CommonUtil.escapeReservedKeyword(newName);
                                textEdits.add(new AnnotatedTextEdit(editRange,
                                        "as " + escapedNewName, RenameChangeAnnotation.QUOTED_KEYWORD.getID()));
                                textEdits.add(new AnnotatedTextEdit(editRange,
                                        "as " + newName, RenameChangeAnnotation.UNQUOTED_KEYWORD.getID()));
                            } else {
                                textEdits.add(new TextEdit(range, " as " + newName));
                            }
                        } else {
                            List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                            if (context.getHonorsChangeAnnotations() && SyntaxInfo.isKeyword(newName)) {
                                String escapedNewName = CommonUtil.escapeReservedKeyword(newName);
                                textEdits.add(new AnnotatedTextEdit(editRange, escapedNewName,
                                        RenameChangeAnnotation.QUOTED_KEYWORD.getID()));
                                textEdits.add(new AnnotatedTextEdit(editRange, newName,
                                        RenameChangeAnnotation.UNQUOTED_KEYWORD.getID()));
                            } else {
                                textEdits.add(new TextEdit(PathUtil.getRange(location), newName));
                            }
                        }
                    });
                });
        return changes;
    }

    private static Map<String, List<TextEdit>> handleImportPrefixRename(
            RenameContext context,
            Document document,
            NonTerminalNode nodeAtCursor) {

        String newName = context.getParams().getNewName();
        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);
        Map<String, List<TextEdit>> changes = new HashMap<>();
        // Filter location map for refs in same doc
        locationMap.entrySet().stream()
                .filter(moduleListEntry -> moduleListEntry.getKey().moduleId().equals(document.documentId().moduleId()))
                .forEach(moduleLocations -> {
                    Module module = moduleLocations.getKey();
                    List<Location> locations = moduleLocations.getValue();
                    locations.forEach(location -> {
                        String fileUri = PathUtil.getUriFromLocation(module, location);
                        // If within the same file
                        if (!context.fileUri().equals(fileUri)) {
                            return;
                        }
                        List<TextEdit> textEdits = changes.computeIfAbsent(fileUri, k -> new ArrayList<>());
                        Range editRange = PathUtil.getRange(location);
                        if (context.getHonorsChangeAnnotations() && SyntaxInfo.isKeyword(newName)) {
                            String escapedNewName = ModuleUtil.escapeModuleName(newName);
                            textEdits.add(new AnnotatedTextEdit(editRange,
                                    escapedNewName, RenameChangeAnnotation.QUOTED_KEYWORD.getID()));
                            textEdits.add(new AnnotatedTextEdit(editRange,
                                    newName, RenameChangeAnnotation.UNQUOTED_KEYWORD.getID()));
                        } else {
                            textEdits.add(new TextEdit(editRange, newName));
                        }
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
    
    private static boolean isSelfClassSymbol(ReferencesContext context) {
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (srcFile.isEmpty() || semanticModel.isEmpty() || context.currentSyntaxTree().isEmpty()) {
            return false;
        }
        Position position = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(position.getLine(), position.getCharacter());
        Optional<Symbol> symbol = semanticModel.get().symbol(srcFile.get(), linePosition);
        if (symbol.isEmpty()) {
            return false;
        }
        Optional<ModuleMemberDeclarationNode> enclosingNode = BallerinaContextUtils.
                getEnclosingModuleMember(context.currentSyntaxTree().get(), context.getCursorPositionInTree());
        if (enclosingNode.isEmpty()) {
            return false;
        }
        return SymbolUtil.isSelfClassSymbol(symbol.get(), context, enclosingNode.get());
    }

    private enum RenameChangeAnnotation {
        QUOTED_KEYWORD("Quoted Rename", "Rename to keyword with a quote", true, "quoted"),
        UNQUOTED_KEYWORD("Un-quoted Rename", "Rename to keyword without a quote", true, "unquoted");

        private final String id;
        private final String label;
        private final String description;
        private final Boolean needsConfirmation;

        RenameChangeAnnotation(String label, String description, Boolean needsConfirmation, String id) {
            this.id = id;
            this.label = label;
            this.description = description;
            this.needsConfirmation = needsConfirmation;
        }

        public String getID() {
            return this.id;
        }

        public ChangeAnnotation getChangeAnnotation() {
            ChangeAnnotation changeAnnotation = new ChangeAnnotation(this.label);
            changeAnnotation.setDescription(this.description);
            changeAnnotation.setNeedsConfirmation(this.needsConfirmation);
            return changeAnnotation;
        }
    }
}
