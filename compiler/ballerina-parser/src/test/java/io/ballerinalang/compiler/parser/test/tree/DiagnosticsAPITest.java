/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerinalang.compiler.diagnostics.Diagnostic;
import io.ballerinalang.compiler.diagnostics.DiagnosticInfo;
import io.ballerinalang.compiler.diagnostics.DiagnosticSeverity;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerinalang.compiler.internal.diagnostics.DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN;

/**
 * Contains cases to test the diagnostic API.
 *
 * @since 2.0.0
 */
public class DiagnosticsAPITest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testHasDiagnosticsMethod() {
        // This bal file has two missing semicolon error.
        // Therefore the diagnostic flag should get propagated to the root of the tree.
        // Some branches has this flag, but not all branches of three.
        // This method tests above mentioned points.
        SyntaxTree syntaxTree = parseFile("diagnostics_test_001.bal");
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        // Check the hasDiagnostics() method in the syntax tree
        Assert.assertTrue(syntaxTree.hasDiagnostics());
        // Check the hasDiagnostics() method in the syntax tree
        Assert.assertTrue(modulePartNode.hasDiagnostics());

        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();
        // First import should not have diagnostics
        Assert.assertFalse(imports.get(0).hasDiagnostics());
        // Second import should have diagnostics
        Assert.assertTrue(imports.get(1).hasDiagnostics());

        FunctionDefinitionNode funcDefWithDiagnostics = (FunctionDefinitionNode) modulePartNode.members().get(0);
        Assert.assertTrue(funcDefWithDiagnostics.hasDiagnostics());

        FunctionBodyBlockNode funcBody = (FunctionBodyBlockNode) funcDefWithDiagnostics.functionBody();
        Assert.assertFalse(funcBody.statements().get(0).hasDiagnostics());
        Assert.assertTrue(funcBody.statements().get(1).hasDiagnostics());
        Assert.assertFalse(funcBody.statements().get(2).hasDiagnostics());

        FunctionDefinitionNode funcDefWithoutDiagnostics = (FunctionDefinitionNode) modulePartNode.members().get(1);
        Assert.assertFalse(funcDefWithoutDiagnostics.hasDiagnostics());
    }

    @Test
    public void testGetDiagnosticsMethod() {
        // This bal file has two missing semicolon error.
        SyntaxTree syntaxTree = parseFile("diagnostics_test_001.bal");
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        List<Diagnostic> diagnosticList = new ArrayList<>();
        syntaxTree.diagnostics().forEach(diagnosticList::add);


        Diagnostic firstDiagnostic = diagnosticList.get(0);
        DiagnosticInfo firstDiagnosticInfo = firstDiagnostic.diagnosticInfo();
        Assert.assertEquals(firstDiagnosticInfo.severity(), DiagnosticSeverity.ERROR);
        Assert.assertEquals(firstDiagnosticInfo.code(),
                ERROR_MISSING_SEMICOLON_TOKEN.diagnosticId());

        Diagnostic secondDiagnostic = diagnosticList.get(1);
        DiagnosticInfo secondDiagnosticInfo = secondDiagnostic.diagnosticInfo();
        Assert.assertEquals(secondDiagnosticInfo.severity(), DiagnosticSeverity.ERROR);
        Assert.assertEquals(secondDiagnosticInfo.code(),
                ERROR_MISSING_SEMICOLON_TOKEN.diagnosticId());
    }

    protected SyntaxTree parseFile(String sourceFileName) {
        return super.parseFile(Paths.get("diagnostics").resolve(sourceFileName));
    }
}
