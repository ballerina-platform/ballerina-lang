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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

/**
 * Test {@code parseFunctionBodyBlock} method.
 *
 * @since 2.0.0
 */
public class ParseFunctionBodyBlock {

    // Valid syntax tests

    @Test
    public void testEmptyFuncBodyBlock() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("{}");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());
    }

    @Test
    public void testSingleStmt() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("{ int a = 3; }");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());

        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 1);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    @Test
    public void testMultipleStmtsInLine() {
        FunctionBodyBlockNode funcBodyBlockNode =
                NodeParser.parseFunctionBodyBlock("{ int a = 3; int b = 4; a = 11; }");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());

        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
    }

    @Test
    public void testMultipleStmtsWithNewlines() {
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

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());

        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.LOCAL_VAR_DECL);

    }

    @Test
    public void testNamedWorkerOnly() {
        String funcBodyBlock = "{\n" +
                "    @strand { thread:\"any\" }\n" +
                "    worker w1 {\n" +
                "        int sum = 0;\n" +
                "    }\n" +
                "}";
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock(funcBodyBlock);
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        Optional<NamedWorkerDeclarator> namedWorkerDecl = funcBodyBlockNode.namedWorkerDeclarator();
        Assert.assertTrue(namedWorkerDecl.isPresent());
        NamedWorkerDeclarator namedWorkerDeclarator = namedWorkerDecl.get();

        Assert.assertEquals(namedWorkerDeclarator.kind(), SyntaxKind.NAMED_WORKER_DECLARATOR);
        Assert.assertEquals(namedWorkerDeclarator.workerInitStatements().size(), 0);

        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclNodes = namedWorkerDeclarator.namedWorkerDeclarations();
        Assert.assertEquals(namedWorkerDeclNodes.size(), 1);
        Assert.assertEquals(namedWorkerDeclNodes.get(0).kind(), SyntaxKind.NAMED_WORKER_DECLARATION);
    }

    @Test
    public void testNamedWorkerAndStmts() {
        String funcBodyBlock = "{\n" +
                "    int x = 1;\n" +
                "    int y = 1;\n\n" +
                "    @strand { thread:\"any\" }\n" +
                "    worker w1 {\n" +
                "        x += x;\n" +
                "        x *= 2;\n" +
                "    }\n\n" +
                "    boolean z = x == y;\n" +
                "}";
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock(funcBodyBlock);
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertFalse(funcBodyBlockNode.hasDiagnostics());

        Optional<NamedWorkerDeclarator> optionalNamedWorkerDeclarator = funcBodyBlockNode.namedWorkerDeclarator();
        Assert.assertTrue(optionalNamedWorkerDeclarator.isPresent());

        NamedWorkerDeclarator namedWorkerDeclarator = optionalNamedWorkerDeclarator.get();
        Assert.assertEquals(namedWorkerDeclarator.kind(), SyntaxKind.NAMED_WORKER_DECLARATOR);


        NodeList<StatementNode> statementNodes = namedWorkerDeclarator.workerInitStatements();
        Assert.assertEquals(statementNodes.size(), 2);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);

        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclNodes = namedWorkerDeclarator.namedWorkerDeclarations();
        Assert.assertEquals(namedWorkerDeclNodes.size(), 1);
        Assert.assertEquals(namedWorkerDeclNodes.get(0).kind(), SyntaxKind.NAMED_WORKER_DECLARATION);

        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 1);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertTrue(funcBodyBlockNode.hasDiagnostics());

        Assert.assertEquals(funcBodyBlockNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(funcBodyBlockNode.trailingInvalidTokens().size(), 0);

        Assert.assertTrue(funcBodyBlockNode.openBraceToken().isMissing());
        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());
        Assert.assertEquals(funcBodyBlockNode.statements().size(), 0);
        Assert.assertTrue(funcBodyBlockNode.closeBraceToken().isMissing());
    }

    @Test
    public void testMissingSemicolons() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("{ int a = 3 b = 4 c = 2 }");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertTrue(funcBodyBlockNode.hasDiagnostics());

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());

        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 3);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statements.get(1).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
        Assert.assertEquals(statements.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);

        Assert.assertEquals(funcBodyBlockNode.toString(), "{ int a = 3  MISSING[;]b = 4  MISSING[;]c = 2  MISSING[;]}");
    }

    @Test
    public void testWithModuleConstDecl() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("{ const pi = 3.142 }");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertTrue(funcBodyBlockNode.hasDiagnostics());

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());
        Assert.assertEquals(funcBodyBlockNode.statements().size(), 0);
        Assert.assertEquals(funcBodyBlockNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(funcBodyBlockNode.trailingInvalidTokens().size(), 0);

        Token closeBraceToken = funcBodyBlockNode.closeBraceToken();
        List<Token> tokens = closeBraceToken.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 4);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.CONST_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
        Assert.assertEquals(closeBraceToken.trailingInvalidTokens().size(), 0);

        Assert.assertEquals(funcBodyBlockNode.toString(),
                "{  INVALID[const]  INVALID[pi]  INVALID[=]  INVALID[3.142] }");
    }

    @Test
    public void testFuncBodyBlockRecovery() {
        FunctionBodyBlockNode funcBodyBlockNode = NodeParser.parseFunctionBodyBlock("%{ int a;; };;");
        Assert.assertEquals(funcBodyBlockNode.kind(), SyntaxKind.FUNCTION_BODY_BLOCK);
        Assert.assertTrue(funcBodyBlockNode.hasDiagnostics());

        Assert.assertFalse(funcBodyBlockNode.namedWorkerDeclarator().isPresent());
        NodeList<StatementNode> statements = funcBodyBlockNode.statements();
        Assert.assertEquals(statements.size(), 1);
        Assert.assertEquals(statements.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);

        List<Token> tokens = funcBodyBlockNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);

        tokens = funcBodyBlockNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.SEMICOLON_TOKEN);

        Token closeBraceToken = funcBodyBlockNode.closeBraceToken();
        tokens = closeBraceToken.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);

        tokens = closeBraceToken.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.SEMICOLON_TOKEN);

        Assert.assertEquals(funcBodyBlockNode.toString(), " INVALID[%]{ int a; INVALID[;] } INVALID[;] INVALID[;]");
    }
}
