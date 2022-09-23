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
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.GeneratorTask;
import io.ballerina.projects.plugins.SourceGeneratorContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.List;

/**
 * Code generation task to generate the main Testerina runtime function.
 *
 * @since 2201.3.0
 */
public class TestExecutionGenerationTask implements GeneratorTask<SourceGeneratorContext> {

    @Override
    public void generate(SourceGeneratorContext generatorContext) {
       for (ModuleId moduleId : generatorContext.currentPackage().moduleIds()) {
            Module module = generatorContext.currentPackage().module(moduleId);
            // Code generation skipped for the module since no tests are available.
            if (module.testDocumentIds().isEmpty()) {
                continue;
            }
            TextDocument document = generateDocument(module);
            generatorContext.addTestSourceFile(document, TesterinaCompilerPluginConstants.TEST_EXEC_FILENAME, moduleId);
        }
    }

    private static TextDocument generateDocument(Module module) {

        List<StatementNode> statements = new ArrayList<>();

        TesterinaCompilerPluginUtils.addSetTestOptionsCall(statements);

        // Register all the test cases of the module
        for (DocumentId documentId : module.testDocumentIds()) {
            Document document = module.document(documentId);
            Node node = document.syntaxTree().rootNode();
            TestFunctionVisitor testFunctionVisitor = new TestFunctionVisitor();
            node.accept(testFunctionVisitor);

            // Add the statements, 'check test:registerTest(<name>, <function>);'
            testFunctionVisitor.getTestStaticFunctions().forEach(func ->
                    statements.add(TesterinaCompilerPluginUtils.invokeRegisterFunction(func.functionName().toString(),
                            func.functionName().toString())));

            //TODO: Enable dynamic registration upon approval
//            // Add the statements, 'check <function>()'
//            testFunctionVisitor.getTestDynamicFunctions().forEach(func ->
//                    statements.add(invokeFactoryFunction(func.functionName().toString(),
//                            func.functionSignature().returnTypeDesc())));
        }

        TesterinaCompilerPluginUtils.addStartSuiteCall(statements);
        FunctionDefinitionNode functionDefinition =
                TesterinaCompilerPluginUtils.createTestExecutionFunction(statements);

        // Construct the module part node
        // Add the line, 'import ballerina/test;'
        ImportDeclarationNode testImport = NodeFactory.createImportDeclarationNode(
                NodeFactory.createToken(
                        SyntaxKind.IMPORT_KEYWORD,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))),
                NodeFactory.createImportOrgNameNode(
                        NodeFactory.createIdentifierToken(TesterinaCompilerPluginConstants.TEST_ORG_NAME),
                        NodeFactory.createToken(SyntaxKind.SLASH_TOKEN)),
                NodeFactory.createSeparatedNodeList(
                        NodeFactory.createIdentifierToken(TesterinaCompilerPluginConstants.TEST_MODULE_NAME)),
                null,
                NodeFactory.createToken(
                        SyntaxKind.SEMICOLON_TOKEN,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n"))));

        // Construct the document content
        NodeList<ModuleMemberDeclarationNode> nodeList = NodeFactory.createNodeList(List.of(functionDefinition));
        Token eofToken = NodeFactory.createToken(SyntaxKind.EOF_TOKEN, NodeFactory.createEmptyMinutiaeList(),
                NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n")));
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(
                NodeFactory.createNodeList(testImport), nodeList, eofToken);

        // Construct the document config
        return TextDocuments.from(modulePartNode.toSourceCode());
    }

    //TODO: Enable dynamic registration upon approval
//    private static StatementNode invokeFactoryFunction(String functionName,
//                                                       Optional<ReturnTypeDescriptorNode> returnTypeDesc) {
//
//        SimpleNameReferenceNode simpleNameReferenceNode =
//                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createIdentifierToken(functionName));
//        ExpressionNode functionExpressionNode = NodeFactory.createFunctionCallExpressionNode(
//                simpleNameReferenceNode,
//                NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN),
//                NodeFactory.createSeparatedNodeList(new ArrayList<>()),
//                NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
//
//        return getFunctionCallStatement(returnTypeDesc.isPresent()
//                ? getCheckedExpressionStatement(functionExpressionNode)
//                : functionExpressionNode);
//    }

    //TODO: Enable dynamic registration upon approval
//    private static CheckExpressionNode getCheckedExpressionStatement(ExpressionNode expression) {
//
//        return NodeFactory.createCheckExpressionNode(
//                SyntaxKind.CHECK_EXPRESSION,
//                NodeFactory.createToken(SyntaxKind.CHECK_KEYWORD,
//                        NodeFactory.createEmptyMinutiaeList(),
//                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))),
//                expression);
//    }
}
