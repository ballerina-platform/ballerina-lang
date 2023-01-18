/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseLetVarDeclaration} method.
 *
 * @since 2201.4.0
 */
public class ParseLetVarDeclarationTest {

    // Valid syntax tests

    @Test
    public void testBasicLetVarDeclaration() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "boolean flag = getFlag()", false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());
    }

    @Test
    public void testLetVarDeclarationWithAnnotation() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "@myAnnot @b {priority: 1} T a = 4", false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());
    }

    @Test
    public void testLetVarDeclarationAllowingActions() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "@myAnnot @b {priority: 1} T a = start foo()", true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertFalse(letVarDecl.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testAdditionalTokens() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "let boolean flag = getFlag(), int", false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());
        
        List<Token> tokens = letVarDecl.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.LET_KEYWORD);

        tokens = letVarDecl.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.COMMA_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.INT_KEYWORD);
    }

    @Test
    public void testAllowActionsFalse() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "boolean flag = wait f", false);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());

        Token missingVarRef = ((SimpleNameReferenceNode) letVarDecl.expression()).name();
        Assert.assertTrue(missingVarRef.isMissing());
        List<Token> tokens = missingVarRef.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 2);

        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.WAIT_KEYWORD);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.IDENTIFIER_TOKEN);
    }

    @Test
    public void testMissingRhs() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration("var b =", true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());

        Assert.assertTrue(((SimpleNameReferenceNode) letVarDecl.expression()).name().isMissing());
    }

    @Test
    public void testMissingEqualToken() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "boolean flag getFlag()", true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());
        
        Assert.assertTrue(letVarDecl.equalsToken().isMissing());
    }

    @Test
    public void testMultipleLetVarDecl() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration(
                "@a var b = (), int c = 5", true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());

        List<Token> tokens = letVarDecl.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 5);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.COMMA_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.INT_KEYWORD);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.IDENTIFIER_TOKEN);
        Assert.assertEquals(tokens.get(3).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(4).kind(), SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
    }

    @Test
    public void testEmptyString() {
        LetVariableDeclarationNode letVarDecl = NodeParser.parseLetVarDeclaration("", true);
        Assert.assertEquals(letVarDecl.kind(), SyntaxKind.LET_VAR_DECL);
        Assert.assertTrue(letVarDecl.hasDiagnostics());

        Assert.assertTrue(
                ((SimpleNameReferenceNode) letVarDecl.typedBindingPattern().typeDescriptor()).name().isMissing());
        Assert.assertTrue(letVarDecl.equalsToken().isMissing());
        Assert.assertTrue(((SimpleNameReferenceNode) letVarDecl.expression()).name().isMissing());
    }
}
