/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.wsdltoballerina;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLOperation;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLService;
import io.ballerina.wsdltoballerina.wsdlmodel.SOAPVersion;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.wsdltoballerina.GeneratorConstants.CLIENT;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SELF;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SERVICE_URL;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SOAP11;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SOAP12;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SOAP_CLIENT;

public class SOAPClientGenerator {

    private final WSDLService wsdlService;

    protected SOAPClientGenerator(WSDLService wsdlService) {
        this.wsdlService = wsdlService;
    }

    protected ClassDefinitionNode getClassDefinitionNode() {
        List<Node> memberNodeList = new ArrayList<>();
        memberNodeList.addAll(createClassInstanceVariables());
        memberNodeList.add(createInitFunction());
        memberNodeList.addAll(getSOAPFunctions());
        IdentifierToken className = createIdentifierToken(CLIENT);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(null, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN), null);
    }

    protected List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);
        QualifiedNameReferenceNode typeName = getSOAPClientName();
        IdentifierToken fieldName = createIdentifierToken(SOAP_CLIENT);
        ObjectFieldNode httpClientField = createObjectFieldNode(null, null,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
        fieldNodeList.add(httpClientField);
        return fieldNodeList;
    }

    protected QualifiedNameReferenceNode getSOAPClientName() {
        return createQualifiedNameReferenceNode(
                createIdentifierToken(wsdlService.getSoapVersion() == SOAPVersion.SOAP11 ? SOAP11 : SOAP12),
                createToken(COLON_TOKEN), createIdentifierToken(CLIENT));
    }

    private FunctionDefinitionNode createInitFunction() {
        FunctionSignatureNode functionSignatureNode = getInitFunctionSignatureNode();
        FunctionBodyNode functionBodyNode = getInitFunctionBodyNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        return createFunctionDefinitionNode(FUNCTION_DEFINITION, null, qualifierList,
                createToken(FUNCTION_KEYWORD), functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    private List<FunctionDefinitionNode> getSOAPFunctions() {
        List<FunctionDefinitionNode> functionDefinitions = new ArrayList<>();
        for (WSDLOperation wsdlOperation : wsdlService.getWSDLOperations()) {
            SOAPFunctionGenerator functionGenerator = new SOAPFunctionGenerator(wsdlOperation);
            functionDefinitions.add(functionGenerator.generateFunction());
        }
        return functionDefinitions;
    }

    private FunctionSignatureNode getInitFunctionSignatureNode() {
        SeparatedNodeList<ParameterNode> parameterList =
                NodeFactory.createSeparatedNodeList(getServiceURLNode(wsdlService.getSoapServiceUrl()));
        OptionalTypeDescriptorNode returnType =
                NodeFactory.createOptionalTypeDescriptorNode(createToken(ERROR_KEYWORD),
                        createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = NodeFactory.createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);
        return NodeFactory.createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameterList,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    private FunctionBodyNode getInitFunctionBodyNode() {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("soapClient")));
        SeparatedNodeList<FunctionArgumentNode> arguments =
                createSeparatedNodeList(createPositionalArgumentNode(
                        createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.SERVICE_URL))));
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(
                createToken(OPEN_PAREN_TOKEN), arguments, createToken(CLOSE_PAREN_TOKEN));
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(httpClientAssignmentStatementNode);

        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }


    private ParameterNode getServiceURLNode(String serviceUrl) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("string"));
        IdentifierToken serviceURLVarName = NodeFactory.createIdentifierToken(SERVICE_URL);

        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(STRING_LITERAL,
                createIdentifierToken('"' + serviceUrl + '"'));
        return NodeFactory.createDefaultableParameterNode(annotationNodes, serviceURLType,
                serviceURLVarName, createIdentifierToken("="), expression);
    }
}
