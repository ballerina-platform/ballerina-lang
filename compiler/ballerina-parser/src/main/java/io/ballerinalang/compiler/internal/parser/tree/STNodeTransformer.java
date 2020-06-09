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
 * The {@code NodeTransformer} transform each node in the syntax tree to
 * another object of type T.
 * <p>
 * This is a generated class.
 *
 * @param <T> the type of class that is returned by visit methods
 * @since 2.0.0
 */
public abstract class STNodeTransformer<T> {

    public T transform(STModulePartNode modulePartNode) {
        return transformSyntaxNode(modulePartNode);
    }

    public T transform(STFunctionDefinitionNode functionDefinitionNode) {
        return transformSyntaxNode(functionDefinitionNode);
    }

    public T transform(STImportDeclarationNode importDeclarationNode) {
        return transformSyntaxNode(importDeclarationNode);
    }

    public T transform(STListenerDeclarationNode listenerDeclarationNode) {
        return transformSyntaxNode(listenerDeclarationNode);
    }

    public T transform(STTypeDefinitionNode typeDefinitionNode) {
        return transformSyntaxNode(typeDefinitionNode);
    }

    public T transform(STServiceDeclarationNode serviceDeclarationNode) {
        return transformSyntaxNode(serviceDeclarationNode);
    }

    public T transform(STAssignmentStatementNode assignmentStatementNode) {
        return transformSyntaxNode(assignmentStatementNode);
    }

    public T transform(STCompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        return transformSyntaxNode(compoundAssignmentStatementNode);
    }

    public T transform(STVariableDeclarationNode variableDeclarationNode) {
        return transformSyntaxNode(variableDeclarationNode);
    }

    public T transform(STBlockStatementNode blockStatementNode) {
        return transformSyntaxNode(blockStatementNode);
    }

    public T transform(STBreakStatementNode breakStatementNode) {
        return transformSyntaxNode(breakStatementNode);
    }

    public T transform(STExpressionStatementNode expressionStatementNode) {
        return transformSyntaxNode(expressionStatementNode);
    }

    public T transform(STContinueStatementNode continueStatementNode) {
        return transformSyntaxNode(continueStatementNode);
    }

    public T transform(STExternalFunctionBodyNode externalFunctionBodyNode) {
        return transformSyntaxNode(externalFunctionBodyNode);
    }

    public T transform(STIfElseStatementNode ifElseStatementNode) {
        return transformSyntaxNode(ifElseStatementNode);
    }

    public T transform(STElseBlockNode elseBlockNode) {
        return transformSyntaxNode(elseBlockNode);
    }

    public T transform(STWhileStatementNode whileStatementNode) {
        return transformSyntaxNode(whileStatementNode);
    }

    public T transform(STPanicStatementNode panicStatementNode) {
        return transformSyntaxNode(panicStatementNode);
    }

    public T transform(STReturnStatementNode returnStatementNode) {
        return transformSyntaxNode(returnStatementNode);
    }

    public T transform(STLocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        return transformSyntaxNode(localTypeDefinitionStatementNode);
    }

    public T transform(STLockStatementNode lockStatementNode) {
        return transformSyntaxNode(lockStatementNode);
    }

    public T transform(STForkStatementNode forkStatementNode) {
        return transformSyntaxNode(forkStatementNode);
    }

    public T transform(STForEachStatementNode forEachStatementNode) {
        return transformSyntaxNode(forEachStatementNode);
    }

    public T transform(STBinaryExpressionNode binaryExpressionNode) {
        return transformSyntaxNode(binaryExpressionNode);
    }

    public T transform(STBracedExpressionNode bracedExpressionNode) {
        return transformSyntaxNode(bracedExpressionNode);
    }

    public T transform(STCheckExpressionNode checkExpressionNode) {
        return transformSyntaxNode(checkExpressionNode);
    }

    public T transform(STFieldAccessExpressionNode fieldAccessExpressionNode) {
        return transformSyntaxNode(fieldAccessExpressionNode);
    }

    public T transform(STFunctionCallExpressionNode functionCallExpressionNode) {
        return transformSyntaxNode(functionCallExpressionNode);
    }

    public T transform(STMethodCallExpressionNode methodCallExpressionNode) {
        return transformSyntaxNode(methodCallExpressionNode);
    }

    public T transform(STMappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return transformSyntaxNode(mappingConstructorExpressionNode);
    }

    public T transform(STIndexedExpressionNode indexedExpressionNode) {
        return transformSyntaxNode(indexedExpressionNode);
    }

    public T transform(STTypeofExpressionNode typeofExpressionNode) {
        return transformSyntaxNode(typeofExpressionNode);
    }

    public T transform(STUnaryExpressionNode unaryExpressionNode) {
        return transformSyntaxNode(unaryExpressionNode);
    }

    public T transform(STComputedNameFieldNode computedNameFieldNode) {
        return transformSyntaxNode(computedNameFieldNode);
    }

    public T transform(STConstantDeclarationNode constantDeclarationNode) {
        return transformSyntaxNode(constantDeclarationNode);
    }

    public T transform(STDefaultableParameterNode defaultableParameterNode) {
        return transformSyntaxNode(defaultableParameterNode);
    }

    public T transform(STRequiredParameterNode requiredParameterNode) {
        return transformSyntaxNode(requiredParameterNode);
    }

    public T transform(STRestParameterNode restParameterNode) {
        return transformSyntaxNode(restParameterNode);
    }

    public T transform(STExpressionListItemNode expressionListItemNode) {
        return transformSyntaxNode(expressionListItemNode);
    }

    public T transform(STImportOrgNameNode importOrgNameNode) {
        return transformSyntaxNode(importOrgNameNode);
    }

    public T transform(STImportPrefixNode importPrefixNode) {
        return transformSyntaxNode(importPrefixNode);
    }

    public T transform(STImportSubVersionNode importSubVersionNode) {
        return transformSyntaxNode(importSubVersionNode);
    }

    public T transform(STImportVersionNode importVersionNode) {
        return transformSyntaxNode(importVersionNode);
    }

    public T transform(STSpecificFieldNode specificFieldNode) {
        return transformSyntaxNode(specificFieldNode);
    }

    public T transform(STSpreadFieldNode spreadFieldNode) {
        return transformSyntaxNode(spreadFieldNode);
    }

    public T transform(STNamedArgumentNode namedArgumentNode) {
        return transformSyntaxNode(namedArgumentNode);
    }

    public T transform(STPositionalArgumentNode positionalArgumentNode) {
        return transformSyntaxNode(positionalArgumentNode);
    }

    public T transform(STRestArgumentNode restArgumentNode) {
        return transformSyntaxNode(restArgumentNode);
    }

    public T transform(STObjectTypeDescriptorNode objectTypeDescriptorNode) {
        return transformSyntaxNode(objectTypeDescriptorNode);
    }

    public T transform(STRecordTypeDescriptorNode recordTypeDescriptorNode) {
        return transformSyntaxNode(recordTypeDescriptorNode);
    }

    public T transform(STReturnTypeDescriptorNode returnTypeDescriptorNode) {
        return transformSyntaxNode(returnTypeDescriptorNode);
    }

    public T transform(STNilTypeDescriptorNode nilTypeDescriptorNode) {
        return transformSyntaxNode(nilTypeDescriptorNode);
    }

    public T transform(STOptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        return transformSyntaxNode(optionalTypeDescriptorNode);
    }

    public T transform(STObjectFieldNode objectFieldNode) {
        return transformSyntaxNode(objectFieldNode);
    }

    public T transform(STRecordFieldNode recordFieldNode) {
        return transformSyntaxNode(recordFieldNode);
    }

    public T transform(STRecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return transformSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public T transform(STRecordRestDescriptorNode recordRestDescriptorNode) {
        return transformSyntaxNode(recordRestDescriptorNode);
    }

    public T transform(STTypeReferenceNode typeReferenceNode) {
        return transformSyntaxNode(typeReferenceNode);
    }

    public T transform(STServiceBodyNode serviceBodyNode) {
        return transformSyntaxNode(serviceBodyNode);
    }

    public T transform(STAnnotationNode annotationNode) {
        return transformSyntaxNode(annotationNode);
    }

    public T transform(STMetadataNode metadataNode) {
        return transformSyntaxNode(metadataNode);
    }

    public T transform(STModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return transformSyntaxNode(moduleVariableDeclarationNode);
    }

    public T transform(STTypeTestExpressionNode typeTestExpressionNode) {
        return transformSyntaxNode(typeTestExpressionNode);
    }

    public T transform(STRemoteMethodCallActionNode remoteMethodCallActionNode) {
        return transformSyntaxNode(remoteMethodCallActionNode);
    }

    public T transform(STParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        return transformSyntaxNode(parameterizedTypeDescriptorNode);
    }

    public T transform(STNilLiteralNode nilLiteralNode) {
        return transformSyntaxNode(nilLiteralNode);
    }

    public T transform(STAnnotationDeclarationNode annotationDeclarationNode) {
        return transformSyntaxNode(annotationDeclarationNode);
    }

    public T transform(STAnnotationAttachPointNode annotationAttachPointNode) {
        return transformSyntaxNode(annotationAttachPointNode);
    }

    public T transform(STXMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        return transformSyntaxNode(xMLNamespaceDeclarationNode);
    }

    public T transform(STModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        return transformSyntaxNode(moduleXMLNamespaceDeclarationNode);
    }

    public T transform(STFunctionBodyBlockNode functionBodyBlockNode) {
        return transformSyntaxNode(functionBodyBlockNode);
    }

    public T transform(STNamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return transformSyntaxNode(namedWorkerDeclarationNode);
    }

    public T transform(STNamedWorkerDeclarator namedWorkerDeclarator) {
        return transformSyntaxNode(namedWorkerDeclarator);
    }

    public T transform(STDocumentationStringNode documentationStringNode) {
        return transformSyntaxNode(documentationStringNode);
    }

    public T transform(STBasicLiteralNode basicLiteralNode) {
        return transformSyntaxNode(basicLiteralNode);
    }

    public T transform(STSimpleNameReferenceNode simpleNameReferenceNode) {
        return transformSyntaxNode(simpleNameReferenceNode);
    }

    public T transform(STQualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return transformSyntaxNode(qualifiedNameReferenceNode);
    }

    public T transform(STBuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        return transformSyntaxNode(builtinSimpleNameReferenceNode);
    }

    public T transform(STTrapExpressionNode trapExpressionNode) {
        return transformSyntaxNode(trapExpressionNode);
    }

    public T transform(STListConstructorExpressionNode listConstructorExpressionNode) {
        return transformSyntaxNode(listConstructorExpressionNode);
    }

    public T transform(STTypeCastExpressionNode typeCastExpressionNode) {
        return transformSyntaxNode(typeCastExpressionNode);
    }

    public T transform(STTypeCastParamNode typeCastParamNode) {
        return transformSyntaxNode(typeCastParamNode);
    }

    public T transform(STUnionTypeDescriptorNode unionTypeDescriptorNode) {
        return transformSyntaxNode(unionTypeDescriptorNode);
    }

    public T transform(STTableConstructorExpressionNode tableConstructorExpressionNode) {
        return transformSyntaxNode(tableConstructorExpressionNode);
    }

    public T transform(STKeySpecifierNode keySpecifierNode) {
        return transformSyntaxNode(keySpecifierNode);
    }

    public T transform(STErrorTypeDescriptorNode errorTypeDescriptorNode) {
        return transformSyntaxNode(errorTypeDescriptorNode);
    }

    public T transform(STErrorTypeParamsNode errorTypeParamsNode) {
        return transformSyntaxNode(errorTypeParamsNode);
    }

    public T transform(STStreamTypeDescriptorNode streamTypeDescriptorNode) {
        return transformSyntaxNode(streamTypeDescriptorNode);
    }

    public T transform(STStreamTypeParamsNode streamTypeParamsNode) {
        return transformSyntaxNode(streamTypeParamsNode);
    }

    public T transform(STLetExpressionNode letExpressionNode) {
        return transformSyntaxNode(letExpressionNode);
    }

    public T transform(STLetVariableDeclarationNode letVariableDeclarationNode) {
        return transformSyntaxNode(letVariableDeclarationNode);
    }

    public T transform(STTemplateExpressionNode templateExpressionNode) {
        return transformSyntaxNode(templateExpressionNode);
    }

    public T transform(STXMLElementNode xMLElementNode) {
        return transformSyntaxNode(xMLElementNode);
    }

    public T transform(STXMLStartTagNode xMLStartTagNode) {
        return transformSyntaxNode(xMLStartTagNode);
    }

    public T transform(STXMLEndTagNode xMLEndTagNode) {
        return transformSyntaxNode(xMLEndTagNode);
    }

    public T transform(STXMLSimpleNameNode xMLSimpleNameNode) {
        return transformSyntaxNode(xMLSimpleNameNode);
    }

    public T transform(STXMLQualifiedNameNode xMLQualifiedNameNode) {
        return transformSyntaxNode(xMLQualifiedNameNode);
    }

    public T transform(STXMLEmptyElementNode xMLEmptyElementNode) {
        return transformSyntaxNode(xMLEmptyElementNode);
    }

    public T transform(STInterpolationNode interpolationNode) {
        return transformSyntaxNode(interpolationNode);
    }

    public T transform(STXMLTextNode xMLTextNode) {
        return transformSyntaxNode(xMLTextNode);
    }

    public T transform(STXMLAttributeNode xMLAttributeNode) {
        return transformSyntaxNode(xMLAttributeNode);
    }

    public T transform(STXMLAttributeValue xMLAttributeValue) {
        return transformSyntaxNode(xMLAttributeValue);
    }

    public T transform(STXMLComment xMLComment) {
        return transformSyntaxNode(xMLComment);
    }

    public T transform(STXMLProcessingInstruction xMLProcessingInstruction) {
        return transformSyntaxNode(xMLProcessingInstruction);
    }

    public T transform(STTableTypeDescriptorNode tableTypeDescriptorNode) {
        return transformSyntaxNode(tableTypeDescriptorNode);
    }

    public T transform(STTypeParameterNode typeParameterNode) {
        return transformSyntaxNode(typeParameterNode);
    }

    public T transform(STKeyTypeConstraintNode keyTypeConstraintNode) {
        return transformSyntaxNode(keyTypeConstraintNode);
    }

    public T transform(STFunctionTypeDescriptorNode functionTypeDescriptorNode) {
        return transformSyntaxNode(functionTypeDescriptorNode);
    }

    public T transform(STFunctionSignatureNode functionSignatureNode) {
        return transformSyntaxNode(functionSignatureNode);
    }

    public T transform(STExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        return transformSyntaxNode(explicitAnonymousFunctionExpressionNode);
    }

    public T transform(STExpressionFunctionBodyNode expressionFunctionBodyNode) {
        return transformSyntaxNode(expressionFunctionBodyNode);
    }

    public T transform(STTupleTypeDescriptorNode tupleTypeDescriptorNode) {
        return transformSyntaxNode(tupleTypeDescriptorNode);
    }

    public T transform(STParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        return transformSyntaxNode(parenthesisedTypeDescriptorNode);
    }

    public T transform(STExplicitNewExpressionNode explicitNewExpressionNode) {
        return transformSyntaxNode(explicitNewExpressionNode);
    }

    public T transform(STImplicitNewExpressionNode implicitNewExpressionNode) {
        return transformSyntaxNode(implicitNewExpressionNode);
    }

    public T transform(STParenthesizedArgList parenthesizedArgList) {
        return transformSyntaxNode(parenthesizedArgList);
    }

    public T transform(STQueryConstructTypeNode queryConstructTypeNode) {
        return transformSyntaxNode(queryConstructTypeNode);
    }

    public T transform(STFromClauseNode fromClauseNode) {
        return transformSyntaxNode(fromClauseNode);
    }

    public T transform(STWhereClauseNode whereClauseNode) {
        return transformSyntaxNode(whereClauseNode);
    }

    public T transform(STLetClauseNode letClauseNode) {
        return transformSyntaxNode(letClauseNode);
    }

    public T transform(STQueryPipelineNode queryPipelineNode) {
        return transformSyntaxNode(queryPipelineNode);
    }

    public T transform(STSelectClauseNode selectClauseNode) {
        return transformSyntaxNode(selectClauseNode);
    }

    public T transform(STQueryExpressionNode queryExpressionNode) {
        return transformSyntaxNode(queryExpressionNode);
    }

    public T transform(STIntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        return transformSyntaxNode(intersectionTypeDescriptorNode);
    }

    public T transform(STImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        return transformSyntaxNode(implicitAnonymousFunctionParameters);
    }

    public T transform(STImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        return transformSyntaxNode(implicitAnonymousFunctionExpressionNode);
    }

    public T transform(STStartActionNode startActionNode) {
        return transformSyntaxNode(startActionNode);
    }

    public T transform(STFlushActionNode flushActionNode) {
        return transformSyntaxNode(flushActionNode);
    }

    public T transform(STSingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        return transformSyntaxNode(singletonTypeDescriptorNode);
    }

    public T transform(STMethodDeclarationNode methodDeclarationNode) {
        return transformSyntaxNode(methodDeclarationNode);
    }

    public T transform(STTypedBindingPatternNode typedBindingPatternNode) {
        return transformSyntaxNode(typedBindingPatternNode);
    }

    public T transform(STCaptureBindingPatternNode captureBindingPatternNode) {
        return transformSyntaxNode(captureBindingPatternNode);
    }

    public T transform(STWildcardBindingPatternNode wildcardBindingPatternNode) {
        return transformSyntaxNode(wildcardBindingPatternNode);
    }

    public T transform(STListBindingPatternNode listBindingPatternNode) {
        return transformSyntaxNode(listBindingPatternNode);
    }

    public T transform(STMappingBindingPatternNode mappingBindingPatternNode) {
        return transformSyntaxNode(mappingBindingPatternNode);
    }

    public T transform(STFieldBindingPatternFullNode fieldBindingPatternFullNode) {
        return transformSyntaxNode(fieldBindingPatternFullNode);
    }

    public T transform(STFieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        return transformSyntaxNode(fieldBindingPatternVarnameNode);
    }

    public T transform(STRestBindingPatternNode restBindingPatternNode) {
        return transformSyntaxNode(restBindingPatternNode);
    }

    public T transform(STAsyncSendActionNode asyncSendActionNode) {
        return transformSyntaxNode(asyncSendActionNode);
    }

    public T transform(STSyncSendActionNode syncSendActionNode) {
        return transformSyntaxNode(syncSendActionNode);
    }

    public T transform(STReceiveActionNode receiveActionNode) {
        return transformSyntaxNode(receiveActionNode);
    }

    public T transform(STReceiveFieldsNode receiveFieldsNode) {
        return transformSyntaxNode(receiveFieldsNode);
    }

    public T transform(STRestDescriptorNode restDescriptorNode) {
        return transformSyntaxNode(restDescriptorNode);
    }

    public T transform(STDoubleGTTokenNode doubleGTTokenNode) {
        return transformSyntaxNode(doubleGTTokenNode);
    }

    public T transform(STTrippleGTTokenNode trippleGTTokenNode) {
        return transformSyntaxNode(trippleGTTokenNode);
    }

    public T transform(STWaitActionNode waitActionNode) {
        return transformSyntaxNode(waitActionNode);
    }

    public T transform(STWaitFieldsListNode waitFieldsListNode) {
        return transformSyntaxNode(waitFieldsListNode);
    }

    public T transform(STWaitFieldNode waitFieldNode) {
        return transformSyntaxNode(waitFieldNode);
    }

    public T transform(STAnnotAccessExpressionNode annotAccessExpressionNode) {
        return transformSyntaxNode(annotAccessExpressionNode);
    }

    public T transform(STQueryActionNode queryActionNode) {
        return transformSyntaxNode(queryActionNode);
    }

    public T transform(STOptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        return transformSyntaxNode(optionalFieldAccessExpressionNode);
    }

    public T transform(STConditionalExpressionNode conditionalExpressionNode) {
        return transformSyntaxNode(conditionalExpressionNode);
    }

    public T transform(STEnumDeclarationNode enumDeclarationNode) {
        return transformSyntaxNode(enumDeclarationNode);
    }

    public T transform(STEnumMemberNode enumMemberNode) {
        return transformSyntaxNode(enumMemberNode);
    }

    public T transform(STArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        return transformSyntaxNode(arrayTypeDescriptorNode);
    }

    public T transform(STTransactionStatementNode transactionStatementNode) {
        return transformSyntaxNode(transactionStatementNode);
    }

    public T transform(STRollbackStatementNode rollbackStatementNode) {
        return transformSyntaxNode(rollbackStatementNode);
    }

    public T transform(STRetryStatementNode retryStatementNode) {
        return transformSyntaxNode(retryStatementNode);
    }

    public T transform(STCommitActionNode commitActionNode) {
        return transformSyntaxNode(commitActionNode);
    }

    public T transform(STTransactionalExpressionNode transactionalExpressionNode) {
        return transformSyntaxNode(transactionalExpressionNode);
    }

    public T transform(STServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        return transformSyntaxNode(serviceConstructorExpressionNode);
    }

    public T transform(STByteArrayLiteralNode byteArrayLiteralNode) {
        return transformSyntaxNode(byteArrayLiteralNode);
    }

    public T transform(STXMLFilterExpressionNode xMLFilterExpressionNode) {
        return transformSyntaxNode(xMLFilterExpressionNode);
    }

    public T transform(STXMLStepExpressionNode xMLStepExpressionNode) {
        return transformSyntaxNode(xMLStepExpressionNode);
    }

    public T transform(STXMLNamePatternChainingNode xMLNamePatternChainingNode) {
        return transformSyntaxNode(xMLNamePatternChainingNode);
    }

    public T transform(STXMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        return transformSyntaxNode(xMLAtomicNamePatternNode);
    }

    public T transform(STTypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        return transformSyntaxNode(typeReferenceTypeDescNode);
    }

    // Tokens

    public T transform(STToken token) {
        return null;
    }

    public T transform(STIdentifierToken identifier) {
        return transform((STToken) identifier);
    }

    public T transform(STLiteralValueToken literalValueToken) {
        return transform((STToken) literalValueToken);
    }

    public T transform(STDocumentationLineToken documentationLineToken) {
        return transform((STToken) documentationLineToken);
    }

    public T transform(STMissingToken missingToken) {
        return transform((STToken) missingToken);
    }

    // Misc

    public T transform(STNodeList nodeList) {
        return transformSyntaxNode(nodeList);
    }

    /**
     * Transforms the given {@code STNode} into an object of type T.
     * <p>
     * This method is invoked by each transform method in this class. You can
     * override it to provide a common transformation for each node.
     *
     * @param node the {@code STNode} to be transformed
     * @return the transformed object
     */
    protected abstract T transformSyntaxNode(STNode node);
}

