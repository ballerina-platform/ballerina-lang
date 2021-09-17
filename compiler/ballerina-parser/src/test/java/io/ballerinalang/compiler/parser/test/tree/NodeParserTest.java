/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Contains test cases to test the {@code NodeParser} API.
 *
 * @since 2.0.0
 */
public class NodeParserTest {

    @Test
    public void testParseStatements() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3;");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());

        statementNodes = NodeParser.parseStatements("int a = 3; int b = 4; a = 11;");
        Assert.assertEquals(statementNodes.size(), 3);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(1).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
        Assert.assertFalse(statementNodes.get(2).hasDiagnostics());

        String stmts = "int[] nums = [1, 2, 3, 4];\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 where i % 2 == 0\n" +
                "                 select i;\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 select i * 10;\n";
        statementNodes = NodeParser.parseStatements(stmts);
        Assert.assertEquals(statementNodes.size(), 3);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(1).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(2).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(2).hasDiagnostics());
    }

    @Test
    public void testParseStatementsWithDiagnostics1() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertTrue(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(0).toString(), "int a = 3 MISSING[;]");
    }

    @Test
    public void testParseStatementsWithDiagnostics2() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3;;");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertTrue(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(0).toString(), "int a = 3; INVALID[;]");
    }

    @Test
    public void testParseStatementsWithDiagnostics3() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3 b = 4 c = 2;");
        Assert.assertEquals(statementNodes.size(), 3);

        Assert.assertTrue(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(0).toString(), "int a = 3  MISSING[;]");

        Assert.assertTrue(statementNodes.get(1).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
        Assert.assertEquals(statementNodes.get(1).toString(), "b = 4  MISSING[;]");

        Assert.assertFalse(statementNodes.get(2).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
    }

    @Test
    public void testParseStatementsWithDiagnostics4() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("");
        Assert.assertEquals(statementNodes.size(), 1);

        StatementNode stmt = statementNodes.get(0);
        Assert.assertEquals(stmt.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmt.hasDiagnostics());
        Assert.assertEquals(stmt.toString(), " MISSING[] MISSING[] MISSING[;]");
    }

    @Test
    public void testParseStatementsWithDiagnostics5() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("const pi = 3.142");
        Assert.assertEquals(statementNodes.size(), 1);

        StatementNode stmt = statementNodes.get(0);
        Assert.assertEquals(stmt.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmt.hasDiagnostics());

        // test the virtually created local-var-decl
        VariableDeclarationNode variableDecl = (VariableDeclarationNode) stmt;
        Assert.assertTrue(variableDecl.annotations().isEmpty());
        Assert.assertFalse(variableDecl.finalKeyword().isPresent());
        Assert.assertEquals(variableDecl.typedBindingPattern().toString(), " MISSING[] MISSING[]");
        Assert.assertFalse(variableDecl.equalsToken().isPresent());
        Assert.assertFalse(variableDecl.initializer().isPresent());
        Assert.assertTrue(variableDecl.semicolonToken().isMissing());

        // test if the invalid stmt is included in the trailing minutiae
        Assert.assertEquals(variableDecl.semicolonToken().trailingMinutiae().toString(),
                " INVALID[const]  INVALID[pi]  INVALID[=]  INVALID[3.142]");
    }

    @Test
    public void testParseExpression() {
        ExpressionNode expressionNode = NodeParser.parseExpression("10 + 3");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.BINARY_EXPRESSION);
        Assert.assertFalse(expressionNode.hasDiagnostics());

        expressionNode = NodeParser.parseExpression("[1, 2, 3]");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.LIST_CONSTRUCTOR);
        Assert.assertFalse(expressionNode.hasDiagnostics());

        expressionNode = NodeParser.parseExpression("foo.toString()");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.METHOD_CALL);
        Assert.assertFalse(expressionNode.hasDiagnostics());

        expressionNode = NodeParser.parseExpression("foo(5, a, age = 18, ...subjects)");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.FUNCTION_CALL);
        Assert.assertFalse(expressionNode.hasDiagnostics());

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
        expressionNode = NodeParser.parseExpression(mappingConstructor);
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.MAPPING_CONSTRUCTOR);
        Assert.assertFalse(expressionNode.hasDiagnostics());
    }

    @Test
    public void testParseExpressionWithDiagnostics1() {
        ExpressionNode expressionNode = NodeParser.parseExpression("int num = 3;");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.INT_TYPE_DESC);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        List<Token> tokens = expressionNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 4);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(expressionNode.toString(), "int  INVALID[num]  INVALID[=]  INVALID[3] INVALID[;]");
    }

    @Test
    public void testParseExpressionWithDiagnostics2() {
        ExpressionNode expressionNode = NodeParser.parseExpression("% isolated 3 is int");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.TYPE_TEST_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        List<Token> tokens = expressionNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.ISOLATED_KEYWORD);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), " INVALID[%]  INVALID[isolated] 3 is int");
    }

    @Test
    public void testParseExpressionWithDiagnostics3() {
        ExpressionNode expressionNode = NodeParser.parseExpression("3 2");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.BINARY_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), "3  MISSING[+]2");
    }

    @Test
    public void testParseExpressionWithDiagnostics4() {
        ExpressionNode expressionNode = NodeParser.parseExpression("foo ? ");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.CONDITIONAL_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), "foo ?  MISSING[] MISSING[:] MISSING[]");
    }

    @Test
    public void testParseExpressionWithDiagnostics5() {
        ExpressionNode expressionNode = NodeParser.parseExpression("");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.SIMPLE_NAME_REFERENCE);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), " MISSING[]");
    }
}
