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
package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code lineRange} for {@code NodeParser} API.
 *
 * @since 2.0.0
 */
public class NodeParserLineRangeTest {

    private void assertLineRange(Node node, SyntaxKind kind, int startLine, int startLineOffset, int endLine,
                                 int endLineOffset) {
        Assert.assertEquals(node.kind(), kind);
        Assert.assertFalse(node.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(startLine, startLineOffset);
        LinePosition expectedEndPos = LinePosition.from(endLine, endLineOffset);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(node.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseActionOrExpression() {
        String queryAction = "from var a in b\n" +
                "where (c > d)\n" +
                "do {\n" +
                "int x;\n" +
                "int y = 4;\n" +
                "}";

        ExpressionNode actionNode = NodeParser.parseActionOrExpression(queryAction);
        assertLineRange(actionNode, SyntaxKind.QUERY_ACTION, 0, 0, 5, 1);
    }

    @Test
    public void testParseBindingPattern() {
        String mappingBindingPatten = "{\n" +
                "    a: {\n" +
                "        x: a1, y: {\n" +
                "            m: a2, n: {\n" +
                "                p: a3, q: a4\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern(mappingBindingPatten);
        assertLineRange(bindingPatternNode, SyntaxKind.MAPPING_BINDING_PATTERN, 0, 0, 8, 1);
    }

    @Test
    public void testParseBlockStatement() {
        String blockStmt = "{\n" +
                "int[] nums = [1, 2, 3, 4];\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 where i % 2 == 0\n" +
                "                 select i;\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 select i * 10;\n" +
                "}";

        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement(blockStmt);
        assertLineRange(blockStmtNode, SyntaxKind.BLOCK_STATEMENT, 0, 0, 7, 1);
        assertLineRange(blockStmtNode.statements().get(2), SyntaxKind.LOCAL_VAR_DECL, 5, 0, 6, 31);
    }

    @Test
    public void testParseExpression() {
        String mappingConstructor = "{    age:20, \n" +
                "  ...marks1,\n" +
                "  \"name\":\"John\",\n" +
                "  parent: { age:50,\n" +
                "            ...marks2,\n" +
                "            \"name\":\"Jane\",\n" +
                "            address2,\n" +
                "            [expr2]:\"value2\"\n" +
                "           },\n" +
                "  address,\n" +
                "  [expr1]: \"value1\"\n" +
                "}";

        ExpressionNode expressionNode = NodeParser.parseExpression(mappingConstructor);
        assertLineRange(expressionNode, SyntaxKind.MAPPING_CONSTRUCTOR, 0, 0, 11, 1);
    }

    @Test
    public void testParseFunctionBodyBlock() {
        String funcBodyBlock = "{\n" +
                "int[] nums = [1, 2, 3, 4];\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 where i % 2 == 0\n" +
                "                 select i;\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 select i * 10;\n" +
                "}";

        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock(funcBodyBlock);
        assertLineRange(funcBodyBlockNode, SyntaxKind.FUNCTION_BODY_BLOCK, 0, 0, 7, 1);
    }

    @Test
    public void testParseImportDeclaration() {
        String importDecl = "\n\nimport foobar/foo.bar.baz as qux;\n";

        ImportDeclarationNode importDeclarationNode = NodeParser.parseImportDeclaration(importDecl);
        assertLineRange(importDeclarationNode, SyntaxKind.IMPORT_DECLARATION, 2, 0, 2, 33);
    }

    @Test
    public void testParseModuleMemberDeclaration() {
        String funcDef = "function foo() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";

        ModuleMemberDeclarationNode funcBodyBlockNode = NodeParser.parseModuleMemberDeclaration(funcDef);
        assertLineRange(funcBodyBlockNode, SyntaxKind.FUNCTION_DEFINITION, 0, 0, 3, 1);
    }

    @Test
    public void testParseStatement() {
        String ifElseStmt = "if (a < b) {\n" +
                "    a:b(\"a < b\");\n" +
                "} else {\n" +
                "    a:b(\"a >= b\");\n" +
                "}";

        StatementNode statementNode = NodeParser.parseStatement(ifElseStmt);
        assertLineRange(statementNode, SyntaxKind.IF_ELSE_STATEMENT, 0, 0, 4, 1);
    }

    @Test
    public void testParseTypeDescriptor() {
        String recordTypeDesc = "record {|\n" +
                "    string name;\n" +
                "    boolean married;\n" +
                "|}\n";

        TypeDescriptorNode recordTypeDescriptor = NodeParser.parseTypeDescriptor(recordTypeDesc);
        assertLineRange(recordTypeDescriptor, SyntaxKind.RECORD_TYPE_DESC, 0, 0, 3, 2);
    }

    @Test
    public void testParseIntermediateFromClause() {
        String intermediateClauseText = "from int i in [1, 2, 3]";
        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        assertLineRange(intermediateClause, SyntaxKind.FROM_CLAUSE, 0, 0, 0, 23);
    }

    @Test
    public void testParseIntermediateWhereClause() {
        String intermediateClauseText = "where i == 1";
        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        assertLineRange(intermediateClause, SyntaxKind.WHERE_CLAUSE, 0, 0, 0, 12);
    }

    @Test
    public void testParseIntermediateLetClause() {
        String intermediateClauseText = "let int a = 3, string b = \"\"";
        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        assertLineRange(intermediateClause, SyntaxKind.LET_CLAUSE, 0, 0, 0, 28);
    }

    @Test
    public void testParseIntermediateJoinClause() {
        String intermediateClauseText = "join var user in table [{id: 1234, " +
                "name: \"Keith\"}, {id: 6789, name: \"Anne\"}] on login.userId equals user.id";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        assertLineRange(intermediateClause, SyntaxKind.JOIN_CLAUSE, 0, 0, 0, 107);
    }

    @Test
    public void testParseIntermediateOrderClause() {
        String intermediateClauseText = "order by name";
        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        assertLineRange(intermediateClause, SyntaxKind.ORDER_BY_CLAUSE, 0, 0, 0, 13);
    }

    @Test
    public void testParseIntermediateLimitClause() {
        String intermediateClauseText = "limit getIntValue()";
        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        assertLineRange(intermediateClause, SyntaxKind.LIMIT_CLAUSE, 0, 0, 0, 19);
    }

    @Test
    public void testParseLetVarDeclaration() {
        String letVarDeclTxt = "int a = 5";
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, false);
        assertLineRange(letVarDecl, SyntaxKind.LET_VAR_DECL, 0, 0, 0, 9);

        letVarDeclTxt = "@a {m: 5} @b T b = getValue()";
        letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, false);
        assertLineRange(letVarDecl, SyntaxKind.LET_VAR_DECL, 0, 0, 0, 29);

        letVarDeclTxt = "@a {m: 5} @b var b = c->getValue()";
        letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, true);
        assertLineRange(letVarDecl, SyntaxKind.LET_VAR_DECL, 0, 0, 0, 34);
    }

    @Test
    public void testParseAnnotation() {
        String annotationText = "\n    @foo:bar {qux: 5}";
        AnnotationNode annotation = NodeParser.parseAnnotation(annotationText);
        assertLineRange(annotation, SyntaxKind.ANNOTATION, 1, 4, 1, 21);
    }

    @Test
    public void testParseMarkdownAnnotation() {
        String markdownDocText = "\n\n# This is the description\n" +
                "#\n" +
                "# + value - value input parameter\n" +
                "# + return - return a integer value\n\n\n";

        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation(markdownDocText);
        assertLineRange(markdownDoc, SyntaxKind.MARKDOWN_DOCUMENTATION, 2, 0, 5, 35);
    }
}
