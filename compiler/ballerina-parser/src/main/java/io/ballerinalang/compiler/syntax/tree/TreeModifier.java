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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;

/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 *
 * This is a generated class.
 *
 * @since 1.3.0
 */
public abstract class TreeModifier extends NodeTransformer<Node> {

    @Override
    public Node transform(ModulePartNode modulePart) {
        NodeList<ImportDeclarationNode> imports = modifyNodeList(modulePart.imports());
        NodeList<ModuleMemberDeclarationNode> members = modifyNodeList(modulePart.members());
        Token eofToken = modifyToken(modulePart.eofToken());
        return modulePart.modify(
                imports,
                members,
                eofToken);
    }

    @Override
    public Node transform(FunctionDefinitionNode functionDefinition) {
        MetadataNode metadata = modifyNode(functionDefinition.metadata());
        Token visibilityQualifier = modifyToken(functionDefinition.visibilityQualifier());
        Token functionKeyword = modifyToken(functionDefinition.functionKeyword());
        IdentifierToken functionName = modifyNode(functionDefinition.functionName());
        Token openParenToken = modifyToken(functionDefinition.openParenToken());
        NodeList<ParameterNode> parameters = modifyNodeList(functionDefinition.parameters());
        Token closeParenToken = modifyToken(functionDefinition.closeParenToken());
        Node returnTypeDesc = modifyNode(functionDefinition.returnTypeDesc());
        BlockStatementNode functionBody = modifyNode(functionDefinition.functionBody());
        return functionDefinition.modify(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc,
                functionBody);
    }

    @Override
    public Node transform(ImportDeclarationNode importDeclaration) {
        Token importKeyword = modifyToken(importDeclaration.importKeyword());
        Token orgName = modifyToken(importDeclaration.orgName());
        Node moduleName = modifyNode(importDeclaration.moduleName());
        Node version = modifyNode(importDeclaration.version());
        Node prefix = modifyNode(importDeclaration.prefix());
        Token semicolon = modifyToken(importDeclaration.semicolon());
        return importDeclaration.modify(
                importKeyword,
                orgName,
                moduleName,
                version,
                prefix,
                semicolon);
    }

    @Override
    public Node transform(ListenerDeclarationNode listenerDeclaration) {
        MetadataNode metadata = modifyNode(listenerDeclaration.metadata());
        Token visibilityQualifier = modifyToken(listenerDeclaration.visibilityQualifier());
        Token listenerKeyword = modifyToken(listenerDeclaration.listenerKeyword());
        Node typeDescriptor = modifyNode(listenerDeclaration.typeDescriptor());
        Token variableName = modifyToken(listenerDeclaration.variableName());
        Token equalsToken = modifyToken(listenerDeclaration.equalsToken());
        Node initializer = modifyNode(listenerDeclaration.initializer());
        Token semicolonToken = modifyToken(listenerDeclaration.semicolonToken());
        return listenerDeclaration.modify(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(TypeDefinitionNode typeDefinitionNode) {
        MetadataNode metadata = modifyNode(typeDefinitionNode.metadata());
        Token visibilityQualifier = modifyToken(typeDefinitionNode.visibilityQualifier());
        Token typeKeyword = modifyToken(typeDefinitionNode.typeKeyword());
        Token typeName = modifyToken(typeDefinitionNode.typeName());
        Node typeDescriptor = modifyNode(typeDefinitionNode.typeDescriptor());
        Token semicolonToken = modifyToken(typeDefinitionNode.semicolonToken());
        return typeDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public Node transform(ServiceDeclarationNode serviceDeclaration) {
        MetadataNode metadata = modifyNode(serviceDeclaration.metadata());
        Token serviceKeyword = modifyToken(serviceDeclaration.serviceKeyword());
        IdentifierToken serviceName = modifyNode(serviceDeclaration.serviceName());
        Token onKeyword = modifyToken(serviceDeclaration.onKeyword());
        NodeList<ExpressionNode> expressions = modifyNodeList(serviceDeclaration.expressions());
        Node serviceBody = modifyNode(serviceDeclaration.serviceBody());
        return serviceDeclaration.modify(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    @Override
    public Node transform(AssignmentStatementNode assignmentStatement) {
        Node varRef = modifyNode(assignmentStatement.varRef());
        Token equalsToken = modifyToken(assignmentStatement.equalsToken());
        ExpressionNode expression = modifyNode(assignmentStatement.expression());
        Token semicolonToken = modifyToken(assignmentStatement.semicolonToken());
        return assignmentStatement.modify(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(CompoundAssignmentStatementNode compoundAssignmentStatement) {
        ExpressionNode lhsExpression = modifyNode(compoundAssignmentStatement.lhsExpression());
        Token binaryOperator = modifyToken(compoundAssignmentStatement.binaryOperator());
        Token equalsToken = modifyToken(compoundAssignmentStatement.equalsToken());
        ExpressionNode rhsExpression = modifyNode(compoundAssignmentStatement.rhsExpression());
        Token semicolonToken = modifyToken(compoundAssignmentStatement.semicolonToken());
        return compoundAssignmentStatement.modify(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    @Override
    public Node transform(VariableDeclarationNode variableDeclaration) {
        NodeList<AnnotationNode> annotations = modifyNodeList(variableDeclaration.annotations());
        Token finalKeyword = modifyToken(variableDeclaration.finalKeyword());
        Node typeName = modifyNode(variableDeclaration.typeName());
        Token variableName = modifyToken(variableDeclaration.variableName());
        Token equalsToken = modifyToken(variableDeclaration.equalsToken());
        ExpressionNode initializer = modifyNode(variableDeclaration.initializer());
        Token semicolonToken = modifyToken(variableDeclaration.semicolonToken());
        return variableDeclaration.modify(
                annotations,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(BlockStatementNode blockStatement) {
        Token openBraceToken = modifyToken(blockStatement.openBraceToken());
        NodeList<StatementNode> statements = modifyNodeList(blockStatement.statements());
        Token closeBraceToken = modifyToken(blockStatement.closeBraceToken());
        return blockStatement.modify(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    @Override
    public Node transform(BreakStatementNode breakStatement) {
        Token breakToken = modifyToken(breakStatement.breakToken());
        Token semicolonToken = modifyToken(breakStatement.semicolonToken());
        return breakStatement.modify(
                breakToken,
                semicolonToken);
    }

    @Override
    public Node transform(CallStatementNode callStatement) {
        Node expression = modifyNode(callStatement.expression());
        Token semicolonToken = modifyToken(callStatement.semicolonToken());
        return callStatement.modify(
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(ContinueStatementNode continueStatement) {
        Token continueToken = modifyToken(continueStatement.continueToken());
        Token semicolonToken = modifyToken(continueStatement.semicolonToken());
        return continueStatement.modify(
                continueToken,
                semicolonToken);
    }

    @Override
    public Node transform(ExternalFunctionBodyNode externalFunctionBody) {
        Token equalsToken = modifyToken(externalFunctionBody.equalsToken());
        NodeList<AnnotationNode> annotations = modifyNodeList(externalFunctionBody.annotations());
        Token externalKeyword = modifyToken(externalFunctionBody.externalKeyword());
        Token semicolonToken = modifyToken(externalFunctionBody.semicolonToken());
        return externalFunctionBody.modify(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    @Override
    public Node transform(IfElseStatementNode ifElseStatement) {
        Token ifKeyword = modifyToken(ifElseStatement.ifKeyword());
        Node condition = modifyNode(ifElseStatement.condition());
        Node ifBody = modifyNode(ifElseStatement.ifBody());
        Node elseBody = modifyNode(ifElseStatement.elseBody());
        return ifElseStatement.modify(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    @Override
    public Node transform(ElseBlockNode elseBlock) {
        Token elseKeyword = modifyToken(elseBlock.elseKeyword());
        Node elseBody = modifyNode(elseBlock.elseBody());
        return elseBlock.modify(
                elseKeyword,
                elseBody);
    }

    @Override
    public Node transform(WhileStatementNode whileStatement) {
        Token whileKeyword = modifyToken(whileStatement.whileKeyword());
        ExpressionNode condition = modifyNode(whileStatement.condition());
        Node whileBody = modifyNode(whileStatement.whileBody());
        return whileStatement.modify(
                whileKeyword,
                condition,
                whileBody);
    }

    @Override
    public Node transform(PanicStatementNode panicStatement) {
        Token panicKeyword = modifyToken(panicStatement.panicKeyword());
        ExpressionNode expression = modifyNode(panicStatement.expression());
        Token semicolonToken = modifyToken(panicStatement.semicolonToken());
        return panicStatement.modify(
                panicKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(ReturnStatementNode returnStatement) {
        Token returnKeyword = modifyToken(returnStatement.returnKeyword());
        ExpressionNode expression = modifyNode(returnStatement.expression());
        Token semicolonToken = modifyToken(returnStatement.semicolonToken());
        return returnStatement.modify(
                returnKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(BinaryExpressionNode binaryExpression) {
        Node lhsExpr = modifyNode(binaryExpression.lhsExpr());
        Token operator = modifyToken(binaryExpression.operator());
        Node rhsExpr = modifyNode(binaryExpression.rhsExpr());
        return binaryExpression.modify(
                binaryExpression.kind(),
                lhsExpr,
                operator,
                rhsExpr);
    }

    @Override
    public Node transform(BracedExpressionNode bracedExpression) {
        Token openParen = modifyToken(bracedExpression.openParen());
        Node expression = modifyNode(bracedExpression.expression());
        Token closeParen = modifyToken(bracedExpression.closeParen());
        return bracedExpression.modify(
                bracedExpression.kind(),
                openParen,
                expression,
                closeParen);
    }

    @Override
    public Node transform(CheckExpressionNode checkExpression) {
        Token checkKeyword = modifyToken(checkExpression.checkKeyword());
        Node expression = modifyNode(checkExpression.expression());
        return checkExpression.modify(
                checkKeyword,
                expression);
    }

    @Override
    public Node transform(FieldAccessExpressionNode fieldAccessExpression) {
        Node expression = modifyNode(fieldAccessExpression.expression());
        Token dotToken = modifyToken(fieldAccessExpression.dotToken());
        Token fieldName = modifyToken(fieldAccessExpression.fieldName());
        return fieldAccessExpression.modify(
                expression,
                dotToken,
                fieldName);
    }

    @Override
    public Node transform(FunctionCallExpressionNode functionCallExpression) {
        Node functionName = modifyNode(functionCallExpression.functionName());
        Token openParenToken = modifyToken(functionCallExpression.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(functionCallExpression.arguments());
        Token closeParenToken = modifyToken(functionCallExpression.closeParenToken());
        return functionCallExpression.modify(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(MethodCallExpressionNode methodCallExpression) {
        ExpressionNode expression = modifyNode(methodCallExpression.expression());
        Token dotToken = modifyToken(methodCallExpression.dotToken());
        Token methodName = modifyToken(methodCallExpression.methodName());
        Token openParenToken = modifyToken(methodCallExpression.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(methodCallExpression.arguments());
        Token closeParenToken = modifyToken(methodCallExpression.closeParenToken());
        return methodCallExpression.modify(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(MappingConstructorExpressionNode mappingConstructorExpression) {
        Token openBrace = modifyToken(mappingConstructorExpression.openBrace());
        NodeList<MappingFieldNode> fields = modifyNodeList(mappingConstructorExpression.fields());
        Token closeBrace = modifyToken(mappingConstructorExpression.closeBrace());
        return mappingConstructorExpression.modify(
                openBrace,
                fields,
                closeBrace);
    }

    @Override
    public Node transform(MemberAccessExpressionNode memberAccessExpression) {
        ExpressionNode containerExpression = modifyNode(memberAccessExpression.containerExpression());
        Token openBracket = modifyToken(memberAccessExpression.openBracket());
        ExpressionNode keyExpression = modifyNode(memberAccessExpression.keyExpression());
        Token closeBracket = modifyToken(memberAccessExpression.closeBracket());
        return memberAccessExpression.modify(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    @Override
    public Node transform(TypeofExpressionNode typeofExpression) {
        Token typeofKeyword = modifyToken(typeofExpression.typeofKeyword());
        Node expression = modifyNode(typeofExpression.expression());
        return typeofExpression.modify(
                typeofKeyword,
                expression);
    }

    @Override
    public Node transform(UnaryExpressionNode unaryExpression) {
        Token unaryOperator = modifyToken(unaryExpression.unaryOperator());
        Node expression = modifyNode(unaryExpression.expression());
        return unaryExpression.modify(
                unaryOperator,
                expression);
    }

    @Override
    public Node transform(ComputedNameFieldNode computedNameField) {
        Token leadingComma = modifyToken(computedNameField.leadingComma());
        Token openBracket = modifyToken(computedNameField.openBracket());
        Node fieldNameExpr = modifyNode(computedNameField.fieldNameExpr());
        Token closeBracket = modifyToken(computedNameField.closeBracket());
        Token colonToken = modifyToken(computedNameField.colonToken());
        Node valueExpr = modifyNode(computedNameField.valueExpr());
        return computedNameField.modify(
                leadingComma,
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    @Override
    public Node transform(ConstantDeclarationNode constantDeclaration) {
        MetadataNode metadata = modifyNode(constantDeclaration.metadata());
        Token visibilityQualifier = modifyToken(constantDeclaration.visibilityQualifier());
        Token constKeyword = modifyToken(constantDeclaration.constKeyword());
        Node typeDescriptor = modifyNode(constantDeclaration.typeDescriptor());
        Token variableName = modifyToken(constantDeclaration.variableName());
        Token equalsToken = modifyToken(constantDeclaration.equalsToken());
        Node initializer = modifyNode(constantDeclaration.initializer());
        Token semicolonToken = modifyToken(constantDeclaration.semicolonToken());
        return constantDeclaration.modify(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(DefaultableParameterNode defaultableParameter) {
        Token leadingComma = modifyToken(defaultableParameter.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(defaultableParameter.annotations());
        Token visibilityQualifier = modifyToken(defaultableParameter.visibilityQualifier());
        Node type = modifyNode(defaultableParameter.type());
        Token paramName = modifyToken(defaultableParameter.paramName());
        Token equalsToken = modifyToken(defaultableParameter.equalsToken());
        Node expression = modifyNode(defaultableParameter.expression());
        return defaultableParameter.modify(
                leadingComma,
                annotations,
                visibilityQualifier,
                type,
                paramName,
                equalsToken,
                expression);
    }

    @Override
    public Node transform(RequiredParameterNode requiredParameter) {
        Token leadingComma = modifyToken(requiredParameter.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(requiredParameter.annotations());
        Token visibilityQualifier = modifyToken(requiredParameter.visibilityQualifier());
        Node type = modifyNode(requiredParameter.type());
        Token paramName = modifyToken(requiredParameter.paramName());
        return requiredParameter.modify(
                leadingComma,
                annotations,
                visibilityQualifier,
                type,
                paramName);
    }

    @Override
    public Node transform(RestParameterNode restParameter) {
        Token leadingComma = modifyToken(restParameter.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(restParameter.annotations());
        Node type = modifyNode(restParameter.type());
        Token ellipsisToken = modifyToken(restParameter.ellipsisToken());
        Token paramName = modifyToken(restParameter.paramName());
        return restParameter.modify(
                leadingComma,
                annotations,
                type,
                ellipsisToken,
                paramName);
    }

    @Override
    public Node transform(ExpressionListItemNode expressionListItem) {
        Token leadingComma = modifyToken(expressionListItem.leadingComma());
        Node expression = modifyNode(expressionListItem.expression());
        return expressionListItem.modify(
                leadingComma,
                expression);
    }

    @Override
    public Node transform(ImportOrgNameNode importOrgName) {
        Token orgName = modifyToken(importOrgName.orgName());
        Token slashToken = modifyToken(importOrgName.slashToken());
        return importOrgName.modify(
                orgName,
                slashToken);
    }

    @Override
    public Node transform(ImportPrefixNode importPrefix) {
        Token asKeyword = modifyToken(importPrefix.asKeyword());
        Token prefix = modifyToken(importPrefix.prefix());
        return importPrefix.modify(
                asKeyword,
                prefix);
    }

    @Override
    public Node transform(ImportSubVersionNode importSubVersion) {
        Token leadingDot = modifyToken(importSubVersion.leadingDot());
        Token versionNumber = modifyToken(importSubVersion.versionNumber());
        return importSubVersion.modify(
                leadingDot,
                versionNumber);
    }

    @Override
    public Node transform(ImportVersionNode importVersion) {
        Token versionKeyword = modifyToken(importVersion.versionKeyword());
        Node versionNumber = modifyNode(importVersion.versionNumber());
        return importVersion.modify(
                versionKeyword,
                versionNumber);
    }

    @Override
    public Node transform(SubModuleNameNode subModuleName) {
        Token leadingDot = modifyToken(subModuleName.leadingDot());
        IdentifierToken moduleName = modifyNode(subModuleName.moduleName());
        return subModuleName.modify(
                leadingDot,
                moduleName);
    }

    @Override
    public Node transform(SpecificFieldNode specificField) {
        Token leadingComma = modifyToken(specificField.leadingComma());
        IdentifierToken fieldName = modifyNode(specificField.fieldName());
        Token colon = modifyToken(specificField.colon());
        ExpressionNode valueExpr = modifyNode(specificField.valueExpr());
        return specificField.modify(
                leadingComma,
                fieldName,
                colon,
                valueExpr);
    }

    @Override
    public Node transform(SpreadFieldNode spreadField) {
        Token leadingComma = modifyToken(spreadField.leadingComma());
        Token ellipsis = modifyToken(spreadField.ellipsis());
        ExpressionNode valueExpr = modifyNode(spreadField.valueExpr());
        return spreadField.modify(
                leadingComma,
                ellipsis,
                valueExpr);
    }

    @Override
    public Node transform(NamedArgumentNode namedArgument) {
        Token leadingComma = modifyToken(namedArgument.leadingComma());
        Token argumentName = modifyToken(namedArgument.argumentName());
        Token equalsToken = modifyToken(namedArgument.equalsToken());
        ExpressionNode expression = modifyNode(namedArgument.expression());
        return namedArgument.modify(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    @Override
    public Node transform(PositionalArgumentNode positionalArgument) {
        Token leadingComma = modifyToken(positionalArgument.leadingComma());
        ExpressionNode expression = modifyNode(positionalArgument.expression());
        return positionalArgument.modify(
                leadingComma,
                expression);
    }

    @Override
    public Node transform(RestArgumentNode restArgument) {
        Token leadingComma = modifyToken(restArgument.leadingComma());
        Token ellipsis = modifyToken(restArgument.ellipsis());
        ExpressionNode expression = modifyNode(restArgument.expression());
        return restArgument.modify(
                leadingComma,
                ellipsis,
                expression);
    }

    @Override
    public Node transform(ObjectTypeDescriptorNode objectTypeDescriptor) {
        NodeList<Token> objectTypeQualifiers = modifyNodeList(objectTypeDescriptor.objectTypeQualifiers());
        Token objectKeyword = modifyToken(objectTypeDescriptor.objectKeyword());
        Token openBrace = modifyToken(objectTypeDescriptor.openBrace());
        NodeList<Node> members = modifyNodeList(objectTypeDescriptor.members());
        Token closeBrace = modifyToken(objectTypeDescriptor.closeBrace());
        return objectTypeDescriptor.modify(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    @Override
    public Node transform(RecordTypeDescriptorNode recordTypeDescriptor) {
        Token objectKeyword = modifyToken(recordTypeDescriptor.objectKeyword());
        Token bodyStartDelimiter = modifyToken(recordTypeDescriptor.bodyStartDelimiter());
        NodeList<Node> fields = modifyNodeList(recordTypeDescriptor.fields());
        Token bodyEndDelimiter = modifyToken(recordTypeDescriptor.bodyEndDelimiter());
        return recordTypeDescriptor.modify(
                objectKeyword,
                bodyStartDelimiter,
                fields,
                bodyEndDelimiter);
    }

    @Override
    public Node transform(ReturnTypeDescriptorNode returnTypeDescriptor) {
        Token returnsKeyword = modifyToken(returnTypeDescriptor.returnsKeyword());
        NodeList<AnnotationNode> annotations = modifyNodeList(returnTypeDescriptor.annotations());
        Node type = modifyNode(returnTypeDescriptor.type());
        return returnTypeDescriptor.modify(
                returnsKeyword,
                annotations,
                type);
    }

    @Override
    public Node transform(NilTypeDescriptorNode nilTypeDescriptor) {
        Token openParenToken = modifyToken(nilTypeDescriptor.openParenToken());
        Token closeParenToken = modifyToken(nilTypeDescriptor.closeParenToken());
        return nilTypeDescriptor.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public Node transform(OptionalTypeDescriptorNode optionalTypeDescriptor) {
        Node typeDescriptor = modifyNode(optionalTypeDescriptor.typeDescriptor());
        Token questionMarkToken = modifyToken(optionalTypeDescriptor.questionMarkToken());
        return optionalTypeDescriptor.modify(
                typeDescriptor,
                questionMarkToken);
    }

    @Override
    public Node transform(ObjectFieldNode objectField) {
        MetadataNode metadata = modifyNode(objectField.metadata());
        Token visibilityQualifier = modifyToken(objectField.visibilityQualifier());
        Node type = modifyNode(objectField.type());
        Token fieldName = modifyToken(objectField.fieldName());
        Token equalsToken = modifyToken(objectField.equalsToken());
        ExpressionNode expression = modifyNode(objectField.expression());
        Token semicolonToken = modifyToken(objectField.semicolonToken());
        return objectField.modify(
                metadata,
                visibilityQualifier,
                type,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(RecordFieldNode recordField) {
        MetadataNode metadata = modifyNode(recordField.metadata());
        Node type = modifyNode(recordField.type());
        Token fieldName = modifyToken(recordField.fieldName());
        Token questionMarkToken = modifyToken(recordField.questionMarkToken());
        Token semicolonToken = modifyToken(recordField.semicolonToken());
        return recordField.modify(
                metadata,
                type,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    @Override
    public Node transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValue) {
        MetadataNode metadata = modifyNode(recordFieldWithDefaultValue.metadata());
        Node type = modifyNode(recordFieldWithDefaultValue.type());
        Token fieldName = modifyToken(recordFieldWithDefaultValue.fieldName());
        Token equalsToken = modifyToken(recordFieldWithDefaultValue.equalsToken());
        ExpressionNode expression = modifyNode(recordFieldWithDefaultValue.expression());
        Token semicolonToken = modifyToken(recordFieldWithDefaultValue.semicolonToken());
        return recordFieldWithDefaultValue.modify(
                metadata,
                type,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(RecordRestDescriptorNode recordRestDescriptor) {
        Node type = modifyNode(recordRestDescriptor.type());
        Token ellipsisToken = modifyToken(recordRestDescriptor.ellipsisToken());
        Token semicolonToken = modifyToken(recordRestDescriptor.semicolonToken());
        return recordRestDescriptor.modify(
                type,
                ellipsisToken,
                semicolonToken);
    }

    @Override
    public Node transform(TypeReferenceNode typeReference) {
        Token asteriskToken = modifyToken(typeReference.asteriskToken());
        Node type = modifyNode(typeReference.type());
        Token semicolonToken = modifyToken(typeReference.semicolonToken());
        return typeReference.modify(
                asteriskToken,
                type,
                semicolonToken);
    }

    @Override
    public Node transform(QualifiedIdentifierNode qualifiedIdentifier) {
        Token modulePrefix = modifyToken(qualifiedIdentifier.modulePrefix());
        Node colon = modifyNode(qualifiedIdentifier.colon());
        IdentifierToken identifier = modifyNode(qualifiedIdentifier.identifier());
        return qualifiedIdentifier.modify(
                modulePrefix,
                colon,
                identifier);
    }

    @Override
    public Node transform(ServiceBodyNode serviceBody) {
        Token openBraceToken = modifyToken(serviceBody.openBraceToken());
        NodeList<Node> resources = modifyNodeList(serviceBody.resources());
        Token closeBraceToken = modifyToken(serviceBody.closeBraceToken());
        return serviceBody.modify(
                openBraceToken,
                resources,
                closeBraceToken);
    }

    @Override
    public Node transform(AnnotationNode annotationNode) {
        Token atToken = modifyToken(annotationNode.atToken());
        Node annotReference = modifyNode(annotationNode.annotReference());
        MappingConstructorExpressionNode annotValue = modifyNode(annotationNode.annotValue());
        return annotationNode.modify(
                atToken,
                annotReference,
                annotValue);
    }

    @Override
    public Node transform(MetadataNode metadata) {
        Node documentationString = modifyNode(metadata.documentationString());
        NodeList<AnnotationNode> annotations = modifyNodeList(metadata.annotations());
        return metadata.modify(
                documentationString,
                annotations);
    }

    @Override
    public Node transform(ModuleVariableDeclarationNode moduleVariableDeclaration) {
        MetadataNode metadata = modifyNode(moduleVariableDeclaration.metadata());
        Token finalKeyword = modifyToken(moduleVariableDeclaration.finalKeyword());
        Node typeName = modifyNode(moduleVariableDeclaration.typeName());
        Token variableName = modifyToken(moduleVariableDeclaration.variableName());
        Token equalsToken = modifyToken(moduleVariableDeclaration.equalsToken());
        ExpressionNode initializer = modifyNode(moduleVariableDeclaration.initializer());
        Token semicolonToken = modifyToken(moduleVariableDeclaration.semicolonToken());
        return moduleVariableDeclaration.modify(
                metadata,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(IsExpressionNode isExpression) {
        Node expression = modifyNode(isExpression.expression());
        Token isKeyword = modifyToken(isExpression.isKeyword());
        Node typeDescriptor = modifyNode(isExpression.typeDescriptor());
        return isExpression.modify(
                expression,
                isKeyword,
                typeDescriptor);
    }

    // Tokens

    @Override
    public Node transform(Token token) {
        return token;
    }

    @Override
    public Node transform(IdentifierToken identifier) {
        return identifier;
    }

    @Override
    public Node transform(EmptyToken emptyToken) {
        return emptyToken;
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        return node;
    }

    protected <T extends Node> NodeList<T> modifyNodeList(NodeList<T> nodeList) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            T oldNode = nodeList.get(index);
            T newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }
            newSTNodes[index] = newNode.internalNode();
        }

        if (!nodeModified) {
            return nodeList;
        }

        STNode stNodeList = STNodeFactory.createNodeList(java.util.Arrays.asList(newSTNodes));
        return new NodeList<>(stNodeList.createUnlinkedFacade());
    }

    protected <T extends Token> T modifyToken(T token) {
        // TODO
        return (T) token.apply(this);
    }

    protected <T extends Node> T modifyNode(T node) {
        // TODO
        return (T) node.apply(this);
    }
}

