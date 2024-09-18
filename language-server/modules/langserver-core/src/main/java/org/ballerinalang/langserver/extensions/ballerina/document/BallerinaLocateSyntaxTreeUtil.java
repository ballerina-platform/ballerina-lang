/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Range;

import java.util.Map;
import java.util.Optional;

/**
 * This is the BallerinaLocateSyntaxTreeUtil class for related utils in mapping the location of syntax nodes
 * for a given range.
 *
 * @since 2.0.0
 */
public final class BallerinaLocateSyntaxTreeUtil {

    private static final String POSITION = "position";
    private static final String LOCATE_PATH_PROPERTY = "isNodePath";
    private static final String LOCATED_NODE_PROPERTY = "isLocatedNode";
    private static final String START_LINE = "startLine";
    private static final String START_COLUMN = "startColumn";
    private static final String END_LINE = "endLine";
    private static final String END_COLUMN = "endColumn";

    private BallerinaLocateSyntaxTreeUtil() {
    }

    private enum NodeRange {
        INCLUSIVE,
        EQUALS,
        EXCLUSIVE
    }

    /**
     * Returns the JSON syntax tree object with the node path mapped on it.
     *
     * @param range  {@link Range} The range for which the path should be found
     * @param syntaxTree {@link SyntaxTree} The syntax tree of the source file
     * @param syntaxTreeJson {@link JsonElement} The JSON syntax tree object
     * @return {@link JsonElement} The JSON syntax tree object with the mapped node path
     */
    public static JsonElement mapNodePath(Range range, SyntaxTree syntaxTree, JsonElement syntaxTreeJson) {
        syntaxTreeJson.getAsJsonObject().addProperty(LOCATE_PATH_PROPERTY, true);
        NonTerminalNode node = CommonUtil.findNode(range, syntaxTree);

        if (node.kind() == SyntaxKind.LIST) {
            node = node.parent();
        }
        if (node == syntaxTree.rootNode()) {
            syntaxTreeJson.getAsJsonObject().addProperty(LOCATED_NODE_PROPERTY, true);
            return syntaxTreeJson;
        }
        return findNodePath(syntaxTreeJson, syntaxTree, node);
    }

    private static JsonElement findNodePath (JsonElement syntaxTreeJson,
                                             SyntaxTree syntaxTree,
                                             NonTerminalNode node) {
        Optional<JsonElement> temp = Optional.of(syntaxTreeJson);

        while (temp.isPresent()) {
            temp = findChildren(syntaxTree, node, temp.get());
        }
        return syntaxTreeJson;
    }

    private static Optional<JsonElement> findChildren(SyntaxTree syntaxTree,
                                                      NonTerminalNode node,
                                                      JsonElement jsonNode) {
        for (Map.Entry<String, JsonElement> childEntry: jsonNode.getAsJsonObject().entrySet()) {
            if (childEntry.getValue().isJsonArray()) {
                for (JsonElement childProps: childEntry.getValue().getAsJsonArray()) {
                    NodeRange nodeRange = evaluateRange(syntaxTree, node, childProps,
                            childProps.getAsJsonObject().get(POSITION).getAsJsonObject());
                    if (nodeRange == NodeRange.INCLUSIVE) {
                        return Optional.of(childProps);
                    }
                    if (nodeRange == NodeRange.EQUALS) {
                        return Optional.empty();
                    }
                }
            } else if (childEntry.getValue().isJsonObject() && childEntry.getValue().getAsJsonObject().has(POSITION)) {
                NodeRange nodeRange = evaluateRange(syntaxTree, node, childEntry.getValue(),
                            childEntry.getValue().getAsJsonObject().get(POSITION).getAsJsonObject());
                if (nodeRange == NodeRange.INCLUSIVE) {
                    return Optional.ofNullable(childEntry.getValue());
                }
                if (nodeRange == NodeRange.EQUALS) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private static NodeRange evaluateRange(SyntaxTree syntaxTree,
                                           NonTerminalNode node,
                                           JsonElement jsonNode,
                                           JsonObject position) {
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(LinePosition.from(position.get(START_LINE).getAsInt(),
                position.get(START_COLUMN).getAsInt()));
        int end = textDocument.textPositionFrom(LinePosition.from(position.get(END_LINE).getAsInt(),
                position.get(END_COLUMN).getAsInt()));
        TextRange textRange = TextRange.from(start, end - start);

        if (node.textRange().startOffset() >= textRange.startOffset()
                && node.textRange().endOffset() <= textRange.endOffset()) {
            jsonNode.getAsJsonObject().addProperty(LOCATE_PATH_PROPERTY, true);
            if (textRange.equals(node.textRange())) {
                jsonNode.getAsJsonObject().addProperty(LOCATED_NODE_PROPERTY, true);
                return NodeRange.EQUALS;
            }
            return NodeRange.INCLUSIVE;
        }
        return NodeRange.EXCLUSIVE;
    }
}
