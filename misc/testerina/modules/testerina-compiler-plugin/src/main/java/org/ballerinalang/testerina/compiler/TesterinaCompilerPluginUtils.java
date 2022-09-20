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
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Testerina compiler plugin utils for the Testerina module.
 *
 * @since 2201.3.0
 */
public class TesterinaCompilerPluginUtils {

    public static void addSetTestOptionsCall(List<StatementNode> statements) {
        // Add the statement, 'test:setTestOptions(<args[]>);'
        statements.add(getFunctionCallStatement(
                getTestFunctionCall(TesterinaCompilerPluginConstants.SET_OPTIONS_FUNCTION, getFunctionParamList(
                        getPositionalArg(TesterinaCompilerPluginConstants.TARGET_PATH_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.PACKAGE_NAME_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.MODULE_NAME_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.REPORT_PATH_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.COVERAGE_PATH_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.GROUPS_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.DISABLE_GROUPS_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.TESTS_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.RERUN_FAILED_PARAMETER),
                        getPositionalArg(TesterinaCompilerPluginConstants.LIST_GROUPS_PARAMETER)))));
    }

    public static void addStartSuiteCall(List<StatementNode> statements) {
        // Add the statement, 'test:startSuite();'
        statements.add(getFunctionCallStatement(getTestFunctionCall(
                TesterinaCompilerPluginConstants.START_SUITE_FUNCTION,
                NodeFactory.createSeparatedNodeList(new ArrayList<>()))));
    }

    public static FunctionDefinitionNode createTestExecutionFunction(List<StatementNode> statements) {
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
                NodeFactory.createIdentifierToken(TesterinaCompilerPluginConstants.TEST_EXEC_FUNCTION,
                        NodeFactory.createEmptyMinutiaeList(),
                        NodeFactory.createMinutiaeList()),
                NodeFactory.createEmptyNodeList(),
                getFunctionSignature(),
                functionBodyNode);
        return functionDefinition;
    }

    /**
     * Returns true if a project is single file project kind.
     * @param project
     * @return is single file project
     */
    public static boolean isSingleFileProject(Project project) {
        boolean isSingleFileProject = false;
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            isSingleFileProject = true;
        }
        return isSingleFileProject;
    }

    public static StatementNode invokeRegisterFunction(String testNameVal, String testFuncVal) {

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

        return getFunctionCallStatement(getTestFunctionCall(
                TesterinaCompilerPluginConstants.TEST_REGISTER_FUNCTION, separatedNodeList));
    }

    public static MethodCallExpressionNode getTestFunctionCall(String functionName,
                                                               SeparatedNodeList<FunctionArgumentNode> nodeList) {
        SimpleNameReferenceNode functionRefNode = NodeFactory.createSimpleNameReferenceNode(
                NodeFactory.createIdentifierToken(functionName));
        SimpleNameReferenceNode testRefNode = NodeFactory.createSimpleNameReferenceNode(
                NodeFactory.createIdentifierToken(TesterinaCompilerPluginConstants.TEST_MODULE_NAME));
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

    public static SeparatedNodeList<FunctionArgumentNode> getFunctionParamList(PositionalArgumentNode... args) {

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

    public static FunctionSignatureNode getFunctionSignature() {

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
                NodeFactory.createSeparatedNodeList(
                        getStringParameter(TesterinaCompilerPluginConstants.TARGET_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.PACKAGE_NAME_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.MODULE_NAME_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.REPORT_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.COVERAGE_PATH_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.GROUPS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.DISABLE_GROUPS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.TESTS_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.RERUN_FAILED_PARAMETER),
                        NodeFactory.createToken(SyntaxKind.COMMA_TOKEN),
                        getStringParameter(TesterinaCompilerPluginConstants.LIST_GROUPS_PARAMETER)),
                NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    public static RequiredParameterNode getStringParameter(String varName) {

        return NodeFactory.createRequiredParameterNode(
                NodeFactory.createEmptyNodeList(),
                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createToken(
                        SyntaxKind.STRING_KEYWORD,
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" ")),
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" ")))),
                NodeFactory.createIdentifierToken(varName)
        );
    }

    public static PositionalArgumentNode getPositionalArg(String argName) {

        return NodeFactory.createPositionalArgumentNode(
                NodeFactory.createSimpleNameReferenceNode(NodeFactory.createIdentifierToken(argName)));
    }
}
