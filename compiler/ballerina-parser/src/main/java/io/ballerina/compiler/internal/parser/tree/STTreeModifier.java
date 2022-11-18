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
package io.ballerina.compiler.internal.parser.tree;


/**
 * Produces a new tree by doing a depth-first traversal of the internal tree.
 * <p>
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class STTreeModifier extends STNodeTransformer<STNode> {

    @Override
    public STModulePartNode transform(
            STModulePartNode modulePartNode) {
        STNode imports = modifyNode(modulePartNode.imports);
        STNode members = modifyNode(modulePartNode.members);
        STNode eofToken = modifyNode(modulePartNode.eofToken);
        return modulePartNode.modify(
                imports,
                members,
                eofToken);
    }

    @Override
    public STFunctionDefinitionNode transform(
            STFunctionDefinitionNode functionDefinitionNode) {
        STNode metadata = modifyNode(functionDefinitionNode.metadata);
        STNode qualifierList = modifyNode(functionDefinitionNode.qualifierList);
        STNode functionKeyword = modifyNode(functionDefinitionNode.functionKeyword);
        STNode functionName = modifyNode(functionDefinitionNode.functionName);
        STNode relativeResourcePath = modifyNode(functionDefinitionNode.relativeResourcePath);
        STNode functionSignature = modifyNode(functionDefinitionNode.functionSignature);
        STNode functionBody = modifyNode(functionDefinitionNode.functionBody);
        return functionDefinitionNode.modify(
                functionDefinitionNode.kind,
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    @Override
    public STImportDeclarationNode transform(
            STImportDeclarationNode importDeclarationNode) {
        STNode importKeyword = modifyNode(importDeclarationNode.importKeyword);
        STNode orgName = modifyNode(importDeclarationNode.orgName);
        STNode moduleName = modifyNode(importDeclarationNode.moduleName);
        STNode prefix = modifyNode(importDeclarationNode.prefix);
        STNode semicolon = modifyNode(importDeclarationNode.semicolon);
        return importDeclarationNode.modify(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon);
    }

    @Override
    public STListenerDeclarationNode transform(
            STListenerDeclarationNode listenerDeclarationNode) {
        STNode metadata = modifyNode(listenerDeclarationNode.metadata);
        STNode visibilityQualifier = modifyNode(listenerDeclarationNode.visibilityQualifier);
        STNode listenerKeyword = modifyNode(listenerDeclarationNode.listenerKeyword);
        STNode typeDescriptor = modifyNode(listenerDeclarationNode.typeDescriptor);
        STNode variableName = modifyNode(listenerDeclarationNode.variableName);
        STNode equalsToken = modifyNode(listenerDeclarationNode.equalsToken);
        STNode initializer = modifyNode(listenerDeclarationNode.initializer);
        STNode semicolonToken = modifyNode(listenerDeclarationNode.semicolonToken);
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
    public STTypeDefinitionNode transform(
            STTypeDefinitionNode typeDefinitionNode) {
        STNode metadata = modifyNode(typeDefinitionNode.metadata);
        STNode visibilityQualifier = modifyNode(typeDefinitionNode.visibilityQualifier);
        STNode typeKeyword = modifyNode(typeDefinitionNode.typeKeyword);
        STNode typeName = modifyNode(typeDefinitionNode.typeName);
        STNode typeDescriptor = modifyNode(typeDefinitionNode.typeDescriptor);
        STNode semicolonToken = modifyNode(typeDefinitionNode.semicolonToken);
        return typeDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public STServiceDeclarationNode transform(
            STServiceDeclarationNode serviceDeclarationNode) {
        STNode metadata = modifyNode(serviceDeclarationNode.metadata);
        STNode qualifiers = modifyNode(serviceDeclarationNode.qualifiers);
        STNode serviceKeyword = modifyNode(serviceDeclarationNode.serviceKeyword);
        STNode typeDescriptor = modifyNode(serviceDeclarationNode.typeDescriptor);
        STNode absoluteResourcePath = modifyNode(serviceDeclarationNode.absoluteResourcePath);
        STNode onKeyword = modifyNode(serviceDeclarationNode.onKeyword);
        STNode expressions = modifyNode(serviceDeclarationNode.expressions);
        STNode openBraceToken = modifyNode(serviceDeclarationNode.openBraceToken);
        STNode members = modifyNode(serviceDeclarationNode.members);
        STNode closeBraceToken = modifyNode(serviceDeclarationNode.closeBraceToken);
        STNode semicolonToken = modifyNode(serviceDeclarationNode.semicolonToken);
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
    public STAssignmentStatementNode transform(
            STAssignmentStatementNode assignmentStatementNode) {
        STNode varRef = modifyNode(assignmentStatementNode.varRef);
        STNode equalsToken = modifyNode(assignmentStatementNode.equalsToken);
        STNode expression = modifyNode(assignmentStatementNode.expression);
        STNode semicolonToken = modifyNode(assignmentStatementNode.semicolonToken);
        return assignmentStatementNode.modify(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    @Override
    public STCompoundAssignmentStatementNode transform(
            STCompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        STNode lhsExpression = modifyNode(compoundAssignmentStatementNode.lhsExpression);
        STNode binaryOperator = modifyNode(compoundAssignmentStatementNode.binaryOperator);
        STNode equalsToken = modifyNode(compoundAssignmentStatementNode.equalsToken);
        STNode rhsExpression = modifyNode(compoundAssignmentStatementNode.rhsExpression);
        STNode semicolonToken = modifyNode(compoundAssignmentStatementNode.semicolonToken);
        return compoundAssignmentStatementNode.modify(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    @Override
    public STVariableDeclarationNode transform(
            STVariableDeclarationNode variableDeclarationNode) {
        STNode annotations = modifyNode(variableDeclarationNode.annotations);
        STNode finalKeyword = modifyNode(variableDeclarationNode.finalKeyword);
        STNode typedBindingPattern = modifyNode(variableDeclarationNode.typedBindingPattern);
        STNode equalsToken = modifyNode(variableDeclarationNode.equalsToken);
        STNode initializer = modifyNode(variableDeclarationNode.initializer);
        STNode semicolonToken = modifyNode(variableDeclarationNode.semicolonToken);
        return variableDeclarationNode.modify(
                annotations,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    @Override
    public STBlockStatementNode transform(
            STBlockStatementNode blockStatementNode) {
        STNode openBraceToken = modifyNode(blockStatementNode.openBraceToken);
        STNode statements = modifyNode(blockStatementNode.statements);
        STNode closeBraceToken = modifyNode(blockStatementNode.closeBraceToken);
        return blockStatementNode.modify(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    @Override
    public STBreakStatementNode transform(
            STBreakStatementNode breakStatementNode) {
        STNode breakToken = modifyNode(breakStatementNode.breakToken);
        STNode semicolonToken = modifyNode(breakStatementNode.semicolonToken);
        return breakStatementNode.modify(
                breakToken,
                semicolonToken);
    }

    @Override
    public STFailStatementNode transform(
            STFailStatementNode failStatementNode) {
        STNode failKeyword = modifyNode(failStatementNode.failKeyword);
        STNode expression = modifyNode(failStatementNode.expression);
        STNode semicolonToken = modifyNode(failStatementNode.semicolonToken);
        return failStatementNode.modify(
                failKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public STExpressionStatementNode transform(
            STExpressionStatementNode expressionStatementNode) {
        STNode expression = modifyNode(expressionStatementNode.expression);
        STNode semicolonToken = modifyNode(expressionStatementNode.semicolonToken);
        return expressionStatementNode.modify(
                expressionStatementNode.kind,
                expression,
                semicolonToken);
    }

    @Override
    public STContinueStatementNode transform(
            STContinueStatementNode continueStatementNode) {
        STNode continueToken = modifyNode(continueStatementNode.continueToken);
        STNode semicolonToken = modifyNode(continueStatementNode.semicolonToken);
        return continueStatementNode.modify(
                continueToken,
                semicolonToken);
    }

    @Override
    public STExternalFunctionBodyNode transform(
            STExternalFunctionBodyNode externalFunctionBodyNode) {
        STNode equalsToken = modifyNode(externalFunctionBodyNode.equalsToken);
        STNode annotations = modifyNode(externalFunctionBodyNode.annotations);
        STNode externalKeyword = modifyNode(externalFunctionBodyNode.externalKeyword);
        STNode semicolonToken = modifyNode(externalFunctionBodyNode.semicolonToken);
        return externalFunctionBodyNode.modify(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    @Override
    public STIfElseStatementNode transform(
            STIfElseStatementNode ifElseStatementNode) {
        STNode ifKeyword = modifyNode(ifElseStatementNode.ifKeyword);
        STNode condition = modifyNode(ifElseStatementNode.condition);
        STNode ifBody = modifyNode(ifElseStatementNode.ifBody);
        STNode elseBody = modifyNode(ifElseStatementNode.elseBody);
        return ifElseStatementNode.modify(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    @Override
    public STElseBlockNode transform(
            STElseBlockNode elseBlockNode) {
        STNode elseKeyword = modifyNode(elseBlockNode.elseKeyword);
        STNode elseBody = modifyNode(elseBlockNode.elseBody);
        return elseBlockNode.modify(
                elseKeyword,
                elseBody);
    }

    @Override
    public STWhileStatementNode transform(
            STWhileStatementNode whileStatementNode) {
        STNode whileKeyword = modifyNode(whileStatementNode.whileKeyword);
        STNode condition = modifyNode(whileStatementNode.condition);
        STNode whileBody = modifyNode(whileStatementNode.whileBody);
        STNode onFailClause = modifyNode(whileStatementNode.onFailClause);
        return whileStatementNode.modify(
                whileKeyword,
                condition,
                whileBody,
                onFailClause);
    }

    @Override
    public STPanicStatementNode transform(
            STPanicStatementNode panicStatementNode) {
        STNode panicKeyword = modifyNode(panicStatementNode.panicKeyword);
        STNode expression = modifyNode(panicStatementNode.expression);
        STNode semicolonToken = modifyNode(panicStatementNode.semicolonToken);
        return panicStatementNode.modify(
                panicKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public STReturnStatementNode transform(
            STReturnStatementNode returnStatementNode) {
        STNode returnKeyword = modifyNode(returnStatementNode.returnKeyword);
        STNode expression = modifyNode(returnStatementNode.expression);
        STNode semicolonToken = modifyNode(returnStatementNode.semicolonToken);
        return returnStatementNode.modify(
                returnKeyword,
                expression,
                semicolonToken);
    }

    @Override
    public STLocalTypeDefinitionStatementNode transform(
            STLocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        STNode annotations = modifyNode(localTypeDefinitionStatementNode.annotations);
        STNode typeKeyword = modifyNode(localTypeDefinitionStatementNode.typeKeyword);
        STNode typeName = modifyNode(localTypeDefinitionStatementNode.typeName);
        STNode typeDescriptor = modifyNode(localTypeDefinitionStatementNode.typeDescriptor);
        STNode semicolonToken = modifyNode(localTypeDefinitionStatementNode.semicolonToken);
        return localTypeDefinitionStatementNode.modify(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    @Override
    public STLockStatementNode transform(
            STLockStatementNode lockStatementNode) {
        STNode lockKeyword = modifyNode(lockStatementNode.lockKeyword);
        STNode blockStatement = modifyNode(lockStatementNode.blockStatement);
        STNode onFailClause = modifyNode(lockStatementNode.onFailClause);
        return lockStatementNode.modify(
                lockKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public STForkStatementNode transform(
            STForkStatementNode forkStatementNode) {
        STNode forkKeyword = modifyNode(forkStatementNode.forkKeyword);
        STNode openBraceToken = modifyNode(forkStatementNode.openBraceToken);
        STNode namedWorkerDeclarations = modifyNode(forkStatementNode.namedWorkerDeclarations);
        STNode closeBraceToken = modifyNode(forkStatementNode.closeBraceToken);
        return forkStatementNode.modify(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations,
                closeBraceToken);
    }

    @Override
    public STForEachStatementNode transform(
            STForEachStatementNode forEachStatementNode) {
        STNode forEachKeyword = modifyNode(forEachStatementNode.forEachKeyword);
        STNode typedBindingPattern = modifyNode(forEachStatementNode.typedBindingPattern);
        STNode inKeyword = modifyNode(forEachStatementNode.inKeyword);
        STNode actionOrExpressionNode = modifyNode(forEachStatementNode.actionOrExpressionNode);
        STNode blockStatement = modifyNode(forEachStatementNode.blockStatement);
        STNode onFailClause = modifyNode(forEachStatementNode.onFailClause);
        return forEachStatementNode.modify(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement,
                onFailClause);
    }

    @Override
    public STBinaryExpressionNode transform(
            STBinaryExpressionNode binaryExpressionNode) {
        STNode lhsExpr = modifyNode(binaryExpressionNode.lhsExpr);
        STNode operator = modifyNode(binaryExpressionNode.operator);
        STNode rhsExpr = modifyNode(binaryExpressionNode.rhsExpr);
        return binaryExpressionNode.modify(
                binaryExpressionNode.kind,
                lhsExpr,
                operator,
                rhsExpr);
    }

    @Override
    public STBracedExpressionNode transform(
            STBracedExpressionNode bracedExpressionNode) {
        STNode openParen = modifyNode(bracedExpressionNode.openParen);
        STNode expression = modifyNode(bracedExpressionNode.expression);
        STNode closeParen = modifyNode(bracedExpressionNode.closeParen);
        return bracedExpressionNode.modify(
                bracedExpressionNode.kind,
                openParen,
                expression,
                closeParen);
    }

    @Override
    public STCheckExpressionNode transform(
            STCheckExpressionNode checkExpressionNode) {
        STNode checkKeyword = modifyNode(checkExpressionNode.checkKeyword);
        STNode expression = modifyNode(checkExpressionNode.expression);
        return checkExpressionNode.modify(
                checkExpressionNode.kind,
                checkKeyword,
                expression);
    }

    @Override
    public STFieldAccessExpressionNode transform(
            STFieldAccessExpressionNode fieldAccessExpressionNode) {
        STNode expression = modifyNode(fieldAccessExpressionNode.expression);
        STNode dotToken = modifyNode(fieldAccessExpressionNode.dotToken);
        STNode fieldName = modifyNode(fieldAccessExpressionNode.fieldName);
        return fieldAccessExpressionNode.modify(
                expression,
                dotToken,
                fieldName);
    }

    @Override
    public STFunctionCallExpressionNode transform(
            STFunctionCallExpressionNode functionCallExpressionNode) {
        STNode functionName = modifyNode(functionCallExpressionNode.functionName);
        STNode openParenToken = modifyNode(functionCallExpressionNode.openParenToken);
        STNode arguments = modifyNode(functionCallExpressionNode.arguments);
        STNode closeParenToken = modifyNode(functionCallExpressionNode.closeParenToken);
        return functionCallExpressionNode.modify(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STMethodCallExpressionNode transform(
            STMethodCallExpressionNode methodCallExpressionNode) {
        STNode expression = modifyNode(methodCallExpressionNode.expression);
        STNode dotToken = modifyNode(methodCallExpressionNode.dotToken);
        STNode methodName = modifyNode(methodCallExpressionNode.methodName);
        STNode openParenToken = modifyNode(methodCallExpressionNode.openParenToken);
        STNode arguments = modifyNode(methodCallExpressionNode.arguments);
        STNode closeParenToken = modifyNode(methodCallExpressionNode.closeParenToken);
        return methodCallExpressionNode.modify(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STMappingConstructorExpressionNode transform(
            STMappingConstructorExpressionNode mappingConstructorExpressionNode) {
        STNode openBrace = modifyNode(mappingConstructorExpressionNode.openBrace);
        STNode fields = modifyNode(mappingConstructorExpressionNode.fields);
        STNode closeBrace = modifyNode(mappingConstructorExpressionNode.closeBrace);
        return mappingConstructorExpressionNode.modify(
                openBrace,
                fields,
                closeBrace);
    }

    @Override
    public STIndexedExpressionNode transform(
            STIndexedExpressionNode indexedExpressionNode) {
        STNode containerExpression = modifyNode(indexedExpressionNode.containerExpression);
        STNode openBracket = modifyNode(indexedExpressionNode.openBracket);
        STNode keyExpression = modifyNode(indexedExpressionNode.keyExpression);
        STNode closeBracket = modifyNode(indexedExpressionNode.closeBracket);
        return indexedExpressionNode.modify(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    @Override
    public STTypeofExpressionNode transform(
            STTypeofExpressionNode typeofExpressionNode) {
        STNode typeofKeyword = modifyNode(typeofExpressionNode.typeofKeyword);
        STNode expression = modifyNode(typeofExpressionNode.expression);
        return typeofExpressionNode.modify(
                typeofKeyword,
                expression);
    }

    @Override
    public STUnaryExpressionNode transform(
            STUnaryExpressionNode unaryExpressionNode) {
        STNode unaryOperator = modifyNode(unaryExpressionNode.unaryOperator);
        STNode expression = modifyNode(unaryExpressionNode.expression);
        return unaryExpressionNode.modify(
                unaryOperator,
                expression);
    }

    @Override
    public STComputedNameFieldNode transform(
            STComputedNameFieldNode computedNameFieldNode) {
        STNode openBracket = modifyNode(computedNameFieldNode.openBracket);
        STNode fieldNameExpr = modifyNode(computedNameFieldNode.fieldNameExpr);
        STNode closeBracket = modifyNode(computedNameFieldNode.closeBracket);
        STNode colonToken = modifyNode(computedNameFieldNode.colonToken);
        STNode valueExpr = modifyNode(computedNameFieldNode.valueExpr);
        return computedNameFieldNode.modify(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    @Override
    public STConstantDeclarationNode transform(
            STConstantDeclarationNode constantDeclarationNode) {
        STNode metadata = modifyNode(constantDeclarationNode.metadata);
        STNode visibilityQualifier = modifyNode(constantDeclarationNode.visibilityQualifier);
        STNode constKeyword = modifyNode(constantDeclarationNode.constKeyword);
        STNode typeDescriptor = modifyNode(constantDeclarationNode.typeDescriptor);
        STNode variableName = modifyNode(constantDeclarationNode.variableName);
        STNode equalsToken = modifyNode(constantDeclarationNode.equalsToken);
        STNode initializer = modifyNode(constantDeclarationNode.initializer);
        STNode semicolonToken = modifyNode(constantDeclarationNode.semicolonToken);
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
    public STDefaultableParameterNode transform(
            STDefaultableParameterNode defaultableParameterNode) {
        STNode annotations = modifyNode(defaultableParameterNode.annotations);
        STNode typeName = modifyNode(defaultableParameterNode.typeName);
        STNode paramName = modifyNode(defaultableParameterNode.paramName);
        STNode equalsToken = modifyNode(defaultableParameterNode.equalsToken);
        STNode expression = modifyNode(defaultableParameterNode.expression);
        return defaultableParameterNode.modify(
                annotations,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    @Override
    public STRequiredParameterNode transform(
            STRequiredParameterNode requiredParameterNode) {
        STNode annotations = modifyNode(requiredParameterNode.annotations);
        STNode typeName = modifyNode(requiredParameterNode.typeName);
        STNode paramName = modifyNode(requiredParameterNode.paramName);
        return requiredParameterNode.modify(
                annotations,
                typeName,
                paramName);
    }

    @Override
    public STIncludedRecordParameterNode transform(
            STIncludedRecordParameterNode includedRecordParameterNode) {
        STNode annotations = modifyNode(includedRecordParameterNode.annotations);
        STNode asteriskToken = modifyNode(includedRecordParameterNode.asteriskToken);
        STNode typeName = modifyNode(includedRecordParameterNode.typeName);
        STNode paramName = modifyNode(includedRecordParameterNode.paramName);
        return includedRecordParameterNode.modify(
                annotations,
                asteriskToken,
                typeName,
                paramName);
    }

    @Override
    public STRestParameterNode transform(
            STRestParameterNode restParameterNode) {
        STNode annotations = modifyNode(restParameterNode.annotations);
        STNode typeName = modifyNode(restParameterNode.typeName);
        STNode ellipsisToken = modifyNode(restParameterNode.ellipsisToken);
        STNode paramName = modifyNode(restParameterNode.paramName);
        return restParameterNode.modify(
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    @Override
    public STImportOrgNameNode transform(
            STImportOrgNameNode importOrgNameNode) {
        STNode orgName = modifyNode(importOrgNameNode.orgName);
        STNode slashToken = modifyNode(importOrgNameNode.slashToken);
        return importOrgNameNode.modify(
                orgName,
                slashToken);
    }

    @Override
    public STImportPrefixNode transform(
            STImportPrefixNode importPrefixNode) {
        STNode asKeyword = modifyNode(importPrefixNode.asKeyword);
        STNode prefix = modifyNode(importPrefixNode.prefix);
        return importPrefixNode.modify(
                asKeyword,
                prefix);
    }

    @Override
    public STSpecificFieldNode transform(
            STSpecificFieldNode specificFieldNode) {
        STNode readonlyKeyword = modifyNode(specificFieldNode.readonlyKeyword);
        STNode fieldName = modifyNode(specificFieldNode.fieldName);
        STNode colon = modifyNode(specificFieldNode.colon);
        STNode valueExpr = modifyNode(specificFieldNode.valueExpr);
        return specificFieldNode.modify(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr);
    }

    @Override
    public STSpreadFieldNode transform(
            STSpreadFieldNode spreadFieldNode) {
        STNode ellipsis = modifyNode(spreadFieldNode.ellipsis);
        STNode valueExpr = modifyNode(spreadFieldNode.valueExpr);
        return spreadFieldNode.modify(
                ellipsis,
                valueExpr);
    }

    @Override
    public STNamedArgumentNode transform(
            STNamedArgumentNode namedArgumentNode) {
        STNode argumentName = modifyNode(namedArgumentNode.argumentName);
        STNode equalsToken = modifyNode(namedArgumentNode.equalsToken);
        STNode expression = modifyNode(namedArgumentNode.expression);
        return namedArgumentNode.modify(
                argumentName,
                equalsToken,
                expression);
    }

    @Override
    public STPositionalArgumentNode transform(
            STPositionalArgumentNode positionalArgumentNode) {
        STNode expression = modifyNode(positionalArgumentNode.expression);
        return positionalArgumentNode.modify(
                expression);
    }

    @Override
    public STRestArgumentNode transform(
            STRestArgumentNode restArgumentNode) {
        STNode ellipsis = modifyNode(restArgumentNode.ellipsis);
        STNode expression = modifyNode(restArgumentNode.expression);
        return restArgumentNode.modify(
                ellipsis,
                expression);
    }

    @Override
    public STInferredTypedescDefaultNode transform(
            STInferredTypedescDefaultNode inferredTypedescDefaultNode) {
        STNode ltToken = modifyNode(inferredTypedescDefaultNode.ltToken);
        STNode gtToken = modifyNode(inferredTypedescDefaultNode.gtToken);
        return inferredTypedescDefaultNode.modify(
                ltToken,
                gtToken);
    }

    @Override
    public STObjectTypeDescriptorNode transform(
            STObjectTypeDescriptorNode objectTypeDescriptorNode) {
        STNode objectTypeQualifiers = modifyNode(objectTypeDescriptorNode.objectTypeQualifiers);
        STNode objectKeyword = modifyNode(objectTypeDescriptorNode.objectKeyword);
        STNode openBrace = modifyNode(objectTypeDescriptorNode.openBrace);
        STNode members = modifyNode(objectTypeDescriptorNode.members);
        STNode closeBrace = modifyNode(objectTypeDescriptorNode.closeBrace);
        return objectTypeDescriptorNode.modify(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    @Override
    public STObjectConstructorExpressionNode transform(
            STObjectConstructorExpressionNode objectConstructorExpressionNode) {
        STNode annotations = modifyNode(objectConstructorExpressionNode.annotations);
        STNode objectTypeQualifiers = modifyNode(objectConstructorExpressionNode.objectTypeQualifiers);
        STNode objectKeyword = modifyNode(objectConstructorExpressionNode.objectKeyword);
        STNode typeReference = modifyNode(objectConstructorExpressionNode.typeReference);
        STNode openBraceToken = modifyNode(objectConstructorExpressionNode.openBraceToken);
        STNode members = modifyNode(objectConstructorExpressionNode.members);
        STNode closeBraceToken = modifyNode(objectConstructorExpressionNode.closeBraceToken);
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
    public STRecordTypeDescriptorNode transform(
            STRecordTypeDescriptorNode recordTypeDescriptorNode) {
        STNode recordKeyword = modifyNode(recordTypeDescriptorNode.recordKeyword);
        STNode bodyStartDelimiter = modifyNode(recordTypeDescriptorNode.bodyStartDelimiter);
        STNode fields = modifyNode(recordTypeDescriptorNode.fields);
        STNode recordRestDescriptor = modifyNode(recordTypeDescriptorNode.recordRestDescriptor);
        STNode bodyEndDelimiter = modifyNode(recordTypeDescriptorNode.bodyEndDelimiter);
        return recordTypeDescriptorNode.modify(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
                bodyEndDelimiter);
    }

    @Override
    public STReturnTypeDescriptorNode transform(
            STReturnTypeDescriptorNode returnTypeDescriptorNode) {
        STNode returnsKeyword = modifyNode(returnTypeDescriptorNode.returnsKeyword);
        STNode annotations = modifyNode(returnTypeDescriptorNode.annotations);
        STNode type = modifyNode(returnTypeDescriptorNode.type);
        return returnTypeDescriptorNode.modify(
                returnsKeyword,
                annotations,
                type);
    }

    @Override
    public STNilTypeDescriptorNode transform(
            STNilTypeDescriptorNode nilTypeDescriptorNode) {
        STNode openParenToken = modifyNode(nilTypeDescriptorNode.openParenToken);
        STNode closeParenToken = modifyNode(nilTypeDescriptorNode.closeParenToken);
        return nilTypeDescriptorNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public STOptionalTypeDescriptorNode transform(
            STOptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        STNode typeDescriptor = modifyNode(optionalTypeDescriptorNode.typeDescriptor);
        STNode questionMarkToken = modifyNode(optionalTypeDescriptorNode.questionMarkToken);
        return optionalTypeDescriptorNode.modify(
                typeDescriptor,
                questionMarkToken);
    }

    @Override
    public STObjectFieldNode transform(
            STObjectFieldNode objectFieldNode) {
        STNode metadata = modifyNode(objectFieldNode.metadata);
        STNode visibilityQualifier = modifyNode(objectFieldNode.visibilityQualifier);
        STNode qualifierList = modifyNode(objectFieldNode.qualifierList);
        STNode typeName = modifyNode(objectFieldNode.typeName);
        STNode fieldName = modifyNode(objectFieldNode.fieldName);
        STNode equalsToken = modifyNode(objectFieldNode.equalsToken);
        STNode expression = modifyNode(objectFieldNode.expression);
        STNode semicolonToken = modifyNode(objectFieldNode.semicolonToken);
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
    public STRecordFieldNode transform(
            STRecordFieldNode recordFieldNode) {
        STNode metadata = modifyNode(recordFieldNode.metadata);
        STNode readonlyKeyword = modifyNode(recordFieldNode.readonlyKeyword);
        STNode typeName = modifyNode(recordFieldNode.typeName);
        STNode fieldName = modifyNode(recordFieldNode.fieldName);
        STNode questionMarkToken = modifyNode(recordFieldNode.questionMarkToken);
        STNode semicolonToken = modifyNode(recordFieldNode.semicolonToken);
        return recordFieldNode.modify(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    @Override
    public STRecordFieldWithDefaultValueNode transform(
            STRecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        STNode metadata = modifyNode(recordFieldWithDefaultValueNode.metadata);
        STNode readonlyKeyword = modifyNode(recordFieldWithDefaultValueNode.readonlyKeyword);
        STNode typeName = modifyNode(recordFieldWithDefaultValueNode.typeName);
        STNode fieldName = modifyNode(recordFieldWithDefaultValueNode.fieldName);
        STNode equalsToken = modifyNode(recordFieldWithDefaultValueNode.equalsToken);
        STNode expression = modifyNode(recordFieldWithDefaultValueNode.expression);
        STNode semicolonToken = modifyNode(recordFieldWithDefaultValueNode.semicolonToken);
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
    public STRecordRestDescriptorNode transform(
            STRecordRestDescriptorNode recordRestDescriptorNode) {
        STNode typeName = modifyNode(recordRestDescriptorNode.typeName);
        STNode ellipsisToken = modifyNode(recordRestDescriptorNode.ellipsisToken);
        STNode semicolonToken = modifyNode(recordRestDescriptorNode.semicolonToken);
        return recordRestDescriptorNode.modify(
                typeName,
                ellipsisToken,
                semicolonToken);
    }

    @Override
    public STTypeReferenceNode transform(
            STTypeReferenceNode typeReferenceNode) {
        STNode asteriskToken = modifyNode(typeReferenceNode.asteriskToken);
        STNode typeName = modifyNode(typeReferenceNode.typeName);
        STNode semicolonToken = modifyNode(typeReferenceNode.semicolonToken);
        return typeReferenceNode.modify(
                asteriskToken,
                typeName,
                semicolonToken);
    }

    @Override
    public STAnnotationNode transform(
            STAnnotationNode annotationNode) {
        STNode atToken = modifyNode(annotationNode.atToken);
        STNode annotReference = modifyNode(annotationNode.annotReference);
        STNode annotValue = modifyNode(annotationNode.annotValue);
        return annotationNode.modify(
                atToken,
                annotReference,
                annotValue);
    }

    @Override
    public STMetadataNode transform(
            STMetadataNode metadataNode) {
        STNode documentationString = modifyNode(metadataNode.documentationString);
        STNode annotations = modifyNode(metadataNode.annotations);
        return metadataNode.modify(
                documentationString,
                annotations);
    }

    @Override
    public STModuleVariableDeclarationNode transform(
            STModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        STNode metadata = modifyNode(moduleVariableDeclarationNode.metadata);
        STNode visibilityQualifier = modifyNode(moduleVariableDeclarationNode.visibilityQualifier);
        STNode qualifiers = modifyNode(moduleVariableDeclarationNode.qualifiers);
        STNode typedBindingPattern = modifyNode(moduleVariableDeclarationNode.typedBindingPattern);
        STNode equalsToken = modifyNode(moduleVariableDeclarationNode.equalsToken);
        STNode initializer = modifyNode(moduleVariableDeclarationNode.initializer);
        STNode semicolonToken = modifyNode(moduleVariableDeclarationNode.semicolonToken);
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
    public STTypeTestExpressionNode transform(
            STTypeTestExpressionNode typeTestExpressionNode) {
        STNode expression = modifyNode(typeTestExpressionNode.expression);
        STNode isKeyword = modifyNode(typeTestExpressionNode.isKeyword);
        STNode typeDescriptor = modifyNode(typeTestExpressionNode.typeDescriptor);
        return typeTestExpressionNode.modify(
                expression,
                isKeyword,
                typeDescriptor);
    }

    @Override
    public STRemoteMethodCallActionNode transform(
            STRemoteMethodCallActionNode remoteMethodCallActionNode) {
        STNode expression = modifyNode(remoteMethodCallActionNode.expression);
        STNode rightArrowToken = modifyNode(remoteMethodCallActionNode.rightArrowToken);
        STNode methodName = modifyNode(remoteMethodCallActionNode.methodName);
        STNode openParenToken = modifyNode(remoteMethodCallActionNode.openParenToken);
        STNode arguments = modifyNode(remoteMethodCallActionNode.arguments);
        STNode closeParenToken = modifyNode(remoteMethodCallActionNode.closeParenToken);
        return remoteMethodCallActionNode.modify(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STMapTypeDescriptorNode transform(
            STMapTypeDescriptorNode mapTypeDescriptorNode) {
        STNode mapKeywordToken = modifyNode(mapTypeDescriptorNode.mapKeywordToken);
        STNode mapTypeParamsNode = modifyNode(mapTypeDescriptorNode.mapTypeParamsNode);
        return mapTypeDescriptorNode.modify(
                mapKeywordToken,
                mapTypeParamsNode);
    }

    @Override
    public STNilLiteralNode transform(
            STNilLiteralNode nilLiteralNode) {
        STNode openParenToken = modifyNode(nilLiteralNode.openParenToken);
        STNode closeParenToken = modifyNode(nilLiteralNode.closeParenToken);
        return nilLiteralNode.modify(
                openParenToken,
                closeParenToken);
    }

    @Override
    public STAnnotationDeclarationNode transform(
            STAnnotationDeclarationNode annotationDeclarationNode) {
        STNode metadata = modifyNode(annotationDeclarationNode.metadata);
        STNode visibilityQualifier = modifyNode(annotationDeclarationNode.visibilityQualifier);
        STNode constKeyword = modifyNode(annotationDeclarationNode.constKeyword);
        STNode annotationKeyword = modifyNode(annotationDeclarationNode.annotationKeyword);
        STNode typeDescriptor = modifyNode(annotationDeclarationNode.typeDescriptor);
        STNode annotationTag = modifyNode(annotationDeclarationNode.annotationTag);
        STNode onKeyword = modifyNode(annotationDeclarationNode.onKeyword);
        STNode attachPoints = modifyNode(annotationDeclarationNode.attachPoints);
        STNode semicolonToken = modifyNode(annotationDeclarationNode.semicolonToken);
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
    public STAnnotationAttachPointNode transform(
            STAnnotationAttachPointNode annotationAttachPointNode) {
        STNode sourceKeyword = modifyNode(annotationAttachPointNode.sourceKeyword);
        STNode identifiers = modifyNode(annotationAttachPointNode.identifiers);
        return annotationAttachPointNode.modify(
                sourceKeyword,
                identifiers);
    }

    @Override
    public STXMLNamespaceDeclarationNode transform(
            STXMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        STNode xmlnsKeyword = modifyNode(xMLNamespaceDeclarationNode.xmlnsKeyword);
        STNode namespaceuri = modifyNode(xMLNamespaceDeclarationNode.namespaceuri);
        STNode asKeyword = modifyNode(xMLNamespaceDeclarationNode.asKeyword);
        STNode namespacePrefix = modifyNode(xMLNamespaceDeclarationNode.namespacePrefix);
        STNode semicolonToken = modifyNode(xMLNamespaceDeclarationNode.semicolonToken);
        return xMLNamespaceDeclarationNode.modify(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    @Override
    public STModuleXMLNamespaceDeclarationNode transform(
            STModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        STNode xmlnsKeyword = modifyNode(moduleXMLNamespaceDeclarationNode.xmlnsKeyword);
        STNode namespaceuri = modifyNode(moduleXMLNamespaceDeclarationNode.namespaceuri);
        STNode asKeyword = modifyNode(moduleXMLNamespaceDeclarationNode.asKeyword);
        STNode namespacePrefix = modifyNode(moduleXMLNamespaceDeclarationNode.namespacePrefix);
        STNode semicolonToken = modifyNode(moduleXMLNamespaceDeclarationNode.semicolonToken);
        return moduleXMLNamespaceDeclarationNode.modify(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    @Override
    public STFunctionBodyBlockNode transform(
            STFunctionBodyBlockNode functionBodyBlockNode) {
        STNode openBraceToken = modifyNode(functionBodyBlockNode.openBraceToken);
        STNode namedWorkerDeclarator = modifyNode(functionBodyBlockNode.namedWorkerDeclarator);
        STNode statements = modifyNode(functionBodyBlockNode.statements);
        STNode closeBraceToken = modifyNode(functionBodyBlockNode.closeBraceToken);
        STNode semicolonToken = modifyNode(functionBodyBlockNode.semicolonToken);
        return functionBodyBlockNode.modify(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken);
    }

    @Override
    public STNamedWorkerDeclarationNode transform(
            STNamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        STNode annotations = modifyNode(namedWorkerDeclarationNode.annotations);
        STNode transactionalKeyword = modifyNode(namedWorkerDeclarationNode.transactionalKeyword);
        STNode workerKeyword = modifyNode(namedWorkerDeclarationNode.workerKeyword);
        STNode workerName = modifyNode(namedWorkerDeclarationNode.workerName);
        STNode returnTypeDesc = modifyNode(namedWorkerDeclarationNode.returnTypeDesc);
        STNode workerBody = modifyNode(namedWorkerDeclarationNode.workerBody);
        return namedWorkerDeclarationNode.modify(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    @Override
    public STNamedWorkerDeclarator transform(
            STNamedWorkerDeclarator namedWorkerDeclarator) {
        STNode workerInitStatements = modifyNode(namedWorkerDeclarator.workerInitStatements);
        STNode namedWorkerDeclarations = modifyNode(namedWorkerDeclarator.namedWorkerDeclarations);
        return namedWorkerDeclarator.modify(
                workerInitStatements,
                namedWorkerDeclarations);
    }

    @Override
    public STBasicLiteralNode transform(
            STBasicLiteralNode basicLiteralNode) {
        STNode literalToken = modifyNode(basicLiteralNode.literalToken);
        return basicLiteralNode.modify(
                basicLiteralNode.kind,
                literalToken);
    }

    @Override
    public STSimpleNameReferenceNode transform(
            STSimpleNameReferenceNode simpleNameReferenceNode) {
        STNode name = modifyNode(simpleNameReferenceNode.name);
        return simpleNameReferenceNode.modify(
                name);
    }

    @Override
    public STQualifiedNameReferenceNode transform(
            STQualifiedNameReferenceNode qualifiedNameReferenceNode) {
        STNode modulePrefix = modifyNode(qualifiedNameReferenceNode.modulePrefix);
        STNode colon = modifyNode(qualifiedNameReferenceNode.colon);
        STNode identifier = modifyNode(qualifiedNameReferenceNode.identifier);
        return qualifiedNameReferenceNode.modify(
                modulePrefix,
                colon,
                identifier);
    }

    @Override
    public STBuiltinSimpleNameReferenceNode transform(
            STBuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        STNode name = modifyNode(builtinSimpleNameReferenceNode.name);
        return builtinSimpleNameReferenceNode.modify(
                builtinSimpleNameReferenceNode.kind,
                name);
    }

    @Override
    public STTrapExpressionNode transform(
            STTrapExpressionNode trapExpressionNode) {
        STNode trapKeyword = modifyNode(trapExpressionNode.trapKeyword);
        STNode expression = modifyNode(trapExpressionNode.expression);
        return trapExpressionNode.modify(
                trapExpressionNode.kind,
                trapKeyword,
                expression);
    }

    @Override
    public STListConstructorExpressionNode transform(
            STListConstructorExpressionNode listConstructorExpressionNode) {
        STNode openBracket = modifyNode(listConstructorExpressionNode.openBracket);
        STNode expressions = modifyNode(listConstructorExpressionNode.expressions);
        STNode closeBracket = modifyNode(listConstructorExpressionNode.closeBracket);
        return listConstructorExpressionNode.modify(
                openBracket,
                expressions,
                closeBracket);
    }

    @Override
    public STTypeCastExpressionNode transform(
            STTypeCastExpressionNode typeCastExpressionNode) {
        STNode ltToken = modifyNode(typeCastExpressionNode.ltToken);
        STNode typeCastParam = modifyNode(typeCastExpressionNode.typeCastParam);
        STNode gtToken = modifyNode(typeCastExpressionNode.gtToken);
        STNode expression = modifyNode(typeCastExpressionNode.expression);
        return typeCastExpressionNode.modify(
                ltToken,
                typeCastParam,
                gtToken,
                expression);
    }

    @Override
    public STTypeCastParamNode transform(
            STTypeCastParamNode typeCastParamNode) {
        STNode annotations = modifyNode(typeCastParamNode.annotations);
        STNode type = modifyNode(typeCastParamNode.type);
        return typeCastParamNode.modify(
                annotations,
                type);
    }

    @Override
    public STUnionTypeDescriptorNode transform(
            STUnionTypeDescriptorNode unionTypeDescriptorNode) {
        STNode leftTypeDesc = modifyNode(unionTypeDescriptorNode.leftTypeDesc);
        STNode pipeToken = modifyNode(unionTypeDescriptorNode.pipeToken);
        STNode rightTypeDesc = modifyNode(unionTypeDescriptorNode.rightTypeDesc);
        return unionTypeDescriptorNode.modify(
                leftTypeDesc,
                pipeToken,
                rightTypeDesc);
    }

    @Override
    public STTableConstructorExpressionNode transform(
            STTableConstructorExpressionNode tableConstructorExpressionNode) {
        STNode tableKeyword = modifyNode(tableConstructorExpressionNode.tableKeyword);
        STNode keySpecifier = modifyNode(tableConstructorExpressionNode.keySpecifier);
        STNode openBracket = modifyNode(tableConstructorExpressionNode.openBracket);
        STNode rows = modifyNode(tableConstructorExpressionNode.rows);
        STNode closeBracket = modifyNode(tableConstructorExpressionNode.closeBracket);
        return tableConstructorExpressionNode.modify(
                tableKeyword,
                keySpecifier,
                openBracket,
                rows,
                closeBracket);
    }

    @Override
    public STKeySpecifierNode transform(
            STKeySpecifierNode keySpecifierNode) {
        STNode keyKeyword = modifyNode(keySpecifierNode.keyKeyword);
        STNode openParenToken = modifyNode(keySpecifierNode.openParenToken);
        STNode fieldNames = modifyNode(keySpecifierNode.fieldNames);
        STNode closeParenToken = modifyNode(keySpecifierNode.closeParenToken);
        return keySpecifierNode.modify(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    @Override
    public STStreamTypeDescriptorNode transform(
            STStreamTypeDescriptorNode streamTypeDescriptorNode) {
        STNode streamKeywordToken = modifyNode(streamTypeDescriptorNode.streamKeywordToken);
        STNode streamTypeParamsNode = modifyNode(streamTypeDescriptorNode.streamTypeParamsNode);
        return streamTypeDescriptorNode.modify(
                streamKeywordToken,
                streamTypeParamsNode);
    }

    @Override
    public STStreamTypeParamsNode transform(
            STStreamTypeParamsNode streamTypeParamsNode) {
        STNode ltToken = modifyNode(streamTypeParamsNode.ltToken);
        STNode leftTypeDescNode = modifyNode(streamTypeParamsNode.leftTypeDescNode);
        STNode commaToken = modifyNode(streamTypeParamsNode.commaToken);
        STNode rightTypeDescNode = modifyNode(streamTypeParamsNode.rightTypeDescNode);
        STNode gtToken = modifyNode(streamTypeParamsNode.gtToken);
        return streamTypeParamsNode.modify(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    @Override
    public STLetExpressionNode transform(
            STLetExpressionNode letExpressionNode) {
        STNode letKeyword = modifyNode(letExpressionNode.letKeyword);
        STNode letVarDeclarations = modifyNode(letExpressionNode.letVarDeclarations);
        STNode inKeyword = modifyNode(letExpressionNode.inKeyword);
        STNode expression = modifyNode(letExpressionNode.expression);
        return letExpressionNode.modify(
                letKeyword,
                letVarDeclarations,
                inKeyword,
                expression);
    }

    @Override
    public STLetVariableDeclarationNode transform(
            STLetVariableDeclarationNode letVariableDeclarationNode) {
        STNode annotations = modifyNode(letVariableDeclarationNode.annotations);
        STNode typedBindingPattern = modifyNode(letVariableDeclarationNode.typedBindingPattern);
        STNode equalsToken = modifyNode(letVariableDeclarationNode.equalsToken);
        STNode expression = modifyNode(letVariableDeclarationNode.expression);
        return letVariableDeclarationNode.modify(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression);
    }

    @Override
    public STTemplateExpressionNode transform(
            STTemplateExpressionNode templateExpressionNode) {
        STNode type = modifyNode(templateExpressionNode.type);
        STNode startBacktick = modifyNode(templateExpressionNode.startBacktick);
        STNode content = modifyNode(templateExpressionNode.content);
        STNode endBacktick = modifyNode(templateExpressionNode.endBacktick);
        return templateExpressionNode.modify(
                templateExpressionNode.kind,
                type,
                startBacktick,
                content,
                endBacktick);
    }

    @Override
    public STXMLElementNode transform(
            STXMLElementNode xMLElementNode) {
        STNode startTag = modifyNode(xMLElementNode.startTag);
        STNode content = modifyNode(xMLElementNode.content);
        STNode endTag = modifyNode(xMLElementNode.endTag);
        return xMLElementNode.modify(
                startTag,
                content,
                endTag);
    }

    @Override
    public STXMLStartTagNode transform(
            STXMLStartTagNode xMLStartTagNode) {
        STNode ltToken = modifyNode(xMLStartTagNode.ltToken);
        STNode name = modifyNode(xMLStartTagNode.name);
        STNode attributes = modifyNode(xMLStartTagNode.attributes);
        STNode getToken = modifyNode(xMLStartTagNode.getToken);
        return xMLStartTagNode.modify(
                ltToken,
                name,
                attributes,
                getToken);
    }

    @Override
    public STXMLEndTagNode transform(
            STXMLEndTagNode xMLEndTagNode) {
        STNode ltToken = modifyNode(xMLEndTagNode.ltToken);
        STNode slashToken = modifyNode(xMLEndTagNode.slashToken);
        STNode name = modifyNode(xMLEndTagNode.name);
        STNode getToken = modifyNode(xMLEndTagNode.getToken);
        return xMLEndTagNode.modify(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    @Override
    public STXMLSimpleNameNode transform(
            STXMLSimpleNameNode xMLSimpleNameNode) {
        STNode name = modifyNode(xMLSimpleNameNode.name);
        return xMLSimpleNameNode.modify(
                name);
    }

    @Override
    public STXMLQualifiedNameNode transform(
            STXMLQualifiedNameNode xMLQualifiedNameNode) {
        STNode prefix = modifyNode(xMLQualifiedNameNode.prefix);
        STNode colon = modifyNode(xMLQualifiedNameNode.colon);
        STNode name = modifyNode(xMLQualifiedNameNode.name);
        return xMLQualifiedNameNode.modify(
                prefix,
                colon,
                name);
    }

    @Override
    public STXMLEmptyElementNode transform(
            STXMLEmptyElementNode xMLEmptyElementNode) {
        STNode ltToken = modifyNode(xMLEmptyElementNode.ltToken);
        STNode name = modifyNode(xMLEmptyElementNode.name);
        STNode attributes = modifyNode(xMLEmptyElementNode.attributes);
        STNode slashToken = modifyNode(xMLEmptyElementNode.slashToken);
        STNode getToken = modifyNode(xMLEmptyElementNode.getToken);
        return xMLEmptyElementNode.modify(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    @Override
    public STInterpolationNode transform(
            STInterpolationNode interpolationNode) {
        STNode interpolationStartToken = modifyNode(interpolationNode.interpolationStartToken);
        STNode expression = modifyNode(interpolationNode.expression);
        STNode interpolationEndToken = modifyNode(interpolationNode.interpolationEndToken);
        return interpolationNode.modify(
                interpolationStartToken,
                expression,
                interpolationEndToken);
    }

    @Override
    public STXMLTextNode transform(
            STXMLTextNode xMLTextNode) {
        STNode content = modifyNode(xMLTextNode.content);
        return xMLTextNode.modify(
                content);
    }

    @Override
    public STXMLAttributeNode transform(
            STXMLAttributeNode xMLAttributeNode) {
        STNode attributeName = modifyNode(xMLAttributeNode.attributeName);
        STNode equalToken = modifyNode(xMLAttributeNode.equalToken);
        STNode value = modifyNode(xMLAttributeNode.value);
        return xMLAttributeNode.modify(
                attributeName,
                equalToken,
                value);
    }

    @Override
    public STXMLAttributeValue transform(
            STXMLAttributeValue xMLAttributeValue) {
        STNode startQuote = modifyNode(xMLAttributeValue.startQuote);
        STNode value = modifyNode(xMLAttributeValue.value);
        STNode endQuote = modifyNode(xMLAttributeValue.endQuote);
        return xMLAttributeValue.modify(
                startQuote,
                value,
                endQuote);
    }

    @Override
    public STXMLComment transform(
            STXMLComment xMLComment) {
        STNode commentStart = modifyNode(xMLComment.commentStart);
        STNode content = modifyNode(xMLComment.content);
        STNode commentEnd = modifyNode(xMLComment.commentEnd);
        return xMLComment.modify(
                commentStart,
                content,
                commentEnd);
    }

    @Override
    public STXMLCDATANode transform(
            STXMLCDATANode xMLCDATANode) {
        STNode cdataStart = modifyNode(xMLCDATANode.cdataStart);
        STNode content = modifyNode(xMLCDATANode.content);
        STNode cdataEnd = modifyNode(xMLCDATANode.cdataEnd);
        return xMLCDATANode.modify(
                cdataStart,
                content,
                cdataEnd);
    }

    @Override
    public STXMLProcessingInstruction transform(
            STXMLProcessingInstruction xMLProcessingInstruction) {
        STNode piStart = modifyNode(xMLProcessingInstruction.piStart);
        STNode target = modifyNode(xMLProcessingInstruction.target);
        STNode data = modifyNode(xMLProcessingInstruction.data);
        STNode piEnd = modifyNode(xMLProcessingInstruction.piEnd);
        return xMLProcessingInstruction.modify(
                piStart,
                target,
                data,
                piEnd);
    }

    @Override
    public STTableTypeDescriptorNode transform(
            STTableTypeDescriptorNode tableTypeDescriptorNode) {
        STNode tableKeywordToken = modifyNode(tableTypeDescriptorNode.tableKeywordToken);
        STNode rowTypeParameterNode = modifyNode(tableTypeDescriptorNode.rowTypeParameterNode);
        STNode keyConstraintNode = modifyNode(tableTypeDescriptorNode.keyConstraintNode);
        return tableTypeDescriptorNode.modify(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode);
    }

    @Override
    public STTypeParameterNode transform(
            STTypeParameterNode typeParameterNode) {
        STNode ltToken = modifyNode(typeParameterNode.ltToken);
        STNode typeNode = modifyNode(typeParameterNode.typeNode);
        STNode gtToken = modifyNode(typeParameterNode.gtToken);
        return typeParameterNode.modify(
                ltToken,
                typeNode,
                gtToken);
    }

    @Override
    public STKeyTypeConstraintNode transform(
            STKeyTypeConstraintNode keyTypeConstraintNode) {
        STNode keyKeywordToken = modifyNode(keyTypeConstraintNode.keyKeywordToken);
        STNode typeParameterNode = modifyNode(keyTypeConstraintNode.typeParameterNode);
        return keyTypeConstraintNode.modify(
                keyKeywordToken,
                typeParameterNode);
    }

    @Override
    public STFunctionTypeDescriptorNode transform(
            STFunctionTypeDescriptorNode functionTypeDescriptorNode) {
        STNode qualifierList = modifyNode(functionTypeDescriptorNode.qualifierList);
        STNode functionKeyword = modifyNode(functionTypeDescriptorNode.functionKeyword);
        STNode functionSignature = modifyNode(functionTypeDescriptorNode.functionSignature);
        return functionTypeDescriptorNode.modify(
                qualifierList,
                functionKeyword,
                functionSignature);
    }

    @Override
    public STFunctionSignatureNode transform(
            STFunctionSignatureNode functionSignatureNode) {
        STNode openParenToken = modifyNode(functionSignatureNode.openParenToken);
        STNode parameters = modifyNode(functionSignatureNode.parameters);
        STNode closeParenToken = modifyNode(functionSignatureNode.closeParenToken);
        STNode returnTypeDesc = modifyNode(functionSignatureNode.returnTypeDesc);
        return functionSignatureNode.modify(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc);
    }

    @Override
    public STExplicitAnonymousFunctionExpressionNode transform(
            STExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        STNode annotations = modifyNode(explicitAnonymousFunctionExpressionNode.annotations);
        STNode qualifierList = modifyNode(explicitAnonymousFunctionExpressionNode.qualifierList);
        STNode functionKeyword = modifyNode(explicitAnonymousFunctionExpressionNode.functionKeyword);
        STNode functionSignature = modifyNode(explicitAnonymousFunctionExpressionNode.functionSignature);
        STNode functionBody = modifyNode(explicitAnonymousFunctionExpressionNode.functionBody);
        return explicitAnonymousFunctionExpressionNode.modify(
                annotations,
                qualifierList,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    @Override
    public STExpressionFunctionBodyNode transform(
            STExpressionFunctionBodyNode expressionFunctionBodyNode) {
        STNode rightDoubleArrow = modifyNode(expressionFunctionBodyNode.rightDoubleArrow);
        STNode expression = modifyNode(expressionFunctionBodyNode.expression);
        STNode semicolon = modifyNode(expressionFunctionBodyNode.semicolon);
        return expressionFunctionBodyNode.modify(
                rightDoubleArrow,
                expression,
                semicolon);
    }

    @Override
    public STTupleTypeDescriptorNode transform(
            STTupleTypeDescriptorNode tupleTypeDescriptorNode) {
        STNode openBracketToken = modifyNode(tupleTypeDescriptorNode.openBracketToken);
        STNode memberTypeDesc = modifyNode(tupleTypeDescriptorNode.memberTypeDesc);
        STNode closeBracketToken = modifyNode(tupleTypeDescriptorNode.closeBracketToken);
        return tupleTypeDescriptorNode.modify(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken);
    }

    @Override
    public STParenthesisedTypeDescriptorNode transform(
            STParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        STNode openParenToken = modifyNode(parenthesisedTypeDescriptorNode.openParenToken);
        STNode typedesc = modifyNode(parenthesisedTypeDescriptorNode.typedesc);
        STNode closeParenToken = modifyNode(parenthesisedTypeDescriptorNode.closeParenToken);
        return parenthesisedTypeDescriptorNode.modify(
                openParenToken,
                typedesc,
                closeParenToken);
    }

    @Override
    public STExplicitNewExpressionNode transform(
            STExplicitNewExpressionNode explicitNewExpressionNode) {
        STNode newKeyword = modifyNode(explicitNewExpressionNode.newKeyword);
        STNode typeDescriptor = modifyNode(explicitNewExpressionNode.typeDescriptor);
        STNode parenthesizedArgList = modifyNode(explicitNewExpressionNode.parenthesizedArgList);
        return explicitNewExpressionNode.modify(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList);
    }

    @Override
    public STImplicitNewExpressionNode transform(
            STImplicitNewExpressionNode implicitNewExpressionNode) {
        STNode newKeyword = modifyNode(implicitNewExpressionNode.newKeyword);
        STNode parenthesizedArgList = modifyNode(implicitNewExpressionNode.parenthesizedArgList);
        return implicitNewExpressionNode.modify(
                newKeyword,
                parenthesizedArgList);
    }

    @Override
    public STParenthesizedArgList transform(
            STParenthesizedArgList parenthesizedArgList) {
        STNode openParenToken = modifyNode(parenthesizedArgList.openParenToken);
        STNode arguments = modifyNode(parenthesizedArgList.arguments);
        STNode closeParenToken = modifyNode(parenthesizedArgList.closeParenToken);
        return parenthesizedArgList.modify(
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STQueryConstructTypeNode transform(
            STQueryConstructTypeNode queryConstructTypeNode) {
        STNode keyword = modifyNode(queryConstructTypeNode.keyword);
        STNode keySpecifier = modifyNode(queryConstructTypeNode.keySpecifier);
        return queryConstructTypeNode.modify(
                keyword,
                keySpecifier);
    }

    @Override
    public STFromClauseNode transform(
            STFromClauseNode fromClauseNode) {
        STNode fromKeyword = modifyNode(fromClauseNode.fromKeyword);
        STNode typedBindingPattern = modifyNode(fromClauseNode.typedBindingPattern);
        STNode inKeyword = modifyNode(fromClauseNode.inKeyword);
        STNode expression = modifyNode(fromClauseNode.expression);
        return fromClauseNode.modify(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression);
    }

    @Override
    public STWhereClauseNode transform(
            STWhereClauseNode whereClauseNode) {
        STNode whereKeyword = modifyNode(whereClauseNode.whereKeyword);
        STNode expression = modifyNode(whereClauseNode.expression);
        return whereClauseNode.modify(
                whereKeyword,
                expression);
    }

    @Override
    public STLetClauseNode transform(
            STLetClauseNode letClauseNode) {
        STNode letKeyword = modifyNode(letClauseNode.letKeyword);
        STNode letVarDeclarations = modifyNode(letClauseNode.letVarDeclarations);
        return letClauseNode.modify(
                letKeyword,
                letVarDeclarations);
    }

    @Override
    public STJoinClauseNode transform(
            STJoinClauseNode joinClauseNode) {
        STNode outerKeyword = modifyNode(joinClauseNode.outerKeyword);
        STNode joinKeyword = modifyNode(joinClauseNode.joinKeyword);
        STNode typedBindingPattern = modifyNode(joinClauseNode.typedBindingPattern);
        STNode inKeyword = modifyNode(joinClauseNode.inKeyword);
        STNode expression = modifyNode(joinClauseNode.expression);
        STNode joinOnCondition = modifyNode(joinClauseNode.joinOnCondition);
        return joinClauseNode.modify(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition);
    }

    @Override
    public STOnClauseNode transform(
            STOnClauseNode onClauseNode) {
        STNode onKeyword = modifyNode(onClauseNode.onKeyword);
        STNode lhsExpression = modifyNode(onClauseNode.lhsExpression);
        STNode equalsKeyword = modifyNode(onClauseNode.equalsKeyword);
        STNode rhsExpression = modifyNode(onClauseNode.rhsExpression);
        return onClauseNode.modify(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression);
    }

    @Override
    public STLimitClauseNode transform(
            STLimitClauseNode limitClauseNode) {
        STNode limitKeyword = modifyNode(limitClauseNode.limitKeyword);
        STNode expression = modifyNode(limitClauseNode.expression);
        return limitClauseNode.modify(
                limitKeyword,
                expression);
    }

    @Override
    public STOnConflictClauseNode transform(
            STOnConflictClauseNode onConflictClauseNode) {
        STNode onKeyword = modifyNode(onConflictClauseNode.onKeyword);
        STNode conflictKeyword = modifyNode(onConflictClauseNode.conflictKeyword);
        STNode expression = modifyNode(onConflictClauseNode.expression);
        return onConflictClauseNode.modify(
                onKeyword,
                conflictKeyword,
                expression);
    }

    @Override
    public STQueryPipelineNode transform(
            STQueryPipelineNode queryPipelineNode) {
        STNode fromClause = modifyNode(queryPipelineNode.fromClause);
        STNode intermediateClauses = modifyNode(queryPipelineNode.intermediateClauses);
        return queryPipelineNode.modify(
                fromClause,
                intermediateClauses);
    }

    @Override
    public STSelectClauseNode transform(
            STSelectClauseNode selectClauseNode) {
        STNode selectKeyword = modifyNode(selectClauseNode.selectKeyword);
        STNode expression = modifyNode(selectClauseNode.expression);
        return selectClauseNode.modify(
                selectKeyword,
                expression);
    }

    @Override
    public STQueryExpressionNode transform(
            STQueryExpressionNode queryExpressionNode) {
        STNode queryConstructType = modifyNode(queryExpressionNode.queryConstructType);
        STNode queryPipeline = modifyNode(queryExpressionNode.queryPipeline);
        STNode selectClause = modifyNode(queryExpressionNode.selectClause);
        STNode onConflictClause = modifyNode(queryExpressionNode.onConflictClause);
        return queryExpressionNode.modify(
                queryConstructType,
                queryPipeline,
                selectClause,
                onConflictClause);
    }

    @Override
    public STQueryActionNode transform(
            STQueryActionNode queryActionNode) {
        STNode queryPipeline = modifyNode(queryActionNode.queryPipeline);
        STNode doKeyword = modifyNode(queryActionNode.doKeyword);
        STNode blockStatement = modifyNode(queryActionNode.blockStatement);
        return queryActionNode.modify(
                queryPipeline,
                doKeyword,
                blockStatement);
    }

    @Override
    public STIntersectionTypeDescriptorNode transform(
            STIntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        STNode leftTypeDesc = modifyNode(intersectionTypeDescriptorNode.leftTypeDesc);
        STNode bitwiseAndToken = modifyNode(intersectionTypeDescriptorNode.bitwiseAndToken);
        STNode rightTypeDesc = modifyNode(intersectionTypeDescriptorNode.rightTypeDesc);
        return intersectionTypeDescriptorNode.modify(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc);
    }

    @Override
    public STImplicitAnonymousFunctionParameters transform(
            STImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        STNode openParenToken = modifyNode(implicitAnonymousFunctionParameters.openParenToken);
        STNode parameters = modifyNode(implicitAnonymousFunctionParameters.parameters);
        STNode closeParenToken = modifyNode(implicitAnonymousFunctionParameters.closeParenToken);
        return implicitAnonymousFunctionParameters.modify(
                openParenToken,
                parameters,
                closeParenToken);
    }

    @Override
    public STImplicitAnonymousFunctionExpressionNode transform(
            STImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        STNode params = modifyNode(implicitAnonymousFunctionExpressionNode.params);
        STNode rightDoubleArrow = modifyNode(implicitAnonymousFunctionExpressionNode.rightDoubleArrow);
        STNode expression = modifyNode(implicitAnonymousFunctionExpressionNode.expression);
        return implicitAnonymousFunctionExpressionNode.modify(
                params,
                rightDoubleArrow,
                expression);
    }

    @Override
    public STStartActionNode transform(
            STStartActionNode startActionNode) {
        STNode annotations = modifyNode(startActionNode.annotations);
        STNode startKeyword = modifyNode(startActionNode.startKeyword);
        STNode expression = modifyNode(startActionNode.expression);
        return startActionNode.modify(
                annotations,
                startKeyword,
                expression);
    }

    @Override
    public STFlushActionNode transform(
            STFlushActionNode flushActionNode) {
        STNode flushKeyword = modifyNode(flushActionNode.flushKeyword);
        STNode peerWorker = modifyNode(flushActionNode.peerWorker);
        return flushActionNode.modify(
                flushKeyword,
                peerWorker);
    }

    @Override
    public STSingletonTypeDescriptorNode transform(
            STSingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        STNode simpleContExprNode = modifyNode(singletonTypeDescriptorNode.simpleContExprNode);
        return singletonTypeDescriptorNode.modify(
                simpleContExprNode);
    }

    @Override
    public STMethodDeclarationNode transform(
            STMethodDeclarationNode methodDeclarationNode) {
        STNode metadata = modifyNode(methodDeclarationNode.metadata);
        STNode qualifierList = modifyNode(methodDeclarationNode.qualifierList);
        STNode functionKeyword = modifyNode(methodDeclarationNode.functionKeyword);
        STNode methodName = modifyNode(methodDeclarationNode.methodName);
        STNode relativeResourcePath = modifyNode(methodDeclarationNode.relativeResourcePath);
        STNode methodSignature = modifyNode(methodDeclarationNode.methodSignature);
        STNode semicolon = modifyNode(methodDeclarationNode.semicolon);
        return methodDeclarationNode.modify(
                methodDeclarationNode.kind,
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon);
    }

    @Override
    public STTypedBindingPatternNode transform(
            STTypedBindingPatternNode typedBindingPatternNode) {
        STNode typeDescriptor = modifyNode(typedBindingPatternNode.typeDescriptor);
        STNode bindingPattern = modifyNode(typedBindingPatternNode.bindingPattern);
        return typedBindingPatternNode.modify(
                typeDescriptor,
                bindingPattern);
    }

    @Override
    public STCaptureBindingPatternNode transform(
            STCaptureBindingPatternNode captureBindingPatternNode) {
        STNode variableName = modifyNode(captureBindingPatternNode.variableName);
        return captureBindingPatternNode.modify(
                variableName);
    }

    @Override
    public STWildcardBindingPatternNode transform(
            STWildcardBindingPatternNode wildcardBindingPatternNode) {
        STNode underscoreToken = modifyNode(wildcardBindingPatternNode.underscoreToken);
        return wildcardBindingPatternNode.modify(
                underscoreToken);
    }

    @Override
    public STListBindingPatternNode transform(
            STListBindingPatternNode listBindingPatternNode) {
        STNode openBracket = modifyNode(listBindingPatternNode.openBracket);
        STNode bindingPatterns = modifyNode(listBindingPatternNode.bindingPatterns);
        STNode closeBracket = modifyNode(listBindingPatternNode.closeBracket);
        return listBindingPatternNode.modify(
                openBracket,
                bindingPatterns,
                closeBracket);
    }

    @Override
    public STMappingBindingPatternNode transform(
            STMappingBindingPatternNode mappingBindingPatternNode) {
        STNode openBrace = modifyNode(mappingBindingPatternNode.openBrace);
        STNode fieldBindingPatterns = modifyNode(mappingBindingPatternNode.fieldBindingPatterns);
        STNode closeBrace = modifyNode(mappingBindingPatternNode.closeBrace);
        return mappingBindingPatternNode.modify(
                openBrace,
                fieldBindingPatterns,
                closeBrace);
    }

    @Override
    public STFieldBindingPatternFullNode transform(
            STFieldBindingPatternFullNode fieldBindingPatternFullNode) {
        STNode variableName = modifyNode(fieldBindingPatternFullNode.variableName);
        STNode colon = modifyNode(fieldBindingPatternFullNode.colon);
        STNode bindingPattern = modifyNode(fieldBindingPatternFullNode.bindingPattern);
        return fieldBindingPatternFullNode.modify(
                variableName,
                colon,
                bindingPattern);
    }

    @Override
    public STFieldBindingPatternVarnameNode transform(
            STFieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        STNode variableName = modifyNode(fieldBindingPatternVarnameNode.variableName);
        return fieldBindingPatternVarnameNode.modify(
                variableName);
    }

    @Override
    public STRestBindingPatternNode transform(
            STRestBindingPatternNode restBindingPatternNode) {
        STNode ellipsisToken = modifyNode(restBindingPatternNode.ellipsisToken);
        STNode variableName = modifyNode(restBindingPatternNode.variableName);
        return restBindingPatternNode.modify(
                ellipsisToken,
                variableName);
    }

    @Override
    public STErrorBindingPatternNode transform(
            STErrorBindingPatternNode errorBindingPatternNode) {
        STNode errorKeyword = modifyNode(errorBindingPatternNode.errorKeyword);
        STNode typeReference = modifyNode(errorBindingPatternNode.typeReference);
        STNode openParenthesis = modifyNode(errorBindingPatternNode.openParenthesis);
        STNode argListBindingPatterns = modifyNode(errorBindingPatternNode.argListBindingPatterns);
        STNode closeParenthesis = modifyNode(errorBindingPatternNode.closeParenthesis);
        return errorBindingPatternNode.modify(
                errorKeyword,
                typeReference,
                openParenthesis,
                argListBindingPatterns,
                closeParenthesis);
    }

    @Override
    public STNamedArgBindingPatternNode transform(
            STNamedArgBindingPatternNode namedArgBindingPatternNode) {
        STNode argName = modifyNode(namedArgBindingPatternNode.argName);
        STNode equalsToken = modifyNode(namedArgBindingPatternNode.equalsToken);
        STNode bindingPattern = modifyNode(namedArgBindingPatternNode.bindingPattern);
        return namedArgBindingPatternNode.modify(
                argName,
                equalsToken,
                bindingPattern);
    }

    @Override
    public STAsyncSendActionNode transform(
            STAsyncSendActionNode asyncSendActionNode) {
        STNode expression = modifyNode(asyncSendActionNode.expression);
        STNode rightArrowToken = modifyNode(asyncSendActionNode.rightArrowToken);
        STNode peerWorker = modifyNode(asyncSendActionNode.peerWorker);
        return asyncSendActionNode.modify(
                expression,
                rightArrowToken,
                peerWorker);
    }

    @Override
    public STSyncSendActionNode transform(
            STSyncSendActionNode syncSendActionNode) {
        STNode expression = modifyNode(syncSendActionNode.expression);
        STNode syncSendToken = modifyNode(syncSendActionNode.syncSendToken);
        STNode peerWorker = modifyNode(syncSendActionNode.peerWorker);
        return syncSendActionNode.modify(
                expression,
                syncSendToken,
                peerWorker);
    }

    @Override
    public STReceiveActionNode transform(
            STReceiveActionNode receiveActionNode) {
        STNode leftArrow = modifyNode(receiveActionNode.leftArrow);
        STNode receiveWorkers = modifyNode(receiveActionNode.receiveWorkers);
        return receiveActionNode.modify(
                leftArrow,
                receiveWorkers);
    }

    @Override
    public STReceiveFieldsNode transform(
            STReceiveFieldsNode receiveFieldsNode) {
        STNode openBrace = modifyNode(receiveFieldsNode.openBrace);
        STNode receiveFields = modifyNode(receiveFieldsNode.receiveFields);
        STNode closeBrace = modifyNode(receiveFieldsNode.closeBrace);
        return receiveFieldsNode.modify(
                openBrace,
                receiveFields,
                closeBrace);
    }

    @Override
    public STRestDescriptorNode transform(
            STRestDescriptorNode restDescriptorNode) {
        STNode typeDescriptor = modifyNode(restDescriptorNode.typeDescriptor);
        STNode ellipsisToken = modifyNode(restDescriptorNode.ellipsisToken);
        return restDescriptorNode.modify(
                typeDescriptor,
                ellipsisToken);
    }

    @Override
    public STDoubleGTTokenNode transform(
            STDoubleGTTokenNode doubleGTTokenNode) {
        STNode openGTToken = modifyNode(doubleGTTokenNode.openGTToken);
        STNode endGTToken = modifyNode(doubleGTTokenNode.endGTToken);
        return doubleGTTokenNode.modify(
                openGTToken,
                endGTToken);
    }

    @Override
    public STTrippleGTTokenNode transform(
            STTrippleGTTokenNode trippleGTTokenNode) {
        STNode openGTToken = modifyNode(trippleGTTokenNode.openGTToken);
        STNode middleGTToken = modifyNode(trippleGTTokenNode.middleGTToken);
        STNode endGTToken = modifyNode(trippleGTTokenNode.endGTToken);
        return trippleGTTokenNode.modify(
                openGTToken,
                middleGTToken,
                endGTToken);
    }

    @Override
    public STWaitActionNode transform(
            STWaitActionNode waitActionNode) {
        STNode waitKeyword = modifyNode(waitActionNode.waitKeyword);
        STNode waitFutureExpr = modifyNode(waitActionNode.waitFutureExpr);
        return waitActionNode.modify(
                waitKeyword,
                waitFutureExpr);
    }

    @Override
    public STWaitFieldsListNode transform(
            STWaitFieldsListNode waitFieldsListNode) {
        STNode openBrace = modifyNode(waitFieldsListNode.openBrace);
        STNode waitFields = modifyNode(waitFieldsListNode.waitFields);
        STNode closeBrace = modifyNode(waitFieldsListNode.closeBrace);
        return waitFieldsListNode.modify(
                openBrace,
                waitFields,
                closeBrace);
    }

    @Override
    public STWaitFieldNode transform(
            STWaitFieldNode waitFieldNode) {
        STNode fieldName = modifyNode(waitFieldNode.fieldName);
        STNode colon = modifyNode(waitFieldNode.colon);
        STNode waitFutureExpr = modifyNode(waitFieldNode.waitFutureExpr);
        return waitFieldNode.modify(
                fieldName,
                colon,
                waitFutureExpr);
    }

    @Override
    public STAnnotAccessExpressionNode transform(
            STAnnotAccessExpressionNode annotAccessExpressionNode) {
        STNode expression = modifyNode(annotAccessExpressionNode.expression);
        STNode annotChainingToken = modifyNode(annotAccessExpressionNode.annotChainingToken);
        STNode annotTagReference = modifyNode(annotAccessExpressionNode.annotTagReference);
        return annotAccessExpressionNode.modify(
                expression,
                annotChainingToken,
                annotTagReference);
    }

    @Override
    public STOptionalFieldAccessExpressionNode transform(
            STOptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        STNode expression = modifyNode(optionalFieldAccessExpressionNode.expression);
        STNode optionalChainingToken = modifyNode(optionalFieldAccessExpressionNode.optionalChainingToken);
        STNode fieldName = modifyNode(optionalFieldAccessExpressionNode.fieldName);
        return optionalFieldAccessExpressionNode.modify(
                expression,
                optionalChainingToken,
                fieldName);
    }

    @Override
    public STConditionalExpressionNode transform(
            STConditionalExpressionNode conditionalExpressionNode) {
        STNode lhsExpression = modifyNode(conditionalExpressionNode.lhsExpression);
        STNode questionMarkToken = modifyNode(conditionalExpressionNode.questionMarkToken);
        STNode middleExpression = modifyNode(conditionalExpressionNode.middleExpression);
        STNode colonToken = modifyNode(conditionalExpressionNode.colonToken);
        STNode endExpression = modifyNode(conditionalExpressionNode.endExpression);
        return conditionalExpressionNode.modify(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression);
    }

    @Override
    public STEnumDeclarationNode transform(
            STEnumDeclarationNode enumDeclarationNode) {
        STNode metadata = modifyNode(enumDeclarationNode.metadata);
        STNode qualifier = modifyNode(enumDeclarationNode.qualifier);
        STNode enumKeywordToken = modifyNode(enumDeclarationNode.enumKeywordToken);
        STNode identifier = modifyNode(enumDeclarationNode.identifier);
        STNode openBraceToken = modifyNode(enumDeclarationNode.openBraceToken);
        STNode enumMemberList = modifyNode(enumDeclarationNode.enumMemberList);
        STNode closeBraceToken = modifyNode(enumDeclarationNode.closeBraceToken);
        STNode semicolonToken = modifyNode(enumDeclarationNode.semicolonToken);
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
    public STEnumMemberNode transform(
            STEnumMemberNode enumMemberNode) {
        STNode metadata = modifyNode(enumMemberNode.metadata);
        STNode identifier = modifyNode(enumMemberNode.identifier);
        STNode equalToken = modifyNode(enumMemberNode.equalToken);
        STNode constExprNode = modifyNode(enumMemberNode.constExprNode);
        return enumMemberNode.modify(
                metadata,
                identifier,
                equalToken,
                constExprNode);
    }

    @Override
    public STArrayTypeDescriptorNode transform(
            STArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        STNode memberTypeDesc = modifyNode(arrayTypeDescriptorNode.memberTypeDesc);
        STNode dimensions = modifyNode(arrayTypeDescriptorNode.dimensions);
        return arrayTypeDescriptorNode.modify(
                memberTypeDesc,
                dimensions);
    }

    @Override
    public STArrayDimensionNode transform(
            STArrayDimensionNode arrayDimensionNode) {
        STNode openBracket = modifyNode(arrayDimensionNode.openBracket);
        STNode arrayLength = modifyNode(arrayDimensionNode.arrayLength);
        STNode closeBracket = modifyNode(arrayDimensionNode.closeBracket);
        return arrayDimensionNode.modify(
                openBracket,
                arrayLength,
                closeBracket);
    }

    @Override
    public STTransactionStatementNode transform(
            STTransactionStatementNode transactionStatementNode) {
        STNode transactionKeyword = modifyNode(transactionStatementNode.transactionKeyword);
        STNode blockStatement = modifyNode(transactionStatementNode.blockStatement);
        STNode onFailClause = modifyNode(transactionStatementNode.onFailClause);
        return transactionStatementNode.modify(
                transactionKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public STRollbackStatementNode transform(
            STRollbackStatementNode rollbackStatementNode) {
        STNode rollbackKeyword = modifyNode(rollbackStatementNode.rollbackKeyword);
        STNode expression = modifyNode(rollbackStatementNode.expression);
        STNode semicolon = modifyNode(rollbackStatementNode.semicolon);
        return rollbackStatementNode.modify(
                rollbackKeyword,
                expression,
                semicolon);
    }

    @Override
    public STRetryStatementNode transform(
            STRetryStatementNode retryStatementNode) {
        STNode retryKeyword = modifyNode(retryStatementNode.retryKeyword);
        STNode typeParameter = modifyNode(retryStatementNode.typeParameter);
        STNode arguments = modifyNode(retryStatementNode.arguments);
        STNode retryBody = modifyNode(retryStatementNode.retryBody);
        STNode onFailClause = modifyNode(retryStatementNode.onFailClause);
        return retryStatementNode.modify(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody,
                onFailClause);
    }

    @Override
    public STCommitActionNode transform(
            STCommitActionNode commitActionNode) {
        STNode commitKeyword = modifyNode(commitActionNode.commitKeyword);
        return commitActionNode.modify(
                commitKeyword);
    }

    @Override
    public STTransactionalExpressionNode transform(
            STTransactionalExpressionNode transactionalExpressionNode) {
        STNode transactionalKeyword = modifyNode(transactionalExpressionNode.transactionalKeyword);
        return transactionalExpressionNode.modify(
                transactionalKeyword);
    }

    @Override
    public STByteArrayLiteralNode transform(
            STByteArrayLiteralNode byteArrayLiteralNode) {
        STNode type = modifyNode(byteArrayLiteralNode.type);
        STNode startBacktick = modifyNode(byteArrayLiteralNode.startBacktick);
        STNode content = modifyNode(byteArrayLiteralNode.content);
        STNode endBacktick = modifyNode(byteArrayLiteralNode.endBacktick);
        return byteArrayLiteralNode.modify(
                type,
                startBacktick,
                content,
                endBacktick);
    }

    @Override
    public STXMLFilterExpressionNode transform(
            STXMLFilterExpressionNode xMLFilterExpressionNode) {
        STNode expression = modifyNode(xMLFilterExpressionNode.expression);
        STNode xmlPatternChain = modifyNode(xMLFilterExpressionNode.xmlPatternChain);
        return xMLFilterExpressionNode.modify(
                expression,
                xmlPatternChain);
    }

    @Override
    public STXMLStepExpressionNode transform(
            STXMLStepExpressionNode xMLStepExpressionNode) {
        STNode expression = modifyNode(xMLStepExpressionNode.expression);
        STNode xmlStepStart = modifyNode(xMLStepExpressionNode.xmlStepStart);
        return xMLStepExpressionNode.modify(
                expression,
                xmlStepStart);
    }

    @Override
    public STXMLNamePatternChainingNode transform(
            STXMLNamePatternChainingNode xMLNamePatternChainingNode) {
        STNode startToken = modifyNode(xMLNamePatternChainingNode.startToken);
        STNode xmlNamePattern = modifyNode(xMLNamePatternChainingNode.xmlNamePattern);
        STNode gtToken = modifyNode(xMLNamePatternChainingNode.gtToken);
        return xMLNamePatternChainingNode.modify(
                startToken,
                xmlNamePattern,
                gtToken);
    }

    @Override
    public STXMLAtomicNamePatternNode transform(
            STXMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        STNode prefix = modifyNode(xMLAtomicNamePatternNode.prefix);
        STNode colon = modifyNode(xMLAtomicNamePatternNode.colon);
        STNode name = modifyNode(xMLAtomicNamePatternNode.name);
        return xMLAtomicNamePatternNode.modify(
                prefix,
                colon,
                name);
    }

    @Override
    public STTypeReferenceTypeDescNode transform(
            STTypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        STNode typeRef = modifyNode(typeReferenceTypeDescNode.typeRef);
        return typeReferenceTypeDescNode.modify(
                typeRef);
    }

    @Override
    public STMatchStatementNode transform(
            STMatchStatementNode matchStatementNode) {
        STNode matchKeyword = modifyNode(matchStatementNode.matchKeyword);
        STNode condition = modifyNode(matchStatementNode.condition);
        STNode openBrace = modifyNode(matchStatementNode.openBrace);
        STNode matchClauses = modifyNode(matchStatementNode.matchClauses);
        STNode closeBrace = modifyNode(matchStatementNode.closeBrace);
        STNode onFailClause = modifyNode(matchStatementNode.onFailClause);
        return matchStatementNode.modify(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                onFailClause);
    }

    @Override
    public STMatchClauseNode transform(
            STMatchClauseNode matchClauseNode) {
        STNode matchPatterns = modifyNode(matchClauseNode.matchPatterns);
        STNode matchGuard = modifyNode(matchClauseNode.matchGuard);
        STNode rightDoubleArrow = modifyNode(matchClauseNode.rightDoubleArrow);
        STNode blockStatement = modifyNode(matchClauseNode.blockStatement);
        return matchClauseNode.modify(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement);
    }

    @Override
    public STMatchGuardNode transform(
            STMatchGuardNode matchGuardNode) {
        STNode ifKeyword = modifyNode(matchGuardNode.ifKeyword);
        STNode expression = modifyNode(matchGuardNode.expression);
        return matchGuardNode.modify(
                ifKeyword,
                expression);
    }

    @Override
    public STDistinctTypeDescriptorNode transform(
            STDistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        STNode distinctKeyword = modifyNode(distinctTypeDescriptorNode.distinctKeyword);
        STNode typeDescriptor = modifyNode(distinctTypeDescriptorNode.typeDescriptor);
        return distinctTypeDescriptorNode.modify(
                distinctKeyword,
                typeDescriptor);
    }

    @Override
    public STListMatchPatternNode transform(
            STListMatchPatternNode listMatchPatternNode) {
        STNode openBracket = modifyNode(listMatchPatternNode.openBracket);
        STNode matchPatterns = modifyNode(listMatchPatternNode.matchPatterns);
        STNode closeBracket = modifyNode(listMatchPatternNode.closeBracket);
        return listMatchPatternNode.modify(
                openBracket,
                matchPatterns,
                closeBracket);
    }

    @Override
    public STRestMatchPatternNode transform(
            STRestMatchPatternNode restMatchPatternNode) {
        STNode ellipsisToken = modifyNode(restMatchPatternNode.ellipsisToken);
        STNode varKeywordToken = modifyNode(restMatchPatternNode.varKeywordToken);
        STNode variableName = modifyNode(restMatchPatternNode.variableName);
        return restMatchPatternNode.modify(
                ellipsisToken,
                varKeywordToken,
                variableName);
    }

    @Override
    public STMappingMatchPatternNode transform(
            STMappingMatchPatternNode mappingMatchPatternNode) {
        STNode openBraceToken = modifyNode(mappingMatchPatternNode.openBraceToken);
        STNode fieldMatchPatterns = modifyNode(mappingMatchPatternNode.fieldMatchPatterns);
        STNode closeBraceToken = modifyNode(mappingMatchPatternNode.closeBraceToken);
        return mappingMatchPatternNode.modify(
                openBraceToken,
                fieldMatchPatterns,
                closeBraceToken);
    }

    @Override
    public STFieldMatchPatternNode transform(
            STFieldMatchPatternNode fieldMatchPatternNode) {
        STNode fieldNameNode = modifyNode(fieldMatchPatternNode.fieldNameNode);
        STNode colonToken = modifyNode(fieldMatchPatternNode.colonToken);
        STNode matchPattern = modifyNode(fieldMatchPatternNode.matchPattern);
        return fieldMatchPatternNode.modify(
                fieldNameNode,
                colonToken,
                matchPattern);
    }

    @Override
    public STErrorMatchPatternNode transform(
            STErrorMatchPatternNode errorMatchPatternNode) {
        STNode errorKeyword = modifyNode(errorMatchPatternNode.errorKeyword);
        STNode typeReference = modifyNode(errorMatchPatternNode.typeReference);
        STNode openParenthesisToken = modifyNode(errorMatchPatternNode.openParenthesisToken);
        STNode argListMatchPatternNode = modifyNode(errorMatchPatternNode.argListMatchPatternNode);
        STNode closeParenthesisToken = modifyNode(errorMatchPatternNode.closeParenthesisToken);
        return errorMatchPatternNode.modify(
                errorKeyword,
                typeReference,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    @Override
    public STNamedArgMatchPatternNode transform(
            STNamedArgMatchPatternNode namedArgMatchPatternNode) {
        STNode identifier = modifyNode(namedArgMatchPatternNode.identifier);
        STNode equalToken = modifyNode(namedArgMatchPatternNode.equalToken);
        STNode matchPattern = modifyNode(namedArgMatchPatternNode.matchPattern);
        return namedArgMatchPatternNode.modify(
                identifier,
                equalToken,
                matchPattern);
    }

    @Override
    public STMarkdownDocumentationNode transform(
            STMarkdownDocumentationNode markdownDocumentationNode) {
        STNode documentationLines = modifyNode(markdownDocumentationNode.documentationLines);
        return markdownDocumentationNode.modify(
                documentationLines);
    }

    @Override
    public STMarkdownDocumentationLineNode transform(
            STMarkdownDocumentationLineNode markdownDocumentationLineNode) {
        STNode hashToken = modifyNode(markdownDocumentationLineNode.hashToken);
        STNode documentElements = modifyNode(markdownDocumentationLineNode.documentElements);
        return markdownDocumentationLineNode.modify(
                markdownDocumentationLineNode.kind,
                hashToken,
                documentElements);
    }

    @Override
    public STMarkdownParameterDocumentationLineNode transform(
            STMarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        STNode hashToken = modifyNode(markdownParameterDocumentationLineNode.hashToken);
        STNode plusToken = modifyNode(markdownParameterDocumentationLineNode.plusToken);
        STNode parameterName = modifyNode(markdownParameterDocumentationLineNode.parameterName);
        STNode minusToken = modifyNode(markdownParameterDocumentationLineNode.minusToken);
        STNode documentElements = modifyNode(markdownParameterDocumentationLineNode.documentElements);
        return markdownParameterDocumentationLineNode.modify(
                markdownParameterDocumentationLineNode.kind,
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements);
    }

    @Override
    public STBallerinaNameReferenceNode transform(
            STBallerinaNameReferenceNode ballerinaNameReferenceNode) {
        STNode referenceType = modifyNode(ballerinaNameReferenceNode.referenceType);
        STNode startBacktick = modifyNode(ballerinaNameReferenceNode.startBacktick);
        STNode nameReference = modifyNode(ballerinaNameReferenceNode.nameReference);
        STNode endBacktick = modifyNode(ballerinaNameReferenceNode.endBacktick);
        return ballerinaNameReferenceNode.modify(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick);
    }

    @Override
    public STInlineCodeReferenceNode transform(
            STInlineCodeReferenceNode inlineCodeReferenceNode) {
        STNode startBacktick = modifyNode(inlineCodeReferenceNode.startBacktick);
        STNode codeReference = modifyNode(inlineCodeReferenceNode.codeReference);
        STNode endBacktick = modifyNode(inlineCodeReferenceNode.endBacktick);
        return inlineCodeReferenceNode.modify(
                startBacktick,
                codeReference,
                endBacktick);
    }

    @Override
    public STMarkdownCodeBlockNode transform(
            STMarkdownCodeBlockNode markdownCodeBlockNode) {
        STNode startLineHashToken = modifyNode(markdownCodeBlockNode.startLineHashToken);
        STNode startBacktick = modifyNode(markdownCodeBlockNode.startBacktick);
        STNode langAttribute = modifyNode(markdownCodeBlockNode.langAttribute);
        STNode codeLines = modifyNode(markdownCodeBlockNode.codeLines);
        STNode endLineHashToken = modifyNode(markdownCodeBlockNode.endLineHashToken);
        STNode endBacktick = modifyNode(markdownCodeBlockNode.endBacktick);
        return markdownCodeBlockNode.modify(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick);
    }

    @Override
    public STMarkdownCodeLineNode transform(
            STMarkdownCodeLineNode markdownCodeLineNode) {
        STNode hashToken = modifyNode(markdownCodeLineNode.hashToken);
        STNode codeDescription = modifyNode(markdownCodeLineNode.codeDescription);
        return markdownCodeLineNode.modify(
                hashToken,
                codeDescription);
    }

    @Override
    public STOrderByClauseNode transform(
            STOrderByClauseNode orderByClauseNode) {
        STNode orderKeyword = modifyNode(orderByClauseNode.orderKeyword);
        STNode byKeyword = modifyNode(orderByClauseNode.byKeyword);
        STNode orderKey = modifyNode(orderByClauseNode.orderKey);
        return orderByClauseNode.modify(
                orderKeyword,
                byKeyword,
                orderKey);
    }

    @Override
    public STOrderKeyNode transform(
            STOrderKeyNode orderKeyNode) {
        STNode expression = modifyNode(orderKeyNode.expression);
        STNode orderDirection = modifyNode(orderKeyNode.orderDirection);
        return orderKeyNode.modify(
                expression,
                orderDirection);
    }

    @Override
    public STOnFailClauseNode transform(
            STOnFailClauseNode onFailClauseNode) {
        STNode onKeyword = modifyNode(onFailClauseNode.onKeyword);
        STNode failKeyword = modifyNode(onFailClauseNode.failKeyword);
        STNode typeDescriptor = modifyNode(onFailClauseNode.typeDescriptor);
        STNode failErrorName = modifyNode(onFailClauseNode.failErrorName);
        STNode blockStatement = modifyNode(onFailClauseNode.blockStatement);
        return onFailClauseNode.modify(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement);
    }

    @Override
    public STDoStatementNode transform(
            STDoStatementNode doStatementNode) {
        STNode doKeyword = modifyNode(doStatementNode.doKeyword);
        STNode blockStatement = modifyNode(doStatementNode.blockStatement);
        STNode onFailClause = modifyNode(doStatementNode.onFailClause);
        return doStatementNode.modify(
                doKeyword,
                blockStatement,
                onFailClause);
    }

    @Override
    public STClassDefinitionNode transform(
            STClassDefinitionNode classDefinitionNode) {
        STNode metadata = modifyNode(classDefinitionNode.metadata);
        STNode visibilityQualifier = modifyNode(classDefinitionNode.visibilityQualifier);
        STNode classTypeQualifiers = modifyNode(classDefinitionNode.classTypeQualifiers);
        STNode classKeyword = modifyNode(classDefinitionNode.classKeyword);
        STNode className = modifyNode(classDefinitionNode.className);
        STNode openBrace = modifyNode(classDefinitionNode.openBrace);
        STNode members = modifyNode(classDefinitionNode.members);
        STNode closeBrace = modifyNode(classDefinitionNode.closeBrace);
        STNode semicolonToken = modifyNode(classDefinitionNode.semicolonToken);
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
    public STResourcePathParameterNode transform(
            STResourcePathParameterNode resourcePathParameterNode) {
        STNode openBracketToken = modifyNode(resourcePathParameterNode.openBracketToken);
        STNode annotations = modifyNode(resourcePathParameterNode.annotations);
        STNode typeDescriptor = modifyNode(resourcePathParameterNode.typeDescriptor);
        STNode ellipsisToken = modifyNode(resourcePathParameterNode.ellipsisToken);
        STNode paramName = modifyNode(resourcePathParameterNode.paramName);
        STNode closeBracketToken = modifyNode(resourcePathParameterNode.closeBracketToken);
        return resourcePathParameterNode.modify(
                resourcePathParameterNode.kind,
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken);
    }

    @Override
    public STRequiredExpressionNode transform(
            STRequiredExpressionNode requiredExpressionNode) {
        STNode questionMarkToken = modifyNode(requiredExpressionNode.questionMarkToken);
        return requiredExpressionNode.modify(
                questionMarkToken);
    }

    @Override
    public STErrorConstructorExpressionNode transform(
            STErrorConstructorExpressionNode errorConstructorExpressionNode) {
        STNode errorKeyword = modifyNode(errorConstructorExpressionNode.errorKeyword);
        STNode typeReference = modifyNode(errorConstructorExpressionNode.typeReference);
        STNode openParenToken = modifyNode(errorConstructorExpressionNode.openParenToken);
        STNode arguments = modifyNode(errorConstructorExpressionNode.arguments);
        STNode closeParenToken = modifyNode(errorConstructorExpressionNode.closeParenToken);
        return errorConstructorExpressionNode.modify(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STParameterizedTypeDescriptorNode transform(
            STParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        STNode keywordToken = modifyNode(parameterizedTypeDescriptorNode.keywordToken);
        STNode typeParamNode = modifyNode(parameterizedTypeDescriptorNode.typeParamNode);
        return parameterizedTypeDescriptorNode.modify(
                parameterizedTypeDescriptorNode.kind,
                keywordToken,
                typeParamNode);
    }

    @Override
    public STSpreadMemberNode transform(
            STSpreadMemberNode spreadMemberNode) {
        STNode ellipsis = modifyNode(spreadMemberNode.ellipsis);
        STNode expression = modifyNode(spreadMemberNode.expression);
        return spreadMemberNode.modify(
                ellipsis,
                expression);
    }

    @Override
    public STClientResourceAccessActionNode transform(
            STClientResourceAccessActionNode clientResourceAccessActionNode) {
        STNode expression = modifyNode(clientResourceAccessActionNode.expression);
        STNode rightArrowToken = modifyNode(clientResourceAccessActionNode.rightArrowToken);
        STNode slashToken = modifyNode(clientResourceAccessActionNode.slashToken);
        STNode resourceAccessPath = modifyNode(clientResourceAccessActionNode.resourceAccessPath);
        STNode dotToken = modifyNode(clientResourceAccessActionNode.dotToken);
        STNode methodName = modifyNode(clientResourceAccessActionNode.methodName);
        STNode arguments = modifyNode(clientResourceAccessActionNode.arguments);
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
    public STComputedResourceAccessSegmentNode transform(
            STComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        STNode openBracketToken = modifyNode(computedResourceAccessSegmentNode.openBracketToken);
        STNode expression = modifyNode(computedResourceAccessSegmentNode.expression);
        STNode closeBracketToken = modifyNode(computedResourceAccessSegmentNode.closeBracketToken);
        return computedResourceAccessSegmentNode.modify(
                openBracketToken,
                expression,
                closeBracketToken);
    }

    @Override
    public STResourceAccessRestSegmentNode transform(
            STResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        STNode openBracketToken = modifyNode(resourceAccessRestSegmentNode.openBracketToken);
        STNode ellipsisToken = modifyNode(resourceAccessRestSegmentNode.ellipsisToken);
        STNode expression = modifyNode(resourceAccessRestSegmentNode.expression);
        STNode closeBracketToken = modifyNode(resourceAccessRestSegmentNode.closeBracketToken);
        return resourceAccessRestSegmentNode.modify(
                openBracketToken,
                ellipsisToken,
                expression,
                closeBracketToken);
    }

    @Override
    public STReSequenceNode transform(
            STReSequenceNode reSequenceNode) {
        STNode reTerm = modifyNode(reSequenceNode.reTerm);
        return reSequenceNode.modify(
                reTerm);
    }

    @Override
    public STReAtomQuantifierNode transform(
            STReAtomQuantifierNode reAtomQuantifierNode) {
        STNode reAtom = modifyNode(reAtomQuantifierNode.reAtom);
        STNode reQuantifier = modifyNode(reAtomQuantifierNode.reQuantifier);
        return reAtomQuantifierNode.modify(
                reAtom,
                reQuantifier);
    }

    @Override
    public STReAtomCharOrEscapeNode transform(
            STReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        STNode reAtomCharOrEscape = modifyNode(reAtomCharOrEscapeNode.reAtomCharOrEscape);
        return reAtomCharOrEscapeNode.modify(
                reAtomCharOrEscape);
    }

    @Override
    public STReQuoteEscapeNode transform(
            STReQuoteEscapeNode reQuoteEscapeNode) {
        STNode slashToken = modifyNode(reQuoteEscapeNode.slashToken);
        STNode reSyntaxChar = modifyNode(reQuoteEscapeNode.reSyntaxChar);
        return reQuoteEscapeNode.modify(
                slashToken,
                reSyntaxChar);
    }

    @Override
    public STReSimpleCharClassEscapeNode transform(
            STReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        STNode slashToken = modifyNode(reSimpleCharClassEscapeNode.slashToken);
        STNode reSimpleCharClassCode = modifyNode(reSimpleCharClassEscapeNode.reSimpleCharClassCode);
        return reSimpleCharClassEscapeNode.modify(
                slashToken,
                reSimpleCharClassCode);
    }

    @Override
    public STReUnicodePropertyEscapeNode transform(
            STReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        STNode slashToken = modifyNode(reUnicodePropertyEscapeNode.slashToken);
        STNode property = modifyNode(reUnicodePropertyEscapeNode.property);
        STNode openBraceToken = modifyNode(reUnicodePropertyEscapeNode.openBraceToken);
        STNode reUnicodeProperty = modifyNode(reUnicodePropertyEscapeNode.reUnicodeProperty);
        STNode closeBraceToken = modifyNode(reUnicodePropertyEscapeNode.closeBraceToken);
        return reUnicodePropertyEscapeNode.modify(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken);
    }

    @Override
    public STReUnicodeScriptNode transform(
            STReUnicodeScriptNode reUnicodeScriptNode) {
        STNode scriptStart = modifyNode(reUnicodeScriptNode.scriptStart);
        STNode reUnicodePropertyValue = modifyNode(reUnicodeScriptNode.reUnicodePropertyValue);
        return reUnicodeScriptNode.modify(
                scriptStart,
                reUnicodePropertyValue);
    }

    @Override
    public STReUnicodeGeneralCategoryNode transform(
            STReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        STNode categoryStart = modifyNode(reUnicodeGeneralCategoryNode.categoryStart);
        STNode reUnicodeGeneralCategoryName = modifyNode(reUnicodeGeneralCategoryNode.reUnicodeGeneralCategoryName);
        return reUnicodeGeneralCategoryNode.modify(
                categoryStart,
                reUnicodeGeneralCategoryName);
    }

    @Override
    public STReCharacterClassNode transform(
            STReCharacterClassNode reCharacterClassNode) {
        STNode openBracket = modifyNode(reCharacterClassNode.openBracket);
        STNode negation = modifyNode(reCharacterClassNode.negation);
        STNode reCharSet = modifyNode(reCharacterClassNode.reCharSet);
        STNode closeBracket = modifyNode(reCharacterClassNode.closeBracket);
        return reCharacterClassNode.modify(
                openBracket,
                negation,
                reCharSet,
                closeBracket);
    }

    @Override
    public STReCharSetRangeWithReCharSetNode transform(
            STReCharSetRangeWithReCharSetNode reCharSetRangeWithReCharSetNode) {
        STNode reCharSetRange = modifyNode(reCharSetRangeWithReCharSetNode.reCharSetRange);
        STNode reCharSet = modifyNode(reCharSetRangeWithReCharSetNode.reCharSet);
        return reCharSetRangeWithReCharSetNode.modify(
                reCharSetRange,
                reCharSet);
    }

    @Override
    public STReCharSetRangeNode transform(
            STReCharSetRangeNode reCharSetRangeNode) {
        STNode lhsReCharSetAtom = modifyNode(reCharSetRangeNode.lhsReCharSetAtom);
        STNode minusToken = modifyNode(reCharSetRangeNode.minusToken);
        STNode rhsReCharSetAtom = modifyNode(reCharSetRangeNode.rhsReCharSetAtom);
        return reCharSetRangeNode.modify(
                lhsReCharSetAtom,
                minusToken,
                rhsReCharSetAtom);
    }

    @Override
    public STReCharSetAtomWithReCharSetNoDashNode transform(
            STReCharSetAtomWithReCharSetNoDashNode reCharSetAtomWithReCharSetNoDashNode) {
        STNode reCharSetAtom = modifyNode(reCharSetAtomWithReCharSetNoDashNode.reCharSetAtom);
        STNode reCharSetNoDash = modifyNode(reCharSetAtomWithReCharSetNoDashNode.reCharSetNoDash);
        return reCharSetAtomWithReCharSetNoDashNode.modify(
                reCharSetAtom,
                reCharSetNoDash);
    }

    @Override
    public STReCharSetRangeNoDashWithReCharSetNode transform(
            STReCharSetRangeNoDashWithReCharSetNode reCharSetRangeNoDashWithReCharSetNode) {
        STNode reCharSetRangeNoDash = modifyNode(reCharSetRangeNoDashWithReCharSetNode.reCharSetRangeNoDash);
        STNode reCharSet = modifyNode(reCharSetRangeNoDashWithReCharSetNode.reCharSet);
        return reCharSetRangeNoDashWithReCharSetNode.modify(
                reCharSetRangeNoDash,
                reCharSet);
    }

    @Override
    public STReCharSetRangeNoDashNode transform(
            STReCharSetRangeNoDashNode reCharSetRangeNoDashNode) {
        STNode reCharSetAtomNoDash = modifyNode(reCharSetRangeNoDashNode.reCharSetAtomNoDash);
        STNode minusToken = modifyNode(reCharSetRangeNoDashNode.minusToken);
        STNode reCharSetAtom = modifyNode(reCharSetRangeNoDashNode.reCharSetAtom);
        return reCharSetRangeNoDashNode.modify(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom);
    }

    @Override
    public STReCharSetAtomNoDashWithReCharSetNoDashNode transform(
            STReCharSetAtomNoDashWithReCharSetNoDashNode reCharSetAtomNoDashWithReCharSetNoDashNode) {
        STNode reCharSetAtomNoDash = modifyNode(reCharSetAtomNoDashWithReCharSetNoDashNode.reCharSetAtomNoDash);
        STNode reCharSetNoDash = modifyNode(reCharSetAtomNoDashWithReCharSetNoDashNode.reCharSetNoDash);
        return reCharSetAtomNoDashWithReCharSetNoDashNode.modify(
                reCharSetAtomNoDash,
                reCharSetNoDash);
    }

    @Override
    public STReCapturingGroupsNode transform(
            STReCapturingGroupsNode reCapturingGroupsNode) {
        STNode openParenthesis = modifyNode(reCapturingGroupsNode.openParenthesis);
        STNode reFlagExpression = modifyNode(reCapturingGroupsNode.reFlagExpression);
        STNode reSequences = modifyNode(reCapturingGroupsNode.reSequences);
        STNode closeParenthesis = modifyNode(reCapturingGroupsNode.closeParenthesis);
        return reCapturingGroupsNode.modify(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis);
    }

    @Override
    public STReFlagExpressionNode transform(
            STReFlagExpressionNode reFlagExpressionNode) {
        STNode questionMark = modifyNode(reFlagExpressionNode.questionMark);
        STNode reFlagsOnOff = modifyNode(reFlagExpressionNode.reFlagsOnOff);
        STNode colon = modifyNode(reFlagExpressionNode.colon);
        return reFlagExpressionNode.modify(
                questionMark,
                reFlagsOnOff,
                colon);
    }

    @Override
    public STReFlagsOnOffNode transform(
            STReFlagsOnOffNode reFlagsOnOffNode) {
        STNode lhsReFlags = modifyNode(reFlagsOnOffNode.lhsReFlags);
        STNode minusToken = modifyNode(reFlagsOnOffNode.minusToken);
        STNode rhsReFlags = modifyNode(reFlagsOnOffNode.rhsReFlags);
        return reFlagsOnOffNode.modify(
                lhsReFlags,
                minusToken,
                rhsReFlags);
    }

    @Override
    public STReFlagsNode transform(
            STReFlagsNode reFlagsNode) {
        STNode reFlag = modifyNode(reFlagsNode.reFlag);
        return reFlagsNode.modify(
                reFlag);
    }

    @Override
    public STReAssertionNode transform(
            STReAssertionNode reAssertionNode) {
        STNode reAssertion = modifyNode(reAssertionNode.reAssertion);
        return reAssertionNode.modify(
                reAssertion);
    }

    @Override
    public STReQuantifierNode transform(
            STReQuantifierNode reQuantifierNode) {
        STNode reBaseQuantifier = modifyNode(reQuantifierNode.reBaseQuantifier);
        STNode nonGreedyChar = modifyNode(reQuantifierNode.nonGreedyChar);
        return reQuantifierNode.modify(
                reBaseQuantifier,
                nonGreedyChar);
    }

    @Override
    public STReBracedQuantifierNode transform(
            STReBracedQuantifierNode reBracedQuantifierNode) {
        STNode openBraceToken = modifyNode(reBracedQuantifierNode.openBraceToken);
        STNode leastTimesMatchedDigit = modifyNode(reBracedQuantifierNode.leastTimesMatchedDigit);
        STNode commaToken = modifyNode(reBracedQuantifierNode.commaToken);
        STNode mostTimesMatchedDigit = modifyNode(reBracedQuantifierNode.mostTimesMatchedDigit);
        STNode closeBraceToken = modifyNode(reBracedQuantifierNode.closeBraceToken);
        return reBracedQuantifierNode.modify(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken);
    }

    // Tokens

    public STToken transform(STToken token) {
        return token;
    }

    public STIdentifierToken transform(STIdentifierToken identifier) {
        return identifier;
    }

    public STLiteralValueToken transform(STLiteralValueToken literalValueToken) {
        return literalValueToken;
    }

    public STDocumentationLineToken transform(STDocumentationLineToken documentationLineToken) {
        return documentationLineToken;
    }

    public STMissingToken transform(STMissingToken missingToken) {
        return missingToken;
    }

    // Misc

    public STNode transform(STNodeList nodeList) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            STNode oldNode = nodeList.get(index);
            STNode newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }
            newSTNodes[index] = newNode;
        }
        
        if (!nodeModified) {
            return nodeList;
        }

        return STNodeFactory.createNodeList(newSTNodes);
    }

    @Override
    protected STNode transformSyntaxNode(STNode node) {
        return node;
    }

    protected <T extends STNode> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }
}

