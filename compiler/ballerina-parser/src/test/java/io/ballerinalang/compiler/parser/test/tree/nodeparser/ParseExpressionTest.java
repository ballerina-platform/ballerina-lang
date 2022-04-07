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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseExpression} method.
 *
 * @since 2.0.0
 */
public class ParseExpressionTest {

    // Valid syntax tests

    @Test
    public void testBinaryExpr() {
        ExpressionNode expressionNode = NodeParser.parseExpression("10 + 3");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.BINARY_EXPRESSION);
        Assert.assertFalse(expressionNode.hasDiagnostics());
    }

    @Test
    public void testListConstructorExpr() {
        ExpressionNode expressionNode = NodeParser.parseExpression("[1, 2, 3]");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.LIST_CONSTRUCTOR);
        Assert.assertFalse(expressionNode.hasDiagnostics());
    }

    @Test
    public void testMethodCallExpr() {
        ExpressionNode expressionNode = NodeParser.parseExpression("foo.toString()");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.METHOD_CALL);
        Assert.assertFalse(expressionNode.hasDiagnostics());
    }

    @Test
    public void testFuncCallExpr() {
        ExpressionNode expressionNode = NodeParser.parseExpression("foo(5, a, age = 18, ...subjects)");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.FUNCTION_CALL);
        Assert.assertFalse(expressionNode.hasDiagnostics());
    }

    @Test
    public void testMappingConstructorExpr() {
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
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        ExpressionNode expressionNode = NodeParser.parseExpression("");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.SIMPLE_NAME_REFERENCE);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertTrue(((SimpleNameReferenceNode) expressionNode).name().isMissing());
    }

    @Test
    public void testWithAStmt() {
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
    public void testWithInvalidTokens() {
        ExpressionNode expressionNode = NodeParser.parseExpression("% isolated 3 is int;");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.TYPE_TEST_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        List<Token> tokens = expressionNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.ISOLATED_KEYWORD);

        tokens = expressionNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(expressionNode.toString(), " INVALID[%]  INVALID[isolated] 3 is int INVALID[;]");
    }

    @Test
    public void testWithExprRecovery1() {
        ExpressionNode expressionNode = NodeParser.parseExpression("3 2");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.BINARY_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), "3  MISSING[+]2");
    }

    @Test
    public void testWithExprRecovery2() {
        ExpressionNode expressionNode = NodeParser.parseExpression("foo ? ");
        Assert.assertEquals(expressionNode.kind(), SyntaxKind.CONDITIONAL_EXPRESSION);
        Assert.assertTrue(expressionNode.hasDiagnostics());

        Assert.assertEquals(expressionNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(expressionNode.toString(), "foo ?  MISSING[] MISSING[:] MISSING[]");
    }
}
