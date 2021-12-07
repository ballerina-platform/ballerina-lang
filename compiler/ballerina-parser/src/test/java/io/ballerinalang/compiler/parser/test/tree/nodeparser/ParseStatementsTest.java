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

import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseStatements} method.
 *
 * @since 2.0.0
 */
public class ParseStatementsTest {

    // Valid syntax tests

    @Test
    public void testSingleStmt() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3;");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());
    }

    @Test
    public void testMultipleStmtsInline() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3; int b = 4; a = 11;");
        Assert.assertEquals(statementNodes.size(), 3);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(1).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(2).kind(), SyntaxKind.ASSIGNMENT_STATEMENT);
        Assert.assertFalse(statementNodes.get(2).hasDiagnostics());
    }

    @Test
    public void testMultipleStmtsWithNewlines() {
        String stmts = "int[] nums = [1, 2, 3, 4];\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 where i % 2 == 0\n" +
                "                 select i;\n" +
                "int[] evenNums = from var i in nums\n" +
                "                 select i * 10;\n";
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements(stmts);
        Assert.assertEquals(statementNodes.size(), 3);
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(1).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(1).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(2).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertFalse(statementNodes.get(2).hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("");
        Assert.assertEquals(statementNodes.size(), 1);

        StatementNode stmt = statementNodes.get(0);
        Assert.assertEquals(stmt.kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertTrue(stmt.hasDiagnostics());
        Assert.assertEquals(stmt.toString(), " MISSING[] MISSING[] MISSING[;]");
    }

    @Test
    public void testMissingSemicolon() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertTrue(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(0).toString(), "int a = 3 MISSING[;]");
    }

    @Test
    public void testExtraSemicolon() {
        NodeList<StatementNode> statementNodes = NodeParser.parseStatements("int a = 3;;");
        Assert.assertEquals(statementNodes.size(), 1);
        Assert.assertTrue(statementNodes.get(0).hasDiagnostics());
        Assert.assertEquals(statementNodes.get(0).kind(), SyntaxKind.LOCAL_VAR_DECL);
        Assert.assertEquals(statementNodes.get(0).toString(), "int a = 3; INVALID[;]");
    }

    @Test
    public void testMissingSemicolons() {
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
    public void testModuleConstDecl() {
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
}
