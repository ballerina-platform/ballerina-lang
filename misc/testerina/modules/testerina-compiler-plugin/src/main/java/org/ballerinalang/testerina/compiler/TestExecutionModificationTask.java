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

package org.ballerinalang.testerina.compiler;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.ModifierTask;
import io.ballerina.projects.plugins.SourceModifierContext;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.TextDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Code modification task to generate the main Testerina runtime function.
 *
 * @since 2201.3.0
 */
public class TestExecutionModificationTask implements ModifierTask<SourceModifierContext> {

    @Override
    public void modify(SourceModifierContext modifierContext) {
        if (!(modifierContext.currentPackage().project().kind() == ProjectKind.SINGLE_FILE_PROJECT)) {
            return;
        }

        for (ModuleId moduleId : modifierContext.currentPackage().moduleIds()) {
            Module module = modifierContext.currentPackage().module(moduleId);
            for (DocumentId documentId: module.documentIds()) {
                Document document = module.document(documentId);
                TextDocument updatedTextDocument = modifyDocument(document);
                modifierContext.modifySourceFile(updatedTextDocument, documentId);
            }
        }
    }

    private static TextDocument modifyDocument(Document document) {
        List<StatementNode> statements = new ArrayList<>();

        TesterinaCompilerPluginUtils.addSetTestOptionsCall(statements);

        // TODO: replace visitor in modifier with a simple statement addition
        ModulePartNode node = document.syntaxTree().rootNode();
        TestFunctionVisitor testFunctionVisitor = new TestFunctionVisitor();
        node.accept(testFunctionVisitor);

        // Add the statements, 'check test:registerTest(<name>, <function>);'
        testFunctionVisitor.getTestStaticFunctions().forEach(func ->
                statements.add(TesterinaCompilerPluginUtils.invokeRegisterFunction(func.functionName().toString(),
                        func.functionName().toString())));

        TesterinaCompilerPluginUtils.addStartSuiteCall(statements);
        FunctionDefinitionNode functionDefinition =
                TesterinaCompilerPluginUtils.createTestExecutionFunction(statements);

        NodeList<ModuleMemberDeclarationNode> newMembers = node.members().add(functionDefinition);
        ModulePartNode newModulePart = node.modify(node.imports(), newMembers, node.eofToken());
        SyntaxTree updatedSyntaxTree = document.syntaxTree().modifyWith(newModulePart);
        return updatedSyntaxTree.textDocument();
    }
}
