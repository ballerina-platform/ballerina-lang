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

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
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

    @Test
    public void testParseActionOrExpression() {
        String queryAction = "from var a in b\n" +
                "where (c > d)\n" +
                "do {\n" +
                "int x;\n" +
                "int y = 4;\n" +
                "}";

        ExpressionNode actionNode = NodeParser.parseActionOrExpression(queryAction);
        Assert.assertEquals(actionNode.kind(), SyntaxKind.QUERY_ACTION);
        Assert.assertFalse(actionNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(5, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(actionNode.lineRange(), expectedLineRange);
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
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.MAPPING_BINDING_PATTERN);
        Assert.assertFalse(bindingPatternNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(8, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(bindingPatternNode.lineRange(), expectedLineRange);
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
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertFalse(blockStmtNode.hasDiagnostics());

        LinePosition expectedStartPos1 = LinePosition.from(0, 0);
        LinePosition expectedEndPos1 = LinePosition.from(7, 1);
        LineRange expectedLineRange1 = LineRange.from(null, expectedStartPos1, expectedEndPos1);
        Assert.assertEquals(blockStmtNode.lineRange(), expectedLineRange1);

        LinePosition expectedStartPos2 = LinePosition.from(5, 0);
        LinePosition expectedEndPos2 = LinePosition.from(6, 31);
        LineRange expectedLineRange2 = LineRange.from(null, expectedStartPos2, expectedEndPos2);
        Assert.assertEquals(blockStmtNode.statements().get(2).lineRange(), expectedLineRange2);
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
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.MAPPING_CONSTRUCTOR);
        Assert.assertFalse(expressionNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(11, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(expressionNode.lineRange(), expectedLineRange);
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
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(7, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(funcBodyBlockNode.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseImportDeclaration() {
        String importDecl = "\n\nimport foobar/foo.bar.baz as qux;\n";

        ImportDeclarationNode importDeclarationNode = NodeParser.parseImportDeclaration(importDecl);
        Assert.assertEquals(importDeclarationNode.kind(), SyntaxKind.IMPORT_DECLARATION);
        Assert.assertFalse(importDeclarationNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(2, 0);
        LinePosition expectedEndPos = LinePosition.from(2, 33);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(importDeclarationNode.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseModuleMemberDeclaration() {
        String funcDef = "function foo() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";

        ModuleMemberDeclarationNode funcBodyBlockNode = NodeParser.parseModuleMemberDeclaration(funcDef);
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_DEFINITION);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(3, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(funcBodyBlockNode.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseStatement() {
        String ifElseStmt = "if (a < b) {\n" +
                "    a:b(\"a < b\");\n" +
                "} else {\n" +
                "    a:b(\"a >= b\");\n" +
                "}";

        StatementNode statementNode = NodeParser.parseStatement(ifElseStmt);
        Assert.assertEquals(statementNode.kind(), SyntaxKind.IF_ELSE_STATEMENT);
        Assert.assertFalse(statementNode.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(4, 1);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(statementNode.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseTypeDescriptor() {
        String recordTypeDesc = "record {|\n" +
                "    string name;\n" +
                "    boolean married;\n" +
                "|}\n";

        TypeDescriptorNode recordTypeDescriptor = NodeParser.parseTypeDescriptor(recordTypeDesc);
        Assert.assertEquals(recordTypeDescriptor.kind(), SyntaxKind.RECORD_TYPE_DESC);
        Assert.assertFalse(recordTypeDescriptor.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(3, 2);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(recordTypeDescriptor.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateFromClause() {
        String intermediateClauseText = "from int i in [1, 2, 3]";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 23);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateWhereClause() {
        String intermediateClauseText = "where i == 1";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 12);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateLetClause() {
        String intermediateClauseText = "let int a = 3, string b = \"\"";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 28);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateJoinClause() {
        String intermediateClauseText = "join var user in table [{id: 1234, " +
                "name: \"Keith\"}, {id: 6789, name: \"Anne\"}] on login.userId equals user.id";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 107);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateOrderClause() {
        String intermediateClauseText = "order by name";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, true);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 13);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseIntermediateLimitClause() {
        String intermediateClauseText = "limit getIntValue()";

        IntermediateClauseNode intermediateClause = NodeParser.parseIntermediateClause(intermediateClauseText, false);
        Assert.assertEquals(intermediateClause.kind(), SyntaxKind.LIMIT_CLAUSE);
        Assert.assertFalse(intermediateClause.hasDiagnostics());

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 19);
        LineRange expectedLineRange = LineRange.from(null, expectedStartPos, expectedEndPos);
        Assert.assertEquals(intermediateClause.lineRange(), expectedLineRange);
    }

    @Test
    public void testParseLetVarDeclaration() {
        String letVarDeclTxt = "int a = 5";

        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());

        LineRange expectedLineRange = LineRange.from(
                null, LinePosition.from(0, 0), LinePosition.from(0, 9));
        Assert.assertEquals(letVarDecl.lineRange(), expectedLineRange);

        letVarDeclTxt = "@a {m: 5} @b T b = getValue()";

        letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());
        
        expectedLineRange = LineRange.from(null, LinePosition.from(0, 0), LinePosition.from(0, 29));
        Assert.assertEquals(letVarDecl.lineRange(), expectedLineRange);

        letVarDeclTxt = "@a {m: 5} @b var b = c->getValue()";

        letVarDecl = NodeParser.parseLetVarDeclaration(letVarDeclTxt, true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());

        expectedLineRange = LineRange.from(null, LinePosition.from(0, 0), LinePosition.from(0, 34));
        Assert.assertEquals(letVarDecl.lineRange(), expectedLineRange);
    }
}
