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

import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseStatement} method.
 *
 * @since 2.0.0
 */
public class ParseStatementTest {

    // Valid syntax tests

    @Test
    public void testVariableDecl() {
        StatementNode stmtNode = NodeParser.parseStatement("int a = 3;");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(stmtNode.hasDiagnostics());
    }

    @Test
    public void testIfElseStmt() {
        String ifElseStmt = "if (a < b) {\n" +
                "    a:b(\"a < b\");\n" +
                "} else {\n" +
                "    a:b(\"a >= b\");\n" +
                "}";
        StatementNode stmtNode = NodeParser.parseStatement(ifElseStmt);
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.IF_ELSE_STATEMENT);
        Assert.assertFalse(stmtNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        StatementNode stmtNode = NodeParser.parseStatement("");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmtNode.hasDiagnostics());

        Assert.assertEquals(stmtNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(stmtNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(stmtNode.toString(), " MISSING[] MISSING[] MISSING[;]");
    }

    @Test
    public void testWithMultipleStmts() {
        StatementNode stmtNode = NodeParser.parseStatement("int a = 3; int b = 4;");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmtNode.hasDiagnostics());

        Assert.assertEquals(stmtNode.leadingInvalidTokens().size(), 0);
        List<Token> tokens = stmtNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 5);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.INT_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
        Assert.assertEquals(tokens.get(4).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(stmtNode.toString(),
                "int a = 3;  INVALID[int]  INVALID[b]  INVALID[=]  INVALID[4] INVALID[;]");
    }

    @Test
    public void testWithInvalidTokens() {
        StatementNode stmtNode = NodeParser.parseStatement("% isolated int a = 3; int c;;");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmtNode.hasDiagnostics());

        List<Token> tokens = stmtNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.ISOLATED_KEYWORD);

        tokens = stmtNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 4);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.INT_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(stmtNode.toString(),
                " INVALID[%]  INVALID[isolated] int a = 3;  INVALID[int]  INVALID[c] INVALID[;] INVALID[;]");
    }

    @Test
    public void testWithNamedWorker() {
        StatementNode stmtNode = NodeParser.parseStatement("worker w1 { }");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmtNode.hasDiagnostics());

        Assert.assertEquals(stmtNode.leadingInvalidTokens().size(), 0);
        List<Token> tokens = stmtNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.WORKER_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.OPEN_BRACE_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.CLOSE_BRACE_TOKEN);
        Assert.assertEquals(stmtNode.toString(),
                " MISSING[] MISSING[] MISSING[; INVALID[worker]  INVALID[w1]  INVALID[{]  INVALID[}]]");
    }

    @Test
    public void testWithModuleConstDecl() {
        StatementNode stmtNode = NodeParser.parseStatement("const pi = 3.142;");
        Assert.assertEquals(stmtNode.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmtNode.hasDiagnostics());

        Assert.assertEquals(stmtNode.leadingInvalidTokens().size(), 0);
        List<Token> tokens = stmtNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 5);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.CONST_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
        Assert.assertEquals(tokens.get(4).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(stmtNode.toString(),
                " MISSING[] MISSING[] MISSING[; INVALID[const]  INVALID[pi]  INVALID[=]  INVALID[3.142] INVALID[;]]");
    }
}
