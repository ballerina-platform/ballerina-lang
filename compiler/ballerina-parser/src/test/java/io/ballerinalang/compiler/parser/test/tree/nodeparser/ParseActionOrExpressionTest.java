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
 * Test {@code parseActionOrExpression} method.
 *
 * @since 2.0.0
 */
public class ParseActionOrExpressionTest {

    // Valid syntax tests

    @Test
    public void testSendAction() {
        ExpressionNode actionNode = NodeParser.parseActionOrExpression("abc->method(arg1)");
        Assert.assertEquals(actionNode.kind(), SyntaxKind.REMOTE_METHOD_CALL_ACTION);
        Assert.assertFalse(actionNode.hasDiagnostics());
    }

    @Test
    public void testQueryAction() {
        String queryAction = "from var a in b\n" +
                "where (c > d)\n" +
                "do {\n" +
                "int x;\n" +
                "int y = 4;\n" +
                "}";
        ExpressionNode actionNode = NodeParser.parseActionOrExpression(queryAction);
        Assert.assertEquals(actionNode.kind(), SyntaxKind.QUERY_ACTION);
        Assert.assertFalse(actionNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        ExpressionNode actionOrExprNode = NodeParser.parseActionOrExpression("");
        Assert.assertEquals(actionOrExprNode.kind(), SyntaxKind.SIMPLE_NAME_REFERENCE);
        Assert.assertTrue(actionOrExprNode.hasDiagnostics());

        Assert.assertEquals(actionOrExprNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(actionOrExprNode.trailingInvalidTokens().size(), 0);
        Assert.assertTrue(((SimpleNameReferenceNode) actionOrExprNode).name().isMissing());
    }

    @Test
    public void testWithAnActionStmt() {
        ExpressionNode actionOrExprNode = NodeParser.parseActionOrExpression("abc->method(arg1);");
        Assert.assertEquals(actionOrExprNode.kind(), SyntaxKind.REMOTE_METHOD_CALL_ACTION);
        Assert.assertTrue(actionOrExprNode.hasDiagnostics());

        Assert.assertEquals(actionOrExprNode.leadingInvalidTokens().size(), 0);
        List<Token> tokens = actionOrExprNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(actionOrExprNode.toString(), "abc->method(arg1) INVALID[;]");
    }

    @Test
    public void testWithInvalidTokens() {
        ExpressionNode actionOrExprNode = NodeParser.parseActionOrExpression("% isolated abc->method(arg1);");
        Assert.assertEquals(actionOrExprNode.kind(), SyntaxKind.REMOTE_METHOD_CALL_ACTION);
        Assert.assertTrue(actionOrExprNode.hasDiagnostics());

        List<Token> tokens = actionOrExprNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.ISOLATED_KEYWORD);

        tokens = actionOrExprNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(actionOrExprNode.toString(), " INVALID[%]  INVALID[isolated] abc->method(arg1)");
    }

    @Test
    public void testWithExprRecovery() {
        ExpressionNode actionOrExprNode = NodeParser.parseActionOrExpression("abc -> ");
        Assert.assertEquals(actionOrExprNode.kind(), SyntaxKind.ASYNC_SEND_ACTION);
        Assert.assertTrue(actionOrExprNode.hasDiagnostics());

        Assert.assertEquals(actionOrExprNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(actionOrExprNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(actionOrExprNode.toString(), "abc ->  MISSING[]");
    }
}
