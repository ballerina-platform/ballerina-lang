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
import org.ballerinalang.langserver.commons.LanguageServerContext;
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
import java.util.Collections;
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
    public static List<CompletionItem> getCompletionItems(TomlCompletionContext ctx,
                                                          LanguageServerContext serverContext) {
        fillNodeAtCursor(ctx);
        return getCompletionItemsBasedOnCursor(ctx, serverContext);
    }

    /**
     * Get the matching parent table for the current node.
     *
     * @return completion items for the current context.
     */
    public static List<CompletionItem> getCompletionItemsBasedOnCursor(TomlCompletionContext ctx,
                                                                       LanguageServerContext serverContext) {
        Node node = ctx.getNodeAtCursor();
        if (node == null) {
            return Collections.emptyList();
        }
        Map<String, CompletionItem> completions;
        Node reference = node;
        //Gets all the support completions from Schema.
        Map<Parent, Map<String, CompletionItem>> c2cSnippets =
                TomlSnippetManager.getInstance(serverContext).getCompletions();

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
        return new ArrayList<>(addRooTablesToCompletions(c2cSnippets.keySet(), new HashMap<>()).values());
    }

    private static Map<String, CompletionItem> getTableArrayCompletions(TableArrayNode arrayNode, Map<Parent,
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
    public static void fillNodeAtCursor(TomlCompletionContext context) {
        Path tomlFilePath = context.filePath();
        //Completion gets called only from Kubernetes.toml in a project.
        SyntaxTree st = context.workspace().project(tomlFilePath).orElseThrow().currentPackage().kubernetesToml()
                .orElseThrow().tomlDocument().syntaxTree();
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

    private static Map<String, CompletionItem> getTableCompletions(TableNode tableNode,
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

    private static Map<String, CompletionItem> addRooTablesToCompletions(Set<Parent> parents,
                                                                         Map<String, CompletionItem> completions) {
        for (Parent parent : parents) {
            String key = parent.getKey();
            if (completions.containsKey(key)) {
                continue;
            }
            ParentType type = parent.getType();
            CompletionItem item;
            if (type == ParentType.TABLE) {
                Table objectNode = new Table(key);
                item = createRootCompletionItem(key, TomlSyntaxTreeUtil.TABLE, objectNode.prettyPrint());
            } else if (type == ParentType.TABLE_ARRAY) {
                TableArray objectNode = new TableArray(key);
                item = createRootCompletionItem(key, TomlSyntaxTreeUtil.TABLE_ARRAY, objectNode.prettyPrint());
            } else {
                continue;
            }
            completions.put(key, item);
        }
        return completions;
    }

    private static CompletionItem createRootCompletionItem(String key, String type, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setInsertText(insertText);
        item.setDetail(type);
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setSortText(SortingUtil.genSortText(3));
        return item;
    }

    private static Map<String, CompletionItem> getChildFromDottedKey(Map<Parent, Map<String, CompletionItem>> snippets,
                                                                     String tableKey) {
        for (Map.Entry<Parent, Map<String, CompletionItem>> entry : snippets.entrySet()) {
            Parent key = entry.getKey();
            if (key.getKey().equals(tableKey)) {
                return entry.getValue();
            }
        }
        return Collections.emptyMap();
    }

    private static List<String> findExistingChildEntries(TableNode tableNode, Map<String, CompletionItem> completions) {
        DocumentNode documentNode = (DocumentNode) tableNode.parent();
        return findExistingChildEntriesInDocument(documentNode, completions);
    }

    private static List<String> findExistingChildEntries(TableArrayNode tableArrayNode,
                                                         Map<String, CompletionItem> completions) {
        DocumentNode documentNode = (DocumentNode) tableArrayNode.parent();
        return findExistingChildEntriesInDocument(documentNode, completions);
    }

    private static List<String> findExistingChildEntriesInDocument(DocumentNode documentNode,
                                                                   Map<String, CompletionItem> completions) {
        List<String> removal = new ArrayList<>();
        for (Map.Entry<String, CompletionItem> entry : completions.entrySet()) {
            String completionKey = entry.getKey();
            removal.addAll(findEntryInChildTables(documentNode, completionKey));
        }
        return removal;
    }

    private static List<String> findEntryInChildTables(DocumentNode documentNode, String completionKey) {
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

    private static Map<String, CompletionItem> removeExistingChildTables(Map<String, CompletionItem> completions,
                                                                         List<String> removal) {
        for (String key : removal) {
            completions.remove(key);
        }
        return completions;
    }
}
