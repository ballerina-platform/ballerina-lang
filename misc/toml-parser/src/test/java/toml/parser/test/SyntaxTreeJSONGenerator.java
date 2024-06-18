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

package toml.parser.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.toml.internal.parser.ParserFactory;
import io.ballerina.toml.internal.parser.TomlParser;
import io.ballerina.toml.internal.parser.tree.STInvalidNodeMinutiae;
import io.ballerina.toml.internal.parser.tree.STMinutiae;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.toml.internal.parser.tree.STNodeList;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

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

    private static final PrintStream STANDARD_OUT = System.out;
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    private SyntaxTreeJSONGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path syntax = RESOURCE_DIRECTORY.resolve(Paths.get("syntax"));

        File[] resourceContents = syntax.toFile().listFiles();
        if (resourceContents != null) {
            for (File resourceContent :resourceContents) {
                Path path = resourceContent.toPath();
                generateJsonOfDir(path);
            }
        }

        // Using a file source as input
//        String path = "syntax/key-value/values.toml";
//        String jsonString = generateJSON(Paths.get(path));
//        STANDARD_OUT.println(jsonString);
    }

    private static void generateJsonOfDir(Path dirPath) throws IOException {
        File directory = dirPath.toFile();
        if (directory.isDirectory()) {
            File[] directoryListing = directory.listFiles();
            if (directoryListing != null) {
                for (File file : directoryListing) {
                    if (getFileExtension(file).equals("toml")) {
                        String jsonString = generateJSON(file.toPath());
                        writeTreeToJson(file, jsonString);
                    }
                }
            }
        } else {
            STANDARD_OUT.println("No files generated.");
        }
    }

    private static void writeTreeToJson(File file, String content) throws IOException {
        content = content + System.lineSeparator();
        String tomlPath = file.getAbsolutePath();
        String jsonPath = tomlPath.replace(".toml", ".json");
        Path jsonFile = Paths.get(jsonPath);
        Files.writeString(jsonFile, content, StandardCharsets.UTF_8);

    }

    private static String getFileExtension(File file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1) : "";
    }

    public static String generateJSON(Path sourceFilePath) throws IOException {
        byte[] bytes = Files.readAllBytes(sourceFilePath);
        String content = new String(bytes, StandardCharsets.UTF_8);
        return generateJSON(content);
    }

    public static String generateJSON(STNode treeNode) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getJSON(treeNode));
    }

    public static String generateJSON(String source) {
        STNode tree = getParserTree(source);
        return generateJSON(tree);
    }

    private static STNode getParserTree(String source) {
        TomlParser parser = ParserFactory.getParser(source);
        return parser.parse();
    }

    private static JsonElement getJSON(STNode treeNode) {
        JsonObject jsonNode = new JsonObject();
        SyntaxKind nodeKind = treeNode.kind;
        jsonNode.addProperty(ParserTestConstants.KIND_FIELD, nodeKind.name());

        if (treeNode.isMissing()) {
            jsonNode.addProperty(ParserTestConstants.IS_MISSING_FIELD, treeNode.isMissing());
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
                jsonNode.addProperty(ParserTestConstants.VALUE_FIELD, ParserTestUtils.getTokenText((STToken) treeNode));
            }
            addTrivia((STToken) treeNode, jsonNode);
            // else do nothing
        } else {
            addChildren(treeNode, jsonNode);
        }

        return jsonNode;
    }

    private static void addChildren(STNode tree, JsonObject node) {
        addNodeList(tree, node, ParserTestConstants.CHILDREN_FIELD);
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
            addMinutiaeList((STNodeList) token.leadingMinutiae(), jsonNode, ParserTestConstants.LEADING_MINUTIAE);
        }

        if (token.trailingMinutiae().bucketCount() != 0) {
            addMinutiaeList((STNodeList) token.trailingMinutiae(), jsonNode, ParserTestConstants.TRAILING_MINUTIAE);
        }
    }

    private static void addMinutiaeList(STNodeList minutiaeList, JsonObject node, String key) {
        JsonArray minutiaeJsonArray = new JsonArray();
        int size = minutiaeList.size();
        for (int i = 0; i < size; i++) {
            STMinutiae minutiae = (STMinutiae) minutiaeList.get(i);
            JsonObject minutiaeJson = new JsonObject();
            minutiaeJson.addProperty(ParserTestConstants.KIND_FIELD, minutiae.kind.name());
            switch (minutiae.kind) {
                case WHITESPACE_MINUTIAE:
                case END_OF_LINE_MINUTIAE:
                case COMMENT_MINUTIAE:
                    minutiaeJson.addProperty(ParserTestConstants.VALUE_FIELD, minutiae.text());
                    break;
                case INVALID_NODE_MINUTIAE:
                    STInvalidNodeMinutiae invalidNodeMinutiae = (STInvalidNodeMinutiae) minutiae;
                    STNode invalidNode = invalidNodeMinutiae.invalidNode();
                    minutiaeJson.add(ParserTestConstants.INVALID_NODE_FIELD, getJSON(invalidNode));
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

        jsonNode.addProperty(ParserTestConstants.HAS_DIAGNOSTICS, treeNode.hasDiagnostics());
        Collection<STNodeDiagnostic> diagnostics = treeNode.diagnostics();
        if (diagnostics.isEmpty()) {
            return;
        }

        JsonArray diagnosticsJsonArray = new JsonArray();
        diagnostics.forEach(syntaxDiagnostic ->
                diagnosticsJsonArray.add(syntaxDiagnostic.diagnosticCode().toString()));
        jsonNode.add(ParserTestConstants.DIAGNOSTICS_FIELD, diagnosticsJsonArray);
    }
}
