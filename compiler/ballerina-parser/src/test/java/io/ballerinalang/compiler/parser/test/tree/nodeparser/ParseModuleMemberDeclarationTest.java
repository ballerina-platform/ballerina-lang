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

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseModuleMemberDeclaration} method.
 *
 * @since 2.0.0
 */
public class ParseModuleMemberDeclarationTest {

    // Valid syntax tests

    @Test
    public void testTypeDefn() {
        String funcDef = "type Ints 0|1|2|3;";
        ModuleMemberDeclarationNode moduleMemDeclNode = NodeParser.parseModuleMemberDeclaration(funcDef);
        Assert.assertEquals(moduleMemDeclNode.kind(), SyntaxKind.TYPE_DEFINITION);
        Assert.assertFalse(moduleMemDeclNode.hasDiagnostics());
    }

    @Test
    public void testFuncDefn() {
        String funcDef = "function foo() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        ModuleMemberDeclarationNode moduleMemDeclNode = NodeParser.parseModuleMemberDeclaration(funcDef);
        Assert.assertEquals(moduleMemDeclNode.kind(), SyntaxKind.FUNCTION_DEFINITION);
        Assert.assertFalse(moduleMemDeclNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        ModuleMemberDeclarationNode moduleMemDeclNode = NodeParser.parseModuleMemberDeclaration("");
        Assert.assertEquals(moduleMemDeclNode.kind(), SyntaxKind.MODULE_VAR_DECL);
        Assert.assertTrue(moduleMemDeclNode.hasDiagnostics());

        Assert.assertEquals(moduleMemDeclNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(moduleMemDeclNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(moduleMemDeclNode.toString(), " MISSING[] MISSING[] MISSING[;]");
    }

    @Test
    public void testWithModuleMemberRecovery() {
        ModuleMemberDeclarationNode moduleMemDeclNode = NodeParser.parseModuleMemberDeclaration("% type  0|1|2|3;;");
        Assert.assertEquals(moduleMemDeclNode.kind(), SyntaxKind.TYPE_DEFINITION);
        Assert.assertTrue(moduleMemDeclNode.hasDiagnostics());

        List<Token> tokens = moduleMemDeclNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);

        tokens = moduleMemDeclNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);

        TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) moduleMemDeclNode;
        Assert.assertTrue(typeDefinitionNode.typeName().isMissing());

        Assert.assertEquals(moduleMemDeclNode.toString(), " INVALID[%] type   MISSING[]0|1|2|3; INVALID[;]");
    }
}
