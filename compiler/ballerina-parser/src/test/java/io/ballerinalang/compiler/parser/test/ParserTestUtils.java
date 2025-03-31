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
import io.ballerina.compiler.syntax.tree.CommentNode;
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
import java.util.Collection;
import java.util.List;

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
public final class ParserTestUtils {

    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/");

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
        return switch (syntaxKind) {
            case WHITESPACE_MINUTIAE, END_OF_LINE_MINUTIAE, COMMENT_MINUTIAE, INVALID_NODE_MINUTIAE -> true;
            default -> false;
        };
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
            case DEPRECATION_LITERAL:
            case INVALID_TOKEN:
                return token.text();
            case XML_TEXT:
            case XML_TEXT_CONTENT:
            case TEMPLATE_STRING:
            case RE_LITERAL_CHAR:
            case RE_NUMERIC_ESCAPE:
            case RE_CONTROL_ESCAPE:
            case RE_SIMPLE_CHAR_CLASS_CODE:
            case RE_PROPERTY:
            case RE_UNICODE_SCRIPT_START:
            case RE_UNICODE_PROPERTY_VALUE:
            case RE_UNICODE_GENERAL_CATEGORY_START:
            case RE_UNICODE_GENERAL_CATEGORY_NAME:
            case RE_FLAGS_VALUE:
            case DIGIT:
            case DOCUMENTATION_DESCRIPTION:
            case DOCUMENTATION_STRING:
            case CODE_CONTENT:
            case PROMPT_CONTENT:
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
        return switch (kind) {
            case "MODULE_PART" -> SyntaxKind.MODULE_PART;
            case "TYPE_DEFINITION" -> SyntaxKind.TYPE_DEFINITION;
            case "FUNCTION_DEFINITION" -> SyntaxKind.FUNCTION_DEFINITION;
            case "IMPORT_DECLARATION" -> SyntaxKind.IMPORT_DECLARATION;
            case "SERVICE_DECLARATION" -> SyntaxKind.SERVICE_DECLARATION;
            case "LISTENER_DECLARATION" -> SyntaxKind.LISTENER_DECLARATION;
            case "CONST_DECLARATION" -> SyntaxKind.CONST_DECLARATION;
            case "MODULE_VAR_DECL" -> SyntaxKind.MODULE_VAR_DECL;
            case "XML_NAMESPACE_DECLARATION" -> SyntaxKind.XML_NAMESPACE_DECLARATION;
            case "MODULE_XML_NAMESPACE_DECLARATION" -> SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION;
            case "ANNOTATION_DECLARATION" -> SyntaxKind.ANNOTATION_DECLARATION;
            case "ENUM_DECLARATION" -> SyntaxKind.ENUM_DECLARATION;
            case "CLASS_DEFINITION" -> SyntaxKind.CLASS_DEFINITION;

            // Others
            case "FUNCTION_BODY_BLOCK" -> SyntaxKind.FUNCTION_BODY_BLOCK;
            case "LIST" -> SyntaxKind.LIST;
            case "RETURN_TYPE_DESCRIPTOR" -> SyntaxKind.RETURN_TYPE_DESCRIPTOR;
            case "EXTERNAL_FUNCTION_BODY" -> SyntaxKind.EXTERNAL_FUNCTION_BODY;
            case "REQUIRED_PARAM" -> SyntaxKind.REQUIRED_PARAM;
            case "INCLUDED_RECORD_PARAM" -> SyntaxKind.INCLUDED_RECORD_PARAM;
            case "DEFAULTABLE_PARAM" -> SyntaxKind.DEFAULTABLE_PARAM;
            case "REST_PARAM" -> SyntaxKind.REST_PARAM;
            case "RECORD_FIELD" -> SyntaxKind.RECORD_FIELD;
            case "RECORD_FIELD_WITH_DEFAULT_VALUE" -> SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE;
            case "TYPE_REFERENCE" -> SyntaxKind.TYPE_REFERENCE;
            case "RECORD_REST_TYPE" -> SyntaxKind.RECORD_REST_TYPE;
            case "OBJECT_FIELD" -> SyntaxKind.OBJECT_FIELD;
            case "IMPORT_ORG_NAME" -> SyntaxKind.IMPORT_ORG_NAME;
            case "MODULE_NAME" -> SyntaxKind.MODULE_NAME;
            case "SUB_MODULE_NAME" -> SyntaxKind.SUB_MODULE_NAME;
            case "IMPORT_VERSION" -> SyntaxKind.IMPORT_VERSION;
            case "IMPORT_PREFIX" -> SyntaxKind.IMPORT_PREFIX;
            case "SPECIFIC_FIELD" -> SyntaxKind.SPECIFIC_FIELD;
            case "COMPUTED_NAME_FIELD" -> SyntaxKind.COMPUTED_NAME_FIELD;
            case "SPREAD_FIELD" -> SyntaxKind.SPREAD_FIELD;
            case "ARRAY_DIMENSION" -> SyntaxKind.ARRAY_DIMENSION;
            case "METADATA" -> SyntaxKind.METADATA;
            case "ANNOTATION" -> SyntaxKind.ANNOTATION;
            case "ANNOTATION_ATTACH_POINT" -> SyntaxKind.ANNOTATION_ATTACH_POINT;
            case "NAMED_WORKER_DECLARATION" -> SyntaxKind.NAMED_WORKER_DECLARATION;
            case "NAMED_WORKER_DECLARATOR" -> SyntaxKind.NAMED_WORKER_DECLARATOR;
            case "TYPE_CAST_PARAM" -> SyntaxKind.TYPE_CAST_PARAM;
            case "KEY_SPECIFIER" -> SyntaxKind.KEY_SPECIFIER;
            case "LET_VAR_DECL" -> SyntaxKind.LET_VAR_DECL;
            case "ORDER_KEY" -> SyntaxKind.ORDER_KEY;
            case "GROUPING_KEY_VAR_DECLARATION" -> SyntaxKind.GROUPING_KEY_VAR_DECLARATION;
            case "GROUPING_KEY_VAR_NAME" -> SyntaxKind.GROUPING_KEY_VAR_NAME;
            case "STREAM_TYPE_PARAMS" -> SyntaxKind.STREAM_TYPE_PARAMS;
            case "FUNCTION_SIGNATURE" -> SyntaxKind.FUNCTION_SIGNATURE;
            case "QUERY_CONSTRUCT_TYPE" -> SyntaxKind.QUERY_CONSTRUCT_TYPE;
            case "FROM_CLAUSE" -> SyntaxKind.FROM_CLAUSE;
            case "WHERE_CLAUSE" -> SyntaxKind.WHERE_CLAUSE;
            case "LET_CLAUSE" -> SyntaxKind.LET_CLAUSE;
            case "ON_FAIL_CLAUSE" -> SyntaxKind.ON_FAIL_CLAUSE;
            case "QUERY_PIPELINE" -> SyntaxKind.QUERY_PIPELINE;
            case "SELECT_CLAUSE" -> SyntaxKind.SELECT_CLAUSE;
            case "COLLECT_CLAUSE" -> SyntaxKind.COLLECT_CLAUSE;
            case "ORDER_BY_CLAUSE" -> SyntaxKind.ORDER_BY_CLAUSE;
            case "GROUP_BY_CLAUSE" -> SyntaxKind.GROUP_BY_CLAUSE;
            case "PARENTHESIZED_ARG_LIST" -> SyntaxKind.PARENTHESIZED_ARG_LIST;
            case "EXPRESSION_FUNCTION_BODY" -> SyntaxKind.EXPRESSION_FUNCTION_BODY;
            case "INFER_PARAM_LIST" -> SyntaxKind.INFER_PARAM_LIST;
            case "METHOD_DECLARATION" -> SyntaxKind.METHOD_DECLARATION;
            case "TYPED_BINDING_PATTERN" -> SyntaxKind.TYPED_BINDING_PATTERN;
            case "BINDING_PATTERN" -> SyntaxKind.BINDING_PATTERN;
            case "CAPTURE_BINDING_PATTERN" -> SyntaxKind.CAPTURE_BINDING_PATTERN;
            case "LIST_BINDING_PATTERN" -> SyntaxKind.LIST_BINDING_PATTERN;
            case "REST_BINDING_PATTERN" -> SyntaxKind.REST_BINDING_PATTERN;
            case "FIELD_BINDING_PATTERN" -> SyntaxKind.FIELD_BINDING_PATTERN;
            case "MAPPING_BINDING_PATTERN" -> SyntaxKind.MAPPING_BINDING_PATTERN;
            case "ERROR_BINDING_PATTERN" -> SyntaxKind.ERROR_BINDING_PATTERN;
            case "NAMED_ARG_BINDING_PATTERN" -> SyntaxKind.NAMED_ARG_BINDING_PATTERN;
            case "TYPE_PARAMETER" -> SyntaxKind.TYPE_PARAMETER;
            case "KEY_TYPE_CONSTRAINT" -> SyntaxKind.KEY_TYPE_CONSTRAINT;
            case "RECEIVE_FIELDS" -> SyntaxKind.RECEIVE_FIELDS;
            case "REST_TYPE" -> SyntaxKind.REST_TYPE;
            case "WAIT_FIELDS_LIST" -> SyntaxKind.WAIT_FIELDS_LIST;
            case "WAIT_FIELD" -> SyntaxKind.WAIT_FIELD;
            case "ENUM_MEMBER" -> SyntaxKind.ENUM_MEMBER;
            case "WILDCARD_BINDING_PATTERN" -> SyntaxKind.WILDCARD_BINDING_PATTERN;
            case "MATCH_CLAUSE" -> SyntaxKind.MATCH_CLAUSE;
            case "MATCH_GUARD" -> SyntaxKind.MATCH_GUARD;
            case "OBJECT_METHOD_DEFINITION" -> SyntaxKind.OBJECT_METHOD_DEFINITION;
            case "LIST_MATCH_PATTERN" -> SyntaxKind.LIST_MATCH_PATTERN;
            case "REST_MATCH_PATTERN" -> SyntaxKind.REST_MATCH_PATTERN;
            case "MAPPING_MATCH_PATTERN" -> SyntaxKind.MAPPING_MATCH_PATTERN;
            case "FIELD_MATCH_PATTERN" -> SyntaxKind.FIELD_MATCH_PATTERN;
            case "ERROR_MATCH_PATTERN" -> SyntaxKind.ERROR_MATCH_PATTERN;
            case "NAMED_ARG_MATCH_PATTERN" -> SyntaxKind.NAMED_ARG_MATCH_PATTERN;
            case "ON_CONFLICT_CLAUSE" -> SyntaxKind.ON_CONFLICT_CLAUSE;
            case "LIMIT_CLAUSE" -> SyntaxKind.LIMIT_CLAUSE;
            case "JOIN_CLAUSE" -> SyntaxKind.JOIN_CLAUSE;
            case "ON_CLAUSE" -> SyntaxKind.ON_CLAUSE;
            case "RESOURCE_ACCESSOR_DEFINITION" -> SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
            case "RESOURCE_ACCESSOR_DECLARATION" -> SyntaxKind.RESOURCE_ACCESSOR_DECLARATION;
            case "RESOURCE_PATH_SEGMENT_PARAM" -> SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM;
            case "RESOURCE_PATH_REST_PARAM" -> SyntaxKind.RESOURCE_PATH_REST_PARAM;
            case "CLIENT_RESOURCE_ACCESS_ACTION" -> SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION;
            case "COMPUTED_RESOURCE_ACCESS_SEGMENT" -> SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT;
            case "RESOURCE_ACCESS_REST_SEGMENT" -> SyntaxKind.RESOURCE_ACCESS_REST_SEGMENT;
            case "ALTERNATE_RECEIVE" -> SyntaxKind.ALTERNATE_RECEIVE;
            case "RECEIVE_FIELD" -> SyntaxKind.RECEIVE_FIELD;

            // Trivia
            case "EOF_TOKEN" -> SyntaxKind.EOF_TOKEN;
            case "END_OF_LINE_MINUTIAE" -> SyntaxKind.END_OF_LINE_MINUTIAE;
            case "WHITESPACE_MINUTIAE" -> SyntaxKind.WHITESPACE_MINUTIAE;
            case "COMMENT_MINUTIAE" -> SyntaxKind.COMMENT_MINUTIAE;
            case "INVALID_NODE_MINUTIAE" -> SyntaxKind.INVALID_NODE_MINUTIAE;

            // Invalid Token
            case "INVALID_TOKEN" -> SyntaxKind.INVALID_TOKEN;
            case "INVALID_TOKEN_MINUTIAE_NODE" -> SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE;
            default -> getStatementKind(kind);
        };
    }

    private static SyntaxKind getStatementKind(String kind) {
        return switch (kind) {
            case "BLOCK_STATEMENT" -> SyntaxKind.BLOCK_STATEMENT;
            case "LOCAL_VAR_DECL" -> SyntaxKind.LOCAL_VAR_DECL;
            case "ASSIGNMENT_STATEMENT" -> SyntaxKind.ASSIGNMENT_STATEMENT;
            case "IF_ELSE_STATEMENT" -> SyntaxKind.IF_ELSE_STATEMENT;
            case "ELSE_BLOCK" -> SyntaxKind.ELSE_BLOCK;
            case "WHILE_STATEMENT" -> SyntaxKind.WHILE_STATEMENT;
            case "DO_STATEMENT" -> SyntaxKind.DO_STATEMENT;
            case "CALL_STATEMENT" -> SyntaxKind.CALL_STATEMENT;
            case "PANIC_STATEMENT" -> SyntaxKind.PANIC_STATEMENT;
            case "CONTINUE_STATEMENT" -> SyntaxKind.CONTINUE_STATEMENT;
            case "BREAK_STATEMENT" -> SyntaxKind.BREAK_STATEMENT;
            case "FAIL_STATEMENT" -> SyntaxKind.FAIL_STATEMENT;
            case "RETURN_STATEMENT" -> SyntaxKind.RETURN_STATEMENT;
            case "COMPOUND_ASSIGNMENT_STATEMENT" -> SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT;
            case "LOCAL_TYPE_DEFINITION_STATEMENT" -> SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT;
            case "ACTION_STATEMENT" -> SyntaxKind.ACTION_STATEMENT;
            case "LOCK_STATEMENT" -> SyntaxKind.LOCK_STATEMENT;
            case "FORK_STATEMENT" -> SyntaxKind.FORK_STATEMENT;
            case "FOREACH_STATEMENT" -> SyntaxKind.FOREACH_STATEMENT;
            case "TRANSACTION_STATEMENT" -> SyntaxKind.TRANSACTION_STATEMENT;
            case "RETRY_STATEMENT" -> SyntaxKind.RETRY_STATEMENT;
            case "ROLLBACK_STATEMENT" -> SyntaxKind.ROLLBACK_STATEMENT;
            case "MATCH_STATEMENT" -> SyntaxKind.MATCH_STATEMENT;
            case "INVALID_EXPRESSION_STATEMENT" -> SyntaxKind.INVALID_EXPRESSION_STATEMENT;
            default -> getExpressionKind(kind);
        };
    }

    private static SyntaxKind getExpressionKind(String kind) {
        return switch (kind) {
            case "IDENTIFIER_TOKEN" -> SyntaxKind.IDENTIFIER_TOKEN;
            case "BRACED_EXPRESSION" -> SyntaxKind.BRACED_EXPRESSION;
            case "BINARY_EXPRESSION" -> SyntaxKind.BINARY_EXPRESSION;
            case "STRING_LITERAL" -> SyntaxKind.STRING_LITERAL;
            case "STRING_LITERAL_TOKEN" -> SyntaxKind.STRING_LITERAL_TOKEN;
            case "NUMERIC_LITERAL" -> SyntaxKind.NUMERIC_LITERAL;
            case "BOOLEAN_LITERAL" -> SyntaxKind.BOOLEAN_LITERAL;
            case "DECIMAL_INTEGER_LITERAL_TOKEN" -> SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN;
            case "HEX_INTEGER_LITERAL_TOKEN" -> SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
            case "DECIMAL_FLOATING_POINT_LITERAL_TOKEN" -> SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN;
            case "HEX_FLOATING_POINT_LITERAL_TOKEN" -> SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN;
            case "ASTERISK_LITERAL" -> SyntaxKind.ASTERISK_LITERAL;
            case "FUNCTION_CALL" -> SyntaxKind.FUNCTION_CALL;
            case "POSITIONAL_ARG" -> SyntaxKind.POSITIONAL_ARG;
            case "NAMED_ARG" -> SyntaxKind.NAMED_ARG;
            case "REST_ARG" -> SyntaxKind.REST_ARG;
            case "QUALIFIED_NAME_REFERENCE" -> SyntaxKind.QUALIFIED_NAME_REFERENCE;
            case "FIELD_ACCESS" -> SyntaxKind.FIELD_ACCESS;
            case "METHOD_CALL" -> SyntaxKind.METHOD_CALL;
            case "INDEXED_EXPRESSION" -> SyntaxKind.INDEXED_EXPRESSION;
            case "CHECK_EXPRESSION" -> SyntaxKind.CHECK_EXPRESSION;
            case "MAPPING_CONSTRUCTOR" -> SyntaxKind.MAPPING_CONSTRUCTOR;
            case "TYPEOF_EXPRESSION" -> SyntaxKind.TYPEOF_EXPRESSION;
            case "UNARY_EXPRESSION" -> SyntaxKind.UNARY_EXPRESSION;
            case "TYPE_TEST_EXPRESSION" -> SyntaxKind.TYPE_TEST_EXPRESSION;
            case "NIL_LITERAL" -> SyntaxKind.NIL_LITERAL;
            case "NULL_LITERAL" -> SyntaxKind.NULL_LITERAL;
            case "SIMPLE_NAME_REFERENCE" -> SyntaxKind.SIMPLE_NAME_REFERENCE;
            case "TRAP_EXPRESSION" -> SyntaxKind.TRAP_EXPRESSION;
            case "LIST_CONSTRUCTOR" -> SyntaxKind.LIST_CONSTRUCTOR;
            case "TYPE_CAST_EXPRESSION" -> SyntaxKind.TYPE_CAST_EXPRESSION;
            case "TABLE_CONSTRUCTOR" -> SyntaxKind.TABLE_CONSTRUCTOR;
            case "LET_EXPRESSION" -> SyntaxKind.LET_EXPRESSION;
            case "RAW_TEMPLATE_EXPRESSION" -> SyntaxKind.RAW_TEMPLATE_EXPRESSION;
            case "XML_TEMPLATE_EXPRESSION" -> SyntaxKind.XML_TEMPLATE_EXPRESSION;
            case "STRING_TEMPLATE_EXPRESSION" -> SyntaxKind.STRING_TEMPLATE_EXPRESSION;
            case "REGEX_TEMPLATE_EXPRESSION" -> SyntaxKind.REGEX_TEMPLATE_EXPRESSION;
            case "QUERY_EXPRESSION" -> SyntaxKind.QUERY_EXPRESSION;
            case "EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION" -> SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION;
            case "IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION" -> SyntaxKind.IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION;
            case "IMPLICIT_NEW_EXPRESSION" -> SyntaxKind.IMPLICIT_NEW_EXPRESSION;
            case "EXPLICIT_NEW_EXPRESSION" -> SyntaxKind.EXPLICIT_NEW_EXPRESSION;
            case "ANNOT_ACCESS" -> SyntaxKind.ANNOT_ACCESS;
            case "OPTIONAL_FIELD_ACCESS" -> SyntaxKind.OPTIONAL_FIELD_ACCESS;
            case "CONDITIONAL_EXPRESSION" -> SyntaxKind.CONDITIONAL_EXPRESSION;
            case "TRANSACTIONAL_EXPRESSION" -> SyntaxKind.TRANSACTIONAL_EXPRESSION;
            case "BYTE_ARRAY_LITERAL" -> SyntaxKind.BYTE_ARRAY_LITERAL;
            case "XML_FILTER_EXPRESSION" -> SyntaxKind.XML_FILTER_EXPRESSION;
            case "XML_STEP_EXPRESSION" -> SyntaxKind.XML_STEP_EXPRESSION;
            case "XML_NAME_PATTERN_CHAIN" -> SyntaxKind.XML_NAME_PATTERN_CHAIN;
            case "XML_ATOMIC_NAME_PATTERN" -> SyntaxKind.XML_ATOMIC_NAME_PATTERN;
            case "XML_STEP_INDEXED_EXTEND" -> SyntaxKind.XML_STEP_INDEXED_EXTEND;
            case "XML_STEP_METHOD_CALL_EXTEND" -> SyntaxKind.XML_STEP_METHOD_CALL_EXTEND;
            case "REQUIRED_EXPRESSION" -> SyntaxKind.REQUIRED_EXPRESSION;
            case "OBJECT_CONSTRUCTOR" -> SyntaxKind.OBJECT_CONSTRUCTOR;
            case "ERROR_CONSTRUCTOR" -> SyntaxKind.ERROR_CONSTRUCTOR;
            case "INFERRED_TYPEDESC_DEFAULT" -> SyntaxKind.INFERRED_TYPEDESC_DEFAULT;
            case "SPREAD_MEMBER" -> SyntaxKind.SPREAD_MEMBER;
            case "MEMBER_TYPE_DESC" -> SyntaxKind.MEMBER_TYPE_DESC;
            case "NATURAL_EXPRESSION" -> SyntaxKind.NATURAL_EXPRESSION;
            default -> getActionKind(kind);
        };
    }

    private static SyntaxKind getActionKind(String kind) {
        return switch (kind) {
            case "REMOTE_METHOD_CALL_ACTION" -> SyntaxKind.REMOTE_METHOD_CALL_ACTION;
            case "BRACED_ACTION" -> SyntaxKind.BRACED_ACTION;
            case "CHECK_ACTION" -> SyntaxKind.CHECK_ACTION;
            case "START_ACTION" -> SyntaxKind.START_ACTION;
            case "TRAP_ACTION" -> SyntaxKind.TRAP_ACTION;
            case "FLUSH_ACTION" -> SyntaxKind.FLUSH_ACTION;
            case "ASYNC_SEND_ACTION" -> SyntaxKind.ASYNC_SEND_ACTION;
            case "SYNC_SEND_ACTION" -> SyntaxKind.SYNC_SEND_ACTION;
            case "RECEIVE_ACTION" -> SyntaxKind.RECEIVE_ACTION;
            case "WAIT_ACTION" -> SyntaxKind.WAIT_ACTION;
            case "QUERY_ACTION" -> SyntaxKind.QUERY_ACTION;
            case "COMMIT_ACTION" -> SyntaxKind.COMMIT_ACTION;
            default -> getTypeKind(kind);
        };
    }

    private static SyntaxKind getTypeKind(String kind) {
        return switch (kind) {
            case "TYPE_DESC" -> SyntaxKind.TYPE_DESC;
            case "INT_TYPE_DESC" -> SyntaxKind.INT_TYPE_DESC;
            case "FLOAT_TYPE_DESC" -> SyntaxKind.FLOAT_TYPE_DESC;
            case "DECIMAL_TYPE_DESC" -> SyntaxKind.DECIMAL_TYPE_DESC;
            case "BOOLEAN_TYPE_DESC" -> SyntaxKind.BOOLEAN_TYPE_DESC;
            case "STRING_TYPE_DESC" -> SyntaxKind.STRING_TYPE_DESC;
            case "BYTE_TYPE_DESC" -> SyntaxKind.BYTE_TYPE_DESC;
            case "XML_TYPE_DESC" -> SyntaxKind.XML_TYPE_DESC;
            case "JSON_TYPE_DESC" -> SyntaxKind.JSON_TYPE_DESC;
            case "HANDLE_TYPE_DESC" -> SyntaxKind.HANDLE_TYPE_DESC;
            case "ANY_TYPE_DESC" -> SyntaxKind.ANY_TYPE_DESC;
            case "ANYDATA_TYPE_DESC" -> SyntaxKind.ANYDATA_TYPE_DESC;
            case "NEVER_TYPE_DESC" -> SyntaxKind.NEVER_TYPE_DESC;
            case "NIL_TYPE_DESC" -> SyntaxKind.NIL_TYPE_DESC;
            case "OPTIONAL_TYPE_DESC" -> SyntaxKind.OPTIONAL_TYPE_DESC;
            case "ARRAY_TYPE_DESC" -> SyntaxKind.ARRAY_TYPE_DESC;
            case "RECORD_TYPE_DESC" -> SyntaxKind.RECORD_TYPE_DESC;
            case "OBJECT_TYPE_DESC" -> SyntaxKind.OBJECT_TYPE_DESC;
            case "UNION_TYPE_DESC" -> SyntaxKind.UNION_TYPE_DESC;
            case "ERROR_TYPE_DESC" -> SyntaxKind.ERROR_TYPE_DESC;
            case "EXPLICIT_TYPE_PARAMS" -> SyntaxKind.EXPLICIT_TYPE_PARAMS;
            case "STREAM_TYPE_DESC" -> SyntaxKind.STREAM_TYPE_DESC;
            case "FUNCTION_TYPE_DESC" -> SyntaxKind.FUNCTION_TYPE_DESC;
            case "TABLE_TYPE_DESC" -> SyntaxKind.TABLE_TYPE_DESC;
            case "TUPLE_TYPE_DESC" -> SyntaxKind.TUPLE_TYPE_DESC;
            case "PARENTHESISED_TYPE_DESC" -> SyntaxKind.PARENTHESISED_TYPE_DESC;
            case "READONLY_TYPE_DESC" -> SyntaxKind.READONLY_TYPE_DESC;
            case "DISTINCT_TYPE_DESC" -> SyntaxKind.DISTINCT_TYPE_DESC;
            case "INTERSECTION_TYPE_DESC" -> SyntaxKind.INTERSECTION_TYPE_DESC;
            case "SINGLETON_TYPE_DESC" -> SyntaxKind.SINGLETON_TYPE_DESC;
            case "TYPEDESC_TYPE_DESC" -> SyntaxKind.TYPEDESC_TYPE_DESC;
            case "VAR_TYPE_DESC" -> SyntaxKind.VAR_TYPE_DESC;
            case "SERVICE_TYPE_DESC" -> SyntaxKind.SERVICE_TYPE_DESC;
            case "MAP_TYPE_DESC" -> SyntaxKind.MAP_TYPE_DESC;
            case "FUTURE_TYPE_DESC" -> SyntaxKind.FUTURE_TYPE_DESC;
            default -> getOperatorKind(kind);
        };
    }
    
    private static SyntaxKind getOperatorKind(String kind) {
        return switch (kind) {
            case "PLUS_TOKEN" -> SyntaxKind.PLUS_TOKEN;
            case "MINUS_TOKEN" -> SyntaxKind.MINUS_TOKEN;
            case "ASTERISK_TOKEN" -> SyntaxKind.ASTERISK_TOKEN;
            case "SLASH_TOKEN" -> SyntaxKind.SLASH_TOKEN;
            case "LT_TOKEN" -> SyntaxKind.LT_TOKEN;
            case "EQUAL_TOKEN" -> SyntaxKind.EQUAL_TOKEN;
            case "DOUBLE_EQUAL_TOKEN" -> SyntaxKind.DOUBLE_EQUAL_TOKEN;
            case "TRIPPLE_EQUAL_TOKEN" -> SyntaxKind.TRIPPLE_EQUAL_TOKEN;
            case "PERCENT_TOKEN" -> SyntaxKind.PERCENT_TOKEN;
            case "GT_TOKEN" -> SyntaxKind.GT_TOKEN;
            case "RIGHT_DOUBLE_ARROW_TOKEN" -> SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
            case "QUESTION_MARK_TOKEN" -> SyntaxKind.QUESTION_MARK_TOKEN;
            case "LT_EQUAL_TOKEN" -> SyntaxKind.LT_EQUAL_TOKEN;
            case "GT_EQUAL_TOKEN" -> SyntaxKind.GT_EQUAL_TOKEN;
            case "EXCLAMATION_MARK_TOKEN" -> SyntaxKind.EXCLAMATION_MARK_TOKEN;
            case "NOT_EQUAL_TOKEN" -> SyntaxKind.NOT_EQUAL_TOKEN;
            case "NOT_DOUBLE_EQUAL_TOKEN" -> SyntaxKind.NOT_DOUBLE_EQUAL_TOKEN;
            case "BITWISE_AND_TOKEN" -> SyntaxKind.BITWISE_AND_TOKEN;
            case "BITWISE_XOR_TOKEN" -> SyntaxKind.BITWISE_XOR_TOKEN;
            case "LOGICAL_AND_TOKEN" -> SyntaxKind.LOGICAL_AND_TOKEN;
            case "LOGICAL_OR_TOKEN" -> SyntaxKind.LOGICAL_OR_TOKEN;
            case "NEGATION_TOKEN" -> SyntaxKind.NEGATION_TOKEN;
            case "DOUBLE_LT_TOKEN" -> SyntaxKind.DOUBLE_LT_TOKEN;
            case "DOUBLE_GT_TOKEN" -> SyntaxKind.DOUBLE_GT_TOKEN;
            case "TRIPPLE_GT_TOKEN" -> SyntaxKind.TRIPPLE_GT_TOKEN;
            case "DOUBLE_DOT_LT_TOKEN" -> SyntaxKind.DOUBLE_DOT_LT_TOKEN;
            case "ANNOT_CHAINING_TOKEN" -> SyntaxKind.ANNOT_CHAINING_TOKEN;
            case "OPTIONAL_CHAINING_TOKEN" -> SyntaxKind.OPTIONAL_CHAINING_TOKEN;
            case "ELVIS_TOKEN" -> SyntaxKind.ELVIS_TOKEN;
            case "DOT_LT_TOKEN" -> SyntaxKind.DOT_LT_TOKEN;
            case "SLASH_LT_TOKEN" -> SyntaxKind.SLASH_LT_TOKEN;
            case "DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN" -> SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
            case "SLASH_ASTERISK_TOKEN" -> SyntaxKind.SLASH_ASTERISK_TOKEN;
            default -> getSeparatorKind(kind);
        };
    
    }

    private static SyntaxKind getSeparatorKind(String kind) {
        return switch (kind) {
            case "OPEN_BRACE_TOKEN" -> SyntaxKind.OPEN_BRACE_TOKEN;
            case "CLOSE_BRACE_TOKEN" -> SyntaxKind.CLOSE_BRACE_TOKEN;
            case "OPEN_PAREN_TOKEN" -> SyntaxKind.OPEN_PAREN_TOKEN;
            case "CLOSE_PAREN_TOKEN" -> SyntaxKind.CLOSE_PAREN_TOKEN;
            case "OPEN_BRACKET_TOKEN" -> SyntaxKind.OPEN_BRACKET_TOKEN;
            case "CLOSE_BRACKET_TOKEN" -> SyntaxKind.CLOSE_BRACKET_TOKEN;
            case "SEMICOLON_TOKEN" -> SyntaxKind.SEMICOLON_TOKEN;
            case "DOT_TOKEN" -> SyntaxKind.DOT_TOKEN;
            case "COLON_TOKEN" -> SyntaxKind.COLON_TOKEN;
            case "COMMA_TOKEN" -> SyntaxKind.COMMA_TOKEN;
            case "ELLIPSIS_TOKEN" -> SyntaxKind.ELLIPSIS_TOKEN;
            case "OPEN_BRACE_PIPE_TOKEN" -> SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case "CLOSE_BRACE_PIPE_TOKEN" -> SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
            case "PIPE_TOKEN" -> SyntaxKind.PIPE_TOKEN;
            case "AT_TOKEN" -> SyntaxKind.AT_TOKEN;
            case "RIGHT_ARROW_TOKEN" -> SyntaxKind.RIGHT_ARROW_TOKEN;
            case "BACKTICK_TOKEN" -> SyntaxKind.BACKTICK_TOKEN;
            case "DOUBLE_BACKTICK_TOKEN" -> SyntaxKind.DOUBLE_BACKTICK_TOKEN;
            case "TRIPLE_BACKTICK_TOKEN" -> SyntaxKind.TRIPLE_BACKTICK_TOKEN;
            case "DOUBLE_QUOTE_TOKEN" -> SyntaxKind.DOUBLE_QUOTE_TOKEN;
            case "SINGLE_QUOTE_TOKEN" -> SyntaxKind.SINGLE_QUOTE_TOKEN;
            case "SYNC_SEND_TOKEN" -> SyntaxKind.SYNC_SEND_TOKEN;
            case "LEFT_ARROW_TOKEN" -> SyntaxKind.LEFT_ARROW_TOKEN;
            case "HASH_TOKEN" -> SyntaxKind.HASH_TOKEN;
            case "BACK_SLASH_TOKEN" -> SyntaxKind.BACK_SLASH_TOKEN;
            default -> getKeywordKind(kind);
        };
    }
    
    private static SyntaxKind getKeywordKind(String kind) {
        return switch (kind) {
            case "PUBLIC_KEYWORD" -> SyntaxKind.PUBLIC_KEYWORD;
            case "PRIVATE_KEYWORD" -> SyntaxKind.PRIVATE_KEYWORD;
            case "FUNCTION_KEYWORD" -> SyntaxKind.FUNCTION_KEYWORD;
            case "TYPE_KEYWORD" -> SyntaxKind.TYPE_KEYWORD;
            case "EXTERNAL_KEYWORD" -> SyntaxKind.EXTERNAL_KEYWORD;
            case "RETURNS_KEYWORD" -> SyntaxKind.RETURNS_KEYWORD;
            case "RECORD_KEYWORD" -> SyntaxKind.RECORD_KEYWORD;
            case "OBJECT_KEYWORD" -> SyntaxKind.OBJECT_KEYWORD;
            case "REMOTE_KEYWORD" -> SyntaxKind.REMOTE_KEYWORD;
            case "CLIENT_KEYWORD" -> SyntaxKind.CLIENT_KEYWORD;
            case "ABSTRACT_KEYWORD" -> SyntaxKind.ABSTRACT_KEYWORD;
            case "IF_KEYWORD" -> SyntaxKind.IF_KEYWORD;
            case "ELSE_KEYWORD" -> SyntaxKind.ELSE_KEYWORD;
            case "WHILE_KEYWORD" -> SyntaxKind.WHILE_KEYWORD;
            case "TRUE_KEYWORD" -> SyntaxKind.TRUE_KEYWORD;
            case "FALSE_KEYWORD" -> SyntaxKind.FALSE_KEYWORD;
            case "CHECK_KEYWORD" -> SyntaxKind.CHECK_KEYWORD;
            case "CHECKPANIC_KEYWORD" -> SyntaxKind.CHECKPANIC_KEYWORD;
            case "FAIL_KEYWORD" -> SyntaxKind.FAIL_KEYWORD;
            case "PANIC_KEYWORD" -> SyntaxKind.PANIC_KEYWORD;
            case "IMPORT_KEYWORD" -> SyntaxKind.IMPORT_KEYWORD;
            case "AS_KEYWORD" -> SyntaxKind.AS_KEYWORD;
            case "CONTINUE_KEYWORD" -> SyntaxKind.CONTINUE_KEYWORD;
            case "BREAK_KEYWORD" -> SyntaxKind.BREAK_KEYWORD;
            case "RETURN_KEYWORD" -> SyntaxKind.RETURN_KEYWORD;
            case "SERVICE_KEYWORD" -> SyntaxKind.SERVICE_KEYWORD;
            case "ON_KEYWORD" -> SyntaxKind.ON_KEYWORD;
            case "RESOURCE_KEYWORD" -> SyntaxKind.RESOURCE_KEYWORD;
            case "LISTENER_KEYWORD" -> SyntaxKind.LISTENER_KEYWORD;
            case "CONST_KEYWORD" -> SyntaxKind.CONST_KEYWORD;
            case "FINAL_KEYWORD" -> SyntaxKind.FINAL_KEYWORD;
            case "TYPEOF_KEYWORD" -> SyntaxKind.TYPEOF_KEYWORD;
            case "ANNOTATION_KEYWORD" -> SyntaxKind.ANNOTATION_KEYWORD;
            case "IS_KEYWORD" -> SyntaxKind.IS_KEYWORD;
            case "NOT_IS_KEYWORD" -> SyntaxKind.NOT_IS_KEYWORD;
            case "MAP_KEYWORD" -> SyntaxKind.MAP_KEYWORD;
            case "FUTURE_KEYWORD" -> SyntaxKind.FUTURE_KEYWORD;
            case "TYPEDESC_KEYWORD" -> SyntaxKind.TYPEDESC_KEYWORD;
            case "NULL_KEYWORD" -> SyntaxKind.NULL_KEYWORD;
            case "LOCK_KEYWORD" -> SyntaxKind.LOCK_KEYWORD;
            case "VAR_KEYWORD" -> SyntaxKind.VAR_KEYWORD;
            case "SOURCE_KEYWORD" -> SyntaxKind.SOURCE_KEYWORD;
            case "WORKER_KEYWORD" -> SyntaxKind.WORKER_KEYWORD;
            case "PARAMETER_KEYWORD" -> SyntaxKind.PARAMETER_KEYWORD;
            case "FIELD_KEYWORD" -> SyntaxKind.FIELD_KEYWORD;
            case "XMLNS_KEYWORD" -> SyntaxKind.XMLNS_KEYWORD;
            case "INT_KEYWORD" -> SyntaxKind.INT_KEYWORD;
            case "FLOAT_KEYWORD" -> SyntaxKind.FLOAT_KEYWORD;
            case "DECIMAL_KEYWORD" -> SyntaxKind.DECIMAL_KEYWORD;
            case "BOOLEAN_KEYWORD" -> SyntaxKind.BOOLEAN_KEYWORD;
            case "STRING_KEYWORD" -> SyntaxKind.STRING_KEYWORD;
            case "BYTE_KEYWORD" -> SyntaxKind.BYTE_KEYWORD;
            case "XML_KEYWORD" -> SyntaxKind.XML_KEYWORD;
            case "RE_KEYWORD" -> SyntaxKind.RE_KEYWORD;
            case "JSON_KEYWORD" -> SyntaxKind.JSON_KEYWORD;
            case "HANDLE_KEYWORD" -> SyntaxKind.HANDLE_KEYWORD;
            case "ANY_KEYWORD" -> SyntaxKind.ANY_KEYWORD;
            case "ANYDATA_KEYWORD" -> SyntaxKind.ANYDATA_KEYWORD;
            case "NEVER_KEYWORD" -> SyntaxKind.NEVER_KEYWORD;
            case "FORK_KEYWORD" -> SyntaxKind.FORK_KEYWORD;
            case "TRAP_KEYWORD" -> SyntaxKind.TRAP_KEYWORD;
            case "FOREACH_KEYWORD" -> SyntaxKind.FOREACH_KEYWORD;
            case "IN_KEYWORD" -> SyntaxKind.IN_KEYWORD;
            case "TABLE_KEYWORD" -> SyntaxKind.TABLE_KEYWORD;
            case "KEY_KEYWORD" -> SyntaxKind.KEY_KEYWORD;
            case "ERROR_KEYWORD" -> SyntaxKind.ERROR_KEYWORD;
            case "LET_KEYWORD" -> SyntaxKind.LET_KEYWORD;
            case "STREAM_KEYWORD" -> SyntaxKind.STREAM_KEYWORD;
            case "READONLY_KEYWORD" -> SyntaxKind.READONLY_KEYWORD;
            case "DISTINCT_KEYWORD" -> SyntaxKind.DISTINCT_KEYWORD;
            case "FROM_KEYWORD" -> SyntaxKind.FROM_KEYWORD;
            case "WHERE_KEYWORD" -> SyntaxKind.WHERE_KEYWORD;
            case "SELECT_KEYWORD" -> SyntaxKind.SELECT_KEYWORD;
            case "COLLECT_KEYWORD" -> SyntaxKind.COLLECT_KEYWORD;
            case "ORDER_KEYWORD" -> SyntaxKind.ORDER_KEYWORD;
            case "BY_KEYWORD" -> SyntaxKind.BY_KEYWORD;
            case "GROUP_KEYWORD" -> SyntaxKind.GROUP_KEYWORD;
            case "ASCENDING_KEYWORD" -> SyntaxKind.ASCENDING_KEYWORD;
            case "DESCENDING_KEYWORD" -> SyntaxKind.DESCENDING_KEYWORD;
            case "NEW_KEYWORD" -> SyntaxKind.NEW_KEYWORD;
            case "START_KEYWORD" -> SyntaxKind.START_KEYWORD;
            case "FLUSH_KEYWORD" -> SyntaxKind.FLUSH_KEYWORD;
            case "WAIT_KEYWORD" -> SyntaxKind.WAIT_KEYWORD;
            case "DO_KEYWORD" -> SyntaxKind.DO_KEYWORD;
            case "TRANSACTION_KEYWORD" -> SyntaxKind.TRANSACTION_KEYWORD;
            case "COMMIT_KEYWORD" -> SyntaxKind.COMMIT_KEYWORD;
            case "RETRY_KEYWORD" -> SyntaxKind.RETRY_KEYWORD;
            case "ROLLBACK_KEYWORD" -> SyntaxKind.ROLLBACK_KEYWORD;
            case "TRANSACTIONAL_KEYWORD" -> SyntaxKind.TRANSACTIONAL_KEYWORD;
            case "ISOLATED_KEYWORD" -> SyntaxKind.ISOLATED_KEYWORD;
            case "ENUM_KEYWORD" -> SyntaxKind.ENUM_KEYWORD;
            case "BASE16_KEYWORD" -> SyntaxKind.BASE16_KEYWORD;
            case "BASE64_KEYWORD" -> SyntaxKind.BASE64_KEYWORD;
            case "MATCH_KEYWORD" -> SyntaxKind.MATCH_KEYWORD;
            case "CLASS_KEYWORD" -> SyntaxKind.CLASS_KEYWORD;
            case "CONFLICT_KEYWORD" -> SyntaxKind.CONFLICT_KEYWORD;
            case "LIMIT_KEYWORD" -> SyntaxKind.LIMIT_KEYWORD;
            case "JOIN_KEYWORD" -> SyntaxKind.JOIN_KEYWORD;
            case "EQUALS_KEYWORD" -> SyntaxKind.EQUALS_KEYWORD;
            case "OUTER_KEYWORD" -> SyntaxKind.OUTER_KEYWORD;
            case "CONFIGURABLE_KEYWORD" -> SyntaxKind.CONFIGURABLE_KEYWORD;
            case "UNDERSCORE_KEYWORD" -> SyntaxKind.UNDERSCORE_KEYWORD;
            case "NATURAL_KEYWORD" -> SyntaxKind.NATURAL_KEYWORD;
            default -> getXMLTemplateKind(kind);
        };
    }

    private static SyntaxKind getXMLTemplateKind(String kind) {
        return switch (kind) {
            case "XML_ELEMENT" -> SyntaxKind.XML_ELEMENT;
            case "XML_EMPTY_ELEMENT" -> SyntaxKind.XML_EMPTY_ELEMENT;
            case "XML_ELEMENT_START_TAG" -> SyntaxKind.XML_ELEMENT_START_TAG;
            case "XML_ELEMENT_END_TAG" -> SyntaxKind.XML_ELEMENT_END_TAG;
            case "XML_TEXT" -> SyntaxKind.XML_TEXT;
            case "XML_PI" -> SyntaxKind.XML_PI;
            case "XML_ATTRIBUTE" -> SyntaxKind.XML_ATTRIBUTE;
            case "XML_SIMPLE_NAME" -> SyntaxKind.XML_SIMPLE_NAME;
            case "XML_QUALIFIED_NAME" -> SyntaxKind.XML_QUALIFIED_NAME;
            case "INTERPOLATION" -> SyntaxKind.INTERPOLATION;
            case "INTERPOLATION_START_TOKEN" -> SyntaxKind.INTERPOLATION_START_TOKEN;
            case "XML_COMMENT" -> SyntaxKind.XML_COMMENT;
            case "XML_COMMENT_START_TOKEN" -> SyntaxKind.XML_COMMENT_START_TOKEN;
            case "XML_COMMENT_END_TOKEN" -> SyntaxKind.XML_COMMENT_END_TOKEN;
            case "XML_TEXT_CONTENT" -> SyntaxKind.XML_TEXT_CONTENT;
            case "XML_PI_START_TOKEN" -> SyntaxKind.XML_PI_START_TOKEN;
            case "XML_PI_END_TOKEN" -> SyntaxKind.XML_PI_END_TOKEN;
            case "XML_ATTRIBUTE_VALUE" -> SyntaxKind.XML_ATTRIBUTE_VALUE;
            case "TEMPLATE_STRING" -> SyntaxKind.TEMPLATE_STRING;
            case "XML_CDATA" -> SyntaxKind.XML_CDATA;
            case "XML_CDATA_START_TOKEN" -> SyntaxKind.XML_CDATA_START_TOKEN;
            case "XML_CDATA_END_TOKEN" -> SyntaxKind.XML_CDATA_END_TOKEN;
            default -> getRegExpTemplateKind(kind);
        };
    }

    private static SyntaxKind getRegExpTemplateKind(String kind) {
        return switch (kind) {
            case "RE_SEQUENCE" -> SyntaxKind.RE_SEQUENCE;
            case "RE_ATOM_QUANTIFIER" -> SyntaxKind.RE_ATOM_QUANTIFIER;
            case "RE_ASSERTION" -> SyntaxKind.RE_ASSERTION;
            case "DOLLAR_TOKEN" -> SyntaxKind.DOLLAR_TOKEN;
            case "DOT_TOKEN" -> SyntaxKind.DOT_TOKEN;
            case "RE_LITERAL_CHAR_DOT_OR_ESCAPE" -> SyntaxKind.RE_LITERAL_CHAR_DOT_OR_ESCAPE;
            case "RE_LITERAL_CHAR" -> SyntaxKind.RE_LITERAL_CHAR;
            case "RE_NUMERIC_ESCAPE" -> SyntaxKind.RE_NUMERIC_ESCAPE;
            case "RE_CONTROL_ESCAPE" -> SyntaxKind.RE_CONTROL_ESCAPE;
            case "RE_QUOTE_ESCAPE" -> SyntaxKind.RE_QUOTE_ESCAPE;
            case "RE_SIMPLE_CHAR_CLASS_ESCAPE" -> SyntaxKind.RE_SIMPLE_CHAR_CLASS_ESCAPE;
            case "RE_SIMPLE_CHAR_CLASS_CODE" -> SyntaxKind.RE_SIMPLE_CHAR_CLASS_CODE;
            case "RE_UNICODE_PROPERTY_ESCAPE" -> SyntaxKind.RE_UNICODE_PROPERTY_ESCAPE;
            case "RE_PROPERTY" -> SyntaxKind.RE_PROPERTY;
            case "RE_UNICODE_SCRIPT" -> SyntaxKind.RE_UNICODE_SCRIPT;
            case "RE_UNICODE_SCRIPT_START" -> SyntaxKind.RE_UNICODE_SCRIPT_START;
            case "RE_UNICODE_PROPERTY_VALUE" -> SyntaxKind.RE_UNICODE_PROPERTY_VALUE;
            case "RE_UNICODE_GENERAL_CATEGORY" -> SyntaxKind.RE_UNICODE_GENERAL_CATEGORY;
            case "RE_UNICODE_GENERAL_CATEGORY_START" -> SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_START;
            case "RE_UNICODE_GENERAL_CATEGORY_NAME" -> SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_NAME;
            case "RE_CHARACTER_CLASS" -> SyntaxKind.RE_CHARACTER_CLASS;
            case "RE_CHAR_SET_RANGE" -> SyntaxKind.RE_CHAR_SET_RANGE;
            case "RE_CHAR_SET_RANGE_NO_DASH" -> SyntaxKind.RE_CHAR_SET_RANGE_NO_DASH;
            case "RE_CHAR_SET_RANGE_WITH_RE_CHAR_SET" -> SyntaxKind.RE_CHAR_SET_RANGE_WITH_RE_CHAR_SET;
            case "RE_CHAR_SET_RANGE_NO_DASH_WITH_RE_CHAR_SET" -> SyntaxKind.RE_CHAR_SET_RANGE_NO_DASH_WITH_RE_CHAR_SET;
            case "RE_CHAR_SET_ATOM_WITH_RE_CHAR_SET_NO_DASH" -> SyntaxKind.RE_CHAR_SET_ATOM_WITH_RE_CHAR_SET_NO_DASH;
            case "RE_CHAR_SET_ATOM_NO_DASH_WITH_RE_CHAR_SET_NO_DASH" ->
                    SyntaxKind.RE_CHAR_SET_ATOM_NO_DASH_WITH_RE_CHAR_SET_NO_DASH;
            case "RE_CAPTURING_GROUP" -> SyntaxKind.RE_CAPTURING_GROUP;
            case "RE_FLAG_EXPR" -> SyntaxKind.RE_FLAG_EXPR;
            case "RE_FLAGS_ON_OFF" -> SyntaxKind.RE_FLAGS_ON_OFF;
            case "RE_FLAGS" -> SyntaxKind.RE_FLAGS;
            case "RE_FLAGS_VALUE" -> SyntaxKind.RE_FLAGS_VALUE;
            case "RE_QUANTIFIER" -> SyntaxKind.RE_QUANTIFIER;
            case "RE_BRACED_QUANTIFIER" -> SyntaxKind.RE_BRACED_QUANTIFIER;
            case "DIGIT" -> SyntaxKind.DIGIT;
            case "ESCAPED_MINUS_TOKEN" -> SyntaxKind.ESCAPED_MINUS_TOKEN;
            default -> getDocumentationKind(kind);
        };
    }
    
    private static SyntaxKind getDocumentationKind(String kind) {
        return switch (kind) {
            // Documentation reference
            case "TYPE_DOC_REFERENCE_TOKEN" -> SyntaxKind.TYPE_DOC_REFERENCE_TOKEN;
            case "SERVICE_DOC_REFERENCE_TOKEN" -> SyntaxKind.SERVICE_DOC_REFERENCE_TOKEN;
            case "VARIABLE_DOC_REFERENCE_TOKEN" -> SyntaxKind.VARIABLE_DOC_REFERENCE_TOKEN;
            case "VAR_DOC_REFERENCE_TOKEN" -> SyntaxKind.VAR_DOC_REFERENCE_TOKEN;
            case "ANNOTATION_DOC_REFERENCE_TOKEN" -> SyntaxKind.ANNOTATION_DOC_REFERENCE_TOKEN;
            case "MODULE_DOC_REFERENCE_TOKEN" -> SyntaxKind.MODULE_DOC_REFERENCE_TOKEN;
            case "FUNCTION_DOC_REFERENCE_TOKEN" -> SyntaxKind.FUNCTION_DOC_REFERENCE_TOKEN;
            case "PARAMETER_DOC_REFERENCE_TOKEN" -> SyntaxKind.PARAMETER_DOC_REFERENCE_TOKEN;
            case "CONST_DOC_REFERENCE_TOKEN" -> SyntaxKind.CONST_DOC_REFERENCE_TOKEN;

            // Documentation
            case "MARKDOWN_DOCUMENTATION" -> SyntaxKind.MARKDOWN_DOCUMENTATION;
            case "MARKDOWN_DOCUMENTATION_LINE" -> SyntaxKind.MARKDOWN_DOCUMENTATION_LINE;
            case "MARKDOWN_REFERENCE_DOCUMENTATION_LINE" -> SyntaxKind.MARKDOWN_REFERENCE_DOCUMENTATION_LINE;
            case "MARKDOWN_PARAMETER_DOCUMENTATION_LINE" -> SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE;
            case "MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE" ->
                    SyntaxKind.MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE;
            case "MARKDOWN_DEPRECATION_DOCUMENTATION_LINE" -> SyntaxKind.MARKDOWN_DEPRECATION_DOCUMENTATION_LINE;
            case "MARKDOWN_CODE_LINE" -> SyntaxKind.MARKDOWN_CODE_LINE;
            case "DOCUMENTATION_DESCRIPTION" -> SyntaxKind.DOCUMENTATION_DESCRIPTION;
            case "BALLERINA_NAME_REFERENCE" -> SyntaxKind.BALLERINA_NAME_REFERENCE;
            case "PARAMETER_NAME" -> SyntaxKind.PARAMETER_NAME;
            case "DEPRECATION_LITERAL" -> SyntaxKind.DEPRECATION_LITERAL;
            case "DOCUMENTATION_STRING" -> SyntaxKind.DOCUMENTATION_STRING;
            case "CODE_CONTENT" -> SyntaxKind.CODE_CONTENT;
            case "INLINE_CODE_REFERENCE" -> SyntaxKind.INLINE_CODE_REFERENCE;
            case "MARKDOWN_CODE_BLOCK" -> SyntaxKind.MARKDOWN_CODE_BLOCK;

            // Unsupported
            default -> throw new UnsupportedOperationException("cannot find syntax kind: " + kind);
        };
    }

    public static void assertCommentNode(Node node, List<String> comments) {
        Assert.assertTrue(node instanceof CommentNode);
        CommentNode commentNode = (CommentNode) node;
        List<String> commentLines = commentNode.getCommentLines();
        Assert.assertEquals(commentLines.size(), comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Assert.assertEquals(commentLines.get(i), comments.get(i));
        }
    }
}
