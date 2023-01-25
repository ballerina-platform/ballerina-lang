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
 * The {@code NodeVisitor} visits each node in the syntax tree allowing
 * us to do something at each node.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * {@code NodeVisitor} is a abstract class that itself visits the complete
 * tree. Subclasses have the ability to override only the required visit methods.
 * <p>
 * There exists a visit method for each node in the Ballerina syntax tree.
 * These methods return void. If you are looking for a visitor that has visit
 * methods that returns something, see {@link NodeTransformer}.
 *
 * This is a generated class.
 *
 * @see NodeTransformer
 * @since 2.0.0
 */
public abstract class NodeVisitor {

    public void visit(ModulePartNode modulePartNode) {
        visitSyntaxNode(modulePartNode);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(ImportDeclarationNode importDeclarationNode) {
        visitSyntaxNode(importDeclarationNode);
    }

    public void visit(ListenerDeclarationNode listenerDeclarationNode) {
        visitSyntaxNode(listenerDeclarationNode);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        visitSyntaxNode(serviceDeclarationNode);
    }

    public void visit(AssignmentStatementNode assignmentStatementNode) {
        visitSyntaxNode(assignmentStatementNode);
    }

    public void visit(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        visitSyntaxNode(compoundAssignmentStatementNode);
    }

    public void visit(VariableDeclarationNode variableDeclarationNode) {
        visitSyntaxNode(variableDeclarationNode);
    }

    public void visit(BlockStatementNode blockStatementNode) {
        visitSyntaxNode(blockStatementNode);
    }

    public void visit(BreakStatementNode breakStatementNode) {
        visitSyntaxNode(breakStatementNode);
    }

    public void visit(FailStatementNode failStatementNode) {
        visitSyntaxNode(failStatementNode);
    }

    public void visit(ExpressionStatementNode expressionStatementNode) {
        visitSyntaxNode(expressionStatementNode);
    }

    public void visit(ContinueStatementNode continueStatementNode) {
        visitSyntaxNode(continueStatementNode);
    }

    public void visit(ExternalFunctionBodyNode externalFunctionBodyNode) {
        visitSyntaxNode(externalFunctionBodyNode);
    }

    public void visit(IfElseStatementNode ifElseStatementNode) {
        visitSyntaxNode(ifElseStatementNode);
    }

    public void visit(ElseBlockNode elseBlockNode) {
        visitSyntaxNode(elseBlockNode);
    }

    public void visit(WhileStatementNode whileStatementNode) {
        visitSyntaxNode(whileStatementNode);
    }

    public void visit(PanicStatementNode panicStatementNode) {
        visitSyntaxNode(panicStatementNode);
    }

    public void visit(ReturnStatementNode returnStatementNode) {
        visitSyntaxNode(returnStatementNode);
    }

    public void visit(LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        visitSyntaxNode(localTypeDefinitionStatementNode);
    }

    public void visit(LockStatementNode lockStatementNode) {
        visitSyntaxNode(lockStatementNode);
    }

    public void visit(ForkStatementNode forkStatementNode) {
        visitSyntaxNode(forkStatementNode);
    }

    public void visit(ForEachStatementNode forEachStatementNode) {
        visitSyntaxNode(forEachStatementNode);
    }

    public void visit(BinaryExpressionNode binaryExpressionNode) {
        visitSyntaxNode(binaryExpressionNode);
    }

    public void visit(BracedExpressionNode bracedExpressionNode) {
        visitSyntaxNode(bracedExpressionNode);
    }

    public void visit(CheckExpressionNode checkExpressionNode) {
        visitSyntaxNode(checkExpressionNode);
    }

    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
        visitSyntaxNode(fieldAccessExpressionNode);
    }

    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        visitSyntaxNode(functionCallExpressionNode);
    }

    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        visitSyntaxNode(methodCallExpressionNode);
    }

    public void visit(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        visitSyntaxNode(mappingConstructorExpressionNode);
    }

    public void visit(IndexedExpressionNode indexedExpressionNode) {
        visitSyntaxNode(indexedExpressionNode);
    }

    public void visit(TypeofExpressionNode typeofExpressionNode) {
        visitSyntaxNode(typeofExpressionNode);
    }

    public void visit(UnaryExpressionNode unaryExpressionNode) {
        visitSyntaxNode(unaryExpressionNode);
    }

    public void visit(ComputedNameFieldNode computedNameFieldNode) {
        visitSyntaxNode(computedNameFieldNode);
    }

    public void visit(ConstantDeclarationNode constantDeclarationNode) {
        visitSyntaxNode(constantDeclarationNode);
    }

    public void visit(DefaultableParameterNode defaultableParameterNode) {
        visitSyntaxNode(defaultableParameterNode);
    }

    public void visit(RequiredParameterNode requiredParameterNode) {
        visitSyntaxNode(requiredParameterNode);
    }

    public void visit(IncludedRecordParameterNode includedRecordParameterNode) {
        visitSyntaxNode(includedRecordParameterNode);
    }

    public void visit(RestParameterNode restParameterNode) {
        visitSyntaxNode(restParameterNode);
    }

    public void visit(ImportOrgNameNode importOrgNameNode) {
        visitSyntaxNode(importOrgNameNode);
    }

    public void visit(ImportPrefixNode importPrefixNode) {
        visitSyntaxNode(importPrefixNode);
    }

    public void visit(SpecificFieldNode specificFieldNode) {
        visitSyntaxNode(specificFieldNode);
    }

    public void visit(SpreadFieldNode spreadFieldNode) {
        visitSyntaxNode(spreadFieldNode);
    }

    public void visit(NamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
    }

    public void visit(PositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
    }

    public void visit(RestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
    }

    public void visit(InferredTypedescDefaultNode inferredTypedescDefaultNode) {
        visitSyntaxNode(inferredTypedescDefaultNode);
    }

    public void visit(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    public void visit(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        visitSyntaxNode(objectConstructorExpressionNode);
    }

    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        visitSyntaxNode(recordTypeDescriptorNode);
    }

    public void visit(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        visitSyntaxNode(returnTypeDescriptorNode);
    }

    public void visit(NilTypeDescriptorNode nilTypeDescriptorNode) {
        visitSyntaxNode(nilTypeDescriptorNode);
    }

    public void visit(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        visitSyntaxNode(optionalTypeDescriptorNode);
    }

    public void visit(ObjectFieldNode objectFieldNode) {
        visitSyntaxNode(objectFieldNode);
    }

    public void visit(RecordFieldNode recordFieldNode) {
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        visitSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public void visit(RecordRestDescriptorNode recordRestDescriptorNode) {
        visitSyntaxNode(recordRestDescriptorNode);
    }

    public void visit(TypeReferenceNode typeReferenceNode) {
        visitSyntaxNode(typeReferenceNode);
    }

    public void visit(AnnotationNode annotationNode) {
        visitSyntaxNode(annotationNode);
    }

    public void visit(MetadataNode metadataNode) {
        visitSyntaxNode(metadataNode);
    }

    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        visitSyntaxNode(moduleVariableDeclarationNode);
    }

    public void visit(TypeTestExpressionNode typeTestExpressionNode) {
        visitSyntaxNode(typeTestExpressionNode);
    }

    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        visitSyntaxNode(remoteMethodCallActionNode);
    }

    public void visit(MapTypeDescriptorNode mapTypeDescriptorNode) {
        visitSyntaxNode(mapTypeDescriptorNode);
    }

    public void visit(NilLiteralNode nilLiteralNode) {
        visitSyntaxNode(nilLiteralNode);
    }

    public void visit(AnnotationDeclarationNode annotationDeclarationNode) {
        visitSyntaxNode(annotationDeclarationNode);
    }

    public void visit(AnnotationAttachPointNode annotationAttachPointNode) {
        visitSyntaxNode(annotationAttachPointNode);
    }

    public void visit(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        visitSyntaxNode(xMLNamespaceDeclarationNode);
    }

    public void visit(ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        visitSyntaxNode(moduleXMLNamespaceDeclarationNode);
    }

    public void visit(FunctionBodyBlockNode functionBodyBlockNode) {
        visitSyntaxNode(functionBodyBlockNode);
    }

    public void visit(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        visitSyntaxNode(namedWorkerDeclarationNode);
    }

    public void visit(NamedWorkerDeclarator namedWorkerDeclarator) {
        visitSyntaxNode(namedWorkerDeclarator);
    }

    public void visit(BasicLiteralNode basicLiteralNode) {
        visitSyntaxNode(basicLiteralNode);
    }

    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        visitSyntaxNode(simpleNameReferenceNode);
    }

    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        visitSyntaxNode(qualifiedNameReferenceNode);
    }

    public void visit(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        visitSyntaxNode(builtinSimpleNameReferenceNode);
    }

    public void visit(TrapExpressionNode trapExpressionNode) {
        visitSyntaxNode(trapExpressionNode);
    }

    public void visit(ListConstructorExpressionNode listConstructorExpressionNode) {
        visitSyntaxNode(listConstructorExpressionNode);
    }

    public void visit(TypeCastExpressionNode typeCastExpressionNode) {
        visitSyntaxNode(typeCastExpressionNode);
    }

    public void visit(TypeCastParamNode typeCastParamNode) {
        visitSyntaxNode(typeCastParamNode);
    }

    public void visit(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        visitSyntaxNode(unionTypeDescriptorNode);
    }

    public void visit(TableConstructorExpressionNode tableConstructorExpressionNode) {
        visitSyntaxNode(tableConstructorExpressionNode);
    }

    public void visit(KeySpecifierNode keySpecifierNode) {
        visitSyntaxNode(keySpecifierNode);
    }

    public void visit(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        visitSyntaxNode(streamTypeDescriptorNode);
    }

    public void visit(StreamTypeParamsNode streamTypeParamsNode) {
        visitSyntaxNode(streamTypeParamsNode);
    }

    public void visit(LetExpressionNode letExpressionNode) {
        visitSyntaxNode(letExpressionNode);
    }

    public void visit(LetVariableDeclarationNode letVariableDeclarationNode) {
        visitSyntaxNode(letVariableDeclarationNode);
    }

    public void visit(TemplateExpressionNode templateExpressionNode) {
        visitSyntaxNode(templateExpressionNode);
    }

    public void visit(XMLElementNode xMLElementNode) {
        visitSyntaxNode(xMLElementNode);
    }

    public void visit(XMLStartTagNode xMLStartTagNode) {
        visitSyntaxNode(xMLStartTagNode);
    }

    public void visit(XMLEndTagNode xMLEndTagNode) {
        visitSyntaxNode(xMLEndTagNode);
    }

    public void visit(XMLSimpleNameNode xMLSimpleNameNode) {
        visitSyntaxNode(xMLSimpleNameNode);
    }

    public void visit(XMLQualifiedNameNode xMLQualifiedNameNode) {
        visitSyntaxNode(xMLQualifiedNameNode);
    }

    public void visit(XMLEmptyElementNode xMLEmptyElementNode) {
        visitSyntaxNode(xMLEmptyElementNode);
    }

    public void visit(InterpolationNode interpolationNode) {
        visitSyntaxNode(interpolationNode);
    }

    public void visit(XMLTextNode xMLTextNode) {
        visitSyntaxNode(xMLTextNode);
    }

    public void visit(XMLAttributeNode xMLAttributeNode) {
        visitSyntaxNode(xMLAttributeNode);
    }

    public void visit(XMLAttributeValue xMLAttributeValue) {
        visitSyntaxNode(xMLAttributeValue);
    }

    public void visit(XMLComment xMLComment) {
        visitSyntaxNode(xMLComment);
    }

    public void visit(XMLCDATANode xMLCDATANode) {
        visitSyntaxNode(xMLCDATANode);
    }

    public void visit(XMLProcessingInstruction xMLProcessingInstruction) {
        visitSyntaxNode(xMLProcessingInstruction);
    }

    public void visit(TableTypeDescriptorNode tableTypeDescriptorNode) {
        visitSyntaxNode(tableTypeDescriptorNode);
    }

    public void visit(TypeParameterNode typeParameterNode) {
        visitSyntaxNode(typeParameterNode);
    }

    public void visit(KeyTypeConstraintNode keyTypeConstraintNode) {
        visitSyntaxNode(keyTypeConstraintNode);
    }

    public void visit(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        visitSyntaxNode(functionTypeDescriptorNode);
    }

    public void visit(FunctionSignatureNode functionSignatureNode) {
        visitSyntaxNode(functionSignatureNode);
    }

    public void visit(ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        visitSyntaxNode(explicitAnonymousFunctionExpressionNode);
    }

    public void visit(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        visitSyntaxNode(expressionFunctionBodyNode);
    }

    public void visit(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        visitSyntaxNode(tupleTypeDescriptorNode);
    }

    public void visit(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        visitSyntaxNode(parenthesisedTypeDescriptorNode);
    }

    public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
        visitSyntaxNode(explicitNewExpressionNode);
    }

    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
        visitSyntaxNode(implicitNewExpressionNode);
    }

    public void visit(ParenthesizedArgList parenthesizedArgList) {
        visitSyntaxNode(parenthesizedArgList);
    }

    public void visit(QueryConstructTypeNode queryConstructTypeNode) {
        visitSyntaxNode(queryConstructTypeNode);
    }

    public void visit(FromClauseNode fromClauseNode) {
        visitSyntaxNode(fromClauseNode);
    }

    public void visit(WhereClauseNode whereClauseNode) {
        visitSyntaxNode(whereClauseNode);
    }

    public void visit(LetClauseNode letClauseNode) {
        visitSyntaxNode(letClauseNode);
    }

    public void visit(JoinClauseNode joinClauseNode) {
        visitSyntaxNode(joinClauseNode);
    }

    public void visit(OnClauseNode onClauseNode) {
        visitSyntaxNode(onClauseNode);
    }

    public void visit(LimitClauseNode limitClauseNode) {
        visitSyntaxNode(limitClauseNode);
    }

    public void visit(OnConflictClauseNode onConflictClauseNode) {
        visitSyntaxNode(onConflictClauseNode);
    }

    public void visit(QueryPipelineNode queryPipelineNode) {
        visitSyntaxNode(queryPipelineNode);
    }

    public void visit(SelectClauseNode selectClauseNode) {
        visitSyntaxNode(selectClauseNode);
    }

    public void visit(QueryExpressionNode queryExpressionNode) {
        visitSyntaxNode(queryExpressionNode);
    }

    public void visit(QueryActionNode queryActionNode) {
        visitSyntaxNode(queryActionNode);
    }

    public void visit(IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        visitSyntaxNode(intersectionTypeDescriptorNode);
    }

    public void visit(ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        visitSyntaxNode(implicitAnonymousFunctionParameters);
    }

    public void visit(ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        visitSyntaxNode(implicitAnonymousFunctionExpressionNode);
    }

    public void visit(StartActionNode startActionNode) {
        visitSyntaxNode(startActionNode);
    }

    public void visit(FlushActionNode flushActionNode) {
        visitSyntaxNode(flushActionNode);
    }

    public void visit(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        visitSyntaxNode(singletonTypeDescriptorNode);
    }

    public void visit(MethodDeclarationNode methodDeclarationNode) {
        visitSyntaxNode(methodDeclarationNode);
    }

    public void visit(TypedBindingPatternNode typedBindingPatternNode) {
        visitSyntaxNode(typedBindingPatternNode);
    }

    public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
        visitSyntaxNode(captureBindingPatternNode);
    }

    public void visit(WildcardBindingPatternNode wildcardBindingPatternNode) {
        visitSyntaxNode(wildcardBindingPatternNode);
    }

    public void visit(ListBindingPatternNode listBindingPatternNode) {
        visitSyntaxNode(listBindingPatternNode);
    }

    public void visit(MappingBindingPatternNode mappingBindingPatternNode) {
        visitSyntaxNode(mappingBindingPatternNode);
    }

    public void visit(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        visitSyntaxNode(fieldBindingPatternFullNode);
    }

    public void visit(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        visitSyntaxNode(fieldBindingPatternVarnameNode);
    }

    public void visit(RestBindingPatternNode restBindingPatternNode) {
        visitSyntaxNode(restBindingPatternNode);
    }

    public void visit(ErrorBindingPatternNode errorBindingPatternNode) {
        visitSyntaxNode(errorBindingPatternNode);
    }

    public void visit(NamedArgBindingPatternNode namedArgBindingPatternNode) {
        visitSyntaxNode(namedArgBindingPatternNode);
    }

    public void visit(AsyncSendActionNode asyncSendActionNode) {
        visitSyntaxNode(asyncSendActionNode);
    }

    public void visit(SyncSendActionNode syncSendActionNode) {
        visitSyntaxNode(syncSendActionNode);
    }

    public void visit(ReceiveActionNode receiveActionNode) {
        visitSyntaxNode(receiveActionNode);
    }

    public void visit(ReceiveFieldsNode receiveFieldsNode) {
        visitSyntaxNode(receiveFieldsNode);
    }

    public void visit(RestDescriptorNode restDescriptorNode) {
        visitSyntaxNode(restDescriptorNode);
    }

    public void visit(DoubleGTTokenNode doubleGTTokenNode) {
        visitSyntaxNode(doubleGTTokenNode);
    }

    public void visit(TrippleGTTokenNode trippleGTTokenNode) {
        visitSyntaxNode(trippleGTTokenNode);
    }

    public void visit(WaitActionNode waitActionNode) {
        visitSyntaxNode(waitActionNode);
    }

    public void visit(WaitFieldsListNode waitFieldsListNode) {
        visitSyntaxNode(waitFieldsListNode);
    }

    public void visit(WaitFieldNode waitFieldNode) {
        visitSyntaxNode(waitFieldNode);
    }

    public void visit(AnnotAccessExpressionNode annotAccessExpressionNode) {
        visitSyntaxNode(annotAccessExpressionNode);
    }

    public void visit(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        visitSyntaxNode(optionalFieldAccessExpressionNode);
    }

    public void visit(ConditionalExpressionNode conditionalExpressionNode) {
        visitSyntaxNode(conditionalExpressionNode);
    }

    public void visit(EnumDeclarationNode enumDeclarationNode) {
        visitSyntaxNode(enumDeclarationNode);
    }

    public void visit(EnumMemberNode enumMemberNode) {
        visitSyntaxNode(enumMemberNode);
    }

    public void visit(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        visitSyntaxNode(arrayTypeDescriptorNode);
    }

    public void visit(ArrayDimensionNode arrayDimensionNode) {
        visitSyntaxNode(arrayDimensionNode);
    }

    public void visit(TransactionStatementNode transactionStatementNode) {
        visitSyntaxNode(transactionStatementNode);
    }

    public void visit(RollbackStatementNode rollbackStatementNode) {
        visitSyntaxNode(rollbackStatementNode);
    }

    public void visit(RetryStatementNode retryStatementNode) {
        visitSyntaxNode(retryStatementNode);
    }

    public void visit(CommitActionNode commitActionNode) {
        visitSyntaxNode(commitActionNode);
    }

    public void visit(TransactionalExpressionNode transactionalExpressionNode) {
        visitSyntaxNode(transactionalExpressionNode);
    }

    public void visit(ByteArrayLiteralNode byteArrayLiteralNode) {
        visitSyntaxNode(byteArrayLiteralNode);
    }

    public void visit(XMLFilterExpressionNode xMLFilterExpressionNode) {
        visitSyntaxNode(xMLFilterExpressionNode);
    }

    public void visit(XMLStepExpressionNode xMLStepExpressionNode) {
        visitSyntaxNode(xMLStepExpressionNode);
    }

    public void visit(XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        visitSyntaxNode(xMLNamePatternChainingNode);
    }

    public void visit(XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        visitSyntaxNode(xMLAtomicNamePatternNode);
    }

    public void visit(TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        visitSyntaxNode(typeReferenceTypeDescNode);
    }

    public void visit(MatchStatementNode matchStatementNode) {
        visitSyntaxNode(matchStatementNode);
    }

    public void visit(MatchClauseNode matchClauseNode) {
        visitSyntaxNode(matchClauseNode);
    }

    public void visit(MatchGuardNode matchGuardNode) {
        visitSyntaxNode(matchGuardNode);
    }

    public void visit(DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        visitSyntaxNode(distinctTypeDescriptorNode);
    }

    public void visit(ListMatchPatternNode listMatchPatternNode) {
        visitSyntaxNode(listMatchPatternNode);
    }

    public void visit(RestMatchPatternNode restMatchPatternNode) {
        visitSyntaxNode(restMatchPatternNode);
    }

    public void visit(MappingMatchPatternNode mappingMatchPatternNode) {
        visitSyntaxNode(mappingMatchPatternNode);
    }

    public void visit(FieldMatchPatternNode fieldMatchPatternNode) {
        visitSyntaxNode(fieldMatchPatternNode);
    }

    public void visit(ErrorMatchPatternNode errorMatchPatternNode) {
        visitSyntaxNode(errorMatchPatternNode);
    }

    public void visit(NamedArgMatchPatternNode namedArgMatchPatternNode) {
        visitSyntaxNode(namedArgMatchPatternNode);
    }

    public void visit(MarkdownDocumentationNode markdownDocumentationNode) {
        visitSyntaxNode(markdownDocumentationNode);
    }

    public void visit(MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        visitSyntaxNode(markdownDocumentationLineNode);
    }

    public void visit(MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        visitSyntaxNode(markdownParameterDocumentationLineNode);
    }

    public void visit(BallerinaNameReferenceNode ballerinaNameReferenceNode) {
        visitSyntaxNode(ballerinaNameReferenceNode);
    }

    public void visit(InlineCodeReferenceNode inlineCodeReferenceNode) {
        visitSyntaxNode(inlineCodeReferenceNode);
    }

    public void visit(MarkdownCodeBlockNode markdownCodeBlockNode) {
        visitSyntaxNode(markdownCodeBlockNode);
    }

    public void visit(MarkdownCodeLineNode markdownCodeLineNode) {
        visitSyntaxNode(markdownCodeLineNode);
    }

    public void visit(OrderByClauseNode orderByClauseNode) {
        visitSyntaxNode(orderByClauseNode);
    }

    public void visit(OrderKeyNode orderKeyNode) {
        visitSyntaxNode(orderKeyNode);
    }

    public void visit(OnFailClauseNode onFailClauseNode) {
        visitSyntaxNode(onFailClauseNode);
    }

    public void visit(DoStatementNode doStatementNode) {
        visitSyntaxNode(doStatementNode);
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        visitSyntaxNode(classDefinitionNode);
    }

    public void visit(ResourcePathParameterNode resourcePathParameterNode) {
        visitSyntaxNode(resourcePathParameterNode);
    }

    public void visit(RequiredExpressionNode requiredExpressionNode) {
        visitSyntaxNode(requiredExpressionNode);
    }

    public void visit(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        visitSyntaxNode(errorConstructorExpressionNode);
    }

    public void visit(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        visitSyntaxNode(parameterizedTypeDescriptorNode);
    }

    public void visit(SpreadMemberNode spreadMemberNode) {
        visitSyntaxNode(spreadMemberNode);
    }

    public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        visitSyntaxNode(clientResourceAccessActionNode);
    }

    public void visit(ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        visitSyntaxNode(computedResourceAccessSegmentNode);
    }

    public void visit(ResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        visitSyntaxNode(resourceAccessRestSegmentNode);
    }

    public void visit(ReSequenceNode reSequenceNode) {
        visitSyntaxNode(reSequenceNode);
    }

    public void visit(ReAtomQuantifierNode reAtomQuantifierNode) {
        visitSyntaxNode(reAtomQuantifierNode);
    }

    public void visit(ReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        visitSyntaxNode(reAtomCharOrEscapeNode);
    }

    public void visit(ReQuoteEscapeNode reQuoteEscapeNode) {
        visitSyntaxNode(reQuoteEscapeNode);
    }

    public void visit(ReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        visitSyntaxNode(reSimpleCharClassEscapeNode);
    }

    public void visit(ReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        visitSyntaxNode(reUnicodePropertyEscapeNode);
    }

    public void visit(ReUnicodeScriptNode reUnicodeScriptNode) {
        visitSyntaxNode(reUnicodeScriptNode);
    }

    public void visit(ReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        visitSyntaxNode(reUnicodeGeneralCategoryNode);
    }

    public void visit(ReCharacterClassNode reCharacterClassNode) {
        visitSyntaxNode(reCharacterClassNode);
    }

    public void visit(ReCharSetRangeWithReCharSetNode reCharSetRangeWithReCharSetNode) {
        visitSyntaxNode(reCharSetRangeWithReCharSetNode);
    }

    public void visit(ReCharSetRangeNode reCharSetRangeNode) {
        visitSyntaxNode(reCharSetRangeNode);
    }

    public void visit(ReCharSetAtomWithReCharSetNoDashNode reCharSetAtomWithReCharSetNoDashNode) {
        visitSyntaxNode(reCharSetAtomWithReCharSetNoDashNode);
    }

    public void visit(ReCharSetRangeNoDashWithReCharSetNode reCharSetRangeNoDashWithReCharSetNode) {
        visitSyntaxNode(reCharSetRangeNoDashWithReCharSetNode);
    }

    public void visit(ReCharSetRangeNoDashNode reCharSetRangeNoDashNode) {
        visitSyntaxNode(reCharSetRangeNoDashNode);
    }

    public void visit(ReCharSetAtomNoDashWithReCharSetNoDashNode reCharSetAtomNoDashWithReCharSetNoDashNode) {
        visitSyntaxNode(reCharSetAtomNoDashWithReCharSetNoDashNode);
    }

    public void visit(ReCapturingGroupsNode reCapturingGroupsNode) {
        visitSyntaxNode(reCapturingGroupsNode);
    }

    public void visit(ReFlagExpressionNode reFlagExpressionNode) {
        visitSyntaxNode(reFlagExpressionNode);
    }

    public void visit(ReFlagsOnOffNode reFlagsOnOffNode) {
        visitSyntaxNode(reFlagsOnOffNode);
    }

    public void visit(ReFlagsNode reFlagsNode) {
        visitSyntaxNode(reFlagsNode);
    }

    public void visit(ReAssertionNode reAssertionNode) {
        visitSyntaxNode(reAssertionNode);
    }

    public void visit(ReQuantifierNode reQuantifierNode) {
        visitSyntaxNode(reQuantifierNode);
    }

    public void visit(ReBracedQuantifierNode reBracedQuantifierNode) {
        visitSyntaxNode(reBracedQuantifierNode);
    }

    // Tokens

    public void visit(Token token) {
    }

    // Misc

    protected void visitSyntaxNode(Node node) {
        // TODO Find a better way to check for token
        if (node instanceof Token) {
            node.accept(this);
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }
}

