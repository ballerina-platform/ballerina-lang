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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseImportDeclaration} method.
 *
 * @since 2.0.0
 */
public class ParseImportDeclarationTest {

    // Valid syntax tests

    @Test
    public void testImportDecl() {
        ImportDeclarationNode importDeclNode = NodeParser.parseImportDeclaration("import foobar/foo.bar.baz as qux;");
        Assert.assertEquals(importDeclNode.kind(), SyntaxKind.IMPORT_DECLARATION);
        Assert.assertFalse(importDeclNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        ImportDeclarationNode importDeclarationNode = NodeParser.parseImportDeclaration("");
        Assert.assertEquals(importDeclarationNode.kind(), SyntaxKind.IMPORT_DECLARATION);
        Assert.assertTrue(importDeclarationNode.hasDiagnostics());

        Assert.assertEquals(importDeclarationNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(importDeclarationNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(importDeclarationNode.toString(), " MISSING[import] MISSING[] MISSING[;]");
    }

    @Test
    public void testWithImportDeclRecovery() {
        ImportDeclarationNode importDeclNode = NodeParser.parseImportDeclaration("%import foobar/foo.bar.baz qux;;");
        Assert.assertEquals(importDeclNode.kind(), SyntaxKind.IMPORT_DECLARATION);
        Assert.assertTrue(importDeclNode.hasDiagnostics());
        Assert.assertEquals(importDeclNode.toString(), " INVALID[%]import foobar/foo.bar.baz  MISSING[as]qux; INVALID[;]");
    }
}
