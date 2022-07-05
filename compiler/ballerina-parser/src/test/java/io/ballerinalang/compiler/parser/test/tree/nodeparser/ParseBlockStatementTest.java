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

import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseBlockStatement} method.
 *
 * @since 2.0.0
 */
public class ParseBlockStatementTest {

    // Valid syntax tests

    @Test
    public void testEmptyBlockStmt() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("{}");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertFalse(blockStmtNode.hasDiagnostics());
    }

    @Test
    public void testSingleStmt() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("{ int a = 3; }");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertFalse(blockStmtNode.hasDiagnostics());

        NodeList<StatementNode> statements = blockStmtNode.statements();
        Assert.assertEquals(statements.size(), 1);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    @Test
    public void testMultipleStmtsInLine() {
        BlockStatementNode blockStmtNode =
                NodeParser.parseBlockStatement("{ int a = 3; int b = 4; a = 11; }");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertFalse(blockStmtNode.hasDiagnostics());

        NodeList<StatementNode> statements = blockStmtNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
    }

    @Test
    public void testMultipleStmtsWithNewlines() {
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

        NodeList<StatementNode> statements = blockStmtNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertTrue(blockStmtNode.hasDiagnostics());

        Assert.assertEquals(blockStmtNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(blockStmtNode.trailingInvalidTokens().size(), 0);

        Assert.assertTrue(blockStmtNode.openBraceToken().isMissing());
        Assert.assertEquals(blockStmtNode.statements().size(), 0);
        Assert.assertTrue(blockStmtNode.closeBraceToken().isMissing());
    }

    @Test
    public void testMissingSemicolons() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("{ int a = 3 b = 4 c = 2 }");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertTrue(blockStmtNode.hasDiagnostics());

        NodeList<StatementNode> statements = blockStmtNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);

        Assert.assertEquals(blockStmtNode.toString(), "{ int a = 3  MISSING[;]b = 4  MISSING[;]c = 2  MISSING[;]}");
    }

    @Test
    public void testWithModuleConstDecl() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("{ const pi = 3.142 }");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertTrue(blockStmtNode.hasDiagnostics());

        Assert.assertEquals(blockStmtNode.statements().size(), 0);
        Assert.assertEquals(blockStmtNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(blockStmtNode.trailingInvalidTokens().size(), 0);

        Token closeBraceToken = blockStmtNode.closeBraceToken();
        List<Token> tokens = closeBraceToken.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 4);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.CONST_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
        Assert.assertEquals(closeBraceToken.trailingInvalidTokens().size(), 0);

        Assert.assertEquals(blockStmtNode.toString(),
                "{  INVALID[const]  INVALID[pi]  INVALID[=]  INVALID[3.142] }");
    }

    @Test
    public void testBlockStmtRecovery() {
        BlockStatementNode blockStmtNode = NodeParser.parseBlockStatement("%{ int a;; };;");
        Assert.assertEquals(blockStmtNode.kind(), SyntaxKind.BLOCK_STATEMENT);
        Assert.assertTrue(blockStmtNode.hasDiagnostics());

        NodeList<StatementNode> statements = blockStmtNode.statements();
        Assert.assertEquals(statements.size(), 1);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);

        List<Token> tokens = blockStmtNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);

        tokens = blockStmtNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.SEMICOLON_TOKEN);

        Token closeBraceToken = blockStmtNode.closeBraceToken();
        tokens = closeBraceToken.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);

        tokens = closeBraceToken.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.SEMICOLON_TOKEN);

        Assert.assertEquals(blockStmtNode.toString(), " INVALID[%]{ int a; INVALID[;] } INVALID[;] INVALID[;]");
    }
}
