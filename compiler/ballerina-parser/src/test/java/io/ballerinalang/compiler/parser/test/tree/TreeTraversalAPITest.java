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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.InvalidTokenMinutiaeNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextLine;
import io.ballerina.tools.text.TextRange;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Contains methods to test external syntax tree traversal API.
 *
 * @since 1.3.0
 */
public class TreeTraversalAPITest extends AbstractSyntaxTreeAPITest {

    @Test(enabled = false)
    public void testFindTokenFromPosition() {
        String sourceFilePath = "find_token_test_1.bal";
        String sourceText = getFileContentAsString(sourceFilePath);
        SyntaxTree syntaxTree = parseFile(sourceFilePath);
        ModulePartNode modulePart = syntaxTree.rootNode();

        // Get the expected lexemes from the TextDocument itself
        // You will get text lines with normalized newline characters
        TextDocument textDocument = TextDocuments.from(sourceText);
        TextLine textLine = textDocument.line(5);

        String expectedLexeme = textLine.text().substring(40, 41); // This should be the char 'g' in '....(f + g));'
        Token actualToken = modulePart.findToken(115);
        Assert.assertEquals(actualToken.toString(), expectedLexeme);

        expectedLexeme = textLine.text().substring(36, 38); // This should be the chars 'f ' in '....(f + g));'
        actualToken = modulePart.findToken(111);
        Assert.assertEquals(actualToken.toString(), expectedLexeme);
    }

    @Test
    public void testFindNodeFromPosition() {
        String sourceFilePath = "find_node_test_1.bal";
        SyntaxTree syntaxTree = parseFile(sourceFilePath);
        ModulePartNode modulePart = syntaxTree.rootNode();
        String sourceCode = modulePart.toSourceCode();
        NonTerminalNode varDecl = modulePart.findNode(TextRange.from(sourceCode.indexOf("123;") + "123;".length(), 0));
        NonTerminalNode blockStmtNode = modulePart.findNode(TextRange.from(sourceCode.indexOf("modName:TestType) {")
                + "modName:TestType) {".length(), 0));
        Assert.assertSame(varDecl.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(blockStmtNode.kind() == SyntaxKind.BLOCK_STATEMENT
                && blockStmtNode.parent().kind() == SyntaxKind.IF_ELSE_STATEMENT);
    }

    @Test(enabled = false) //disabled since it fails in windows due to token position
    public void testGetParentOfToken() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token token = modulePart.findToken(115);

        Node parent = token.parent().parent();
        Assert.assertEquals(parent.kind(), SyntaxKind.BINARY_EXPRESSION);

        parent = token.parent().parent().parent().parent().parent().parent().parent().parent();
        Assert.assertEquals(parent.kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    @Test
    public void testFindTokenWithEndTextPosition() {
        String text = "import ballerina/http;\n" +
                "import ballerina/lang.'object as lang;\n" +
                "\n" +
                "http:";
        int lastIndex = text.length();

        SyntaxTree syntaxTree = SyntaxTree.from(TextDocuments.from(text));
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        Token token = modulePartNode.findToken(lastIndex);

        Assert.assertEquals(text.length(), token.textRangeWithMinutiae().endOffset());
    }

    @Test
    public void testFindTokenInsideMinutiae() {
        String text = "import ballerina/http;\n" +
                "import ballerina/lang.'object as lang;\n" +
                "\n" +
                "@http:ServiceConfig";
        int position = text.indexOf(":ServiceConfig");

        SyntaxTree syntaxTree = SyntaxTree.from(TextDocuments.from(text));
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        Token token = modulePartNode.findToken(position, true);
        Assert.assertEquals(SyntaxKind.COLON_TOKEN, token.kind());
        Assert.assertEquals(SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE, token.parent().kind());

        InvalidTokenMinutiaeNode invalidTokenMinutiaeNode = (InvalidTokenMinutiaeNode) token.parent();
        Token attachedToken = invalidTokenMinutiaeNode.parentMinutiae().parentToken();
        Assert.assertEquals(SyntaxKind.EOF_TOKEN, attachedToken.kind());

        MinutiaeList leadingMinutiae = attachedToken.leadingMinutiae();
        Assert.assertEquals(5, leadingMinutiae.size());
        Assert.assertEquals(SyntaxKind.END_OF_LINE_MINUTIAE, leadingMinutiae.get(0).kind());
        Assert.assertEquals(SyntaxKind.AT_TOKEN,
                leadingMinutiae.get(1).invalidTokenMinutiaeNode().get().invalidToken().kind());
    }

    @Test
    public void testFindTokenInsideMinutiaeSimple() {
        String text = "import ballerina/http;\n" +
                "import ballerina/lang.'object as lang;\n" +
                "\n" +
                "public & function foo() returns int {" +
                "int a = 5;" +
                "return a;" +
                "}";
        int position = text.indexOf("&");

        SyntaxTree syntaxTree = SyntaxTree.from(TextDocuments.from(text));
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        Token token = modulePartNode.findToken(position, true);
        Assert.assertEquals(SyntaxKind.BITWISE_AND_TOKEN, token.kind());
        Assert.assertEquals(SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE, token.parent().kind());

        token = modulePartNode.findToken(position, false);
        Assert.assertEquals(SyntaxKind.FUNCTION_KEYWORD, token.kind());
        Assert.assertNotEquals(SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE, token.parent().kind());
    }

    @Test
    public void testFindTokenInsideMinutiaeNamedWorkerDecl() {
        String text = "function sar() {\n" +
                "    int a = 12;\n" +
                "    \n" +
                "    worker myWorker r  {\n" +
                "    }\n" +
                "}";
        int position = text.indexOf("r  {");

        SyntaxTree syntaxTree = SyntaxTree.from(TextDocuments.from(text));
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        Token token = modulePartNode.findToken(position, true);
        Assert.assertEquals(SyntaxKind.IDENTIFIER_TOKEN, token.kind());
        Assert.assertEquals(SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE, token.parent().kind());

        token = modulePartNode.findToken(position, false);
        Assert.assertEquals(SyntaxKind.OPEN_BRACE_TOKEN, token.kind());
        Assert.assertNotEquals(SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE, token.parent().kind());
    }

    @Test
    public void testGetSpecificAncestorOfToken() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token token = modulePart.findToken(115);

        //testing a positive case
        Optional<NonTerminalNode> ancestor = token.ancestor(node -> node instanceof FunctionDefinitionNode);
        Assert.assertEquals(ancestor.get().kind(), SyntaxKind.FUNCTION_DEFINITION);

        //testing a negative case
        ancestor = token.ancestor(node -> node instanceof ForEachStatementNode);
        Assert.assertFalse(ancestor.isPresent());
    }

    @Test(enabled = false) //disabled since it fails in windows due to token position
    public void testGetAllAncestorsOfToken() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token token = modulePart.findToken(115);

        List<NonTerminalNode> ancestors = token.ancestors();
        Assert.assertEquals(ancestors.size(), 11);
    }

    @Test
    public void testGetParentOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);

        Node funcDef = funcToken.parent();
        Assert.assertEquals(funcDef.kind(), SyntaxKind.FUNCTION_DEFINITION);
        Assert.assertEquals(funcDef.parent().kind(), SyntaxKind.MODULE_PART);
    }

    @Test
    public void testGetSpecificAncestorOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);

        //testing a positive case
        Optional<NonTerminalNode> ancestor = funcToken.ancestor(node -> node instanceof ModulePartNode);
        Assert.assertEquals(ancestor.get().kind(), SyntaxKind.MODULE_PART);

        //testing a negative case
        ancestor = funcToken.ancestor(node -> node instanceof ForEachStatementNode);
        Assert.assertFalse(ancestor.isPresent());
    }

    @Test
    public void testGetAllAncestorsOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);

        List<NonTerminalNode> ancestors = funcToken.ancestors();
        Assert.assertEquals(ancestors.size(), 2);
    }

    @Test
    public void testGenChildrenOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);
        NonTerminalNode funcDef = funcToken.parent();

        List<SyntaxKind> actualChildNodeKindList = new ArrayList<>();
        List<SyntaxKind> expectedChildNodeKindList = Arrays.asList(
                SyntaxKind.PUBLIC_KEYWORD,
                SyntaxKind.FUNCTION_KEYWORD,
                SyntaxKind.IDENTIFIER_TOKEN,
                SyntaxKind.FUNCTION_SIGNATURE,
                SyntaxKind.FUNCTION_BODY_BLOCK);

        for (Node child : funcDef.children()) {
            if (child.kind() == SyntaxKind.NONE) {
                continue;
            }
            actualChildNodeKindList.add(child.kind());
        }

        Assert.assertEquals(actualChildNodeKindList, expectedChildNodeKindList);
    }
}
