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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
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

    private static final String TEST_REGISTER_FUNCTION = "registerTest";
    private static final String SET_OPTIONS_FUNCTION = "setTestOptions";
    private static final String START_SUITE_FUNCTION = "startSuite";
    private static final String TEST_ORG_NAME = "ballerina";
    private static final String TEST_MODULE_NAME = "test";
    private static final String TEST_EXEC_FUNCTION = "__execute__";
    private static final String TEST_EXEC_FILENAME = "test_execute";

    private static final String TARGET_PATH_PARAMETER = "targetPath";
    private static final String PACKAGE_NAME_PARAMETER = "packageName";
    private static final String MODULE_NAME_PARAMETER = "moduleName";
    private static final String REPORT_PATH_PARAMETER = "report";
    private static final String COVERAGE_PATH_PARAMETER = "coverage";
    private static final String GROUPS_PARAMETER = "groups";
    private static final String DISABLE_GROUPS_PARAMETER = "disableGroups";
    private static final String TESTS_PARAMETER = "tests";
    private static final String RERUN_FAILED_PARAMETER = "rerunFailed";
    private static final String LIST_GROUPS_PARAMETER = "listGroups";

    @Override
    public void generate(SourceGeneratorContext generatorContext) {
        // TODO: refactor and reuse methods with TestExecutionModificationTask
        if (TesterinaCompilerPluginUtils.isSingleFileProject(generatorContext.currentPackage().project())) {
            return;
        }

        for (ModuleId moduleId : generatorContext.currentPackage().moduleIds()) {
            Module module = generatorContext.currentPackage().module(moduleId);
            TextDocument document = generateDocument(module);
            generatorContext.addTestSourceFile(document, TEST_EXEC_FILENAME, moduleId);
        }
    }

    private static TextDocument generateDocument(Module module) {

        List<StatementNode> statements = new ArrayList<>();

        // Add the statement, 'test:setTestOptions(<args[]>);'
        statements.add(getFunctionCallStatement(getTestFunctionCall(SET_OPTIONS_FUNCTION, getFunctionParamList(
                getPositionalArg(TARGET_PATH_PARAMETER),
                getPositionalArg(PACKAGE_NAME_PARAMETER),
                getPositionalArg(MODULE_NAME_PARAMETER),
                getPositionalArg(REPORT_PATH_PARAMETER),
                getPositionalArg(COVERAGE_PATH_PARAMETER),
                getPositionalArg(GROUPS_PARAMETER),
                getPositionalArg(DISABLE_GROUPS_PARAMETER),
                getPositionalArg(TESTS_PARAMETER),
                getPositionalArg(RERUN_FAILED_PARAMETER),
                getPositionalArg(LIST_GROUPS_PARAMETER)))));

        // Register all the test cases of the module
        for (DocumentId documentId : module.testDocumentIds()) {
            Document document = module.document(documentId);
            Node node = document.syntaxTree().rootNode();
            TestFunctionVisitor testFunctionVisitor = new TestFunctionVisitor();
            node.accept(testFunctionVisitor);

            // Add the statements, 'check test:registerTest(<name>, <function>);'
            testFunctionVisitor.getTestStaticFunctions().forEach(func ->
                    statements.add(invokeRegisterFunction(func.functionName().toString(),
                            func.functionName().toString())));

            //TODO: Enable dynamic registration upon approval
//            // Add the statements, 'check <function>()'
//            testFunctionVisitor.getTestDynamicFunctions().forEach(func ->
//                    statements.add(invokeFactoryFunction(func.functionName().toString(),
//                            func.functionSignature().returnTypeDesc())));
        }

        // Add the statement, 'test:startSuite();'
        statements.add(getFunctionCallStatement(getTestFunctionCall(START_SUITE_FUNCTION,
                NodeFactory.createSeparatedNodeList(new ArrayList<>()))));

        // Construct the test execution function
        FunctionBodyBlockNode functionBodyNode = NodeFactory.createFunctionBodyBlockNode(
                NodeFactory.createToken(
                        SyntaxKind.OPEN_BRACE_TOKEN,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n"))),
                null,
                NodeFactory.createNodeList(statements.toArray(new StatementNode[0])),
                NodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        FunctionDefinitionNode functionDefinition = NodeFactory.createFunctionDefinitionNode(
                SyntaxKind.FUNCTION_DEFINITION,
                null,
                NodeFactory.createNodeList(NodeFactory.createToken(
                        SyntaxKind.PUBLIC_KEYWORD,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" ")))),
                NodeFactory.createToken(
                        SyntaxKind.FUNCTION_KEYWORD,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))),
                NodeFactory.createIdentifierToken(TEST_EXEC_FUNCTION,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList()),
                NodeFactory.createEmptyNodeList(),
                getFunctionSignature(),
                functionBodyNode);

        // Construct the module part node
        // Add the line, 'import ballerina/test;'
        ImportDeclarationNode testImport = NodeFactory.createImportDeclarationNode(
                NodeFactory.createToken(
                        SyntaxKind.IMPORT_KEYWORD,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))),
                NodeFactory.createImportOrgNameNode(
                        NodeFactory.createIdentifierToken(TEST_ORG_NAME),
                        NodeFactory.createToken(SyntaxKind.SLASH_TOKEN)),
                NodeFactory.createSeparatedNodeList(
                        NodeFactory.createIdentifierToken(TEST_MODULE_NAME)),
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

    private static StatementNode invokeRegisterFunction(String testNameVal, String testFuncVal) {

        PositionalArgumentNode testName = NodeFactory.createPositionalArgumentNode(
                NodeFactory.createBasicLiteralNode(
                        SyntaxKind.STRING_LITERAL,
                        NodeFactory.createLiteralValueToken(
                                SyntaxKind.STRING_LITERAL_TOKEN,
                                "\"" + testNameVal + "\"",
                                NodeFactory.createEmptyMinutiaeList(),
                                NodeFactory.createEmptyMinutiaeList())));
        PositionalArgumentNode testFunction = NodeFactory.createPositionalArgumentNode(
                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createIdentifierToken(testFuncVal)));

        SeparatedNodeList<FunctionArgumentNode> separatedNodeList = getFunctionParamList(testName, testFunction);

        return getFunctionCallStatement(getTestFunctionCall(TEST_REGISTER_FUNCTION, separatedNodeList));
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

    public static MethodCallExpressionNode getTestFunctionCall(String functionName,
                                                               SeparatedNodeList<FunctionArgumentNode> nodeList) {

        SimpleNameReferenceNode functionRefNode = NodeFactory.createSimpleNameReferenceNode(
                NodeFactory.createIdentifierToken(functionName));
        SimpleNameReferenceNode testRefNode = NodeFactory.createSimpleNameReferenceNode(
                NodeFactory.createIdentifierToken(TEST_MODULE_NAME));
        return NodeFactory.createMethodCallExpressionNode(
                testRefNode,
                NodeFactory.createToken(SyntaxKind.COLON_TOKEN),
                functionRefNode,
                NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                nodeList,
                NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
    }

    public static StatementNode getFunctionCallStatement(ExpressionNode expression) {

        return NodeFactory.createExpressionStatementNode(SyntaxKind.FUNCTION_CALL, expression,
                NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN, NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n"))));
    }

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

    private static SeparatedNodeList<FunctionArgumentNode> getFunctionParamList(PositionalArgumentNode... args) {

        List<Node> nodeList = new ArrayList<>();
        for (PositionalArgumentNode arg : args) {
            nodeList.add(arg);
            nodeList.add(NodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (args.length > 0) {
            nodeList.remove(nodeList.size() - 1);
        }
        return NodeFactory.createSeparatedNodeList(nodeList);
    }

    private static FunctionSignatureNode getFunctionSignature() {

        OptionalTypeDescriptorNode optionalErrorTypeDescriptorNode =
                NodeFactory.createOptionalTypeDescriptorNode(
                        NodeFactory.createParameterizedTypeDescriptorNode(
                                SyntaxKind.ERROR_TYPE_DESC, NodeFactory.createToken(SyntaxKind.ERROR_KEYWORD), null),
                        NodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN, NodeFactory.createEmptyMinutiaeList(),
                                NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))));

        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                NodeFactory.createReturnTypeDescriptorNode(
                        NodeFactory.createToken(SyntaxKind.RETURNS_KEYWORD, NodeFactory.createMinutiaeList(
                                        NodeFactory.createWhitespaceMinutiae(" ")),
                                NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "))),
                        NodeFactory.createEmptyNodeList(), optionalErrorTypeDescriptorNode);

        return NodeFactory.createFunctionSignatureNode(NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                NodeFactory.createSeparatedNodeList(getStringParameter(TARGET_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(PACKAGE_NAME_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(MODULE_NAME_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(REPORT_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(COVERAGE_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(GROUPS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(DISABLE_GROUPS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(TESTS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(RERUN_FAILED_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN), getStringParameter(LIST_GROUPS_PARAMETER)),
                NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    private static RequiredParameterNode getStringParameter(String varName) {

        return NodeFactory.createRequiredParameterNode(
                NodeFactory.createEmptyNodeList(),
                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createToken(
                        SyntaxKind.STRING_KEYWORD,
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" ")),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" ")))),
                NodeFactory.createIdentifierToken(varName)
        );
    }

    private static PositionalArgumentNode getPositionalArg(String argName) {

        return NodeFactory.createPositionalArgumentNode(
                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createIdentifierToken(argName)));
    }
}
