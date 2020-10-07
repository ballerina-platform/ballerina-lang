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
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.ParserRuleContext;
import io.ballerina.compiler.internal.parser.tree.STInvalidNodeMinutiae;
import io.ballerina.compiler.internal.parser.tree.STMinutiae;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static io.ballerinalang.compiler.parser.test.ParserTestConstants.CHILDREN_FIELD;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.DIAGNOSTICS_FIELD;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.HAS_DIAGNOSTICS;
import static io.ballerinalang.compiler.parser.test.ParserTestConstants.INVALID_NODE_FIELD;
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
        JsonObject jsonNode = new JsonObject();
        SyntaxKind nodeKind = treeNode.kind;
        jsonNode.addProperty(KIND_FIELD, nodeKind.name());

        if (treeNode.isMissing()) {
            jsonNode.addProperty(IS_MISSING_FIELD, treeNode.isMissing());
            addDiagnostics(treeNode, jsonNode);
            if (ParserTestUtils.isToken(treeNode)) {
                addTrivia((STToken) treeNode, jsonNode);
            }
            return jsonNode;
        }

        addDiagnostics(treeNode, jsonNode);
        if (ParserTestUtils.isToken(treeNode)) {

            // If the node is a terminal node with a dynamic value (i.e: non-syntax node)
            // then add the value to the json.
            if (!ParserTestUtils.isKeyword(nodeKind)) {
                jsonNode.addProperty(VALUE_FIELD, ParserTestUtils.getTokenText((STToken) treeNode));
            }
            addTrivia((STToken) treeNode, jsonNode);
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
        if (token.leadingMinutiae().bucketCount() != 0) {
            addMinutiaeList((STNodeList) token.leadingMinutiae(), jsonNode, LEADING_MINUTIAE);
        }

        if (token.trailingMinutiae().bucketCount() != 0) {
            addMinutiaeList((STNodeList) token.trailingMinutiae(), jsonNode, TRAILING_MINUTIAE);
        }
    }

    private static void addMinutiaeList(STNodeList minutiaeList, JsonObject node, String key) {
        JsonArray minutiaeJsonArray = new JsonArray();
        int size = minutiaeList.size();
        for (int i = 0; i < size; i++) {
            STMinutiae minutiae = (STMinutiae) minutiaeList.get(i);
            JsonObject minutiaeJson = new JsonObject();
            minutiaeJson.addProperty(KIND_FIELD, minutiae.kind.name());
            switch (minutiae.kind) {
                case WHITESPACE_MINUTIAE:
                case END_OF_LINE_MINUTIAE:
                case COMMENT_MINUTIAE:
                    minutiaeJson.addProperty(VALUE_FIELD, minutiae.text());
                    break;
                case INVALID_NODE_MINUTIAE:
                    STInvalidNodeMinutiae invalidNodeMinutiae = (STInvalidNodeMinutiae) minutiae;
                    STNode invalidNode = invalidNodeMinutiae.invalidNode();
                    minutiaeJson.add(INVALID_NODE_FIELD, getJSON(invalidNode));
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported minutiae kind: '" + minutiae.kind + "'");
            }

            minutiaeJsonArray.add(minutiaeJson);
        }
        node.add(key, minutiaeJsonArray);
    }

    private static void addDiagnostics(STNode treeNode, JsonObject jsonNode) {
        if (!treeNode.hasDiagnostics()) {
            return;
        }

        jsonNode.addProperty(HAS_DIAGNOSTICS, treeNode.hasDiagnostics());
        Collection<STNodeDiagnostic> diagnostics = treeNode.diagnostics();
        if (diagnostics.isEmpty()) {
            return;
        }

        JsonArray diagnosticsJsonArray = new JsonArray();
        diagnostics.forEach(syntaxDiagnostic ->
                diagnosticsJsonArray.add(syntaxDiagnostic.diagnosticCode().toString()));
        jsonNode.add(DIAGNOSTICS_FIELD, diagnosticsJsonArray);
    }
}
