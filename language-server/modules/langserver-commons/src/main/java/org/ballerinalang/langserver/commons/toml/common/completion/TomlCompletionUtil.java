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
package org.ballerinalang.langserver.commons.toml.common.completion;

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
import org.ballerinalang.langserver.commons.toml.TomlCompletionContext;
import org.ballerinalang.langserver.commons.toml.common.TomlCommonUtil;
import org.ballerinalang.langserver.commons.toml.common.TomlSyntaxTreeUtil;
import org.ballerinalang.langserver.commons.toml.visitor.TomlNode;
import org.ballerinalang.langserver.commons.toml.visitor.TomlNodeType;
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
 * Contains utility functions required for providing auto-completion for .toml files.
 *
 * @since 2.0.0
 */
public class TomlCompletionUtil {

    private TomlCompletionUtil() {

    }

    /**
     * Find and set the Toml node information based on the context.
     *
     * @param context
     */
    public static void fillNodeAtCursor(TomlCompletionContext context) {
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
    public static Map<String, CompletionItem> getFilteredCompletions(Either<TableNode,
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
     * @return {@link Map<String,CompletionItem>} Map of top-level node key to completion item.
     */
    public static Map<String, CompletionItem> addTopLevelNodeCompletions(Set<TomlNode> topLevelNodes,
                                                                         Map<String, CompletionItem> completions) {
        for (TomlNode topLevelNode : topLevelNodes) {
            String key = topLevelNode.getKey();
            if (completions.containsKey(key) || !(topLevelNode.type() == TomlNodeType.TABLE ||
                    topLevelNode.type() == TomlNodeType.TABLE_ARRAY)) {
                continue;
            }
            completions.put(key, createTopLevelCompletionItem(topLevelNode));
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
    public static Map<String, CompletionItem> removeExistingTableKeys(Map<String, CompletionItem> completions,
                                                                      DocumentNode documentNode) {
        List<String> existingKeys = completions.keySet().stream()
                .filter(key -> documentNode.members().stream().filter(node -> node.kind() == SyntaxKind.TABLE)
                        .map(node -> TomlSyntaxTreeUtil.toQualifiedName(((TableNode) node).identifier().value()))
                        .collect(Collectors.toSet()).contains(key)).collect(Collectors.toList());
        for (String key : existingKeys) {
            completions.remove(key);
        }
        return completions;
    }

    /**
     * Generates and returns completion item for a given top-level node.
     *
     * @param node Toplevel node.
     * @return {@link CompletionItem} completion item.
     */
    public static CompletionItem createTopLevelCompletionItem(TomlNode node) {
        CompletionItem item = new CompletionItem();
        item.setInsertText(node.getTomlSyntax());
        if (node.type() == TomlNodeType.TABLE) {
            item.setDetail(TomlSyntaxTreeUtil.TABLE);
        } else if (node.type() == TomlNodeType.TABLE_ARRAY) {
            item.setDetail(TomlSyntaxTreeUtil.TABLE_ARRAY);
        } else {
            item.setDetail("Top Level Node");
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
     * @param snippets        completion items.
     * @param topLevelNodeKey qualified table or table array key.
     * @return {@link Map<String, CompletionItem>} completion items of properties under the provided top-level node.
     */
    public static Map<String, CompletionItem> findCompletionItemsFromQualifiedKey(Map<TomlNode, Map<String,
            CompletionItem>> snippets, String topLevelNodeKey) {
        List<Map.Entry<TomlNode, Map<String, CompletionItem>>> filteredMap =
                snippets.entrySet().stream().filter(entry ->
                        topLevelNodeKey.equals((entry.getKey().getKey()))).collect(Collectors.toList());
        if (filteredMap.size() == 1) {
            return filteredMap.get(0).getValue();
        }
        return Collections.emptyMap();
    }
}
