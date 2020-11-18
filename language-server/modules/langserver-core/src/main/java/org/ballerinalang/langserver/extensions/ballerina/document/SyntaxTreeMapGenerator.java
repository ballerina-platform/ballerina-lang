/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a Map<String, Object> for a given SyntaxTree.
 */
public class SyntaxTreeMapGenerator extends NodeTransformer<JsonElement> {
    private Map<String, JsonObject> typeInfo = new HashMap<>();

    public SyntaxTreeMapGenerator(Map<String, JsonObject> typeInfo) {
        this.typeInfo = typeInfo;
    }

    @Override
    protected JsonElement transformSyntaxNode(Node node) {
        JsonObject nodeJson = new JsonObject();
        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (ChildNodeEntry childNodeEntry : nonTerminalNode.childEntries()) {
            if (childNodeEntry.isList()) {
                JsonArray childList = new JsonArray();
                for (Node listChildNode : childNodeEntry.nodeList()) {
                    childList.add(apply(listChildNode));
                }
                nodeJson.add(childNodeEntry.name(), childList);
            } else if (childNodeEntry.node().isPresent()) {
                nodeJson.add(childNodeEntry.name(), apply(childNodeEntry.node().get()));
            }
        }
        nodeJson.addProperty("source", node.toSourceCode());
        nodeJson.addProperty("kind", prettifyKind(node.kind().toString()));

        if (node.lineRange() != null) {
            LineRange lineRange = node.lineRange();
            LinePosition startLine = lineRange.startLine();
            LinePosition endLine = lineRange.endLine();
            JsonObject position = new JsonObject();
            position.addProperty("startLine", startLine.line());
            position.addProperty("startColumn", startLine.offset());
            position.addProperty("endLine", endLine.line());
            position.addProperty("endColumn", endLine.offset());
            nodeJson.add("position", position);
            if (typeInfo.size() > 0 && typeInfo.get(startLine.line() + ":" + startLine.offset()) != null) {
                nodeJson.add("typeData", typeInfo.get(startLine.line() + ":" + startLine.offset()));
                typeInfo.remove(startLine.line() + ":" + startLine.offset());
            }
        }

        return nodeJson;
    }

    private JsonElement apply(Node node) {
        JsonObject nodeInfo = new JsonObject();
        nodeInfo.addProperty("kind", prettifyKind(node.kind().toString()));
        if (node instanceof Token) {
            nodeInfo.addProperty("isToken", true);
            nodeInfo.addProperty("value", ((Token) node).text());
        } else {
            JsonElement memberValues = node.apply(this);
            memberValues.getAsJsonObject().entrySet().forEach(memberEntry -> {
                nodeInfo.add(memberEntry.getKey(), memberEntry.getValue());
            });
        }
        return nodeInfo;
    }

    private String prettifyKind(String kind) {
        return Arrays.stream(kind.split("_"))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
}
