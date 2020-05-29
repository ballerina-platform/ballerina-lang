/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerinalang.compiler.internal.parser.BallerinaParser;
import io.ballerinalang.compiler.internal.parser.ParserFactory;
import io.ballerinalang.compiler.internal.parser.ParserRuleContext;
import io.ballerinalang.compiler.internal.parser.tree.STBasicLiteralNode;
import io.ballerinalang.compiler.internal.parser.tree.STBuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STSimpleNameReferenceNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STXMLTextNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerinalang.compiler.parser.test.ParserTestConstants.CHILDREN_FIELD;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.IS_MISSING_FIELD;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.KIND_FIELD;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.LEADING_MINUTIAE;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.TRAILING_MINUTIAE;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.VALUE_FIELD;

/**
 * Generates a JSON that represents the structure of the syntax tree. This JSON
 * can be used to validate the parsed tree during unit-tests.
 * 
 * @since 1.2.0
 */
public class SyntaxTreeJSONGenerator {

    /*
     * Change the below two constants as required, depending on the type of test.
     */
    private static final boolean INCLUDE_TRIVIA = false;
    private static final ParserRuleContext PARSER_CONTEXT = ParserRuleContext.COMP_UNIT;

    private static final PrintStream STANDARD_OUT = System.out;
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    public static void main(String[] args) throws IOException {
        // Using a string source as input
        // generateJSON("a 7", ParserRuleContext.EXPRESSION);

        // Using a file source as input
        String path = "test1.bal";
        String jsonString = generateJSON(Paths.get(path), PARSER_CONTEXT);
        STANDARD_OUT.println(jsonString);
    }

    public static String generateJSON(Path sourceFilePath, ParserRuleContext context) throws IOException {
        byte[] bytes = Files.readAllBytes(RESOURCE_DIRECTORY.resolve(sourceFilePath));
        String content = new String(bytes, StandardCharsets.UTF_8);
        return generateJSON(content, context);
    }

    public static String generateJSON(STNode treeNode) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getJSON(treeNode));
    }

    public static String generateJSON(String source, ParserRuleContext context) {
        STNode tree = getParserTree(source, context);
        return generateJSON(tree);
    }

    private static STNode getParserTree(String source, ParserRuleContext context) {
        BallerinaParser parser = ParserFactory.getParser(source);
        return parser.parse(context);
    }

    private static JsonElement getJSON(STNode treeNode) {
        // Remove the wrappers
        if (treeNode instanceof STBasicLiteralNode) {
            treeNode = ((STBasicLiteralNode) treeNode).literalToken;
        } else if (treeNode instanceof STSimpleNameReferenceNode) {
            treeNode = ((STSimpleNameReferenceNode) treeNode).name;
        } else if (treeNode instanceof STBuiltinSimpleNameReferenceNode) {
            treeNode = ((STBuiltinSimpleNameReferenceNode) treeNode).name;
        } else if (treeNode instanceof STXMLTextNode) {
            treeNode = ((STXMLTextNode) treeNode).content;
        }

        JsonObject jsonNode = new JsonObject();
        SyntaxKind nodeKind = treeNode.kind;
        jsonNode.addProperty(KIND_FIELD, nodeKind.name());

        boolean isMissing = treeNode instanceof STMissingToken;
        if (isMissing) {
            jsonNode.addProperty(IS_MISSING_FIELD, isMissing);
            return jsonNode;
        }

        if (ParserTestUtils.isTerminalNode(nodeKind)) {

            // If the node is a terminal node with a dynamic value (i.e: non-syntax node)
            // then add the value to the json.
            if (!ParserTestUtils.isSyntaxToken(nodeKind)) {
                jsonNode.addProperty(VALUE_FIELD, ParserTestUtils.getTokenText(treeNode));
            }

            if (INCLUDE_TRIVIA && !ParserTestUtils.isTrivia(nodeKind)) {
                addTrivia((STToken) treeNode, jsonNode);
            }
            // else do nothing
        } else {
            addChildren(treeNode, jsonNode);
        }

        return jsonNode;
    }

    private static void addChildren(STNode tree, JsonObject node) {
        addNodeList(tree, node, CHILDREN_FIELD);
    }

    private static void addNodeList(STNode tree, JsonObject node, String key) {
        JsonArray children = new JsonArray();
        int size = tree.bucketCount();
        for (int i = 0; i < size; i++) {
            STNode childNode = tree.childInBucket(i);
            if (childNode == null || childNode.kind == SyntaxKind.NONE) {
                continue;
            }

            children.add(getJSON(childNode));
        }
        node.add(key, children);
    }

    private static void addTrivia(STToken token, JsonObject jsonNode) {
        addNodeList(token.leadingMinutiae(), jsonNode, LEADING_MINUTIAE);
        addNodeList(token.trailingMinutiae(), jsonNode, TRAILING_MINUTIAE);
    }
}
