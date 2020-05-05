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

import java.util.function.Function;

/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 *
 * This is a generated class.
 *
 * @since 1.3.0
 */
public abstract class TreeModifier extends NodeTransformer<Node> {

    @Override
    public Node transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = modifyNodeList(modulePartNode.imports());
        NodeList<ModuleMemberDeclarationNode> members = modifyNodeList(modulePartNode.members());
        Token eofToken = modifyToken(modulePartNode.eofToken());
        return modulePartNode.modify(
                imports,
                members,
                eofToken);
    }

    @Override
    public Node transform(FunctionDefinitionNode functionDefinitionNode) {
        MetadataNode metadata = modifyNode(functionDefinitionNode.metadata());
        Token visibilityQualifier = modifyToken(functionDefinitionNode.visibilityQualifier().orElse(null));
        Token functionKeyword = modifyToken(functionDefinitionNode.functionKeyword());
        IdentifierToken functionName = modifyNode(functionDefinitionNode.functionName());
        FunctionSignatureNode functionSignature = modifyNode(functionDefinitionNode.functionSignature());
        Node functionBody = modifyNode(functionDefinitionNode.functionBody());
        return functionDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                functionSignature,
                functionBody);
    }

    @Override
    public Node transform(ImportDeclarationNode importDeclarationNode) {
        Token importKeyword = modifyToken(importDeclarationNode.importKeyword());
        Node orgName = modifyNode(importDeclarationNode.orgName().orElse(null));
        SeparatedNodeList<IdentifierToken> moduleName = modifySeparatedNodeList(importDeclarationNode.moduleName());
        Node version = modifyNode(importDeclarationNode.version().orElse(null));
        Node prefix = modifyNode(importDeclarationNode.prefix().orElse(null));
        Token semicolon = modifyToken(importDeclarationNode.semicolon());
        return importDeclarationNode.modify(
                importKeyword,
                orgName,
                moduleName,
                version,
                prefix,
                semicolon);
    }

    @Override
    public Node transform(ListenerDeclarationNode listenerDeclarationNode) {
        MetadataNode metadata = modifyNode(listenerDeclarationNode.metadata());
        Token visibilityQualifier = modifyToken(listenerDeclarationNode.visibilityQualifier().orElse(null));
        Token listenerKeyword = modifyToken(listenerDeclarationNode.listenerKeyword());
        Node typeDescriptor = modifyNode(listenerDeclarationNode.typeDescriptor());
        Token variableName = modifyToken(listenerDeclarationNode.variableName());
        Token equalsToken = modifyToken(listenerDeclarationNode.equalsToken());
        Node initializer = modifyNode(listenerDeclarationNode.initializer());
        Token semicolonToken = modifyToken(listenerDeclarationNode.semicolonToken());
        return listenerDeclarationNode.modify(
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
        Token visibilityQualifier = modifyToken(typeDefinitionNode.visibilityQualifier().orElse(null));
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
    public Node transform(ServiceDeclarationNode serviceDeclarationNode) {
        MetadataNode metadata = modifyNode(serviceDeclarationNode.metadata());
        Token serviceKeyword = modifyToken(serviceDeclarationNode.serviceKeyword());
        IdentifierToken serviceName = modifyNode(serviceDeclarationNode.serviceName());
        Token onKeyword = modifyToken(serviceDeclarationNode.onKeyword());
        NodeList<ExpressionNode> expressions = modifyNodeList(serviceDeclarationNode.expressions());
        Node serviceBody = modifyNode(serviceDeclarationNode.serviceBody());
        return serviceDeclarationNode.modify(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    @Override
    public Node transform(AssignmentStatementNode assignmentStatementNode) {
        Node varRef = modifyNode(assignmentStatementNode.varRef());
        Token equalsToken = modifyToken(assignmentStatementNode.equalsToken());
        ExpressionNode expression = modifyNode(assignmentStatementNode.expression());
        Token semicolonToken = modifyToken(assignmentStatementNode.semicolonToken());
        return assignmentStatementNode.modify(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        ExpressionNode lhsExpression = modifyNode(compoundAssignmentStatementNode.lhsExpression());
        Token binaryOperator = modifyToken(compoundAssignmentStatementNode.binaryOperator());
        Token equalsToken = modifyToken(compoundAssignmentStatementNode.equalsToken());
        ExpressionNode rhsExpression = modifyNode(compoundAssignmentStatementNode.rhsExpression());
        Token semicolonToken = modifyToken(compoundAssignmentStatementNode.semicolonToken());
        return compoundAssignmentStatementNode.modify(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    @Override
    public Node transform(VariableDeclarationNode variableDeclarationNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(variableDeclarationNode.annotations());
        Token finalKeyword = modifyToken(variableDeclarationNode.finalKeyword().orElse(null));
        Node typeName = modifyNode(variableDeclarationNode.typeName());
        Token variableName = modifyToken(variableDeclarationNode.variableName());
        Token equalsToken = modifyToken(variableDeclarationNode.equalsToken().orElse(null));
        ExpressionNode initializer = modifyNode(variableDeclarationNode.initializer().orElse(null));
        Token semicolonToken = modifyToken(variableDeclarationNode.semicolonToken());
        return variableDeclarationNode.modify(
                annotations,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(BlockStatementNode blockStatementNode) {
        Token openBraceToken = modifyToken(blockStatementNode.openBraceToken());
        NodeList<StatementNode> statements = modifyNodeList(blockStatementNode.statements());
        Token closeBraceToken = modifyToken(blockStatementNode.closeBraceToken());
        return blockStatementNode.modify(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    @Override
    public Node transform(BreakStatementNode breakStatementNode) {
        Token breakToken = modifyToken(breakStatementNode.breakToken());
        Token semicolonToken = modifyToken(breakStatementNode.semicolonToken());
        return breakStatementNode.modify(
                breakToken,
                semicolonToken);
    }

    @Override
    public Node transform(ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression = modifyNode(expressionStatementNode.expression());
        Token semicolonToken = modifyToken(expressionStatementNode.semicolonToken());
        return expressionStatementNode.modify(
                expressionStatementNode.kind(),
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(ContinueStatementNode continueStatementNode) {
        Token continueToken = modifyToken(continueStatementNode.continueToken());
        Token semicolonToken = modifyToken(continueStatementNode.semicolonToken());
        return continueStatementNode.modify(
                continueToken,
                semicolonToken);
    }

    @Override
    public Node transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        Token equalsToken = modifyToken(externalFunctionBodyNode.equalsToken());
        NodeList<AnnotationNode> annotations = modifyNodeList(externalFunctionBodyNode.annotations());
        Token externalKeyword = modifyToken(externalFunctionBodyNode.externalKeyword());
        Token semicolonToken = modifyToken(externalFunctionBodyNode.semicolonToken());
        return externalFunctionBodyNode.modify(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    @Override
    public Node transform(IfElseStatementNode ifElseStatementNode) {
        Token ifKeyword = modifyToken(ifElseStatementNode.ifKeyword());
        ExpressionNode condition = modifyNode(ifElseStatementNode.condition());
        BlockStatementNode ifBody = modifyNode(ifElseStatementNode.ifBody());
        Node elseBody = modifyNode(ifElseStatementNode.elseBody().orElse(null));
        return ifElseStatementNode.modify(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    @Override
    public Node transform(ElseBlockNode elseBlockNode) {
        Token elseKeyword = modifyToken(elseBlockNode.elseKeyword());
        StatementNode elseBody = modifyNode(elseBlockNode.elseBody());
        return elseBlockNode.modify(
                elseKeyword,
                elseBody);
    }

    @Override
    public Node transform(WhileStatementNode whileStatementNode) {
        Token whileKeyword = modifyToken(whileStatementNode.whileKeyword());
        ExpressionNode condition = modifyNode(whileStatementNode.condition());
        BlockStatementNode whileBody = modifyNode(whileStatementNode.whileBody());
        return whileStatementNode.modify(
                whileKeyword,
                condition,
                whileBody);
    }

    @Override
    public Node transform(PanicStatementNode panicStatementNode) {
        Token panicKeyword = modifyToken(panicStatementNode.panicKeyword());
        ExpressionNode expression = modifyNode(panicStatementNode.expression());
        Token semicolonToken = modifyToken(panicStatementNode.semicolonToken());
        return panicStatementNode.modify(
                panicKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(ReturnStatementNode returnStatementNode) {
        Token returnKeyword = modifyToken(returnStatementNode.returnKeyword());
        ExpressionNode expression = modifyNode(returnStatementNode.expression().orElse(null));
        Token semicolonToken = modifyToken(returnStatementNode.semicolonToken());
        return returnStatementNode.modify(
                returnKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(localTypeDefinitionStatementNode.annotations());
        Token typeKeyword = modifyToken(localTypeDefinitionStatementNode.typeKeyword());
        Node typeName = modifyNode(localTypeDefinitionStatementNode.typeName());
        Node typeDescriptor = modifyNode(localTypeDefinitionStatementNode.typeDescriptor());
        Token semicolonToken = modifyToken(localTypeDefinitionStatementNode.semicolonToken());
        return localTypeDefinitionStatementNode.modify(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public Node transform(LockStatementNode lockStatementNode) {
        Token lockKeyword = modifyToken(lockStatementNode.lockKeyword());
        StatementNode blockStatement = modifyNode(lockStatementNode.blockStatement());
        return lockStatementNode.modify(
                lockKeyword,
                blockStatement);
    }

    @Override
    public Node transform(ForkStatementNode forkStatementNode) {
        Token forkKeyword = modifyToken(forkStatementNode.forkKeyword());
        Token openBraceToken = modifyToken(forkStatementNode.openBraceToken());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations = modifyNodeList(forkStatementNode.namedWorkerDeclarations());
        Token closeBraceToken = modifyToken(forkStatementNode.closeBraceToken());
        return forkStatementNode.modify(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations,
                closeBraceToken);
    }

    @Override
    public Node transform(ForEachStatementNode forEachStatementNode) {
        Token forEachKeyword = modifyToken(forEachStatementNode.forEachKeyword());
        Node typeDescriptor = modifyNode(forEachStatementNode.typeDescriptor());
        Token variableName = modifyToken(forEachStatementNode.variableName());
        Token inKeyword = modifyToken(forEachStatementNode.inKeyword());
        Node ActionOrExpressionNode = modifyNode(forEachStatementNode.ActionOrExpressionNode());
        StatementNode blockStatement = modifyNode(forEachStatementNode.blockStatement());
        return forEachStatementNode.modify(
                forEachKeyword,
                typeDescriptor,
                variableName,
                inKeyword,
                ActionOrExpressionNode,
                blockStatement);
    }

    @Override
    public Node transform(BinaryExpressionNode binaryExpressionNode) {
        Node lhsExpr = modifyNode(binaryExpressionNode.lhsExpr());
        Token operator = modifyToken(binaryExpressionNode.operator());
        Node rhsExpr = modifyNode(binaryExpressionNode.rhsExpr());
        return binaryExpressionNode.modify(
                binaryExpressionNode.kind(),
                lhsExpr,
                operator,
                rhsExpr);
    }

    @Override
    public Node transform(BracedExpressionNode bracedExpressionNode) {
        Token openParen = modifyToken(bracedExpressionNode.openParen());
        ExpressionNode expression = modifyNode(bracedExpressionNode.expression());
        Token closeParen = modifyToken(bracedExpressionNode.closeParen());
        return bracedExpressionNode.modify(
                bracedExpressionNode.kind(),
                openParen,
                expression,
                closeParen);
    }

    @Override
    public Node transform(CheckExpressionNode checkExpressionNode) {
        Token checkKeyword = modifyToken(checkExpressionNode.checkKeyword());
        ExpressionNode expression = modifyNode(checkExpressionNode.expression());
        return checkExpressionNode.modify(
                checkExpressionNode.kind(),
                checkKeyword,
                expression);
    }

    @Override
    public Node transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        ExpressionNode expression = modifyNode(fieldAccessExpressionNode.expression());
        Token dotToken = modifyToken(fieldAccessExpressionNode.dotToken());
        Token fieldName = modifyToken(fieldAccessExpressionNode.fieldName());
        return fieldAccessExpressionNode.modify(
                expression,
                dotToken,
                fieldName);
    }

    @Override
    public Node transform(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = modifyNode(functionCallExpressionNode.functionName());
        Token openParenToken = modifyToken(functionCallExpressionNode.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(functionCallExpressionNode.arguments());
        Token closeParenToken = modifyToken(functionCallExpressionNode.closeParenToken());
        return functionCallExpressionNode.modify(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        Token errorKeyword = modifyToken(errorConstructorExpressionNode.errorKeyword());
        Token openParenToken = modifyToken(errorConstructorExpressionNode.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(errorConstructorExpressionNode.arguments());
        Token closeParenToken = modifyToken(errorConstructorExpressionNode.closeParenToken());
        return errorConstructorExpressionNode.modify(
                errorKeyword,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(MethodCallExpressionNode methodCallExpressionNode) {
        ExpressionNode expression = modifyNode(methodCallExpressionNode.expression());
        Token dotToken = modifyToken(methodCallExpressionNode.dotToken());
        Token methodName = modifyToken(methodCallExpressionNode.methodName());
        Token openParenToken = modifyToken(methodCallExpressionNode.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(methodCallExpressionNode.arguments());
        Token closeParenToken = modifyToken(methodCallExpressionNode.closeParenToken());
        return methodCallExpressionNode.modify(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        Token openBrace = modifyToken(mappingConstructorExpressionNode.openBrace());
        NodeList<MappingFieldNode> fields = modifyNodeList(mappingConstructorExpressionNode.fields());
        Token closeBrace = modifyToken(mappingConstructorExpressionNode.closeBrace());
        return mappingConstructorExpressionNode.modify(
                openBrace,
                fields,
                closeBrace);
    }

    @Override
    public Node transform(IndexedExpressionNode indexedExpressionNode) {
        ExpressionNode containerExpression = modifyNode(indexedExpressionNode.containerExpression());
        Token openBracket = modifyToken(indexedExpressionNode.openBracket());
        ExpressionNode keyExpression = modifyNode(indexedExpressionNode.keyExpression());
        Token closeBracket = modifyToken(indexedExpressionNode.closeBracket());
        return indexedExpressionNode.modify(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    @Override
    public Node transform(TypeofExpressionNode typeofExpressionNode) {
        Token typeofKeyword = modifyToken(typeofExpressionNode.typeofKeyword());
        ExpressionNode expression = modifyNode(typeofExpressionNode.expression());
        return typeofExpressionNode.modify(
                typeofKeyword,
                expression);
    }

    @Override
    public Node transform(UnaryExpressionNode unaryExpressionNode) {
        Token unaryOperator = modifyToken(unaryExpressionNode.unaryOperator());
        ExpressionNode expression = modifyNode(unaryExpressionNode.expression());
        return unaryExpressionNode.modify(
                unaryOperator,
                expression);
    }

    @Override
    public Node transform(ComputedNameFieldNode computedNameFieldNode) {
        Token leadingComma = modifyToken(computedNameFieldNode.leadingComma());
        Token openBracket = modifyToken(computedNameFieldNode.openBracket());
        ExpressionNode fieldNameExpr = modifyNode(computedNameFieldNode.fieldNameExpr());
        Token closeBracket = modifyToken(computedNameFieldNode.closeBracket());
        Token colonToken = modifyToken(computedNameFieldNode.colonToken());
        ExpressionNode valueExpr = modifyNode(computedNameFieldNode.valueExpr());
        return computedNameFieldNode.modify(
                leadingComma,
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    @Override
    public Node transform(ConstantDeclarationNode constantDeclarationNode) {
        MetadataNode metadata = modifyNode(constantDeclarationNode.metadata());
        Token visibilityQualifier = modifyToken(constantDeclarationNode.visibilityQualifier());
        Token constKeyword = modifyToken(constantDeclarationNode.constKeyword());
        Node typeDescriptor = modifyNode(constantDeclarationNode.typeDescriptor());
        Token variableName = modifyToken(constantDeclarationNode.variableName());
        Token equalsToken = modifyToken(constantDeclarationNode.equalsToken());
        Node initializer = modifyNode(constantDeclarationNode.initializer());
        Token semicolonToken = modifyToken(constantDeclarationNode.semicolonToken());
        return constantDeclarationNode.modify(
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
    public Node transform(DefaultableParameterNode defaultableParameterNode) {
        Token leadingComma = modifyToken(defaultableParameterNode.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(defaultableParameterNode.annotations());
        Token visibilityQualifier = modifyToken(defaultableParameterNode.visibilityQualifier().orElse(null));
        Node typeName = modifyNode(defaultableParameterNode.typeName());
        Token paramName = modifyToken(defaultableParameterNode.paramName());
        Token equalsToken = modifyToken(defaultableParameterNode.equalsToken());
        Node expression = modifyNode(defaultableParameterNode.expression());
        return defaultableParameterNode.modify(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    @Override
    public Node transform(RequiredParameterNode requiredParameterNode) {
        Token leadingComma = modifyToken(requiredParameterNode.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(requiredParameterNode.annotations());
        Token visibilityQualifier = modifyToken(requiredParameterNode.visibilityQualifier().orElse(null));
        Node typeName = modifyNode(requiredParameterNode.typeName());
        Token paramName = modifyToken(requiredParameterNode.paramName());
        return requiredParameterNode.modify(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName);
    }

    @Override
    public Node transform(RestParameterNode restParameterNode) {
        Token leadingComma = modifyToken(restParameterNode.leadingComma());
        NodeList<AnnotationNode> annotations = modifyNodeList(restParameterNode.annotations());
        Node typeName = modifyNode(restParameterNode.typeName());
        Token ellipsisToken = modifyToken(restParameterNode.ellipsisToken());
        Token paramName = modifyToken(restParameterNode.paramName());
        return restParameterNode.modify(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    @Override
    public Node transform(ExpressionListItemNode expressionListItemNode) {
        Token leadingComma = modifyToken(expressionListItemNode.leadingComma());
        ExpressionNode expression = modifyNode(expressionListItemNode.expression());
        return expressionListItemNode.modify(
                leadingComma,
                expression);
    }

    @Override
    public Node transform(ImportOrgNameNode importOrgNameNode) {
        Token orgName = modifyToken(importOrgNameNode.orgName());
        Token slashToken = modifyToken(importOrgNameNode.slashToken());
        return importOrgNameNode.modify(
                orgName,
                slashToken);
    }

    @Override
    public Node transform(ImportPrefixNode importPrefixNode) {
        Token asKeyword = modifyToken(importPrefixNode.asKeyword());
        Token prefix = modifyToken(importPrefixNode.prefix());
        return importPrefixNode.modify(
                asKeyword,
                prefix);
    }

    @Override
    public Node transform(ImportSubVersionNode importSubVersionNode) {
        Token leadingDot = modifyToken(importSubVersionNode.leadingDot());
        Token versionNumber = modifyToken(importSubVersionNode.versionNumber());
        return importSubVersionNode.modify(
                leadingDot,
                versionNumber);
    }

    @Override
    public Node transform(ImportVersionNode importVersionNode) {
        Token versionKeyword = modifyToken(importVersionNode.versionKeyword());
        Node versionNumber = modifyNode(importVersionNode.versionNumber());
        return importVersionNode.modify(
                versionKeyword,
                versionNumber);
    }

    @Override
    public Node transform(SpecificFieldNode specificFieldNode) {
        Token leadingComma = modifyToken(specificFieldNode.leadingComma());
        Token fieldName = modifyToken(specificFieldNode.fieldName());
        Token colon = modifyToken(specificFieldNode.colon());
        ExpressionNode valueExpr = modifyNode(specificFieldNode.valueExpr());
        return specificFieldNode.modify(
                leadingComma,
                fieldName,
                colon,
                valueExpr);
    }

    @Override
    public Node transform(SpreadFieldNode spreadFieldNode) {
        Token leadingComma = modifyToken(spreadFieldNode.leadingComma());
        Token ellipsis = modifyToken(spreadFieldNode.ellipsis());
        ExpressionNode valueExpr = modifyNode(spreadFieldNode.valueExpr());
        return spreadFieldNode.modify(
                leadingComma,
                ellipsis,
                valueExpr);
    }

    @Override
    public Node transform(NamedArgumentNode namedArgumentNode) {
        Token leadingComma = modifyToken(namedArgumentNode.leadingComma());
        SimpleNameReferenceNode argumentName = modifyNode(namedArgumentNode.argumentName());
        Token equalsToken = modifyToken(namedArgumentNode.equalsToken());
        ExpressionNode expression = modifyNode(namedArgumentNode.expression());
        return namedArgumentNode.modify(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    @Override
    public Node transform(PositionalArgumentNode positionalArgumentNode) {
        Token leadingComma = modifyToken(positionalArgumentNode.leadingComma());
        ExpressionNode expression = modifyNode(positionalArgumentNode.expression());
        return positionalArgumentNode.modify(
                leadingComma,
                expression);
    }

    @Override
    public Node transform(RestArgumentNode restArgumentNode) {
        Token leadingComma = modifyToken(restArgumentNode.leadingComma());
        Token ellipsis = modifyToken(restArgumentNode.ellipsis());
        ExpressionNode expression = modifyNode(restArgumentNode.expression());
        return restArgumentNode.modify(
                leadingComma,
                ellipsis,
                expression);
    }

    @Override
    public Node transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        NodeList<Token> objectTypeQualifiers = modifyNodeList(objectTypeDescriptorNode.objectTypeQualifiers());
        Token objectKeyword = modifyToken(objectTypeDescriptorNode.objectKeyword());
        Token openBrace = modifyToken(objectTypeDescriptorNode.openBrace());
        NodeList<Node> members = modifyNodeList(objectTypeDescriptorNode.members());
        Token closeBrace = modifyToken(objectTypeDescriptorNode.closeBrace());
        return objectTypeDescriptorNode.modify(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    @Override
    public Node transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        Token objectKeyword = modifyToken(recordTypeDescriptorNode.objectKeyword());
        Token bodyStartDelimiter = modifyToken(recordTypeDescriptorNode.bodyStartDelimiter());
        NodeList<Node> fields = modifyNodeList(recordTypeDescriptorNode.fields());
        Token bodyEndDelimiter = modifyToken(recordTypeDescriptorNode.bodyEndDelimiter());
        return recordTypeDescriptorNode.modify(
                objectKeyword,
                bodyStartDelimiter,
                fields,
                bodyEndDelimiter);
    }

    @Override
    public Node transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        Token returnsKeyword = modifyToken(returnTypeDescriptorNode.returnsKeyword());
        NodeList<AnnotationNode> annotations = modifyNodeList(returnTypeDescriptorNode.annotations());
        Node type = modifyNode(returnTypeDescriptorNode.type());
        return returnTypeDescriptorNode.modify(
                returnsKeyword,
                annotations,
                type);
    }

    @Override
    public Node transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken = modifyToken(nilTypeDescriptorNode.openParenToken());
        Token closeParenToken = modifyToken(nilTypeDescriptorNode.closeParenToken());
        return nilTypeDescriptorNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public Node transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        Node typeDescriptor = modifyNode(optionalTypeDescriptorNode.typeDescriptor());
        Token questionMarkToken = modifyToken(optionalTypeDescriptorNode.questionMarkToken());
        return optionalTypeDescriptorNode.modify(
                typeDescriptor,
                questionMarkToken);
    }

    @Override
    public Node transform(ObjectFieldNode objectFieldNode) {
        MetadataNode metadata = modifyNode(objectFieldNode.metadata());
        Token visibilityQualifier = modifyToken(objectFieldNode.visibilityQualifier());
        Node typeName = modifyNode(objectFieldNode.typeName());
        Token fieldName = modifyToken(objectFieldNode.fieldName());
        Token equalsToken = modifyToken(objectFieldNode.equalsToken());
        ExpressionNode expression = modifyNode(objectFieldNode.expression());
        Token semicolonToken = modifyToken(objectFieldNode.semicolonToken());
        return objectFieldNode.modify(
                metadata,
                visibilityQualifier,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(RecordFieldNode recordFieldNode) {
        MetadataNode metadata = modifyNode(recordFieldNode.metadata());
        Node typeName = modifyNode(recordFieldNode.typeName());
        Token fieldName = modifyToken(recordFieldNode.fieldName());
        Token questionMarkToken = modifyToken(recordFieldNode.questionMarkToken().orElse(null));
        Token semicolonToken = modifyToken(recordFieldNode.semicolonToken());
        return recordFieldNode.modify(
                metadata,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    @Override
    public Node transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        MetadataNode metadata = modifyNode(recordFieldWithDefaultValueNode.metadata());
        Node typeName = modifyNode(recordFieldWithDefaultValueNode.typeName());
        Token fieldName = modifyToken(recordFieldWithDefaultValueNode.fieldName());
        Token equalsToken = modifyToken(recordFieldWithDefaultValueNode.equalsToken());
        ExpressionNode expression = modifyNode(recordFieldWithDefaultValueNode.expression());
        Token semicolonToken = modifyToken(recordFieldWithDefaultValueNode.semicolonToken());
        return recordFieldWithDefaultValueNode.modify(
                metadata,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public Node transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        Node typeName = modifyNode(recordRestDescriptorNode.typeName());
        Token ellipsisToken = modifyToken(recordRestDescriptorNode.ellipsisToken());
        Token semicolonToken = modifyToken(recordRestDescriptorNode.semicolonToken());
        return recordRestDescriptorNode.modify(
                typeName,
                ellipsisToken,
                semicolonToken);
    }

    @Override
    public Node transform(TypeReferenceNode typeReferenceNode) {
        Token asteriskToken = modifyToken(typeReferenceNode.asteriskToken());
        Node typeName = modifyNode(typeReferenceNode.typeName());
        Token semicolonToken = modifyToken(typeReferenceNode.semicolonToken());
        return typeReferenceNode.modify(
                asteriskToken,
                typeName,
                semicolonToken);
    }

    @Override
    public Node transform(ServiceBodyNode serviceBodyNode) {
        Token openBraceToken = modifyToken(serviceBodyNode.openBraceToken());
        NodeList<Node> resources = modifyNodeList(serviceBodyNode.resources());
        Token closeBraceToken = modifyToken(serviceBodyNode.closeBraceToken());
        return serviceBodyNode.modify(
                openBraceToken,
                resources,
                closeBraceToken);
    }

    @Override
    public Node transform(AnnotationNode annotationNode) {
        Token atToken = modifyToken(annotationNode.atToken());
        Node annotReference = modifyNode(annotationNode.annotReference());
        MappingConstructorExpressionNode annotValue = modifyNode(annotationNode.annotValue().orElse(null));
        return annotationNode.modify(
                atToken,
                annotReference,
                annotValue);
    }

    @Override
    public Node transform(MetadataNode metadataNode) {
        Node documentationString = modifyNode(metadataNode.documentationString().orElse(null));
        NodeList<AnnotationNode> annotations = modifyNodeList(metadataNode.annotations());
        return metadataNode.modify(
                documentationString,
                annotations);
    }

    @Override
    public Node transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        MetadataNode metadata = modifyNode(moduleVariableDeclarationNode.metadata());
        Token finalKeyword = modifyToken(moduleVariableDeclarationNode.finalKeyword().orElse(null));
        Node typeName = modifyNode(moduleVariableDeclarationNode.typeName());
        Token variableName = modifyToken(moduleVariableDeclarationNode.variableName());
        Token equalsToken = modifyToken(moduleVariableDeclarationNode.equalsToken());
        ExpressionNode initializer = modifyNode(moduleVariableDeclarationNode.initializer());
        Token semicolonToken = modifyToken(moduleVariableDeclarationNode.semicolonToken());
        return moduleVariableDeclarationNode.modify(
                metadata,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public Node transform(TypeTestExpressionNode typeTestExpressionNode) {
        ExpressionNode expression = modifyNode(typeTestExpressionNode.expression());
        Token isKeyword = modifyToken(typeTestExpressionNode.isKeyword());
        Node typeDescriptor = modifyNode(typeTestExpressionNode.typeDescriptor());
        return typeTestExpressionNode.modify(
                expression,
                isKeyword,
                typeDescriptor);
    }

    @Override
    public Node transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        ExpressionNode expression = modifyNode(remoteMethodCallActionNode.expression());
        Token rightArrowToken = modifyToken(remoteMethodCallActionNode.rightArrowToken());
        Token methodName = modifyToken(remoteMethodCallActionNode.methodName());
        Token openParenToken = modifyToken(remoteMethodCallActionNode.openParenToken());
        NodeList<FunctionArgumentNode> arguments = modifyNodeList(remoteMethodCallActionNode.arguments());
        Token closeParenToken = modifyToken(remoteMethodCallActionNode.closeParenToken());
        return remoteMethodCallActionNode.modify(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public Node transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        Token parameterizedType = modifyToken(parameterizedTypeDescriptorNode.parameterizedType());
        Token ltToken = modifyToken(parameterizedTypeDescriptorNode.ltToken());
        Node typeNode = modifyNode(parameterizedTypeDescriptorNode.typeNode());
        Token gtToken = modifyToken(parameterizedTypeDescriptorNode.gtToken());
        return parameterizedTypeDescriptorNode.modify(
                parameterizedType,
                ltToken,
                typeNode,
                gtToken);
    }

    @Override
    public Node transform(NilLiteralNode nilLiteralNode) {
        Token openParenToken = modifyToken(nilLiteralNode.openParenToken());
        Token closeParenToken = modifyToken(nilLiteralNode.closeParenToken());
        return nilLiteralNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public Node transform(AnnotationDeclarationNode annotationDeclarationNode) {
        MetadataNode metadata = modifyNode(annotationDeclarationNode.metadata());
        Token visibilityQualifier = modifyToken(annotationDeclarationNode.visibilityQualifier());
        Token constKeyword = modifyToken(annotationDeclarationNode.constKeyword());
        Token annotationKeyword = modifyToken(annotationDeclarationNode.annotationKeyword());
        Node typeDescriptor = modifyNode(annotationDeclarationNode.typeDescriptor());
        Token annotationTag = modifyToken(annotationDeclarationNode.annotationTag());
        Token onKeyword = modifyToken(annotationDeclarationNode.onKeyword());
        SeparatedNodeList<Node> attachPoints = modifySeparatedNodeList(annotationDeclarationNode.attachPoints());
        Token semicolonToken = modifyToken(annotationDeclarationNode.semicolonToken());
        return annotationDeclarationNode.modify(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints,
                semicolonToken);
    }

    @Override
    public Node transform(AnnotationAttachPointNode annotationAttachPointNode) {
        Token sourceKeyword = modifyToken(annotationAttachPointNode.sourceKeyword());
        Token firstIdent = modifyToken(annotationAttachPointNode.firstIdent());
        Token secondIdent = modifyToken(annotationAttachPointNode.secondIdent());
        return annotationAttachPointNode.modify(
                sourceKeyword,
                firstIdent,
                secondIdent);
    }

    @Override
    public Node transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = modifyToken(xMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri = modifyNode(xMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword = modifyToken(xMLNamespaceDeclarationNode.asKeyword());
        IdentifierToken namespacePrefix = modifyNode(xMLNamespaceDeclarationNode.namespacePrefix());
        Token semicolonToken = modifyToken(xMLNamespaceDeclarationNode.semicolonToken());
        return xMLNamespaceDeclarationNode.modify(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    @Override
    public Node transform(FunctionBodyBlockNode functionBodyBlockNode) {
        Token openBraceToken = modifyToken(functionBodyBlockNode.openBraceToken());
        NamedWorkerDeclarator namedWorkerDeclarator = modifyNode(functionBodyBlockNode.namedWorkerDeclarator().orElse(null));
        NodeList<StatementNode> statements = modifyNodeList(functionBodyBlockNode.statements());
        Token closeBraceToken = modifyToken(functionBodyBlockNode.closeBraceToken());
        return functionBodyBlockNode.modify(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken);
    }

    @Override
    public Node transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(namedWorkerDeclarationNode.annotations());
        Token workerKeyword = modifyToken(namedWorkerDeclarationNode.workerKeyword());
        IdentifierToken workerName = modifyNode(namedWorkerDeclarationNode.workerName());
        Node returnTypeDesc = modifyNode(namedWorkerDeclarationNode.returnTypeDesc().orElse(null));
        BlockStatementNode workerBody = modifyNode(namedWorkerDeclarationNode.workerBody());
        return namedWorkerDeclarationNode.modify(
                annotations,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    @Override
    public Node transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        NodeList<StatementNode> workerInitStatements = modifyNodeList(namedWorkerDeclarator.workerInitStatements());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations = modifyNodeList(namedWorkerDeclarator.namedWorkerDeclarations());
        return namedWorkerDeclarator.modify(
                workerInitStatements,
                namedWorkerDeclarations);
    }

    @Override
    public Node transform(DocumentationStringNode documentationStringNode) {
        NodeList<Token> documentationLines = modifyNodeList(documentationStringNode.documentationLines());
        return documentationStringNode.modify(
                documentationLines);
    }

    @Override
    public Node transform(BasicLiteralNode basicLiteralNode) {
        Token literalToken = modifyToken(basicLiteralNode.literalToken());
        return basicLiteralNode.modify(
                basicLiteralNode.kind(),
                literalToken);
    }

    @Override
    public Node transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        Token name = modifyToken(simpleNameReferenceNode.name());
        return simpleNameReferenceNode.modify(
                name);
    }

    @Override
    public Node transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Token modulePrefix = modifyToken(qualifiedNameReferenceNode.modulePrefix());
        Node colon = modifyNode(qualifiedNameReferenceNode.colon());
        IdentifierToken identifier = modifyNode(qualifiedNameReferenceNode.identifier());
        return qualifiedNameReferenceNode.modify(
                modulePrefix,
                colon,
                identifier);
    }

    @Override
    public Node transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        Token name = modifyToken(builtinSimpleNameReferenceNode.name());
        return builtinSimpleNameReferenceNode.modify(
                builtinSimpleNameReferenceNode.kind(),
                name);
    }

    @Override
    public Node transform(TrapExpressionNode trapExpressionNode) {
        Token trapKeyword = modifyToken(trapExpressionNode.trapKeyword());
        ExpressionNode expression = modifyNode(trapExpressionNode.expression());
        return trapExpressionNode.modify(
                trapKeyword,
                expression);
    }

    @Override
    public Node transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Token openBracket = modifyToken(listConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> expressions = modifySeparatedNodeList(listConstructorExpressionNode.expressions());
        Token closeBracket = modifyToken(listConstructorExpressionNode.closeBracket());
        return listConstructorExpressionNode.modify(
                openBracket,
                expressions,
                closeBracket);
    }

    @Override
    public Node transform(TypeCastExpressionNode typeCastExpressionNode) {
        Token ltToken = modifyToken(typeCastExpressionNode.ltToken());
        TypeCastParamNode typeCastParam = modifyNode(typeCastExpressionNode.typeCastParam());
        Token gtToken = modifyToken(typeCastExpressionNode.gtToken());
        ExpressionNode expression = modifyNode(typeCastExpressionNode.expression());
        return typeCastExpressionNode.modify(
                ltToken,
                typeCastParam,
                gtToken,
                expression);
    }

    @Override
    public Node transform(TypeCastParamNode typeCastParamNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(typeCastParamNode.annotations());
        Node type = modifyNode(typeCastParamNode.type());
        return typeCastParamNode.modify(
                annotations,
                type);
    }

    @Override
    public Node transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        Node leftTypeDesc = modifyNode(unionTypeDescriptorNode.leftTypeDesc());
        Token pipeToken = modifyToken(unionTypeDescriptorNode.pipeToken());
        Node rightTypeDesc = modifyNode(unionTypeDescriptorNode.rightTypeDesc());
        return unionTypeDescriptorNode.modify(
                leftTypeDesc,
                pipeToken,
                rightTypeDesc);
    }

    @Override
    public Node transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        Token tableKeyword = modifyToken(tableConstructorExpressionNode.tableKeyword());
        KeySpecifierNode KeySpecifier = modifyNode(tableConstructorExpressionNode.KeySpecifier());
        Token openBracket = modifyToken(tableConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> mappingConstructors = modifySeparatedNodeList(tableConstructorExpressionNode.mappingConstructors());
        Token closeBracket = modifyToken(tableConstructorExpressionNode.closeBracket());
        return tableConstructorExpressionNode.modify(
                tableKeyword,
                KeySpecifier,
                openBracket,
                mappingConstructors,
                closeBracket);
    }

    @Override
    public Node transform(KeySpecifierNode keySpecifierNode) {
        Token keyKeyword = modifyToken(keySpecifierNode.keyKeyword());
        Token openParenToken = modifyToken(keySpecifierNode.openParenToken());
        SeparatedNodeList<Node> fieldNames = modifySeparatedNodeList(keySpecifierNode.fieldNames());
        Token closeParenToken = modifyToken(keySpecifierNode.closeParenToken());
        return keySpecifierNode.modify(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    @Override
    public Node transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        Token errorKeywordToken = modifyToken(errorTypeDescriptorNode.errorKeywordToken());
        Node errorTypeParamsNode = modifyNode(errorTypeDescriptorNode.errorTypeParamsNode());
        return errorTypeDescriptorNode.modify(
                errorKeywordToken,
                errorTypeParamsNode);
    }

    @Override
    public Node transform(ErrorTypeParamsNode errorTypeParamsNode) {
        Token ltToken = modifyToken(errorTypeParamsNode.ltToken());
        Node parameter = modifyNode(errorTypeParamsNode.parameter());
        Token gtToken = modifyToken(errorTypeParamsNode.gtToken());
        return errorTypeParamsNode.modify(
                ltToken,
                parameter,
                gtToken);
    }

    @Override
    public Node transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        Token streamKeywordToken = modifyToken(streamTypeDescriptorNode.streamKeywordToken());
        Node streamTypeParamsNode = modifyNode(streamTypeDescriptorNode.streamTypeParamsNode());
        return streamTypeDescriptorNode.modify(
                streamKeywordToken,
                streamTypeParamsNode);
    }

    @Override
    public Node transform(StreamTypeParamsNode streamTypeParamsNode) {
        Token ltToken = modifyToken(streamTypeParamsNode.ltToken());
        Node leftTypeDescNode = modifyNode(streamTypeParamsNode.leftTypeDescNode());
        Token commaToken = modifyToken(streamTypeParamsNode.commaToken());
        Node rightTypeDescNode = modifyNode(streamTypeParamsNode.rightTypeDescNode());
        Token gtToken = modifyToken(streamTypeParamsNode.gtToken());
        return streamTypeParamsNode.modify(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    @Override
    public Node transform(LetExpressionNode letExpressionNode) {
        Token letKeyword = modifyToken(letExpressionNode.letKeyword());
        SeparatedNodeList<Node> letVarDeclarations = modifySeparatedNodeList(letExpressionNode.letVarDeclarations());
        Token inKeyword = modifyToken(letExpressionNode.inKeyword());
        ExpressionNode expression = modifyNode(letExpressionNode.expression());
        return letExpressionNode.modify(
                letKeyword,
                letVarDeclarations,
                inKeyword,
                expression);
    }

    @Override
    public Node transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(letVariableDeclarationNode.annotations());
        Node typeName = modifyNode(letVariableDeclarationNode.typeName());
        Token variableName = modifyToken(letVariableDeclarationNode.variableName());
        Token equalsToken = modifyToken(letVariableDeclarationNode.equalsToken());
        ExpressionNode expression = modifyNode(letVariableDeclarationNode.expression());
        return letVariableDeclarationNode.modify(
                annotations,
                typeName,
                variableName,
                equalsToken,
                expression);
    }

    @Override
    public Node transform(TemplateExpressionNode templateExpressionNode) {
        Token type = modifyToken(templateExpressionNode.type());
        Token startBacktick = modifyToken(templateExpressionNode.startBacktick());
        NodeList<TemplateMemberNode> content = modifyNodeList(templateExpressionNode.content());
        Token endBacktick = modifyToken(templateExpressionNode.endBacktick());
        return templateExpressionNode.modify(
                templateExpressionNode.kind(),
                type,
                startBacktick,
                content,
                endBacktick);
    }

    @Override
    public Node transform(XMLElementNode xMLElementNode) {
        XMLStartTagNode startTag = modifyNode(xMLElementNode.startTag());
        NodeList<XMLItemNode> content = modifyNodeList(xMLElementNode.content());
        XMLEndTagNode endTag = modifyNode(xMLElementNode.endTag());
        return xMLElementNode.modify(
                startTag,
                content,
                endTag);
    }

    @Override
    public Node transform(XMLStartTagNode xMLStartTagNode) {
        Token ltToken = modifyToken(xMLStartTagNode.ltToken());
        XMLNameNode name = modifyNode(xMLStartTagNode.name());
        NodeList<XMLAttributeNode> attributes = modifyNodeList(xMLStartTagNode.attributes());
        Token getToken = modifyToken(xMLStartTagNode.getToken());
        return xMLStartTagNode.modify(
                ltToken,
                name,
                attributes,
                getToken);
    }

    @Override
    public Node transform(XMLEndTagNode xMLEndTagNode) {
        Token ltToken = modifyToken(xMLEndTagNode.ltToken());
        Token slashToken = modifyToken(xMLEndTagNode.slashToken());
        XMLNameNode name = modifyNode(xMLEndTagNode.name());
        Token getToken = modifyToken(xMLEndTagNode.getToken());
        return xMLEndTagNode.modify(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    @Override
    public Node transform(XMLSimpleNameNode xMLSimpleNameNode) {
        XMLSimpleNameNode name = modifyNode(xMLSimpleNameNode.name());
        return xMLSimpleNameNode.modify(
                name);
    }

    @Override
    public Node transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        XMLSimpleNameNode prefix = modifyNode(xMLQualifiedNameNode.prefix());
        Token colon = modifyToken(xMLQualifiedNameNode.colon());
        XMLSimpleNameNode name = modifyNode(xMLQualifiedNameNode.name());
        return xMLQualifiedNameNode.modify(
                prefix,
                colon,
                name);
    }

    @Override
    public Node transform(XMLEmptyElementNode xMLEmptyElementNode) {
        Token ltToken = modifyToken(xMLEmptyElementNode.ltToken());
        XMLNameNode name = modifyNode(xMLEmptyElementNode.name());
        NodeList<XMLAttributeNode> attributes = modifyNodeList(xMLEmptyElementNode.attributes());
        Token slashToken = modifyToken(xMLEmptyElementNode.slashToken());
        Token getToken = modifyToken(xMLEmptyElementNode.getToken());
        return xMLEmptyElementNode.modify(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    @Override
    public Node transform(InterpolationNode interpolationNode) {
        Token interpolationStartToken = modifyToken(interpolationNode.interpolationStartToken());
        ExpressionNode expression = modifyNode(interpolationNode.expression());
        Token interpolationEndToken = modifyToken(interpolationNode.interpolationEndToken());
        return interpolationNode.modify(
                interpolationStartToken,
                expression,
                interpolationEndToken);
    }

    @Override
    public Node transform(XMLTextNode xMLTextNode) {
        Token content = modifyToken(xMLTextNode.content());
        return xMLTextNode.modify(
                content);
    }

    @Override
    public Node transform(XMLAttributeNode xMLAttributeNode) {
        XMLNameNode attributeName = modifyNode(xMLAttributeNode.attributeName());
        Token equalToken = modifyToken(xMLAttributeNode.equalToken());
        XMLAttributeValue value = modifyNode(xMLAttributeNode.value());
        return xMLAttributeNode.modify(
                attributeName,
                equalToken,
                value);
    }

    @Override
    public Node transform(XMLAttributeValue xMLAttributeValue) {
        Token startQuote = modifyToken(xMLAttributeValue.startQuote());
        NodeList<Node> value = modifyNodeList(xMLAttributeValue.value());
        Token endQuote = modifyToken(xMLAttributeValue.endQuote());
        return xMLAttributeValue.modify(
                startQuote,
                value,
                endQuote);
    }

    @Override
    public Node transform(XMLComment xMLComment) {
        Token commentStart = modifyToken(xMLComment.commentStart());
        NodeList<Node> content = modifyNodeList(xMLComment.content());
        Token commentEnd = modifyToken(xMLComment.commentEnd());
        return xMLComment.modify(
                commentStart,
                content,
                commentEnd);
    }

    @Override
    public Node transform(XMLProcessingInstruction xMLProcessingInstruction) {
        Token piStart = modifyToken(xMLProcessingInstruction.piStart());
        XMLNameNode target = modifyNode(xMLProcessingInstruction.target());
        NodeList<Node> data = modifyNodeList(xMLProcessingInstruction.data());
        Token piEnd = modifyToken(xMLProcessingInstruction.piEnd());
        return xMLProcessingInstruction.modify(
                piStart,
                target,
                data,
                piEnd);
    }

    @Override
    public Node transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        Token functionKeyword = modifyToken(functionTypeDescriptorNode.functionKeyword());
        FunctionSignatureNode functionSignature = modifyNode(functionTypeDescriptorNode.functionSignature());
        return functionTypeDescriptorNode.modify(
                functionKeyword,
                functionSignature);
    }

    @Override
    public Node transform(AnonymousFunctionExpressionNode anonymousFunctionExpressionNode) {
        NodeList<AnnotationNode> annotations = modifyNodeList(anonymousFunctionExpressionNode.annotations());
        Token functionKeyword = modifyToken(anonymousFunctionExpressionNode.functionKeyword());
        FunctionSignatureNode functionSignature = modifyNode(anonymousFunctionExpressionNode.functionSignature());
        Node functionBody = modifyNode(anonymousFunctionExpressionNode.functionBody());
        return anonymousFunctionExpressionNode.modify(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    @Override
    public Node transform(FunctionSignatureNode functionSignatureNode) {
        Token openParenToken = modifyToken(functionSignatureNode.openParenToken());
        NodeList<ParameterNode> parameters = modifyNodeList(functionSignatureNode.parameters());
        Token closeParenToken = modifyToken(functionSignatureNode.closeParenToken());
        ReturnTypeDescriptorNode returnTypeDesc = modifyNode(functionSignatureNode.returnTypeDesc().orElse(null));
        return functionSignatureNode.modify(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc);
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
    protected Node transformSyntaxNode(Node node) {
        return node;
    }

    protected <T extends Node> NodeList<T> modifyNodeList(NodeList<T> nodeList) {
        return modifyGenericNodeList(nodeList, NodeList::new);
    }

    protected <T extends Node> SeparatedNodeList<T> modifySeparatedNodeList(SeparatedNodeList<T> nodeList) {
        return modifyGenericNodeList(nodeList, SeparatedNodeList::new);
    }

    private <T extends Node, N extends NodeList<T>> N modifyGenericNodeList(
            N nodeList,
            Function<NonTerminalNode, N> nodeListCreator) {
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
        return nodeListCreator.apply(stNodeList.createUnlinkedFacade());
    }

    protected <T extends Token> T modifyToken(T token) {
        if (token == null) {
            return null;
        }
        // TODO
        return (T) token.apply(this);
    }

    protected <T extends Node> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }
}

