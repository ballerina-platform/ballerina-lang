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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;

import java.util.function.Function;

/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class TreeModifier extends NodeTransformer<Node> {

    @Override
    public ModulePartNode transform(
            ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports =
                modifyNodeList(modulePartNode.imports());
        NodeList<ModuleMemberDeclarationNode> members =
                modifyNodeList(modulePartNode.members());
        Token eofToken =
                modifyToken(modulePartNode.eofToken());
        return modulePartNode.modify(
                imports,
                members,
                eofToken);
    }

    @Override
    public FunctionDefinitionNode transform(
            FunctionDefinitionNode functionDefinitionNode) {
        MetadataNode metadata =
                modifyNode(functionDefinitionNode.metadata().orElse(null));
        NodeList<Token> qualifierList =
                modifyNodeList(functionDefinitionNode.qualifierList());
        Token functionKeyword =
                modifyToken(functionDefinitionNode.functionKeyword());
        IdentifierToken functionName =
                modifyNode(functionDefinitionNode.functionName());
        NodeList<Node> relativeResourcePath =
                modifyNodeList(functionDefinitionNode.relativeResourcePath());
        FunctionSignatureNode functionSignature =
                modifyNode(functionDefinitionNode.functionSignature());
        FunctionBodyNode functionBody =
                modifyNode(functionDefinitionNode.functionBody());
        return functionDefinitionNode.modify(
                functionDefinitionNode.kind(),
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    @Override
    public ImportDeclarationNode transform(
            ImportDeclarationNode importDeclarationNode) {
        Token importKeyword =
                modifyToken(importDeclarationNode.importKeyword());
        ImportOrgNameNode orgName =
                modifyNode(importDeclarationNode.orgName().orElse(null));
        SeparatedNodeList<IdentifierToken> moduleName =
                modifySeparatedNodeList(importDeclarationNode.moduleName());
        ImportPrefixNode prefix =
                modifyNode(importDeclarationNode.prefix().orElse(null));
        Token semicolon =
                modifyToken(importDeclarationNode.semicolon());
        return importDeclarationNode.modify(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon);
    }

    @Override
    public ListenerDeclarationNode transform(
            ListenerDeclarationNode listenerDeclarationNode) {
        MetadataNode metadata =
                modifyNode(listenerDeclarationNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(listenerDeclarationNode.visibilityQualifier().orElse(null));
        Token listenerKeyword =
                modifyToken(listenerDeclarationNode.listenerKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(listenerDeclarationNode.typeDescriptor().orElse(null));
        Token variableName =
                modifyToken(listenerDeclarationNode.variableName());
        Token equalsToken =
                modifyToken(listenerDeclarationNode.equalsToken());
        Node initializer =
                modifyNode(listenerDeclarationNode.initializer());
        Token semicolonToken =
                modifyToken(listenerDeclarationNode.semicolonToken());
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
    public TypeDefinitionNode transform(
            TypeDefinitionNode typeDefinitionNode) {
        MetadataNode metadata =
                modifyNode(typeDefinitionNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(typeDefinitionNode.visibilityQualifier().orElse(null));
        Token typeKeyword =
                modifyToken(typeDefinitionNode.typeKeyword());
        Token typeName =
                modifyToken(typeDefinitionNode.typeName());
        Node typeDescriptor =
                modifyNode(typeDefinitionNode.typeDescriptor());
        Token semicolonToken =
                modifyToken(typeDefinitionNode.semicolonToken());
        return typeDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public ServiceDeclarationNode transform(
            ServiceDeclarationNode serviceDeclarationNode) {
        MetadataNode metadata =
                modifyNode(serviceDeclarationNode.metadata().orElse(null));
        NodeList<Token> qualifiers =
                modifyNodeList(serviceDeclarationNode.qualifiers());
        Token serviceKeyword =
                modifyToken(serviceDeclarationNode.serviceKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(serviceDeclarationNode.typeDescriptor().orElse(null));
        NodeList<Node> absoluteResourcePath =
                modifyNodeList(serviceDeclarationNode.absoluteResourcePath());
        Token onKeyword =
                modifyToken(serviceDeclarationNode.onKeyword());
        SeparatedNodeList<ExpressionNode> expressions =
                modifySeparatedNodeList(serviceDeclarationNode.expressions());
        Token openBraceToken =
                modifyToken(serviceDeclarationNode.openBraceToken());
        NodeList<Node> members =
                modifyNodeList(serviceDeclarationNode.members());
        Token closeBraceToken =
                modifyToken(serviceDeclarationNode.closeBraceToken());
        Token semicolonToken =
                modifyToken(serviceDeclarationNode.semicolonToken().orElse(null));
        return serviceDeclarationNode.modify(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken);
    }

    @Override
    public AssignmentStatementNode transform(
            AssignmentStatementNode assignmentStatementNode) {
        Node varRef =
                modifyNode(assignmentStatementNode.varRef());
        Token equalsToken =
                modifyToken(assignmentStatementNode.equalsToken());
        ExpressionNode expression =
                modifyNode(assignmentStatementNode.expression());
        Token semicolonToken =
                modifyToken(assignmentStatementNode.semicolonToken());
        return assignmentStatementNode.modify(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public CompoundAssignmentStatementNode transform(
            CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        ExpressionNode lhsExpression =
                modifyNode(compoundAssignmentStatementNode.lhsExpression());
        Token binaryOperator =
                modifyToken(compoundAssignmentStatementNode.binaryOperator());
        Token equalsToken =
                modifyToken(compoundAssignmentStatementNode.equalsToken());
        ExpressionNode rhsExpression =
                modifyNode(compoundAssignmentStatementNode.rhsExpression());
        Token semicolonToken =
                modifyToken(compoundAssignmentStatementNode.semicolonToken());
        return compoundAssignmentStatementNode.modify(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    @Override
    public VariableDeclarationNode transform(
            VariableDeclarationNode variableDeclarationNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(variableDeclarationNode.annotations());
        Token finalKeyword =
                modifyToken(variableDeclarationNode.finalKeyword().orElse(null));
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(variableDeclarationNode.typedBindingPattern());
        Token equalsToken =
                modifyToken(variableDeclarationNode.equalsToken().orElse(null));
        ExpressionNode initializer =
                modifyNode(variableDeclarationNode.initializer().orElse(null));
        Token semicolonToken =
                modifyToken(variableDeclarationNode.semicolonToken());
        return variableDeclarationNode.modify(
                annotations,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public BlockStatementNode transform(
            BlockStatementNode blockStatementNode) {
        Token openBraceToken =
                modifyToken(blockStatementNode.openBraceToken());
        NodeList<StatementNode> statements =
                modifyNodeList(blockStatementNode.statements());
        Token closeBraceToken =
                modifyToken(blockStatementNode.closeBraceToken());
        return blockStatementNode.modify(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    @Override
    public BreakStatementNode transform(
            BreakStatementNode breakStatementNode) {
        Token breakToken =
                modifyToken(breakStatementNode.breakToken());
        Token semicolonToken =
                modifyToken(breakStatementNode.semicolonToken());
        return breakStatementNode.modify(
                breakToken,
                semicolonToken);
    }

    @Override
    public FailStatementNode transform(
            FailStatementNode failStatementNode) {
        Token failKeyword =
                modifyToken(failStatementNode.failKeyword());
        ExpressionNode expression =
                modifyNode(failStatementNode.expression());
        Token semicolonToken =
                modifyToken(failStatementNode.semicolonToken());
        return failStatementNode.modify(
                failKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public ExpressionStatementNode transform(
            ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression =
                modifyNode(expressionStatementNode.expression());
        Token semicolonToken =
                modifyToken(expressionStatementNode.semicolonToken());
        return expressionStatementNode.modify(
                expressionStatementNode.kind(),
                expression,
                semicolonToken);
    }

    @Override
    public ContinueStatementNode transform(
            ContinueStatementNode continueStatementNode) {
        Token continueToken =
                modifyToken(continueStatementNode.continueToken());
        Token semicolonToken =
                modifyToken(continueStatementNode.semicolonToken());
        return continueStatementNode.modify(
                continueToken,
                semicolonToken);
    }

    @Override
    public ExternalFunctionBodyNode transform(
            ExternalFunctionBodyNode externalFunctionBodyNode) {
        Token equalsToken =
                modifyToken(externalFunctionBodyNode.equalsToken());
        NodeList<AnnotationNode> annotations =
                modifyNodeList(externalFunctionBodyNode.annotations());
        Token externalKeyword =
                modifyToken(externalFunctionBodyNode.externalKeyword());
        Token semicolonToken =
                modifyToken(externalFunctionBodyNode.semicolonToken());
        return externalFunctionBodyNode.modify(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    @Override
    public IfElseStatementNode transform(
            IfElseStatementNode ifElseStatementNode) {
        Token ifKeyword =
                modifyToken(ifElseStatementNode.ifKeyword());
        ExpressionNode condition =
                modifyNode(ifElseStatementNode.condition());
        BlockStatementNode ifBody =
                modifyNode(ifElseStatementNode.ifBody());
        Node elseBody =
                modifyNode(ifElseStatementNode.elseBody().orElse(null));
        return ifElseStatementNode.modify(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    @Override
    public ElseBlockNode transform(
            ElseBlockNode elseBlockNode) {
        Token elseKeyword =
                modifyToken(elseBlockNode.elseKeyword());
        StatementNode elseBody =
                modifyNode(elseBlockNode.elseBody());
        return elseBlockNode.modify(
                elseKeyword,
                elseBody);
    }

    @Override
    public WhileStatementNode transform(
            WhileStatementNode whileStatementNode) {
        Token whileKeyword =
                modifyToken(whileStatementNode.whileKeyword());
        ExpressionNode condition =
                modifyNode(whileStatementNode.condition());
        BlockStatementNode whileBody =
                modifyNode(whileStatementNode.whileBody());
        OnFailClauseNode onFailClause =
                modifyNode(whileStatementNode.onFailClause().orElse(null));
        return whileStatementNode.modify(
                whileKeyword,
                condition,
                whileBody,
                onFailClause);
    }

    @Override
    public PanicStatementNode transform(
            PanicStatementNode panicStatementNode) {
        Token panicKeyword =
                modifyToken(panicStatementNode.panicKeyword());
        ExpressionNode expression =
                modifyNode(panicStatementNode.expression());
        Token semicolonToken =
                modifyToken(panicStatementNode.semicolonToken());
        return panicStatementNode.modify(
                panicKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public ReturnStatementNode transform(
            ReturnStatementNode returnStatementNode) {
        Token returnKeyword =
                modifyToken(returnStatementNode.returnKeyword());
        ExpressionNode expression =
                modifyNode(returnStatementNode.expression().orElse(null));
        Token semicolonToken =
                modifyToken(returnStatementNode.semicolonToken());
        return returnStatementNode.modify(
                returnKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public LocalTypeDefinitionStatementNode transform(
            LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(localTypeDefinitionStatementNode.annotations());
        Token typeKeyword =
                modifyToken(localTypeDefinitionStatementNode.typeKeyword());
        Node typeName =
                modifyNode(localTypeDefinitionStatementNode.typeName());
        Node typeDescriptor =
                modifyNode(localTypeDefinitionStatementNode.typeDescriptor());
        Token semicolonToken =
                modifyToken(localTypeDefinitionStatementNode.semicolonToken());
        return localTypeDefinitionStatementNode.modify(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public LockStatementNode transform(
            LockStatementNode lockStatementNode) {
        Token lockKeyword =
                modifyToken(lockStatementNode.lockKeyword());
        BlockStatementNode blockStatement =
                modifyNode(lockStatementNode.blockStatement());
        OnFailClauseNode onFailClause =
                modifyNode(lockStatementNode.onFailClause().orElse(null));
        return lockStatementNode.modify(
                lockKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public ForkStatementNode transform(
            ForkStatementNode forkStatementNode) {
        Token forkKeyword =
                modifyToken(forkStatementNode.forkKeyword());
        Token openBraceToken =
                modifyToken(forkStatementNode.openBraceToken());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                modifyNodeList(forkStatementNode.namedWorkerDeclarations());
        Token closeBraceToken =
                modifyToken(forkStatementNode.closeBraceToken());
        return forkStatementNode.modify(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations,
                closeBraceToken);
    }

    @Override
    public ForEachStatementNode transform(
            ForEachStatementNode forEachStatementNode) {
        Token forEachKeyword =
                modifyToken(forEachStatementNode.forEachKeyword());
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(forEachStatementNode.typedBindingPattern());
        Token inKeyword =
                modifyToken(forEachStatementNode.inKeyword());
        Node actionOrExpressionNode =
                modifyNode(forEachStatementNode.actionOrExpressionNode());
        BlockStatementNode blockStatement =
                modifyNode(forEachStatementNode.blockStatement());
        OnFailClauseNode onFailClause =
                modifyNode(forEachStatementNode.onFailClause().orElse(null));
        return forEachStatementNode.modify(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement,
                onFailClause);
    }

    @Override
    public BinaryExpressionNode transform(
            BinaryExpressionNode binaryExpressionNode) {
        Node lhsExpr =
                modifyNode(binaryExpressionNode.lhsExpr());
        Token operator =
                modifyToken(binaryExpressionNode.operator());
        Node rhsExpr =
                modifyNode(binaryExpressionNode.rhsExpr());
        return binaryExpressionNode.modify(
                binaryExpressionNode.kind(),
                lhsExpr,
                operator,
                rhsExpr);
    }

    @Override
    public BracedExpressionNode transform(
            BracedExpressionNode bracedExpressionNode) {
        Token openParen =
                modifyToken(bracedExpressionNode.openParen());
        ExpressionNode expression =
                modifyNode(bracedExpressionNode.expression());
        Token closeParen =
                modifyToken(bracedExpressionNode.closeParen());
        return bracedExpressionNode.modify(
                bracedExpressionNode.kind(),
                openParen,
                expression,
                closeParen);
    }

    @Override
    public CheckExpressionNode transform(
            CheckExpressionNode checkExpressionNode) {
        Token checkKeyword =
                modifyToken(checkExpressionNode.checkKeyword());
        ExpressionNode expression =
                modifyNode(checkExpressionNode.expression());
        return checkExpressionNode.modify(
                checkExpressionNode.kind(),
                checkKeyword,
                expression);
    }

    @Override
    public FieldAccessExpressionNode transform(
            FieldAccessExpressionNode fieldAccessExpressionNode) {
        ExpressionNode expression =
                modifyNode(fieldAccessExpressionNode.expression());
        Token dotToken =
                modifyToken(fieldAccessExpressionNode.dotToken());
        NameReferenceNode fieldName =
                modifyNode(fieldAccessExpressionNode.fieldName());
        return fieldAccessExpressionNode.modify(
                expression,
                dotToken,
                fieldName);
    }

    @Override
    public FunctionCallExpressionNode transform(
            FunctionCallExpressionNode functionCallExpressionNode) {
        NameReferenceNode functionName =
                modifyNode(functionCallExpressionNode.functionName());
        Token openParenToken =
                modifyToken(functionCallExpressionNode.openParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments =
                modifySeparatedNodeList(functionCallExpressionNode.arguments());
        Token closeParenToken =
                modifyToken(functionCallExpressionNode.closeParenToken());
        return functionCallExpressionNode.modify(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public MethodCallExpressionNode transform(
            MethodCallExpressionNode methodCallExpressionNode) {
        ExpressionNode expression =
                modifyNode(methodCallExpressionNode.expression());
        Token dotToken =
                modifyToken(methodCallExpressionNode.dotToken());
        NameReferenceNode methodName =
                modifyNode(methodCallExpressionNode.methodName());
        Token openParenToken =
                modifyToken(methodCallExpressionNode.openParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments =
                modifySeparatedNodeList(methodCallExpressionNode.arguments());
        Token closeParenToken =
                modifyToken(methodCallExpressionNode.closeParenToken());
        return methodCallExpressionNode.modify(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        Token openBrace =
                modifyToken(mappingConstructorExpressionNode.openBrace());
        SeparatedNodeList<MappingFieldNode> fields =
                modifySeparatedNodeList(mappingConstructorExpressionNode.fields());
        Token closeBrace =
                modifyToken(mappingConstructorExpressionNode.closeBrace());
        return mappingConstructorExpressionNode.modify(
                openBrace,
                fields,
                closeBrace);
    }

    @Override
    public IndexedExpressionNode transform(
            IndexedExpressionNode indexedExpressionNode) {
        ExpressionNode containerExpression =
                modifyNode(indexedExpressionNode.containerExpression());
        Token openBracket =
                modifyToken(indexedExpressionNode.openBracket());
        SeparatedNodeList<ExpressionNode> keyExpression =
                modifySeparatedNodeList(indexedExpressionNode.keyExpression());
        Token closeBracket =
                modifyToken(indexedExpressionNode.closeBracket());
        return indexedExpressionNode.modify(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    @Override
    public TypeofExpressionNode transform(
            TypeofExpressionNode typeofExpressionNode) {
        Token typeofKeyword =
                modifyToken(typeofExpressionNode.typeofKeyword());
        ExpressionNode expression =
                modifyNode(typeofExpressionNode.expression());
        return typeofExpressionNode.modify(
                typeofKeyword,
                expression);
    }

    @Override
    public UnaryExpressionNode transform(
            UnaryExpressionNode unaryExpressionNode) {
        Token unaryOperator =
                modifyToken(unaryExpressionNode.unaryOperator());
        ExpressionNode expression =
                modifyNode(unaryExpressionNode.expression());
        return unaryExpressionNode.modify(
                unaryOperator,
                expression);
    }

    @Override
    public ComputedNameFieldNode transform(
            ComputedNameFieldNode computedNameFieldNode) {
        Token openBracket =
                modifyToken(computedNameFieldNode.openBracket());
        ExpressionNode fieldNameExpr =
                modifyNode(computedNameFieldNode.fieldNameExpr());
        Token closeBracket =
                modifyToken(computedNameFieldNode.closeBracket());
        Token colonToken =
                modifyToken(computedNameFieldNode.colonToken());
        ExpressionNode valueExpr =
                modifyNode(computedNameFieldNode.valueExpr());
        return computedNameFieldNode.modify(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    @Override
    public ConstantDeclarationNode transform(
            ConstantDeclarationNode constantDeclarationNode) {
        MetadataNode metadata =
                modifyNode(constantDeclarationNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(constantDeclarationNode.visibilityQualifier().orElse(null));
        Token constKeyword =
                modifyToken(constantDeclarationNode.constKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(constantDeclarationNode.typeDescriptor().orElse(null));
        Token variableName =
                modifyToken(constantDeclarationNode.variableName());
        Token equalsToken =
                modifyToken(constantDeclarationNode.equalsToken());
        Node initializer =
                modifyNode(constantDeclarationNode.initializer());
        Token semicolonToken =
                modifyToken(constantDeclarationNode.semicolonToken());
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
    public DefaultableParameterNode transform(
            DefaultableParameterNode defaultableParameterNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(defaultableParameterNode.annotations());
        Node typeName =
                modifyNode(defaultableParameterNode.typeName());
        Token paramName =
                modifyToken(defaultableParameterNode.paramName().orElse(null));
        Token equalsToken =
                modifyToken(defaultableParameterNode.equalsToken());
        Node expression =
                modifyNode(defaultableParameterNode.expression());
        return defaultableParameterNode.modify(
                annotations,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    @Override
    public RequiredParameterNode transform(
            RequiredParameterNode requiredParameterNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(requiredParameterNode.annotations());
        Node typeName =
                modifyNode(requiredParameterNode.typeName());
        Token paramName =
                modifyToken(requiredParameterNode.paramName().orElse(null));
        return requiredParameterNode.modify(
                annotations,
                typeName,
                paramName);
    }

    @Override
    public IncludedRecordParameterNode transform(
            IncludedRecordParameterNode includedRecordParameterNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(includedRecordParameterNode.annotations());
        Token asteriskToken =
                modifyToken(includedRecordParameterNode.asteriskToken());
        Node typeName =
                modifyNode(includedRecordParameterNode.typeName());
        Token paramName =
                modifyToken(includedRecordParameterNode.paramName().orElse(null));
        return includedRecordParameterNode.modify(
                annotations,
                asteriskToken,
                typeName,
                paramName);
    }

    @Override
    public RestParameterNode transform(
            RestParameterNode restParameterNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(restParameterNode.annotations());
        Node typeName =
                modifyNode(restParameterNode.typeName());
        Token ellipsisToken =
                modifyToken(restParameterNode.ellipsisToken());
        Token paramName =
                modifyToken(restParameterNode.paramName().orElse(null));
        return restParameterNode.modify(
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    @Override
    public ImportOrgNameNode transform(
            ImportOrgNameNode importOrgNameNode) {
        Token orgName =
                modifyToken(importOrgNameNode.orgName());
        Token slashToken =
                modifyToken(importOrgNameNode.slashToken());
        return importOrgNameNode.modify(
                orgName,
                slashToken);
    }

    @Override
    public ImportPrefixNode transform(
            ImportPrefixNode importPrefixNode) {
        Token asKeyword =
                modifyToken(importPrefixNode.asKeyword());
        Token prefix =
                modifyToken(importPrefixNode.prefix());
        return importPrefixNode.modify(
                asKeyword,
                prefix);
    }

    @Override
    public SpecificFieldNode transform(
            SpecificFieldNode specificFieldNode) {
        Token readonlyKeyword =
                modifyToken(specificFieldNode.readonlyKeyword().orElse(null));
        Node fieldName =
                modifyNode(specificFieldNode.fieldName());
        Token colon =
                modifyToken(specificFieldNode.colon().orElse(null));
        ExpressionNode valueExpr =
                modifyNode(specificFieldNode.valueExpr().orElse(null));
        return specificFieldNode.modify(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr);
    }

    @Override
    public SpreadFieldNode transform(
            SpreadFieldNode spreadFieldNode) {
        Token ellipsis =
                modifyToken(spreadFieldNode.ellipsis());
        ExpressionNode valueExpr =
                modifyNode(spreadFieldNode.valueExpr());
        return spreadFieldNode.modify(
                ellipsis,
                valueExpr);
    }

    @Override
    public NamedArgumentNode transform(
            NamedArgumentNode namedArgumentNode) {
        SimpleNameReferenceNode argumentName =
                modifyNode(namedArgumentNode.argumentName());
        Token equalsToken =
                modifyToken(namedArgumentNode.equalsToken());
        ExpressionNode expression =
                modifyNode(namedArgumentNode.expression());
        return namedArgumentNode.modify(
                argumentName,
                equalsToken,
                expression);
    }

    @Override
    public PositionalArgumentNode transform(
            PositionalArgumentNode positionalArgumentNode) {
        ExpressionNode expression =
                modifyNode(positionalArgumentNode.expression());
        return positionalArgumentNode.modify(
                expression);
    }

    @Override
    public RestArgumentNode transform(
            RestArgumentNode restArgumentNode) {
        Token ellipsis =
                modifyToken(restArgumentNode.ellipsis());
        ExpressionNode expression =
                modifyNode(restArgumentNode.expression());
        return restArgumentNode.modify(
                ellipsis,
                expression);
    }

    @Override
    public InferredTypedescDefaultNode transform(
            InferredTypedescDefaultNode inferredTypedescDefaultNode) {
        Token ltToken =
                modifyToken(inferredTypedescDefaultNode.ltToken());
        Token gtToken =
                modifyToken(inferredTypedescDefaultNode.gtToken());
        return inferredTypedescDefaultNode.modify(
                ltToken,
                gtToken);
    }

    @Override
    public ObjectTypeDescriptorNode transform(
            ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        NodeList<Token> objectTypeQualifiers =
                modifyNodeList(objectTypeDescriptorNode.objectTypeQualifiers());
        Token objectKeyword =
                modifyToken(objectTypeDescriptorNode.objectKeyword());
        Token openBrace =
                modifyToken(objectTypeDescriptorNode.openBrace());
        NodeList<Node> members =
                modifyNodeList(objectTypeDescriptorNode.members());
        Token closeBrace =
                modifyToken(objectTypeDescriptorNode.closeBrace());
        return objectTypeDescriptorNode.modify(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    @Override
    public ObjectConstructorExpressionNode transform(
            ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(objectConstructorExpressionNode.annotations());
        NodeList<Token> objectTypeQualifiers =
                modifyNodeList(objectConstructorExpressionNode.objectTypeQualifiers());
        Token objectKeyword =
                modifyToken(objectConstructorExpressionNode.objectKeyword());
        TypeDescriptorNode typeReference =
                modifyNode(objectConstructorExpressionNode.typeReference().orElse(null));
        Token openBraceToken =
                modifyToken(objectConstructorExpressionNode.openBraceToken());
        NodeList<Node> members =
                modifyNodeList(objectConstructorExpressionNode.members());
        Token closeBraceToken =
                modifyToken(objectConstructorExpressionNode.closeBraceToken());
        return objectConstructorExpressionNode.modify(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken);
    }

    @Override
    public RecordTypeDescriptorNode transform(
            RecordTypeDescriptorNode recordTypeDescriptorNode) {
        Token recordKeyword =
                modifyToken(recordTypeDescriptorNode.recordKeyword());
        Token bodyStartDelimiter =
                modifyToken(recordTypeDescriptorNode.bodyStartDelimiter());
        NodeList<Node> fields =
                modifyNodeList(recordTypeDescriptorNode.fields());
        RecordRestDescriptorNode recordRestDescriptor =
                modifyNode(recordTypeDescriptorNode.recordRestDescriptor().orElse(null));
        Token bodyEndDelimiter =
                modifyToken(recordTypeDescriptorNode.bodyEndDelimiter());
        return recordTypeDescriptorNode.modify(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter);
    }

    @Override
    public ReturnTypeDescriptorNode transform(
            ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        Token returnsKeyword =
                modifyToken(returnTypeDescriptorNode.returnsKeyword());
        NodeList<AnnotationNode> annotations =
                modifyNodeList(returnTypeDescriptorNode.annotations());
        Node type =
                modifyNode(returnTypeDescriptorNode.type());
        return returnTypeDescriptorNode.modify(
                returnsKeyword,
                annotations,
                type);
    }

    @Override
    public NilTypeDescriptorNode transform(
            NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken =
                modifyToken(nilTypeDescriptorNode.openParenToken());
        Token closeParenToken =
                modifyToken(nilTypeDescriptorNode.closeParenToken());
        return nilTypeDescriptorNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public OptionalTypeDescriptorNode transform(
            OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        Node typeDescriptor =
                modifyNode(optionalTypeDescriptorNode.typeDescriptor());
        Token questionMarkToken =
                modifyToken(optionalTypeDescriptorNode.questionMarkToken());
        return optionalTypeDescriptorNode.modify(
                typeDescriptor,
                questionMarkToken);
    }

    @Override
    public ObjectFieldNode transform(
            ObjectFieldNode objectFieldNode) {
        MetadataNode metadata =
                modifyNode(objectFieldNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(objectFieldNode.visibilityQualifier().orElse(null));
        NodeList<Token> qualifierList =
                modifyNodeList(objectFieldNode.qualifierList());
        Node typeName =
                modifyNode(objectFieldNode.typeName());
        Token fieldName =
                modifyToken(objectFieldNode.fieldName());
        Token equalsToken =
                modifyToken(objectFieldNode.equalsToken().orElse(null));
        ExpressionNode expression =
                modifyNode(objectFieldNode.expression().orElse(null));
        Token semicolonToken =
                modifyToken(objectFieldNode.semicolonToken());
        return objectFieldNode.modify(
                metadata,
                visibilityQualifier,
                qualifierList,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public RecordFieldNode transform(
            RecordFieldNode recordFieldNode) {
        MetadataNode metadata =
                modifyNode(recordFieldNode.metadata().orElse(null));
        Token readonlyKeyword =
                modifyToken(recordFieldNode.readonlyKeyword().orElse(null));
        Node typeName =
                modifyNode(recordFieldNode.typeName());
        Token fieldName =
                modifyToken(recordFieldNode.fieldName());
        Token questionMarkToken =
                modifyToken(recordFieldNode.questionMarkToken().orElse(null));
        Token semicolonToken =
                modifyToken(recordFieldNode.semicolonToken());
        return recordFieldNode.modify(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    @Override
    public RecordFieldWithDefaultValueNode transform(
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        MetadataNode metadata =
                modifyNode(recordFieldWithDefaultValueNode.metadata().orElse(null));
        Token readonlyKeyword =
                modifyToken(recordFieldWithDefaultValueNode.readonlyKeyword().orElse(null));
        Node typeName =
                modifyNode(recordFieldWithDefaultValueNode.typeName());
        Token fieldName =
                modifyToken(recordFieldWithDefaultValueNode.fieldName());
        Token equalsToken =
                modifyToken(recordFieldWithDefaultValueNode.equalsToken());
        ExpressionNode expression =
                modifyNode(recordFieldWithDefaultValueNode.expression());
        Token semicolonToken =
                modifyToken(recordFieldWithDefaultValueNode.semicolonToken());
        return recordFieldWithDefaultValueNode.modify(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public RecordRestDescriptorNode transform(
            RecordRestDescriptorNode recordRestDescriptorNode) {
        Node typeName =
                modifyNode(recordRestDescriptorNode.typeName());
        Token ellipsisToken =
                modifyToken(recordRestDescriptorNode.ellipsisToken());
        Token semicolonToken =
                modifyToken(recordRestDescriptorNode.semicolonToken());
        return recordRestDescriptorNode.modify(
                typeName,
                ellipsisToken,
                semicolonToken);
    }

    @Override
    public TypeReferenceNode transform(
            TypeReferenceNode typeReferenceNode) {
        Token asteriskToken =
                modifyToken(typeReferenceNode.asteriskToken());
        Node typeName =
                modifyNode(typeReferenceNode.typeName());
        Token semicolonToken =
                modifyToken(typeReferenceNode.semicolonToken());
        return typeReferenceNode.modify(
                asteriskToken,
                typeName,
                semicolonToken);
    }

    @Override
    public AnnotationNode transform(
            AnnotationNode annotationNode) {
        Token atToken =
                modifyToken(annotationNode.atToken());
        Node annotReference =
                modifyNode(annotationNode.annotReference());
        MappingConstructorExpressionNode annotValue =
                modifyNode(annotationNode.annotValue().orElse(null));
        return annotationNode.modify(
                atToken,
                annotReference,
                annotValue);
    }

    @Override
    public MetadataNode transform(
            MetadataNode metadataNode) {
        Node documentationString =
                modifyNode(metadataNode.documentationString().orElse(null));
        NodeList<AnnotationNode> annotations =
                modifyNodeList(metadataNode.annotations());
        return metadataNode.modify(
                documentationString,
                annotations);
    }

    @Override
    public ModuleVariableDeclarationNode transform(
            ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        MetadataNode metadata =
                modifyNode(moduleVariableDeclarationNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(moduleVariableDeclarationNode.visibilityQualifier().orElse(null));
        NodeList<Token> qualifiers =
                modifyNodeList(moduleVariableDeclarationNode.qualifiers());
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(moduleVariableDeclarationNode.typedBindingPattern());
        Token equalsToken =
                modifyToken(moduleVariableDeclarationNode.equalsToken().orElse(null));
        ExpressionNode initializer =
                modifyNode(moduleVariableDeclarationNode.initializer().orElse(null));
        Token semicolonToken =
                modifyToken(moduleVariableDeclarationNode.semicolonToken());
        return moduleVariableDeclarationNode.modify(
                metadata,
                visibilityQualifier,
                qualifiers,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public TypeTestExpressionNode transform(
            TypeTestExpressionNode typeTestExpressionNode) {
        ExpressionNode expression =
                modifyNode(typeTestExpressionNode.expression());
        Token isKeyword =
                modifyToken(typeTestExpressionNode.isKeyword());
        Node typeDescriptor =
                modifyNode(typeTestExpressionNode.typeDescriptor());
        return typeTestExpressionNode.modify(
                expression,
                isKeyword,
                typeDescriptor);
    }

    @Override
    public RemoteMethodCallActionNode transform(
            RemoteMethodCallActionNode remoteMethodCallActionNode) {
        ExpressionNode expression =
                modifyNode(remoteMethodCallActionNode.expression());
        Token rightArrowToken =
                modifyToken(remoteMethodCallActionNode.rightArrowToken());
        SimpleNameReferenceNode methodName =
                modifyNode(remoteMethodCallActionNode.methodName());
        Token openParenToken =
                modifyToken(remoteMethodCallActionNode.openParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments =
                modifySeparatedNodeList(remoteMethodCallActionNode.arguments());
        Token closeParenToken =
                modifyToken(remoteMethodCallActionNode.closeParenToken());
        return remoteMethodCallActionNode.modify(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public MapTypeDescriptorNode transform(
            MapTypeDescriptorNode mapTypeDescriptorNode) {
        Token mapKeywordToken =
                modifyToken(mapTypeDescriptorNode.mapKeywordToken());
        TypeParameterNode mapTypeParamsNode =
                modifyNode(mapTypeDescriptorNode.mapTypeParamsNode());
        return mapTypeDescriptorNode.modify(
                mapKeywordToken,
                mapTypeParamsNode);
    }

    @Override
    public NilLiteralNode transform(
            NilLiteralNode nilLiteralNode) {
        Token openParenToken =
                modifyToken(nilLiteralNode.openParenToken());
        Token closeParenToken =
                modifyToken(nilLiteralNode.closeParenToken());
        return nilLiteralNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public AnnotationDeclarationNode transform(
            AnnotationDeclarationNode annotationDeclarationNode) {
        MetadataNode metadata =
                modifyNode(annotationDeclarationNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(annotationDeclarationNode.visibilityQualifier().orElse(null));
        Token constKeyword =
                modifyToken(annotationDeclarationNode.constKeyword().orElse(null));
        Token annotationKeyword =
                modifyToken(annotationDeclarationNode.annotationKeyword());
        Node typeDescriptor =
                modifyNode(annotationDeclarationNode.typeDescriptor().orElse(null));
        Token annotationTag =
                modifyToken(annotationDeclarationNode.annotationTag());
        Token onKeyword =
                modifyToken(annotationDeclarationNode.onKeyword().orElse(null));
        SeparatedNodeList<Node> attachPoints =
                modifySeparatedNodeList(annotationDeclarationNode.attachPoints());
        Token semicolonToken =
                modifyToken(annotationDeclarationNode.semicolonToken());
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
    public AnnotationAttachPointNode transform(
            AnnotationAttachPointNode annotationAttachPointNode) {
        Token sourceKeyword =
                modifyToken(annotationAttachPointNode.sourceKeyword().orElse(null));
        NodeList<Token> identifiers =
                modifyNodeList(annotationAttachPointNode.identifiers());
        return annotationAttachPointNode.modify(
                sourceKeyword,
                identifiers);
    }

    @Override
    public XMLNamespaceDeclarationNode transform(
            XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        Token xmlnsKeyword =
                modifyToken(xMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri =
                modifyNode(xMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword =
                modifyToken(xMLNamespaceDeclarationNode.asKeyword().orElse(null));
        IdentifierToken namespacePrefix =
                modifyNode(xMLNamespaceDeclarationNode.namespacePrefix().orElse(null));
        Token semicolonToken =
                modifyToken(xMLNamespaceDeclarationNode.semicolonToken());
        return xMLNamespaceDeclarationNode.modify(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        Token xmlnsKeyword =
                modifyToken(moduleXMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri =
                modifyNode(moduleXMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword =
                modifyToken(moduleXMLNamespaceDeclarationNode.asKeyword().orElse(null));
        IdentifierToken namespacePrefix =
                modifyNode(moduleXMLNamespaceDeclarationNode.namespacePrefix().orElse(null));
        Token semicolonToken =
                modifyToken(moduleXMLNamespaceDeclarationNode.semicolonToken());
        return moduleXMLNamespaceDeclarationNode.modify(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    @Override
    public FunctionBodyBlockNode transform(
            FunctionBodyBlockNode functionBodyBlockNode) {
        Token openBraceToken =
                modifyToken(functionBodyBlockNode.openBraceToken());
        NamedWorkerDeclarator namedWorkerDeclarator =
                modifyNode(functionBodyBlockNode.namedWorkerDeclarator().orElse(null));
        NodeList<StatementNode> statements =
                modifyNodeList(functionBodyBlockNode.statements());
        Token closeBraceToken =
                modifyToken(functionBodyBlockNode.closeBraceToken());
        Token semicolonToken =
                modifyToken(functionBodyBlockNode.semicolonToken().orElse(null));
        return functionBodyBlockNode.modify(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken);
    }

    @Override
    public NamedWorkerDeclarationNode transform(
            NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(namedWorkerDeclarationNode.annotations());
        Token transactionalKeyword =
                modifyToken(namedWorkerDeclarationNode.transactionalKeyword().orElse(null));
        Token workerKeyword =
                modifyToken(namedWorkerDeclarationNode.workerKeyword());
        IdentifierToken workerName =
                modifyNode(namedWorkerDeclarationNode.workerName());
        Node returnTypeDesc =
                modifyNode(namedWorkerDeclarationNode.returnTypeDesc().orElse(null));
        BlockStatementNode workerBody =
                modifyNode(namedWorkerDeclarationNode.workerBody());
        return namedWorkerDeclarationNode.modify(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    @Override
    public NamedWorkerDeclarator transform(
            NamedWorkerDeclarator namedWorkerDeclarator) {
        NodeList<StatementNode> workerInitStatements =
                modifyNodeList(namedWorkerDeclarator.workerInitStatements());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                modifyNodeList(namedWorkerDeclarator.namedWorkerDeclarations());
        return namedWorkerDeclarator.modify(
                workerInitStatements,
                namedWorkerDeclarations);
    }

    @Override
    public BasicLiteralNode transform(
            BasicLiteralNode basicLiteralNode) {
        Token literalToken =
                modifyToken(basicLiteralNode.literalToken());
        return basicLiteralNode.modify(
                basicLiteralNode.kind(),
                literalToken);
    }

    @Override
    public SimpleNameReferenceNode transform(
            SimpleNameReferenceNode simpleNameReferenceNode) {
        Token name =
                modifyToken(simpleNameReferenceNode.name());
        return simpleNameReferenceNode.modify(
                name);
    }

    @Override
    public QualifiedNameReferenceNode transform(
            QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Token modulePrefix =
                modifyToken(qualifiedNameReferenceNode.modulePrefix());
        Node colon =
                modifyNode(qualifiedNameReferenceNode.colon());
        IdentifierToken identifier =
                modifyNode(qualifiedNameReferenceNode.identifier());
        return qualifiedNameReferenceNode.modify(
                modulePrefix,
                colon,
                identifier);
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        Token name =
                modifyToken(builtinSimpleNameReferenceNode.name());
        return builtinSimpleNameReferenceNode.modify(
                builtinSimpleNameReferenceNode.kind(),
                name);
    }

    @Override
    public TrapExpressionNode transform(
            TrapExpressionNode trapExpressionNode) {
        Token trapKeyword =
                modifyToken(trapExpressionNode.trapKeyword());
        ExpressionNode expression =
                modifyNode(trapExpressionNode.expression());
        return trapExpressionNode.modify(
                trapExpressionNode.kind(),
                trapKeyword,
                expression);
    }

    @Override
    public ListConstructorExpressionNode transform(
            ListConstructorExpressionNode listConstructorExpressionNode) {
        Token openBracket =
                modifyToken(listConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> expressions =
                modifySeparatedNodeList(listConstructorExpressionNode.expressions());
        Token closeBracket =
                modifyToken(listConstructorExpressionNode.closeBracket());
        return listConstructorExpressionNode.modify(
                openBracket,
                expressions,
                closeBracket);
    }

    @Override
    public TypeCastExpressionNode transform(
            TypeCastExpressionNode typeCastExpressionNode) {
        Token ltToken =
                modifyToken(typeCastExpressionNode.ltToken());
        TypeCastParamNode typeCastParam =
                modifyNode(typeCastExpressionNode.typeCastParam());
        Token gtToken =
                modifyToken(typeCastExpressionNode.gtToken());
        ExpressionNode expression =
                modifyNode(typeCastExpressionNode.expression());
        return typeCastExpressionNode.modify(
                ltToken,
                typeCastParam,
                gtToken,
                expression);
    }

    @Override
    public TypeCastParamNode transform(
            TypeCastParamNode typeCastParamNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(typeCastParamNode.annotations());
        Node type =
                modifyNode(typeCastParamNode.type().orElse(null));
        return typeCastParamNode.modify(
                annotations,
                type);
    }

    @Override
    public UnionTypeDescriptorNode transform(
            UnionTypeDescriptorNode unionTypeDescriptorNode) {
        TypeDescriptorNode leftTypeDesc =
                modifyNode(unionTypeDescriptorNode.leftTypeDesc());
        Token pipeToken =
                modifyToken(unionTypeDescriptorNode.pipeToken());
        TypeDescriptorNode rightTypeDesc =
                modifyNode(unionTypeDescriptorNode.rightTypeDesc());
        return unionTypeDescriptorNode.modify(
                leftTypeDesc,
                pipeToken,
                rightTypeDesc);
    }

    @Override
    public TableConstructorExpressionNode transform(
            TableConstructorExpressionNode tableConstructorExpressionNode) {
        Token tableKeyword =
                modifyToken(tableConstructorExpressionNode.tableKeyword());
        KeySpecifierNode keySpecifier =
                modifyNode(tableConstructorExpressionNode.keySpecifier().orElse(null));
        Token openBracket =
                modifyToken(tableConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> rows =
                modifySeparatedNodeList(tableConstructorExpressionNode.rows());
        Token closeBracket =
                modifyToken(tableConstructorExpressionNode.closeBracket());
        return tableConstructorExpressionNode.modify(
                tableKeyword,
                keySpecifier,
                openBracket,
                rows,
                closeBracket);
    }

    @Override
    public KeySpecifierNode transform(
            KeySpecifierNode keySpecifierNode) {
        Token keyKeyword =
                modifyToken(keySpecifierNode.keyKeyword());
        Token openParenToken =
                modifyToken(keySpecifierNode.openParenToken());
        SeparatedNodeList<IdentifierToken> fieldNames =
                modifySeparatedNodeList(keySpecifierNode.fieldNames());
        Token closeParenToken =
                modifyToken(keySpecifierNode.closeParenToken());
        return keySpecifierNode.modify(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    @Override
    public StreamTypeDescriptorNode transform(
            StreamTypeDescriptorNode streamTypeDescriptorNode) {
        Token streamKeywordToken =
                modifyToken(streamTypeDescriptorNode.streamKeywordToken());
        Node streamTypeParamsNode =
                modifyNode(streamTypeDescriptorNode.streamTypeParamsNode().orElse(null));
        return streamTypeDescriptorNode.modify(
                streamKeywordToken,
                streamTypeParamsNode);
    }

    @Override
    public StreamTypeParamsNode transform(
            StreamTypeParamsNode streamTypeParamsNode) {
        Token ltToken =
                modifyToken(streamTypeParamsNode.ltToken());
        Node leftTypeDescNode =
                modifyNode(streamTypeParamsNode.leftTypeDescNode());
        Token commaToken =
                modifyToken(streamTypeParamsNode.commaToken().orElse(null));
        Node rightTypeDescNode =
                modifyNode(streamTypeParamsNode.rightTypeDescNode().orElse(null));
        Token gtToken =
                modifyToken(streamTypeParamsNode.gtToken());
        return streamTypeParamsNode.modify(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    @Override
    public LetExpressionNode transform(
            LetExpressionNode letExpressionNode) {
        Token letKeyword =
                modifyToken(letExpressionNode.letKeyword());
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                modifySeparatedNodeList(letExpressionNode.letVarDeclarations());
        Token inKeyword =
                modifyToken(letExpressionNode.inKeyword());
        ExpressionNode expression =
                modifyNode(letExpressionNode.expression());
        return letExpressionNode.modify(
                letKeyword,
                letVarDeclarations,
                inKeyword,
                expression);
    }

    @Override
    public LetVariableDeclarationNode transform(
            LetVariableDeclarationNode letVariableDeclarationNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(letVariableDeclarationNode.annotations());
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(letVariableDeclarationNode.typedBindingPattern());
        Token equalsToken =
                modifyToken(letVariableDeclarationNode.equalsToken());
        ExpressionNode expression =
                modifyNode(letVariableDeclarationNode.expression());
        return letVariableDeclarationNode.modify(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression);
    }

    @Override
    public TemplateExpressionNode transform(
            TemplateExpressionNode templateExpressionNode) {
        Token type =
                modifyToken(templateExpressionNode.type().orElse(null));
        Token startBacktick =
                modifyToken(templateExpressionNode.startBacktick());
        NodeList<Node> content =
                modifyNodeList(templateExpressionNode.content());
        Token endBacktick =
                modifyToken(templateExpressionNode.endBacktick());
        return templateExpressionNode.modify(
                templateExpressionNode.kind(),
                type,
                startBacktick,
                content,
                endBacktick);
    }

    @Override
    public XMLElementNode transform(
            XMLElementNode xMLElementNode) {
        XMLStartTagNode startTag =
                modifyNode(xMLElementNode.startTag());
        NodeList<XMLItemNode> content =
                modifyNodeList(xMLElementNode.content());
        XMLEndTagNode endTag =
                modifyNode(xMLElementNode.endTag());
        return xMLElementNode.modify(
                startTag,
                content,
                endTag);
    }

    @Override
    public XMLStartTagNode transform(
            XMLStartTagNode xMLStartTagNode) {
        Token ltToken =
                modifyToken(xMLStartTagNode.ltToken());
        XMLNameNode name =
                modifyNode(xMLStartTagNode.name());
        NodeList<XMLAttributeNode> attributes =
                modifyNodeList(xMLStartTagNode.attributes());
        Token getToken =
                modifyToken(xMLStartTagNode.getToken());
        return xMLStartTagNode.modify(
                ltToken,
                name,
                attributes,
                getToken);
    }

    @Override
    public XMLEndTagNode transform(
            XMLEndTagNode xMLEndTagNode) {
        Token ltToken =
                modifyToken(xMLEndTagNode.ltToken());
        Token slashToken =
                modifyToken(xMLEndTagNode.slashToken());
        XMLNameNode name =
                modifyNode(xMLEndTagNode.name());
        Token getToken =
                modifyToken(xMLEndTagNode.getToken());
        return xMLEndTagNode.modify(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    @Override
    public XMLSimpleNameNode transform(
            XMLSimpleNameNode xMLSimpleNameNode) {
        Token name =
                modifyToken(xMLSimpleNameNode.name());
        return xMLSimpleNameNode.modify(
                name);
    }

    @Override
    public XMLQualifiedNameNode transform(
            XMLQualifiedNameNode xMLQualifiedNameNode) {
        XMLSimpleNameNode prefix =
                modifyNode(xMLQualifiedNameNode.prefix());
        Token colon =
                modifyToken(xMLQualifiedNameNode.colon());
        XMLSimpleNameNode name =
                modifyNode(xMLQualifiedNameNode.name());
        return xMLQualifiedNameNode.modify(
                prefix,
                colon,
                name);
    }

    @Override
    public XMLEmptyElementNode transform(
            XMLEmptyElementNode xMLEmptyElementNode) {
        Token ltToken =
                modifyToken(xMLEmptyElementNode.ltToken());
        XMLNameNode name =
                modifyNode(xMLEmptyElementNode.name());
        NodeList<XMLAttributeNode> attributes =
                modifyNodeList(xMLEmptyElementNode.attributes());
        Token slashToken =
                modifyToken(xMLEmptyElementNode.slashToken());
        Token getToken =
                modifyToken(xMLEmptyElementNode.getToken());
        return xMLEmptyElementNode.modify(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    @Override
    public InterpolationNode transform(
            InterpolationNode interpolationNode) {
        Token interpolationStartToken =
                modifyToken(interpolationNode.interpolationStartToken());
        ExpressionNode expression =
                modifyNode(interpolationNode.expression());
        Token interpolationEndToken =
                modifyToken(interpolationNode.interpolationEndToken());
        return interpolationNode.modify(
                interpolationStartToken,
                expression,
                interpolationEndToken);
    }

    @Override
    public XMLTextNode transform(
            XMLTextNode xMLTextNode) {
        Token content =
                modifyToken(xMLTextNode.content());
        return xMLTextNode.modify(
                content);
    }

    @Override
    public XMLAttributeNode transform(
            XMLAttributeNode xMLAttributeNode) {
        XMLNameNode attributeName =
                modifyNode(xMLAttributeNode.attributeName());
        Token equalToken =
                modifyToken(xMLAttributeNode.equalToken());
        XMLAttributeValue value =
                modifyNode(xMLAttributeNode.value());
        return xMLAttributeNode.modify(
                attributeName,
                equalToken,
                value);
    }

    @Override
    public XMLAttributeValue transform(
            XMLAttributeValue xMLAttributeValue) {
        Token startQuote =
                modifyToken(xMLAttributeValue.startQuote());
        NodeList<Node> value =
                modifyNodeList(xMLAttributeValue.value());
        Token endQuote =
                modifyToken(xMLAttributeValue.endQuote());
        return xMLAttributeValue.modify(
                startQuote,
                value,
                endQuote);
    }

    @Override
    public XMLComment transform(
            XMLComment xMLComment) {
        Token commentStart =
                modifyToken(xMLComment.commentStart());
        NodeList<Node> content =
                modifyNodeList(xMLComment.content());
        Token commentEnd =
                modifyToken(xMLComment.commentEnd());
        return xMLComment.modify(
                commentStart,
                content,
                commentEnd);
    }

    @Override
    public XMLCDATANode transform(
            XMLCDATANode xMLCDATANode) {
        Token cdataStart =
                modifyToken(xMLCDATANode.cdataStart());
        NodeList<Node> content =
                modifyNodeList(xMLCDATANode.content());
        Token cdataEnd =
                modifyToken(xMLCDATANode.cdataEnd());
        return xMLCDATANode.modify(
                cdataStart,
                content,
                cdataEnd);
    }

    @Override
    public XMLProcessingInstruction transform(
            XMLProcessingInstruction xMLProcessingInstruction) {
        Token piStart =
                modifyToken(xMLProcessingInstruction.piStart());
        XMLNameNode target =
                modifyNode(xMLProcessingInstruction.target());
        NodeList<Node> data =
                modifyNodeList(xMLProcessingInstruction.data());
        Token piEnd =
                modifyToken(xMLProcessingInstruction.piEnd());
        return xMLProcessingInstruction.modify(
                piStart,
                target,
                data,
                piEnd);
    }

    @Override
    public TableTypeDescriptorNode transform(
            TableTypeDescriptorNode tableTypeDescriptorNode) {
        Token tableKeywordToken =
                modifyToken(tableTypeDescriptorNode.tableKeywordToken());
        Node rowTypeParameterNode =
                modifyNode(tableTypeDescriptorNode.rowTypeParameterNode());
        Node keyConstraintNode =
                modifyNode(tableTypeDescriptorNode.keyConstraintNode().orElse(null));
        return tableTypeDescriptorNode.modify(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode);
    }

    @Override
    public TypeParameterNode transform(
            TypeParameterNode typeParameterNode) {
        Token ltToken =
                modifyToken(typeParameterNode.ltToken());
        TypeDescriptorNode typeNode =
                modifyNode(typeParameterNode.typeNode());
        Token gtToken =
                modifyToken(typeParameterNode.gtToken());
        return typeParameterNode.modify(
                ltToken,
                typeNode,
                gtToken);
    }

    @Override
    public KeyTypeConstraintNode transform(
            KeyTypeConstraintNode keyTypeConstraintNode) {
        Token keyKeywordToken =
                modifyToken(keyTypeConstraintNode.keyKeywordToken());
        Node typeParameterNode =
                modifyNode(keyTypeConstraintNode.typeParameterNode());
        return keyTypeConstraintNode.modify(
                keyKeywordToken,
                typeParameterNode);
    }

    @Override
    public FunctionTypeDescriptorNode transform(
            FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        NodeList<Token> qualifierList =
                modifyNodeList(functionTypeDescriptorNode.qualifierList());
        Token functionKeyword =
                modifyToken(functionTypeDescriptorNode.functionKeyword());
        FunctionSignatureNode functionSignature =
                modifyNode(functionTypeDescriptorNode.functionSignature().orElse(null));
        return functionTypeDescriptorNode.modify(
                qualifierList,
                functionKeyword,
                functionSignature);
    }

    @Override
    public FunctionSignatureNode transform(
            FunctionSignatureNode functionSignatureNode) {
        Token openParenToken =
                modifyToken(functionSignatureNode.openParenToken());
        SeparatedNodeList<ParameterNode> parameters =
                modifySeparatedNodeList(functionSignatureNode.parameters());
        Token closeParenToken =
                modifyToken(functionSignatureNode.closeParenToken());
        ReturnTypeDescriptorNode returnTypeDesc =
                modifyNode(functionSignatureNode.returnTypeDesc().orElse(null));
        return functionSignatureNode.modify(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc);
    }

    @Override
    public ExplicitAnonymousFunctionExpressionNode transform(
            ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(explicitAnonymousFunctionExpressionNode.annotations());
        NodeList<Token> qualifierList =
                modifyNodeList(explicitAnonymousFunctionExpressionNode.qualifierList());
        Token functionKeyword =
                modifyToken(explicitAnonymousFunctionExpressionNode.functionKeyword());
        FunctionSignatureNode functionSignature =
                modifyNode(explicitAnonymousFunctionExpressionNode.functionSignature());
        FunctionBodyNode functionBody =
                modifyNode(explicitAnonymousFunctionExpressionNode.functionBody());
        return explicitAnonymousFunctionExpressionNode.modify(
                annotations,
                qualifierList,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    @Override
    public ExpressionFunctionBodyNode transform(
            ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        Token rightDoubleArrow =
                modifyToken(expressionFunctionBodyNode.rightDoubleArrow());
        ExpressionNode expression =
                modifyNode(expressionFunctionBodyNode.expression());
        Token semicolon =
                modifyToken(expressionFunctionBodyNode.semicolon().orElse(null));
        return expressionFunctionBodyNode.modify(
                rightDoubleArrow,
                expression,
                semicolon);
    }

    @Override
    public TupleTypeDescriptorNode transform(
            TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        Token openBracketToken =
                modifyToken(tupleTypeDescriptorNode.openBracketToken());
        SeparatedNodeList<Node> memberTypeDesc =
                modifySeparatedNodeList(tupleTypeDescriptorNode.memberTypeDesc());
        Token closeBracketToken =
                modifyToken(tupleTypeDescriptorNode.closeBracketToken());
        return tupleTypeDescriptorNode.modify(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken);
    }

    @Override
    public ParenthesisedTypeDescriptorNode transform(
            ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        Token openParenToken =
                modifyToken(parenthesisedTypeDescriptorNode.openParenToken());
        TypeDescriptorNode typedesc =
                modifyNode(parenthesisedTypeDescriptorNode.typedesc());
        Token closeParenToken =
                modifyToken(parenthesisedTypeDescriptorNode.closeParenToken());
        return parenthesisedTypeDescriptorNode.modify(
                openParenToken,
                typedesc,
                closeParenToken);
    }

    @Override
    public ExplicitNewExpressionNode transform(
            ExplicitNewExpressionNode explicitNewExpressionNode) {
        Token newKeyword =
                modifyToken(explicitNewExpressionNode.newKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(explicitNewExpressionNode.typeDescriptor());
        ParenthesizedArgList parenthesizedArgList =
                modifyNode(explicitNewExpressionNode.parenthesizedArgList());
        return explicitNewExpressionNode.modify(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList);
    }

    @Override
    public ImplicitNewExpressionNode transform(
            ImplicitNewExpressionNode implicitNewExpressionNode) {
        Token newKeyword =
                modifyToken(implicitNewExpressionNode.newKeyword());
        ParenthesizedArgList parenthesizedArgList =
                modifyNode(implicitNewExpressionNode.parenthesizedArgList().orElse(null));
        return implicitNewExpressionNode.modify(
                newKeyword,
                parenthesizedArgList);
    }

    @Override
    public ParenthesizedArgList transform(
            ParenthesizedArgList parenthesizedArgList) {
        Token openParenToken =
                modifyToken(parenthesizedArgList.openParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments =
                modifySeparatedNodeList(parenthesizedArgList.arguments());
        Token closeParenToken =
                modifyToken(parenthesizedArgList.closeParenToken());
        return parenthesizedArgList.modify(
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public QueryConstructTypeNode transform(
            QueryConstructTypeNode queryConstructTypeNode) {
        Token keyword =
                modifyToken(queryConstructTypeNode.keyword());
        KeySpecifierNode keySpecifier =
                modifyNode(queryConstructTypeNode.keySpecifier().orElse(null));
        return queryConstructTypeNode.modify(
                keyword,
                keySpecifier);
    }

    @Override
    public FromClauseNode transform(
            FromClauseNode fromClauseNode) {
        Token fromKeyword =
                modifyToken(fromClauseNode.fromKeyword());
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(fromClauseNode.typedBindingPattern());
        Token inKeyword =
                modifyToken(fromClauseNode.inKeyword());
        ExpressionNode expression =
                modifyNode(fromClauseNode.expression());
        return fromClauseNode.modify(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression);
    }

    @Override
    public WhereClauseNode transform(
            WhereClauseNode whereClauseNode) {
        Token whereKeyword =
                modifyToken(whereClauseNode.whereKeyword());
        ExpressionNode expression =
                modifyNode(whereClauseNode.expression());
        return whereClauseNode.modify(
                whereKeyword,
                expression);
    }

    @Override
    public LetClauseNode transform(
            LetClauseNode letClauseNode) {
        Token letKeyword =
                modifyToken(letClauseNode.letKeyword());
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                modifySeparatedNodeList(letClauseNode.letVarDeclarations());
        return letClauseNode.modify(
                letKeyword,
                letVarDeclarations);
    }

    @Override
    public JoinClauseNode transform(
            JoinClauseNode joinClauseNode) {
        Token outerKeyword =
                modifyToken(joinClauseNode.outerKeyword().orElse(null));
        Token joinKeyword =
                modifyToken(joinClauseNode.joinKeyword());
        TypedBindingPatternNode typedBindingPattern =
                modifyNode(joinClauseNode.typedBindingPattern());
        Token inKeyword =
                modifyToken(joinClauseNode.inKeyword());
        ExpressionNode expression =
                modifyNode(joinClauseNode.expression());
        OnClauseNode joinOnCondition =
                modifyNode(joinClauseNode.joinOnCondition());
        return joinClauseNode.modify(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition);
    }

    @Override
    public OnClauseNode transform(
            OnClauseNode onClauseNode) {
        Token onKeyword =
                modifyToken(onClauseNode.onKeyword());
        ExpressionNode lhsExpression =
                modifyNode(onClauseNode.lhsExpression());
        Token equalsKeyword =
                modifyToken(onClauseNode.equalsKeyword());
        ExpressionNode rhsExpression =
                modifyNode(onClauseNode.rhsExpression());
        return onClauseNode.modify(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression);
    }

    @Override
    public LimitClauseNode transform(
            LimitClauseNode limitClauseNode) {
        Token limitKeyword =
                modifyToken(limitClauseNode.limitKeyword());
        ExpressionNode expression =
                modifyNode(limitClauseNode.expression());
        return limitClauseNode.modify(
                limitKeyword,
                expression);
    }

    @Override
    public OnConflictClauseNode transform(
            OnConflictClauseNode onConflictClauseNode) {
        Token onKeyword =
                modifyToken(onConflictClauseNode.onKeyword());
        Token conflictKeyword =
                modifyToken(onConflictClauseNode.conflictKeyword());
        ExpressionNode expression =
                modifyNode(onConflictClauseNode.expression());
        return onConflictClauseNode.modify(
                onKeyword,
                conflictKeyword,
                expression);
    }

    @Override
    public QueryPipelineNode transform(
            QueryPipelineNode queryPipelineNode) {
        FromClauseNode fromClause =
                modifyNode(queryPipelineNode.fromClause());
        NodeList<IntermediateClauseNode> intermediateClauses =
                modifyNodeList(queryPipelineNode.intermediateClauses());
        return queryPipelineNode.modify(
                fromClause,
                intermediateClauses);
    }

    @Override
    public SelectClauseNode transform(
            SelectClauseNode selectClauseNode) {
        Token selectKeyword =
                modifyToken(selectClauseNode.selectKeyword());
        ExpressionNode expression =
                modifyNode(selectClauseNode.expression());
        return selectClauseNode.modify(
                selectKeyword,
                expression);
    }

    @Override
    public QueryExpressionNode transform(
            QueryExpressionNode queryExpressionNode) {
        QueryConstructTypeNode queryConstructType =
                modifyNode(queryExpressionNode.queryConstructType().orElse(null));
        QueryPipelineNode queryPipeline =
                modifyNode(queryExpressionNode.queryPipeline());
        SelectClauseNode selectClause =
                modifyNode(queryExpressionNode.selectClause());
        OnConflictClauseNode onConflictClause =
                modifyNode(queryExpressionNode.onConflictClause().orElse(null));
        return queryExpressionNode.modify(
                queryConstructType,
                queryPipeline,
                selectClause,
                onConflictClause);
    }

    @Override
    public QueryActionNode transform(
            QueryActionNode queryActionNode) {
        QueryPipelineNode queryPipeline =
                modifyNode(queryActionNode.queryPipeline());
        Token doKeyword =
                modifyToken(queryActionNode.doKeyword());
        BlockStatementNode blockStatement =
                modifyNode(queryActionNode.blockStatement());
        return queryActionNode.modify(
                queryPipeline,
                doKeyword,
                blockStatement);
    }

    @Override
    public IntersectionTypeDescriptorNode transform(
            IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        Node leftTypeDesc =
                modifyNode(intersectionTypeDescriptorNode.leftTypeDesc());
        Token bitwiseAndToken =
                modifyToken(intersectionTypeDescriptorNode.bitwiseAndToken());
        Node rightTypeDesc =
                modifyNode(intersectionTypeDescriptorNode.rightTypeDesc());
        return intersectionTypeDescriptorNode.modify(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc);
    }

    @Override
    public ImplicitAnonymousFunctionParameters transform(
            ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        Token openParenToken =
                modifyToken(implicitAnonymousFunctionParameters.openParenToken());
        SeparatedNodeList<SimpleNameReferenceNode> parameters =
                modifySeparatedNodeList(implicitAnonymousFunctionParameters.parameters());
        Token closeParenToken =
                modifyToken(implicitAnonymousFunctionParameters.closeParenToken());
        return implicitAnonymousFunctionParameters.modify(
                openParenToken,
                parameters,
                closeParenToken);
    }

    @Override
    public ImplicitAnonymousFunctionExpressionNode transform(
            ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        Node params =
                modifyNode(implicitAnonymousFunctionExpressionNode.params());
        Token rightDoubleArrow =
                modifyToken(implicitAnonymousFunctionExpressionNode.rightDoubleArrow());
        ExpressionNode expression =
                modifyNode(implicitAnonymousFunctionExpressionNode.expression());
        return implicitAnonymousFunctionExpressionNode.modify(
                params,
                rightDoubleArrow,
                expression);
    }

    @Override
    public StartActionNode transform(
            StartActionNode startActionNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(startActionNode.annotations());
        Token startKeyword =
                modifyToken(startActionNode.startKeyword());
        ExpressionNode expression =
                modifyNode(startActionNode.expression());
        return startActionNode.modify(
                annotations,
                startKeyword,
                expression);
    }

    @Override
    public FlushActionNode transform(
            FlushActionNode flushActionNode) {
        Token flushKeyword =
                modifyToken(flushActionNode.flushKeyword());
        NameReferenceNode peerWorker =
                modifyNode(flushActionNode.peerWorker().orElse(null));
        return flushActionNode.modify(
                flushKeyword,
                peerWorker);
    }

    @Override
    public SingletonTypeDescriptorNode transform(
            SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        ExpressionNode simpleContExprNode =
                modifyNode(singletonTypeDescriptorNode.simpleContExprNode());
        return singletonTypeDescriptorNode.modify(
                simpleContExprNode);
    }

    @Override
    public MethodDeclarationNode transform(
            MethodDeclarationNode methodDeclarationNode) {
        MetadataNode metadata =
                modifyNode(methodDeclarationNode.metadata().orElse(null));
        NodeList<Token> qualifierList =
                modifyNodeList(methodDeclarationNode.qualifierList());
        Token functionKeyword =
                modifyToken(methodDeclarationNode.functionKeyword());
        IdentifierToken methodName =
                modifyNode(methodDeclarationNode.methodName());
        NodeList<Node> relativeResourcePath =
                modifyNodeList(methodDeclarationNode.relativeResourcePath());
        FunctionSignatureNode methodSignature =
                modifyNode(methodDeclarationNode.methodSignature());
        Token semicolon =
                modifyToken(methodDeclarationNode.semicolon());
        return methodDeclarationNode.modify(
                methodDeclarationNode.kind(),
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon);
    }

    @Override
    public TypedBindingPatternNode transform(
            TypedBindingPatternNode typedBindingPatternNode) {
        TypeDescriptorNode typeDescriptor =
                modifyNode(typedBindingPatternNode.typeDescriptor());
        BindingPatternNode bindingPattern =
                modifyNode(typedBindingPatternNode.bindingPattern());
        return typedBindingPatternNode.modify(
                typeDescriptor,
                bindingPattern);
    }

    @Override
    public CaptureBindingPatternNode transform(
            CaptureBindingPatternNode captureBindingPatternNode) {
        Token variableName =
                modifyToken(captureBindingPatternNode.variableName());
        return captureBindingPatternNode.modify(
                variableName);
    }

    @Override
    public WildcardBindingPatternNode transform(
            WildcardBindingPatternNode wildcardBindingPatternNode) {
        Token underscoreToken =
                modifyToken(wildcardBindingPatternNode.underscoreToken());
        return wildcardBindingPatternNode.modify(
                underscoreToken);
    }

    @Override
    public ListBindingPatternNode transform(
            ListBindingPatternNode listBindingPatternNode) {
        Token openBracket =
                modifyToken(listBindingPatternNode.openBracket());
        SeparatedNodeList<BindingPatternNode> bindingPatterns =
                modifySeparatedNodeList(listBindingPatternNode.bindingPatterns());
        Token closeBracket =
                modifyToken(listBindingPatternNode.closeBracket());
        return listBindingPatternNode.modify(
                openBracket,
                bindingPatterns,
                closeBracket);
    }

    @Override
    public MappingBindingPatternNode transform(
            MappingBindingPatternNode mappingBindingPatternNode) {
        Token openBrace =
                modifyToken(mappingBindingPatternNode.openBrace());
        SeparatedNodeList<BindingPatternNode> fieldBindingPatterns =
                modifySeparatedNodeList(mappingBindingPatternNode.fieldBindingPatterns());
        Token closeBrace =
                modifyToken(mappingBindingPatternNode.closeBrace());
        return mappingBindingPatternNode.modify(
                openBrace,
                fieldBindingPatterns,
                closeBrace);
    }

    @Override
    public FieldBindingPatternFullNode transform(
            FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        SimpleNameReferenceNode variableName =
                modifyNode(fieldBindingPatternFullNode.variableName());
        Token colon =
                modifyToken(fieldBindingPatternFullNode.colon());
        BindingPatternNode bindingPattern =
                modifyNode(fieldBindingPatternFullNode.bindingPattern());
        return fieldBindingPatternFullNode.modify(
                variableName,
                colon,
                bindingPattern);
    }

    @Override
    public FieldBindingPatternVarnameNode transform(
            FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        SimpleNameReferenceNode variableName =
                modifyNode(fieldBindingPatternVarnameNode.variableName());
        return fieldBindingPatternVarnameNode.modify(
                variableName);
    }

    @Override
    public RestBindingPatternNode transform(
            RestBindingPatternNode restBindingPatternNode) {
        Token ellipsisToken =
                modifyToken(restBindingPatternNode.ellipsisToken());
        SimpleNameReferenceNode variableName =
                modifyNode(restBindingPatternNode.variableName());
        return restBindingPatternNode.modify(
                ellipsisToken,
                variableName);
    }

    @Override
    public ErrorBindingPatternNode transform(
            ErrorBindingPatternNode errorBindingPatternNode) {
        Token errorKeyword =
                modifyToken(errorBindingPatternNode.errorKeyword());
        Node typeReference =
                modifyNode(errorBindingPatternNode.typeReference().orElse(null));
        Token openParenthesis =
                modifyToken(errorBindingPatternNode.openParenthesis());
        SeparatedNodeList<BindingPatternNode> argListBindingPatterns =
                modifySeparatedNodeList(errorBindingPatternNode.argListBindingPatterns());
        Token closeParenthesis =
                modifyToken(errorBindingPatternNode.closeParenthesis());
        return errorBindingPatternNode.modify(
                errorKeyword,
                typeReference,
                openParenthesis,
                argListBindingPatterns,
                closeParenthesis);
    }

    @Override
    public NamedArgBindingPatternNode transform(
            NamedArgBindingPatternNode namedArgBindingPatternNode) {
        IdentifierToken argName =
                modifyNode(namedArgBindingPatternNode.argName());
        Token equalsToken =
                modifyToken(namedArgBindingPatternNode.equalsToken());
        BindingPatternNode bindingPattern =
                modifyNode(namedArgBindingPatternNode.bindingPattern());
        return namedArgBindingPatternNode.modify(
                argName,
                equalsToken,
                bindingPattern);
    }

    @Override
    public AsyncSendActionNode transform(
            AsyncSendActionNode asyncSendActionNode) {
        ExpressionNode expression =
                modifyNode(asyncSendActionNode.expression());
        Token rightArrowToken =
                modifyToken(asyncSendActionNode.rightArrowToken());
        SimpleNameReferenceNode peerWorker =
                modifyNode(asyncSendActionNode.peerWorker());
        return asyncSendActionNode.modify(
                expression,
                rightArrowToken,
                peerWorker);
    }

    @Override
    public SyncSendActionNode transform(
            SyncSendActionNode syncSendActionNode) {
        ExpressionNode expression =
                modifyNode(syncSendActionNode.expression());
        Token syncSendToken =
                modifyToken(syncSendActionNode.syncSendToken());
        SimpleNameReferenceNode peerWorker =
                modifyNode(syncSendActionNode.peerWorker());
        return syncSendActionNode.modify(
                expression,
                syncSendToken,
                peerWorker);
    }

    @Override
    public ReceiveActionNode transform(
            ReceiveActionNode receiveActionNode) {
        Token leftArrow =
                modifyToken(receiveActionNode.leftArrow());
        Node receiveWorkers =
                modifyNode(receiveActionNode.receiveWorkers());
        return receiveActionNode.modify(
                leftArrow,
                receiveWorkers);
    }

    @Override
    public ReceiveFieldsNode transform(
            ReceiveFieldsNode receiveFieldsNode) {
        Token openBrace =
                modifyToken(receiveFieldsNode.openBrace());
        SeparatedNodeList<NameReferenceNode> receiveFields =
                modifySeparatedNodeList(receiveFieldsNode.receiveFields());
        Token closeBrace =
                modifyToken(receiveFieldsNode.closeBrace());
        return receiveFieldsNode.modify(
                openBrace,
                receiveFields,
                closeBrace);
    }

    @Override
    public RestDescriptorNode transform(
            RestDescriptorNode restDescriptorNode) {
        TypeDescriptorNode typeDescriptor =
                modifyNode(restDescriptorNode.typeDescriptor());
        Token ellipsisToken =
                modifyToken(restDescriptorNode.ellipsisToken());
        return restDescriptorNode.modify(
                typeDescriptor,
                ellipsisToken);
    }

    @Override
    public DoubleGTTokenNode transform(
            DoubleGTTokenNode doubleGTTokenNode) {
        Token openGTToken =
                modifyToken(doubleGTTokenNode.openGTToken());
        Token endGTToken =
                modifyToken(doubleGTTokenNode.endGTToken());
        return doubleGTTokenNode.modify(
                openGTToken,
                endGTToken);
    }

    @Override
    public TrippleGTTokenNode transform(
            TrippleGTTokenNode trippleGTTokenNode) {
        Token openGTToken =
                modifyToken(trippleGTTokenNode.openGTToken());
        Token middleGTToken =
                modifyToken(trippleGTTokenNode.middleGTToken());
        Token endGTToken =
                modifyToken(trippleGTTokenNode.endGTToken());
        return trippleGTTokenNode.modify(
                openGTToken,
                middleGTToken,
                endGTToken);
    }

    @Override
    public WaitActionNode transform(
            WaitActionNode waitActionNode) {
        Token waitKeyword =
                modifyToken(waitActionNode.waitKeyword());
        Node waitFutureExpr =
                modifyNode(waitActionNode.waitFutureExpr());
        return waitActionNode.modify(
                waitKeyword,
                waitFutureExpr);
    }

    @Override
    public WaitFieldsListNode transform(
            WaitFieldsListNode waitFieldsListNode) {
        Token openBrace =
                modifyToken(waitFieldsListNode.openBrace());
        SeparatedNodeList<Node> waitFields =
                modifySeparatedNodeList(waitFieldsListNode.waitFields());
        Token closeBrace =
                modifyToken(waitFieldsListNode.closeBrace());
        return waitFieldsListNode.modify(
                openBrace,
                waitFields,
                closeBrace);
    }

    @Override
    public WaitFieldNode transform(
            WaitFieldNode waitFieldNode) {
        SimpleNameReferenceNode fieldName =
                modifyNode(waitFieldNode.fieldName());
        Token colon =
                modifyToken(waitFieldNode.colon());
        ExpressionNode waitFutureExpr =
                modifyNode(waitFieldNode.waitFutureExpr());
        return waitFieldNode.modify(
                fieldName,
                colon,
                waitFutureExpr);
    }

    @Override
    public AnnotAccessExpressionNode transform(
            AnnotAccessExpressionNode annotAccessExpressionNode) {
        ExpressionNode expression =
                modifyNode(annotAccessExpressionNode.expression());
        Token annotChainingToken =
                modifyToken(annotAccessExpressionNode.annotChainingToken());
        NameReferenceNode annotTagReference =
                modifyNode(annotAccessExpressionNode.annotTagReference());
        return annotAccessExpressionNode.modify(
                expression,
                annotChainingToken,
                annotTagReference);
    }

    @Override
    public OptionalFieldAccessExpressionNode transform(
            OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        ExpressionNode expression =
                modifyNode(optionalFieldAccessExpressionNode.expression());
        Token optionalChainingToken =
                modifyToken(optionalFieldAccessExpressionNode.optionalChainingToken());
        NameReferenceNode fieldName =
                modifyNode(optionalFieldAccessExpressionNode.fieldName());
        return optionalFieldAccessExpressionNode.modify(
                expression,
                optionalChainingToken,
                fieldName);
    }

    @Override
    public ConditionalExpressionNode transform(
            ConditionalExpressionNode conditionalExpressionNode) {
        ExpressionNode lhsExpression =
                modifyNode(conditionalExpressionNode.lhsExpression());
        Token questionMarkToken =
                modifyToken(conditionalExpressionNode.questionMarkToken());
        ExpressionNode middleExpression =
                modifyNode(conditionalExpressionNode.middleExpression());
        Token colonToken =
                modifyToken(conditionalExpressionNode.colonToken());
        ExpressionNode endExpression =
                modifyNode(conditionalExpressionNode.endExpression());
        return conditionalExpressionNode.modify(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression);
    }

    @Override
    public EnumDeclarationNode transform(
            EnumDeclarationNode enumDeclarationNode) {
        MetadataNode metadata =
                modifyNode(enumDeclarationNode.metadata().orElse(null));
        Token qualifier =
                modifyToken(enumDeclarationNode.qualifier().orElse(null));
        Token enumKeywordToken =
                modifyToken(enumDeclarationNode.enumKeywordToken());
        IdentifierToken identifier =
                modifyNode(enumDeclarationNode.identifier());
        Token openBraceToken =
                modifyToken(enumDeclarationNode.openBraceToken());
        SeparatedNodeList<Node> enumMemberList =
                modifySeparatedNodeList(enumDeclarationNode.enumMemberList());
        Token closeBraceToken =
                modifyToken(enumDeclarationNode.closeBraceToken());
        Token semicolonToken =
                modifyToken(enumDeclarationNode.semicolonToken().orElse(null));
        return enumDeclarationNode.modify(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken,
                semicolonToken);
    }

    @Override
    public EnumMemberNode transform(
            EnumMemberNode enumMemberNode) {
        MetadataNode metadata =
                modifyNode(enumMemberNode.metadata().orElse(null));
        IdentifierToken identifier =
                modifyNode(enumMemberNode.identifier());
        Token equalToken =
                modifyToken(enumMemberNode.equalToken().orElse(null));
        ExpressionNode constExprNode =
                modifyNode(enumMemberNode.constExprNode().orElse(null));
        return enumMemberNode.modify(
                metadata,
                identifier,
                equalToken,
                constExprNode);
    }

    @Override
    public ArrayTypeDescriptorNode transform(
            ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        TypeDescriptorNode memberTypeDesc =
                modifyNode(arrayTypeDescriptorNode.memberTypeDesc());
        NodeList<ArrayDimensionNode> dimensions =
                modifyNodeList(arrayTypeDescriptorNode.dimensions());
        return arrayTypeDescriptorNode.modify(
                memberTypeDesc,
                dimensions);
    }

    @Override
    public ArrayDimensionNode transform(
            ArrayDimensionNode arrayDimensionNode) {
        Token openBracket =
                modifyToken(arrayDimensionNode.openBracket());
        Node arrayLength =
                modifyNode(arrayDimensionNode.arrayLength().orElse(null));
        Token closeBracket =
                modifyToken(arrayDimensionNode.closeBracket());
        return arrayDimensionNode.modify(
                openBracket,
                arrayLength,
                closeBracket);
    }

    @Override
    public TransactionStatementNode transform(
            TransactionStatementNode transactionStatementNode) {
        Token transactionKeyword =
                modifyToken(transactionStatementNode.transactionKeyword());
        BlockStatementNode blockStatement =
                modifyNode(transactionStatementNode.blockStatement());
        OnFailClauseNode onFailClause =
                modifyNode(transactionStatementNode.onFailClause().orElse(null));
        return transactionStatementNode.modify(
                transactionKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public RollbackStatementNode transform(
            RollbackStatementNode rollbackStatementNode) {
        Token rollbackKeyword =
                modifyToken(rollbackStatementNode.rollbackKeyword());
        ExpressionNode expression =
                modifyNode(rollbackStatementNode.expression().orElse(null));
        Token semicolon =
                modifyToken(rollbackStatementNode.semicolon());
        return rollbackStatementNode.modify(
                rollbackKeyword,
                expression,
                semicolon);
    }

    @Override
    public RetryStatementNode transform(
            RetryStatementNode retryStatementNode) {
        Token retryKeyword =
                modifyToken(retryStatementNode.retryKeyword());
        TypeParameterNode typeParameter =
                modifyNode(retryStatementNode.typeParameter().orElse(null));
        ParenthesizedArgList arguments =
                modifyNode(retryStatementNode.arguments().orElse(null));
        StatementNode retryBody =
                modifyNode(retryStatementNode.retryBody());
        OnFailClauseNode onFailClause =
                modifyNode(retryStatementNode.onFailClause().orElse(null));
        return retryStatementNode.modify(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody,
                onFailClause);
    }

    @Override
    public CommitActionNode transform(
            CommitActionNode commitActionNode) {
        Token commitKeyword =
                modifyToken(commitActionNode.commitKeyword());
        return commitActionNode.modify(
                commitKeyword);
    }

    @Override
    public TransactionalExpressionNode transform(
            TransactionalExpressionNode transactionalExpressionNode) {
        Token transactionalKeyword =
                modifyToken(transactionalExpressionNode.transactionalKeyword());
        return transactionalExpressionNode.modify(
                transactionalKeyword);
    }

    @Override
    public ByteArrayLiteralNode transform(
            ByteArrayLiteralNode byteArrayLiteralNode) {
        Token type =
                modifyToken(byteArrayLiteralNode.type());
        Token startBacktick =
                modifyToken(byteArrayLiteralNode.startBacktick());
        Token content =
                modifyToken(byteArrayLiteralNode.content().orElse(null));
        Token endBacktick =
                modifyToken(byteArrayLiteralNode.endBacktick());
        return byteArrayLiteralNode.modify(
                type,
                startBacktick,
                content,
                endBacktick);
    }

    @Override
    public XMLFilterExpressionNode transform(
            XMLFilterExpressionNode xMLFilterExpressionNode) {
        ExpressionNode expression =
                modifyNode(xMLFilterExpressionNode.expression());
        XMLNamePatternChainingNode xmlPatternChain =
                modifyNode(xMLFilterExpressionNode.xmlPatternChain());
        return xMLFilterExpressionNode.modify(
                expression,
                xmlPatternChain);
    }

    @Override
    public XMLStepExpressionNode transform(
            XMLStepExpressionNode xMLStepExpressionNode) {
        ExpressionNode expression =
                modifyNode(xMLStepExpressionNode.expression());
        Node xmlStepStart =
                modifyNode(xMLStepExpressionNode.xmlStepStart());
        return xMLStepExpressionNode.modify(
                expression,
                xmlStepStart);
    }

    @Override
    public XMLNamePatternChainingNode transform(
            XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        Token startToken =
                modifyToken(xMLNamePatternChainingNode.startToken());
        SeparatedNodeList<Node> xmlNamePattern =
                modifySeparatedNodeList(xMLNamePatternChainingNode.xmlNamePattern());
        Token gtToken =
                modifyToken(xMLNamePatternChainingNode.gtToken());
        return xMLNamePatternChainingNode.modify(
                startToken,
                xmlNamePattern,
                gtToken);
    }

    @Override
    public XMLAtomicNamePatternNode transform(
            XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        Token prefix =
                modifyToken(xMLAtomicNamePatternNode.prefix());
        Token colon =
                modifyToken(xMLAtomicNamePatternNode.colon());
        Token name =
                modifyToken(xMLAtomicNamePatternNode.name());
        return xMLAtomicNamePatternNode.modify(
                prefix,
                colon,
                name);
    }

    @Override
    public TypeReferenceTypeDescNode transform(
            TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        NameReferenceNode typeRef =
                modifyNode(typeReferenceTypeDescNode.typeRef());
        return typeReferenceTypeDescNode.modify(
                typeRef);
    }

    @Override
    public MatchStatementNode transform(
            MatchStatementNode matchStatementNode) {
        Token matchKeyword =
                modifyToken(matchStatementNode.matchKeyword());
        ExpressionNode condition =
                modifyNode(matchStatementNode.condition());
        Token openBrace =
                modifyToken(matchStatementNode.openBrace());
        NodeList<MatchClauseNode> matchClauses =
                modifyNodeList(matchStatementNode.matchClauses());
        Token closeBrace =
                modifyToken(matchStatementNode.closeBrace());
        OnFailClauseNode onFailClause =
                modifyNode(matchStatementNode.onFailClause().orElse(null));
        return matchStatementNode.modify(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                onFailClause);
    }

    @Override
    public MatchClauseNode transform(
            MatchClauseNode matchClauseNode) {
        SeparatedNodeList<Node> matchPatterns =
                modifySeparatedNodeList(matchClauseNode.matchPatterns());
        MatchGuardNode matchGuard =
                modifyNode(matchClauseNode.matchGuard().orElse(null));
        Token rightDoubleArrow =
                modifyToken(matchClauseNode.rightDoubleArrow());
        BlockStatementNode blockStatement =
                modifyNode(matchClauseNode.blockStatement());
        return matchClauseNode.modify(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement);
    }

    @Override
    public MatchGuardNode transform(
            MatchGuardNode matchGuardNode) {
        Token ifKeyword =
                modifyToken(matchGuardNode.ifKeyword());
        ExpressionNode expression =
                modifyNode(matchGuardNode.expression());
        return matchGuardNode.modify(
                ifKeyword,
                expression);
    }

    @Override
    public DistinctTypeDescriptorNode transform(
            DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        Token distinctKeyword =
                modifyToken(distinctTypeDescriptorNode.distinctKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(distinctTypeDescriptorNode.typeDescriptor());
        return distinctTypeDescriptorNode.modify(
                distinctKeyword,
                typeDescriptor);
    }

    @Override
    public ListMatchPatternNode transform(
            ListMatchPatternNode listMatchPatternNode) {
        Token openBracket =
                modifyToken(listMatchPatternNode.openBracket());
        SeparatedNodeList<Node> matchPatterns =
                modifySeparatedNodeList(listMatchPatternNode.matchPatterns());
        Token closeBracket =
                modifyToken(listMatchPatternNode.closeBracket());
        return listMatchPatternNode.modify(
                openBracket,
                matchPatterns,
                closeBracket);
    }

    @Override
    public RestMatchPatternNode transform(
            RestMatchPatternNode restMatchPatternNode) {
        Token ellipsisToken =
                modifyToken(restMatchPatternNode.ellipsisToken());
        Token varKeywordToken =
                modifyToken(restMatchPatternNode.varKeywordToken());
        SimpleNameReferenceNode variableName =
                modifyNode(restMatchPatternNode.variableName());
        return restMatchPatternNode.modify(
                ellipsisToken,
                varKeywordToken,
                variableName);
    }

    @Override
    public MappingMatchPatternNode transform(
            MappingMatchPatternNode mappingMatchPatternNode) {
        Token openBraceToken =
                modifyToken(mappingMatchPatternNode.openBraceToken());
        SeparatedNodeList<Node> fieldMatchPatterns =
                modifySeparatedNodeList(mappingMatchPatternNode.fieldMatchPatterns());
        Token closeBraceToken =
                modifyToken(mappingMatchPatternNode.closeBraceToken());
        return mappingMatchPatternNode.modify(
                openBraceToken,
                fieldMatchPatterns,
                closeBraceToken);
    }

    @Override
    public FieldMatchPatternNode transform(
            FieldMatchPatternNode fieldMatchPatternNode) {
        IdentifierToken fieldNameNode =
                modifyNode(fieldMatchPatternNode.fieldNameNode());
        Token colonToken =
                modifyToken(fieldMatchPatternNode.colonToken());
        Node matchPattern =
                modifyNode(fieldMatchPatternNode.matchPattern());
        return fieldMatchPatternNode.modify(
                fieldNameNode,
                colonToken,
                matchPattern);
    }

    @Override
    public ErrorMatchPatternNode transform(
            ErrorMatchPatternNode errorMatchPatternNode) {
        Token errorKeyword =
                modifyToken(errorMatchPatternNode.errorKeyword());
        NameReferenceNode typeReference =
                modifyNode(errorMatchPatternNode.typeReference().orElse(null));
        Token openParenthesisToken =
                modifyToken(errorMatchPatternNode.openParenthesisToken());
        SeparatedNodeList<Node> argListMatchPatternNode =
                modifySeparatedNodeList(errorMatchPatternNode.argListMatchPatternNode());
        Token closeParenthesisToken =
                modifyToken(errorMatchPatternNode.closeParenthesisToken());
        return errorMatchPatternNode.modify(
                errorKeyword,
                typeReference,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    @Override
    public NamedArgMatchPatternNode transform(
            NamedArgMatchPatternNode namedArgMatchPatternNode) {
        IdentifierToken identifier =
                modifyNode(namedArgMatchPatternNode.identifier());
        Token equalToken =
                modifyToken(namedArgMatchPatternNode.equalToken());
        Node matchPattern =
                modifyNode(namedArgMatchPatternNode.matchPattern());
        return namedArgMatchPatternNode.modify(
                identifier,
                equalToken,
                matchPattern);
    }

    @Override
    public MarkdownDocumentationNode transform(
            MarkdownDocumentationNode markdownDocumentationNode) {
        NodeList<Node> documentationLines =
                modifyNodeList(markdownDocumentationNode.documentationLines());
        return markdownDocumentationNode.modify(
                documentationLines);
    }

    @Override
    public MarkdownDocumentationLineNode transform(
            MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        Token hashToken =
                modifyToken(markdownDocumentationLineNode.hashToken());
        NodeList<Node> documentElements =
                modifyNodeList(markdownDocumentationLineNode.documentElements());
        return markdownDocumentationLineNode.modify(
                markdownDocumentationLineNode.kind(),
                hashToken,
                documentElements);
    }

    @Override
    public MarkdownParameterDocumentationLineNode transform(
            MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        Token hashToken =
                modifyToken(markdownParameterDocumentationLineNode.hashToken());
        Token plusToken =
                modifyToken(markdownParameterDocumentationLineNode.plusToken());
        Token parameterName =
                modifyToken(markdownParameterDocumentationLineNode.parameterName());
        Token minusToken =
                modifyToken(markdownParameterDocumentationLineNode.minusToken());
        NodeList<Node> documentElements =
                modifyNodeList(markdownParameterDocumentationLineNode.documentElements());
        return markdownParameterDocumentationLineNode.modify(
                markdownParameterDocumentationLineNode.kind(),
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements);
    }

    @Override
    public BallerinaNameReferenceNode transform(
            BallerinaNameReferenceNode ballerinaNameReferenceNode) {
        Token referenceType =
                modifyToken(ballerinaNameReferenceNode.referenceType().orElse(null));
        Token startBacktick =
                modifyToken(ballerinaNameReferenceNode.startBacktick());
        Node nameReference =
                modifyNode(ballerinaNameReferenceNode.nameReference());
        Token endBacktick =
                modifyToken(ballerinaNameReferenceNode.endBacktick());
        return ballerinaNameReferenceNode.modify(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick);
    }

    @Override
    public InlineCodeReferenceNode transform(
            InlineCodeReferenceNode inlineCodeReferenceNode) {
        Token startBacktick =
                modifyToken(inlineCodeReferenceNode.startBacktick());
        Token codeReference =
                modifyToken(inlineCodeReferenceNode.codeReference());
        Token endBacktick =
                modifyToken(inlineCodeReferenceNode.endBacktick());
        return inlineCodeReferenceNode.modify(
                startBacktick,
                codeReference,
                endBacktick);
    }

    @Override
    public MarkdownCodeBlockNode transform(
            MarkdownCodeBlockNode markdownCodeBlockNode) {
        Token startLineHashToken =
                modifyToken(markdownCodeBlockNode.startLineHashToken());
        Token startBacktick =
                modifyToken(markdownCodeBlockNode.startBacktick());
        Token langAttribute =
                modifyToken(markdownCodeBlockNode.langAttribute().orElse(null));
        NodeList<MarkdownCodeLineNode> codeLines =
                modifyNodeList(markdownCodeBlockNode.codeLines());
        Token endLineHashToken =
                modifyToken(markdownCodeBlockNode.endLineHashToken());
        Token endBacktick =
                modifyToken(markdownCodeBlockNode.endBacktick());
        return markdownCodeBlockNode.modify(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick);
    }

    @Override
    public MarkdownCodeLineNode transform(
            MarkdownCodeLineNode markdownCodeLineNode) {
        Token hashToken =
                modifyToken(markdownCodeLineNode.hashToken());
        Token codeDescription =
                modifyToken(markdownCodeLineNode.codeDescription());
        return markdownCodeLineNode.modify(
                hashToken,
                codeDescription);
    }

    @Override
    public OrderByClauseNode transform(
            OrderByClauseNode orderByClauseNode) {
        Token orderKeyword =
                modifyToken(orderByClauseNode.orderKeyword());
        Token byKeyword =
                modifyToken(orderByClauseNode.byKeyword());
        SeparatedNodeList<OrderKeyNode> orderKey =
                modifySeparatedNodeList(orderByClauseNode.orderKey());
        return orderByClauseNode.modify(
                orderKeyword,
                byKeyword,
                orderKey);
    }

    @Override
    public OrderKeyNode transform(
            OrderKeyNode orderKeyNode) {
        ExpressionNode expression =
                modifyNode(orderKeyNode.expression());
        Token orderDirection =
                modifyToken(orderKeyNode.orderDirection().orElse(null));
        return orderKeyNode.modify(
                expression,
                orderDirection);
    }

    @Override
    public OnFailClauseNode transform(
            OnFailClauseNode onFailClauseNode) {
        Token onKeyword =
                modifyToken(onFailClauseNode.onKeyword());
        Token failKeyword =
                modifyToken(onFailClauseNode.failKeyword());
        TypeDescriptorNode typeDescriptor =
                modifyNode(onFailClauseNode.typeDescriptor().orElse(null));
        IdentifierToken failErrorName =
                modifyNode(onFailClauseNode.failErrorName().orElse(null));
        BlockStatementNode blockStatement =
                modifyNode(onFailClauseNode.blockStatement());
        return onFailClauseNode.modify(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement);
    }

    @Override
    public DoStatementNode transform(
            DoStatementNode doStatementNode) {
        Token doKeyword =
                modifyToken(doStatementNode.doKeyword());
        BlockStatementNode blockStatement =
                modifyNode(doStatementNode.blockStatement());
        OnFailClauseNode onFailClause =
                modifyNode(doStatementNode.onFailClause().orElse(null));
        return doStatementNode.modify(
                doKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public ClassDefinitionNode transform(
            ClassDefinitionNode classDefinitionNode) {
        MetadataNode metadata =
                modifyNode(classDefinitionNode.metadata().orElse(null));
        Token visibilityQualifier =
                modifyToken(classDefinitionNode.visibilityQualifier().orElse(null));
        NodeList<Token> classTypeQualifiers =
                modifyNodeList(classDefinitionNode.classTypeQualifiers());
        Token classKeyword =
                modifyToken(classDefinitionNode.classKeyword());
        Token className =
                modifyToken(classDefinitionNode.className());
        Token openBrace =
                modifyToken(classDefinitionNode.openBrace());
        NodeList<Node> members =
                modifyNodeList(classDefinitionNode.members());
        Token closeBrace =
                modifyToken(classDefinitionNode.closeBrace());
        Token semicolonToken =
                modifyToken(classDefinitionNode.semicolonToken().orElse(null));
        return classDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace,
                semicolonToken);
    }

    @Override
    public ResourcePathParameterNode transform(
            ResourcePathParameterNode resourcePathParameterNode) {
        Token openBracketToken =
                modifyToken(resourcePathParameterNode.openBracketToken());
        NodeList<AnnotationNode> annotations =
                modifyNodeList(resourcePathParameterNode.annotations());
        TypeDescriptorNode typeDescriptor =
                modifyNode(resourcePathParameterNode.typeDescriptor());
        Token ellipsisToken =
                modifyToken(resourcePathParameterNode.ellipsisToken().orElse(null));
        Token paramName =
                modifyToken(resourcePathParameterNode.paramName().orElse(null));
        Token closeBracketToken =
                modifyToken(resourcePathParameterNode.closeBracketToken());
        return resourcePathParameterNode.modify(
                resourcePathParameterNode.kind(),
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken);
    }

    @Override
    public RequiredExpressionNode transform(
            RequiredExpressionNode requiredExpressionNode) {
        Token questionMarkToken =
                modifyToken(requiredExpressionNode.questionMarkToken());
        return requiredExpressionNode.modify(
                questionMarkToken);
    }

    @Override
    public ErrorConstructorExpressionNode transform(
            ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        Token errorKeyword =
                modifyToken(errorConstructorExpressionNode.errorKeyword());
        TypeDescriptorNode typeReference =
                modifyNode(errorConstructorExpressionNode.typeReference().orElse(null));
        Token openParenToken =
                modifyToken(errorConstructorExpressionNode.openParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments =
                modifySeparatedNodeList(errorConstructorExpressionNode.arguments());
        Token closeParenToken =
                modifyToken(errorConstructorExpressionNode.closeParenToken());
        return errorConstructorExpressionNode.modify(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(
            ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        Token keywordToken =
                modifyToken(parameterizedTypeDescriptorNode.keywordToken());
        TypeParameterNode typeParamNode =
                modifyNode(parameterizedTypeDescriptorNode.typeParamNode().orElse(null));
        return parameterizedTypeDescriptorNode.modify(
                parameterizedTypeDescriptorNode.kind(),
                keywordToken,
                typeParamNode);
    }

    @Override
    public SpreadMemberNode transform(
            SpreadMemberNode spreadMemberNode) {
        Token ellipsis =
                modifyToken(spreadMemberNode.ellipsis());
        ExpressionNode expression =
                modifyNode(spreadMemberNode.expression());
        return spreadMemberNode.modify(
                ellipsis,
                expression);
    }

    @Override
    public ClientResourceAccessActionNode transform(
            ClientResourceAccessActionNode clientResourceAccessActionNode) {
        ExpressionNode expression =
                modifyNode(clientResourceAccessActionNode.expression());
        Token rightArrowToken =
                modifyToken(clientResourceAccessActionNode.rightArrowToken());
        Token slashToken =
                modifyToken(clientResourceAccessActionNode.slashToken());
        SeparatedNodeList<Node> resourceAccessPath =
                modifySeparatedNodeList(clientResourceAccessActionNode.resourceAccessPath());
        Token dotToken =
                modifyToken(clientResourceAccessActionNode.dotToken().orElse(null));
        SimpleNameReferenceNode methodName =
                modifyNode(clientResourceAccessActionNode.methodName().orElse(null));
        ParenthesizedArgList arguments =
                modifyNode(clientResourceAccessActionNode.arguments().orElse(null));
        return clientResourceAccessActionNode.modify(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments);
    }

    @Override
    public ComputedResourceAccessSegmentNode transform(
            ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        Token openBracketToken =
                modifyToken(computedResourceAccessSegmentNode.openBracketToken());
        ExpressionNode expression =
                modifyNode(computedResourceAccessSegmentNode.expression());
        Token closeBracketToken =
                modifyToken(computedResourceAccessSegmentNode.closeBracketToken());
        return computedResourceAccessSegmentNode.modify(
                openBracketToken,
                expression,
                closeBracketToken);
    }

    @Override
    public ResourceAccessRestSegmentNode transform(
            ResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        Token openBracketToken =
                modifyToken(resourceAccessRestSegmentNode.openBracketToken());
        Token ellipsisToken =
                modifyToken(resourceAccessRestSegmentNode.ellipsisToken());
        ExpressionNode expression =
                modifyNode(resourceAccessRestSegmentNode.expression());
        Token closeBracketToken =
                modifyToken(resourceAccessRestSegmentNode.closeBracketToken());
        return resourceAccessRestSegmentNode.modify(
                openBracketToken,
                ellipsisToken,
                expression,
                closeBracketToken);
    }

    @Override
    public ReSequenceNode transform(
            ReSequenceNode reSequenceNode) {
        NodeList<ReTermNode> reTerm =
                modifyNodeList(reSequenceNode.reTerm());
        return reSequenceNode.modify(
                reTerm);
    }

    @Override
    public ReAtomQuantifierNode transform(
            ReAtomQuantifierNode reAtomQuantifierNode) {
        Node reAtom =
                modifyNode(reAtomQuantifierNode.reAtom());
        ReQuantifierNode reQuantifier =
                modifyNode(reAtomQuantifierNode.reQuantifier().orElse(null));
        return reAtomQuantifierNode.modify(
                reAtom,
                reQuantifier);
    }

    @Override
    public ReAtomCharOrEscapeNode transform(
            ReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        Node reAtomCharOrEscape =
                modifyNode(reAtomCharOrEscapeNode.reAtomCharOrEscape());
        return reAtomCharOrEscapeNode.modify(
                reAtomCharOrEscape);
    }

    @Override
    public ReQuoteEscapeNode transform(
            ReQuoteEscapeNode reQuoteEscapeNode) {
        Token slashToken =
                modifyToken(reQuoteEscapeNode.slashToken());
        Node reSyntaxChar =
                modifyNode(reQuoteEscapeNode.reSyntaxChar());
        return reQuoteEscapeNode.modify(
                slashToken,
                reSyntaxChar);
    }

    @Override
    public ReSimpleCharClassEscapeNode transform(
            ReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        Token slashToken =
                modifyToken(reSimpleCharClassEscapeNode.slashToken());
        Node reSimpleCharClassCode =
                modifyNode(reSimpleCharClassEscapeNode.reSimpleCharClassCode());
        return reSimpleCharClassEscapeNode.modify(
                slashToken,
                reSimpleCharClassCode);
    }

    @Override
    public ReUnicodePropertyEscapeNode transform(
            ReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        Token slashToken =
                modifyToken(reUnicodePropertyEscapeNode.slashToken());
        Node property =
                modifyNode(reUnicodePropertyEscapeNode.property());
        Token openBraceToken =
                modifyToken(reUnicodePropertyEscapeNode.openBraceToken());
        ReUnicodePropertyNode reUnicodeProperty =
                modifyNode(reUnicodePropertyEscapeNode.reUnicodeProperty());
        Token closeBraceToken =
                modifyToken(reUnicodePropertyEscapeNode.closeBraceToken());
        return reUnicodePropertyEscapeNode.modify(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken);
    }

    @Override
    public ReUnicodeScriptNode transform(
            ReUnicodeScriptNode reUnicodeScriptNode) {
        Node scriptStart =
                modifyNode(reUnicodeScriptNode.scriptStart());
        Node reUnicodePropertyValue =
                modifyNode(reUnicodeScriptNode.reUnicodePropertyValue());
        return reUnicodeScriptNode.modify(
                scriptStart,
                reUnicodePropertyValue);
    }

    @Override
    public ReUnicodeGeneralCategoryNode transform(
            ReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        Node categoryStart =
                modifyNode(reUnicodeGeneralCategoryNode.categoryStart().orElse(null));
        Node reUnicodeGeneralCategoryName =
                modifyNode(reUnicodeGeneralCategoryNode.reUnicodeGeneralCategoryName());
        return reUnicodeGeneralCategoryNode.modify(
                categoryStart,
                reUnicodeGeneralCategoryName);
    }

    @Override
    public ReCharacterClassNode transform(
            ReCharacterClassNode reCharacterClassNode) {
        Token openBracket =
                modifyToken(reCharacterClassNode.openBracket());
        Token negation =
                modifyToken(reCharacterClassNode.negation().orElse(null));
        Node reCharSet =
                modifyNode(reCharacterClassNode.reCharSet().orElse(null));
        Token closeBracket =
                modifyToken(reCharacterClassNode.closeBracket());
        return reCharacterClassNode.modify(
                openBracket,
                negation,
                reCharSet,
                closeBracket);
    }

    @Override
    public ReCharSetRangeWithReCharSetNode transform(
            ReCharSetRangeWithReCharSetNode reCharSetRangeWithReCharSetNode) {
        ReCharSetRangeNode reCharSetRange =
                modifyNode(reCharSetRangeWithReCharSetNode.reCharSetRange());
        Node reCharSet =
                modifyNode(reCharSetRangeWithReCharSetNode.reCharSet().orElse(null));
        return reCharSetRangeWithReCharSetNode.modify(
                reCharSetRange,
                reCharSet);
    }

    @Override
    public ReCharSetRangeNode transform(
            ReCharSetRangeNode reCharSetRangeNode) {
        Node lhsReCharSetAtom =
                modifyNode(reCharSetRangeNode.lhsReCharSetAtom());
        Token minusToken =
                modifyToken(reCharSetRangeNode.minusToken());
        Node rhsReCharSetAtom =
                modifyNode(reCharSetRangeNode.rhsReCharSetAtom());
        return reCharSetRangeNode.modify(
                lhsReCharSetAtom,
                minusToken,
                rhsReCharSetAtom);
    }

    @Override
    public ReCharSetAtomWithReCharSetNoDashNode transform(
            ReCharSetAtomWithReCharSetNoDashNode reCharSetAtomWithReCharSetNoDashNode) {
        Node reCharSetAtom =
                modifyNode(reCharSetAtomWithReCharSetNoDashNode.reCharSetAtom());
        Node reCharSetNoDash =
                modifyNode(reCharSetAtomWithReCharSetNoDashNode.reCharSetNoDash());
        return reCharSetAtomWithReCharSetNoDashNode.modify(
                reCharSetAtom,
                reCharSetNoDash);
    }

    @Override
    public ReCharSetRangeNoDashWithReCharSetNode transform(
            ReCharSetRangeNoDashWithReCharSetNode reCharSetRangeNoDashWithReCharSetNode) {
        ReCharSetRangeNoDashNode reCharSetRangeNoDash =
                modifyNode(reCharSetRangeNoDashWithReCharSetNode.reCharSetRangeNoDash());
        Node reCharSet =
                modifyNode(reCharSetRangeNoDashWithReCharSetNode.reCharSet().orElse(null));
        return reCharSetRangeNoDashWithReCharSetNode.modify(
                reCharSetRangeNoDash,
                reCharSet);
    }

    @Override
    public ReCharSetRangeNoDashNode transform(
            ReCharSetRangeNoDashNode reCharSetRangeNoDashNode) {
        Node reCharSetAtomNoDash =
                modifyNode(reCharSetRangeNoDashNode.reCharSetAtomNoDash());
        Token minusToken =
                modifyToken(reCharSetRangeNoDashNode.minusToken());
        Node reCharSetAtom =
                modifyNode(reCharSetRangeNoDashNode.reCharSetAtom());
        return reCharSetRangeNoDashNode.modify(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom);
    }

    @Override
    public ReCharSetAtomNoDashWithReCharSetNoDashNode transform(
            ReCharSetAtomNoDashWithReCharSetNoDashNode reCharSetAtomNoDashWithReCharSetNoDashNode) {
        Node reCharSetAtomNoDash =
                modifyNode(reCharSetAtomNoDashWithReCharSetNoDashNode.reCharSetAtomNoDash());
        Node reCharSetNoDash =
                modifyNode(reCharSetAtomNoDashWithReCharSetNoDashNode.reCharSetNoDash());
        return reCharSetAtomNoDashWithReCharSetNoDashNode.modify(
                reCharSetAtomNoDash,
                reCharSetNoDash);
    }

    @Override
    public ReCapturingGroupsNode transform(
            ReCapturingGroupsNode reCapturingGroupsNode) {
        Token openParenthesis =
                modifyToken(reCapturingGroupsNode.openParenthesis());
        ReFlagExpressionNode reFlagExpression =
                modifyNode(reCapturingGroupsNode.reFlagExpression().orElse(null));
        NodeList<Node> reSequences =
                modifyNodeList(reCapturingGroupsNode.reSequences());
        Token closeParenthesis =
                modifyToken(reCapturingGroupsNode.closeParenthesis());
        return reCapturingGroupsNode.modify(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis);
    }

    @Override
    public ReFlagExpressionNode transform(
            ReFlagExpressionNode reFlagExpressionNode) {
        Token questionMark =
                modifyToken(reFlagExpressionNode.questionMark());
        ReFlagsOnOffNode reFlagsOnOff =
                modifyNode(reFlagExpressionNode.reFlagsOnOff());
        Token colon =
                modifyToken(reFlagExpressionNode.colon());
        return reFlagExpressionNode.modify(
                questionMark,
                reFlagsOnOff,
                colon);
    }

    @Override
    public ReFlagsOnOffNode transform(
            ReFlagsOnOffNode reFlagsOnOffNode) {
        ReFlagsNode lhsReFlags =
                modifyNode(reFlagsOnOffNode.lhsReFlags());
        Token minusToken =
                modifyToken(reFlagsOnOffNode.minusToken().orElse(null));
        ReFlagsNode rhsReFlags =
                modifyNode(reFlagsOnOffNode.rhsReFlags().orElse(null));
        return reFlagsOnOffNode.modify(
                lhsReFlags,
                minusToken,
                rhsReFlags);
    }

    @Override
    public ReFlagsNode transform(
            ReFlagsNode reFlagsNode) {
        NodeList<Node> reFlag =
                modifyNodeList(reFlagsNode.reFlag());
        return reFlagsNode.modify(
                reFlag);
    }

    @Override
    public ReAssertionNode transform(
            ReAssertionNode reAssertionNode) {
        Node reAssertion =
                modifyNode(reAssertionNode.reAssertion());
        return reAssertionNode.modify(
                reAssertion);
    }

    @Override
    public ReQuantifierNode transform(
            ReQuantifierNode reQuantifierNode) {
        Node reBaseQuantifier =
                modifyNode(reQuantifierNode.reBaseQuantifier());
        Token nonGreedyChar =
                modifyToken(reQuantifierNode.nonGreedyChar().orElse(null));
        return reQuantifierNode.modify(
                reBaseQuantifier,
                nonGreedyChar);
    }

    @Override
    public ReBracedQuantifierNode transform(
            ReBracedQuantifierNode reBracedQuantifierNode) {
        Token openBraceToken =
                modifyToken(reBracedQuantifierNode.openBraceToken());
        NodeList<Node> leastTimesMatchedDigit =
                modifyNodeList(reBracedQuantifierNode.leastTimesMatchedDigit());
        Token commaToken =
                modifyToken(reBracedQuantifierNode.commaToken().orElse(null));
        NodeList<Node> mostTimesMatchedDigit =
                modifyNodeList(reBracedQuantifierNode.mostTimesMatchedDigit());
        Token closeBraceToken =
                modifyToken(reBracedQuantifierNode.closeBraceToken());
        return reBracedQuantifierNode.modify(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken);
    }
  
    public MemberTypeDescriptorNode transform(
            MemberTypeDescriptorNode memberTypeDescriptorNode) {
        NodeList<AnnotationNode> annotations =
                modifyNodeList(memberTypeDescriptorNode.annotations());
        TypeDescriptorNode typeDescriptor =
                modifyNode(memberTypeDescriptorNode.typeDescriptor());
        return memberTypeDescriptorNode.modify(
                annotations,
                typeDescriptor);
    }

    // Tokens

    @Override
    public Token transform(Token token) {
        return token;
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
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
        Function<NonTerminalNode, SeparatedNodeList> nodeListCreator = SeparatedNodeList::new;
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.internalListNode.size()];

        for (int index = 0; index < nodeList.size(); index++) {
            T oldNode = nodeList.get(index);
            T newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }

            newSTNodes[2 * index] = newNode.internalNode();
            if (index == nodeList.size() - 1) {
                break;
            }

            Token oldSeperator = nodeList.getSeparator(index);
            Token newSeperator = modifyToken(oldSeperator);

            if (oldSeperator != newSeperator) {
                nodeModified = true;
            }

            newSTNodes[(2 * index) + 1] = newSeperator.internalNode();
        }

        if (!nodeModified) {
            return nodeList;
        }

        STNode stNodeList = STNodeFactory.createNodeList(java.util.Arrays.asList(newSTNodes));
        return nodeListCreator.apply(stNodeList.createUnlinkedFacade());
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

