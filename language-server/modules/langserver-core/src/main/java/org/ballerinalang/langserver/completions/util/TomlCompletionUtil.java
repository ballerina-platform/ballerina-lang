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
package org.ballerinalang.langserver.completions.util;

import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.completions.TomlCompletionContext;
import org.ballerinalang.langserver.completions.toml.C2CSnippetManager;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common utility methods for the completion operation.
 */
public class TomlCompletionUtil {

    /**
     * Get the completion Items for the context.
     *
     * @param ctx Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(TomlCompletionContext ctx) {
        fillNodeAtCursor(ctx);
        return getCompletionItemsBasedOnParent(ctx);
    }

    /**
     * Get the matching parent table for the context node.
     *
     * @return completion items for the current context.
     */
    public static List<CompletionItem> getCompletionItemsBasedOnParent(TomlCompletionContext ctx) {
        Node node = ctx.getNodeAtCursor();
        Map<String, CompletionItem> completions = new HashMap<>();
        if (node == null) {
            return new ArrayList<>();
        }
        Node reference = node;
        Map<String, Map<String, CompletionItem>> c2cSnippets = C2CSnippetManager.getInstance().getCompletions();
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

    private static Map<String, CompletionItem> getTableArrayCompletions(TableArrayNode arrayNode, Map<String,
            Map<String, CompletionItem>> snippets) {
        Map<String, CompletionItem> completions =
                snippets.get(TomlSyntaxTreeUtil.toDottedString(arrayNode.identifier()));

        for (KeyValueNode field : arrayNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(field.identifier());
            completions.remove(key);
        }
        return completions;
    }

    /**
     * Find the node based on the cursor position.
     */
    public static void fillNodeAtCursor(TomlCompletionContext context) {
        Path tomlFilePath = context.filePath();
        SyntaxTree st = TomlSyntaxTreeUtil.getTomlSyntaxTree(tomlFilePath).orElseThrow();
        Position position = context.getCursorPosition();
        TextDocument textDocument = st.textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((DocumentNode) st.rootNode()).findNode(range);
        while (nonTerminalNode.parent() != null && !TomlSyntaxTreeUtil.withinTextRange(txtPos, nonTerminalNode)) {
            nonTerminalNode = nonTerminalNode.parent();
        }
        context.setNodeAtCursor(nonTerminalNode);
    }

    private static Map<String, CompletionItem> getTableCompletions(TableNode tableNode,
                                                                   Map<String, Map<String, CompletionItem>> snippets) {
        Map<String, CompletionItem> completions =
                snippets.get(TomlSyntaxTreeUtil.toDottedString(tableNode.identifier()));

        for (KeyValueNode field : tableNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(field.identifier());
            completions.remove(key);
        }
        return completions;
    }
}
