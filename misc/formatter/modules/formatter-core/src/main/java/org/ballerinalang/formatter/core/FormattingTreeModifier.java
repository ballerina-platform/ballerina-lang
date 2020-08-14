/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerinalang.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.CheckExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerinalang.compiler.syntax.tree.FieldMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.MetadataNode;
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.NilLiteralNode;
import io.ballerinalang.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.ServiceBodyNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TemplateMemberNode;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;
import io.ballerinalang.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeParameterNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceNode;
import io.ballerinalang.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;
import io.ballerinalang.compiler.syntax.tree.XMLAtomicNamePatternNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeValue;
import io.ballerinalang.compiler.syntax.tree.XMLComment;
import io.ballerinalang.compiler.syntax.tree.XMLElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEmptyElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEndTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerinalang.compiler.syntax.tree.XMLItemNode;
import io.ballerinalang.compiler.syntax.tree.XMLNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLNamePatternChainingNode;
import io.ballerinalang.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerinalang.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLStartTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerinalang.compiler.syntax.tree.XMLTextNode;
import io.ballerinalang.compiler.syntax.tree.XmlTypeDescriptorNode;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.LineRange;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Modifies the given tree to format the nodes.
 *
 * @since 2.0.0
 */
public class FormattingTreeModifier extends TreeModifier {

    private FormattingOptions formattingOptions;
    private LineRange lineRange;

    @Override
    public ImportDeclarationNode transform(ImportDeclarationNode importDeclarationNode) {
        if (!isInLineRange(importDeclarationNode)) {
            return importDeclarationNode;
        }
        Token importKeyword = getToken(importDeclarationNode.importKeyword());
        Token semicolon = getToken(importDeclarationNode.semicolon());

        SeparatedNodeList<IdentifierToken> moduleNames = this.modifySeparatedNodeList(
                importDeclarationNode.moduleName());
        ImportOrgNameNode orgName = importDeclarationNode.orgName().orElse(null);
        ImportPrefixNode prefix = importDeclarationNode.prefix().orElse(null);
        ImportVersionNode version = importDeclarationNode.version().orElse(null);

        if (orgName != null) {
            importDeclarationNode = importDeclarationNode.modify().withOrgName(this.modifyNode(orgName)).apply();
        }
        if (prefix != null) {
            importDeclarationNode = importDeclarationNode.modify().withPrefix(this.modifyNode(prefix)).apply();
        }
        if (version != null) {
            importDeclarationNode = importDeclarationNode.modify().withVersion(this.modifyNode(version)).apply();
        }

        return importDeclarationNode.modify()
                .withImportKeyword(formatToken(importKeyword, 0, 0, 0, 0))
                .withModuleName(moduleNames)
                .withSemicolon(formatToken(semicolon, 0, 0, 0, 1))
                .apply();
    }

    @Override
    public ImportOrgNameNode transform(ImportOrgNameNode importOrgNameNode) {
        if (!isInLineRange(importOrgNameNode)) {
            return importOrgNameNode;
        }
        Token orgName = getToken(importOrgNameNode.orgName());
        Token slashToken = getToken(importOrgNameNode.slashToken());

        return importOrgNameNode.modify()
                .withOrgName(formatToken(orgName, 1, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportPrefixNode transform(ImportPrefixNode importPrefixNode) {
        if (!isInLineRange(importPrefixNode)) {
            return importPrefixNode;
        }
        Token asKeyword = getToken(importPrefixNode.asKeyword());
        Token prefix = getToken(importPrefixNode.prefix());

        return importPrefixNode.modify()
                .withAsKeyword(formatToken(asKeyword, 1, 0, 0, 0))
                .withPrefix(formatToken(prefix, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportVersionNode transform(ImportVersionNode importVersionNode) {
        if (!isInLineRange(importVersionNode)) {
            return importVersionNode;
        }
        Token versionKeyword = getToken(importVersionNode.versionKeyword());
        SeparatedNodeList<Token> versionNumber = this.modifySeparatedNodeList(importVersionNode.versionNumber());

        return importVersionNode.modify()
                .withVersionKeyword(formatToken(versionKeyword, 1, 1, 0, 0))
                .withVersionNumber(versionNumber)
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        if (!isInLineRange(identifier)) {
            return identifier;
        }
        Token identifierToken = getToken(identifier);

        return (IdentifierToken) formatToken(identifierToken, 0, 0, 0, 0);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        if (!isInLineRange(functionDefinitionNode)) {
            return functionDefinitionNode;
        }
        MetadataNode metadata = this.modifyNode(functionDefinitionNode.metadata().orElse(null));
        NodeList<Token> qualifierList = this.modifyNodeList(functionDefinitionNode.qualifierList());
        Token functionKeyword = getToken(functionDefinitionNode.functionKeyword());
        Token functionName = getToken(functionDefinitionNode.functionName());

        FunctionSignatureNode functionSignatureNode = this.modifyNode(functionDefinitionNode.functionSignature());

        if (metadata != null) {
            functionDefinitionNode = functionDefinitionNode.modify()
                    .withMetadata(metadata)
                    .apply();
        }

        functionDefinitionNode = functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 0, 0, 0))
                .withFunctionName((IdentifierToken) formatToken(functionName, 1, 0, 0, 0))
                .withFunctionSignature(functionSignatureNode)
                .apply();

        FunctionBodyNode functionBodyNode = this.modifyNode(functionDefinitionNode.functionBody());
        return functionDefinitionNode.modify()
                .withQualifierList(qualifierList)
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public FunctionSignatureNode transform(FunctionSignatureNode functionSignatureNode) {
        if (!isInLineRange(functionSignatureNode)) {
            return functionSignatureNode;
        }
        Token openPara = getToken(functionSignatureNode.openParenToken());
        Token closePara = getToken(functionSignatureNode.closeParenToken());

        SeparatedNodeList<ParameterNode> parameters = this.modifySeparatedNodeList(functionSignatureNode.parameters());
        ReturnTypeDescriptorNode returnTypeDescriptorNode = functionSignatureNode.returnTypeDesc().orElse(null);

        if (returnTypeDescriptorNode != null) {
            returnTypeDescriptorNode = this.modifyNode(returnTypeDescriptorNode);
            functionSignatureNode = functionSignatureNode.modify()
                    .withReturnTypeDesc(returnTypeDescriptorNode)
                    .apply();
        }

        return functionSignatureNode.modify()
                .withOpenParenToken(formatToken(openPara, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closePara, 0, 0, 0, 0))
                .withParameters(parameters)
                .apply();
    }

    @Override
    public ReturnTypeDescriptorNode transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        if (!isInLineRange(returnTypeDescriptorNode)) {
            return returnTypeDescriptorNode;
        }
        Token returnsKeyword = getToken(returnTypeDescriptorNode.returnsKeyword());
        NodeList<AnnotationNode> annotations = this.modifyNodeList(returnTypeDescriptorNode.annotations());
        Node type = this.modifyNode(returnTypeDescriptorNode.type());

        return returnTypeDescriptorNode.modify()
                .withAnnotations(annotations)
                .withReturnsKeyword(formatToken(returnsKeyword, 1, 1, 0, 0))
                .withType(type)
                .apply();
    }

    @Override
    public OptionalTypeDescriptorNode transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        if (!isInLineRange(optionalTypeDescriptorNode)) {
            return optionalTypeDescriptorNode;
        }
        Node typeDescriptor = this.modifyNode(optionalTypeDescriptorNode.typeDescriptor());
        Token questionMarkToken = getToken(optionalTypeDescriptorNode.questionMarkToken());

        return optionalTypeDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withQuestionMarkToken(formatToken(questionMarkToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        if (!isInLineRange(requiredParameterNode)) {
            return requiredParameterNode;
        }
        Token paramName = getToken(requiredParameterNode.paramName().orElse(null));
        Token visibilityQualifier = getToken(requiredParameterNode.visibilityQualifier().orElse(null));

        NodeList<AnnotationNode> annotations = this.modifyNodeList(requiredParameterNode.annotations());
        Node typeName = this.modifyNode(requiredParameterNode.typeName());

        if (paramName != null) {
            requiredParameterNode = requiredParameterNode.modify()
                    .withParamName(formatToken(paramName, 1, 0, 0, 0))
                    .apply();
        }
        if (visibilityQualifier != null) {
            requiredParameterNode = requiredParameterNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 0, 0, 0))
                    .apply();
        }
        return requiredParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .apply();
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        if (!isInLineRange(builtinSimpleNameReferenceNode)) {
            return builtinSimpleNameReferenceNode;
        }
        int startCol = getStartColumn(builtinSimpleNameReferenceNode, builtinSimpleNameReferenceNode.kind(), true);
        Token name = getToken(builtinSimpleNameReferenceNode.name());

        return builtinSimpleNameReferenceNode.modify()
                .withName(formatToken(name, startCol, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        if (!isInLineRange(functionBodyBlockNode)) {
            return functionBodyBlockNode;
        }
        int startColumn = getStartColumn(functionBodyBlockNode, functionBodyBlockNode.kind(), false);
        Token functionBodyOpenBrace = getToken(functionBodyBlockNode.openBraceToken());
        Token functionBodyCloseBrace = getToken(functionBodyBlockNode.closeBraceToken());

        NodeList<StatementNode> statements = this.modifyNodeList(functionBodyBlockNode.statements());

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(formatToken(functionBodyOpenBrace, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(functionBodyCloseBrace, startColumn, 0, 0, 1))
                .withStatements(statements)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        if (!isInLineRange(expressionStatementNode)) {
            return expressionStatementNode;
        }
        ExpressionNode expression = this.modifyNode(expressionStatementNode.expression());
        Token semicolonToken = expressionStatementNode.semicolonToken();

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        if (!isInLineRange(functionCallExpressionNode)) {
            return functionCallExpressionNode;
        }
        NameReferenceNode functionName = this.modifyNode(functionCallExpressionNode.functionName());
        Token functionCallOpenPara = getToken(functionCallExpressionNode.openParenToken());
        Token functionCallClosePara = getToken(functionCallExpressionNode.closeParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(functionCallExpressionNode
                .arguments());

        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .withOpenParenToken(formatToken(functionCallOpenPara, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(functionCallClosePara, 0, 0, 0, 0))
                .withArguments(arguments)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        if (!isInLineRange(qualifiedNameReferenceNode)) {
            return qualifiedNameReferenceNode;
        }
        int startCol = getStartColumn(qualifiedNameReferenceNode, qualifiedNameReferenceNode.kind(), true);
        Token modulePrefix = getToken(qualifiedNameReferenceNode.modulePrefix());
        Token identifier = getToken(qualifiedNameReferenceNode.identifier());
        Token colon = getToken((Token) qualifiedNameReferenceNode.colon());

        return qualifiedNameReferenceNode.modify()
                .withModulePrefix(formatToken(modulePrefix, startCol, 0, 0, 0))
                .withIdentifier((IdentifierToken) formatToken(identifier, 0, 0, 0, 0))
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public PositionalArgumentNode transform(PositionalArgumentNode positionalArgumentNode) {
        if (!isInLineRange(positionalArgumentNode)) {
            return positionalArgumentNode;
        }
        ExpressionNode expression = this.modifyNode(positionalArgumentNode.expression());

        return positionalArgumentNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        if (!isInLineRange(basicLiteralNode)) {
            return basicLiteralNode;
        }
        Token literalToken = getToken(basicLiteralNode.literalToken());

        return basicLiteralNode.modify()
                .withLiteralToken(formatToken(literalToken, 0, 0, 0, 0))
                .apply();
    }


    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        if (!isInLineRange(serviceDeclarationNode)) {
            return serviceDeclarationNode;
        }
        Token serviceKeyword = getToken(serviceDeclarationNode.serviceKeyword());
        IdentifierToken serviceName = (IdentifierToken) getToken(serviceDeclarationNode.serviceName());
        Token onKeyword = getToken(serviceDeclarationNode.onKeyword());

        MetadataNode metadata = this.modifyNode(serviceDeclarationNode.metadata().orElse(null));
        SeparatedNodeList<ExpressionNode> expressions =
                this.modifySeparatedNodeList(serviceDeclarationNode.expressions());

        serviceDeclarationNode = serviceDeclarationNode.modify()
                .withServiceKeyword(formatToken(serviceKeyword, 0, 0, 1, 0))
                .withServiceName((IdentifierToken) formatToken(serviceName, 1, 0, 0, 0))
                .withOnKeyword(formatToken(onKeyword, 1, 0, 0, 0))
                .withExpressions(expressions)
                .withMetadata(metadata)
                .apply();

        Node serviceBody = this.modifyNode(serviceDeclarationNode.serviceBody());

        return serviceDeclarationNode.modify()
                .withServiceBody(serviceBody)
                .apply();
    }

    @Override
    public ServiceBodyNode transform(ServiceBodyNode serviceBodyNode) {
        if (!isInLineRange(serviceBodyNode)) {
            return serviceBodyNode;
        }
        Token openBraceToken = getToken(serviceBodyNode.openBraceToken());
        Token closeBraceToken = getToken(serviceBodyNode.closeBraceToken());
        NodeList<Node> resources = this.modifyNodeList(serviceBodyNode.resources());

        return serviceBodyNode.modify()
                .withOpenBraceToken(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(closeBraceToken, 0, 0, 0, 1))
                .withResources(resources)
                .apply();
    }

    @Override
    public ExplicitNewExpressionNode transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        if (!isInLineRange(explicitNewExpressionNode)) {
            return explicitNewExpressionNode;
        }
        Token newKeywordToken = getToken(explicitNewExpressionNode.newKeyword());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(explicitNewExpressionNode.typeDescriptor());

        return explicitNewExpressionNode.modify()
                .withNewKeyword(formatToken(newKeywordToken, 1, 1, 0, 0))
                .withParenthesizedArgList(modifyNode(explicitNewExpressionNode.parenthesizedArgList()))
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public ParenthesizedArgList transform(ParenthesizedArgList parenthesizedArgList) {
        if (!isInLineRange(parenthesizedArgList)) {
            return parenthesizedArgList;
        }
        Token openParenToken = getToken(parenthesizedArgList.openParenToken());
        Token closeParenToken = getToken(parenthesizedArgList.closeParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(parenthesizedArgList
                .arguments());

        return parenthesizedArgList.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
        if (!isInLineRange(variableDeclarationNode)) {
            return variableDeclarationNode;
        }
        Token semicolonToken = getToken(variableDeclarationNode.semicolonToken());
        Token equalToken = getToken(variableDeclarationNode.equalsToken().orElse(null));
        Token finalToken = getToken(variableDeclarationNode.finalKeyword().orElse(null));
        ExpressionNode initializerNode = variableDeclarationNode.initializer().orElse(null);
        NodeList<AnnotationNode> annotationNodes = this.modifyNodeList(variableDeclarationNode.annotations());
        TypedBindingPatternNode typedBindingPatternNode = this.modifyNode(
                variableDeclarationNode.typedBindingPattern());

        if (equalToken != null) {
            variableDeclarationNode = variableDeclarationNode.modify().withEqualsToken(
                    formatToken(equalToken, 1, 1, 0, 0)).apply();
        }
        if (finalToken != null) {
            variableDeclarationNode = variableDeclarationNode.modify().withFinalKeyword(
                    formatToken(finalToken, 0, 0, 0, 0)).apply();
        }
        if (initializerNode != null) {
            variableDeclarationNode = variableDeclarationNode.modify().withInitializer(this.modifyNode(initializerNode))
                    .apply();
        }

        return variableDeclarationNode.modify()
                .withAnnotations(annotationNodes)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .withTypedBindingPattern(typedBindingPatternNode)
                .apply();
    }

    @Override
    public TypedBindingPatternNode transform(TypedBindingPatternNode typedBindingPatternNode) {
        if (!isInLineRange(typedBindingPatternNode)) {
            return typedBindingPatternNode;
        }
        BindingPatternNode bindingPatternNode = this.modifyNode(typedBindingPatternNode.bindingPattern());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(typedBindingPatternNode.typeDescriptor());

        return typedBindingPatternNode.modify()
                .withBindingPattern(bindingPatternNode)
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public CaptureBindingPatternNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        if (!isInLineRange(captureBindingPatternNode)) {
            return captureBindingPatternNode;
        }
        Token variableName = getToken(captureBindingPatternNode.variableName());

        return captureBindingPatternNode.modify()
                .withVariableName(formatToken(variableName, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ListBindingPatternNode transform(ListBindingPatternNode listBindingPatternNode) {
        if (!isInLineRange(listBindingPatternNode)) {
            return listBindingPatternNode;
        }
        SeparatedNodeList<BindingPatternNode> bindingPatternNodes = this.modifySeparatedNodeList(
                listBindingPatternNode.bindingPatterns());
        Token openBracket = getToken(listBindingPatternNode.openBracket());
        Token closeBracket = getToken(listBindingPatternNode.closeBracket());
        RestBindingPatternNode restBindingPatternNode = listBindingPatternNode.restBindingPattern().orElse(null);

        if (restBindingPatternNode != null) {
            listBindingPatternNode = listBindingPatternNode.modify().withRestBindingPattern(
                    this.modifyNode(restBindingPatternNode)).apply();
        }

        return listBindingPatternNode.modify()
                .withBindingPatterns(bindingPatternNodes)
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public MappingBindingPatternNode transform(MappingBindingPatternNode mappingBindingPatternNode) {
        if (!isInLineRange(mappingBindingPatternNode)) {
            return mappingBindingPatternNode;
        }
        Token openBraceToken = getToken(mappingBindingPatternNode.openBrace());
        Token closeBraceToken = getToken(mappingBindingPatternNode.closeBrace());
        SeparatedNodeList<FieldBindingPatternNode> fieldBindingPatternNodes =
                this.modifySeparatedNodeList(mappingBindingPatternNode.fieldBindingPatterns());
        RestBindingPatternNode restBindingPatternNode = mappingBindingPatternNode.restBindingPattern().orElse(null);

        if (restBindingPatternNode != null) {
            mappingBindingPatternNode = mappingBindingPatternNode.modify()
                    .withRestBindingPattern(
                            this.modifyNode(mappingBindingPatternNode.restBindingPattern().orElse(null))).apply();
        }

        return mappingBindingPatternNode.modify()
                .withOpenBrace(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBrace(formatToken(closeBraceToken, 0, 0, 1, 0))
                .withFieldBindingPatterns(fieldBindingPatternNodes)
                .apply();
    }

    @Override
    public FieldBindingPatternFullNode transform(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        if (!isInLineRange(fieldBindingPatternFullNode)) {
            return fieldBindingPatternFullNode;
        }
        Token colon = getToken(fieldBindingPatternFullNode.colon());
        BindingPatternNode bindingPatternNode = this.modifyNode(fieldBindingPatternFullNode.bindingPattern());
        SimpleNameReferenceNode variableName = this.modifyNode(fieldBindingPatternFullNode.variableName());

        return fieldBindingPatternFullNode.modify()
                .withBindingPattern(bindingPatternNode)
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public FieldBindingPatternVarnameNode transform(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        if (!isInLineRange(fieldBindingPatternVarnameNode)) {
            return fieldBindingPatternVarnameNode;
        }
        SimpleNameReferenceNode variableName = this.modifyNode(fieldBindingPatternVarnameNode.variableName());

        return fieldBindingPatternVarnameNode.modify()
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RestBindingPatternNode transform(RestBindingPatternNode restBindingPatternNode) {
        if (!isInLineRange(restBindingPatternNode)) {
            return restBindingPatternNode;
        }
        Token ellipsisToken = getToken(restBindingPatternNode.ellipsisToken());
        SimpleNameReferenceNode variableName = restBindingPatternNode.variableName();

        return restBindingPatternNode.modify()
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RemoteMethodCallActionNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        if (!isInLineRange(remoteMethodCallActionNode)) {
            return remoteMethodCallActionNode;
        }
        Token openParenToken = getToken(remoteMethodCallActionNode.openParenToken());
        Token closeParenToken = getToken(remoteMethodCallActionNode.closeParenToken());
        Token rightArrowToken = getToken(remoteMethodCallActionNode.rightArrowToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(remoteMethodCallActionNode
                .arguments());
        ExpressionNode expression = this.modifyNode(remoteMethodCallActionNode.expression());
        SimpleNameReferenceNode methodName = this.modifyNode(remoteMethodCallActionNode.methodName());

        return remoteMethodCallActionNode.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .withExpression(expression)
                .withMethodName(methodName)
                .withRightArrowToken(formatToken(rightArrowToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public SimpleNameReferenceNode transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        if (!isInLineRange(simpleNameReferenceNode)) {
            return simpleNameReferenceNode;
        }
        Token name = getToken(simpleNameReferenceNode.name());

        return simpleNameReferenceNode.modify()
                .withName(formatToken(name, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {
        if (!isInLineRange(ifElseStatementNode)) {
            return ifElseStatementNode;
        }
        int startColumn = 1;
        if (ifElseStatementNode.parent().kind() != SyntaxKind.ELSE_BLOCK) {
            startColumn = getStartColumn(ifElseStatementNode, ifElseStatementNode.kind(), true);
        }
        Token ifKeyword = getToken(ifElseStatementNode.ifKeyword());
        Node elseBody = ifElseStatementNode.elseBody().orElse(null);

        ifElseStatementNode = ifElseStatementNode.modify()
                .withIfKeyword(formatToken(ifKeyword, startColumn, 0, 0, 0))
                .apply();

        BlockStatementNode ifBody = this.modifyNode(ifElseStatementNode.ifBody());
        ExpressionNode condition = this.modifyNode(ifElseStatementNode.condition());

        if (elseBody != null) {
            ifElseStatementNode = ifElseStatementNode.modify().withElseBody(this.modifyNode(elseBody)).apply();
        }

        return ifElseStatementNode.modify()
                .withIfBody(ifBody)
                .withCondition(condition)
                .apply();
    }

    @Override
    public ElseBlockNode transform(ElseBlockNode elseBlockNode) {
        if (!isInLineRange(elseBlockNode)) {
            return elseBlockNode;
        }
        Token elseKeyword = getToken(elseBlockNode.elseKeyword());
        StatementNode elseBody = this.modifyNode(elseBlockNode.elseBody());
        elseBlockNode = elseBlockNode.modify()
                .withElseKeyword(formatToken(elseKeyword, 1, 0, 0, 0))
                .apply();


        return elseBlockNode.modify()
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public BracedExpressionNode transform(BracedExpressionNode bracedExpressionNode) {
        if (!isInLineRange(bracedExpressionNode)) {
            return bracedExpressionNode;
        }
        Token openParen = getToken(bracedExpressionNode.openParen());
        Token closeParen = getToken(bracedExpressionNode.closeParen());
        ExpressionNode expression = this.modifyNode(bracedExpressionNode.expression());

        return bracedExpressionNode.modify()
                .withOpenParen(formatToken(openParen, 1, 0, 0, 0))
                .withCloseParen(formatToken(closeParen, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public TypeTestExpressionNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        if (!isInLineRange(typeTestExpressionNode)) {
            return typeTestExpressionNode;
        }
        ExpressionNode expression = this.modifyNode(typeTestExpressionNode.expression());
        Node typeDescriptor = this.modifyNode(typeTestExpressionNode.typeDescriptor());
        Token isToken = getToken(typeTestExpressionNode.isKeyword());

        return typeTestExpressionNode.modify()
                .withExpression(expression)
                .withIsKeyword(formatToken(isToken, 1, 1, 0, 0))
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public ErrorTypeDescriptorNode transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        if (!isInLineRange(errorTypeDescriptorNode)) {
            return errorTypeDescriptorNode;
        }
        Token errorKeywordToken = getToken(errorTypeDescriptorNode.errorKeywordToken());
        ErrorTypeParamsNode errorTypeParamsNode = errorTypeDescriptorNode.errorTypeParamsNode().orElse(null);

        if (errorTypeParamsNode != null) {
            errorTypeDescriptorNode = errorTypeDescriptorNode.modify().withErrorTypeParamsNode(
                    this.modifyNode(errorTypeParamsNode)).apply();
        }

        return errorTypeDescriptorNode.modify()
                .withErrorKeywordToken(formatToken(errorKeywordToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ModuleVariableDeclarationNode transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        if (!isInLineRange(moduleVariableDeclarationNode)) {
            return moduleVariableDeclarationNode;
        }
        Token equalsToken = getToken(moduleVariableDeclarationNode.equalsToken());
        Token semicolonToken = getToken(moduleVariableDeclarationNode.semicolonToken());
        Token finalKeyword = getToken(moduleVariableDeclarationNode.finalKeyword().orElse(null));
        MetadataNode metadata = this.modifyNode(moduleVariableDeclarationNode.metadata().orElse(null));

        moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                .withTypedBindingPattern(this.modifyNode(moduleVariableDeclarationNode.typedBindingPattern()))
                .apply();

        ExpressionNode initializer = this.modifyNode(moduleVariableDeclarationNode.initializer());

        if (finalKeyword != null) {
            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                    .withFinalKeyword(formatToken(finalKeyword, 0, 1, 0, 0))
                    .apply();
        }
        return moduleVariableDeclarationNode.modify()
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withMetadata(metadata)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 2))
                .apply();
    }

    @Override
    public ConstantDeclarationNode transform(ConstantDeclarationNode constantDeclarationNode) {
        if (!isInLineRange(constantDeclarationNode)) {
            return constantDeclarationNode;
        }
        Token constKeyword = getToken(constantDeclarationNode.constKeyword());
        Token variableName = getToken(constantDeclarationNode.variableName());
        Token equalsToken = getToken(constantDeclarationNode.equalsToken());
        Token semicolonToken = getToken(constantDeclarationNode.semicolonToken());
        Token visibilityQualifier = getToken(constantDeclarationNode.visibilityQualifier());
        Node initializer = this.modifyNode(constantDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(constantDeclarationNode.metadata().orElse(null));
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(constantDeclarationNode.typeDescriptor());

        if (visibilityQualifier != null) {
            constantDeclarationNode = constantDeclarationNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 1, 1, 0, 0))
                    .apply();
        }

        return constantDeclarationNode.modify()
                .withConstKeyword(formatToken(constKeyword, 1, 1, 0, 0))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withMetadata(metadata)
                .withSemicolonToken(formatToken(semicolonToken, 1, 1, 0, 1))
                .withTypeDescriptor(typeDescriptorNode)
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public MetadataNode transform(MetadataNode metadataNode) {
        if (!isInLineRange(metadataNode)) {
            return metadataNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(metadataNode.annotations());
        Node documentationString = metadataNode.documentationString().orElse(null);

        if (documentationString != null) {
            metadataNode = metadataNode.modify().withDocumentationString(this.modifyNode(documentationString)).apply();
        }
        return metadataNode.modify()
                .withAnnotations(annotations)
                .apply();
    }

    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {
        if (!isInLineRange(blockStatementNode)) {
            return blockStatementNode;
        }
        int startColumn = getStartColumn(blockStatementNode, blockStatementNode.kind(), false);
        Token openBraceToken = getToken(blockStatementNode.openBraceToken());
        Token closeBraceToken = getToken(blockStatementNode.closeBraceToken());
        NodeList<StatementNode> statements = this.modifyNodeList(blockStatementNode.statements());

        int trailingNewLines = 1;
        if (blockStatementNode.parent() != null && blockStatementNode.parent().kind() == SyntaxKind.IF_ELSE_STATEMENT) {
            IfElseStatementNode ifElseStatementNode = (IfElseStatementNode) blockStatementNode.parent();
            if (ifElseStatementNode.elseBody().isPresent()) {
                trailingNewLines = 0;
            }
        }

        blockStatementNode = blockStatementNode.modify()
                .withOpenBraceToken(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(closeBraceToken, startColumn, 0, 0, trailingNewLines))
                .apply();

        return blockStatementNode.modify()
                .withStatements(statements)
                .apply();
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        if (!isInLineRange(mappingConstructorExpressionNode)) {
            return mappingConstructorExpressionNode;
        }
        int startColumn = getStartColumn(mappingConstructorExpressionNode, mappingConstructorExpressionNode.kind(),
                false);
        Token openBrace = getToken(mappingConstructorExpressionNode.openBrace());
        Token closeBrace = getToken(mappingConstructorExpressionNode.closeBrace());
        SeparatedNodeList<MappingFieldNode> fields = this.modifySeparatedNodeList(
                mappingConstructorExpressionNode.fields());

        return mappingConstructorExpressionNode.modify()
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 1))
                .withCloseBrace(formatToken(closeBrace, startColumn, 0, 1, 0))
                .withFields(fields)
                .apply();
    }

    @Override
    public ListenerDeclarationNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        if (!isInLineRange(listenerDeclarationNode)) {
            return listenerDeclarationNode;
        }
        Token equalsToken = getToken(listenerDeclarationNode.equalsToken());
        Token variableName = getToken(listenerDeclarationNode.variableName());
        Token semicolonToken = getToken(listenerDeclarationNode.semicolonToken());
        Token listenerKeyword = getToken(listenerDeclarationNode.listenerKeyword());
        Token visibilityQualifier = getToken(listenerDeclarationNode.visibilityQualifier().orElse(null));
        Node initializer = this.modifyNode(listenerDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(listenerDeclarationNode.metadata().orElse(null));
        Node typeDescriptor = this.modifyNode(listenerDeclarationNode.typeDescriptor());

        if (visibilityQualifier != null) {
            listenerDeclarationNode = listenerDeclarationNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 0, 0, 0))
                    .apply();
        }

        return listenerDeclarationNode.modify()
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withListenerKeyword(formatToken(listenerKeyword, 0, 0, 0, 0))
                .withMetadata(metadata)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .withTypeDescriptor(typeDescriptor)
                .withVariableName(formatToken(variableName, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public SpecificFieldNode transform(SpecificFieldNode specificFieldNode) {
        if (!isInLineRange(specificFieldNode)) {
            return specificFieldNode;
        }
        int startColumn = getStartColumn(specificFieldNode, specificFieldNode.kind(), true);
        Token fieldName = getToken(specificFieldNode.fieldName());
        Token readOnlyKeyword = specificFieldNode.readonlyKeyword().orElse(null);
        Token colon = getToken(specificFieldNode.colon());

        if (readOnlyKeyword != null) {
            specificFieldNode = specificFieldNode.modify()
                    .withReadonlyKeyword(formatToken(readOnlyKeyword, 0, 0, 0, 0))
                    .apply();
        }
        specificFieldNode = specificFieldNode.modify()
                .withFieldName(formatToken(fieldName, startColumn, 0, 0, 0))
                .withColon(formatToken(colon, 0, 1, 0, 0))
                .apply();

        ExpressionNode expressionNode = this.modifyNode(specificFieldNode.valueExpr());

        return specificFieldNode.modify()
                .withValueExpr(expressionNode)
                .apply();
    }

    @Override
    public BinaryExpressionNode transform(BinaryExpressionNode binaryExpressionNode) {
        if (!isInLineRange(binaryExpressionNode)) {
            return binaryExpressionNode;
        }
        Node lhsExpr = this.modifyNode(binaryExpressionNode.lhsExpr());
        Node rhsExpr = this.modifyNode(binaryExpressionNode.rhsExpr());
        Token operator = getToken(binaryExpressionNode.operator());

        return binaryExpressionNode.modify()
                .withLhsExpr(lhsExpr)
                .withRhsExpr(rhsExpr)
                .withOperator(formatToken(operator, 1, 1, 0, 0))
                .apply();
    }

    @Override
    public ArrayTypeDescriptorNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        if (!isInLineRange(arrayTypeDescriptorNode)) {
            return arrayTypeDescriptorNode;
        }
        Node arrayLength = arrayTypeDescriptorNode.arrayLength().orElse(null);
        Token openBracket = getToken(arrayTypeDescriptorNode.openBracket());
        Token closeBracket = getToken(arrayTypeDescriptorNode.closeBracket());
        TypeDescriptorNode memberTypeDesc = this.modifyNode(arrayTypeDescriptorNode.memberTypeDesc());

        if (arrayLength != null) {
            arrayTypeDescriptorNode = arrayTypeDescriptorNode.modify()
                    .withArrayLength(this.modifyNode(arrayLength)).apply();
        }
        return arrayTypeDescriptorNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .withMemberTypeDesc(memberTypeDesc)
                .apply();
    }

    @Override
    public AssignmentStatementNode transform(AssignmentStatementNode assignmentStatementNode) {
        if (!isInLineRange(assignmentStatementNode)) {
            return assignmentStatementNode;
        }
        Node varRef = this.modifyNode(assignmentStatementNode.varRef());
        ExpressionNode expression = this.modifyNode(assignmentStatementNode.expression());
        Token equalsToken = getToken(assignmentStatementNode.equalsToken());
        Token semicolonToken = getToken(assignmentStatementNode.semicolonToken());
        return assignmentStatementNode.modify()
                .withVarRef(varRef)
                .withExpression(expression)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .apply();
    }

    @Override
    public IndexedExpressionNode transform(IndexedExpressionNode indexedExpressionNode) {
        if (!isInLineRange(indexedExpressionNode)) {
            return indexedExpressionNode;
        }
        SeparatedNodeList<ExpressionNode> keyExpression = this.modifySeparatedNodeList(
                indexedExpressionNode.keyExpression());
        ExpressionNode containerExpression = this.modifyNode(indexedExpressionNode.containerExpression());
        Token openBracket = getToken(indexedExpressionNode.openBracket());
        Token closeBracket = getToken(indexedExpressionNode.closeBracket());

        return indexedExpressionNode.modify()
                .withKeyExpression(keyExpression)
                .withContainerExpression(containerExpression)
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public CheckExpressionNode transform(CheckExpressionNode checkExpressionNode) {
        if (!isInLineRange(checkExpressionNode)) {
            return checkExpressionNode;
        }
        int startColumn = getStartColumn(checkExpressionNode, checkExpressionNode.kind(), true);
        Token checkKeyword = getToken(checkExpressionNode.checkKeyword());
        ExpressionNode expressionNode = this.modifyNode(checkExpressionNode.expression());

        return checkExpressionNode.modify()
                .withCheckKeyword(formatToken(checkKeyword, startColumn, 1, 0, 0))
                .withExpression(expressionNode)
                .apply();
    }

    @Override
    public WhileStatementNode transform(WhileStatementNode whileStatementNode) {
        if (!isInLineRange(whileStatementNode)) {
            return whileStatementNode;
        }
        int startColumn = getStartColumn(whileStatementNode, whileStatementNode.kind(), true);
        Token whileKeyword = getToken(whileStatementNode.whileKeyword());

        whileStatementNode = whileStatementNode.modify()
                .withWhileKeyword(formatToken(whileKeyword, startColumn, 0, 0, 0))
                .apply();

        ExpressionNode condition = this.modifyNode(whileStatementNode.condition());
        BlockStatementNode whileBody = this.modifyNode(whileStatementNode.whileBody());

        return whileStatementNode.modify()
                .withCondition(condition)
                .withWhileBody(whileBody)
                .apply();
    }

    @Override
    public ReturnStatementNode transform(ReturnStatementNode returnStatementNode) {
        if (!isInLineRange(returnStatementNode)) {
            return returnStatementNode;
        }
        int startColumn = getStartColumn(returnStatementNode, returnStatementNode.kind(), true);
        Token returnKeyword = getToken(returnStatementNode.returnKeyword());
        ExpressionNode expressionNode = returnStatementNode.expression().orElse(null);
        Token semicolonToken = getToken(returnStatementNode.semicolonToken());

        returnStatementNode = returnStatementNode.modify()
                .withReturnKeyword(formatToken(returnKeyword, startColumn, 1, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .apply();

        if (expressionNode != null) {
            returnStatementNode = returnStatementNode.modify()
                    .withExpression(this.modifyNode(expressionNode))
                    .apply();
        }
        return returnStatementNode;
    }

    @Override
    public MethodCallExpressionNode transform(MethodCallExpressionNode methodCallExpressionNode) {
        if (!isInLineRange(methodCallExpressionNode)) {
            return methodCallExpressionNode;
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(methodCallExpressionNode
                .arguments());
        Token openParenToken = getToken(methodCallExpressionNode.openParenToken());
        Token closeParenToken = getToken(methodCallExpressionNode.closeParenToken());
        Token dotToken = getToken(methodCallExpressionNode.dotToken());
        ExpressionNode expression = this.modifyNode(methodCallExpressionNode.expression());
        NameReferenceNode methodName = this.modifyNode(methodCallExpressionNode.methodName());

        return methodCallExpressionNode.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .withDotToken(formatToken(dotToken, 0, 0, 0, 0))
                .withExpression(expression)
                .withMethodName(methodName)
                .apply();
    }

    @Override
    public NilLiteralNode transform(NilLiteralNode nilLiteralNode) {
        Token openParenToken = getToken(nilLiteralNode.openParenToken());
        Token closeParenToken = getToken(nilLiteralNode.closeParenToken());

        return nilLiteralNode.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public NilTypeDescriptorNode transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken = getToken(nilTypeDescriptorNode.openParenToken());
        Token closeParenToken = getToken(nilTypeDescriptorNode.closeParenToken());

        return nilTypeDescriptorNode.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public UnionTypeDescriptorNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        TypeDescriptorNode leftTypeDesc = this.modifyNode(unionTypeDescriptorNode.leftTypeDesc());
        Token pipeToken = getToken(unionTypeDescriptorNode.pipeToken());
        TypeDescriptorNode rightTypeDesc = this.modifyNode(unionTypeDescriptorNode.rightTypeDesc());

        return unionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withPipeToken(pipeToken)
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public XMLNamespaceDeclarationNode transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = getToken(xMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri = this.modifyNode(xMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword = getToken(xMLNamespaceDeclarationNode.asKeyword());
        IdentifierToken namespacePrefix = this.modifyNode(xMLNamespaceDeclarationNode.namespacePrefix());
        Token semicolonToken = getToken(xMLNamespaceDeclarationNode.semicolonToken());

        if (namespaceuri != null) {
            xMLNamespaceDeclarationNode = xMLNamespaceDeclarationNode.modify()
                    .withNamespaceuri(namespaceuri)
                    .apply();
        }

        if (namespacePrefix != null) {
            xMLNamespaceDeclarationNode = xMLNamespaceDeclarationNode.modify()
                    .withNamespacePrefix(namespacePrefix)
                    .apply();
        }

        return xMLNamespaceDeclarationNode.modify()
                .withXmlnsKeyword(formatToken(xmlnsKeyword, 0, 0, 0, 0))
                .withAsKeyword(formatToken(asKeyword, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = getToken(moduleXMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri = this.modifyNode(moduleXMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword = getToken(moduleXMLNamespaceDeclarationNode.asKeyword());
        IdentifierToken namespacePrefix = this.modifyNode(moduleXMLNamespaceDeclarationNode.namespacePrefix());
        Token semicolonToken = getToken(moduleXMLNamespaceDeclarationNode.semicolonToken());

        if (namespaceuri != null) {
            moduleXMLNamespaceDeclarationNode = moduleXMLNamespaceDeclarationNode.modify()
                    .withNamespaceuri(namespaceuri)
                    .apply();
        }

        if (namespacePrefix != null) {
            moduleXMLNamespaceDeclarationNode = moduleXMLNamespaceDeclarationNode.modify()
                    .withNamespacePrefix(namespacePrefix)
                    .apply();
        }

        return moduleXMLNamespaceDeclarationNode.modify()
                .withXmlnsKeyword(formatToken(xmlnsKeyword, 0, 0, 0, 0))
                .withAsKeyword(formatToken(asKeyword, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XmlTypeDescriptorNode transform(XmlTypeDescriptorNode xmlTypeDescriptorNode) {
        int startColumn = getStartColumn(xmlTypeDescriptorNode, xmlTypeDescriptorNode.kind(), true);
        Token xmlKeywordToken = getToken(xmlTypeDescriptorNode.xmlKeywordToken());
        TypeParameterNode xmlTypeParamsNode = this.modifyNode(xmlTypeDescriptorNode.xmlTypeParamsNode().orElse(null));

        if (xmlTypeParamsNode != null) {
            xmlTypeDescriptorNode = xmlTypeDescriptorNode.modify()
                    .withXmlTypeParamsNode(xmlTypeParamsNode)
                    .apply();
        }
        return xmlTypeDescriptorNode.modify()
                .withXmlKeywordToken(formatToken(xmlKeywordToken, startColumn, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLElementNode transform(XMLElementNode xMLElementNode) {
        XMLStartTagNode startTag = this.modifyNode(xMLElementNode.startTag());
        NodeList<XMLItemNode> content = modifyNodeList(xMLElementNode.content());
        XMLEndTagNode endTag = this.modifyNode(xMLElementNode.endTag());

        if (startTag != null) {
            xMLElementNode = xMLElementNode.modify()
                    .withStartTag(startTag)
                    .apply();
        }

        if (endTag != null) {
            xMLElementNode = xMLElementNode.modify()
                    .withEndTag(endTag)
                    .apply();
        }

        return xMLElementNode.modify()
                .withContent(content)
                .apply();
    }

    @Override
    public XMLStartTagNode transform(XMLStartTagNode xMLStartTagNode) {
        Token ltToken = getToken(xMLStartTagNode.ltToken());
        XMLNameNode name = this.modifyNode(xMLStartTagNode.name());
        NodeList<XMLAttributeNode> attributes = modifyNodeList(xMLStartTagNode.attributes());
        Token getToken = getToken(xMLStartTagNode.getToken());

        if (name != null) {
            xMLStartTagNode = xMLStartTagNode.modify()
                    .withName(name)
                    .apply();
        }

        return xMLStartTagNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withAttributes(attributes)
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLEndTagNode transform(XMLEndTagNode xMLEndTagNode) {
        Token ltToken = getToken(xMLEndTagNode.ltToken());
        Token slashToken = getToken(xMLEndTagNode.slashToken());
        XMLNameNode name = this.modifyNode(xMLEndTagNode.name());
        Token getToken = getToken(xMLEndTagNode.getToken());

        if (name != null) {
            xMLEndTagNode = xMLEndTagNode.modify()
                    .withName(name)
                    .apply();
        }

        return xMLEndTagNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLSimpleNameNode transform(XMLSimpleNameNode xMLSimpleNameNode) {
        Token name = getToken(xMLSimpleNameNode.name());

        return xMLSimpleNameNode.modify()
                .withName(name)
                .apply();
    }

    @Override
    public XMLQualifiedNameNode transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        XMLSimpleNameNode prefix = this.modifyNode(xMLQualifiedNameNode.prefix());
        Token colon = getToken(xMLQualifiedNameNode.colon());
        XMLSimpleNameNode name = this.modifyNode(xMLQualifiedNameNode.name());

        if (prefix != null) {
            xMLQualifiedNameNode = xMLQualifiedNameNode.modify()
                    .withPrefix(prefix)
                    .apply();
        }

        if (name != null) {
            xMLQualifiedNameNode = xMLQualifiedNameNode.modify()
                    .withName(name)
                    .apply();
        }

        return xMLQualifiedNameNode.modify()
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLEmptyElementNode transform(XMLEmptyElementNode xMLEmptyElementNode) {
        Token ltToken = getToken(xMLEmptyElementNode.ltToken());
        XMLNameNode name = this.modifyNode(xMLEmptyElementNode.name());
        NodeList<XMLAttributeNode> attributes = this.modifyNodeList(xMLEmptyElementNode.attributes());
        Token slashToken = getToken(xMLEmptyElementNode.slashToken());
        Token getToken = getToken(xMLEmptyElementNode.getToken());

        if (name != null) {
            xMLEmptyElementNode = xMLEmptyElementNode.modify()
                    .withName(name)
                    .apply();
        }

        return xMLEmptyElementNode.modify()
                .withAttributes(attributes)
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLTextNode transform(XMLTextNode xMLTextNode) {
        Token content = getToken(xMLTextNode.content());

        return xMLTextNode.modify()
                .withContent(formatToken(content, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAttributeNode transform(XMLAttributeNode xMLAttributeNode) {
        XMLNameNode attributeName = this.modifyNode(xMLAttributeNode.attributeName());
        Token equalToken = getToken(xMLAttributeNode.equalToken());
        XMLAttributeValue value = this.modifyNode(xMLAttributeNode.value());

        if (attributeName != null) {
            xMLAttributeNode = xMLAttributeNode.modify()
                    .withAttributeName(attributeName)
                    .apply();
        }

        if (value != null) {
            xMLAttributeNode = xMLAttributeNode.modify()
                    .withValue(value)
                    .apply();
        }

        return xMLAttributeNode.modify()
                .withEqualToken(formatToken(equalToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAttributeValue transform(XMLAttributeValue xMLAttributeValue) {
        Token startQuote = getToken(xMLAttributeValue.startQuote());
        NodeList<Node> value = this.modifyNodeList(xMLAttributeValue.value());
        Token endQuote = getToken(xMLAttributeValue.endQuote());

        return xMLAttributeValue.modify()
                .withStartQuote(formatToken(startQuote, 0, 0, 0, 0))
                .withValue(value)
                .withEndQuote(formatToken(endQuote, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLComment transform(XMLComment xMLComment) {
        Token commentStart = getToken(xMLComment.commentStart());
        NodeList<Node> content = this.modifyNodeList(xMLComment.content());
        Token commentEnd = getToken(xMLComment.commentEnd());

        return xMLComment.modify()
                .withCommentStart(formatToken(commentStart, 0, 0, 0, 0))
                .withContent(content)
                .withCommentEnd(formatToken(commentEnd, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLProcessingInstruction transform(XMLProcessingInstruction xMLProcessingInstruction) {
        Token piStart = getToken(xMLProcessingInstruction.piStart());
        XMLNameNode target = this.modifyNode(xMLProcessingInstruction.target());
        NodeList<Node> data = this.modifyNodeList(xMLProcessingInstruction.data());
        Token piEnd = getToken(xMLProcessingInstruction.piEnd());

        if (target != null) {
            xMLProcessingInstruction = xMLProcessingInstruction.modify()
                    .withTarget(target)
                    .apply();
        }

        return xMLProcessingInstruction.modify()
                .withPiStart(formatToken(piStart, 0, 0, 0, 0))
                .withData(data)
                .withPiEnd(formatToken(piEnd, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLFilterExpressionNode transform(XMLFilterExpressionNode xMLFilterExpressionNode) {
        ExpressionNode expression = this.modifyNode(xMLFilterExpressionNode.expression());
        XMLNamePatternChainingNode xmlPatternChain = this.modifyNode(xMLFilterExpressionNode.xmlPatternChain());

        if (expression != null) {
            xMLFilterExpressionNode = xMLFilterExpressionNode.modify()
                    .withExpression(expression)
                    .apply();
        }

        if (xmlPatternChain != null) {
            xMLFilterExpressionNode = xMLFilterExpressionNode.modify()
                    .withXmlPatternChain(xmlPatternChain)
                    .apply();
        }

        return xMLFilterExpressionNode;
    }

    @Override
    public XMLStepExpressionNode transform(XMLStepExpressionNode xMLStepExpressionNode) {
        ExpressionNode expression = this.modifyNode(xMLStepExpressionNode.expression());
        Node xmlStepStart = this.modifyNode(xMLStepExpressionNode.xmlStepStart());

        if (expression != null) {
            xMLStepExpressionNode = xMLStepExpressionNode.modify()
                    .withExpression(expression)
                    .apply();
        }

        if (xmlStepStart != null) {
            xMLStepExpressionNode = xMLStepExpressionNode.modify()
                    .withXmlStepStart(xmlStepStart)
                    .apply();
        }

        return xMLStepExpressionNode;
    }

    @Override
    public XMLNamePatternChainingNode transform(XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        Token startToken = getToken(xMLNamePatternChainingNode.startToken());
        SeparatedNodeList<Node> xmlNamePattern = modifySeparatedNodeList(xMLNamePatternChainingNode.xmlNamePattern());
        Token gtToken = getToken(xMLNamePatternChainingNode.gtToken());

        return xMLNamePatternChainingNode.modify()
                .withStartToken(formatToken(startToken, 0, 0, 0, 0))
                .withXmlNamePattern(xmlNamePattern)
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAtomicNamePatternNode transform(XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        Token prefix = getToken(xMLAtomicNamePatternNode.prefix());
        Token colon = getToken(xMLAtomicNamePatternNode.colon());
        Token name = getToken(xMLAtomicNamePatternNode.name());

        return xMLAtomicNamePatternNode.modify()
                .withPrefix(formatToken(prefix, 0, 0, 0, 0))
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .withName(formatToken(name, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TemplateExpressionNode transform(TemplateExpressionNode templateExpressionNode) {
        Token type = getToken(templateExpressionNode.type());
        Token startBacktick = getToken(templateExpressionNode.startBacktick());
        NodeList<TemplateMemberNode> content = modifyNodeList(templateExpressionNode.content());
        Token endBacktick = getToken(templateExpressionNode.endBacktick());

        return templateExpressionNode.modify()
                .withStartBacktick(formatToken(startBacktick, 1, 0, 0, 0))
                .withContent(content)
                .withType(formatToken(type, 0, 0, 0, 0))
                .withEndBacktick(formatToken(endBacktick, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ByteArrayLiteralNode transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        Token type = getToken(byteArrayLiteralNode.type());
        Token startBacktick = getToken(byteArrayLiteralNode.startBacktick());
        Token content = getToken(byteArrayLiteralNode.content().orElse(null));
        Token endBacktick = getToken(byteArrayLiteralNode.endBacktick());

        return byteArrayLiteralNode.modify()
                .withType(formatToken(type, 0, 0, 0, 0))
                .withStartBacktick(formatToken(startBacktick, 0, 0, 0, 0))
                .withContent(formatToken(content, 0, 0, 0, 0))
                .withEndBacktick(formatToken(endBacktick, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ListConstructorExpressionNode transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Token openBracket = getToken(listConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> expressions = this.modifySeparatedNodeList(listConstructorExpressionNode.expressions());
        Token closeBracket = getToken(listConstructorExpressionNode.closeBracket());

        return listConstructorExpressionNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withExpressions(expressions)
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TypeReferenceNode transform(TypeReferenceNode typeReferenceNode) {
        Token asteriskToken = getToken(typeReferenceNode.asteriskToken());
        Node typeName = this.modifyNode(typeReferenceNode.typeName());
        Token semicolonToken = getToken(typeReferenceNode.semicolonToken());

        if (typeName != null) {
            typeReferenceNode = typeReferenceNode.modify()
                    .withTypeName(typeName)
                    .apply();
        }

        return typeReferenceNode.modify()
                .withAsteriskToken(formatToken(asteriskToken, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TupleTypeDescriptorNode transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        int startCol = getStartColumn(tupleTypeDescriptorNode, tupleTypeDescriptorNode.kind(), true);
        Token openBracketToken = getToken(tupleTypeDescriptorNode.openBracketToken());
        SeparatedNodeList<Node> memberTypeDesc = this.modifySeparatedNodeList(tupleTypeDescriptorNode.memberTypeDesc());
        Token closeBracketToken = getToken(tupleTypeDescriptorNode.closeBracketToken());

        return tupleTypeDescriptorNode.modify()
                .withOpenBracketToken(formatToken(openBracketToken, startCol, 0, 0, 0))
                .withMemberTypeDesc(memberTypeDesc)
                .withCloseBracketToken(formatToken(closeBracketToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public MappingMatchPatternNode transform(MappingMatchPatternNode mappingMatchPatternNode) {
        Token openBraceToken = getToken(mappingMatchPatternNode.openBraceToken());
        SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns =
                this.modifySeparatedNodeList(mappingMatchPatternNode.fieldMatchPatterns());
        RestMatchPatternNode restMatchPattern =
                this.modifyNode(mappingMatchPatternNode.restMatchPattern().orElse(null));
        Token closeBraceToken = getToken(mappingMatchPatternNode.closeBraceToken());

        return mappingMatchPatternNode.modify(
                openBraceToken,
                fieldMatchPatterns,
                restMatchPattern,
                closeBraceToken);
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        int startCol = getStartColumn(parameterizedTypeDescriptorNode, parameterizedTypeDescriptorNode.kind(), true);
        Token parameterizedType = getToken(parameterizedTypeDescriptorNode.parameterizedType());
        TypeParameterNode typeParameter = this.modifyNode(parameterizedTypeDescriptorNode.typeParameter());

        return parameterizedTypeDescriptorNode.modify()
                .withParameterizedType(formatToken(parameterizedType, startCol, 0, 0, 0))
                .withTypeParameter(typeParameter)
                .apply();
    }

    @Override
    public TypeParameterNode transform(TypeParameterNode typeParameterNode) {
        Token ltToken = getToken(typeParameterNode.ltToken());
        TypeDescriptorNode typeNode = this.modifyNode(typeParameterNode.typeNode());
        Token gtToken = getToken(typeParameterNode.gtToken());

        if (typeNode != null) {
            typeParameterNode = typeParameterNode.modify()
                    .withTypeNode(typeNode)
                    .apply();
        }

        return typeParameterNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    /**
     * Update the minutiae and return the token.
     *
     * @param token            token
     * @param leadingSpaces    leading spaces
     * @param trailingSpaces   trailing spaces
     * @param leadingNewLines  leading new lines
     * @param trailingNewLines trailing new lines
     * @return updated token
     */
    private Token formatToken(Token token, int leadingSpaces, int trailingSpaces, int leadingNewLines,
                              int trailingNewLines) {
        if (token == null) {
            return token;
        }
        MinutiaeList leadingMinutiaeList = token.leadingMinutiae();
        MinutiaeList trailingMinutiaeList = token.trailingMinutiae();

        MinutiaeList newLeadingMinutiaeList = modifyMinutiaeList(leadingMinutiaeList, leadingSpaces, leadingNewLines);
        MinutiaeList newTrailingMinutiaeList = modifyMinutiaeList(trailingMinutiaeList, trailingSpaces,
                trailingNewLines);

        return token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private MinutiaeList modifyMinutiaeList(MinutiaeList minutiaeList, int spaces, int newLines) {
        Minutiae minutiae = NodeFactory.createWhitespaceMinutiae(getWhiteSpaces(spaces, newLines));
        return minutiaeList.add(minutiae);
    }

    private String getWhiteSpaces(int column, int newLines) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (newLines - 1); i++) {
            whiteSpaces.append("\n");
        }
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    /**
     * Initialize the token with empty minutiae lists.
     *
     * @param node node
     * @return token with empty minutiae
     */
    private <T extends Token> Token getToken(T node) {
        if (node == null) {
            return node;
        }
        MinutiaeList leadingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        MinutiaeList trailingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        if (node.containsLeadingMinutiae()) {
            leadingMinutiaeList = getCommentMinutiae(node.leadingMinutiae(), true);
        }
        if (node.containsTrailingMinutiae()) {
            trailingMinutiaeList = getCommentMinutiae(node.trailingMinutiae(), false);
        }
        return node.modify(leadingMinutiaeList, trailingMinutiaeList);
    }

    private MinutiaeList getCommentMinutiae(MinutiaeList minutiaeList, boolean isLeading) {
        MinutiaeList minutiaes = AbstractNodeFactory.createEmptyMinutiaeList();
        for (int i = 0; i < minutiaeList.size(); i++) {
            if (minutiaeList.get(i).kind().equals(SyntaxKind.COMMENT_MINUTIAE)) {
                if (i > 0) {
                    minutiaes = minutiaes.add(minutiaeList.get(i - 1));
                }
                minutiaes = minutiaes.add(minutiaeList.get(i));
                if ((i + 1) < minutiaeList.size() && isLeading) {
                    minutiaes = minutiaes.add(minutiaeList.get(i + 1));
                }
            }
        }
        return minutiaes;
    }

    // TODO: Use a generic way to get the parent node using querying.
    private <T extends Node> Node getParent(T node, SyntaxKind syntaxKind) {
        Node parent = node.parent();
        if (parent == null) {
            parent = node;
        }
        SyntaxKind parentKind = parent.kind();
        if (parentKind == SyntaxKind.MODULE_VAR_DECL) {
            if (parent.parent() != null && parent.parent().kind() == SyntaxKind.MODULE_PART &&
                    syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        } else if (parentKind == SyntaxKind.FUNCTION_DEFINITION ||
                parentKind == SyntaxKind.IF_ELSE_STATEMENT ||
                parentKind == SyntaxKind.ELSE_BLOCK ||
                parentKind == SyntaxKind.SPECIFIC_FIELD ||
                parentKind == SyntaxKind.WHILE_STATEMENT) {
            return parent;
        } else if (syntaxKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            if (parentKind == SyntaxKind.REQUIRED_PARAM ||
                    parentKind == SyntaxKind.POSITIONAL_ARG ||
                    parentKind == SyntaxKind.BINARY_EXPRESSION ||
                    parentKind == SyntaxKind.RETURN_STATEMENT ||
                    parentKind == SyntaxKind.LOCAL_VAR_DECL ||
                    (parentKind == SyntaxKind.FUNCTION_CALL && parent.parent() != null &&
                            parent.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT)) {
                return null;
            }
            return getParent(parent, syntaxKind);

        } else if (parentKind == SyntaxKind.SERVICE_DECLARATION ||
                parentKind == SyntaxKind.BINARY_EXPRESSION) {
            if (syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        } else if (parentKind == SyntaxKind.REQUIRED_PARAM ||
                parentKind == SyntaxKind.RETURN_TYPE_DESCRIPTOR) {
            return null;
        } else if (parent.parent() != null) {
            return getParent(parent, syntaxKind);
        } else {
            return null;
        }
    }

    /**
     * Get the node position.
     *
     * @param node node
     * @return node position
     */
    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange range = node.lineRange();
        LinePosition startPos = range.startLine();
        LinePosition endPos = range.endLine();
        return new DiagnosticPos(null, startPos.line() + 1, endPos.line() + 1,
                startPos.offset(), endPos.offset());
    }

    /**
     * return the indented start column.
     *
     * @param node       node
     * @param syntaxKind node kind
     * @param addSpaces  add spaces or not
     * @return start position
     */
    private int getStartColumn(Node node, SyntaxKind syntaxKind, boolean addSpaces) {
        Node parent = getParent(node, syntaxKind);
        if (parent != null) {
            return getPosition(parent).sCol + (addSpaces ? 4 : 0);
        }
        return 0;
    }

    private boolean isInLineRange(Node node) {
        if (this.lineRange == null) {
            return true;
        }
        int nodeStartLine = node.lineRange().startLine().line();
        int nodeStartOffset = node.lineRange().startLine().offset();
        int nodeEndLine = node.lineRange().endLine().line();
        int nodeEndOffset = node.lineRange().endLine().offset();

        int startLine = this.lineRange.startLine().line();
        int startOffset = this.lineRange.startLine().offset();
        int endLine = this.lineRange.endLine().line();
        int endOffset = this.lineRange.endLine().offset();

        if (nodeStartLine >= startLine && nodeEndLine <= endLine) {
            if (nodeStartLine == startLine || nodeEndLine == endLine) {
                return nodeStartOffset >= startOffset && nodeEndOffset <= endOffset;
            }
            return true;
        }
        return false;
    }

    public FormattingOptions getFormattingOptions() {
        return formattingOptions;
    }

    void setFormattingOptions(FormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
    }

    void setLineRange(LineRange lineRange) {
        this.lineRange = lineRange;
    }
}
