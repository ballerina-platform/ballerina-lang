/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;


/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 * New tree nodes can only be replaced with nodes of the same kind.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class TreeModifier extends BaseNodeModifier {

    @Override
    public ModulePartNode transform(
            ModulePartNode modulePartNode) {
        return (ModulePartNode) super.transform(modulePartNode);
    }

    @Override
    public FunctionDefinitionNode transform(
            FunctionDefinitionNode functionDefinitionNode) {
        return (FunctionDefinitionNode) super.transform(functionDefinitionNode);
    }

    @Override
    public ImportDeclarationNode transform(
            ImportDeclarationNode importDeclarationNode) {
        return (ImportDeclarationNode) super.transform(importDeclarationNode);
    }

    @Override
    public ListenerDeclarationNode transform(
            ListenerDeclarationNode listenerDeclarationNode) {
        return (ListenerDeclarationNode) super.transform(listenerDeclarationNode);
    }

    @Override
    public TypeDefinitionNode transform(
            TypeDefinitionNode typeDefinitionNode) {
        return (TypeDefinitionNode) super.transform(typeDefinitionNode);
    }

    @Override
    public ServiceDeclarationNode transform(
            ServiceDeclarationNode serviceDeclarationNode) {
        return (ServiceDeclarationNode) super.transform(serviceDeclarationNode);
    }

    @Override
    public AssignmentStatementNode transform(
            AssignmentStatementNode assignmentStatementNode) {
        return (AssignmentStatementNode) super.transform(assignmentStatementNode);
    }

    @Override
    public CompoundAssignmentStatementNode transform(
            CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        return (CompoundAssignmentStatementNode) super.transform(compoundAssignmentStatementNode);
    }

    @Override
    public VariableDeclarationNode transform(
            VariableDeclarationNode variableDeclarationNode) {
        return (VariableDeclarationNode) super.transform(variableDeclarationNode);
    }

    @Override
    public BlockStatementNode transform(
            BlockStatementNode blockStatementNode) {
        return (BlockStatementNode) super.transform(blockStatementNode);
    }

    @Override
    public BreakStatementNode transform(
            BreakStatementNode breakStatementNode) {
        return (BreakStatementNode) super.transform(breakStatementNode);
    }

    @Override
    public FailStatementNode transform(
            FailStatementNode failStatementNode) {
        return (FailStatementNode) super.transform(failStatementNode);
    }

    @Override
    public ExpressionStatementNode transform(
            ExpressionStatementNode expressionStatementNode) {
        return (ExpressionStatementNode) super.transform(expressionStatementNode);
    }

    @Override
    public ContinueStatementNode transform(
            ContinueStatementNode continueStatementNode) {
        return (ContinueStatementNode) super.transform(continueStatementNode);
    }

    @Override
    public ExternalFunctionBodyNode transform(
            ExternalFunctionBodyNode externalFunctionBodyNode) {
        return (ExternalFunctionBodyNode) super.transform(externalFunctionBodyNode);
    }

    @Override
    public IfElseStatementNode transform(
            IfElseStatementNode ifElseStatementNode) {
        return (IfElseStatementNode) super.transform(ifElseStatementNode);
    }

    @Override
    public ElseBlockNode transform(
            ElseBlockNode elseBlockNode) {
        return (ElseBlockNode) super.transform(elseBlockNode);
    }

    @Override
    public WhileStatementNode transform(
            WhileStatementNode whileStatementNode) {
        return (WhileStatementNode) super.transform(whileStatementNode);
    }

    @Override
    public PanicStatementNode transform(
            PanicStatementNode panicStatementNode) {
        return (PanicStatementNode) super.transform(panicStatementNode);
    }

    @Override
    public ReturnStatementNode transform(
            ReturnStatementNode returnStatementNode) {
        return (ReturnStatementNode) super.transform(returnStatementNode);
    }

    @Override
    public LocalTypeDefinitionStatementNode transform(
            LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        return (LocalTypeDefinitionStatementNode) super.transform(localTypeDefinitionStatementNode);
    }

    @Override
    public LockStatementNode transform(
            LockStatementNode lockStatementNode) {
        return (LockStatementNode) super.transform(lockStatementNode);
    }

    @Override
    public ForkStatementNode transform(
            ForkStatementNode forkStatementNode) {
        return (ForkStatementNode) super.transform(forkStatementNode);
    }

    @Override
    public ForEachStatementNode transform(
            ForEachStatementNode forEachStatementNode) {
        return (ForEachStatementNode) super.transform(forEachStatementNode);
    }

    @Override
    public BinaryExpressionNode transform(
            BinaryExpressionNode binaryExpressionNode) {
        return (BinaryExpressionNode) super.transform(binaryExpressionNode);
    }

    @Override
    public BracedExpressionNode transform(
            BracedExpressionNode bracedExpressionNode) {
        return (BracedExpressionNode) super.transform(bracedExpressionNode);
    }

    @Override
    public CheckExpressionNode transform(
            CheckExpressionNode checkExpressionNode) {
        return (CheckExpressionNode) super.transform(checkExpressionNode);
    }

    @Override
    public FieldAccessExpressionNode transform(
            FieldAccessExpressionNode fieldAccessExpressionNode) {
        return (FieldAccessExpressionNode) super.transform(fieldAccessExpressionNode);
    }

    @Override
    public FunctionCallExpressionNode transform(
            FunctionCallExpressionNode functionCallExpressionNode) {
        return (FunctionCallExpressionNode) super.transform(functionCallExpressionNode);
    }

    @Override
    public MethodCallExpressionNode transform(
            MethodCallExpressionNode methodCallExpressionNode) {
        return (MethodCallExpressionNode) super.transform(methodCallExpressionNode);
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return (MappingConstructorExpressionNode) super.transform(mappingConstructorExpressionNode);
    }

    @Override
    public IndexedExpressionNode transform(
            IndexedExpressionNode indexedExpressionNode) {
        return (IndexedExpressionNode) super.transform(indexedExpressionNode);
    }

    @Override
    public TypeofExpressionNode transform(
            TypeofExpressionNode typeofExpressionNode) {
        return (TypeofExpressionNode) super.transform(typeofExpressionNode);
    }

    @Override
    public UnaryExpressionNode transform(
            UnaryExpressionNode unaryExpressionNode) {
        return (UnaryExpressionNode) super.transform(unaryExpressionNode);
    }

    @Override
    public ComputedNameFieldNode transform(
            ComputedNameFieldNode computedNameFieldNode) {
        return (ComputedNameFieldNode) super.transform(computedNameFieldNode);
    }

    @Override
    public ConstantDeclarationNode transform(
            ConstantDeclarationNode constantDeclarationNode) {
        return (ConstantDeclarationNode) super.transform(constantDeclarationNode);
    }

    @Override
    public DefaultableParameterNode transform(
            DefaultableParameterNode defaultableParameterNode) {
        return (DefaultableParameterNode) super.transform(defaultableParameterNode);
    }

    @Override
    public RequiredParameterNode transform(
            RequiredParameterNode requiredParameterNode) {
        return (RequiredParameterNode) super.transform(requiredParameterNode);
    }

    @Override
    public IncludedRecordParameterNode transform(
            IncludedRecordParameterNode includedRecordParameterNode) {
        return (IncludedRecordParameterNode) super.transform(includedRecordParameterNode);
    }

    @Override
    public RestParameterNode transform(
            RestParameterNode restParameterNode) {
        return (RestParameterNode) super.transform(restParameterNode);
    }

    @Override
    public ImportOrgNameNode transform(
            ImportOrgNameNode importOrgNameNode) {
        return (ImportOrgNameNode) super.transform(importOrgNameNode);
    }

    @Override
    public ImportPrefixNode transform(
            ImportPrefixNode importPrefixNode) {
        return (ImportPrefixNode) super.transform(importPrefixNode);
    }

    @Override
    public SpecificFieldNode transform(
            SpecificFieldNode specificFieldNode) {
        return (SpecificFieldNode) super.transform(specificFieldNode);
    }

    @Override
    public SpreadFieldNode transform(
            SpreadFieldNode spreadFieldNode) {
        return (SpreadFieldNode) super.transform(spreadFieldNode);
    }

    @Override
    public NamedArgumentNode transform(
            NamedArgumentNode namedArgumentNode) {
        return (NamedArgumentNode) super.transform(namedArgumentNode);
    }

    @Override
    public PositionalArgumentNode transform(
            PositionalArgumentNode positionalArgumentNode) {
        return (PositionalArgumentNode) super.transform(positionalArgumentNode);
    }

    @Override
    public RestArgumentNode transform(
            RestArgumentNode restArgumentNode) {
        return (RestArgumentNode) super.transform(restArgumentNode);
    }

    @Override
    public InferredTypedescDefaultNode transform(
            InferredTypedescDefaultNode inferredTypedescDefaultNode) {
        return (InferredTypedescDefaultNode) super.transform(inferredTypedescDefaultNode);
    }

    @Override
    public ObjectTypeDescriptorNode transform(
            ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        return (ObjectTypeDescriptorNode) super.transform(objectTypeDescriptorNode);
    }

    @Override
    public ObjectConstructorExpressionNode transform(
            ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        return (ObjectConstructorExpressionNode) super.transform(objectConstructorExpressionNode);
    }

    @Override
    public RecordTypeDescriptorNode transform(
            RecordTypeDescriptorNode recordTypeDescriptorNode) {
        return (RecordTypeDescriptorNode) super.transform(recordTypeDescriptorNode);
    }

    @Override
    public ReturnTypeDescriptorNode transform(
            ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        return (ReturnTypeDescriptorNode) super.transform(returnTypeDescriptorNode);
    }

    @Override
    public NilTypeDescriptorNode transform(
            NilTypeDescriptorNode nilTypeDescriptorNode) {
        return (NilTypeDescriptorNode) super.transform(nilTypeDescriptorNode);
    }

    @Override
    public OptionalTypeDescriptorNode transform(
            OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        return (OptionalTypeDescriptorNode) super.transform(optionalTypeDescriptorNode);
    }

    @Override
    public ObjectFieldNode transform(
            ObjectFieldNode objectFieldNode) {
        return (ObjectFieldNode) super.transform(objectFieldNode);
    }

    @Override
    public RecordFieldNode transform(
            RecordFieldNode recordFieldNode) {
        return (RecordFieldNode) super.transform(recordFieldNode);
    }

    @Override
    public RecordFieldWithDefaultValueNode transform(
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return (RecordFieldWithDefaultValueNode) super.transform(recordFieldWithDefaultValueNode);
    }

    @Override
    public RecordRestDescriptorNode transform(
            RecordRestDescriptorNode recordRestDescriptorNode) {
        return (RecordRestDescriptorNode) super.transform(recordRestDescriptorNode);
    }

    @Override
    public TypeReferenceNode transform(
            TypeReferenceNode typeReferenceNode) {
        return (TypeReferenceNode) super.transform(typeReferenceNode);
    }

    @Override
    public AnnotationNode transform(
            AnnotationNode annotationNode) {
        return (AnnotationNode) super.transform(annotationNode);
    }

    @Override
    public MetadataNode transform(
            MetadataNode metadataNode) {
        return (MetadataNode) super.transform(metadataNode);
    }

    @Override
    public ModuleVariableDeclarationNode transform(
            ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return (ModuleVariableDeclarationNode) super.transform(moduleVariableDeclarationNode);
    }

    @Override
    public TypeTestExpressionNode transform(
            TypeTestExpressionNode typeTestExpressionNode) {
        return (TypeTestExpressionNode) super.transform(typeTestExpressionNode);
    }

    @Override
    public RemoteMethodCallActionNode transform(
            RemoteMethodCallActionNode remoteMethodCallActionNode) {
        return (RemoteMethodCallActionNode) super.transform(remoteMethodCallActionNode);
    }

    @Override
    public MapTypeDescriptorNode transform(
            MapTypeDescriptorNode mapTypeDescriptorNode) {
        return (MapTypeDescriptorNode) super.transform(mapTypeDescriptorNode);
    }

    @Override
    public NilLiteralNode transform(
            NilLiteralNode nilLiteralNode) {
        return (NilLiteralNode) super.transform(nilLiteralNode);
    }

    @Override
    public AnnotationDeclarationNode transform(
            AnnotationDeclarationNode annotationDeclarationNode) {
        return (AnnotationDeclarationNode) super.transform(annotationDeclarationNode);
    }

    @Override
    public AnnotationAttachPointNode transform(
            AnnotationAttachPointNode annotationAttachPointNode) {
        return (AnnotationAttachPointNode) super.transform(annotationAttachPointNode);
    }

    @Override
    public XMLNamespaceDeclarationNode transform(
            XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        return (XMLNamespaceDeclarationNode) super.transform(xMLNamespaceDeclarationNode);
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        return (ModuleXMLNamespaceDeclarationNode) super.transform(moduleXMLNamespaceDeclarationNode);
    }

    @Override
    public FunctionBodyBlockNode transform(
            FunctionBodyBlockNode functionBodyBlockNode) {
        return (FunctionBodyBlockNode) super.transform(functionBodyBlockNode);
    }

    @Override
    public NamedWorkerDeclarationNode transform(
            NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return (NamedWorkerDeclarationNode) super.transform(namedWorkerDeclarationNode);
    }

    @Override
    public NamedWorkerDeclarator transform(
            NamedWorkerDeclarator namedWorkerDeclarator) {
        return (NamedWorkerDeclarator) super.transform(namedWorkerDeclarator);
    }

    @Override
    public BasicLiteralNode transform(
            BasicLiteralNode basicLiteralNode) {
        return (BasicLiteralNode) super.transform(basicLiteralNode);
    }

    @Override
    public SimpleNameReferenceNode transform(
            SimpleNameReferenceNode simpleNameReferenceNode) {
        return (SimpleNameReferenceNode) super.transform(simpleNameReferenceNode);
    }

    @Override
    public QualifiedNameReferenceNode transform(
            QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return (QualifiedNameReferenceNode) super.transform(qualifiedNameReferenceNode);
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        return (BuiltinSimpleNameReferenceNode) super.transform(builtinSimpleNameReferenceNode);
    }

    @Override
    public TrapExpressionNode transform(
            TrapExpressionNode trapExpressionNode) {
        return (TrapExpressionNode) super.transform(trapExpressionNode);
    }

    @Override
    public ListConstructorExpressionNode transform(
            ListConstructorExpressionNode listConstructorExpressionNode) {
        return (ListConstructorExpressionNode) super.transform(listConstructorExpressionNode);
    }

    @Override
    public TypeCastExpressionNode transform(
            TypeCastExpressionNode typeCastExpressionNode) {
        return (TypeCastExpressionNode) super.transform(typeCastExpressionNode);
    }

    @Override
    public TypeCastParamNode transform(
            TypeCastParamNode typeCastParamNode) {
        return (TypeCastParamNode) super.transform(typeCastParamNode);
    }

    @Override
    public UnionTypeDescriptorNode transform(
            UnionTypeDescriptorNode unionTypeDescriptorNode) {
        return (UnionTypeDescriptorNode) super.transform(unionTypeDescriptorNode);
    }

    @Override
    public TableConstructorExpressionNode transform(
            TableConstructorExpressionNode tableConstructorExpressionNode) {
        return (TableConstructorExpressionNode) super.transform(tableConstructorExpressionNode);
    }

    @Override
    public KeySpecifierNode transform(
            KeySpecifierNode keySpecifierNode) {
        return (KeySpecifierNode) super.transform(keySpecifierNode);
    }

    @Override
    public StreamTypeDescriptorNode transform(
            StreamTypeDescriptorNode streamTypeDescriptorNode) {
        return (StreamTypeDescriptorNode) super.transform(streamTypeDescriptorNode);
    }

    @Override
    public StreamTypeParamsNode transform(
            StreamTypeParamsNode streamTypeParamsNode) {
        return (StreamTypeParamsNode) super.transform(streamTypeParamsNode);
    }

    @Override
    public LetExpressionNode transform(
            LetExpressionNode letExpressionNode) {
        return (LetExpressionNode) super.transform(letExpressionNode);
    }

    @Override
    public LetVariableDeclarationNode transform(
            LetVariableDeclarationNode letVariableDeclarationNode) {
        return (LetVariableDeclarationNode) super.transform(letVariableDeclarationNode);
    }

    @Override
    public TemplateExpressionNode transform(
            TemplateExpressionNode templateExpressionNode) {
        return (TemplateExpressionNode) super.transform(templateExpressionNode);
    }

    @Override
    public XMLElementNode transform(
            XMLElementNode xMLElementNode) {
        return (XMLElementNode) super.transform(xMLElementNode);
    }

    @Override
    public XMLStartTagNode transform(
            XMLStartTagNode xMLStartTagNode) {
        return (XMLStartTagNode) super.transform(xMLStartTagNode);
    }

    @Override
    public XMLEndTagNode transform(
            XMLEndTagNode xMLEndTagNode) {
        return (XMLEndTagNode) super.transform(xMLEndTagNode);
    }

    @Override
    public XMLSimpleNameNode transform(
            XMLSimpleNameNode xMLSimpleNameNode) {
        return (XMLSimpleNameNode) super.transform(xMLSimpleNameNode);
    }

    @Override
    public XMLQualifiedNameNode transform(
            XMLQualifiedNameNode xMLQualifiedNameNode) {
        return (XMLQualifiedNameNode) super.transform(xMLQualifiedNameNode);
    }

    @Override
    public XMLEmptyElementNode transform(
            XMLEmptyElementNode xMLEmptyElementNode) {
        return (XMLEmptyElementNode) super.transform(xMLEmptyElementNode);
    }

    @Override
    public InterpolationNode transform(
            InterpolationNode interpolationNode) {
        return (InterpolationNode) super.transform(interpolationNode);
    }

    @Override
    public XMLTextNode transform(
            XMLTextNode xMLTextNode) {
        return (XMLTextNode) super.transform(xMLTextNode);
    }

    @Override
    public XMLAttributeNode transform(
            XMLAttributeNode xMLAttributeNode) {
        return (XMLAttributeNode) super.transform(xMLAttributeNode);
    }

    @Override
    public XMLAttributeValue transform(
            XMLAttributeValue xMLAttributeValue) {
        return (XMLAttributeValue) super.transform(xMLAttributeValue);
    }

    @Override
    public XMLComment transform(
            XMLComment xMLComment) {
        return (XMLComment) super.transform(xMLComment);
    }

    @Override
    public XMLCDATANode transform(
            XMLCDATANode xMLCDATANode) {
        return (XMLCDATANode) super.transform(xMLCDATANode);
    }

    @Override
    public XMLProcessingInstruction transform(
            XMLProcessingInstruction xMLProcessingInstruction) {
        return (XMLProcessingInstruction) super.transform(xMLProcessingInstruction);
    }

    @Override
    public TableTypeDescriptorNode transform(
            TableTypeDescriptorNode tableTypeDescriptorNode) {
        return (TableTypeDescriptorNode) super.transform(tableTypeDescriptorNode);
    }

    @Override
    public TypeParameterNode transform(
            TypeParameterNode typeParameterNode) {
        return (TypeParameterNode) super.transform(typeParameterNode);
    }

    @Override
    public KeyTypeConstraintNode transform(
            KeyTypeConstraintNode keyTypeConstraintNode) {
        return (KeyTypeConstraintNode) super.transform(keyTypeConstraintNode);
    }

    @Override
    public FunctionTypeDescriptorNode transform(
            FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        return (FunctionTypeDescriptorNode) super.transform(functionTypeDescriptorNode);
    }

    @Override
    public FunctionSignatureNode transform(
            FunctionSignatureNode functionSignatureNode) {
        return (FunctionSignatureNode) super.transform(functionSignatureNode);
    }

    @Override
    public ExplicitAnonymousFunctionExpressionNode transform(
            ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        return (ExplicitAnonymousFunctionExpressionNode) super.transform(explicitAnonymousFunctionExpressionNode);
    }

    @Override
    public ExpressionFunctionBodyNode transform(
            ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        return (ExpressionFunctionBodyNode) super.transform(expressionFunctionBodyNode);
    }

    @Override
    public TupleTypeDescriptorNode transform(
            TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        return (TupleTypeDescriptorNode) super.transform(tupleTypeDescriptorNode);
    }

    @Override
    public ParenthesisedTypeDescriptorNode transform(
            ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        return (ParenthesisedTypeDescriptorNode) super.transform(parenthesisedTypeDescriptorNode);
    }

    @Override
    public ExplicitNewExpressionNode transform(
            ExplicitNewExpressionNode explicitNewExpressionNode) {
        return (ExplicitNewExpressionNode) super.transform(explicitNewExpressionNode);
    }

    @Override
    public ImplicitNewExpressionNode transform(
            ImplicitNewExpressionNode implicitNewExpressionNode) {
        return (ImplicitNewExpressionNode) super.transform(implicitNewExpressionNode);
    }

    @Override
    public ParenthesizedArgList transform(
            ParenthesizedArgList parenthesizedArgList) {
        return (ParenthesizedArgList) super.transform(parenthesizedArgList);
    }

    @Override
    public QueryConstructTypeNode transform(
            QueryConstructTypeNode queryConstructTypeNode) {
        return (QueryConstructTypeNode) super.transform(queryConstructTypeNode);
    }

    @Override
    public FromClauseNode transform(
            FromClauseNode fromClauseNode) {
        return (FromClauseNode) super.transform(fromClauseNode);
    }

    @Override
    public WhereClauseNode transform(
            WhereClauseNode whereClauseNode) {
        return (WhereClauseNode) super.transform(whereClauseNode);
    }

    @Override
    public LetClauseNode transform(
            LetClauseNode letClauseNode) {
        return (LetClauseNode) super.transform(letClauseNode);
    }

    @Override
    public JoinClauseNode transform(
            JoinClauseNode joinClauseNode) {
        return (JoinClauseNode) super.transform(joinClauseNode);
    }

    @Override
    public OnClauseNode transform(
            OnClauseNode onClauseNode) {
        return (OnClauseNode) super.transform(onClauseNode);
    }

    @Override
    public LimitClauseNode transform(
            LimitClauseNode limitClauseNode) {
        return (LimitClauseNode) super.transform(limitClauseNode);
    }

    @Override
    public OnConflictClauseNode transform(
            OnConflictClauseNode onConflictClauseNode) {
        return (OnConflictClauseNode) super.transform(onConflictClauseNode);
    }

    @Override
    public QueryPipelineNode transform(
            QueryPipelineNode queryPipelineNode) {
        return (QueryPipelineNode) super.transform(queryPipelineNode);
    }

    @Override
    public SelectClauseNode transform(
            SelectClauseNode selectClauseNode) {
        return (SelectClauseNode) super.transform(selectClauseNode);
    }

    @Override
    public CollectClauseNode transform(
            CollectClauseNode collectClauseNode) {
        return (CollectClauseNode) super.transform(collectClauseNode);
    }

    @Override
    public QueryExpressionNode transform(
            QueryExpressionNode queryExpressionNode) {
        return (QueryExpressionNode) super.transform(queryExpressionNode);
    }

    @Override
    public QueryActionNode transform(
            QueryActionNode queryActionNode) {
        return (QueryActionNode) super.transform(queryActionNode);
    }

    @Override
    public IntersectionTypeDescriptorNode transform(
            IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        return (IntersectionTypeDescriptorNode) super.transform(intersectionTypeDescriptorNode);
    }

    @Override
    public ImplicitAnonymousFunctionParameters transform(
            ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        return (ImplicitAnonymousFunctionParameters) super.transform(implicitAnonymousFunctionParameters);
    }

    @Override
    public ImplicitAnonymousFunctionExpressionNode transform(
            ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        return (ImplicitAnonymousFunctionExpressionNode) super.transform(implicitAnonymousFunctionExpressionNode);
    }

    @Override
    public StartActionNode transform(
            StartActionNode startActionNode) {
        return (StartActionNode) super.transform(startActionNode);
    }

    @Override
    public FlushActionNode transform(
            FlushActionNode flushActionNode) {
        return (FlushActionNode) super.transform(flushActionNode);
    }

    @Override
    public SingletonTypeDescriptorNode transform(
            SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        return (SingletonTypeDescriptorNode) super.transform(singletonTypeDescriptorNode);
    }

    @Override
    public MethodDeclarationNode transform(
            MethodDeclarationNode methodDeclarationNode) {
        return (MethodDeclarationNode) super.transform(methodDeclarationNode);
    }

    @Override
    public TypedBindingPatternNode transform(
            TypedBindingPatternNode typedBindingPatternNode) {
        return (TypedBindingPatternNode) super.transform(typedBindingPatternNode);
    }

    @Override
    public CaptureBindingPatternNode transform(
            CaptureBindingPatternNode captureBindingPatternNode) {
        return (CaptureBindingPatternNode) super.transform(captureBindingPatternNode);
    }

    @Override
    public WildcardBindingPatternNode transform(
            WildcardBindingPatternNode wildcardBindingPatternNode) {
        return (WildcardBindingPatternNode) super.transform(wildcardBindingPatternNode);
    }

    @Override
    public ListBindingPatternNode transform(
            ListBindingPatternNode listBindingPatternNode) {
        return (ListBindingPatternNode) super.transform(listBindingPatternNode);
    }

    @Override
    public MappingBindingPatternNode transform(
            MappingBindingPatternNode mappingBindingPatternNode) {
        return (MappingBindingPatternNode) super.transform(mappingBindingPatternNode);
    }

    @Override
    public FieldBindingPatternFullNode transform(
            FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        return (FieldBindingPatternFullNode) super.transform(fieldBindingPatternFullNode);
    }

    @Override
    public FieldBindingPatternVarnameNode transform(
            FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        return (FieldBindingPatternVarnameNode) super.transform(fieldBindingPatternVarnameNode);
    }

    @Override
    public RestBindingPatternNode transform(
            RestBindingPatternNode restBindingPatternNode) {
        return (RestBindingPatternNode) super.transform(restBindingPatternNode);
    }

    @Override
    public ErrorBindingPatternNode transform(
            ErrorBindingPatternNode errorBindingPatternNode) {
        return (ErrorBindingPatternNode) super.transform(errorBindingPatternNode);
    }

    @Override
    public NamedArgBindingPatternNode transform(
            NamedArgBindingPatternNode namedArgBindingPatternNode) {
        return (NamedArgBindingPatternNode) super.transform(namedArgBindingPatternNode);
    }

    @Override
    public AsyncSendActionNode transform(
            AsyncSendActionNode asyncSendActionNode) {
        return (AsyncSendActionNode) super.transform(asyncSendActionNode);
    }

    @Override
    public SyncSendActionNode transform(
            SyncSendActionNode syncSendActionNode) {
        return (SyncSendActionNode) super.transform(syncSendActionNode);
    }

    @Override
    public ReceiveActionNode transform(
            ReceiveActionNode receiveActionNode) {
        return (ReceiveActionNode) super.transform(receiveActionNode);
    }

    @Override
    public ReceiveFieldsNode transform(
            ReceiveFieldsNode receiveFieldsNode) {
        return (ReceiveFieldsNode) super.transform(receiveFieldsNode);
    }

    @Override
    public AlternateReceiveNode transform(
            AlternateReceiveNode alternateReceiveNode) {
        return (AlternateReceiveNode) super.transform(alternateReceiveNode);
    }

    @Override
    public RestDescriptorNode transform(
            RestDescriptorNode restDescriptorNode) {
        return (RestDescriptorNode) super.transform(restDescriptorNode);
    }

    @Override
    public DoubleGTTokenNode transform(
            DoubleGTTokenNode doubleGTTokenNode) {
        return (DoubleGTTokenNode) super.transform(doubleGTTokenNode);
    }

    @Override
    public TrippleGTTokenNode transform(
            TrippleGTTokenNode trippleGTTokenNode) {
        return (TrippleGTTokenNode) super.transform(trippleGTTokenNode);
    }

    @Override
    public WaitActionNode transform(
            WaitActionNode waitActionNode) {
        return (WaitActionNode) super.transform(waitActionNode);
    }

    @Override
    public WaitFieldsListNode transform(
            WaitFieldsListNode waitFieldsListNode) {
        return (WaitFieldsListNode) super.transform(waitFieldsListNode);
    }

    @Override
    public WaitFieldNode transform(
            WaitFieldNode waitFieldNode) {
        return (WaitFieldNode) super.transform(waitFieldNode);
    }

    @Override
    public AnnotAccessExpressionNode transform(
            AnnotAccessExpressionNode annotAccessExpressionNode) {
        return (AnnotAccessExpressionNode) super.transform(annotAccessExpressionNode);
    }

    @Override
    public OptionalFieldAccessExpressionNode transform(
            OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        return (OptionalFieldAccessExpressionNode) super.transform(optionalFieldAccessExpressionNode);
    }

    @Override
    public ConditionalExpressionNode transform(
            ConditionalExpressionNode conditionalExpressionNode) {
        return (ConditionalExpressionNode) super.transform(conditionalExpressionNode);
    }

    @Override
    public EnumDeclarationNode transform(
            EnumDeclarationNode enumDeclarationNode) {
        return (EnumDeclarationNode) super.transform(enumDeclarationNode);
    }

    @Override
    public EnumMemberNode transform(
            EnumMemberNode enumMemberNode) {
        return (EnumMemberNode) super.transform(enumMemberNode);
    }

    @Override
    public ArrayTypeDescriptorNode transform(
            ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        return (ArrayTypeDescriptorNode) super.transform(arrayTypeDescriptorNode);
    }

    @Override
    public ArrayDimensionNode transform(
            ArrayDimensionNode arrayDimensionNode) {
        return (ArrayDimensionNode) super.transform(arrayDimensionNode);
    }

    @Override
    public TransactionStatementNode transform(
            TransactionStatementNode transactionStatementNode) {
        return (TransactionStatementNode) super.transform(transactionStatementNode);
    }

    @Override
    public RollbackStatementNode transform(
            RollbackStatementNode rollbackStatementNode) {
        return (RollbackStatementNode) super.transform(rollbackStatementNode);
    }

    @Override
    public RetryStatementNode transform(
            RetryStatementNode retryStatementNode) {
        return (RetryStatementNode) super.transform(retryStatementNode);
    }

    @Override
    public CommitActionNode transform(
            CommitActionNode commitActionNode) {
        return (CommitActionNode) super.transform(commitActionNode);
    }

    @Override
    public TransactionalExpressionNode transform(
            TransactionalExpressionNode transactionalExpressionNode) {
        return (TransactionalExpressionNode) super.transform(transactionalExpressionNode);
    }

    @Override
    public ByteArrayLiteralNode transform(
            ByteArrayLiteralNode byteArrayLiteralNode) {
        return (ByteArrayLiteralNode) super.transform(byteArrayLiteralNode);
    }

    @Override
    public XMLFilterExpressionNode transform(
            XMLFilterExpressionNode xMLFilterExpressionNode) {
        return (XMLFilterExpressionNode) super.transform(xMLFilterExpressionNode);
    }

    @Override
    public XMLStepExpressionNode transform(
            XMLStepExpressionNode xMLStepExpressionNode) {
        return (XMLStepExpressionNode) super.transform(xMLStepExpressionNode);
    }

    @Override
    public XMLNamePatternChainingNode transform(
            XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        return (XMLNamePatternChainingNode) super.transform(xMLNamePatternChainingNode);
    }

    @Override
    public XMLStepIndexedExtendNode transform(
            XMLStepIndexedExtendNode xMLStepIndexedExtendNode) {
        return (XMLStepIndexedExtendNode) super.transform(xMLStepIndexedExtendNode);
    }

    @Override
    public XMLStepMethodCallExtendNode transform(
            XMLStepMethodCallExtendNode xMLStepMethodCallExtendNode) {
        return (XMLStepMethodCallExtendNode) super.transform(xMLStepMethodCallExtendNode);
    }

    @Override
    public XMLAtomicNamePatternNode transform(
            XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        return (XMLAtomicNamePatternNode) super.transform(xMLAtomicNamePatternNode);
    }

    @Override
    public TypeReferenceTypeDescNode transform(
            TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        return (TypeReferenceTypeDescNode) super.transform(typeReferenceTypeDescNode);
    }

    @Override
    public MatchStatementNode transform(
            MatchStatementNode matchStatementNode) {
        return (MatchStatementNode) super.transform(matchStatementNode);
    }

    @Override
    public MatchClauseNode transform(
            MatchClauseNode matchClauseNode) {
        return (MatchClauseNode) super.transform(matchClauseNode);
    }

    @Override
    public MatchGuardNode transform(
            MatchGuardNode matchGuardNode) {
        return (MatchGuardNode) super.transform(matchGuardNode);
    }

    @Override
    public DistinctTypeDescriptorNode transform(
            DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        return (DistinctTypeDescriptorNode) super.transform(distinctTypeDescriptorNode);
    }

    @Override
    public ListMatchPatternNode transform(
            ListMatchPatternNode listMatchPatternNode) {
        return (ListMatchPatternNode) super.transform(listMatchPatternNode);
    }

    @Override
    public RestMatchPatternNode transform(
            RestMatchPatternNode restMatchPatternNode) {
        return (RestMatchPatternNode) super.transform(restMatchPatternNode);
    }

    @Override
    public MappingMatchPatternNode transform(
            MappingMatchPatternNode mappingMatchPatternNode) {
        return (MappingMatchPatternNode) super.transform(mappingMatchPatternNode);
    }

    @Override
    public FieldMatchPatternNode transform(
            FieldMatchPatternNode fieldMatchPatternNode) {
        return (FieldMatchPatternNode) super.transform(fieldMatchPatternNode);
    }

    @Override
    public ErrorMatchPatternNode transform(
            ErrorMatchPatternNode errorMatchPatternNode) {
        return (ErrorMatchPatternNode) super.transform(errorMatchPatternNode);
    }

    @Override
    public NamedArgMatchPatternNode transform(
            NamedArgMatchPatternNode namedArgMatchPatternNode) {
        return (NamedArgMatchPatternNode) super.transform(namedArgMatchPatternNode);
    }

    @Override
    public MarkdownDocumentationNode transform(
            MarkdownDocumentationNode markdownDocumentationNode) {
        return (MarkdownDocumentationNode) super.transform(markdownDocumentationNode);
    }

    @Override
    public MarkdownDocumentationLineNode transform(
            MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        return (MarkdownDocumentationLineNode) super.transform(markdownDocumentationLineNode);
    }

    @Override
    public MarkdownParameterDocumentationLineNode transform(
            MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        return (MarkdownParameterDocumentationLineNode) super.transform(markdownParameterDocumentationLineNode);
    }

    @Override
    public BallerinaNameReferenceNode transform(
            BallerinaNameReferenceNode ballerinaNameReferenceNode) {
        return (BallerinaNameReferenceNode) super.transform(ballerinaNameReferenceNode);
    }

    @Override
    public InlineCodeReferenceNode transform(
            InlineCodeReferenceNode inlineCodeReferenceNode) {
        return (InlineCodeReferenceNode) super.transform(inlineCodeReferenceNode);
    }

    @Override
    public MarkdownCodeBlockNode transform(
            MarkdownCodeBlockNode markdownCodeBlockNode) {
        return (MarkdownCodeBlockNode) super.transform(markdownCodeBlockNode);
    }

    @Override
    public MarkdownCodeLineNode transform(
            MarkdownCodeLineNode markdownCodeLineNode) {
        return (MarkdownCodeLineNode) super.transform(markdownCodeLineNode);
    }

    @Override
    public OrderByClauseNode transform(
            OrderByClauseNode orderByClauseNode) {
        return (OrderByClauseNode) super.transform(orderByClauseNode);
    }

    @Override
    public OrderKeyNode transform(
            OrderKeyNode orderKeyNode) {
        return (OrderKeyNode) super.transform(orderKeyNode);
    }

    @Override
    public GroupByClauseNode transform(
            GroupByClauseNode groupByClauseNode) {
        return (GroupByClauseNode) super.transform(groupByClauseNode);
    }

    @Override
    public GroupingKeyVarDeclarationNode transform(
            GroupingKeyVarDeclarationNode groupingKeyVarDeclarationNode) {
        return (GroupingKeyVarDeclarationNode) super.transform(groupingKeyVarDeclarationNode);
    }

    @Override
    public OnFailClauseNode transform(
            OnFailClauseNode onFailClauseNode) {
        return (OnFailClauseNode) super.transform(onFailClauseNode);
    }

    @Override
    public DoStatementNode transform(
            DoStatementNode doStatementNode) {
        return (DoStatementNode) super.transform(doStatementNode);
    }

    @Override
    public ClassDefinitionNode transform(
            ClassDefinitionNode classDefinitionNode) {
        return (ClassDefinitionNode) super.transform(classDefinitionNode);
    }

    @Override
    public ResourcePathParameterNode transform(
            ResourcePathParameterNode resourcePathParameterNode) {
        return (ResourcePathParameterNode) super.transform(resourcePathParameterNode);
    }

    @Override
    public RequiredExpressionNode transform(
            RequiredExpressionNode requiredExpressionNode) {
        return (RequiredExpressionNode) super.transform(requiredExpressionNode);
    }

    @Override
    public ErrorConstructorExpressionNode transform(
            ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        return (ErrorConstructorExpressionNode) super.transform(errorConstructorExpressionNode);
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(
            ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        return (ParameterizedTypeDescriptorNode) super.transform(parameterizedTypeDescriptorNode);
    }

    @Override
    public SpreadMemberNode transform(
            SpreadMemberNode spreadMemberNode) {
        return (SpreadMemberNode) super.transform(spreadMemberNode);
    }

    @Override
    public ClientResourceAccessActionNode transform(
            ClientResourceAccessActionNode clientResourceAccessActionNode) {
        return (ClientResourceAccessActionNode) super.transform(clientResourceAccessActionNode);
    }

    @Override
    public ComputedResourceAccessSegmentNode transform(
            ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        return (ComputedResourceAccessSegmentNode) super.transform(computedResourceAccessSegmentNode);
    }

    @Override
    public ResourceAccessRestSegmentNode transform(
            ResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        return (ResourceAccessRestSegmentNode) super.transform(resourceAccessRestSegmentNode);
    }

    @Override
    public ReSequenceNode transform(
            ReSequenceNode reSequenceNode) {
        return (ReSequenceNode) super.transform(reSequenceNode);
    }

    @Override
    public ReAtomQuantifierNode transform(
            ReAtomQuantifierNode reAtomQuantifierNode) {
        return (ReAtomQuantifierNode) super.transform(reAtomQuantifierNode);
    }

    @Override
    public ReAtomCharOrEscapeNode transform(
            ReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        return (ReAtomCharOrEscapeNode) super.transform(reAtomCharOrEscapeNode);
    }

    @Override
    public ReQuoteEscapeNode transform(
            ReQuoteEscapeNode reQuoteEscapeNode) {
        return (ReQuoteEscapeNode) super.transform(reQuoteEscapeNode);
    }

    @Override
    public ReSimpleCharClassEscapeNode transform(
            ReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        return (ReSimpleCharClassEscapeNode) super.transform(reSimpleCharClassEscapeNode);
    }

    @Override
    public ReUnicodePropertyEscapeNode transform(
            ReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        return (ReUnicodePropertyEscapeNode) super.transform(reUnicodePropertyEscapeNode);
    }

    @Override
    public ReUnicodeScriptNode transform(
            ReUnicodeScriptNode reUnicodeScriptNode) {
        return (ReUnicodeScriptNode) super.transform(reUnicodeScriptNode);
    }

    @Override
    public ReUnicodeGeneralCategoryNode transform(
            ReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        return (ReUnicodeGeneralCategoryNode) super.transform(reUnicodeGeneralCategoryNode);
    }

    @Override
    public ReCharacterClassNode transform(
            ReCharacterClassNode reCharacterClassNode) {
        return (ReCharacterClassNode) super.transform(reCharacterClassNode);
    }

    @Override
    public ReCharSetRangeWithReCharSetNode transform(
            ReCharSetRangeWithReCharSetNode reCharSetRangeWithReCharSetNode) {
        return (ReCharSetRangeWithReCharSetNode) super.transform(reCharSetRangeWithReCharSetNode);
    }

    @Override
    public ReCharSetRangeNode transform(
            ReCharSetRangeNode reCharSetRangeNode) {
        return (ReCharSetRangeNode) super.transform(reCharSetRangeNode);
    }

    @Override
    public ReCharSetAtomWithReCharSetNoDashNode transform(
            ReCharSetAtomWithReCharSetNoDashNode reCharSetAtomWithReCharSetNoDashNode) {
        return (ReCharSetAtomWithReCharSetNoDashNode) super.transform(reCharSetAtomWithReCharSetNoDashNode);
    }

    @Override
    public ReCharSetRangeNoDashWithReCharSetNode transform(
            ReCharSetRangeNoDashWithReCharSetNode reCharSetRangeNoDashWithReCharSetNode) {
        return (ReCharSetRangeNoDashWithReCharSetNode) super.transform(reCharSetRangeNoDashWithReCharSetNode);
    }

    @Override
    public ReCharSetRangeNoDashNode transform(
            ReCharSetRangeNoDashNode reCharSetRangeNoDashNode) {
        return (ReCharSetRangeNoDashNode) super.transform(reCharSetRangeNoDashNode);
    }

    @Override
    public ReCharSetAtomNoDashWithReCharSetNoDashNode transform(
            ReCharSetAtomNoDashWithReCharSetNoDashNode reCharSetAtomNoDashWithReCharSetNoDashNode) {
        return (ReCharSetAtomNoDashWithReCharSetNoDashNode) super.transform(reCharSetAtomNoDashWithReCharSetNoDashNode);
    }

    @Override
    public ReCapturingGroupsNode transform(
            ReCapturingGroupsNode reCapturingGroupsNode) {
        return (ReCapturingGroupsNode) super.transform(reCapturingGroupsNode);
    }

    @Override
    public ReFlagExpressionNode transform(
            ReFlagExpressionNode reFlagExpressionNode) {
        return (ReFlagExpressionNode) super.transform(reFlagExpressionNode);
    }

    @Override
    public ReFlagsOnOffNode transform(
            ReFlagsOnOffNode reFlagsOnOffNode) {
        return (ReFlagsOnOffNode) super.transform(reFlagsOnOffNode);
    }

    @Override
    public ReFlagsNode transform(
            ReFlagsNode reFlagsNode) {
        return (ReFlagsNode) super.transform(reFlagsNode);
    }

    @Override
    public ReAssertionNode transform(
            ReAssertionNode reAssertionNode) {
        return (ReAssertionNode) super.transform(reAssertionNode);
    }

    @Override
    public ReQuantifierNode transform(
            ReQuantifierNode reQuantifierNode) {
        return (ReQuantifierNode) super.transform(reQuantifierNode);
    }

    @Override
    public ReBracedQuantifierNode transform(
            ReBracedQuantifierNode reBracedQuantifierNode) {
        return (ReBracedQuantifierNode) super.transform(reBracedQuantifierNode);
    }

    @Override
    public MemberTypeDescriptorNode transform(
            MemberTypeDescriptorNode memberTypeDescriptorNode) {
        return (MemberTypeDescriptorNode) super.transform(memberTypeDescriptorNode);
    }

    @Override
    public ReceiveFieldNode transform(
            ReceiveFieldNode receiveFieldNode) {
        return (ReceiveFieldNode) super.transform(receiveFieldNode);
    }

    @Override
    public NaturalExpressionNode transform(
            NaturalExpressionNode naturalExpressionNode) {
        return (NaturalExpressionNode) super.transform(naturalExpressionNode);
    }
}
