/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseModulePart} method.
 */
public class ParseModulePartTest {

    // Valid syntax tests

    @Test
    public void testEmptyString() {
        ModulePartNode modulePartNode = NodeParser.parseModulePart("");
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertFalse(modulePartNode.hasDiagnostics());
    }

    @Test
    public void testImportDecl() {
        ModulePartNode modulePartNode = NodeParser.parseModulePart("import foobar/foo.bar.baz as qux;");
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertFalse(modulePartNode.hasDiagnostics());
    }

    @Test
    public void testListnerDecl() {
        String listnerDecl = "public listener int x = 1;";
        ModulePartNode modulePartNode = NodeParser.parseModulePart(listnerDecl);
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertFalse(modulePartNode.hasDiagnostics());
    }

    @Test
    public void testServiceDecl() {
        String serviceDecl = "isolated service on new udp:Listener(8080){\n" +
                "}";
        ModulePartNode modulePartNode = NodeParser.parseModulePart(serviceDecl);
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertFalse(modulePartNode.hasDiagnostics());
    }

    @Test
    public void testFuncDefn() {
        String funcDef = "function foo() {\n" +
                "}";
        ModulePartNode modulePartNode = NodeParser.parseModulePart(funcDef);
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertFalse(modulePartNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testInvalidImport() {
        String invalidImport = "int x = 1; " +
                "import y;";
        ModulePartNode modulePartNode = NodeParser.parseModulePart(invalidImport);
        Assert.assertEquals(modulePartNode.kind(), SyntaxKind.MODULE_PART);
        Assert.assertTrue(modulePartNode.hasDiagnostics());
        Assert.assertEquals(modulePartNode.toString(), "int x = 1;" +
                "  INVALID[import]  INVALID[y] INVALID[;]");
    }

}
