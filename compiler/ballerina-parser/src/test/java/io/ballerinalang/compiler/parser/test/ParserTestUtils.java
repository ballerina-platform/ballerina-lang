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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.ParserRuleContext;
import io.ballerina.compiler.internal.parser.tree.STIdentifierToken;
import io.ballerina.compiler.internal.parser.tree.STInvalidNodeMinutiae;
import io.ballerina.compiler.internal.parser.tree.STMinutiae;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
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

import static io.ballerina.compiler.internal.syntax.SyntaxUtils.isSTNodePresent;
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
 * Convenient methods for testing the parser.
 *
 * @since 1.2.0
 */
public class ParserTestUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    /**
     * <b>WARNING</b>: Enabling this flag will update all the assertion files in unit tests.
     * Should be used only if there is a bulk update that needs to be made to the test assertions.
     */
    private static final boolean UPDATE_ASSERTS = false;

    /**
     * Test parsing a valid source.
     *
     * @param sourceFilePath Path to the ballerina file
     * @param context Context to start parsing the given source
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(Path sourceFilePath, ParserRuleContext context, Path assertFilePath) {
        updateAssertFile(sourceFilePath, assertFilePath, context);

        String content = getSourceText(sourceFilePath);
        test(content, context, assertFilePath);
    }

    /**
     * Test parsing a valid source.
     *
     * @param source Input source that represent a ballerina code
     * @param context Context to start parsing the given source
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(String source, ParserRuleContext context, Path assertFilePath) {
        updateAssertFile(source, assertFilePath, context);

        // Parse the source
        BallerinaParser parser = ParserFactory.getParser(source);
        STNode syntaxTree = parser.parse(context);

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
        JsonElement isMissingField = json.get(IS_MISSING_FIELD);
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
        JsonElement hasDiagnosticsField = json.get(HAS_DIAGNOSTICS);
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
        JsonArray diagnosticJsonArray = json.getAsJsonArray(DIAGNOSTICS_FIELD);
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
        SyntaxKind expectedNodeKind = getNodeKind(json.get(KIND_FIELD).getAsString());
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
            String expectedText = json.get(VALUE_FIELD).getAsString();
            String actualText = getTokenText(node);
            Assert.assertEquals(actualText, expectedText);
        }

        validateMinutiae(json, node);
    }

    private static void validateMinutiae(JsonObject json, STToken token) {
        assertMinutiaeNodes(json.get(LEADING_MINUTIAE), token, true);
        assertMinutiaeNodes(json.get(TRAILING_MINUTIAE), token, false);
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
                Assert.assertEquals(cleanupText(minutiaeNode.text()), minutiaeJson.get(VALUE_FIELD).getAsString(),
                        "mismatch in " + minutiaeDirection + " minutiae value(" +
                                minutiaeNode.kind + ") in token '" + token + "'");
                break;
            case COMMENT_MINUTIAE:
            case WHITESPACE_MINUTIAE:
                Assert.assertEquals(minutiaeNode.text(), minutiaeJson.get(VALUE_FIELD).getAsString(),
                        "mismatch in " + minutiaeDirection + " minutiae value(" +
                                minutiaeNode.kind + ") in token '" + token + "'");
                break;
            case INVALID_NODE_MINUTIAE:
                STInvalidNodeMinutiae invalidNodeMinutiae = (STInvalidNodeMinutiae) minutiaeNode;
                STNode invalidNode = invalidNodeMinutiae.invalidNode();
                assertNode(invalidNode, minutiaeJson.get(INVALID_NODE_FIELD).getAsJsonObject());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported minutiae kind: '" + minutiaeNode.kind + "'");
        }
    }

    private static void assertNonTerminalNode(JsonObject json, STNode tree) {
        JsonArray children = json.getAsJsonArray(CHILDREN_FIELD);
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
        return SyntaxKind.IDENTIFIER_TOKEN.compareTo(syntaxKind) > 0 || syntaxKind == SyntaxKind.EOF_TOKEN;
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
            case IDENTIFIER_TOKEN:
                return ((STIdentifierToken) token).text;
            case STRING_LITERAL_TOKEN:
                String val = token.text();
                int stringLen = val.length();
                int lastCharPosition = val.endsWith("\"") ? stringLen - 1 : stringLen;
                return val.substring(1, lastCharPosition);
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case PARAMETER_NAME:
            case BACKTICK_CONTENT:
            case DEPRECATION_LITERAL:
            case INVALID_TOKEN:
                return token.text();
            case XML_TEXT:
            case XML_TEXT_CONTENT:
            case TEMPLATE_STRING:
            case DOCUMENTATION_DESCRIPTION:
            case DOCUMENTATION_STRING:
                return cleanupText(token.text());
            default:
                return token.kind.toString();
        }
    }

    private static String cleanupText(String text) {
        return text.replace(System.lineSeparator(), "\n");
    }

    private static void updateAssertFile(Path sourceFilePath, Path assertFilePath, ParserRuleContext context) {
        if (!UPDATE_ASSERTS) {
            return;
        }
        try {
            String jsonString = SyntaxTreeJSONGenerator.generateJSON(sourceFilePath, context);
            updateAssertFile(jsonString, assertFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateAssertFile(String source, Path assertFilePath, ParserRuleContext context) {
        if (!UPDATE_ASSERTS) {
            return;
        }

        String jsonString = SyntaxTreeJSONGenerator.generateJSON(source, context);
        updateAssertFile(jsonString, assertFilePath);
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
            case "MODULE_PART":
                return SyntaxKind.MODULE_PART;
            case "TYPE_DEFINITION":
                return SyntaxKind.TYPE_DEFINITION;
            case "FUNCTION_DEFINITION":
                return SyntaxKind.FUNCTION_DEFINITION;
            case "IMPORT_DECLARATION":
                return SyntaxKind.IMPORT_DECLARATION;
            case "SERVICE_DECLARATION":
                return SyntaxKind.SERVICE_DECLARATION;
            case "LISTENER_DECLARATION":
                return SyntaxKind.LISTENER_DECLARATION;
            case "CONST_DECLARATION":
                return SyntaxKind.CONST_DECLARATION;
            case "MODULE_VAR_DECL":
                return SyntaxKind.MODULE_VAR_DECL;
            case "XML_NAMESPACE_DECLARATION":
                return SyntaxKind.XML_NAMESPACE_DECLARATION;
            case "MODULE_XML_NAMESPACE_DECLARATION":
                return SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION;
            case "ANNOTATION_DECLARATION":
                return SyntaxKind.ANNOTATION_DECLARATION;
            case "ENUM_DECLARATION":
                return SyntaxKind.ENUM_DECLARATION;
            case "CLASS_DEFINITION":
                return SyntaxKind.CLASS_DEFINITION;

            // Keywords
            case "PUBLIC_KEYWORD":
                return SyntaxKind.PUBLIC_KEYWORD;
            case "PRIVATE_KEYWORD":
                return SyntaxKind.PRIVATE_KEYWORD;
            case "FUNCTION_KEYWORD":
                return SyntaxKind.FUNCTION_KEYWORD;
            case "TYPE_KEYWORD":
                return SyntaxKind.TYPE_KEYWORD;
            case "EXTERNAL_KEYWORD":
                return SyntaxKind.EXTERNAL_KEYWORD;
            case "RETURNS_KEYWORD":
                return SyntaxKind.RETURNS_KEYWORD;
            case "RECORD_KEYWORD":
                return SyntaxKind.RECORD_KEYWORD;
            case "OBJECT_KEYWORD":
                return SyntaxKind.OBJECT_KEYWORD;
            case "REMOTE_KEYWORD":
                return SyntaxKind.REMOTE_KEYWORD;
            case "CLIENT_KEYWORD":
                return SyntaxKind.CLIENT_KEYWORD;
            case "ABSTRACT_KEYWORD":
                return SyntaxKind.ABSTRACT_KEYWORD;
            case "IF_KEYWORD":
                return SyntaxKind.IF_KEYWORD;
            case "ELSE_KEYWORD":
                return SyntaxKind.ELSE_KEYWORD;
            case "WHILE_KEYWORD":
                return SyntaxKind.WHILE_KEYWORD;
            case "TRUE_KEYWORD":
                return SyntaxKind.TRUE_KEYWORD;
            case "FALSE_KEYWORD":
                return SyntaxKind.FALSE_KEYWORD;
            case "CHECK_KEYWORD":
                return SyntaxKind.CHECK_KEYWORD;
            case "CHECKPANIC_KEYWORD":
                return SyntaxKind.CHECKPANIC_KEYWORD;
            case "FAIL_KEYWORD":
                return SyntaxKind.FAIL_KEYWORD;
            case "PANIC_KEYWORD":
                return SyntaxKind.PANIC_KEYWORD;
            case "IMPORT_KEYWORD":
                return SyntaxKind.IMPORT_KEYWORD;
            case "VERSION_KEYWORD":
                return SyntaxKind.VERSION_KEYWORD;
            case "AS_KEYWORD":
                return SyntaxKind.AS_KEYWORD;
            case "CONTINUE_KEYWORD":
                return SyntaxKind.CONTINUE_KEYWORD;
            case "BREAK_KEYWORD":
                return SyntaxKind.BREAK_KEYWORD;
            case "RETURN_KEYWORD":
                return SyntaxKind.RETURN_KEYWORD;
            case "SERVICE_KEYWORD":
                return SyntaxKind.SERVICE_KEYWORD;
            case "ON_KEYWORD":
                return SyntaxKind.ON_KEYWORD;
            case "RESOURCE_KEYWORD":
                return SyntaxKind.RESOURCE_KEYWORD;
            case "LISTENER_KEYWORD":
                return SyntaxKind.LISTENER_KEYWORD;
            case "CONST_KEYWORD":
                return SyntaxKind.CONST_KEYWORD;
            case "FINAL_KEYWORD":
                return SyntaxKind.FINAL_KEYWORD;
            case "TYPEOF_KEYWORD":
                return SyntaxKind.TYPEOF_KEYWORD;
            case "ANNOTATION_KEYWORD":
                return SyntaxKind.ANNOTATION_KEYWORD;
            case "IS_KEYWORD":
                return SyntaxKind.IS_KEYWORD;
            case "MAP_KEYWORD":
                return SyntaxKind.MAP_KEYWORD;
            case "FUTURE_KEYWORD":
                return SyntaxKind.FUTURE_KEYWORD;
            case "TYPEDESC_KEYWORD":
                return SyntaxKind.TYPEDESC_KEYWORD;
            case "NULL_KEYWORD":
                return SyntaxKind.NULL_KEYWORD;
            case "LOCK_KEYWORD":
                return SyntaxKind.LOCK_KEYWORD;
            case "VAR_KEYWORD":
                return SyntaxKind.VAR_KEYWORD;
            case "SOURCE_KEYWORD":
                return SyntaxKind.SOURCE_KEYWORD;
            case "WORKER_KEYWORD":
                return SyntaxKind.WORKER_KEYWORD;
            case "PARAMETER_KEYWORD":
                return SyntaxKind.PARAMETER_KEYWORD;
            case "FIELD_KEYWORD":
                return SyntaxKind.FIELD_KEYWORD;
            case "XMLNS_KEYWORD":
                return SyntaxKind.XMLNS_KEYWORD;
            case "INT_KEYWORD":
                return SyntaxKind.INT_KEYWORD;
            case "FLOAT_KEYWORD":
                return SyntaxKind.FLOAT_KEYWORD;
            case "DECIMAL_KEYWORD":
                return SyntaxKind.DECIMAL_KEYWORD;
            case "BOOLEAN_KEYWORD":
                return SyntaxKind.BOOLEAN_KEYWORD;
            case "STRING_KEYWORD":
                return SyntaxKind.STRING_KEYWORD;
            case "BYTE_KEYWORD":
                return SyntaxKind.BYTE_KEYWORD;
            case "XML_KEYWORD":
                return SyntaxKind.XML_KEYWORD;
            case "JSON_KEYWORD":
                return SyntaxKind.JSON_KEYWORD;
            case "HANDLE_KEYWORD":
                return SyntaxKind.HANDLE_KEYWORD;
            case "ANY_KEYWORD":
                return SyntaxKind.ANY_KEYWORD;
            case "ANYDATA_KEYWORD":
                return SyntaxKind.ANYDATA_KEYWORD;
            case "NEVER_KEYWORD":
                return SyntaxKind.NEVER_KEYWORD;
            case "FORK_KEYWORD":
                return SyntaxKind.FORK_KEYWORD;
            case "TRAP_KEYWORD":
                return SyntaxKind.TRAP_KEYWORD;
            case "FOREACH_KEYWORD":
                return SyntaxKind.FOREACH_KEYWORD;
            case "IN_KEYWORD":
                return SyntaxKind.IN_KEYWORD;
            case "TABLE_KEYWORD":
                return SyntaxKind.TABLE_KEYWORD;
            case "KEY_KEYWORD":
                return SyntaxKind.KEY_KEYWORD;
            case "ERROR_KEYWORD":
                return SyntaxKind.ERROR_KEYWORD;
            case "LET_KEYWORD":
                return SyntaxKind.LET_KEYWORD;
            case "STREAM_KEYWORD":
                return SyntaxKind.STREAM_KEYWORD;
            case "READONLY_KEYWORD":
                return SyntaxKind.READONLY_KEYWORD;
            case "DISTINCT_KEYWORD":
                return SyntaxKind.DISTINCT_KEYWORD;
            case "FROM_KEYWORD":
                return SyntaxKind.FROM_KEYWORD;
            case "WHERE_KEYWORD":
                return SyntaxKind.WHERE_KEYWORD;
            case "SELECT_KEYWORD":
                return SyntaxKind.SELECT_KEYWORD;
            case "ORDER_KEYWORD":
                return SyntaxKind.ORDER_KEYWORD;
            case "BY_KEYWORD":
                return SyntaxKind.BY_KEYWORD;
            case "ASCENDING_KEYWORD":
                return SyntaxKind.ASCENDING_KEYWORD;
            case "DESCENDING_KEYWORD":
                return SyntaxKind.DESCENDING_KEYWORD;
            case "NEW_KEYWORD":
                return SyntaxKind.NEW_KEYWORD;
            case "START_KEYWORD":
                return SyntaxKind.START_KEYWORD;
            case "FLUSH_KEYWORD":
                return SyntaxKind.FLUSH_KEYWORD;
            case "DEFAULT_KEYWORD":
                return SyntaxKind.DEFAULT_KEYWORD;
            case "WAIT_KEYWORD":
                return SyntaxKind.WAIT_KEYWORD;
            case "DO_KEYWORD":
                return SyntaxKind.DO_KEYWORD;
            case "TRANSACTION_KEYWORD":
                return SyntaxKind.TRANSACTION_KEYWORD;
            case "COMMIT_KEYWORD":
                return SyntaxKind.COMMIT_KEYWORD;
            case "RETRY_KEYWORD":
                return SyntaxKind.RETRY_KEYWORD;
            case "ROLLBACK_KEYWORD":
                return SyntaxKind.ROLLBACK_KEYWORD;
            case "TRANSACTIONAL_KEYWORD":
                return SyntaxKind.TRANSACTIONAL_KEYWORD;
            case "ISOLATED_KEYWORD":
                return SyntaxKind.ISOLATED_KEYWORD;
            case "ENUM_KEYWORD":
                return SyntaxKind.ENUM_KEYWORD;
            case "BASE16_KEYWORD":
                return SyntaxKind.BASE16_KEYWORD;
            case "BASE64_KEYWORD":
                return SyntaxKind.BASE64_KEYWORD;
            case "MATCH_KEYWORD":
                return SyntaxKind.MATCH_KEYWORD;
            case "CLASS_KEYWORD":
                return SyntaxKind.CLASS_KEYWORD;
            case "CONFLICT_KEYWORD":
                return SyntaxKind.CONFLICT_KEYWORD;
            case "LIMIT_KEYWORD":
                return SyntaxKind.LIMIT_KEYWORD;
            case "JOIN_KEYWORD":
                return SyntaxKind.JOIN_KEYWORD;
            case "EQUALS_KEYWORD":
                return SyntaxKind.EQUALS_KEYWORD;
            case "OUTER_KEYWORD":
                return SyntaxKind.OUTER_KEYWORD;

            // Documentation reference
            case "TYPE_DOC_REFERENCE_TOKEN":
                return SyntaxKind.TYPE_DOC_REFERENCE_TOKEN;
            case "SERVICE_DOC_REFERENCE_TOKEN":
                return SyntaxKind.SERVICE_DOC_REFERENCE_TOKEN;
            case "VARIABLE_DOC_REFERENCE_TOKEN":
                return SyntaxKind.VARIABLE_DOC_REFERENCE_TOKEN;
            case "VAR_DOC_REFERENCE_TOKEN":
                return SyntaxKind.VAR_DOC_REFERENCE_TOKEN;
            case "ANNOTATION_DOC_REFERENCE_TOKEN":
                return SyntaxKind.ANNOTATION_DOC_REFERENCE_TOKEN;
            case "MODULE_DOC_REFERENCE_TOKEN":
                return SyntaxKind.MODULE_DOC_REFERENCE_TOKEN;
            case "FUNCTION_DOC_REFERENCE_TOKEN":
                return SyntaxKind.FUNCTION_DOC_REFERENCE_TOKEN;
            case "PARAMETER_DOC_REFERENCE_TOKEN":
                return SyntaxKind.PARAMETER_DOC_REFERENCE_TOKEN;
            case "CONST_DOC_REFERENCE_TOKEN":
                return SyntaxKind.CONST_DOC_REFERENCE_TOKEN;

            // Operators
            case "PLUS_TOKEN":
                return SyntaxKind.PLUS_TOKEN;
            case "MINUS_TOKEN":
                return SyntaxKind.MINUS_TOKEN;
            case "ASTERISK_TOKEN":
                return SyntaxKind.ASTERISK_TOKEN;
            case "SLASH_TOKEN":
                return SyntaxKind.SLASH_TOKEN;
            case "LT_TOKEN":
                return SyntaxKind.LT_TOKEN;
            case "EQUAL_TOKEN":
                return SyntaxKind.EQUAL_TOKEN;
            case "DOUBLE_EQUAL_TOKEN":
                return SyntaxKind.DOUBLE_EQUAL_TOKEN;
            case "TRIPPLE_EQUAL_TOKEN":
                return SyntaxKind.TRIPPLE_EQUAL_TOKEN;
            case "PERCENT_TOKEN":
                return SyntaxKind.PERCENT_TOKEN;
            case "GT_TOKEN":
                return SyntaxKind.GT_TOKEN;
            case "RIGHT_DOUBLE_ARROW_TOKEN":
                return SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
            case "QUESTION_MARK_TOKEN":
                return SyntaxKind.QUESTION_MARK_TOKEN;
            case "LT_EQUAL_TOKEN":
                return SyntaxKind.LT_EQUAL_TOKEN;
            case "GT_EQUAL_TOKEN":
                return SyntaxKind.GT_EQUAL_TOKEN;
            case "EXCLAMATION_MARK_TOKEN":
                return SyntaxKind.EXCLAMATION_MARK_TOKEN;
            case "NOT_EQUAL_TOKEN":
                return SyntaxKind.NOT_EQUAL_TOKEN;
            case "NOT_DOUBLE_EQUAL_TOKEN":
                return SyntaxKind.NOT_DOUBLE_EQUAL_TOKEN;
            case "BITWISE_AND_TOKEN":
                return SyntaxKind.BITWISE_AND_TOKEN;
            case "BITWISE_XOR_TOKEN":
                return SyntaxKind.BITWISE_XOR_TOKEN;
            case "LOGICAL_AND_TOKEN":
                return SyntaxKind.LOGICAL_AND_TOKEN;
            case "LOGICAL_OR_TOKEN":
                return SyntaxKind.LOGICAL_OR_TOKEN;
            case "NEGATION_TOKEN":
                return SyntaxKind.NEGATION_TOKEN;
            case "DOUBLE_LT_TOKEN":
                return SyntaxKind.DOUBLE_LT_TOKEN;
            case "DOUBLE_GT_TOKEN":
                return SyntaxKind.DOUBLE_GT_TOKEN;
            case "TRIPPLE_GT_TOKEN":
                return SyntaxKind.TRIPPLE_GT_TOKEN;
            case "DOUBLE_DOT_LT_TOKEN":
                return SyntaxKind.DOUBLE_DOT_LT_TOKEN;
            case "ANNOT_CHAINING_TOKEN":
                return SyntaxKind.ANNOT_CHAINING_TOKEN;
            case "OPTIONAL_CHAINING_TOKEN":
                return SyntaxKind.OPTIONAL_CHAINING_TOKEN;
            case "ELVIS_TOKEN":
                return SyntaxKind.ELVIS_TOKEN;
            case "DOT_LT_TOKEN":
                return SyntaxKind.DOT_LT_TOKEN;
            case "SLASH_LT_TOKEN":
                return SyntaxKind.SLASH_LT_TOKEN;
            case "DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN":
                return SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
            case "SLASH_ASTERISK_TOKEN":
                return SyntaxKind.SLASH_ASTERISK_TOKEN;

            // Separators
            case "OPEN_BRACE_TOKEN":
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case "CLOSE_BRACE_TOKEN":
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case "OPEN_PAREN_TOKEN":
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case "CLOSE_PAREN_TOKEN":
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case "OPEN_BRACKET_TOKEN":
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case "CLOSE_BRACKET_TOKEN":
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case "SEMICOLON_TOKEN":
                return SyntaxKind.SEMICOLON_TOKEN;
            case "DOT_TOKEN":
                return SyntaxKind.DOT_TOKEN;
            case "COLON_TOKEN":
                return SyntaxKind.COLON_TOKEN;
            case "COMMA_TOKEN":
                return SyntaxKind.COMMA_TOKEN;
            case "ELLIPSIS_TOKEN":
                return SyntaxKind.ELLIPSIS_TOKEN;
            case "OPEN_BRACE_PIPE_TOKEN":
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case "CLOSE_BRACE_PIPE_TOKEN":
                return SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
            case "PIPE_TOKEN":
                return SyntaxKind.PIPE_TOKEN;
            case "AT_TOKEN":
                return SyntaxKind.AT_TOKEN;
            case "RIGHT_ARROW_TOKEN":
                return SyntaxKind.RIGHT_ARROW_TOKEN;
            case "BACKTICK_TOKEN":
                return SyntaxKind.BACKTICK_TOKEN;
            case "DOUBLE_QUOTE_TOKEN":
                return SyntaxKind.DOUBLE_QUOTE_TOKEN;
            case "SINGLE_QUOTE_TOKEN":
                return SyntaxKind.SINGLE_QUOTE_TOKEN;
            case "SYNC_SEND_TOKEN":
                return SyntaxKind.SYNC_SEND_TOKEN;
            case "LEFT_ARROW_TOKEN":
                return SyntaxKind.LEFT_ARROW_TOKEN;
            case "HASH_TOKEN":
                return SyntaxKind.HASH_TOKEN;

            // Expressions
            case "IDENTIFIER_TOKEN":
                return SyntaxKind.IDENTIFIER_TOKEN;
            case "BRACED_EXPRESSION":
                return SyntaxKind.BRACED_EXPRESSION;
            case "BINARY_EXPRESSION":
                return SyntaxKind.BINARY_EXPRESSION;
            case "STRING_LITERAL":
                return SyntaxKind.STRING_LITERAL;
            case "STRING_LITERAL_TOKEN":
                return SyntaxKind.STRING_LITERAL_TOKEN;
            case "NUMERIC_LITERAL":
                return SyntaxKind.NUMERIC_LITERAL;
            case "BOOLEAN_LITERAL":
                return SyntaxKind.BOOLEAN_LITERAL;
            case "DECIMAL_INTEGER_LITERAL_TOKEN":
                return SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN;
            case "HEX_INTEGER_LITERAL_TOKEN":
                return SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
            case "DECIMAL_FLOATING_POINT_LITERAL_TOKEN":
                return SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN;
            case "HEX_FLOATING_POINT_LITERAL_TOKEN":
                return SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN;
            case "ASTERISK_LITERAL":
                return SyntaxKind.ASTERISK_LITERAL;
            case "FUNCTION_CALL":
                return SyntaxKind.FUNCTION_CALL;
            case "POSITIONAL_ARG":
                return SyntaxKind.POSITIONAL_ARG;
            case "NAMED_ARG":
                return SyntaxKind.NAMED_ARG;
            case "REST_ARG":
                return SyntaxKind.REST_ARG;
            case "QUALIFIED_NAME_REFERENCE":
                return SyntaxKind.QUALIFIED_NAME_REFERENCE;
            case "FIELD_ACCESS":
                return SyntaxKind.FIELD_ACCESS;
            case "METHOD_CALL":
                return SyntaxKind.METHOD_CALL;
            case "INDEXED_EXPRESSION":
                return SyntaxKind.INDEXED_EXPRESSION;
            case "CHECK_EXPRESSION":
                return SyntaxKind.CHECK_EXPRESSION;
            case "MAPPING_CONSTRUCTOR":
                return SyntaxKind.MAPPING_CONSTRUCTOR;
            case "TYPEOF_EXPRESSION":
                return SyntaxKind.TYPEOF_EXPRESSION;
            case "UNARY_EXPRESSION":
                return SyntaxKind.UNARY_EXPRESSION;
            case "TYPE_TEST_EXPRESSION":
                return SyntaxKind.TYPE_TEST_EXPRESSION;
            case "NIL_LITERAL":
                return SyntaxKind.NIL_LITERAL;
            case "NULL_LITERAL":
                return SyntaxKind.NULL_LITERAL;
            case "SIMPLE_NAME_REFERENCE":
                return SyntaxKind.SIMPLE_NAME_REFERENCE;
            case "TRAP_EXPRESSION":
                return SyntaxKind.TRAP_EXPRESSION;
            case "LIST_CONSTRUCTOR":
                return SyntaxKind.LIST_CONSTRUCTOR;
            case "TYPE_CAST_EXPRESSION":
                return SyntaxKind.TYPE_CAST_EXPRESSION;
            case "TABLE_CONSTRUCTOR":
                return SyntaxKind.TABLE_CONSTRUCTOR;
            case "LET_EXPRESSION":
                return SyntaxKind.LET_EXPRESSION;
            case "RAW_TEMPLATE_EXPRESSION":
                return SyntaxKind.RAW_TEMPLATE_EXPRESSION;
            case "XML_TEMPLATE_EXPRESSION":
                return SyntaxKind.XML_TEMPLATE_EXPRESSION;
            case "STRING_TEMPLATE_EXPRESSION":
                return SyntaxKind.STRING_TEMPLATE_EXPRESSION;
            case "QUERY_EXPRESSION":
                return SyntaxKind.QUERY_EXPRESSION;
            case "EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION":
                return SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION;
            case "IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION":
                return SyntaxKind.IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION;
            case "IMPLICIT_NEW_EXPRESSION":
                return SyntaxKind.IMPLICIT_NEW_EXPRESSION;
            case "EXPLICIT_NEW_EXPRESSION":
                return SyntaxKind.EXPLICIT_NEW_EXPRESSION;
            case "ANNOT_ACCESS":
                return SyntaxKind.ANNOT_ACCESS;
            case "OPTIONAL_FIELD_ACCESS":
                return SyntaxKind.OPTIONAL_FIELD_ACCESS;
            case "CONDITIONAL_EXPRESSION":
                return SyntaxKind.CONDITIONAL_EXPRESSION;
            case "TRANSACTIONAL_EXPRESSION":
                return SyntaxKind.TRANSACTIONAL_EXPRESSION;
            case "SERVICE_CONSTRUCTOR_EXPRESSION":
                return SyntaxKind.SERVICE_CONSTRUCTOR_EXPRESSION;
            case "BYTE_ARRAY_LITERAL":
                return SyntaxKind.BYTE_ARRAY_LITERAL;
            case "XML_FILTER_EXPRESSION":
                return SyntaxKind.XML_FILTER_EXPRESSION;
            case "XML_STEP_EXPRESSION":
                return SyntaxKind.XML_STEP_EXPRESSION;
            case "XML_NAME_PATTERN_CHAIN":
                return SyntaxKind.XML_NAME_PATTERN_CHAIN;
            case "XML_ATOMIC_NAME_PATTERN":
                return SyntaxKind.XML_ATOMIC_NAME_PATTERN;

            // Actions
            case "REMOTE_METHOD_CALL_ACTION":
                return SyntaxKind.REMOTE_METHOD_CALL_ACTION;
            case "BRACED_ACTION":
                return SyntaxKind.BRACED_ACTION;
            case "CHECK_ACTION":
                return SyntaxKind.CHECK_ACTION;
            case "START_ACTION":
                return SyntaxKind.START_ACTION;
            case "TRAP_ACTION":
                return SyntaxKind.TRAP_ACTION;
            case "FLUSH_ACTION":
                return SyntaxKind.FLUSH_ACTION;
            case "ASYNC_SEND_ACTION":
                return SyntaxKind.ASYNC_SEND_ACTION;
            case "SYNC_SEND_ACTION":
                return SyntaxKind.SYNC_SEND_ACTION;
            case "RECEIVE_ACTION":
                return SyntaxKind.RECEIVE_ACTION;
            case "WAIT_ACTION":
                return SyntaxKind.WAIT_ACTION;
            case "QUERY_ACTION":
                return SyntaxKind.QUERY_ACTION;
            case "COMMIT_ACTION":
                return SyntaxKind.COMMIT_ACTION;

            // Statements
            case "BLOCK_STATEMENT":
                return SyntaxKind.BLOCK_STATEMENT;
            case "LOCAL_VAR_DECL":
                return SyntaxKind.LOCAL_VAR_DECL;
            case "ASSIGNMENT_STATEMENT":
                return SyntaxKind.ASSIGNMENT_STATEMENT;
            case "IF_ELSE_STATEMENT":
                return SyntaxKind.IF_ELSE_STATEMENT;
            case "ELSE_BLOCK":
                return SyntaxKind.ELSE_BLOCK;
            case "WHILE_STATEMENT":
                return SyntaxKind.WHILE_STATEMENT;
            case "DO_STATEMENT":
                return SyntaxKind.DO_STATEMENT;
            case "CALL_STATEMENT":
                return SyntaxKind.CALL_STATEMENT;
            case "PANIC_STATEMENT":
                return SyntaxKind.PANIC_STATEMENT;
            case "CONTINUE_STATEMENT":
                return SyntaxKind.CONTINUE_STATEMENT;
            case "BREAK_STATEMENT":
                return SyntaxKind.BREAK_STATEMENT;
            case "FAIL_STATEMENT":
                return SyntaxKind.FAIL_STATEMENT;
            case "RETURN_STATEMENT":
                return SyntaxKind.RETURN_STATEMENT;
            case "COMPOUND_ASSIGNMENT_STATEMENT":
                return SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT;
            case "LOCAL_TYPE_DEFINITION_STATEMENT":
                return SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT;
            case "ACTION_STATEMENT":
                return SyntaxKind.ACTION_STATEMENT;
            case "LOCK_STATEMENT":
                return SyntaxKind.LOCK_STATEMENT;
            case "FORK_STATEMENT":
                return SyntaxKind.FORK_STATEMENT;
            case "FOREACH_STATEMENT":
                return SyntaxKind.FOREACH_STATEMENT;
            case "TRANSACTION_STATEMENT":
                return SyntaxKind.TRANSACTION_STATEMENT;
            case "RETRY_STATEMENT":
                return SyntaxKind.RETRY_STATEMENT;
            case "ROLLBACK_STATEMENT":
                return SyntaxKind.ROLLBACK_STATEMENT;
            case "MATCH_STATEMENT":
                return SyntaxKind.MATCH_STATEMENT;
            case "INVALID_EXPRESSION_STATEMENT":
                return SyntaxKind.INVALID_EXPRESSION_STATEMENT;

            // Types
            case "TYPE_DESC":
                return SyntaxKind.TYPE_DESC;
            case "INT_TYPE_DESC":
                return SyntaxKind.INT_TYPE_DESC;
            case "FLOAT_TYPE_DESC":
                return SyntaxKind.FLOAT_TYPE_DESC;
            case "DECIMAL_TYPE_DESC":
                return SyntaxKind.DECIMAL_TYPE_DESC;
            case "BOOLEAN_TYPE_DESC":
                return SyntaxKind.BOOLEAN_TYPE_DESC;
            case "STRING_TYPE_DESC":
                return SyntaxKind.STRING_TYPE_DESC;
            case "BYTE_TYPE_DESC":
                return SyntaxKind.BYTE_TYPE_DESC;
            case "XML_TYPE_DESC":
                return SyntaxKind.XML_TYPE_DESC;
            case "JSON_TYPE_DESC":
                return SyntaxKind.JSON_TYPE_DESC;
            case "HANDLE_TYPE_DESC":
                return SyntaxKind.HANDLE_TYPE_DESC;
            case "ANY_TYPE_DESC":
                return SyntaxKind.ANY_TYPE_DESC;
            case "ANYDATA_TYPE_DESC":
                return SyntaxKind.ANYDATA_TYPE_DESC;
            case "NEVER_TYPE_DESC":
                return SyntaxKind.NEVER_TYPE_DESC;
            case "NIL_TYPE_DESC":
                return SyntaxKind.NIL_TYPE_DESC;
            case "OPTIONAL_TYPE_DESC":
                return SyntaxKind.OPTIONAL_TYPE_DESC;
            case "ARRAY_TYPE_DESC":
                return SyntaxKind.ARRAY_TYPE_DESC;
            case "RECORD_TYPE_DESC":
                return SyntaxKind.RECORD_TYPE_DESC;
            case "OBJECT_TYPE_DESC":
                return SyntaxKind.OBJECT_TYPE_DESC;
            case "OBJECT_CONSTRUCTOR":
                return SyntaxKind.OBJECT_CONSTRUCTOR;
            case "UNION_TYPE_DESC":
                return SyntaxKind.UNION_TYPE_DESC;
            case "ERROR_TYPE_DESC":
                return SyntaxKind.ERROR_TYPE_DESC;
            case "EXPLICIT_TYPE_PARAMS":
                return SyntaxKind.EXPLICIT_TYPE_PARAMS;
            case "STREAM_TYPE_DESC":
                return SyntaxKind.STREAM_TYPE_DESC;
            case "FUNCTION_TYPE_DESC":
                return SyntaxKind.FUNCTION_TYPE_DESC;
            case "TABLE_TYPE_DESC":
                return SyntaxKind.TABLE_TYPE_DESC;
            case "TUPLE_TYPE_DESC":
                return SyntaxKind.TUPLE_TYPE_DESC;
            case "PARENTHESISED_TYPE_DESC":
                return SyntaxKind.PARENTHESISED_TYPE_DESC;
            case "READONLY_TYPE_DESC":
                return SyntaxKind.READONLY_TYPE_DESC;
            case "DISTINCT_TYPE_DESC":
                return SyntaxKind.DISTINCT_TYPE_DESC;
            case "INTERSECTION_TYPE_DESC":
                return SyntaxKind.INTERSECTION_TYPE_DESC;
            case "SINGLETON_TYPE_DESC":
                return SyntaxKind.SINGLETON_TYPE_DESC;
            case "TYPEDESC_TYPE_DESC":
                return SyntaxKind.TYPEDESC_TYPE_DESC;
            case "VAR_TYPE_DESC":
                return SyntaxKind.VAR_TYPE_DESC;
            case "SERVICE_TYPE_DESC":
                return SyntaxKind.SERVICE_TYPE_DESC;

            // Others
            case "FUNCTION_BODY_BLOCK":
                return SyntaxKind.FUNCTION_BODY_BLOCK;
            case "LIST":
                return SyntaxKind.LIST;
            case "RETURN_TYPE_DESCRIPTOR":
                return SyntaxKind.RETURN_TYPE_DESCRIPTOR;
            case "EXTERNAL_FUNCTION_BODY":
                return SyntaxKind.EXTERNAL_FUNCTION_BODY;
            case "REQUIRED_PARAM":
                return SyntaxKind.REQUIRED_PARAM;
            case "DEFAULTABLE_PARAM":
                return SyntaxKind.DEFAULTABLE_PARAM;
            case "REST_PARAM":
                return SyntaxKind.REST_PARAM;
            case "RECORD_FIELD":
                return SyntaxKind.RECORD_FIELD;
            case "RECORD_FIELD_WITH_DEFAULT_VALUE":
                return SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE;
            case "TYPE_REFERENCE":
                return SyntaxKind.TYPE_REFERENCE;
            case "RECORD_REST_TYPE":
                return SyntaxKind.RECORD_REST_TYPE;
            case "OBJECT_FIELD":
                return SyntaxKind.OBJECT_FIELD;
            case "IMPORT_ORG_NAME":
                return SyntaxKind.IMPORT_ORG_NAME;
            case "MODULE_NAME":
                return SyntaxKind.MODULE_NAME;
            case "SUB_MODULE_NAME":
                return SyntaxKind.SUB_MODULE_NAME;
            case "IMPORT_VERSION":
                return SyntaxKind.IMPORT_VERSION;
            case "IMPORT_PREFIX":
                return SyntaxKind.IMPORT_PREFIX;
            case "SPECIFIC_FIELD":
                return SyntaxKind.SPECIFIC_FIELD;
            case "COMPUTED_NAME_FIELD":
                return SyntaxKind.COMPUTED_NAME_FIELD;
            case "SPREAD_FIELD":
                return SyntaxKind.SPREAD_FIELD;
            case "SERVICE_BODY":
                return SyntaxKind.SERVICE_BODY;
            case "ARRAY_DIMENSION":
                return SyntaxKind.ARRAY_DIMENSION;
            case "METADATA":
                return SyntaxKind.METADATA;
            case "ANNOTATION":
                return SyntaxKind.ANNOTATION;
            case "PARAMETERIZED_TYPE_DESC":
                return SyntaxKind.PARAMETERIZED_TYPE_DESC;
            case "ANNOTATION_ATTACH_POINT":
                return SyntaxKind.ANNOTATION_ATTACH_POINT;
            case "NAMED_WORKER_DECLARATION":
                return SyntaxKind.NAMED_WORKER_DECLARATION;
            case "NAMED_WORKER_DECLARATOR":
                return SyntaxKind.NAMED_WORKER_DECLARATOR;
            case "TYPE_CAST_PARAM":
                return SyntaxKind.TYPE_CAST_PARAM;
            case "KEY_SPECIFIER":
                return SyntaxKind.KEY_SPECIFIER;
            case "ERROR_TYPE_PARAMS":
                return SyntaxKind.ERROR_TYPE_PARAMS;
            case "LET_VAR_DECL":
                return SyntaxKind.LET_VAR_DECL;
            case "ORDER_KEY":
                return SyntaxKind.ORDER_KEY;
            case "STREAM_TYPE_PARAMS":
                return SyntaxKind.STREAM_TYPE_PARAMS;
            case "FUNCTION_SIGNATURE":
                return SyntaxKind.FUNCTION_SIGNATURE;
            case "QUERY_CONSTRUCT_TYPE":
                return SyntaxKind.QUERY_CONSTRUCT_TYPE;
            case "FROM_CLAUSE":
                return SyntaxKind.FROM_CLAUSE;
            case "WHERE_CLAUSE":
                return SyntaxKind.WHERE_CLAUSE;
            case "LET_CLAUSE":
                return SyntaxKind.LET_CLAUSE;
            case "ON_FAIL_CLAUSE":
                return SyntaxKind.ON_FAIL_CLAUSE;
            case "QUERY_PIPELINE":
                return SyntaxKind.QUERY_PIPELINE;
            case "SELECT_CLAUSE":
                return SyntaxKind.SELECT_CLAUSE;
            case "ORDER_BY_CLAUSE":
                return SyntaxKind.ORDER_BY_CLAUSE;
            case "PARENTHESIZED_ARG_LIST":
                return SyntaxKind.PARENTHESIZED_ARG_LIST;
            case "EXPRESSION_FUNCTION_BODY":
                return SyntaxKind.EXPRESSION_FUNCTION_BODY;
            case "INFER_PARAM_LIST":
                return SyntaxKind.INFER_PARAM_LIST;
            case "METHOD_DECLARATION":
                return SyntaxKind.METHOD_DECLARATION;
            case "TYPED_BINDING_PATTERN":
                return SyntaxKind.TYPED_BINDING_PATTERN;
            case "BINDING_PATTERN":
                return SyntaxKind.BINDING_PATTERN;
            case "CAPTURE_BINDING_PATTERN":
                return SyntaxKind.CAPTURE_BINDING_PATTERN;
            case "LIST_BINDING_PATTERN":
                return SyntaxKind.LIST_BINDING_PATTERN;
            case "REST_BINDING_PATTERN":
                return SyntaxKind.REST_BINDING_PATTERN;
            case "FIELD_BINDING_PATTERN":
                return SyntaxKind.FIELD_BINDING_PATTERN;
            case "MAPPING_BINDING_PATTERN":
                return SyntaxKind.MAPPING_BINDING_PATTERN;
            case "ERROR_BINDING_PATTERN":
                return SyntaxKind.ERROR_BINDING_PATTERN;
            case "NAMED_ARG_BINDING_PATTERN":
                return SyntaxKind.NAMED_ARG_BINDING_PATTERN;
            case "TYPE_PARAMETER":
                return SyntaxKind.TYPE_PARAMETER;
            case "KEY_TYPE_CONSTRAINT":
                return SyntaxKind.KEY_TYPE_CONSTRAINT;
            case "RECEIVE_FIELDS":
                return SyntaxKind.RECEIVE_FIELDS;
            case "REST_TYPE":
                return SyntaxKind.REST_TYPE;
            case "WAIT_FIELDS_LIST":
                return SyntaxKind.WAIT_FIELDS_LIST;
            case "WAIT_FIELD":
                return SyntaxKind.WAIT_FIELD;
            case "ENUM_MEMBER":
                return SyntaxKind.ENUM_MEMBER;
            case "WILDCARD_BINDING_PATTERN":
                return SyntaxKind.WILDCARD_BINDING_PATTERN;
            case "MATCH_CLAUSE":
                return SyntaxKind.MATCH_CLAUSE;
            case "MATCH_GUARD":
                return SyntaxKind.MATCH_GUARD;
            case "OBJECT_METHOD_DEFINITION":
                return SyntaxKind.OBJECT_METHOD_DEFINITION;
            case "LIST_MATCH_PATTERN":
                return SyntaxKind.LIST_MATCH_PATTERN;
            case "REST_MATCH_PATTERN":
                return SyntaxKind.REST_MATCH_PATTERN;
            case "MAPPING_MATCH_PATTERN":
                return SyntaxKind.MAPPING_MATCH_PATTERN;
            case "FIELD_MATCH_PATTERN":
                return SyntaxKind.FIELD_MATCH_PATTERN;
            case "ERROR_MATCH_PATTERN":
                return SyntaxKind.ERROR_MATCH_PATTERN;
            case "NAMED_ARG_MATCH_PATTERN":
                return SyntaxKind.NAMED_ARG_MATCH_PATTERN;
            case "ON_CONFLICT_CLAUSE":
                return SyntaxKind.ON_CONFLICT_CLAUSE;
            case "LIMIT_CLAUSE":
                return SyntaxKind.LIMIT_CLAUSE;
            case "JOIN_CLAUSE":
                return SyntaxKind.JOIN_CLAUSE;
            case "ON_CLAUSE":
                return SyntaxKind.ON_CLAUSE;

            // XML template
            case "XML_ELEMENT":
                return SyntaxKind.XML_ELEMENT;
            case "XML_EMPTY_ELEMENT":
                return SyntaxKind.XML_EMPTY_ELEMENT;
            case "XML_ELEMENT_START_TAG":
                return SyntaxKind.XML_ELEMENT_START_TAG;
            case "XML_ELEMENT_END_TAG":
                return SyntaxKind.XML_ELEMENT_END_TAG;
            case "XML_TEXT":
                return SyntaxKind.XML_TEXT;
            case "XML_PI":
                return SyntaxKind.XML_PI;
            case "XML_ATTRIBUTE":
                return SyntaxKind.XML_ATTRIBUTE;
            case "XML_SIMPLE_NAME":
                return SyntaxKind.XML_SIMPLE_NAME;
            case "XML_QUALIFIED_NAME":
                return SyntaxKind.XML_QUALIFIED_NAME;
            case "INTERPOLATION":
                return SyntaxKind.INTERPOLATION;
            case "INTERPOLATION_START_TOKEN":
                return SyntaxKind.INTERPOLATION_START_TOKEN;
            case "XML_COMMENT":
                return SyntaxKind.XML_COMMENT;
            case "XML_COMMENT_START_TOKEN":
                return SyntaxKind.XML_COMMENT_START_TOKEN;
            case "XML_COMMENT_END_TOKEN":
                return SyntaxKind.XML_COMMENT_END_TOKEN;
            case "XML_TEXT_CONTENT":
                return SyntaxKind.XML_TEXT_CONTENT;
            case "XML_PI_START_TOKEN":
                return SyntaxKind.XML_PI_START_TOKEN;
            case "XML_PI_END_TOKEN":
                return SyntaxKind.XML_PI_END_TOKEN;
            case "XML_ATTRIBUTE_VALUE":
                return SyntaxKind.XML_ATTRIBUTE_VALUE;
            case "TEMPLATE_STRING":
                return SyntaxKind.TEMPLATE_STRING;

            // Documentation
            case "MARKDOWN_DOCUMENTATION":
                return SyntaxKind.MARKDOWN_DOCUMENTATION;
            case "MARKDOWN_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_DOCUMENTATION_LINE;
            case "MARKDOWN_REFERENCE_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_REFERENCE_DOCUMENTATION_LINE;
            case "MARKDOWN_PARAMETER_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE;
            case "MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE;
            case "MARKDOWN_DEPRECATION_DOCUMENTATION_LINE":
                return SyntaxKind.MARKDOWN_DEPRECATION_DOCUMENTATION_LINE;
            case "DOCUMENTATION_DESCRIPTION":
                return SyntaxKind.DOCUMENTATION_DESCRIPTION;
            case "DOCUMENTATION_REFERENCE":
                return SyntaxKind.DOCUMENTATION_REFERENCE;
            case "PARAMETER_NAME":
                return SyntaxKind.PARAMETER_NAME;
            case "BACKTICK_CONTENT":
                return SyntaxKind.BACKTICK_CONTENT;
            case "DEPRECATION_LITERAL":
                return SyntaxKind.DEPRECATION_LITERAL;
            case "DOCUMENTATION_STRING":
                return SyntaxKind.DOCUMENTATION_STRING;

            // Trivia
            case "EOF_TOKEN":
                return SyntaxKind.EOF_TOKEN;
            case "END_OF_LINE_MINUTIAE":
                return SyntaxKind.END_OF_LINE_MINUTIAE;
            case "WHITESPACE_MINUTIAE":
                return SyntaxKind.WHITESPACE_MINUTIAE;
            case "COMMENT_MINUTIAE":
                return SyntaxKind.COMMENT_MINUTIAE;
            case "INVALID_NODE_MINUTIAE":
                return SyntaxKind.INVALID_NODE_MINUTIAE;

            // Invalid Token
            case "INVALID_TOKEN":
                return SyntaxKind.INVALID_TOKEN;
            case "INVALID_TOKEN_MINUTIAE_NODE":
                return SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE;

            // Unsupported
            default:
                throw new UnsupportedOperationException("cannot find syntax kind: " + kind);
        }
    }
}
