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
package org.ballerinalang.langserver.toml;

import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.ValueNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.Position;

/**
 * Utility class used as a helper for Toml Syntax tree related actions.
 *
 * @since 2.0.0
 */
public class TomlSyntaxTreeUtil {

    public static final String NUMBER = "Number";
    public static final String STRING = "String";
    public static final String BOOLEAN = "Boolean";
    public static final String TABLE_ARRAY = "Table Array";
    public static final String TABLE = "Table";

    public static String toDottedString(SeparatedNodeList<ValueNode> nodeList) {
        StringBuilder output = new StringBuilder();
        for (ValueNode valueNode : nodeList) {
            String valueString = valueNode.toString().trim();
            output.append(".").append(valueString);
        }
        return output.substring(1).trim();
    }

    public static TableNode getTableNode(SyntaxTree syntaxTree, CodeActionContext context) {
        Position position = context.cursorPosition();
        TextDocument textDocument = syntaxTree.textDocument();
        int txtPos =
                textDocument
                        .textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((DocumentNode) syntaxTree.rootNode()).findNode(range);
        while (nonTerminalNode.parent() != null && !withinTextRange(txtPos, nonTerminalNode)) {
            nonTerminalNode = nonTerminalNode.parent();
        }

        while (nonTerminalNode != null) {
            if (nonTerminalNode.kind() == SyntaxKind.TABLE) {
                return (TableNode) nonTerminalNode;
            }
            nonTerminalNode = nonTerminalNode.parent();
        }
        return null;
    }

    public static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }

    public static String trimResourcePath(String resourcePath) {
        resourcePath = resourcePath.trim();
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }
        return resourcePath;
    }
}
