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
package org.ballerinalang.diagramutil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

/**
 * This is the DiagramUtil class for Diagram related Utils which include the JSON conversion of the Syntax Tree.
 */
public class DiagramUtil {

    /**
     * Get the Modified JSON ST with type info.
     *
     * @param srcFile       The source file associated with the syntax tree
     * @param semanticModel Semantic model for the syntax tree.
     * @return {@link JsonObject}   ST as a Json Object
     */
    public static JsonElement getSyntaxTreeJSON(Document srcFile, SemanticModel semanticModel) {
        JsonElement syntaxTreeJson;
        try {
            // Map each type data by looking at the line ranges and prepare the SyntaxTree JSON.
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator(semanticModel);
            SyntaxTree syntaxTree = srcFile.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            syntaxTreeJson = mapGenerator.transform(modulePartNode);
        } catch (NullPointerException e) {
            syntaxTreeJson = new JsonObject();
        }

        return syntaxTreeJson;
    }

    public static JsonElement getSyntaxTreeJSONByRange(Node node, SemanticModel semanticModel) {
        JsonElement syntaxTreeJson;
        try {
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator(semanticModel);

            if (node.kind() == SyntaxKind.LIST) {
                syntaxTreeJson = mapGenerator.transformSyntaxNode(node.parent());
            } else if (node instanceof Token) {
                JsonObject nodeJson = new JsonObject();
                JsonObject nodeInfo = new JsonObject();

                String nodeKind = mapGenerator.prettifyKind(node.kind().toString());
                nodeInfo.addProperty("kind", nodeKind);
                nodeInfo.addProperty("isToken", true);
                nodeInfo.addProperty("value", ((Token) node).text());
                if (node.lineRange() != null) {
                    LineRange lineRange = node.lineRange();
                    LinePosition startLine = lineRange.startLine();
                    LinePosition endLine = lineRange.endLine();
                    JsonObject position = new JsonObject();
                    position.addProperty("startLine", startLine.line());
                    position.addProperty("startColumn", startLine.offset());
                    position.addProperty("endLine", endLine.line());
                    position.addProperty("endColumn", endLine.offset());
                    nodeInfo.add("position", position);
                }
                nodeInfo.add("leadingMinutiae", mapGenerator.evaluateMinutiae(node.leadingMinutiae()));
                nodeInfo.add("trailingMinutiae", mapGenerator.evaluateMinutiae(node.trailingMinutiae()));

                nodeJson.add("members", nodeInfo);
                nodeJson.addProperty("kind", nodeKind);
                syntaxTreeJson = nodeJson;
            } else {
                syntaxTreeJson = mapGenerator.transformSyntaxNode(node);
            }
        } catch (NullPointerException e) {
            syntaxTreeJson = new JsonObject();
        }

        return syntaxTreeJson;
    }

    public static JsonElement getClassDefinitionSyntaxJson(ClassDefinitionNode classDefinitionNode,
                                                           SemanticModel semanticModel) {
        JsonElement syntaxTreeJson;
        try {
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator(semanticModel);
            syntaxTreeJson = mapGenerator.transform(classDefinitionNode);
        } catch (NullPointerException e) {
            syntaxTreeJson = new JsonObject();
        }

        return syntaxTreeJson;
    }

    public static JsonElement getTypeDefinitionSyntaxJson(TypeDefinitionNode typeDefinitionNode,
                                                          SemanticModel semanticModel) {
        JsonElement syntaxTreeJson;
        try {
            SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator(semanticModel);
            syntaxTreeJson = mapGenerator.transform(typeDefinitionNode);
        } catch (NullPointerException e) {
            syntaxTreeJson = new JsonObject();
        }

        return syntaxTreeJson;
    }
}
