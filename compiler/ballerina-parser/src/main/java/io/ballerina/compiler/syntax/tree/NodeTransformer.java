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


/**
 * The {@code NodeTransformer} transform each node in the syntax tree to
 * another object of type T.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * This class allows you to transform the syntax tree into something else without
 * mutating instance variables.
 * <p>
 * There exists a transform method for each node in the Ballerina syntax tree.
 * These methods return T. If you are looking for a visitor that has visit
 * methods that return void, see {@link NodeVisitor}.
 *
 * This is a generated class.
 *
 * @param <T> the type of class that is returned by visit methods
 * @see NodeVisitor
 * @since 2.0.0
 */
public abstract class NodeTransformer<T> {

    public T transform(ModulePartNode modulePartNode) {
        return transformSyntaxNode(modulePartNode);
    }

    public T transform(FunctionDefinitionNode functionDefinitionNode) {
        return transformSyntaxNode(functionDefinitionNode);
    }

    public T transform(ImportDeclarationNode importDeclarationNode) {
        return transformSyntaxNode(importDeclarationNode);
    }

    public T transform(ListenerDeclarationNode listenerDeclarationNode) {
        return transformSyntaxNode(listenerDeclarationNode);
    }

    public T transform(TypeDefinitionNode typeDefinitionNode) {
        return transformSyntaxNode(typeDefinitionNode);
    }

    public T transform(ServiceDeclarationNode serviceDeclarationNode) {
        return transformSyntaxNode(serviceDeclarationNode);
    }

    public T transform(AssignmentStatementNode assignmentStatementNode) {
        return transformSyntaxNode(assignmentStatementNode);
    }

    public T transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        return transformSyntaxNode(compoundAssignmentStatementNode);
    }

    public T transform(VariableDeclarationNode variableDeclarationNode) {
        return transformSyntaxNode(variableDeclarationNode);
    }

    public T transform(BlockStatementNode blockStatementNode) {
        return transformSyntaxNode(blockStatementNode);
    }

    public T transform(BreakStatementNode breakStatementNode) {
        return transformSyntaxNode(breakStatementNode);
    }

    public T transform(FailStatementNode failStatementNode) {
        return transformSyntaxNode(failStatementNode);
    }

    public T transform(ExpressionStatementNode expressionStatementNode) {
        return transformSyntaxNode(expressionStatementNode);
    }

    public T transform(ContinueStatementNode continueStatementNode) {
        return transformSyntaxNode(continueStatementNode);
    }

    public T transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        return transformSyntaxNode(externalFunctionBodyNode);
    }

    public T transform(IfElseStatementNode ifElseStatementNode) {
        return transformSyntaxNode(ifElseStatementNode);
    }

    public T transform(ElseBlockNode elseBlockNode) {
        return transformSyntaxNode(elseBlockNode);
    }

    public T transform(WhileStatementNode whileStatementNode) {
        return transformSyntaxNode(whileStatementNode);
    }

    public T transform(PanicStatementNode panicStatementNode) {
        return transformSyntaxNode(panicStatementNode);
    }

    public T transform(ReturnStatementNode returnStatementNode) {
        return transformSyntaxNode(returnStatementNode);
    }

    public T transform(LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        return transformSyntaxNode(localTypeDefinitionStatementNode);
    }

    public T transform(LockStatementNode lockStatementNode) {
        return transformSyntaxNode(lockStatementNode);
    }

    public T transform(ForkStatementNode forkStatementNode) {
        return transformSyntaxNode(forkStatementNode);
    }

    public T transform(ForEachStatementNode forEachStatementNode) {
        return transformSyntaxNode(forEachStatementNode);
    }

    public T transform(BinaryExpressionNode binaryExpressionNode) {
        return transformSyntaxNode(binaryExpressionNode);
    }

    public T transform(BracedExpressionNode bracedExpressionNode) {
        return transformSyntaxNode(bracedExpressionNode);
    }

    public T transform(CheckExpressionNode checkExpressionNode) {
        return transformSyntaxNode(checkExpressionNode);
    }

    public T transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        return transformSyntaxNode(fieldAccessExpressionNode);
    }

    public T transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return transformSyntaxNode(functionCallExpressionNode);
    }

    public T transform(MethodCallExpressionNode methodCallExpressionNode) {
        return transformSyntaxNode(methodCallExpressionNode);
    }

    public T transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return transformSyntaxNode(mappingConstructorExpressionNode);
    }

    public T transform(IndexedExpressionNode indexedExpressionNode) {
        return transformSyntaxNode(indexedExpressionNode);
    }

    public T transform(TypeofExpressionNode typeofExpressionNode) {
        return transformSyntaxNode(typeofExpressionNode);
    }

    public T transform(UnaryExpressionNode unaryExpressionNode) {
        return transformSyntaxNode(unaryExpressionNode);
    }

    public T transform(ComputedNameFieldNode computedNameFieldNode) {
        return transformSyntaxNode(computedNameFieldNode);
    }

    public T transform(ConstantDeclarationNode constantDeclarationNode) {
        return transformSyntaxNode(constantDeclarationNode);
    }

    public T transform(DefaultableParameterNode defaultableParameterNode) {
        return transformSyntaxNode(defaultableParameterNode);
    }

    public T transform(RequiredParameterNode requiredParameterNode) {
        return transformSyntaxNode(requiredParameterNode);
    }

    public T transform(IncludedRecordParameterNode includedRecordParameterNode) {
        return transformSyntaxNode(includedRecordParameterNode);
    }

    public T transform(RestParameterNode restParameterNode) {
        return transformSyntaxNode(restParameterNode);
    }

    public T transform(ImportOrgNameNode importOrgNameNode) {
        return transformSyntaxNode(importOrgNameNode);
    }

    public T transform(ImportPrefixNode importPrefixNode) {
        return transformSyntaxNode(importPrefixNode);
    }

    public T transform(SpecificFieldNode specificFieldNode) {
        return transformSyntaxNode(specificFieldNode);
    }

    public T transform(SpreadFieldNode spreadFieldNode) {
        return transformSyntaxNode(spreadFieldNode);
    }

    public T transform(NamedArgumentNode namedArgumentNode) {
        return transformSyntaxNode(namedArgumentNode);
    }

    public T transform(PositionalArgumentNode positionalArgumentNode) {
        return transformSyntaxNode(positionalArgumentNode);
    }

    public T transform(RestArgumentNode restArgumentNode) {
        return transformSyntaxNode(restArgumentNode);
    }

    public T transform(InferredTypedescDefaultNode inferredTypedescDefaultNode) {
        return transformSyntaxNode(inferredTypedescDefaultNode);
    }

    public T transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        return transformSyntaxNode(objectTypeDescriptorNode);
    }

    public T transform(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        return transformSyntaxNode(objectConstructorExpressionNode);
    }

    public T transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        return transformSyntaxNode(recordTypeDescriptorNode);
    }

    public T transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        return transformSyntaxNode(returnTypeDescriptorNode);
    }

    public T transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        return transformSyntaxNode(nilTypeDescriptorNode);
    }

    public T transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        return transformSyntaxNode(optionalTypeDescriptorNode);
    }

    public T transform(ObjectFieldNode objectFieldNode) {
        return transformSyntaxNode(objectFieldNode);
    }

    public T transform(RecordFieldNode recordFieldNode) {
        return transformSyntaxNode(recordFieldNode);
    }

    public T transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return transformSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public T transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        return transformSyntaxNode(recordRestDescriptorNode);
    }

    public T transform(TypeReferenceNode typeReferenceNode) {
        return transformSyntaxNode(typeReferenceNode);
    }

    public T transform(AnnotationNode annotationNode) {
        return transformSyntaxNode(annotationNode);
    }

    public T transform(MetadataNode metadataNode) {
        return transformSyntaxNode(metadataNode);
    }

    public T transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return transformSyntaxNode(moduleVariableDeclarationNode);
    }

    public T transform(TypeTestExpressionNode typeTestExpressionNode) {
        return transformSyntaxNode(typeTestExpressionNode);
    }

    public T transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        return transformSyntaxNode(remoteMethodCallActionNode);
    }

    public T transform(MapTypeDescriptorNode mapTypeDescriptorNode) {
        return transformSyntaxNode(mapTypeDescriptorNode);
    }

    public T transform(NilLiteralNode nilLiteralNode) {
        return transformSyntaxNode(nilLiteralNode);
    }

    public T transform(AnnotationDeclarationNode annotationDeclarationNode) {
        return transformSyntaxNode(annotationDeclarationNode);
    }

    public T transform(AnnotationAttachPointNode annotationAttachPointNode) {
        return transformSyntaxNode(annotationAttachPointNode);
    }

    public T transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        return transformSyntaxNode(xMLNamespaceDeclarationNode);
    }

    public T transform(ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        return transformSyntaxNode(moduleXMLNamespaceDeclarationNode);
    }

    public T transform(FunctionBodyBlockNode functionBodyBlockNode) {
        return transformSyntaxNode(functionBodyBlockNode);
    }

    public T transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return transformSyntaxNode(namedWorkerDeclarationNode);
    }

    public T transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        return transformSyntaxNode(namedWorkerDeclarator);
    }

    public T transform(BasicLiteralNode basicLiteralNode) {
        return transformSyntaxNode(basicLiteralNode);
    }

    public T transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        return transformSyntaxNode(simpleNameReferenceNode);
    }

    public T transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return transformSyntaxNode(qualifiedNameReferenceNode);
    }

    public T transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        return transformSyntaxNode(builtinSimpleNameReferenceNode);
    }

    public T transform(TrapExpressionNode trapExpressionNode) {
        return transformSyntaxNode(trapExpressionNode);
    }

    public T transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        return transformSyntaxNode(listConstructorExpressionNode);
    }

    public T transform(TypeCastExpressionNode typeCastExpressionNode) {
        return transformSyntaxNode(typeCastExpressionNode);
    }

    public T transform(TypeCastParamNode typeCastParamNode) {
        return transformSyntaxNode(typeCastParamNode);
    }

    public T transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        return transformSyntaxNode(unionTypeDescriptorNode);
    }

    public T transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        return transformSyntaxNode(tableConstructorExpressionNode);
    }

    public T transform(KeySpecifierNode keySpecifierNode) {
        return transformSyntaxNode(keySpecifierNode);
    }

    public T transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        return transformSyntaxNode(streamTypeDescriptorNode);
    }

    public T transform(StreamTypeParamsNode streamTypeParamsNode) {
        return transformSyntaxNode(streamTypeParamsNode);
    }

    public T transform(LetExpressionNode letExpressionNode) {
        return transformSyntaxNode(letExpressionNode);
    }

    public T transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return transformSyntaxNode(letVariableDeclarationNode);
    }

    public T transform(TemplateExpressionNode templateExpressionNode) {
        return transformSyntaxNode(templateExpressionNode);
    }

    public T transform(XMLElementNode xMLElementNode) {
        return transformSyntaxNode(xMLElementNode);
    }

    public T transform(XMLStartTagNode xMLStartTagNode) {
        return transformSyntaxNode(xMLStartTagNode);
    }

    public T transform(XMLEndTagNode xMLEndTagNode) {
        return transformSyntaxNode(xMLEndTagNode);
    }

    public T transform(XMLSimpleNameNode xMLSimpleNameNode) {
        return transformSyntaxNode(xMLSimpleNameNode);
    }

    public T transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        return transformSyntaxNode(xMLQualifiedNameNode);
    }

    public T transform(XMLEmptyElementNode xMLEmptyElementNode) {
        return transformSyntaxNode(xMLEmptyElementNode);
    }

    public T transform(InterpolationNode interpolationNode) {
        return transformSyntaxNode(interpolationNode);
    }

    public T transform(XMLTextNode xMLTextNode) {
        return transformSyntaxNode(xMLTextNode);
    }

    public T transform(XMLAttributeNode xMLAttributeNode) {
        return transformSyntaxNode(xMLAttributeNode);
    }

    public T transform(XMLAttributeValue xMLAttributeValue) {
        return transformSyntaxNode(xMLAttributeValue);
    }

    public T transform(XMLComment xMLComment) {
        return transformSyntaxNode(xMLComment);
    }

    public T transform(XMLCDATANode xMLCDATANode) {
        return transformSyntaxNode(xMLCDATANode);
    }

    public T transform(XMLProcessingInstruction xMLProcessingInstruction) {
        return transformSyntaxNode(xMLProcessingInstruction);
    }

    public T transform(TableTypeDescriptorNode tableTypeDescriptorNode) {
        return transformSyntaxNode(tableTypeDescriptorNode);
    }

    public T transform(TypeParameterNode typeParameterNode) {
        return transformSyntaxNode(typeParameterNode);
    }

    public T transform(KeyTypeConstraintNode keyTypeConstraintNode) {
        return transformSyntaxNode(keyTypeConstraintNode);
    }

    public T transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        return transformSyntaxNode(functionTypeDescriptorNode);
    }

    public T transform(FunctionSignatureNode functionSignatureNode) {
        return transformSyntaxNode(functionSignatureNode);
    }

    public T transform(ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        return transformSyntaxNode(explicitAnonymousFunctionExpressionNode);
    }

    public T transform(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        return transformSyntaxNode(expressionFunctionBodyNode);
    }

    public T transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        return transformSyntaxNode(tupleTypeDescriptorNode);
    }

    public T transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        return transformSyntaxNode(parenthesisedTypeDescriptorNode);
    }

    public T transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        return transformSyntaxNode(explicitNewExpressionNode);
    }

    public T transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        return transformSyntaxNode(implicitNewExpressionNode);
    }

    public T transform(ParenthesizedArgList parenthesizedArgList) {
        return transformSyntaxNode(parenthesizedArgList);
    }

    public T transform(QueryConstructTypeNode queryConstructTypeNode) {
        return transformSyntaxNode(queryConstructTypeNode);
    }

    public T transform(FromClauseNode fromClauseNode) {
        return transformSyntaxNode(fromClauseNode);
    }

    public T transform(WhereClauseNode whereClauseNode) {
        return transformSyntaxNode(whereClauseNode);
    }

    public T transform(LetClauseNode letClauseNode) {
        return transformSyntaxNode(letClauseNode);
    }

    public T transform(JoinClauseNode joinClauseNode) {
        return transformSyntaxNode(joinClauseNode);
    }

    public T transform(OnClauseNode onClauseNode) {
        return transformSyntaxNode(onClauseNode);
    }

    public T transform(LimitClauseNode limitClauseNode) {
        return transformSyntaxNode(limitClauseNode);
    }

    public T transform(OnConflictClauseNode onConflictClauseNode) {
        return transformSyntaxNode(onConflictClauseNode);
    }

    public T transform(QueryPipelineNode queryPipelineNode) {
        return transformSyntaxNode(queryPipelineNode);
    }

    public T transform(SelectClauseNode selectClauseNode) {
        return transformSyntaxNode(selectClauseNode);
    }

    public T transform(QueryExpressionNode queryExpressionNode) {
        return transformSyntaxNode(queryExpressionNode);
    }

    public T transform(QueryActionNode queryActionNode) {
        return transformSyntaxNode(queryActionNode);
    }

    public T transform(IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        return transformSyntaxNode(intersectionTypeDescriptorNode);
    }

    public T transform(ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        return transformSyntaxNode(implicitAnonymousFunctionParameters);
    }

    public T transform(ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        return transformSyntaxNode(implicitAnonymousFunctionExpressionNode);
    }

    public T transform(StartActionNode startActionNode) {
        return transformSyntaxNode(startActionNode);
    }

    public T transform(FlushActionNode flushActionNode) {
        return transformSyntaxNode(flushActionNode);
    }

    public T transform(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        return transformSyntaxNode(singletonTypeDescriptorNode);
    }

    public T transform(MethodDeclarationNode methodDeclarationNode) {
        return transformSyntaxNode(methodDeclarationNode);
    }

    public T transform(TypedBindingPatternNode typedBindingPatternNode) {
        return transformSyntaxNode(typedBindingPatternNode);
    }

    public T transform(CaptureBindingPatternNode captureBindingPatternNode) {
        return transformSyntaxNode(captureBindingPatternNode);
    }

    public T transform(WildcardBindingPatternNode wildcardBindingPatternNode) {
        return transformSyntaxNode(wildcardBindingPatternNode);
    }

    public T transform(ListBindingPatternNode listBindingPatternNode) {
        return transformSyntaxNode(listBindingPatternNode);
    }

    public T transform(MappingBindingPatternNode mappingBindingPatternNode) {
        return transformSyntaxNode(mappingBindingPatternNode);
    }

    public T transform(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        return transformSyntaxNode(fieldBindingPatternFullNode);
    }

    public T transform(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        return transformSyntaxNode(fieldBindingPatternVarnameNode);
    }

    public T transform(RestBindingPatternNode restBindingPatternNode) {
        return transformSyntaxNode(restBindingPatternNode);
    }

    public T transform(ErrorBindingPatternNode errorBindingPatternNode) {
        return transformSyntaxNode(errorBindingPatternNode);
    }

    public T transform(NamedArgBindingPatternNode namedArgBindingPatternNode) {
        return transformSyntaxNode(namedArgBindingPatternNode);
    }

    public T transform(AsyncSendActionNode asyncSendActionNode) {
        return transformSyntaxNode(asyncSendActionNode);
    }

    public T transform(SyncSendActionNode syncSendActionNode) {
        return transformSyntaxNode(syncSendActionNode);
    }

    public T transform(ReceiveActionNode receiveActionNode) {
        return transformSyntaxNode(receiveActionNode);
    }

    public T transform(ReceiveFieldsNode receiveFieldsNode) {
        return transformSyntaxNode(receiveFieldsNode);
    }

    public T transform(RestDescriptorNode restDescriptorNode) {
        return transformSyntaxNode(restDescriptorNode);
    }

    public T transform(DoubleGTTokenNode doubleGTTokenNode) {
        return transformSyntaxNode(doubleGTTokenNode);
    }

    public T transform(TrippleGTTokenNode trippleGTTokenNode) {
        return transformSyntaxNode(trippleGTTokenNode);
    }

    public T transform(WaitActionNode waitActionNode) {
        return transformSyntaxNode(waitActionNode);
    }

    public T transform(WaitFieldsListNode waitFieldsListNode) {
        return transformSyntaxNode(waitFieldsListNode);
    }

    public T transform(WaitFieldNode waitFieldNode) {
        return transformSyntaxNode(waitFieldNode);
    }

    public T transform(AnnotAccessExpressionNode annotAccessExpressionNode) {
        return transformSyntaxNode(annotAccessExpressionNode);
    }

    public T transform(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        return transformSyntaxNode(optionalFieldAccessExpressionNode);
    }

    public T transform(ConditionalExpressionNode conditionalExpressionNode) {
        return transformSyntaxNode(conditionalExpressionNode);
    }

    public T transform(EnumDeclarationNode enumDeclarationNode) {
        return transformSyntaxNode(enumDeclarationNode);
    }

    public T transform(EnumMemberNode enumMemberNode) {
        return transformSyntaxNode(enumMemberNode);
    }

    public T transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        return transformSyntaxNode(arrayTypeDescriptorNode);
    }

    public T transform(ArrayDimensionNode arrayDimensionNode) {
        return transformSyntaxNode(arrayDimensionNode);
    }

    public T transform(TransactionStatementNode transactionStatementNode) {
        return transformSyntaxNode(transactionStatementNode);
    }

    public T transform(RollbackStatementNode rollbackStatementNode) {
        return transformSyntaxNode(rollbackStatementNode);
    }

    public T transform(RetryStatementNode retryStatementNode) {
        return transformSyntaxNode(retryStatementNode);
    }

    public T transform(CommitActionNode commitActionNode) {
        return transformSyntaxNode(commitActionNode);
    }

    public T transform(TransactionalExpressionNode transactionalExpressionNode) {
        return transformSyntaxNode(transactionalExpressionNode);
    }

    public T transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        return transformSyntaxNode(byteArrayLiteralNode);
    }

    public T transform(XMLFilterExpressionNode xMLFilterExpressionNode) {
        return transformSyntaxNode(xMLFilterExpressionNode);
    }

    public T transform(XMLStepExpressionNode xMLStepExpressionNode) {
        return transformSyntaxNode(xMLStepExpressionNode);
    }

    public T transform(XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        return transformSyntaxNode(xMLNamePatternChainingNode);
    }

    public T transform(XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        return transformSyntaxNode(xMLAtomicNamePatternNode);
    }

    public T transform(TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        return transformSyntaxNode(typeReferenceTypeDescNode);
    }

    public T transform(MatchStatementNode matchStatementNode) {
        return transformSyntaxNode(matchStatementNode);
    }

    public T transform(MatchClauseNode matchClauseNode) {
        return transformSyntaxNode(matchClauseNode);
    }

    public T transform(MatchGuardNode matchGuardNode) {
        return transformSyntaxNode(matchGuardNode);
    }

    public T transform(DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        return transformSyntaxNode(distinctTypeDescriptorNode);
    }

    public T transform(ListMatchPatternNode listMatchPatternNode) {
        return transformSyntaxNode(listMatchPatternNode);
    }

    public T transform(RestMatchPatternNode restMatchPatternNode) {
        return transformSyntaxNode(restMatchPatternNode);
    }

    public T transform(MappingMatchPatternNode mappingMatchPatternNode) {
        return transformSyntaxNode(mappingMatchPatternNode);
    }

    public T transform(FieldMatchPatternNode fieldMatchPatternNode) {
        return transformSyntaxNode(fieldMatchPatternNode);
    }

    public T transform(ErrorMatchPatternNode errorMatchPatternNode) {
        return transformSyntaxNode(errorMatchPatternNode);
    }

    public T transform(NamedArgMatchPatternNode namedArgMatchPatternNode) {
        return transformSyntaxNode(namedArgMatchPatternNode);
    }

    public T transform(MarkdownDocumentationNode markdownDocumentationNode) {
        return transformSyntaxNode(markdownDocumentationNode);
    }

    public T transform(MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        return transformSyntaxNode(markdownDocumentationLineNode);
    }

    public T transform(MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        return transformSyntaxNode(markdownParameterDocumentationLineNode);
    }

    public T transform(BallerinaNameReferenceNode ballerinaNameReferenceNode) {
        return transformSyntaxNode(ballerinaNameReferenceNode);
    }

    public T transform(InlineCodeReferenceNode inlineCodeReferenceNode) {
        return transformSyntaxNode(inlineCodeReferenceNode);
    }

    public T transform(MarkdownCodeBlockNode markdownCodeBlockNode) {
        return transformSyntaxNode(markdownCodeBlockNode);
    }

    public T transform(MarkdownCodeLineNode markdownCodeLineNode) {
        return transformSyntaxNode(markdownCodeLineNode);
    }

    public T transform(OrderByClauseNode orderByClauseNode) {
        return transformSyntaxNode(orderByClauseNode);
    }

    public T transform(OrderKeyNode orderKeyNode) {
        return transformSyntaxNode(orderKeyNode);
    }

    public T transform(OnFailClauseNode onFailClauseNode) {
        return transformSyntaxNode(onFailClauseNode);
    }

    public T transform(DoStatementNode doStatementNode) {
        return transformSyntaxNode(doStatementNode);
    }

    public T transform(ClassDefinitionNode classDefinitionNode) {
        return transformSyntaxNode(classDefinitionNode);
    }

    public T transform(ResourcePathParameterNode resourcePathParameterNode) {
        return transformSyntaxNode(resourcePathParameterNode);
    }

    public T transform(RequiredExpressionNode requiredExpressionNode) {
        return transformSyntaxNode(requiredExpressionNode);
    }

    public T transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        return transformSyntaxNode(errorConstructorExpressionNode);
    }

    public T transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        return transformSyntaxNode(parameterizedTypeDescriptorNode);
    }

    public T transform(SpreadMemberNode spreadMemberNode) {
        return transformSyntaxNode(spreadMemberNode);
    }

    public T transform(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        return transformSyntaxNode(clientResourceAccessActionNode);
    }

    public T transform(ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        return transformSyntaxNode(computedResourceAccessSegmentNode);
    }

    public T transform(ResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        return transformSyntaxNode(resourceAccessRestSegmentNode);
    }

    public T transform(ReSequenceNode reSequenceNode) {
        return transformSyntaxNode(reSequenceNode);
    }

    public T transform(ReAtomQuantifierNode reAtomQuantifierNode) {
        return transformSyntaxNode(reAtomQuantifierNode);
    }

    public T transform(ReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        return transformSyntaxNode(reAtomCharOrEscapeNode);
    }

    public T transform(ReQuoteEscapeNode reQuoteEscapeNode) {
        return transformSyntaxNode(reQuoteEscapeNode);
    }

    public T transform(ReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        return transformSyntaxNode(reSimpleCharClassEscapeNode);
    }

    public T transform(ReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        return transformSyntaxNode(reUnicodePropertyEscapeNode);
    }

    public T transform(ReUnicodeScriptNode reUnicodeScriptNode) {
        return transformSyntaxNode(reUnicodeScriptNode);
    }

    public T transform(ReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        return transformSyntaxNode(reUnicodeGeneralCategoryNode);
    }

    public T transform(ReCharacterClassNode reCharacterClassNode) {
        return transformSyntaxNode(reCharacterClassNode);
    }

    public T transform(ReCharSetRangeWithReCharSetNode reCharSetRangeWithReCharSetNode) {
        return transformSyntaxNode(reCharSetRangeWithReCharSetNode);
    }

    public T transform(ReCharSetRangeNode reCharSetRangeNode) {
        return transformSyntaxNode(reCharSetRangeNode);
    }

    public T transform(ReCharSetAtomWithReCharSetNoDashNode reCharSetAtomWithReCharSetNoDashNode) {
        return transformSyntaxNode(reCharSetAtomWithReCharSetNoDashNode);
    }

    public T transform(ReCharSetRangeNoDashWithReCharSetNode reCharSetRangeNoDashWithReCharSetNode) {
        return transformSyntaxNode(reCharSetRangeNoDashWithReCharSetNode);
    }

    public T transform(ReCharSetRangeNoDashNode reCharSetRangeNoDashNode) {
        return transformSyntaxNode(reCharSetRangeNoDashNode);
    }

    public T transform(ReCharSetAtomNoDashWithReCharSetNoDashNode reCharSetAtomNoDashWithReCharSetNoDashNode) {
        return transformSyntaxNode(reCharSetAtomNoDashWithReCharSetNoDashNode);
    }

    public T transform(ReCapturingGroupsNode reCapturingGroupsNode) {
        return transformSyntaxNode(reCapturingGroupsNode);
    }

    public T transform(ReFlagExpressionNode reFlagExpressionNode) {
        return transformSyntaxNode(reFlagExpressionNode);
    }

    public T transform(ReFlagsOnOffNode reFlagsOnOffNode) {
        return transformSyntaxNode(reFlagsOnOffNode);
    }

    public T transform(ReFlagsNode reFlagsNode) {
        return transformSyntaxNode(reFlagsNode);
    }

    public T transform(ReAssertionNode reAssertionNode) {
        return transformSyntaxNode(reAssertionNode);
    }

    public T transform(ReQuantifierNode reQuantifierNode) {
        return transformSyntaxNode(reQuantifierNode);
    }

    public T transform(ReBracedQuantifierNode reBracedQuantifierNode) {
        return transformSyntaxNode(reBracedQuantifierNode);
    }
  
    public T transform(MemberTypeDescriptorNode memberTypeDescriptorNode) {
        return transformSyntaxNode(memberTypeDescriptorNode);
    }

    // Tokens

    public T transform(Token token) {
        return null;
    }

    public T transform(IdentifierToken identifier) {
        return transform((Token) identifier);
    }

    // Misc

    /**
     * Transforms the given {@code Node} into an object of type T.
     * <p>
     * This method is invoked by each transform method in this class. You can
     * override it to provide a common transformation for each node.
     *
     * @param node the {@code Node} to be transformed
     * @return the transformed object
     */
    protected abstract T transformSyntaxNode(Node node);
}

