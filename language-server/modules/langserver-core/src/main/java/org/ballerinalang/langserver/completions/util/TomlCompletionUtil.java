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
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.ValueNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.completions.TomlCompletionContext;
import org.ballerinalang.langserver.completions.toml.C2CSnippetManager;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.io.IOException;
import java.nio.file.Files;
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
    public static List<CompletionItem> getCompletionItems(TomlCompletionContext ctx)
            throws TokenOrSymbolNotFoundException {
        fillNodeAtCursor(ctx);
        return route(ctx);
    }

    /**
     * Get the matching parent table for the context node.
     *
     * @return completion items for the current context.
     */
    public static List<CompletionItem> route(TomlCompletionContext ctx) {
        Node node = ctx.getNodeAtCursor();
        Map<String, CompletionItem> completions = new HashMap<>();
        if (node == null) {
            return new ArrayList<>(completions.values());
        }
        Node reference = node;
        C2CSnippetManager snippetManager = new C2CSnippetManager();
        Map<String, Map<String, CompletionItem>> c2cSupportedSnippets = snippetManager.getAllSnippetsFromSchema();
        while (reference != null) {
            if (reference.kind() == SyntaxKind.TABLE) {
                completions = getTableCompletions((TableNode) reference, c2cSupportedSnippets);
                return new ArrayList<>(completions.values());
            }
            if (reference.kind() == SyntaxKind.TABLE_ARRAY) {
                completions = getTableArrayCompletions((TableArrayNode) reference, c2cSupportedSnippets);
                return new ArrayList<>(completions.values());
            }

            reference = reference.parent();
        }
        return new ArrayList<>(completions.values());
    }

    private static Map<String, CompletionItem> getTableArrayCompletions(TableArrayNode arrayNode,
                                                                        Map<String, Map<String, CompletionItem>> snippets) {
        Map<String, CompletionItem> completions = snippets.get(toDottedString(arrayNode.identifier()));

        for (KeyValueNode field : arrayNode.fields()) {
            String key = toDottedString(field.identifier());
            completions.remove(key);
        }
        return completions;
    }

    private static String toDottedString(SeparatedNodeList<ValueNode> nodeList) {
        StringBuilder output = new StringBuilder();
        for (ValueNode valueNode : nodeList) {
            String valueString = valueNode.toString().trim();
            output.append(".").append(valueString);
        }
        return output.substring(1);
    }

    /**
     * Find the token at cursor.
     */
    public static void fillNodeAtCursor(TomlCompletionContext context) throws TokenOrSymbolNotFoundException {
        try {
            //TODO Replace the with the Toml Syntax Tree when supported by context.
            Path tomlFilePath = context.filePath();
            if (tomlFilePath != null) {
                TextDocument textDocument = TextDocuments.from(Files.readString(tomlFilePath));
                Path filePath = tomlFilePath.getFileName();
                if (filePath != null) {
                    String path = filePath.toString();
                    SyntaxTree st = SyntaxTree.from(textDocument, path);

                    Position position = context.getCursorPosition();
                    int txtPos =
                            textDocument
                                    .textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
                    context.setCursorPositionInTree(txtPos);
                    TextRange range = TextRange.from(txtPos, 0);
                    NonTerminalNode nonTerminalNode = ((DocumentNode) st.rootNode()).findNode(range);
                    while (nonTerminalNode.parent() != null && !withinTextRange(txtPos, nonTerminalNode)) {
                        nonTerminalNode = nonTerminalNode.parent();
                    }
                    context.setNodeAtCursor(nonTerminalNode);
                }
            }
        } catch (IOException e) {
            throw new TokenOrSymbolNotFoundException("Couldn't find a valid document!");
        }
    }

    private static Map<String, CompletionItem> getTableCompletions(TableNode tableNode,
                                                                   Map<String, Map<String, CompletionItem>> snippets) {
        Map<String, CompletionItem> completions = snippets.get(toDottedString(tableNode.identifier()));

        for (KeyValueNode field : tableNode.fields()) {
            String key = toDottedString(field.identifier());
            completions.remove(key);
        }
        return completions;
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }
}
