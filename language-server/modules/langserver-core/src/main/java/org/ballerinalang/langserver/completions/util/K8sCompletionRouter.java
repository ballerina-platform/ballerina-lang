/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.completions.TomlCompletionContext;
import org.ballerinalang.langserver.completions.toml.Parent;
import org.ballerinalang.langserver.completions.toml.ParentType;
import org.ballerinalang.langserver.completions.toml.Table;
import org.ballerinalang.langserver.completions.toml.TableArray;
import org.ballerinalang.langserver.completions.toml.TomlSnippetManager;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for providing completion items based on the context.
 *
 * @since 2.0.0
 */
public class K8sCompletionRouter {

    /**
     * Get the completion Items for Kubernetes.toml depending on the the context.
     *
     * @param ctx Completion context
     * @return {@link List} List of resolved completion Items
     */
    public List<CompletionItem> getCompletionItems(TomlCompletionContext ctx) {
        fillNodeAtCursor(ctx);
        return getCompletionItemsBasedOnCursor(ctx);
    }

    /**
     * Get the matching parent table for the current node.
     *
     * @return completion items for the current context.
     */
    public List<CompletionItem> getCompletionItemsBasedOnCursor(TomlCompletionContext ctx) {
        Node node = ctx.getNodeAtCursor();
        if (node == null) {
            return new ArrayList<>();
        }
        Map<String, CompletionItem> completions = new HashMap<>();
        Node reference = node;
        //Gets all the support completions from Schema. TODO Remove singlethon and add this to startup
        Map<Parent, Map<String, CompletionItem>> c2cSnippets = TomlSnippetManager.getInstance().getCompletions();

        //In Kubernetes.toml all the suggestions are support based on the current Table/Table of Arrray.
        //This loop checks the parent of the current node until table or table of array found.
        while (reference != null) {
            if (reference.kind() == SyntaxKind.TABLE) {
                completions = getTableCompletions((TableNode) reference, c2cSnippets);
                return new ArrayList<>(completions.values());
            }
            if (reference.kind() == SyntaxKind.TABLE_ARRAY) {
                completions = getTableArrayCompletions((TableArrayNode) reference, c2cSnippets);
                return new ArrayList<>(completions.values());
            }
            reference = reference.parent();
        }
        return new ArrayList<>(completions.values());
    }

    private Map<String, CompletionItem> getTableArrayCompletions(TableArrayNode arrayNode, Map<Parent,
            Map<String, CompletionItem>> snippets) {
        String tableArrayName = TomlSyntaxTreeUtil.toDottedString(arrayNode.identifier());
        Map<String, CompletionItem> completions = new HashMap<>(getChildFromDottedKey(snippets, tableArrayName));

        for (KeyValueNode field : arrayNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(field.identifier());
            completions.remove(key);
        }

        //Add main completions to completions
        addRooTablesToCompletions(snippets.keySet(), completions);

        //Remove Existing document level entries from completion
        List<String> existingChildEntries = findExistingChildEntries(arrayNode, completions);
        return removeExistingChildTables(completions, existingChildEntries);
    }

    /**
     * Find the node based on the cursor position.
     */
    public void fillNodeAtCursor(TomlCompletionContext context) {
        Path tomlFilePath = context.filePath();
        SyntaxTree st = TomlSyntaxTreeUtil.getTomlSyntaxTree(tomlFilePath).orElseThrow();
        Position position = context.getCursorPosition();
        TextDocument textDocument = st.textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((DocumentNode) st.rootNode()).findNode(range);
        while (nonTerminalNode.parent() != null && !TomlSyntaxTreeUtil.withinTextRange(txtPos, nonTerminalNode)) {
            //Takes the parent Node of the immediate cursor position
            nonTerminalNode = nonTerminalNode.parent();
        }
        context.setNodeAtCursor(nonTerminalNode);
    }

    private Map<String, CompletionItem> getTableCompletions(TableNode tableNode,
                                                            Map<Parent, Map<String, CompletionItem>> snippets) {

        String tableKey = TomlSyntaxTreeUtil.toDottedString(tableNode.identifier());
        Map<String, CompletionItem> completions = new HashMap<>(getChildFromDottedKey(snippets, tableKey));

        for (KeyValueNode field : tableNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(field.identifier());
            completions.remove(key);
        }
        //Add main completions to completions
        addRooTablesToCompletions(snippets.keySet(), completions);

        //Remove Existing document level entries from completion
        List<String> existingChildEntries = findExistingChildEntries(tableNode, completions);
        return removeExistingChildTables(completions, existingChildEntries);
    }

    private void addRooTablesToCompletions(Set<Parent> parents, Map<String, CompletionItem> completions) {
        for (Parent parent : parents) {
            String key = parent.getKey();
            ParentType type = parent.getType();
            CompletionItem item = new CompletionItem();
            if (type == ParentType.TABLE) {
                Table objectNode = new Table(key);
                item.setInsertText(objectNode.prettyPrint());
                item.setDetail(TomlSyntaxTreeUtil.TABLE);
                item.setLabel(key);
                item.setKind(CompletionItemKind.Snippet);
                item.setInsertTextFormat(InsertTextFormat.Snippet);
            } else if (type == ParentType.TABLE_ARRAY) {
                TableArray objectNode = new TableArray(key);
                item.setInsertText(objectNode.prettyPrint());
                item.setLabel(key);
                item.setKind(CompletionItemKind.Snippet);
                item.setInsertTextFormat(InsertTextFormat.Snippet);
                item.setDetail(TomlSyntaxTreeUtil.TABLE_ARRAY);
            } else {
                continue;
            }
            completions.put(key, item);
        }
    }

    private Map<String, CompletionItem> getChildFromDottedKey(Map<Parent, Map<String, CompletionItem>> snippets,
                                                              String tableKey) {
        for (Map.Entry<Parent, Map<String, CompletionItem>> entry : snippets.entrySet()) {
            Parent key = entry.getKey();
            if (key.getKey().equals(tableKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<String> findExistingChildEntries(TableNode tableNode, Map<String, CompletionItem> completions) {
        DocumentNode documentNode = (DocumentNode) tableNode.parent();
        return findExistingChildEntriesInDocument(documentNode, completions);
    }

    private List<String> findExistingChildEntries(TableArrayNode tableArrayNode,
                                                  Map<String, CompletionItem> completions) {
        DocumentNode documentNode = (DocumentNode) tableArrayNode.parent();
        return findExistingChildEntriesInDocument(documentNode, completions);
    }

    private List<String> findExistingChildEntriesInDocument(DocumentNode documentNode,
                                                            Map<String, CompletionItem> completions) {
        List<String> removal = new ArrayList<>();
        for (Map.Entry<String, CompletionItem> entry : completions.entrySet()) {
            String completionKey = entry.getKey();
            removal.addAll(findEntryInChildTables(documentNode, completionKey));
        }
        return removal;
    }

    private List<String> findEntryInChildTables(DocumentNode documentNode, String completionKey) {
        List<String> removal = new ArrayList<>();
        NodeList<DocumentMemberDeclarationNode> members = documentNode.members();
        for (DocumentMemberDeclarationNode documentMemberNode : members) {
            if (documentMemberNode.kind() == SyntaxKind.TABLE) {
                TableNode rootTableNode = (TableNode) documentMemberNode;
                String rootTableNodeKey = TomlSyntaxTreeUtil.toDottedString(rootTableNode.identifier());
                if (completionKey.equals(rootTableNodeKey)) {
                    removal.add(rootTableNodeKey);
                    break;
                }
            }
        }
        return removal;
    }

    private Map<String, CompletionItem> removeExistingChildTables(Map<String, CompletionItem> completions,
                                                                  List<String> removal) {
        for (String key : removal) {
            completions.remove(key);
        }
        return completions;
    }
}
