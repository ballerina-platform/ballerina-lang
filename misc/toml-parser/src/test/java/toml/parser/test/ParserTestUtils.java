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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.toml.internal.parser.ParserFactory;
import io.ballerina.toml.internal.parser.TomlParser;
import io.ballerina.toml.internal.parser.tree.STIdentifierToken;
import io.ballerina.toml.internal.parser.tree.STInvalidNodeMinutiae;
import io.ballerina.toml.internal.parser.tree.STMinutiae;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.toml.internal.parser.tree.STNodeList;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.internal.syntax.SyntaxUtils;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.testng.Assert;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static io.ballerina.toml.internal.syntax.SyntaxUtils.isSTNodePresent;

/**
 * Convenient methods for testing the parser.
 *
 * @since 1.2.0
 */
public final class ParserTestUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    /**
     * <b>WARNING</b>: Enabling this flag will update all the assertion files in unit tests.
     * Should be used only if there is a bulk update that needs to be made to the test assertions.
     */
    private static final boolean UPDATE_ASSERTS = false;

    private ParserTestUtils() {
    }

    /**
     * Test parsing a valid source.
     *
     * @param sourceFilePath Path to the ballerina file
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(Path sourceFilePath, Path assertFilePath) {
        updateAssertFile(sourceFilePath, assertFilePath);

        String content = getSourceText(sourceFilePath);
        test(content, assertFilePath);
    }

    /**
     * Test parsing a valid source.
     *
     * @param source         Input source that represent a ballerina code
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(String source, Path assertFilePath) {
        updateAssertFile(source, assertFilePath);

        // Parse the source
        TomlParser parser = ParserFactory.getParser(source);
        STNode syntaxTree = parser.parse();

        // Read the assertion file
        JsonObject assertJson = readAssertFile(RESOURCE_DIRECTORY.resolve(assertFilePath));

        // Validate the tree against the assertion file
        assertNode(syntaxTree, assertJson);
    }

    /**
     * Compares the actualTree with the given json.
     *
     * @param actualTreeRoot the syntax tree to be compared
     * @param assertFilePath json file path which contains the tree structure
     */
    public static void testTree(Node actualTreeRoot, Path assertFilePath) {
        updateAssertFile(actualTreeRoot, assertFilePath);

        // Read the assertion file
        JsonObject assertJson = readAssertFile(RESOURCE_DIRECTORY.resolve(assertFilePath));

        // Validate the tree against the assertion file
        assertNode(actualTreeRoot.internalNode(), assertJson);
    }

    /**
     * Returns Ballerina source code in the given file as a {@code String}.
     *
     * @param sourceFilePath Path to the ballerina file
     * @return source code as a {@code String}
     */
    public static String getSourceText(Path sourceFilePath) {
        try {
            return new String(Files.readAllBytes(RESOURCE_DIRECTORY.resolve(sourceFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@code SyntaxTree} after parsing the give source code path.
     *
     * @param sourceFilePath Path to the ballerina file
     */
    public static SyntaxTree parseFile(Path sourceFilePath) {
        String text = getSourceText(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(text);
        return SyntaxTree.from(textDocument, sourceFilePath.getFileName().toString());
    }

    private static JsonObject readAssertFile(Path filePath) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertNode(STNode node, JsonObject json) {
        assertNodeKind(json, node);
        assertMissingToken(json, node);
        assertDiagnostics(json, node);
        if (isToken(node)) {
            assertTerminalNode(json, (STToken) node);
        } else {
            assertNonTerminalNode(json, node);
        }
    }

    private static void assertMissingToken(JsonObject json, STNode node) {
        JsonElement isMissingField = json.get(ParserTestConstants.IS_MISSING_FIELD);
        boolean expectedMissing = isMissingField != null && isMissingField.getAsBoolean();
        boolean actualMissing = node.isMissing();
        if (expectedMissing) {
            Assert.assertTrue(actualMissing, "'" + node.toString().trim() +
                    "' expected to be a STMissingToken, but found '" +
                    node.kind + "'.");
        } else {
            Assert.assertFalse(actualMissing, "Expected:" + json + ", but found " + node);
        }
    }

    private static void assertDiagnostics(JsonObject json, STNode node) {
        JsonElement hasDiagnosticsField = json.get(ParserTestConstants.HAS_DIAGNOSTICS);
        boolean expectedHasDiagnostics = hasDiagnosticsField != null && hasDiagnosticsField.getAsBoolean();
        boolean actualHasDiagnostics = node.hasDiagnostics();
        if (expectedHasDiagnostics) {
            Assert.assertTrue(actualHasDiagnostics, "expected to have diagnostics in node '" + node + "'");
        } else {
            Assert.assertFalse(actualHasDiagnostics, "unexpected diagnostics in node '" + node + "'");
            // Return if there are no diagnostics in the node and no expected diagnostics
            return;
        }

        Collection<STNodeDiagnostic> actualDiagnostics = node.diagnostics();
        int actualSize = actualDiagnostics.size();
        JsonArray diagnosticJsonArray = json.getAsJsonArray(ParserTestConstants.DIAGNOSTICS_FIELD);
        if (diagnosticJsonArray == null) {
            Assert.assertEquals(actualSize, 0, "diagnostic count mismatch in '" + node + "'");
            return;
        }

        int expectedSize = diagnosticJsonArray.size();
        Assert.assertEquals(actualSize, expectedSize, "diagnostic count mismatch in '" + node + "'");

        int index = 0;
        for (STNodeDiagnostic actualDiagnostic : actualDiagnostics) {
            String actualDiagnosticId = actualDiagnostic.diagnosticCode().toString();
            String expectedDiagnosticId = diagnosticJsonArray.get(index++).getAsString();
            Assert.assertEquals(actualDiagnosticId, expectedDiagnosticId,
                    "mismatch in diagnostics in node '" + node + "'");
        }
    }

    private static void assertNodeKind(JsonObject json, STNode node) {
        SyntaxKind expectedNodeKind = getNodeKind(json.get(ParserTestConstants.KIND_FIELD).getAsString());
        SyntaxKind actualNodeKind = node.kind;
        Assert.assertEquals(actualNodeKind, expectedNodeKind, "error at node [" + node.toString() + "].");
    }

    private static void assertTerminalNode(JsonObject json, STToken node) {
        // We've asserted the missing property earlier
        if (node.isMissing()) {
            validateMinutiae(json, node);
            return;
        }

        // Validate the token text, if this is not a syntax token.
        // e.g: identifiers, basic-literals, etc.
        if (!isKeyword(node.kind)) {
            String expectedText = json.get(ParserTestConstants.VALUE_FIELD).getAsString();
            String actualText = getTokenText(node);
            Assert.assertEquals(actualText, expectedText);
        }

        validateMinutiae(json, node);
    }

    private static void validateMinutiae(JsonObject json, STToken token) {
        assertMinutiaeNodes(json.get(ParserTestConstants.LEADING_MINUTIAE), token, true);
        assertMinutiaeNodes(json.get(ParserTestConstants.TRAILING_MINUTIAE), token, false);
    }

    private static void assertMinutiaeNodes(JsonElement jsonElement, STToken token, boolean leading) {
        String minutiaeDirection = leading ? "leading" : "trailing";
        STNodeList minutiaeList = (STNodeList) (leading ? token.leadingMinutiae() : token.trailingMinutiae());
        if (jsonElement == null) {
            Assert.assertTrue(minutiaeList.isEmpty(), "unexpected " + minutiaeDirection +
                    " minutiae present in token '" + token + "'");
            return;
        }

        JsonArray minutiaeJsonArray = jsonElement.getAsJsonArray();
        int expectedSize = minutiaeJsonArray.size();
        int actualSize = minutiaeList.size();
        Assert.assertEquals(actualSize, expectedSize, minutiaeDirection +
                " minutiae count mismatch in token '" + token + "'");

        for (int index = 0; index < minutiaeJsonArray.size(); index++) {
            assertMinutiaeNode(minutiaeJsonArray.get(index).getAsJsonObject(),
                    (STMinutiae) minutiaeList.get(index), token, minutiaeDirection);
        }
    }

    private static void assertMinutiaeNode(JsonObject minutiaeJson,
                                           STMinutiae minutiaeNode,
                                           STToken token,
                                           String minutiaeDirection) {
        assertNodeKind(minutiaeJson, minutiaeNode);
        switch (minutiaeNode.kind) {
            case END_OF_LINE_MINUTIAE:
                Assert.assertEquals(cleanupText(minutiaeNode.text()), minutiaeJson.get(
                        ParserTestConstants.VALUE_FIELD).getAsString(),
                        "mismatch in " + minutiaeDirection + " minutiae value(" +
                                minutiaeNode.kind + ") in token '" + token + "'");
                break;
            case COMMENT_MINUTIAE:
            case WHITESPACE_MINUTIAE:
                Assert.assertEquals(minutiaeNode.text(),
                        minutiaeJson.get(ParserTestConstants.VALUE_FIELD).getAsString(),
                        "mismatch in " + minutiaeDirection + " minutiae value(" +
                                minutiaeNode.kind + ") in token '" + token + "'");
                break;
            case INVALID_NODE_MINUTIAE:
                STInvalidNodeMinutiae invalidNodeMinutiae = (STInvalidNodeMinutiae) minutiaeNode;
                STNode invalidNode = invalidNodeMinutiae.invalidNode();
                assertNode(invalidNode, minutiaeJson.get(ParserTestConstants.INVALID_NODE_FIELD).getAsJsonObject());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported minutiae kind: '" + minutiaeNode.kind + "'");
        }
    }

    private static void assertNonTerminalNode(JsonObject json, STNode tree) {
        JsonArray children = json.getAsJsonArray(ParserTestConstants.CHILDREN_FIELD);
        int size = children.size();
        int j = 0;

        Assert.assertEquals(getNonEmptyChildCount(tree), size, "mismatching child count for '" + tree.toString() + "'");

        for (int i = 0; i < size; i++) {
            // Skip the optional fields that are not present and get the next
            // available node.
            STNode nextChild = tree.childInBucket(j++);
            while (!isSTNodePresent(nextChild)) {
                nextChild = tree.childInBucket(j++);
            }

            // Assert the actual child node against the expected child node.
            assertNode(nextChild, (JsonObject) children.get(i));
        }
    }

    private static int getNonEmptyChildCount(STNode tree) {
        int count = 0;
        for (int i = 0; i < tree.bucketCount(); i++) {
            STNode nextChild = tree.childInBucket(i);
            if (isSTNodePresent(nextChild)) {
                count++;
            }
        }

        return count;
    }

    public static boolean isToken(STNode node) {
        return SyntaxUtils.isToken(node);
    }

    public static boolean isKeyword(SyntaxKind syntaxKind) {
        return SyntaxKind.IDENTIFIER_LITERAL.compareTo(syntaxKind) > 0 || syntaxKind == SyntaxKind.EOF_TOKEN;
    }

    public static boolean isTrivia(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case WHITESPACE_MINUTIAE:
            case END_OF_LINE_MINUTIAE:
            case COMMENT_MINUTIAE:
            case INVALID_NODE_MINUTIAE:
                return true;
            default:
                return false;
        }
    }

    public static String getTokenText(STToken token) {
        switch (token.kind) {
            case IDENTIFIER_LITERAL:
                String text = ((STIdentifierToken) token).text;
                return cleanupText(text);
            case STRING_LITERAL:
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return token.text();
            case INVALID_TOKEN:
                return token.text();
            default:
                return token.kind.toString();
        }
    }

    private static String cleanupText(String text) {
        return text.replace(System.lineSeparator(), "\n");
    }

    private static void updateAssertFile(Path sourceFilePath, Path assertFilePath) {
        if (!UPDATE_ASSERTS) {
            return;
        }
        try {
            String jsonString = SyntaxTreeJSONGenerator.generateJSON(sourceFilePath);
            updateAssertFile(jsonString, assertFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateAssertFile(Node externalNode, Path assertFilePath) {
        if (!UPDATE_ASSERTS) {
            return;
        }
        String jsonString = SyntaxTreeJSONGenerator.generateJSON(externalNode.internalNode());
        updateAssertFile(jsonString, assertFilePath);
    }

    private static void updateAssertFile(String jsonString, Path assertFilePath) {
        if (!UPDATE_ASSERTS) {
            return;
        }

        Path filePath = RESOURCE_DIRECTORY.resolve(assertFilePath);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SyntaxKind getNodeKind(String kind) {
        switch (kind) {
            case "INLINE_TABLE":
                return SyntaxKind.INLINE_TABLE;
            case "NEW_LINE":
                return SyntaxKind.NEWLINE;
            case "TRUE_KEYWORD":
                return SyntaxKind.TRUE_KEYWORD;
            case "FALSE_KEYWORD":
                return SyntaxKind.FALSE_KEYWORD;
            case "OPEN_BRACE_TOKEN":
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case "CLOSE_BRACE_TOKEN":
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case "OPEN_BRACKET_TOKEN":
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case "CLOSE_BRACKET_TOKEN":
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case "DOT_TOKEN":
                return SyntaxKind.DOT_TOKEN;
            case "COMMA_TOKEN":
                return SyntaxKind.COMMA_TOKEN;
            case "HASH_TOKEN":
                return SyntaxKind.HASH_TOKEN;
            case "DOUBLE_QUOTE_TOKEN":
                return SyntaxKind.DOUBLE_QUOTE_TOKEN;
            case "SINGLE_QUOTE_TOKEN":
                return SyntaxKind.SINGLE_QUOTE_TOKEN;
            case "TRIPLE_DOUBLE_QUOTE_TOKEN":
                return SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN;
            case "TRIPLE_SINGLE_QUOTE_TOKEN":
                return SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN;
            case "EQUAL_TOKEN":
                return SyntaxKind.EQUAL_TOKEN;
            case "PLUS_TOKEN":
                return SyntaxKind.PLUS_TOKEN;
            case "MINUS_TOKEN":
                return SyntaxKind.MINUS_TOKEN;
            case "STRING_LITERAL_TOKEN":
                return SyntaxKind.STRING_LITERAL_TOKEN;
            case "IDENTIFIER_LITERAL":
                return SyntaxKind.IDENTIFIER_LITERAL;
            case "STRING_LITERAL":
                return SyntaxKind.STRING_LITERAL;
            case "LITERAL_STRING":
                return SyntaxKind.LITERAL_STRING;
            case "WHITESPACE_MINUTIAE":
                return SyntaxKind.WHITESPACE_MINUTIAE;
            case "END_OF_LINE_MINUTIAE":
                return SyntaxKind.END_OF_LINE_MINUTIAE;
            case "COMMENT_MINUTIAE":
                return SyntaxKind.COMMENT_MINUTIAE;
            case "INVALID_NODE_MINUTIAE":
                return SyntaxKind.INVALID_NODE_MINUTIAE;
            case "INVALID_TOKEN":
                return SyntaxKind.INVALID_TOKEN;
            case "INVALID_TOKEN_MINUTIAE_NODE":
                return SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE;
            case "MARKDOWN_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_DOCUMENTATION_LINE;
            case "TABLE":
                return SyntaxKind.TABLE;
            case "KEY_VALUE":
                return SyntaxKind.KEY_VALUE;
            case "TABLE_ARRAY":
                return SyntaxKind.TABLE_ARRAY;
            case "KEY":
                return SyntaxKind.KEY;
            case "DEC_INT":
                return SyntaxKind.DEC_INT;
            case "HEX_INT":
                return SyntaxKind.HEX_INT;
            case "OCT_INT":
                return SyntaxKind.OCT_INT;
            case "BINARY_INT":
                return SyntaxKind.BINARY_INT;
            case "FLOAT":
                return SyntaxKind.FLOAT;
            case "INF_TOKEN":
                return SyntaxKind.INF_TOKEN;
            case "NAN_TOKEN":
                return SyntaxKind.NAN_TOKEN;
            case "ML_STRING_LITERAL":
                return SyntaxKind.ML_STRING_LITERAL;
            case "DECIMAL_INT_TOKEN":
                return SyntaxKind.DECIMAL_INT_TOKEN;
            case "DECIMAL_FLOAT_TOKEN":
                return SyntaxKind.DECIMAL_FLOAT_TOKEN;
            case "HEX_INTEGER_LITERAL_TOKEN":
                return SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
            case "OCTAL_INTEGER_LITERAL_TOKEN":
                return SyntaxKind.OCTAL_INTEGER_LITERAL_TOKEN;
            case "BINARY_INTEGER_LITERAL_TOKEN":
                return SyntaxKind.BINARY_INTEGER_LITERAL_TOKEN;
            case "BOOLEAN":
                return SyntaxKind.BOOLEAN;
            case "OFFSET_DATE_TIME":
                return SyntaxKind.OFFSET_DATE_TIME;
            case "LOCAL_DATE_TIME":
                return SyntaxKind.LOCAL_DATE_TIME;
            case "LOCAL_DATE":
                return SyntaxKind.LOCAL_DATE;
            case "LOCAL_TIME":
                return SyntaxKind.LOCAL_TIME;
            case "ARRAY":
                return SyntaxKind.ARRAY;
            case "INVALID":
                return SyntaxKind.INVALID;
            case "MODULE_PART":
                return SyntaxKind.MODULE_PART;
            case "EOF_TOKEN":
                return SyntaxKind.EOF_TOKEN;
            case "LIST":
                return SyntaxKind.LIST;
            case "NONE":
                return SyntaxKind.NONE;
        }
        return null;
    }

    public static void assertLineRange(LineRange lineRange, int startLine, int startOffset, int endLine,
                                       int endOffset) {
        Assert.assertEquals(lineRange.startLine().line(), startLine);
        Assert.assertEquals(lineRange.startLine().offset(), startOffset);

        Assert.assertEquals(lineRange.endLine().line(), endLine);
        Assert.assertEquals(lineRange.endLine().offset(), endOffset);
    }
}
