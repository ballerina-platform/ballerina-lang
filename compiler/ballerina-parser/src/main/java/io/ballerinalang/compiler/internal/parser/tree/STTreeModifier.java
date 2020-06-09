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
package io.ballerinalang.compiler.internal.parser.tree;


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
        STNode visibilityQualifier = modifyNode(functionDefinitionNode.visibilityQualifier);
        STNode functionKeyword = modifyNode(functionDefinitionNode.functionKeyword);
        STNode functionName = modifyNode(functionDefinitionNode.functionName);
        STNode functionSignature = modifyNode(functionDefinitionNode.functionSignature);
        STNode functionBody = modifyNode(functionDefinitionNode.functionBody);
        return functionDefinitionNode.modify(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                functionSignature,
                functionBody);
    }

    @Override
    public STImportDeclarationNode transform(
            STImportDeclarationNode importDeclarationNode) {
        STNode importKeyword = modifyNode(importDeclarationNode.importKeyword);
        STNode orgName = modifyNode(importDeclarationNode.orgName);
        STNode moduleName = modifyNode(importDeclarationNode.moduleName);
        STNode version = modifyNode(importDeclarationNode.version);
        STNode prefix = modifyNode(importDeclarationNode.prefix);
        STNode semicolon = modifyNode(importDeclarationNode.semicolon);
        return importDeclarationNode.modify(
                importKeyword,
                orgName,
                moduleName,
                version,
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
        STNode serviceKeyword = modifyNode(serviceDeclarationNode.serviceKeyword);
        STNode serviceName = modifyNode(serviceDeclarationNode.serviceName);
        STNode onKeyword = modifyNode(serviceDeclarationNode.onKeyword);
        STNode expressions = modifyNode(serviceDeclarationNode.expressions);
        STNode serviceBody = modifyNode(serviceDeclarationNode.serviceBody);
        return serviceDeclarationNode.modify(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
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
        return whileStatementNode.modify(
                whileKeyword,
                condition,
                whileBody);
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
        return lockStatementNode.modify(
                lockKeyword,
                blockStatement);
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
        return forEachStatementNode.modify(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement);
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
        STNode leadingComma = modifyNode(defaultableParameterNode.leadingComma);
        STNode annotations = modifyNode(defaultableParameterNode.annotations);
        STNode visibilityQualifier = modifyNode(defaultableParameterNode.visibilityQualifier);
        STNode typeName = modifyNode(defaultableParameterNode.typeName);
        STNode paramName = modifyNode(defaultableParameterNode.paramName);
        STNode equalsToken = modifyNode(defaultableParameterNode.equalsToken);
        STNode expression = modifyNode(defaultableParameterNode.expression);
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
    public STRequiredParameterNode transform(
            STRequiredParameterNode requiredParameterNode) {
        STNode leadingComma = modifyNode(requiredParameterNode.leadingComma);
        STNode annotations = modifyNode(requiredParameterNode.annotations);
        STNode visibilityQualifier = modifyNode(requiredParameterNode.visibilityQualifier);
        STNode typeName = modifyNode(requiredParameterNode.typeName);
        STNode paramName = modifyNode(requiredParameterNode.paramName);
        return requiredParameterNode.modify(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName);
    }

    @Override
    public STRestParameterNode transform(
            STRestParameterNode restParameterNode) {
        STNode leadingComma = modifyNode(restParameterNode.leadingComma);
        STNode annotations = modifyNode(restParameterNode.annotations);
        STNode typeName = modifyNode(restParameterNode.typeName);
        STNode ellipsisToken = modifyNode(restParameterNode.ellipsisToken);
        STNode paramName = modifyNode(restParameterNode.paramName);
        return restParameterNode.modify(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    @Override
    public STExpressionListItemNode transform(
            STExpressionListItemNode expressionListItemNode) {
        STNode leadingComma = modifyNode(expressionListItemNode.leadingComma);
        STNode expression = modifyNode(expressionListItemNode.expression);
        return expressionListItemNode.modify(
                leadingComma,
                expression);
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
    public STImportSubVersionNode transform(
            STImportSubVersionNode importSubVersionNode) {
        STNode leadingDot = modifyNode(importSubVersionNode.leadingDot);
        STNode versionNumber = modifyNode(importSubVersionNode.versionNumber);
        return importSubVersionNode.modify(
                leadingDot,
                versionNumber);
    }

    @Override
    public STImportVersionNode transform(
            STImportVersionNode importVersionNode) {
        STNode versionKeyword = modifyNode(importVersionNode.versionKeyword);
        STNode versionNumber = modifyNode(importVersionNode.versionNumber);
        return importVersionNode.modify(
                versionKeyword,
                versionNumber);
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
        STNode leadingComma = modifyNode(namedArgumentNode.leadingComma);
        STNode argumentName = modifyNode(namedArgumentNode.argumentName);
        STNode equalsToken = modifyNode(namedArgumentNode.equalsToken);
        STNode expression = modifyNode(namedArgumentNode.expression);
        return namedArgumentNode.modify(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    @Override
    public STPositionalArgumentNode transform(
            STPositionalArgumentNode positionalArgumentNode) {
        STNode leadingComma = modifyNode(positionalArgumentNode.leadingComma);
        STNode expression = modifyNode(positionalArgumentNode.expression);
        return positionalArgumentNode.modify(
                leadingComma,
                expression);
    }

    @Override
    public STRestArgumentNode transform(
            STRestArgumentNode restArgumentNode) {
        STNode leadingComma = modifyNode(restArgumentNode.leadingComma);
        STNode ellipsis = modifyNode(restArgumentNode.ellipsis);
        STNode expression = modifyNode(restArgumentNode.expression);
        return restArgumentNode.modify(
                leadingComma,
                ellipsis,
                expression);
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
    public STRecordTypeDescriptorNode transform(
            STRecordTypeDescriptorNode recordTypeDescriptorNode) {
        STNode objectKeyword = modifyNode(recordTypeDescriptorNode.objectKeyword);
        STNode bodyStartDelimiter = modifyNode(recordTypeDescriptorNode.bodyStartDelimiter);
        STNode fields = modifyNode(recordTypeDescriptorNode.fields);
        STNode bodyEndDelimiter = modifyNode(recordTypeDescriptorNode.bodyEndDelimiter);
        return recordTypeDescriptorNode.modify(
                objectKeyword,
                bodyStartDelimiter,
                fields,
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
        STNode readonlyKeyword = modifyNode(objectFieldNode.readonlyKeyword);
        STNode typeName = modifyNode(objectFieldNode.typeName);
        STNode fieldName = modifyNode(objectFieldNode.fieldName);
        STNode equalsToken = modifyNode(objectFieldNode.equalsToken);
        STNode expression = modifyNode(objectFieldNode.expression);
        STNode semicolonToken = modifyNode(objectFieldNode.semicolonToken);
        return objectFieldNode.modify(
                metadata,
                visibilityQualifier,
                readonlyKeyword,
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
    public STServiceBodyNode transform(
            STServiceBodyNode serviceBodyNode) {
        STNode openBraceToken = modifyNode(serviceBodyNode.openBraceToken);
        STNode resources = modifyNode(serviceBodyNode.resources);
        STNode closeBraceToken = modifyNode(serviceBodyNode.closeBraceToken);
        return serviceBodyNode.modify(
                openBraceToken,
                resources,
                closeBraceToken);
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
        STNode finalKeyword = modifyNode(moduleVariableDeclarationNode.finalKeyword);
        STNode typedBindingPattern = modifyNode(moduleVariableDeclarationNode.typedBindingPattern);
        STNode equalsToken = modifyNode(moduleVariableDeclarationNode.equalsToken);
        STNode initializer = modifyNode(moduleVariableDeclarationNode.initializer);
        STNode semicolonToken = modifyNode(moduleVariableDeclarationNode.semicolonToken);
        return moduleVariableDeclarationNode.modify(
                metadata,
                finalKeyword,
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
    public STParameterizedTypeDescriptorNode transform(
            STParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        STNode parameterizedType = modifyNode(parameterizedTypeDescriptorNode.parameterizedType);
        STNode ltToken = modifyNode(parameterizedTypeDescriptorNode.ltToken);
        STNode typeNode = modifyNode(parameterizedTypeDescriptorNode.typeNode);
        STNode gtToken = modifyNode(parameterizedTypeDescriptorNode.gtToken);
        return parameterizedTypeDescriptorNode.modify(
                parameterizedType,
                ltToken,
                typeNode,
                gtToken);
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
        STNode firstIdent = modifyNode(annotationAttachPointNode.firstIdent);
        STNode secondIdent = modifyNode(annotationAttachPointNode.secondIdent);
        return annotationAttachPointNode.modify(
                sourceKeyword,
                firstIdent,
                secondIdent);
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
        return functionBodyBlockNode.modify(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken);
    }

    @Override
    public STNamedWorkerDeclarationNode transform(
            STNamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        STNode annotations = modifyNode(namedWorkerDeclarationNode.annotations);
        STNode workerKeyword = modifyNode(namedWorkerDeclarationNode.workerKeyword);
        STNode workerName = modifyNode(namedWorkerDeclarationNode.workerName);
        STNode returnTypeDesc = modifyNode(namedWorkerDeclarationNode.returnTypeDesc);
        STNode workerBody = modifyNode(namedWorkerDeclarationNode.workerBody);
        return namedWorkerDeclarationNode.modify(
                annotations,
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
    public STDocumentationStringNode transform(
            STDocumentationStringNode documentationStringNode) {
        STNode documentationLines = modifyNode(documentationStringNode.documentationLines);
        return documentationStringNode.modify(
                documentationLines);
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
        STNode mappingConstructors = modifyNode(tableConstructorExpressionNode.mappingConstructors);
        STNode closeBracket = modifyNode(tableConstructorExpressionNode.closeBracket);
        return tableConstructorExpressionNode.modify(
                tableKeyword,
                keySpecifier,
                openBracket,
                mappingConstructors,
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
    public STErrorTypeDescriptorNode transform(
            STErrorTypeDescriptorNode errorTypeDescriptorNode) {
        STNode errorKeywordToken = modifyNode(errorTypeDescriptorNode.errorKeywordToken);
        STNode errorTypeParamsNode = modifyNode(errorTypeDescriptorNode.errorTypeParamsNode);
        return errorTypeDescriptorNode.modify(
                errorKeywordToken,
                errorTypeParamsNode);
    }

    @Override
    public STErrorTypeParamsNode transform(
            STErrorTypeParamsNode errorTypeParamsNode) {
        STNode ltToken = modifyNode(errorTypeParamsNode.ltToken);
        STNode parameter = modifyNode(errorTypeParamsNode.parameter);
        STNode gtToken = modifyNode(errorTypeParamsNode.gtToken);
        return errorTypeParamsNode.modify(
                ltToken,
                parameter,
                gtToken);
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
        STNode functionKeyword = modifyNode(functionTypeDescriptorNode.functionKeyword);
        STNode functionSignature = modifyNode(functionTypeDescriptorNode.functionSignature);
        return functionTypeDescriptorNode.modify(
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
        STNode functionKeyword = modifyNode(explicitAnonymousFunctionExpressionNode.functionKeyword);
        STNode functionSignature = modifyNode(explicitAnonymousFunctionExpressionNode.functionSignature);
        STNode functionBody = modifyNode(explicitAnonymousFunctionExpressionNode.functionBody);
        return explicitAnonymousFunctionExpressionNode.modify(
                annotations,
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
        STNode tableKeyword = modifyNode(queryConstructTypeNode.tableKeyword);
        STNode keySpecifier = modifyNode(queryConstructTypeNode.keySpecifier);
        return queryConstructTypeNode.modify(
                tableKeyword,
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
        return queryExpressionNode.modify(
                queryConstructType,
                queryPipeline,
                selectClause);
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
        STNode visibilityQualifier = modifyNode(methodDeclarationNode.visibilityQualifier);
        STNode functionKeyword = modifyNode(methodDeclarationNode.functionKeyword);
        STNode methodName = modifyNode(methodDeclarationNode.methodName);
        STNode methodSignature = modifyNode(methodDeclarationNode.methodSignature);
        STNode semicolon = modifyNode(methodDeclarationNode.semicolon);
        return methodDeclarationNode.modify(
                metadata,
                visibilityQualifier,
                functionKeyword,
                methodName,
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
        STNode restBindingPattern = modifyNode(listBindingPatternNode.restBindingPattern);
        STNode closeBracket = modifyNode(listBindingPatternNode.closeBracket);
        return listBindingPatternNode.modify(
                openBracket,
                bindingPatterns,
                restBindingPattern,
                closeBracket);
    }

    @Override
    public STMappingBindingPatternNode transform(
            STMappingBindingPatternNode mappingBindingPatternNode) {
        STNode openBrace = modifyNode(mappingBindingPatternNode.openBrace);
        STNode fieldBindingPatterns = modifyNode(mappingBindingPatternNode.fieldBindingPatterns);
        STNode restBindingPattern = modifyNode(mappingBindingPatternNode.restBindingPattern);
        STNode closeBrace = modifyNode(mappingBindingPatternNode.closeBrace);
        return mappingBindingPatternNode.modify(
                openBrace,
                fieldBindingPatterns,
                restBindingPattern,
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
        return enumDeclarationNode.modify(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken);
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
        STNode openBracket = modifyNode(arrayTypeDescriptorNode.openBracket);
        STNode arrayLength = modifyNode(arrayTypeDescriptorNode.arrayLength);
        STNode closeBracket = modifyNode(arrayTypeDescriptorNode.closeBracket);
        return arrayTypeDescriptorNode.modify(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket);
    }

    @Override
    public STTransactionStatementNode transform(
            STTransactionStatementNode transactionStatementNode) {
        STNode transactionKeyword = modifyNode(transactionStatementNode.transactionKeyword);
        STNode blockStatement = modifyNode(transactionStatementNode.blockStatement);
        return transactionStatementNode.modify(
                transactionKeyword,
                blockStatement);
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
        return retryStatementNode.modify(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody);
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
    public STServiceConstructorExpressionNode transform(
            STServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        STNode annotations = modifyNode(serviceConstructorExpressionNode.annotations);
        STNode serviceKeyword = modifyNode(serviceConstructorExpressionNode.serviceKeyword);
        STNode serviceBody = modifyNode(serviceConstructorExpressionNode.serviceBody);
        return serviceConstructorExpressionNode.modify(
                annotations,
                serviceKeyword,
                serviceBody);
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
        return transformSyntaxNode(nodeList);
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

