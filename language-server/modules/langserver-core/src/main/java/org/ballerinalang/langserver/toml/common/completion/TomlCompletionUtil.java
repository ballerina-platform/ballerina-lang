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
package org.ballerinalang.langserver.toml.common.completion;

import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.commons.toml.AbstractTomlCompletionContext;
import org.ballerinalang.langserver.toml.common.TomlCommonUtil;
import org.ballerinalang.langserver.toml.common.TomlSyntaxTreeUtil;
import org.ballerinalang.langserver.toml.common.completion.visitor.TomlNode;
import org.ballerinalang.langserver.toml.common.completion.visitor.TomlNodeType;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the Toml completion utils.
 *
 * @since 2.0.0
 */
public abstract class TomlCompletionUtil {

    /**
     * Find and set the Toml node information based on the context.
     *
     * @param context
     */
    protected static void fillNodeAtCursor(AbstractTomlCompletionContext context) {
        Optional<SyntaxTree> st = context.getTomlSyntaxTree();
        if (st.isEmpty()) {
            return;
        }
        Position position = context.getCursorPosition();
        TextDocument textDocument = st.get().textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((DocumentNode) st.get().rootNode()).findNode(range);
        while (nonTerminalNode.parent() != null && !TomlSyntaxTreeUtil.withinTextRange(txtPos, nonTerminalNode)) {
            //Takes the top-level Node of the immediate cursor position
            nonTerminalNode = nonTerminalNode.parent();
        }
        context.setNodeAtCursor(nonTerminalNode);
    }

    /**
     * Returns the filtered completion items in the provided top-level node context.
     *
     * @param topLevelNode The toplevel node to which the completion items belong to.
     * @param snippets     Set of completion items to be filtered.
     * @return {@link Map<String,CompletionItem> } The set of filtered completion items.
     */
    protected static Map<String, CompletionItem> getFilteredCompletions(Either<TableNode,
            TableArrayNode> topLevelNode, Map<TomlNode, Map<String, CompletionItem>> snippets) {

        String tableKey;
        NodeList<KeyValueNode> fields;
        DocumentNode documentNode;
        if (topLevelNode.isLeft()) {
            tableKey = TomlSyntaxTreeUtil.toQualifiedName(topLevelNode.getLeft().identifier().value());
            fields = topLevelNode.getLeft().fields();
            documentNode = (DocumentNode) topLevelNode.getLeft().parent();
        } else {
            tableKey = TomlSyntaxTreeUtil.toQualifiedName(topLevelNode.getRight().identifier().value());
            fields = topLevelNode.getRight().fields();
            documentNode = (DocumentNode) topLevelNode.getRight().parent();
        }

        Map<String, CompletionItem> completions =
                new HashMap<>(findCompletionItemsFromQualifiedKey(snippets, tableKey));

        /*   Removes the following.
            eg: [build-options]
                cloud="docker"
            The completion item that corresponds to the above key-value pair
            will be removed as it is already defined.
         */
        for (KeyValueNode field : fields) {
            String key = TomlSyntaxTreeUtil.toQualifiedName(field.identifier().value());
            completions.remove(key);
        }

        /*
            Add all possible top-level (Table & TableArray) keys to the completion-item list.
            And remove already existing keys from the completion item list.
            eg: [build-options],[package]
         */
        addTopLevelNodeCompletions(snippets.keySet(), completions);
        return removeExistingTableKeys(completions, documentNode);
    }

    /**
     * Returns the set of  completion items for the given set of toml nodes.
     *
     * @param topLevelNodes The set of top-level toml nodes.
     * @param completions   The set of completion items.
     * @return
     */
    protected static Map<String, CompletionItem> addTopLevelNodeCompletions(Set<TomlNode> topLevelNodes,
                                                                            Map<String, CompletionItem> completions) {
        for (TomlNode topLevelNode : topLevelNodes) {
            String key = topLevelNode.getKey();
            if (completions.containsKey(key)) {
                continue;
            }
            CompletionItem item;
            if (topLevelNode.type() == TomlNodeType.TABLE || topLevelNode.type() == TomlNodeType.TABLE_ARRAY) {
                item = createTopLevelCompletionItem(topLevelNode);
            } else {
                continue;
            }
            completions.put(key, item);
        }
        return completions;
    }

    /**
     * Removes already declared Table completion items from the completion item list.
     *
     * @param completions  The set completion items
     * @param documentNode Toml document node.
     * @return {@link Map<String,CompletionItem>} The optimized completion item list.
     */
    protected static Map<String, CompletionItem> removeExistingTableKeys(Map<String, CompletionItem> completions,
                                                                         DocumentNode documentNode) {
        List<String> existingKeys = completions.entrySet().stream()
                .filter(entry -> isTableDeclared(documentNode, entry.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        for (String key : existingKeys) {
            completions.remove(key);
        }
        return completions;
    }

    /**
     * Returns if the given table key is alreday declared in the Toml document context.
     *
     * @param documentNode Toml document node.
     * @param tableKey     table key.
     * @return {@link Boolean}
     */
    protected static Boolean isTableDeclared(DocumentNode documentNode, String tableKey) {
        NodeList<DocumentMemberDeclarationNode> members = documentNode.members();
        for (DocumentMemberDeclarationNode documentMemberNode : members) {
            if (documentMemberNode.kind() == SyntaxKind.TABLE) {
                TableNode rootTableNode = (TableNode) documentMemberNode;
                String rootTableNodeKey = TomlSyntaxTreeUtil.toQualifiedName(rootTableNode.identifier().value());
                if (tableKey.equals(rootTableNodeKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates and returns completion item for a given top-level node.
     *
     * @param node Toplevel node.
     * @return {@link CompletionItem} completion item.
     */
    protected static CompletionItem createTopLevelCompletionItem(TomlNode node) {
        CompletionItem item = new CompletionItem();
        item.setInsertText(node.getTomlSyntax());
        if (node.type() == TomlNodeType.TABLE) {
            item.setDetail(TomlSyntaxTreeUtil.TABLE);
        } else {
            item.setDetail(TomlSyntaxTreeUtil.TABLE_ARRAY);
        }
        item.setLabel(node.getKey());
        item.setKind(CompletionItemKind.Snippet);
        /*
            Top-level nodes are ranked 3 as it will appear after the key-values pairs in the context.
            eg: [build-options]
                cloud
                codeCov
                ...
                [package]
         */
        item.setSortText(TomlCommonUtil.genSortText(3));
        return item;
    }

    /**
     * Finds and returns the set of completion items corresponding to a given top-level node key.
     *
     * @param snippets completion items.
     * @param tableKey qualified table key.
     * @return
     */
    protected static Map<String, CompletionItem> findCompletionItemsFromQualifiedKey(Map<TomlNode, Map<String,
            CompletionItem>> snippets, String tableKey) {
        List<Map.Entry<TomlNode, Map<String, CompletionItem>>> filteredMap =
                snippets.entrySet().stream().filter(entry ->
                        tableKey.equals((entry.getKey().getKey()))).collect(Collectors.toList());
        if (filteredMap.size() == 1) {
            return filteredMap.get(0).getValue();
        }
        return Collections.emptyMap();
    }
}
