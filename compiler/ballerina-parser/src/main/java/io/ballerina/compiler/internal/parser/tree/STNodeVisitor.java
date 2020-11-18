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

import io.ballerina.compiler.internal.syntax.SyntaxUtils;

/**
 * The {@code STNodeVisitor} visits each node in the internal syntax tree allowing
 * us to do something at each node.
 * <p>
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class STNodeVisitor {

    public void visit(STModulePartNode modulePartNode) {
        visitSyntaxNode(modulePartNode);
    }

    public void visit(STFunctionDefinitionNode functionDefinitionNode) {
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(STImportDeclarationNode importDeclarationNode) {
        visitSyntaxNode(importDeclarationNode);
    }

    public void visit(STListenerDeclarationNode listenerDeclarationNode) {
        visitSyntaxNode(listenerDeclarationNode);
    }

    public void visit(STTypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(STServiceDeclarationNode serviceDeclarationNode) {
        visitSyntaxNode(serviceDeclarationNode);
    }

    public void visit(STAssignmentStatementNode assignmentStatementNode) {
        visitSyntaxNode(assignmentStatementNode);
    }

    public void visit(STCompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        visitSyntaxNode(compoundAssignmentStatementNode);
    }

    public void visit(STVariableDeclarationNode variableDeclarationNode) {
        visitSyntaxNode(variableDeclarationNode);
    }

    public void visit(STBlockStatementNode blockStatementNode) {
        visitSyntaxNode(blockStatementNode);
    }

    public void visit(STBreakStatementNode breakStatementNode) {
        visitSyntaxNode(breakStatementNode);
    }

    public void visit(STFailStatementNode failStatementNode) {
        visitSyntaxNode(failStatementNode);
    }

    public void visit(STExpressionStatementNode expressionStatementNode) {
        visitSyntaxNode(expressionStatementNode);
    }

    public void visit(STContinueStatementNode continueStatementNode) {
        visitSyntaxNode(continueStatementNode);
    }

    public void visit(STExternalFunctionBodyNode externalFunctionBodyNode) {
        visitSyntaxNode(externalFunctionBodyNode);
    }

    public void visit(STIfElseStatementNode ifElseStatementNode) {
        visitSyntaxNode(ifElseStatementNode);
    }

    public void visit(STElseBlockNode elseBlockNode) {
        visitSyntaxNode(elseBlockNode);
    }

    public void visit(STWhileStatementNode whileStatementNode) {
        visitSyntaxNode(whileStatementNode);
    }

    public void visit(STPanicStatementNode panicStatementNode) {
        visitSyntaxNode(panicStatementNode);
    }

    public void visit(STReturnStatementNode returnStatementNode) {
        visitSyntaxNode(returnStatementNode);
    }

    public void visit(STLocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        visitSyntaxNode(localTypeDefinitionStatementNode);
    }

    public void visit(STLockStatementNode lockStatementNode) {
        visitSyntaxNode(lockStatementNode);
    }

    public void visit(STForkStatementNode forkStatementNode) {
        visitSyntaxNode(forkStatementNode);
    }

    public void visit(STForEachStatementNode forEachStatementNode) {
        visitSyntaxNode(forEachStatementNode);
    }

    public void visit(STBinaryExpressionNode binaryExpressionNode) {
        visitSyntaxNode(binaryExpressionNode);
    }

    public void visit(STBracedExpressionNode bracedExpressionNode) {
        visitSyntaxNode(bracedExpressionNode);
    }

    public void visit(STCheckExpressionNode checkExpressionNode) {
        visitSyntaxNode(checkExpressionNode);
    }

    public void visit(STFieldAccessExpressionNode fieldAccessExpressionNode) {
        visitSyntaxNode(fieldAccessExpressionNode);
    }

    public void visit(STFunctionCallExpressionNode functionCallExpressionNode) {
        visitSyntaxNode(functionCallExpressionNode);
    }

    public void visit(STMethodCallExpressionNode methodCallExpressionNode) {
        visitSyntaxNode(methodCallExpressionNode);
    }

    public void visit(STMappingConstructorExpressionNode mappingConstructorExpressionNode) {
        visitSyntaxNode(mappingConstructorExpressionNode);
    }

    public void visit(STIndexedExpressionNode indexedExpressionNode) {
        visitSyntaxNode(indexedExpressionNode);
    }

    public void visit(STTypeofExpressionNode typeofExpressionNode) {
        visitSyntaxNode(typeofExpressionNode);
    }

    public void visit(STUnaryExpressionNode unaryExpressionNode) {
        visitSyntaxNode(unaryExpressionNode);
    }

    public void visit(STComputedNameFieldNode computedNameFieldNode) {
        visitSyntaxNode(computedNameFieldNode);
    }

    public void visit(STConstantDeclarationNode constantDeclarationNode) {
        visitSyntaxNode(constantDeclarationNode);
    }

    public void visit(STDefaultableParameterNode defaultableParameterNode) {
        visitSyntaxNode(defaultableParameterNode);
    }

    public void visit(STRequiredParameterNode requiredParameterNode) {
        visitSyntaxNode(requiredParameterNode);
    }

    public void visit(STRestParameterNode restParameterNode) {
        visitSyntaxNode(restParameterNode);
    }

    public void visit(STImportOrgNameNode importOrgNameNode) {
        visitSyntaxNode(importOrgNameNode);
    }

    public void visit(STImportPrefixNode importPrefixNode) {
        visitSyntaxNode(importPrefixNode);
    }

    public void visit(STImportVersionNode importVersionNode) {
        visitSyntaxNode(importVersionNode);
    }

    public void visit(STSpecificFieldNode specificFieldNode) {
        visitSyntaxNode(specificFieldNode);
    }

    public void visit(STSpreadFieldNode spreadFieldNode) {
        visitSyntaxNode(spreadFieldNode);
    }

    public void visit(STNamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
    }

    public void visit(STPositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
    }

    public void visit(STRestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
    }

    public void visit(STObjectTypeDescriptorNode objectTypeDescriptorNode) {
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    public void visit(STObjectConstructorExpressionNode objectConstructorExpressionNode) {
        visitSyntaxNode(objectConstructorExpressionNode);
    }

    public void visit(STRecordTypeDescriptorNode recordTypeDescriptorNode) {
        visitSyntaxNode(recordTypeDescriptorNode);
    }

    public void visit(STReturnTypeDescriptorNode returnTypeDescriptorNode) {
        visitSyntaxNode(returnTypeDescriptorNode);
    }

    public void visit(STNilTypeDescriptorNode nilTypeDescriptorNode) {
        visitSyntaxNode(nilTypeDescriptorNode);
    }

    public void visit(STOptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        visitSyntaxNode(optionalTypeDescriptorNode);
    }

    public void visit(STObjectFieldNode objectFieldNode) {
        visitSyntaxNode(objectFieldNode);
    }

    public void visit(STRecordFieldNode recordFieldNode) {
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(STRecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        visitSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public void visit(STRecordRestDescriptorNode recordRestDescriptorNode) {
        visitSyntaxNode(recordRestDescriptorNode);
    }

    public void visit(STTypeReferenceNode typeReferenceNode) {
        visitSyntaxNode(typeReferenceNode);
    }

    public void visit(STServiceBodyNode serviceBodyNode) {
        visitSyntaxNode(serviceBodyNode);
    }

    public void visit(STAnnotationNode annotationNode) {
        visitSyntaxNode(annotationNode);
    }

    public void visit(STMetadataNode metadataNode) {
        visitSyntaxNode(metadataNode);
    }

    public void visit(STModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        visitSyntaxNode(moduleVariableDeclarationNode);
    }

    public void visit(STTypeTestExpressionNode typeTestExpressionNode) {
        visitSyntaxNode(typeTestExpressionNode);
    }

    public void visit(STRemoteMethodCallActionNode remoteMethodCallActionNode) {
        visitSyntaxNode(remoteMethodCallActionNode);
    }

    public void visit(STParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        visitSyntaxNode(parameterizedTypeDescriptorNode);
    }

    public void visit(STNilLiteralNode nilLiteralNode) {
        visitSyntaxNode(nilLiteralNode);
    }

    public void visit(STAnnotationDeclarationNode annotationDeclarationNode) {
        visitSyntaxNode(annotationDeclarationNode);
    }

    public void visit(STAnnotationAttachPointNode annotationAttachPointNode) {
        visitSyntaxNode(annotationAttachPointNode);
    }

    public void visit(STXMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        visitSyntaxNode(xMLNamespaceDeclarationNode);
    }

    public void visit(STModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        visitSyntaxNode(moduleXMLNamespaceDeclarationNode);
    }

    public void visit(STFunctionBodyBlockNode functionBodyBlockNode) {
        visitSyntaxNode(functionBodyBlockNode);
    }

    public void visit(STNamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        visitSyntaxNode(namedWorkerDeclarationNode);
    }

    public void visit(STNamedWorkerDeclarator namedWorkerDeclarator) {
        visitSyntaxNode(namedWorkerDeclarator);
    }

    public void visit(STBasicLiteralNode basicLiteralNode) {
        visitSyntaxNode(basicLiteralNode);
    }

    public void visit(STSimpleNameReferenceNode simpleNameReferenceNode) {
        visitSyntaxNode(simpleNameReferenceNode);
    }

    public void visit(STQualifiedNameReferenceNode qualifiedNameReferenceNode) {
        visitSyntaxNode(qualifiedNameReferenceNode);
    }

    public void visit(STBuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        visitSyntaxNode(builtinSimpleNameReferenceNode);
    }

    public void visit(STTrapExpressionNode trapExpressionNode) {
        visitSyntaxNode(trapExpressionNode);
    }

    public void visit(STListConstructorExpressionNode listConstructorExpressionNode) {
        visitSyntaxNode(listConstructorExpressionNode);
    }

    public void visit(STTypeCastExpressionNode typeCastExpressionNode) {
        visitSyntaxNode(typeCastExpressionNode);
    }

    public void visit(STTypeCastParamNode typeCastParamNode) {
        visitSyntaxNode(typeCastParamNode);
    }

    public void visit(STUnionTypeDescriptorNode unionTypeDescriptorNode) {
        visitSyntaxNode(unionTypeDescriptorNode);
    }

    public void visit(STTableConstructorExpressionNode tableConstructorExpressionNode) {
        visitSyntaxNode(tableConstructorExpressionNode);
    }

    public void visit(STKeySpecifierNode keySpecifierNode) {
        visitSyntaxNode(keySpecifierNode);
    }

    public void visit(STErrorTypeDescriptorNode errorTypeDescriptorNode) {
        visitSyntaxNode(errorTypeDescriptorNode);
    }

    public void visit(STErrorTypeParamsNode errorTypeParamsNode) {
        visitSyntaxNode(errorTypeParamsNode);
    }

    public void visit(STStreamTypeDescriptorNode streamTypeDescriptorNode) {
        visitSyntaxNode(streamTypeDescriptorNode);
    }

    public void visit(STStreamTypeParamsNode streamTypeParamsNode) {
        visitSyntaxNode(streamTypeParamsNode);
    }

    public void visit(STTypedescTypeDescriptorNode typedescTypeDescriptorNode) {
        visitSyntaxNode(typedescTypeDescriptorNode);
    }

    public void visit(STLetExpressionNode letExpressionNode) {
        visitSyntaxNode(letExpressionNode);
    }

    public void visit(STXmlTypeDescriptorNode xmlTypeDescriptorNode) {
        visitSyntaxNode(xmlTypeDescriptorNode);
    }

    public void visit(STLetVariableDeclarationNode letVariableDeclarationNode) {
        visitSyntaxNode(letVariableDeclarationNode);
    }

    public void visit(STTemplateExpressionNode templateExpressionNode) {
        visitSyntaxNode(templateExpressionNode);
    }

    public void visit(STXMLElementNode xMLElementNode) {
        visitSyntaxNode(xMLElementNode);
    }

    public void visit(STXMLStartTagNode xMLStartTagNode) {
        visitSyntaxNode(xMLStartTagNode);
    }

    public void visit(STXMLEndTagNode xMLEndTagNode) {
        visitSyntaxNode(xMLEndTagNode);
    }

    public void visit(STXMLSimpleNameNode xMLSimpleNameNode) {
        visitSyntaxNode(xMLSimpleNameNode);
    }

    public void visit(STXMLQualifiedNameNode xMLQualifiedNameNode) {
        visitSyntaxNode(xMLQualifiedNameNode);
    }

    public void visit(STXMLEmptyElementNode xMLEmptyElementNode) {
        visitSyntaxNode(xMLEmptyElementNode);
    }

    public void visit(STInterpolationNode interpolationNode) {
        visitSyntaxNode(interpolationNode);
    }

    public void visit(STXMLTextNode xMLTextNode) {
        visitSyntaxNode(xMLTextNode);
    }

    public void visit(STXMLAttributeNode xMLAttributeNode) {
        visitSyntaxNode(xMLAttributeNode);
    }

    public void visit(STXMLAttributeValue xMLAttributeValue) {
        visitSyntaxNode(xMLAttributeValue);
    }

    public void visit(STXMLComment xMLComment) {
        visitSyntaxNode(xMLComment);
    }

    public void visit(STXMLProcessingInstruction xMLProcessingInstruction) {
        visitSyntaxNode(xMLProcessingInstruction);
    }

    public void visit(STTableTypeDescriptorNode tableTypeDescriptorNode) {
        visitSyntaxNode(tableTypeDescriptorNode);
    }

    public void visit(STTypeParameterNode typeParameterNode) {
        visitSyntaxNode(typeParameterNode);
    }

    public void visit(STKeyTypeConstraintNode keyTypeConstraintNode) {
        visitSyntaxNode(keyTypeConstraintNode);
    }

    public void visit(STFunctionTypeDescriptorNode functionTypeDescriptorNode) {
        visitSyntaxNode(functionTypeDescriptorNode);
    }

    public void visit(STFunctionSignatureNode functionSignatureNode) {
        visitSyntaxNode(functionSignatureNode);
    }

    public void visit(STExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        visitSyntaxNode(explicitAnonymousFunctionExpressionNode);
    }

    public void visit(STExpressionFunctionBodyNode expressionFunctionBodyNode) {
        visitSyntaxNode(expressionFunctionBodyNode);
    }

    public void visit(STTupleTypeDescriptorNode tupleTypeDescriptorNode) {
        visitSyntaxNode(tupleTypeDescriptorNode);
    }

    public void visit(STParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        visitSyntaxNode(parenthesisedTypeDescriptorNode);
    }

    public void visit(STExplicitNewExpressionNode explicitNewExpressionNode) {
        visitSyntaxNode(explicitNewExpressionNode);
    }

    public void visit(STImplicitNewExpressionNode implicitNewExpressionNode) {
        visitSyntaxNode(implicitNewExpressionNode);
    }

    public void visit(STParenthesizedArgList parenthesizedArgList) {
        visitSyntaxNode(parenthesizedArgList);
    }

    public void visit(STQueryConstructTypeNode queryConstructTypeNode) {
        visitSyntaxNode(queryConstructTypeNode);
    }

    public void visit(STFromClauseNode fromClauseNode) {
        visitSyntaxNode(fromClauseNode);
    }

    public void visit(STWhereClauseNode whereClauseNode) {
        visitSyntaxNode(whereClauseNode);
    }

    public void visit(STLetClauseNode letClauseNode) {
        visitSyntaxNode(letClauseNode);
    }

    public void visit(STJoinClauseNode joinClauseNode) {
        visitSyntaxNode(joinClauseNode);
    }

    public void visit(STOnClauseNode onClauseNode) {
        visitSyntaxNode(onClauseNode);
    }

    public void visit(STLimitClauseNode limitClauseNode) {
        visitSyntaxNode(limitClauseNode);
    }

    public void visit(STOnConflictClauseNode onConflictClauseNode) {
        visitSyntaxNode(onConflictClauseNode);
    }

    public void visit(STQueryPipelineNode queryPipelineNode) {
        visitSyntaxNode(queryPipelineNode);
    }

    public void visit(STSelectClauseNode selectClauseNode) {
        visitSyntaxNode(selectClauseNode);
    }

    public void visit(STQueryExpressionNode queryExpressionNode) {
        visitSyntaxNode(queryExpressionNode);
    }

    public void visit(STQueryActionNode queryActionNode) {
        visitSyntaxNode(queryActionNode);
    }

    public void visit(STIntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        visitSyntaxNode(intersectionTypeDescriptorNode);
    }

    public void visit(STImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        visitSyntaxNode(implicitAnonymousFunctionParameters);
    }

    public void visit(STImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        visitSyntaxNode(implicitAnonymousFunctionExpressionNode);
    }

    public void visit(STStartActionNode startActionNode) {
        visitSyntaxNode(startActionNode);
    }

    public void visit(STFlushActionNode flushActionNode) {
        visitSyntaxNode(flushActionNode);
    }

    public void visit(STSingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        visitSyntaxNode(singletonTypeDescriptorNode);
    }

    public void visit(STMethodDeclarationNode methodDeclarationNode) {
        visitSyntaxNode(methodDeclarationNode);
    }

    public void visit(STTypedBindingPatternNode typedBindingPatternNode) {
        visitSyntaxNode(typedBindingPatternNode);
    }

    public void visit(STCaptureBindingPatternNode captureBindingPatternNode) {
        visitSyntaxNode(captureBindingPatternNode);
    }

    public void visit(STWildcardBindingPatternNode wildcardBindingPatternNode) {
        visitSyntaxNode(wildcardBindingPatternNode);
    }

    public void visit(STListBindingPatternNode listBindingPatternNode) {
        visitSyntaxNode(listBindingPatternNode);
    }

    public void visit(STMappingBindingPatternNode mappingBindingPatternNode) {
        visitSyntaxNode(mappingBindingPatternNode);
    }

    public void visit(STFieldBindingPatternFullNode fieldBindingPatternFullNode) {
        visitSyntaxNode(fieldBindingPatternFullNode);
    }

    public void visit(STFieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        visitSyntaxNode(fieldBindingPatternVarnameNode);
    }

    public void visit(STRestBindingPatternNode restBindingPatternNode) {
        visitSyntaxNode(restBindingPatternNode);
    }

    public void visit(STErrorBindingPatternNode errorBindingPatternNode) {
        visitSyntaxNode(errorBindingPatternNode);
    }

    public void visit(STNamedArgBindingPatternNode namedArgBindingPatternNode) {
        visitSyntaxNode(namedArgBindingPatternNode);
    }

    public void visit(STAsyncSendActionNode asyncSendActionNode) {
        visitSyntaxNode(asyncSendActionNode);
    }

    public void visit(STSyncSendActionNode syncSendActionNode) {
        visitSyntaxNode(syncSendActionNode);
    }

    public void visit(STReceiveActionNode receiveActionNode) {
        visitSyntaxNode(receiveActionNode);
    }

    public void visit(STReceiveFieldsNode receiveFieldsNode) {
        visitSyntaxNode(receiveFieldsNode);
    }

    public void visit(STRestDescriptorNode restDescriptorNode) {
        visitSyntaxNode(restDescriptorNode);
    }

    public void visit(STDoubleGTTokenNode doubleGTTokenNode) {
        visitSyntaxNode(doubleGTTokenNode);
    }

    public void visit(STTrippleGTTokenNode trippleGTTokenNode) {
        visitSyntaxNode(trippleGTTokenNode);
    }

    public void visit(STWaitActionNode waitActionNode) {
        visitSyntaxNode(waitActionNode);
    }

    public void visit(STWaitFieldsListNode waitFieldsListNode) {
        visitSyntaxNode(waitFieldsListNode);
    }

    public void visit(STWaitFieldNode waitFieldNode) {
        visitSyntaxNode(waitFieldNode);
    }

    public void visit(STAnnotAccessExpressionNode annotAccessExpressionNode) {
        visitSyntaxNode(annotAccessExpressionNode);
    }

    public void visit(STOptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        visitSyntaxNode(optionalFieldAccessExpressionNode);
    }

    public void visit(STConditionalExpressionNode conditionalExpressionNode) {
        visitSyntaxNode(conditionalExpressionNode);
    }

    public void visit(STEnumDeclarationNode enumDeclarationNode) {
        visitSyntaxNode(enumDeclarationNode);
    }

    public void visit(STEnumMemberNode enumMemberNode) {
        visitSyntaxNode(enumMemberNode);
    }

    public void visit(STArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        visitSyntaxNode(arrayTypeDescriptorNode);
    }

    public void visit(STTransactionStatementNode transactionStatementNode) {
        visitSyntaxNode(transactionStatementNode);
    }

    public void visit(STRollbackStatementNode rollbackStatementNode) {
        visitSyntaxNode(rollbackStatementNode);
    }

    public void visit(STRetryStatementNode retryStatementNode) {
        visitSyntaxNode(retryStatementNode);
    }

    public void visit(STCommitActionNode commitActionNode) {
        visitSyntaxNode(commitActionNode);
    }

    public void visit(STTransactionalExpressionNode transactionalExpressionNode) {
        visitSyntaxNode(transactionalExpressionNode);
    }

    public void visit(STServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        visitSyntaxNode(serviceConstructorExpressionNode);
    }

    public void visit(STByteArrayLiteralNode byteArrayLiteralNode) {
        visitSyntaxNode(byteArrayLiteralNode);
    }

    public void visit(STXMLFilterExpressionNode xMLFilterExpressionNode) {
        visitSyntaxNode(xMLFilterExpressionNode);
    }

    public void visit(STXMLStepExpressionNode xMLStepExpressionNode) {
        visitSyntaxNode(xMLStepExpressionNode);
    }

    public void visit(STXMLNamePatternChainingNode xMLNamePatternChainingNode) {
        visitSyntaxNode(xMLNamePatternChainingNode);
    }

    public void visit(STXMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        visitSyntaxNode(xMLAtomicNamePatternNode);
    }

    public void visit(STTypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        visitSyntaxNode(typeReferenceTypeDescNode);
    }

    public void visit(STMatchStatementNode matchStatementNode) {
        visitSyntaxNode(matchStatementNode);
    }

    public void visit(STMatchClauseNode matchClauseNode) {
        visitSyntaxNode(matchClauseNode);
    }

    public void visit(STMatchGuardNode matchGuardNode) {
        visitSyntaxNode(matchGuardNode);
    }

    public void visit(STDistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        visitSyntaxNode(distinctTypeDescriptorNode);
    }

    public void visit(STListMatchPatternNode listMatchPatternNode) {
        visitSyntaxNode(listMatchPatternNode);
    }

    public void visit(STRestMatchPatternNode restMatchPatternNode) {
        visitSyntaxNode(restMatchPatternNode);
    }

    public void visit(STMappingMatchPatternNode mappingMatchPatternNode) {
        visitSyntaxNode(mappingMatchPatternNode);
    }

    public void visit(STFieldMatchPatternNode fieldMatchPatternNode) {
        visitSyntaxNode(fieldMatchPatternNode);
    }

    public void visit(STErrorMatchPatternNode errorMatchPatternNode) {
        visitSyntaxNode(errorMatchPatternNode);
    }

    public void visit(STNamedArgMatchPatternNode namedArgMatchPatternNode) {
        visitSyntaxNode(namedArgMatchPatternNode);
    }

    public void visit(STMarkdownDocumentationNode markdownDocumentationNode) {
        visitSyntaxNode(markdownDocumentationNode);
    }

    public void visit(STMarkdownDocumentationLineNode markdownDocumentationLineNode) {
        visitSyntaxNode(markdownDocumentationLineNode);
    }

    public void visit(STMarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        visitSyntaxNode(markdownParameterDocumentationLineNode);
    }

    public void visit(STDocumentationReferenceNode documentationReferenceNode) {
        visitSyntaxNode(documentationReferenceNode);
    }

    public void visit(STOrderByClauseNode orderByClauseNode) {
        visitSyntaxNode(orderByClauseNode);
    }

    public void visit(STOrderKeyNode orderKeyNode) {
        visitSyntaxNode(orderKeyNode);
    }

    public void visit(STOnFailClauseNode onFailClauseNode) {
        visitSyntaxNode(onFailClauseNode);
    }

    public void visit(STDoStatementNode doStatementNode) {
        visitSyntaxNode(doStatementNode);
    }

    public void visit(STClassDefinitionNode classDefinitionNode) {
        visitSyntaxNode(classDefinitionNode);
    }

    // STNodeList
    public void visit(STNodeList nodeList) {
        visitChildren(nodeList);
    }

    // Tokens

    public void visit(STToken token) {
    }

    // Misc

    protected void visitSyntaxNode(STNode node) {
        if (SyntaxUtils.isToken(node)) {
            node.accept(this);
            return;
        }

        visitChildren(node);
    }

    private void visitChildren(STNode node) {
        for (int bucket = 0; bucket < node.bucketCount(); bucket++) {
            STNode child = node.childInBucket(bucket);
            if (SyntaxUtils.isSTNodePresent(child)) {
                child.accept(this);
            }
        }
    }
}

