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
package org.ballerinalang.langserver.compiler.format;

import io.ballerinalang.compiler.syntax.tree.*;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.LineRange;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Modifies the given tree to format the nodes.
 *
 * @since 2.0.0
 */
public class FormattingTreeModifier extends TreeModifier {
    @Override
    public ImportDeclarationNode transform(ImportDeclarationNode importDeclarationNode) {
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
        Token orgName = getToken(importOrgNameNode.orgName());
        Token slashToken = getToken(importOrgNameNode.slashToken());

        return importOrgNameNode.modify()
                .withOrgName(formatToken(orgName, 1, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportPrefixNode transform(ImportPrefixNode importPrefixNode) {
        Token asKeyword = getToken(importPrefixNode.asKeyword());
        Token prefix = getToken(importPrefixNode.prefix());

        return importPrefixNode.modify()
                .withAsKeyword(formatToken(asKeyword, 1, 0, 0, 0))
                .withPrefix(formatToken(prefix, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportVersionNode transform(ImportVersionNode importVersionNode) {
        Token versionKeyword = getToken(importVersionNode.versionKeyword());
        NodeList<Node> versionNumber = this.modifyNodeList(importVersionNode.versionNumber());

        return importVersionNode.modify()
                .withVersionKeyword(formatToken(versionKeyword, 1, 0, 0, 0))
                .withVersionNumber(versionNumber)
                .apply();
    }

    @Override
    public ImportSubVersionNode transform(ImportSubVersionNode importSubVersionNode) {
        Token leadingDot = getToken(importSubVersionNode.leadingDot().orElse(null));
        Token versionNumber = getToken(importSubVersionNode.versionNumber());

        if (leadingDot != null) {
            importSubVersionNode = importSubVersionNode.modify().withLeadingDot(formatToken(leadingDot, 0, 0, 0, 0))
                    .apply();
        }

        return importSubVersionNode.modify()
                .withVersionNumber(formatToken(versionNumber, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        Token identifierToken = getToken(identifier);

        return (IdentifierToken) formatToken(identifierToken, 0, 0, 0, 0);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        int startColumn = getStartColumn(functionDefinitionNode, functionDefinitionNode.kind(), true);
        Token visibilityQualifier = getToken(functionDefinitionNode.visibilityQualifier().orElse(null));
        Token functionKeyword = getToken(functionDefinitionNode.functionKeyword());
        Token functionName = getToken(functionDefinitionNode.functionName());

        FunctionSignatureNode functionSignatureNode = this.modifyNode(functionDefinitionNode.functionSignature());
        Token functionSignatureOpenPara = getToken(functionSignatureNode.openParenToken());
        Token functionSignatureClosePara = getToken(functionSignatureNode.closeParenToken());

        if (visibilityQualifier != null) {
            functionDefinitionNode = functionDefinitionNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, startColumn, 1, 0, 0))
                    .apply();
        }

        functionDefinitionNode = functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 0, 0, 0))
                .withFunctionName((IdentifierToken) formatToken(functionName, 1, 0, 0, 0))
                .withFunctionSignature(functionSignatureNode
                        .modify(functionSignatureOpenPara, functionSignatureNode.parameters(),
                                functionSignatureClosePara, null))
                .apply();

        FunctionBodyNode functionBodyNode = this.modifyNode(functionDefinitionNode.functionBody());
        return functionDefinitionNode.modify()
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public FunctionSignatureNode transform(FunctionSignatureNode functionSignatureNode) {
        Token openPara = getToken(functionSignatureNode.openParenToken());
        Token closePara = getToken(functionSignatureNode.closeParenToken());

        NodeList<ParameterNode> parameters = this.modifyNodeList(functionSignatureNode.parameters());
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
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        Token paramName = getToken(requiredParameterNode.paramName().orElse(null));
        Token leadingComma = getToken(requiredParameterNode.leadingComma().orElse(null));
        Token visibilityQualifier = getToken(requiredParameterNode.visibilityQualifier().orElse(null));

        NodeList<AnnotationNode> annotations = this.modifyNodeList(requiredParameterNode.annotations());
        Node typeName = this.modifyNode(requiredParameterNode.typeName());

        if (leadingComma != null) {
            requiredParameterNode = requiredParameterNode.modify()
                    .withLeadingComma(formatToken(leadingComma, 0, 1, 0, 0))
                    .apply();
        }
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
        int startCol = getStartColumn(builtinSimpleNameReferenceNode, builtinSimpleNameReferenceNode.kind(), true);
        Token name = getToken(builtinSimpleNameReferenceNode.name());

        return builtinSimpleNameReferenceNode.modify()
                .withName(formatToken(name, startCol, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
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
        ExpressionNode expression = this.modifyNode(expressionStatementNode.expression());
        Token semicolonToken = expressionStatementNode.semicolonToken();

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = this.modifyNode(functionCallExpressionNode.functionName());
        Token functionCallOpenPara = getToken(functionCallExpressionNode.openParenToken());
        Token functionCallClosePara = getToken(functionCallExpressionNode.closeParenToken());
        NodeList<FunctionArgumentNode> arguments = this.modifyNodeList(functionCallExpressionNode.arguments());

        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .withOpenParenToken(formatToken(functionCallOpenPara, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(functionCallClosePara, 0, 0, 0, 0))
                .withArguments(arguments)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
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
        ExpressionNode expression = this.modifyNode(positionalArgumentNode.expression());

        Token leadingComma = getToken(positionalArgumentNode.leadingComma().orElse(null));

        if (leadingComma != null) {
            return positionalArgumentNode.modify()
                    .withExpression(expression)
                    .withLeadingComma(formatToken(leadingComma, 0, 0, 0, 0))
                    .apply();
        }

        return positionalArgumentNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        Token literalToken = getToken(basicLiteralNode.literalToken());

        return basicLiteralNode.modify()
                .withLiteralToken(formatToken(literalToken, 0, 0, 0, 0))
                .apply();
    }


    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        Token serviceKeyword = getToken(serviceDeclarationNode.serviceKeyword());
        IdentifierToken serviceName = (IdentifierToken) getToken(serviceDeclarationNode.serviceName());
        Token onKeyword = getToken(serviceDeclarationNode.onKeyword());

        MetadataNode metadata = this.modifyNode(serviceDeclarationNode.metadata());
        NodeList<ExpressionNode> expressions = this.modifyNodeList(serviceDeclarationNode.expressions());

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
    public ExpressionListItemNode transform(ExpressionListItemNode expressionListItemNode) {
        ExpressionNode expression = this.modifyNode(expressionListItemNode.expression());
        Token commaToken = getToken(expressionListItemNode.leadingComma().orElse(null));

        if (commaToken != null) {
            return expressionListItemNode.modify()
                    .withExpression(expression)
                    .withLeadingComma(formatToken(commaToken, 0, 0, 0, 0))
                    .apply();
        }
        return expressionListItemNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public ExplicitNewExpressionNode transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        Token newKeywordToken = getToken(explicitNewExpressionNode.newKeyword());
        Node parenthesizedArgList = this.modifyNode(explicitNewExpressionNode.parenthesizedArgList());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(explicitNewExpressionNode.typeDescriptor());

        return explicitNewExpressionNode.modify()
                .withNewKeyword(formatToken(newKeywordToken, 1, 1, 0, 0))
                .withParenthesizedArgList(parenthesizedArgList)
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public ParenthesizedArgList transform(ParenthesizedArgList parenthesizedArgList) {
        Token openParenToken = getToken(parenthesizedArgList.openParenToken());
        Token closeParenToken = getToken(parenthesizedArgList.closeParenToken());
        NodeList<FunctionArgumentNode> arguments = this.modifyNodeList(parenthesizedArgList.arguments());

        return parenthesizedArgList.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
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
        BindingPatternNode bindingPatternNode = this.modifyNode(typedBindingPatternNode.bindingPattern());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(typedBindingPatternNode.typeDescriptor());

        return typedBindingPatternNode.modify()
                .withBindingPattern(bindingPatternNode)
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public CaptureBindingPatternNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        Token variableName = getToken(captureBindingPatternNode.variableName());

        return captureBindingPatternNode.modify()
                .withVariableName(formatToken(variableName, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ListBindingPatternNode transform(ListBindingPatternNode listBindingPatternNode) {
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
        SimpleNameReferenceNode variableName = this.modifyNode(fieldBindingPatternVarnameNode.variableName());

        return fieldBindingPatternVarnameNode.modify()
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RestBindingPatternNode transform(RestBindingPatternNode restBindingPatternNode) {
        Token ellipsisToken = getToken(restBindingPatternNode.ellipsisToken());
        SimpleNameReferenceNode variableName = restBindingPatternNode.variableName();

        return restBindingPatternNode.modify()
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RemoteMethodCallActionNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        Token openParenToken = getToken(remoteMethodCallActionNode.openParenToken());
        Token closeParenToken = getToken(remoteMethodCallActionNode.closeParenToken());
        Token rightArrowToken = getToken(remoteMethodCallActionNode.rightArrowToken());
        NodeList<FunctionArgumentNode> arguments = this.modifyNodeList(remoteMethodCallActionNode.arguments());
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
        int startColumn = getStartColumn(simpleNameReferenceNode, simpleNameReferenceNode.kind(), true);
        Token name = getToken(simpleNameReferenceNode.name());

        return simpleNameReferenceNode.modify()
                .withName(formatToken(name, startColumn, 0, 0, 0))
                .apply();
    }

    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {
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
        Token elseKeyword = getToken(elseBlockNode.elseKeyword());
        elseBlockNode = elseBlockNode.modify()
                .withElseKeyword(formatToken(elseKeyword, 1, 0, 0, 0))
                .apply();

        StatementNode elseBody = this.modifyNode(elseBlockNode.elseBody());

        return elseBlockNode.modify()
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public BracedExpressionNode transform(BracedExpressionNode bracedExpressionNode) {
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
        Token equalsToken = getToken(moduleVariableDeclarationNode.equalsToken());
        Token semicolonToken = getToken(moduleVariableDeclarationNode.semicolonToken());
        Token finalKeyword = getToken(moduleVariableDeclarationNode.finalKeyword().orElse(null));
        MetadataNode metadata = this.modifyNode(moduleVariableDeclarationNode.metadata());

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
        Token constKeyword = getToken(constantDeclarationNode.constKeyword());
        Token variableName = getToken(constantDeclarationNode.variableName());
        Token equalsToken = getToken(constantDeclarationNode.equalsToken());
        Token semicolonToken = getToken(constantDeclarationNode.semicolonToken());
        Token visibilityQualifier = getToken(constantDeclarationNode.visibilityQualifier());
        Node initializer = this.modifyNode(constantDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(constantDeclarationNode.metadata());
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
        Token equalsToken = getToken(listenerDeclarationNode.equalsToken());
        Token variableName = getToken(listenerDeclarationNode.variableName());
        Token semicolonToken = getToken(listenerDeclarationNode.semicolonToken());
        Token listenerKeyword = getToken(listenerDeclarationNode.listenerKeyword());
        Token visibilityQualifier = getToken(listenerDeclarationNode.visibilityQualifier().orElse(null));
        Node initializer = this.modifyNode(listenerDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(listenerDeclarationNode.metadata());
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

    private Token formatToken(Token token, int leadingSpaces, int trailingSpaces, int leadingNewLines,
                              int trailingNewLines) {
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

    private <T extends Token> Token getToken(T node) {
        if (node == null) return node;
        return node.modify(AbstractNodeFactory.createEmptyMinutiaeList(),
                AbstractNodeFactory.createEmptyMinutiaeList());
    }

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
                parentKind == SyntaxKind.SPECIFIC_FIELD) {
            return parent;
        } else if (syntaxKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            if (parent.parent().kind() == SyntaxKind.FUNCTION_BODY_BLOCK) {
                return getParent(parent, syntaxKind);
            }
            return null;
        } else if (parentKind == SyntaxKind.SERVICE_DECLARATION ||
                parentKind == SyntaxKind.BINARY_EXPRESSION) {
            if (syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        } else if (parentKind == SyntaxKind.REQUIRED_PARAM) {
            return null;
        } else if (parent.parent() != null) {
            return getParent(parent, syntaxKind);
        } else {
            return null;
        }
    }

    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(null, startPos.line() + 1, endPos.line() + 1,
                startPos.offset(), endPos.offset());
    }

    private int getStartColumn(Node node, SyntaxKind syntaxKind, boolean addSpaces) {
        Node parent = getParent(node, syntaxKind);
        if (parent != null) {
            return getPosition(parent).sCol + (addSpaces ? 4 : 0);
        }
        return 0;
    }
}
