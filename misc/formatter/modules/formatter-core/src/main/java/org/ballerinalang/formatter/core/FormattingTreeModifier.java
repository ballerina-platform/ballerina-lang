/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.BallerinaNameReferenceNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BreakStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.DoubleGTTokenNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.FieldMatchPatternNode;
import io.ballerina.compiler.syntax.tree.FlushActionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InlineCodeReferenceNode;
import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LocalTypeDefinitionStatementNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.MatchGuardNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgMatchPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.OrderKeyNode;
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryActionNode;
import io.ballerina.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import io.ballerina.compiler.syntax.tree.ReceiveActionNode;
import io.ballerina.compiler.syntax.tree.ReceiveFieldsNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourceAccessRestSegmentNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.RestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadMemberNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyncSendActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TreeModifier;
import io.ballerina.compiler.syntax.tree.TrippleGTTokenNode;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastParamNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceTypeDescNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.compiler.syntax.tree.WildcardBindingPatternNode;
import io.ballerina.compiler.syntax.tree.XMLAtomicNamePatternNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeValue;
import io.ballerina.compiler.syntax.tree.XMLCDATANode;
import io.ballerina.compiler.syntax.tree.XMLComment;
import io.ballerina.compiler.syntax.tree.XMLElementNode;
import io.ballerina.compiler.syntax.tree.XMLEmptyElementNode;
import io.ballerina.compiler.syntax.tree.XMLEndTagNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLItemNode;
import io.ballerina.compiler.syntax.tree.XMLNameNode;
import io.ballerina.compiler.syntax.tree.XMLNamePatternChainingNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerina.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerina.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerina.compiler.syntax.tree.XMLStartTagNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLTextNode;
import io.ballerina.tools.text.LineRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.formatter.core.FormatterUtils.isInlineRange;

/**
 * A formatter implementation that updates the minutiae of a given tree according to the ballerina formatting
 * guidelines.
 *
 * @since 2.0.0
 */
public class FormattingTreeModifier extends TreeModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormattingTreeModifier.class);

    // Formatting configurations of the current session. These configurations
    // are fixed for the given session.
    private final FormattingOptions options;

    // A property bag that holds formatting information associated
    // with the currently processing node.
    private final FormattingEnv env;

    // Range of the file to be formatted.
    private final LineRange lineRange;

    public FormattingTreeModifier(FormattingOptions options, LineRange lineRange) {
        this.options = options;
        this.lineRange = lineRange;
        this.env = new FormattingEnv();
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = formatNodeList(modulePartNode.imports(), 0, 1, 0, 2);
        NodeList<ModuleMemberDeclarationNode> members = formatModuleMembers(modulePartNode.members());
        Token eofToken = formatToken(modulePartNode.eofToken(), 0, 0);
        return modulePartNode.modify(imports, members, eofToken);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        boolean prevPreservedNewLine = env.hasPreservedNewline;
        MetadataNode metadata = formatNode(functionDefinitionNode.metadata().orElse(null), 0, 1);
        // If metadata is documentation string, set preserved new line to false, so to remove user defined new line
        setPreserveNewline(metadata == null ?
                prevPreservedNewLine : metadata.documentationString().isEmpty() && prevPreservedNewLine);
        NodeList<Token> qualifierList = formatNodeList(functionDefinitionNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword = formatToken(functionDefinitionNode.functionKeyword(), 1, 0);
        setPreserveNewline(prevPreservedNewLine);

        IdentifierToken functionName;
        if (functionDefinitionNode.relativeResourcePath().isEmpty()) {
            functionName = formatToken(functionDefinitionNode.functionName(), 0, 0);
        } else {
            functionName = formatToken(functionDefinitionNode.functionName(), 1, 0);
        }
        NodeList<Node> relativeResourcePath = formatNodeList(functionDefinitionNode.relativeResourcePath(), 0, 0, 0, 0);
        FunctionSignatureNode functionSignatureNode = formatNode(functionDefinitionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBodyNode =
                formatNode(functionDefinitionNode.functionBody(), env.trailingWS, env.trailingNL);

        return functionDefinitionNode.modify()
                .withMetadata(metadata)
                .withFunctionKeyword(functionKeyword)
                .withFunctionName(functionName)
                .withRelativeResourcePath(relativeResourcePath)
                .withFunctionSignature(functionSignatureNode).withQualifierList(qualifierList)
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public ResourcePathParameterNode transform(ResourcePathParameterNode resourcePathParameterNode) {
        Token openBracketToken = formatToken(resourcePathParameterNode.openBracketToken(), 0, 0);
        NodeList<AnnotationNode> annotations = formatNodeList(resourcePathParameterNode.annotations(), 1, 0, 1, 0);
        TypeDescriptorNode typeDescriptor;
        if (resourcePathParameterNode.ellipsisToken().isEmpty() && resourcePathParameterNode.paramName().isPresent()) {
            typeDescriptor = formatNode(resourcePathParameterNode.typeDescriptor(), 1, 0);
        } else {
            typeDescriptor = formatNode(resourcePathParameterNode.typeDescriptor(), 0, 0);
        }

        Token ellipsisToken;
        if (resourcePathParameterNode.paramName().isPresent()) {
            ellipsisToken = formatToken(resourcePathParameterNode.ellipsisToken().orElse(null), 1, 0);
        } else {
            ellipsisToken = formatToken(resourcePathParameterNode.ellipsisToken().orElse(null), 0, 0);
        }

        Token paramName = formatToken(resourcePathParameterNode.paramName().orElse(null), 0, 0);
        Token closeBracketToken = formatToken(resourcePathParameterNode.closeBracketToken(), env.trailingWS,
                env.trailingNL);

        return resourcePathParameterNode.modify()
                .withOpenBracketToken(openBracketToken)
                .withAnnotations(annotations)
                .withTypeDescriptor(typeDescriptor)
                .withEllipsisToken(ellipsisToken)
                .withParamName(paramName)
                .withCloseBracketToken(closeBracketToken)
                .apply();
    }

    @Override
    public FunctionSignatureNode transform(FunctionSignatureNode functionSignatureNode) {
        int parenTrailingNL = 0;
        if (hasNonWSMinutiae(functionSignatureNode.openParenToken().trailingMinutiae())) {
            parenTrailingNL++;
        }
        Token openPara = formatToken(functionSignatureNode.openParenToken(), 0, parenTrailingNL);

        // Start a new indentation of two tabs for the parameters.
        indent(2);
        SeparatedNodeList<ParameterNode> parameters =
                formatSeparatedNodeList(functionSignatureNode.parameters(), 0, 0, 0, 0, 0, 0, true);
        unindent(2);

        Token closePara;
        ReturnTypeDescriptorNode returnTypeDesc = null;
        if (functionSignatureNode.returnTypeDesc().isPresent()) {
            closePara = formatToken(functionSignatureNode.closeParenToken(), 1, 0);
            returnTypeDesc =
                    formatNode(functionSignatureNode.returnTypeDesc().get(), env.trailingWS, env.trailingNL);
        } else {
            closePara = formatToken(functionSignatureNode.closeParenToken(), env.trailingWS, env.trailingNL);
        }

        return functionSignatureNode.modify()
                .withOpenParenToken(openPara)
                .withParameters(parameters)
                .withCloseParenToken(closePara)
                .withReturnTypeDesc(returnTypeDesc)
                .apply();
    }

    @Override
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(requiredParameterNode.annotations(), 1, 0, 1, 0);
        Node typeName;
        if (requiredParameterNode.paramName().isPresent()) {
            typeName = formatNode(requiredParameterNode.typeName(), 1, 0);
            Token paramName =
                    formatToken(requiredParameterNode.paramName().orElse(null), env.trailingWS, env.trailingNL);
            return requiredParameterNode.modify()
                    .withAnnotations(annotations)
                    .withTypeName(typeName)
                    .withParamName(paramName)
                    .apply();
        }
        typeName = formatNode(requiredParameterNode.typeName(), env.trailingWS, env.trailingNL);
        return requiredParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .apply();
    }

    @Override
    public IncludedRecordParameterNode transform(IncludedRecordParameterNode includedRecordParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(includedRecordParameterNode.annotations(), 1, 0, 1, 0);
        Node typeName;
        Token asterisk = formatToken(includedRecordParameterNode.asteriskToken(), 0, 0);
        if (includedRecordParameterNode.paramName().isPresent()) {
            typeName = formatNode(includedRecordParameterNode.typeName(), 1, 0);
            Token paramName = formatToken(includedRecordParameterNode.paramName().orElse(null),
                                          env.trailingWS, env.trailingNL);
            return includedRecordParameterNode.modify()
                    .withAsteriskToken(asterisk)
                    .withAnnotations(annotations)
                    .withTypeName(typeName)
                    .withParamName(paramName)
                    .apply();
        }
        typeName = formatNode(includedRecordParameterNode.typeName(), env.trailingWS, env.trailingNL);
        return includedRecordParameterNode.modify()
                .withAsteriskToken(asterisk)
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        Token openBrace = formatToken(functionBodyBlockNode.openBraceToken(), 0, 1);
        indent(); // increase indentation for the statements to follow.
        NodeList<StatementNode> statements = formatNodeList(functionBodyBlockNode.statements(), 0, 1, 0, 1);
        NamedWorkerDeclarator namedWorkerDeclarator =
                formatNode(functionBodyBlockNode.namedWorkerDeclarator().orElse(null), 0, 1);

        unindent(); // reset the indentation
        Optional<Token> optSemicolon = functionBodyBlockNode.semicolonToken();
        Token closeBrace = optSemicolon.isPresent() ?
                formatToken(functionBodyBlockNode.closeBraceToken(), 0, 0) :
                formatToken(functionBodyBlockNode.closeBraceToken(), env.trailingWS, env.trailingNL);
        Token semicolon = formatToken(optSemicolon.orElse(null), env.trailingWS, env.trailingNL);

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(openBrace)
                .withNamedWorkerDeclarator(namedWorkerDeclarator)
                .withStatements(statements)
                .withCloseBraceToken(closeBrace)
                .withSemicolonToken(semicolon)
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
        NodeList<AnnotationNode> annotationNodes = formatNodeList(variableDeclarationNode.annotations(), 0, 1, 0, 1);
        Token finalToken = formatToken(variableDeclarationNode.finalKeyword().orElse(null), 1, 0);
        TypedBindingPatternNode typedBindingPatternNode;
        boolean hasInit = variableDeclarationNode.initializer().isPresent();
        typedBindingPatternNode = formatNode(variableDeclarationNode.typedBindingPattern(), hasInit ? 1 : 0, 0);
        Token equalToken = formatToken(variableDeclarationNode.equalsToken().orElse(null), 1, 0);

        boolean previousInlineAnnotation = env.inlineAnnotation;
        setInlineAnnotation(true);
        ExpressionNode initializer = formatNode(variableDeclarationNode.initializer().orElse(null), 0, 0);
        setInlineAnnotation(previousInlineAnnotation);

        Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);
        return variableDeclarationNode.modify()
                .withAnnotations(annotationNodes)
                .withFinalKeyword(finalToken)
                .withTypedBindingPattern(typedBindingPatternNode)
                .withEqualsToken(equalToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public TypedBindingPatternNode transform(TypedBindingPatternNode typedBindingPatternNode) {
        TypeDescriptorNode typeDescriptorNode = formatNode(typedBindingPatternNode.typeDescriptor(), 1, 0);
        BindingPatternNode bindingPatternNode =
                formatNode(typedBindingPatternNode.bindingPattern(), env.trailingWS, env.trailingNL);
        return typedBindingPatternNode.modify()
                .withTypeDescriptor(typeDescriptorNode)
                .withBindingPattern(bindingPatternNode)
                .apply();
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        Token name = formatToken(builtinSimpleNameReferenceNode.name(), env.trailingWS, env.trailingNL);
        return builtinSimpleNameReferenceNode.modify().withName(name).apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        Token literalToken = formatToken(basicLiteralNode.literalToken(), env.trailingWS, env.trailingNL);
        return basicLiteralNode.modify().withLiteralToken(literalToken).apply();
    }

    @Override
    public CaptureBindingPatternNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        Token variableName = formatToken(captureBindingPatternNode.variableName(), env.trailingWS, env.trailingNL);
        return captureBindingPatternNode.modify().withVariableName(variableName).apply();
    }

    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {
        boolean prevPreservedNewLine = env.hasPreservedNewline;
        Token ifKeyword = formatToken(ifElseStatementNode.ifKeyword(), 1, 0);
        ExpressionNode condition = formatNode(ifElseStatementNode.condition(), 1, 0);
        BlockStatementNode ifBody;
        Node elseBody = null;
        if (ifElseStatementNode.elseBody().isPresent()) {
            ifBody = formatNode(ifElseStatementNode.ifBody(), 1, 0);
            preserveIndentation(!hasTrailingNL(ifElseStatementNode.ifBody().closeBraceToken()));
            elseBody = formatNode(ifElseStatementNode.elseBody().orElse(null), env.trailingWS, env.trailingNL);
            preserveIndentation(prevPreservedNewLine);
        } else {
            ifBody = formatNode(ifElseStatementNode.ifBody(), env.trailingWS, env.trailingNL);
        }

        return ifElseStatementNode.modify()
                .withIfKeyword(ifKeyword)
                .withCondition(condition)
                .withIfBody(ifBody)
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public ElseBlockNode transform(ElseBlockNode elseBlockNode) {
        Token elseKeyword = formatToken(elseBlockNode.elseKeyword(), 1, 0);
        StatementNode elseBody = formatNode(elseBlockNode.elseBody(), env.trailingWS, env.trailingNL);
        return elseBlockNode.modify()
                .withElseKeyword(elseKeyword)
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {
        boolean preserveIndent = env.preserveIndentation;
        preserveIndentation(blockStatementNode.openBraceToken().isMissing() && preserveIndent);
        Token openBrace = formatToken(blockStatementNode.openBraceToken(), 0, 1);
        preserveIndentation(preserveIndent);
        indent(); // start an indentation
        NodeList<StatementNode> statements = formatNodeList(blockStatementNode.statements(), 0, 1, 0, 1);
        unindent(); // end the indentation
        Token closeBrace = formatToken(blockStatementNode.closeBraceToken(), env.trailingWS, env.trailingNL);

        return blockStatementNode.modify()
                .withOpenBraceToken(openBrace)
                .withStatements(statements)
                .withCloseBraceToken(closeBrace)
                .apply();
    }

    @Override
    public RecordTypeDescriptorNode transform(RecordTypeDescriptorNode recordTypeDesc) {
        final int recordKeywordTrailingWS = 1;
        Token recordKeyword = formatNode(recordTypeDesc.recordKeyword(), recordKeywordTrailingWS, 0);
        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(recordTypeDesc)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        Token bodyStartDelimiter = formatToken(recordTypeDesc.bodyStartDelimiter(), 0, fieldTrailingNL);
        indent(); // Set indentation for record fields
        NodeList<Node> fields = formatNodeList(recordTypeDesc.fields(), fieldTrailingWS, fieldTrailingNL,
                recordTypeDesc.recordRestDescriptor().isEmpty() ? 0 : fieldTrailingWS, fieldTrailingNL);
        RecordRestDescriptorNode recordRestDescriptor =
                formatNode(recordTypeDesc.recordRestDescriptor().orElse(null), 0, fieldTrailingNL);
        unindent(); // Revert indentation for record fields
        Token bodyEndDelimiter = formatToken(recordTypeDesc.bodyEndDelimiter(), env.trailingWS, env.trailingNL);

        return recordTypeDesc.modify()
                .withRecordKeyword(recordKeyword)
                .withBodyStartDelimiter(bodyStartDelimiter)
                .withFields(fields)
                .withRecordRestDescriptor(recordRestDescriptor)
                .withBodyEndDelimiter(bodyEndDelimiter)
                .apply();
    }

    @Override
    public RecordFieldNode transform(RecordFieldNode recordField) {
        MetadataNode metadata = formatNode(recordField.metadata().orElse(null), 0, 1);
        Token readonlyKeyword = formatNode(recordField.readonlyKeyword().orElse(null), 1, 0);
        Node typeName = formatNode(recordField.typeName(), 1, 0);
        Token fieldName = formatToken(recordField.fieldName(), 0, 0);
        Token questionMarkToken = formatToken(recordField.questionMarkToken().orElse(null), 0, 0);
        Token semicolonToken = formatToken(recordField.semicolonToken(), env.trailingWS, env.trailingNL);
        return recordField.modify()
                .withMetadata(metadata)
                .withReadonlyKeyword(readonlyKeyword)
                .withTypeName(typeName)
                .withFieldName(fieldName)
                .withQuestionMarkToken(questionMarkToken)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public RecordFieldWithDefaultValueNode transform(RecordFieldWithDefaultValueNode recordField) {
        MetadataNode metadata = formatNode(recordField.metadata().orElse(null), 0, 1);
        Token readonlyKeyword = formatNode(recordField.readonlyKeyword().orElse(null), 1, 0);
        Node typeName = formatNode(recordField.typeName(), 1, 0);
        Token fieldName = formatToken(recordField.fieldName(), 1, 0);
        Token equalsToken = formatToken(recordField.equalsToken(), 1, 0);
        ExpressionNode expression = formatNode(recordField.expression(), 0, 0);
        Token semicolonToken = formatToken(recordField.semicolonToken(), env.trailingWS, env.trailingNL);

        return recordField.modify()
                .withMetadata(metadata)
                .withReadonlyKeyword(readonlyKeyword)
                .withTypeName(typeName)
                .withFieldName(fieldName)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ImportDeclarationNode transform(ImportDeclarationNode importDeclarationNode) {
        Token importKeyword = formatToken(importDeclarationNode.importKeyword(), 1, 0);
        boolean hasPrefix = importDeclarationNode.prefix().isPresent();
        ImportOrgNameNode orgName = formatNode(importDeclarationNode.orgName().orElse(null), 0, 0);
        SeparatedNodeList<IdentifierToken> moduleNames = formatSeparatedNodeList(importDeclarationNode.moduleName(),
                0, 0, 0, 0, hasPrefix ? 1 : 0, 0);
        ImportPrefixNode prefix = formatNode(importDeclarationNode.prefix().orElse(null), 0, 0);
        Token semicolon = formatToken(importDeclarationNode.semicolon(), env.trailingWS, env.trailingNL);

        return importDeclarationNode.modify()
                .withImportKeyword(importKeyword)
                .withOrgName(orgName)
                .withModuleName(moduleNames)
                .withPrefix(prefix)
                .withSemicolon(semicolon)
                .apply();
    }

    @Override
    public ImportOrgNameNode transform(ImportOrgNameNode importOrgNameNode) {
        Token orgName = formatToken(importOrgNameNode.orgName(), 0, 0);
        Token slashToken = formatToken(importOrgNameNode.slashToken(), env.trailingWS, env.trailingNL);

        return importOrgNameNode.modify()
                .withOrgName(orgName)
                .withSlashToken(slashToken)
                .apply();
    }

    @Override
    public ImportPrefixNode transform(ImportPrefixNode importPrefixNode) {
        Token asKeyword = formatToken(importPrefixNode.asKeyword(), 1, 0);
        Token prefix = formatToken(importPrefixNode.prefix(), env.trailingWS, env.trailingNL);

        return importPrefixNode.modify()
                .withAsKeyword(asKeyword)
                .withPrefix(prefix)
                .apply();
    }

    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        MetadataNode metadata = formatNode(serviceDeclarationNode.metadata().orElse(null), 0, 1);
        NodeList<Token> qualifiers = formatNodeList(serviceDeclarationNode.qualifiers(), 1, 0, 1, 0);
        Token serviceKeyword = formatToken(serviceDeclarationNode.serviceKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptor = formatNode(serviceDeclarationNode.typeDescriptor().orElse(null), 1, 0);
        NodeList<Node> absoluteResourcePath = formatNodeList(serviceDeclarationNode.absoluteResourcePath(), 0, 0, 1, 0);
        Token onKeyword = formatToken(serviceDeclarationNode.onKeyword(), 1, 0);
        SeparatedNodeList<ExpressionNode> expressions =
                formatSeparatedNodeList(serviceDeclarationNode.expressions(), 0, 0, 1, 0);
        Token openBrace = formatToken(serviceDeclarationNode.openBraceToken(), 0, 1);
        indent(); // increase the indentation of the following statements.
        NodeList<Node> members = formatNodeList(serviceDeclarationNode.members(), 0, 1, 0, 1);
        unindent(); // reset the indentation.
        Optional<Token> optSemicolon = serviceDeclarationNode.semicolonToken();
        Token closeBrace = optSemicolon.isPresent() ?
                formatToken(serviceDeclarationNode.closeBraceToken(), 0, 0) :
                formatToken(serviceDeclarationNode.closeBraceToken(), env.trailingWS, env.trailingNL);
        Token semicolon = formatToken(optSemicolon.orElse(null), env.trailingWS, env.trailingNL);

        return serviceDeclarationNode.modify()
                .withMetadata(metadata)
                .withQualifiers(qualifiers)
                .withServiceKeyword(serviceKeyword)
                .withTypeDescriptor(typeDescriptor)
                .withAbsoluteResourcePath(absoluteResourcePath)
                .withOnKeyword(onKeyword)
                .withExpressions(expressions)
                .withOpenBraceToken(openBrace)
                .withMembers(members)
                .withCloseBraceToken(closeBrace)
                .withSemicolonToken(semicolon)
                .apply();
    }

    @Override
    public RequiredExpressionNode transform(RequiredExpressionNode requiredExpressionNode) {
        Token questionMarkToken = formatToken(requiredExpressionNode.questionMarkToken(), env.trailingWS,
                env.trailingNL);
        return requiredExpressionNode.modify()
                .withQuestionMarkToken(questionMarkToken)
                .apply();
    }

    @Override
    public ExplicitNewExpressionNode transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        Token newKeywordToken = formatToken(explicitNewExpressionNode.newKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptorNode = formatNode(explicitNewExpressionNode.typeDescriptor(), 0, 0);
        ParenthesizedArgList parenthesizedArgList = formatNode(explicitNewExpressionNode.parenthesizedArgList(),
                env.trailingWS, env.trailingNL);

        return explicitNewExpressionNode.modify()
                .withNewKeyword(newKeywordToken)
                .withTypeDescriptor(typeDescriptorNode)
                .withParenthesizedArgList(parenthesizedArgList)
                .apply();
    }

    @Override
    public ParenthesizedArgList transform(ParenthesizedArgList parenthesizedArgList) {
        Token openParenToken = formatToken(parenthesizedArgList.openParenToken(), 0, 0);
        SeparatedNodeList<FunctionArgumentNode> arguments = formatSeparatedNodeList(parenthesizedArgList
                .arguments(), 0, 0, 0, 0, true);
        Token closeParenToken = formatToken(parenthesizedArgList.closeParenToken(), env.trailingWS, env.trailingNL);

        return parenthesizedArgList.modify()
                .withOpenParenToken(openParenToken)
                .withArguments(arguments)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Token modulePrefix = formatToken(qualifiedNameReferenceNode.modulePrefix(), 0, 0);
        Token colon = formatToken((Token) qualifiedNameReferenceNode.colon(), 0, 0);
        IdentifierToken identifier = formatToken(qualifiedNameReferenceNode.identifier(),
                env.trailingWS, env.trailingNL);

        return qualifiedNameReferenceNode.modify()
                .withModulePrefix(modulePrefix)
                .withColon(colon)
                .withIdentifier(identifier)
                .apply();
    }

    @Override
    public ReturnTypeDescriptorNode transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        Token returnsKeyword = formatToken(returnTypeDescriptorNode.returnsKeyword(), 1, 0);
        NodeList<AnnotationNode> annotations = formatNodeList(returnTypeDescriptorNode.annotations(), 0, 0, 1, 0);
        Node type = formatNode(returnTypeDescriptorNode.type(), env.trailingWS, env.trailingNL);

        return returnTypeDescriptorNode.modify()
                .withReturnsKeyword(returnsKeyword)
                .withAnnotations(annotations)
                .withType(type)
                .apply();
    }

    @Override
    public OptionalTypeDescriptorNode transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        Node typeDescriptor = formatNode(optionalTypeDescriptorNode.typeDescriptor(), 0, 0);
        Token questionMarkToken = formatToken(optionalTypeDescriptorNode.questionMarkToken(),
                env.trailingWS, env.trailingNL);

        return optionalTypeDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withQuestionMarkToken(questionMarkToken)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression = formatNode(expressionStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(expressionStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public CheckExpressionNode transform(CheckExpressionNode checkExpressionNode) {
        Token checkKeyword = formatToken(checkExpressionNode.checkKeyword(), 1, 0);
        ExpressionNode expressionNode = formatNode(checkExpressionNode.expression(), env.trailingWS, env.trailingNL);
        return checkExpressionNode.modify()
                .withCheckKeyword(checkKeyword)
                .withExpression(expressionNode)
                .apply();
    }

    @Override
    public RemoteMethodCallActionNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        ExpressionNode expression = formatNode(remoteMethodCallActionNode.expression(), 0, 0);
        Token rightArrowToken = formatToken(remoteMethodCallActionNode.rightArrowToken(), 0, 0);
        SimpleNameReferenceNode methodName = formatNode(remoteMethodCallActionNode.methodName(), 0, 0);
        Token openParenToken = formatToken(remoteMethodCallActionNode.openParenToken(), 0, 0);
        SeparatedNodeList<FunctionArgumentNode> arguments = formatSeparatedNodeList(remoteMethodCallActionNode
                .arguments(), 0, 0, 0, 0);
        Token closeParenToken = formatToken(remoteMethodCallActionNode.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return remoteMethodCallActionNode.modify()
                .withExpression(expression)
                .withRightArrowToken(rightArrowToken)
                .withMethodName(methodName)
                .withOpenParenToken(openParenToken)
                .withArguments(arguments)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public SimpleNameReferenceNode transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        Token name = formatToken(simpleNameReferenceNode.name(), env.trailingWS, env.trailingNL);
        return simpleNameReferenceNode.modify()
                .withName(name)
                .apply();
    }

    @Override
    public TypeDefinitionNode transform(TypeDefinitionNode typeDefinitionNode) {
        MetadataNode metadata = formatNode(typeDefinitionNode.metadata().orElse(null), 0, 1);
        Token visibilityQualifier = formatToken(typeDefinitionNode.visibilityQualifier().orElse(null), 1, 0);
        Token typeKeyword = formatToken(typeDefinitionNode.typeKeyword(), 1, 0);
        Token typeName = formatToken(typeDefinitionNode.typeName(), 1, 0);
        Node typeDescriptor = formatNode(typeDefinitionNode.typeDescriptor(), 0, 0);
        Token semicolonToken = formatToken(typeDefinitionNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return typeDefinitionNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withTypeKeyword(typeKeyword)
                .withTypeName(typeName)
                .withTypeDescriptor(typeDescriptor)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public SingletonTypeDescriptorNode transform(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        ExpressionNode simpleContExprNode =
                formatNode(singletonTypeDescriptorNode.simpleContExprNode(), env.trailingWS, env.trailingNL);
        return singletonTypeDescriptorNode.modify()
                .withSimpleContExprNode(simpleContExprNode)
                .apply();
    }

    @Override
    public WhileStatementNode transform(WhileStatementNode whileStatementNode) {
        boolean hasOnFailClause = whileStatementNode.onFailClause().isPresent();
        Token whileKeyword = formatToken(whileStatementNode.whileKeyword(), 1, 0);
        ExpressionNode condition = formatNode(whileStatementNode.condition(), 1, 0);
        BlockStatementNode whileBody;

        OnFailClauseNode onFailClause = null;
        if (hasOnFailClause) {
            whileBody = formatNode(whileStatementNode.whileBody(), 1, 0);
            onFailClause = formatNode(whileStatementNode.onFailClause().orElse(null),
                    env.trailingWS, env.trailingNL);
        } else {
            whileBody = formatNode(whileStatementNode.whileBody(), env.trailingWS, env.trailingNL);
        }

        return whileStatementNode.modify()
                .withWhileKeyword(whileKeyword)
                .withCondition(condition)
                .withWhileBody(whileBody)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public BracedExpressionNode transform(BracedExpressionNode bracedExpressionNode) {
        Token openParen = formatToken(bracedExpressionNode.openParen(), 0, 0);
        ExpressionNode expression = formatNode(bracedExpressionNode.expression(), 0, 0);
        Token closeParen = formatToken(bracedExpressionNode.closeParen(), env.trailingWS, env.trailingNL);

        return bracedExpressionNode.modify()
                .withOpenParen(openParen)
                .withExpression(expression)
                .withCloseParen(closeParen)
                .apply();
    }

    @Override
    public AssignmentStatementNode transform(AssignmentStatementNode assignmentStatementNode) {
        Node varRef = formatNode(assignmentStatementNode.varRef(), 1, 0);
        Token equalsToken = formatToken(assignmentStatementNode.equalsToken(), 1, 0);

        boolean previousInlineAnnotation = env.inlineAnnotation;
        setInlineAnnotation(true);

        ExpressionNode expression = formatNode(assignmentStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(assignmentStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        setInlineAnnotation(previousInlineAnnotation);

        return assignmentStatementNode.modify()
                .withVarRef(varRef)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public CompoundAssignmentStatementNode transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        ExpressionNode lhsExpression = formatNode(compoundAssignmentStatementNode.lhsExpression(), 1, 0);
        Token binaryOperator = formatToken(compoundAssignmentStatementNode.binaryOperator(), 0, 0);
        Token equalsToken = formatToken(compoundAssignmentStatementNode.equalsToken(), 1, 0);
        ExpressionNode rhsExpression = formatNode(compoundAssignmentStatementNode.rhsExpression(), 0, 0);
        Token semicolonToken = formatToken(compoundAssignmentStatementNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return compoundAssignmentStatementNode.modify()
                .withLhsExpression(lhsExpression)
                .withBinaryOperator(binaryOperator)
                .withEqualsToken(equalsToken)
                .withRhsExpression(rhsExpression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public DoStatementNode transform(DoStatementNode doStatementNode) {
        boolean hasOnFailClause = doStatementNode.onFailClause().isPresent();
        Token doKeyword = formatToken(doStatementNode.doKeyword(), 1, 0);
        BlockStatementNode blockStatement;

        OnFailClauseNode onFailClause = null;
        if (hasOnFailClause) {
            blockStatement = formatNode(doStatementNode.blockStatement(), 1, 0);
            onFailClause = formatNode(doStatementNode.onFailClause().orElse(null),
                    env.trailingWS, env.trailingNL);
        } else {
            blockStatement = formatNode(doStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return doStatementNode.modify()
                .withDoKeyword(doKeyword)
                .withBlockStatement(blockStatement)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public ForEachStatementNode transform(ForEachStatementNode forEachStatementNode) {
        boolean hasOnFailClause = forEachStatementNode.onFailClause().isPresent();
        Token forEachKeyword = formatToken(forEachStatementNode.forEachKeyword(), 1, 0);
        TypedBindingPatternNode typedBindingPattern = formatNode(forEachStatementNode.typedBindingPattern(), 1, 0);
        Token inKeyword = formatToken(forEachStatementNode.inKeyword(), 1, 0);
        Node actionOrExpressionNode = formatNode(forEachStatementNode.actionOrExpressionNode(), 1, 0);
        BlockStatementNode blockStatement;

        OnFailClauseNode onFailClause = null;
        if (hasOnFailClause) {
            blockStatement = formatNode(forEachStatementNode.blockStatement(), 1, 0);
            onFailClause = formatNode(forEachStatementNode.onFailClause().orElse(null),
                    env.trailingWS, env.trailingNL);
        } else {
            blockStatement = formatNode(forEachStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return forEachStatementNode.modify()
                .withForEachKeyword(forEachKeyword)
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(inKeyword)
                .withActionOrExpressionNode(actionOrExpressionNode)
                .withBlockStatement(blockStatement)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public BinaryExpressionNode transform(BinaryExpressionNode binaryExpressionNode) {
        Node lhsExpr = formatNode(binaryExpressionNode.lhsExpr(), 1, 0);
        Token operator = formatToken(binaryExpressionNode.operator(), 1, 0);
        Node rhsExpr = formatNode(binaryExpressionNode.rhsExpr(), env.trailingWS, env.trailingNL);

        return binaryExpressionNode.modify()
                .withLhsExpr(lhsExpr)
                .withOperator(operator)
                .withRhsExpr(rhsExpr)
                .apply();
    }

    @Override
    public OnFailClauseNode transform(OnFailClauseNode onFailClauseNode) {
        Token onKeyword = formatToken(onFailClauseNode.onKeyword(), 1, 0);
        Token failKeyword = formatToken(onFailClauseNode.failKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptor = formatNode(onFailClauseNode.typeDescriptor().orElse(null), 1, 0);
        IdentifierToken failErrorName = formatToken(onFailClauseNode.failErrorName().orElse(null), 1, 0);
        BlockStatementNode blockStatement = formatNode(onFailClauseNode.blockStatement(),
                env.trailingWS, env.trailingNL);

        return onFailClauseNode.modify()
                .withOnKeyword(onKeyword)
                .withFailKeyword(failKeyword)
                .withTypeDescriptor(typeDescriptor)
                .withFailErrorName(failErrorName)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public ReturnStatementNode transform(ReturnStatementNode returnStatementNode) {
        Token returnKeyword = formatToken(returnStatementNode.returnKeyword(),
                returnStatementNode.expression().isPresent() ? 1 : 0, 0);
        ExpressionNode expressionNode = formatNode(returnStatementNode.expression().orElse(null), 0, 0);
        Token semicolonToken = formatToken(returnStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return returnStatementNode.modify()
                .withReturnKeyword(returnKeyword)
                .withExpression(expressionNode)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        NameReferenceNode functionName = formatNode(functionCallExpressionNode.functionName(), 0, 0);
        Token functionCallOpenPara = formatToken(functionCallExpressionNode.openParenToken(), 0, 0);
        SeparatedNodeList<FunctionArgumentNode> arguments = formatSeparatedNodeList(functionCallExpressionNode
                .arguments(), 0, 0, 0, 0);
        Token functionCallClosePara = formatToken(functionCallExpressionNode.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .withOpenParenToken(functionCallOpenPara)
                .withCloseParenToken(functionCallClosePara)
                .withArguments(arguments)
                .apply();
    }

    @Override
    public ErrorConstructorExpressionNode transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        boolean hasTypeReference = errorConstructorExpressionNode.typeReference().isPresent();
        Token errorKeyword = formatToken(errorConstructorExpressionNode.errorKeyword(), hasTypeReference ? 1 : 0, 0);
        TypeDescriptorNode typeReference = formatNode(errorConstructorExpressionNode.typeReference().orElse(null),
                0, 0);
        Token openParenthesis = formatToken(errorConstructorExpressionNode.openParenToken(), 0, 0);
        SeparatedNodeList<FunctionArgumentNode> arguments = formatSeparatedNodeList(errorConstructorExpressionNode
                .arguments(), 0, 0, 0, 0);
        Token closeParenthesis = formatToken(errorConstructorExpressionNode.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return errorConstructorExpressionNode.modify()
                .withErrorKeyword(errorKeyword)
                .withTypeReference(typeReference)
                .withOpenParenToken(openParenthesis)
                .withArguments(arguments)
                .withCloseParenToken(closeParenthesis)
                .apply();
    }

    @Override
    public UnionTypeDescriptorNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        TypeDescriptorNode leftTypeDesc = formatNode(unionTypeDescriptorNode.leftTypeDesc(), 0, 0);
        Token pipeToken = formatToken(unionTypeDescriptorNode.pipeToken(), 0, 0);
        TypeDescriptorNode rightTypeDesc = formatNode(unionTypeDescriptorNode.rightTypeDesc(),
                env.trailingWS, env.trailingNL);

        return unionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withPipeToken(pipeToken)
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public NilTypeDescriptorNode transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken = formatToken(nilTypeDescriptorNode.openParenToken(), 0, 0);
        Token closeParenToken = formatToken(nilTypeDescriptorNode.closeParenToken(), env.trailingWS, env.trailingNL);

        return nilTypeDescriptorNode.modify()
                .withOpenParenToken(openParenToken)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public ConstantDeclarationNode transform(ConstantDeclarationNode constantDeclarationNode) {
        MetadataNode metadata = formatNode(constantDeclarationNode.metadata().orElse(null), 0, 1);
        Token visibilityQualifier = formatToken(constantDeclarationNode.visibilityQualifier().orElse(null), 1, 0);
        Token constKeyword = formatToken(constantDeclarationNode.constKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptorNode = formatNode(constantDeclarationNode.typeDescriptor().orElse(null), 1, 0);
        Token variableName = formatToken(constantDeclarationNode.variableName(), 1, 0);
        Token equalsToken = formatToken(constantDeclarationNode.equalsToken(), 1, 0);
        Node initializer = formatNode(constantDeclarationNode.initializer(), 0, 0);
        Token semicolonToken = formatToken(constantDeclarationNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return constantDeclarationNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withConstKeyword(constKeyword)
                .withTypeDescriptor(typeDescriptorNode)
                .withVariableName(variableName)
                .withEqualsToken(equalsToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public MapTypeDescriptorNode transform(MapTypeDescriptorNode mapTypeDescriptorNode) {
        Token mapKeywordToken = formatToken(mapTypeDescriptorNode.mapKeywordToken(), 0, 0);
        TypeParameterNode mapTypeParamsNode = formatNode(mapTypeDescriptorNode.mapTypeParamsNode(),
                env.trailingWS, env.trailingNL);
        return mapTypeDescriptorNode.modify()
                .withMapKeywordToken(mapKeywordToken)
                .withMapTypeParamsNode(mapTypeParamsNode)
                .apply();
    }

    @Override
    public TypeParameterNode transform(TypeParameterNode typeParameterNode) {
        Token ltToken = formatToken(typeParameterNode.ltToken(), 0, 0);
        TypeDescriptorNode typeNode = formatNode(typeParameterNode.typeNode(), 0, 0);
        Token gtToken = formatToken(typeParameterNode.gtToken(), env.trailingWS, env.trailingNL);
        return typeParameterNode.modify()
                .withTypeNode(typeNode)
                .withLtToken(ltToken)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public FunctionTypeDescriptorNode transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        NodeList<Token> qualifierList = formatNodeList(functionTypeDescriptorNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword;
        if (functionTypeDescriptorNode.functionSignature().isPresent()) {
            functionKeyword = formatToken(functionTypeDescriptorNode.functionKeyword(), 1, 0);
        } else {
            functionKeyword = formatToken(functionTypeDescriptorNode.functionKeyword(), env.trailingWS, env.trailingNL);
        }
        FunctionSignatureNode functionSignature = formatNode(functionTypeDescriptorNode.functionSignature().
                                                             orElse(null), env.trailingWS, env.trailingNL);
        return functionTypeDescriptorNode.modify()
                .withQualifierList(qualifierList)
                .withFunctionKeyword(functionKeyword)
                .withFunctionSignature(functionSignature)
                .apply();
    }

    @Override
    public ParenthesisedTypeDescriptorNode transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        Token openParenToken = formatToken(parenthesisedTypeDescriptorNode.openParenToken(), 0, 0);
        TypeDescriptorNode typeDesc = formatNode(parenthesisedTypeDescriptorNode.typedesc(), 0, 0);
        Token closeParenToken = formatToken(parenthesisedTypeDescriptorNode.closeParenToken(),
                env.trailingWS, env.trailingNL);
        return parenthesisedTypeDescriptorNode.modify()
                .withOpenParenToken(openParenToken)
                .withTypedesc(typeDesc)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public ExternalFunctionBodyNode transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        Token equalsToken = formatToken(externalFunctionBodyNode.equalsToken(), 1, 0);
        NodeList<AnnotationNode> annotations = formatNodeList(externalFunctionBodyNode.annotations(), 0, 1, 1, 0);
        Token externalKeyword = formatToken(externalFunctionBodyNode.externalKeyword(), 0, 0);
        Token semicolonToken = formatToken(externalFunctionBodyNode.semicolonToken(), env.trailingWS, env.trailingNL);
        return externalFunctionBodyNode.modify()
                .withEqualsToken(equalsToken)
                .withAnnotations(annotations)
                .withExternalKeyword(externalKeyword)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public AnnotationNode transform(AnnotationNode annotationNode) {
        Token atToken = formatToken(annotationNode.atToken(), 0, 0);
        Node annotReference;
        if (annotationNode.annotValue().isPresent()) {
            annotReference = formatNode(annotationNode.annotReference(), 1, 0);
        } else {
            annotReference = formatNode(annotationNode.annotReference(), env.trailingWS, env.trailingNL);
        }

        MappingConstructorExpressionNode annotValue = formatNode(annotationNode.annotValue().orElse(null),
                env.trailingWS, env.trailingNL);
        return annotationNode.modify()
                .withAtToken(atToken)
                .withAnnotReference(annotReference)
                .withAnnotValue(annotValue)
                .apply();
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(mappingConstructorExpressionNode)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        Token openBrace = formatToken(mappingConstructorExpressionNode.openBrace(), 0, fieldTrailingNL);
        indent();
        SeparatedNodeList<MappingFieldNode> fields = formatSeparatedNodeList(
                mappingConstructorExpressionNode.fields(), 0, 0, fieldTrailingWS, fieldTrailingNL, 0, fieldTrailingNL);
        unindent();
        Token closeBrace = formatToken(mappingConstructorExpressionNode.closeBrace(), env.trailingWS, env.trailingNL);

        return mappingConstructorExpressionNode.modify()
                .withOpenBrace(openBrace)
                .withFields(fields)
                .withCloseBrace(closeBrace)
                .apply();
    }

    @Override
    public SpecificFieldNode transform(SpecificFieldNode specificFieldNode) {
        Token readOnlyKeyword = formatToken(specificFieldNode.readonlyKeyword().orElse(null), 1, 0);

        Token fieldName;
        if (specificFieldNode.fieldName() instanceof BasicLiteralNode) {
            fieldName = ((BasicLiteralNode) specificFieldNode.fieldName()).literalToken();
        } else {
            fieldName = (Token) specificFieldNode.fieldName();
        }

        if (specificFieldNode.colon().isPresent()) {
            fieldName = formatToken(fieldName, 0, 0);
        } else {
            fieldName = formatToken(fieldName, env.trailingWS, env.trailingNL);
        }

        Token colon = formatToken(specificFieldNode.colon().orElse(null), 1, 0);
        ExpressionNode expressionNode = formatNode(specificFieldNode.valueExpr().orElse(null),
                env.trailingWS, env.trailingNL);
        return specificFieldNode.modify()
                .withReadonlyKeyword(readOnlyKeyword)
                .withFieldName(fieldName)
                .withColon(colon)
                .withValueExpr(expressionNode)
                .apply();
    }

    @Override
    public ListConstructorExpressionNode transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(listConstructorExpressionNode)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        Token openBracket = formatToken(listConstructorExpressionNode.openBracket(), 0, fieldTrailingNL);
        indent();
        SeparatedNodeList<Node> expressions = formatSeparatedNodeList(listConstructorExpressionNode.expressions(),
                0, 0, fieldTrailingWS, fieldTrailingNL, 0, fieldTrailingNL);
        unindent();
        Token closeBracket = formatToken(listConstructorExpressionNode.closeBracket(),
                env.trailingWS, env.trailingNL);

        return listConstructorExpressionNode.modify()
                .withOpenBracket(openBracket)
                .withExpressions(expressions)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescNode) {
        Token keywordToken;
        if (parameterizedTypeDescNode.typeParamNode().isPresent()) {
            keywordToken = formatToken(parameterizedTypeDescNode.keywordToken(), 0, 0);
        } else {
            keywordToken = formatToken(parameterizedTypeDescNode.keywordToken(), env.trailingWS, env.trailingNL);
        }

        TypeParameterNode typeParamNode = formatNode(parameterizedTypeDescNode.typeParamNode().orElse(null),
                env.trailingWS, env.trailingNL);
        return parameterizedTypeDescNode.modify()
                .withKeywordToken(keywordToken)
                .withTypeParamNode(typeParamNode)
                .apply();
    }

    @Override
    public PanicStatementNode transform(PanicStatementNode panicStatementNode) {
        Token panicKeyword = formatToken(panicStatementNode.panicKeyword(), 1, 0);
        ExpressionNode expression = formatNode(panicStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(panicStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return panicStatementNode.modify()
                .withPanicKeyword(panicKeyword)
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public IntersectionTypeDescriptorNode transform(IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        Node leftTypeDesc = formatNode(intersectionTypeDescriptorNode.leftTypeDesc(), 1, 0);
        Token bitwiseAndToken = formatToken(intersectionTypeDescriptorNode.bitwiseAndToken(), 1, 0);
        Node rightTypeDesc = formatNode(intersectionTypeDescriptorNode.rightTypeDesc(),
                env.trailingWS, env.trailingNL);

        return intersectionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withBitwiseAndToken(bitwiseAndToken)
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public ModuleVariableDeclarationNode transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        MetadataNode metadata = formatNode(moduleVariableDeclarationNode.metadata().orElse(null), 0, 1);
        Token visibilityQual = formatToken(moduleVariableDeclarationNode.visibilityQualifier().orElse(null), 1, 0);
        NodeList<Token> qualifierList = formatNodeList(moduleVariableDeclarationNode.qualifiers(), 1, 0, 1, 0);
        TypedBindingPatternNode typedBindingPatternNode =
                formatNode(moduleVariableDeclarationNode.typedBindingPattern(),
                        moduleVariableDeclarationNode.equalsToken().isPresent() ? 1 : 0, 0);
        Token equalsToken = formatToken(moduleVariableDeclarationNode.equalsToken().orElse(null), 1, 0);

        boolean prevInlineAnnotation = env.inlineAnnotation;
        setInlineAnnotation(true);
        ExpressionNode initializer = formatNode(moduleVariableDeclarationNode.initializer().orElse(null), 0, 0);
        setInlineAnnotation(prevInlineAnnotation);

        Token semicolonToken = formatToken(moduleVariableDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return moduleVariableDeclarationNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQual)
                .withQualifiers(qualifierList)
                .withTypedBindingPattern(typedBindingPatternNode)
                .withEqualsToken(equalsToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ExpressionFunctionBodyNode transform(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        Token rightDoubleArrow = formatToken(expressionFunctionBodyNode.rightDoubleArrow(), 1, 0);
        ExpressionNode expression;
        if (expressionFunctionBodyNode.semicolon().isPresent()) {
            expression = formatNode(expressionFunctionBodyNode.expression(), 0, 0);
        } else {
            expression = formatNode(expressionFunctionBodyNode.expression(),
                    env.trailingWS, env.trailingNL);
        }

        Token semicolon = formatToken(expressionFunctionBodyNode.semicolon().orElse(null),
                env.trailingWS, env.trailingNL);
        return expressionFunctionBodyNode.modify()
                .withRightDoubleArrow(rightDoubleArrow)
                .withExpression(expression)
                .withSemicolon(semicolon)
                .apply();
    }

    @Override
    public TypeCastExpressionNode transform(TypeCastExpressionNode typeCastExpressionNode) {
        Token ltToken = formatToken(typeCastExpressionNode.ltToken(), 0, 0);
        TypeCastParamNode typeCastParam = formatNode(typeCastExpressionNode.typeCastParam(), 0, 0);
        Token gtToken = formatToken(typeCastExpressionNode.gtToken(), 0, 0);
        ExpressionNode expression = formatNode(typeCastExpressionNode.expression(), env.trailingWS, env.trailingNL);

        return typeCastExpressionNode.modify()
                .withLtToken(ltToken)
                .withTypeCastParam(typeCastParam)
                .withGtToken(gtToken)
                .withExpression(expression)
                .apply();
    }

    @Override
    public TypeCastParamNode transform(TypeCastParamNode typeCastParamNode) {
        NodeList<AnnotationNode> annotations;
        Node type = null;

        if (typeCastParamNode.type().isPresent()) {
            annotations = formatNodeList(typeCastParamNode.annotations(), 1, 0, 1, 0);
            type = formatNode(typeCastParamNode.type().get(), env.trailingWS, env.trailingNL);
        } else {
            annotations = formatNodeList(typeCastParamNode.annotations(), 1, 0, env.trailingWS, env.trailingNL);
        }

        return typeCastParamNode.modify()
                .withAnnotations(annotations)
                .withType(type)
                .apply();
    }

    @Override
    public IndexedExpressionNode transform(IndexedExpressionNode indexedExpressionNode) {
        ExpressionNode containerExpression = formatNode(indexedExpressionNode.containerExpression(), 0, 0);
        Token openBracket = formatToken(indexedExpressionNode.openBracket(), 0, 0);
        SeparatedNodeList<ExpressionNode> keyExpression = formatSeparatedNodeList(indexedExpressionNode.keyExpression(),
                0, 0, 0, 0);
        Token closeBracket = formatToken(indexedExpressionNode.closeBracket(), env.trailingWS, env.trailingNL);

        return indexedExpressionNode.modify()
                .withContainerExpression(containerExpression)
                .withOpenBracket(openBracket)
                .withKeyExpression(keyExpression)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public ComputedNameFieldNode transform(ComputedNameFieldNode computedNameFieldNode) {
        Token openBracket = formatToken(computedNameFieldNode.openBracket(), 0, 0);
        ExpressionNode fieldNameExpr = formatNode(computedNameFieldNode.fieldNameExpr(), 0, 0);
        Token closeBracket = formatToken(computedNameFieldNode.closeBracket(), 1, 0);
        Token colonToken = formatToken(computedNameFieldNode.colonToken(), 1, 0);
        ExpressionNode valueExpr = formatNode(computedNameFieldNode.valueExpr(), env.trailingWS, env.trailingNL);

        return computedNameFieldNode.modify()
                .withOpenBracket(openBracket)
                .withFieldNameExpr(fieldNameExpr)
                .withCloseBracket(closeBracket)
                .withColonToken(colonToken)
                .withValueExpr(valueExpr)
                .apply();
    }

    @Override
    public TupleTypeDescriptorNode transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        Token openBracketToken = formatToken(tupleTypeDescriptorNode.openBracketToken(), 0, 0);
        SeparatedNodeList<Node> memberTypeDesc = formatSeparatedNodeList(tupleTypeDescriptorNode.memberTypeDesc(),
                0, 0, 0, 0);
        Token closeBracketToken = formatToken(tupleTypeDescriptorNode.closeBracketToken(),
                env.trailingWS, env.trailingNL);

        return tupleTypeDescriptorNode.modify()
                .withOpenBracketToken(openBracketToken)
                .withMemberTypeDesc(memberTypeDesc)
                .withCloseBracketToken(closeBracketToken)
                .apply();
    }

    @Override
    public ListBindingPatternNode transform(ListBindingPatternNode listBindingPatternNode) {
        Token openBracket = formatToken(listBindingPatternNode.openBracket(), 0, 0);
        SeparatedNodeList<BindingPatternNode> bindingPatternNodes =
                formatSeparatedNodeList(listBindingPatternNode.bindingPatterns(), 0, 0, 0, 0);
        Token closeBracket = formatToken(listBindingPatternNode.closeBracket(), env.trailingWS, env.trailingNL);

        return listBindingPatternNode.modify()
                .withOpenBracket(openBracket)
                .withBindingPatterns(bindingPatternNodes)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public RestBindingPatternNode transform(RestBindingPatternNode restBindingPatternNode) {
        Token ellipsisToken = formatToken(restBindingPatternNode.ellipsisToken(), 0, 0);
        SimpleNameReferenceNode variableName = formatNode(restBindingPatternNode.variableName(),
                env.trailingWS, env.trailingNL);

        return restBindingPatternNode.modify()
                .withEllipsisToken(ellipsisToken)
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public TableTypeDescriptorNode transform(TableTypeDescriptorNode tableTypeDescriptorNode) {
        Token tableKeywordToken = formatToken(tableTypeDescriptorNode.tableKeywordToken(), 0, 0);
        Node rowTypeParameterNode;
        if (tableTypeDescriptorNode.keyConstraintNode().isPresent()) {
            rowTypeParameterNode = formatNode(tableTypeDescriptorNode.rowTypeParameterNode(), 1, 0);
        } else {
            rowTypeParameterNode =
                    formatNode(tableTypeDescriptorNode.rowTypeParameterNode(), env.trailingWS, env.trailingNL);
        }
        Node keyConstraintNode = formatNode(tableTypeDescriptorNode.keyConstraintNode().orElse(null),
                env.trailingWS, env.trailingNL);
        return tableTypeDescriptorNode.modify()
                .withTableKeywordToken(tableKeywordToken)
                .withRowTypeParameterNode(rowTypeParameterNode)
                .withKeyConstraintNode(keyConstraintNode)
                .apply();
    }

    @Override
    public KeyTypeConstraintNode transform(KeyTypeConstraintNode keyTypeConstraintNode) {
        Token keyKeywordToken = formatToken(keyTypeConstraintNode.keyKeywordToken(), 0, 0);
        Node typeParameterNode = formatNode(keyTypeConstraintNode.typeParameterNode(),
                env.trailingWS, env.trailingNL);

        return keyTypeConstraintNode.modify()
                .withKeyKeywordToken(keyKeywordToken)
                .withTypeParameterNode(typeParameterNode)
                .apply();
    }

    @Override
    public MatchStatementNode transform(MatchStatementNode matchStatementNode) {
        boolean hasOnFailClause = matchStatementNode.onFailClause().isPresent();
        Token matchKeyword = formatToken(matchStatementNode.matchKeyword(), 1, 0);
        ExpressionNode condition = formatNode(matchStatementNode.condition(), 1, 0);
        Token openBrace = formatToken(matchStatementNode.openBrace(), 0, 1);
        indent();
        NodeList<MatchClauseNode> matchClauses = formatNodeList(matchStatementNode.matchClauses(), 0, 1, 0, 1);
        unindent();
        Token closeBrace;

        if (hasOnFailClause) {
            closeBrace = formatToken(matchStatementNode.closeBrace(), 1, 0);
        } else {
            closeBrace = formatToken(matchStatementNode.closeBrace(), env.trailingWS, env.trailingNL);
        }

        OnFailClauseNode onFailClause = formatNode(matchStatementNode.onFailClause().orElse(null),
                env.trailingWS, env.trailingNL);
        return matchStatementNode.modify()
                .withMatchKeyword(matchKeyword)
                .withCondition(condition)
                .withOpenBrace(openBrace)
                .withMatchClauses(matchClauses)
                .withCloseBrace(closeBrace)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public MatchClauseNode transform(MatchClauseNode matchClauseNode) {
        SeparatedNodeList<Node> matchPatterns = formatSeparatedNodeList(matchClauseNode.matchPatterns(),
                0, 0, 0, 0, 1, 0);
        MatchGuardNode matchGuard = formatNode(matchClauseNode.matchGuard().orElse(null), 1, 0);
        Token rightDoubleArrow = formatToken(matchClauseNode.rightDoubleArrow(), 1, 0);
        BlockStatementNode blockStatement = formatNode(matchClauseNode.blockStatement(),
                env.trailingWS, env.trailingNL);

        return matchClauseNode.modify()
                .withMatchPatterns(matchPatterns)
                .withMatchGuard(matchGuard)
                .withRightDoubleArrow(rightDoubleArrow)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public MatchGuardNode transform(MatchGuardNode matchGuardNode) {
        Token ifKeyword = formatToken(matchGuardNode.ifKeyword(), 1, 0);
        ExpressionNode expression = formatNode(matchGuardNode.expression(), env.trailingWS, env.trailingNL);
        return matchGuardNode.modify()
                .withIfKeyword(ifKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public LockStatementNode transform(LockStatementNode lockStatementNode) {
        Token lockKeyword = formatToken(lockStatementNode.lockKeyword(), 1, 0);
        BlockStatementNode blockStatement;
        if (lockStatementNode.onFailClause().isPresent()) {
            blockStatement = formatNode(lockStatementNode.blockStatement(), 1, 0);
        } else {
            blockStatement = formatNode(lockStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        OnFailClauseNode onFailClause = formatNode(lockStatementNode.onFailClause().orElse(null),
                env.trailingWS, env.trailingNL);
        return lockStatementNode.modify()
                .withLockKeyword(lockKeyword)
                .withBlockStatement(blockStatement)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public FieldAccessExpressionNode transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        ExpressionNode expression = formatNode(fieldAccessExpressionNode.expression(), 0, 0);
        Token dotToken = formatToken(fieldAccessExpressionNode.dotToken(), 0, 0);
        NameReferenceNode fieldName = formatNode(fieldAccessExpressionNode.fieldName(),
                env.trailingWS, env.trailingNL);

        return fieldAccessExpressionNode.modify()
                .withExpression(expression)
                .withDotToken(dotToken)
                .withFieldName(fieldName)
                .apply();
    }

    @Override
    public MetadataNode transform(MetadataNode metadataNode) {
        if (metadataNode.documentationString().isPresent()) {
            Node documentationString = formatNode(metadataNode.documentationString().orElse(null), 0, 1);
            metadataNode = metadataNode.modify().withDocumentationString(documentationString).apply();
        }

        NodeList<AnnotationNode> annotations = formatNodeList(metadataNode.annotations(),
                0, 1, env.trailingWS, env.trailingNL);
        return metadataNode.modify()
                .withAnnotations(annotations)
                .apply();
    }

    @Override
    public EnumDeclarationNode transform(EnumDeclarationNode enumDeclarationNode) {
        MetadataNode metadata = formatNode(enumDeclarationNode.metadata().orElse(null), 0, 1);
        Token qualifier = formatToken(enumDeclarationNode.qualifier().orElse(null), 1, 0);
        Token enumKeywordToken = formatToken(enumDeclarationNode.enumKeywordToken(), 1, 0);
        IdentifierToken identifier = formatNode(enumDeclarationNode.identifier(), 1, 0);
        Token openBraceToken = formatToken(enumDeclarationNode.openBraceToken(), 0, 1);
        int separatorTrailingWS = 0;
        int separatorTrailingNL = 0;
        if (shouldExpand(enumDeclarationNode)) {
            separatorTrailingNL++;
        } else {
            separatorTrailingWS++;
        }

        indent();
        SeparatedNodeList<Node> enumMemberList = formatSeparatedNodeList(enumDeclarationNode.enumMemberList(),
                0, 0, separatorTrailingWS, separatorTrailingNL, 0, 1);
        unindent();
        Optional<Token> optSemicolon = enumDeclarationNode.semicolonToken();
        Token closeBraceToken = optSemicolon.isPresent() ?
                formatToken(enumDeclarationNode.closeBraceToken(), 0, 0) :
                formatToken(enumDeclarationNode.closeBraceToken(), env.trailingWS, env.trailingNL);
        Token semicolon = formatToken(optSemicolon.orElse(null), env.trailingWS, env.trailingNL);

        return enumDeclarationNode.modify()
                .withMetadata(metadata)
                .withQualifier(qualifier)
                .withEnumKeywordToken(enumKeywordToken)
                .withIdentifier(identifier)
                .withOpenBraceToken(openBraceToken)
                .withEnumMemberList(enumMemberList)
                .withCloseBraceToken(closeBraceToken)
                .withSemicolonToken(semicolon)
                .apply();
    }

    @Override
    public EnumMemberNode transform(EnumMemberNode enumMemberNode) {
        MetadataNode metadata = formatNode(enumMemberNode.metadata().orElse(null), 0, 1);
        IdentifierToken identifier;
        if (enumMemberNode.equalToken().isPresent()) {
            identifier = formatNode(enumMemberNode.identifier(), 1, 0);
        } else {
            identifier = formatNode(enumMemberNode.identifier(), env.trailingWS, env.trailingNL);
        }
        Token equalToken = formatToken(enumMemberNode.equalToken().orElse(null), 1, 0);
        ExpressionNode constExprNode = formatNode(enumMemberNode.constExprNode().orElse(null),
                env.trailingWS, env.trailingNL);

        return enumMemberNode.modify()
                .withMetadata(metadata)
                .withIdentifier(identifier)
                .withEqualToken(equalToken)
                .withConstExprNode(constExprNode)
                .apply();
    }

    @Override
    public MarkdownDocumentationNode transform(MarkdownDocumentationNode markdownDocumentationNode) {
        NodeList<Node> documentationLines = formatNodeList(markdownDocumentationNode.documentationLines(),
                0, 1, env.trailingWS, env.trailingNL);
        return markdownDocumentationNode.modify()
                .withDocumentationLines(documentationLines)
                .apply();
    }

    @Override
    public MarkdownDocumentationLineNode transform(MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        Token hashToken;

        if (markdownDocumentationLineNode.documentElements().isEmpty()) {
            hashToken = formatToken(markdownDocumentationLineNode.hashToken(), env.trailingWS, env.trailingNL);
        } else {
            hashToken = formatToken(markdownDocumentationLineNode.hashToken(), 1, 0);
        }

        NodeList<Node> documentElements = formatNodeList(markdownDocumentationLineNode.documentElements(),
                0, 0, env.trailingWS, env.trailingNL);
        return markdownDocumentationLineNode.modify()
                .withDocumentElements(documentElements)
                .withHashToken(hashToken)
                .apply();
    }

    @Override
    public MarkdownParameterDocumentationLineNode transform(
            MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        Token hashToken = formatToken(markdownParameterDocumentationLineNode.hashToken(), 1, 0);

        Token plusToken = markdownParameterDocumentationLineNode.plusToken();
        Token parameterName = markdownParameterDocumentationLineNode.parameterName();
        Token minusToken = markdownParameterDocumentationLineNode.minusToken();
        NodeList<Node> documentElements = markdownParameterDocumentationLineNode.documentElements();

        if (parameterName.isMissing() && minusToken.isMissing() && documentElements.isEmpty()) {
            // handle a scenario when the plus token is the last token in the documentation line
            plusToken = formatToken(plusToken, env.trailingWS, env.trailingNL);
        } else {
            plusToken = formatToken(plusToken, 1, 0);
            if (minusToken.isMissing() && documentElements.isEmpty()) {
                // handle a scenario when the parameter name is the last token in the documentation line
                parameterName = formatToken(parameterName, env.trailingWS, env.trailingNL);
            } else {
                parameterName = formatToken(parameterName, 1, 0);
                if (documentElements.isEmpty()) {
                    // handle a scenario when the minus token is the last token in the documentation line
                    minusToken = formatToken(minusToken, env.trailingWS, env.trailingNL);
                } else {
                    minusToken = formatToken(minusToken, 1, 0);
                    documentElements = formatNodeList(markdownParameterDocumentationLineNode.documentElements(),
                            0, 0, env.trailingWS, env.trailingNL);
                }
            }
        }

        return markdownParameterDocumentationLineNode.modify()
                .withHashToken(hashToken)
                .withPlusToken(plusToken)
                .withParameterName(parameterName)
                .withMinusToken(minusToken)
                .withDocumentElements(documentElements)
                .apply();
    }

    @Override
    public BallerinaNameReferenceNode transform(BallerinaNameReferenceNode ballerinaNameReferenceNode) {
        Token referenceType = formatToken(ballerinaNameReferenceNode.referenceType().orElse(null), 1, 0);
        Token startBacktick = formatToken(ballerinaNameReferenceNode.startBacktick(), 0, 0);
        Node backtickContent = formatNode(ballerinaNameReferenceNode.nameReference(), 0, 0);
        Token endBacktick = formatToken(ballerinaNameReferenceNode.endBacktick(), env.trailingWS, env.trailingNL);

        return ballerinaNameReferenceNode.modify()
                .withReferenceType(referenceType)
                .withStartBacktick(startBacktick)
                .withNameReference(backtickContent)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public InlineCodeReferenceNode transform(InlineCodeReferenceNode inlineCodeReferenceNode) {
        Token startBacktick = formatToken(inlineCodeReferenceNode.startBacktick(), 0, 0);
        Token codeReference = formatToken(inlineCodeReferenceNode.codeReference(), 0, 0);
        Token endBacktick = formatToken(inlineCodeReferenceNode.endBacktick(), env.trailingWS, env.trailingNL);

        return inlineCodeReferenceNode.modify()
                .withStartBacktick(startBacktick)
                .withCodeReference(codeReference)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public MarkdownCodeBlockNode transform(MarkdownCodeBlockNode markdownCodeBlockNode) {
        Token startLineHash = formatToken(markdownCodeBlockNode.startLineHashToken(), 1, 0);
        boolean hasLangAttribute = markdownCodeBlockNode.langAttribute().isPresent();
        Token startBacktick = formatToken(markdownCodeBlockNode.startBacktick(), 0, hasLangAttribute ? 0 : 1);
        Token langAttribute = formatToken(markdownCodeBlockNode.langAttribute().orElse(null), 0, 1);
        NodeList<MarkdownCodeLineNode> codeLines = formatNodeList(markdownCodeBlockNode.codeLines(), 0, 1, 0, 1);
        Token endLineHash = formatToken(markdownCodeBlockNode.endLineHashToken(), 1, 0);
        Token endBacktick = formatToken(markdownCodeBlockNode.endBacktick(), env.trailingWS, env.trailingNL);

        return markdownCodeBlockNode.modify()
                .withStartLineHashToken(startLineHash)
                .withStartBacktick(startBacktick)
                .withLangAttribute(langAttribute)
                .withCodeLines(codeLines)
                .withEndLineHashToken(endLineHash)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public MarkdownCodeLineNode transform(MarkdownCodeLineNode markdownCodeLineNode) {
        Token codeDescription = markdownCodeLineNode.codeDescription();
        boolean hasDescription = !codeDescription.text().isEmpty();
        Token hashToken;
        if (hasDescription) {
            hashToken = formatToken(markdownCodeLineNode.hashToken(), 1, 0);
            codeDescription = formatToken(codeDescription, env.trailingWS, env.trailingNL);
        } else {
            hashToken = formatToken(markdownCodeLineNode.hashToken(), env.trailingWS, env.trailingNL);
        }

        return markdownCodeLineNode.modify()
                .withHashToken(hashToken)
                .withCodeDescription(codeDescription)
                .apply();
    }

    @Override
    public PositionalArgumentNode transform(PositionalArgumentNode positionalArgumentNode) {
        ExpressionNode expression = formatNode(positionalArgumentNode.expression(), env.trailingWS, env.trailingNL);
        return positionalArgumentNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public MappingBindingPatternNode transform(MappingBindingPatternNode mappingBindingPatternNode) {
        Token openBraceToken = formatToken(mappingBindingPatternNode.openBrace(), 0, 0);
        SeparatedNodeList<BindingPatternNode> fieldBindingPatternNodes =
                formatSeparatedNodeList(mappingBindingPatternNode.fieldBindingPatterns(), 0, 0, 0, 0);
        Token closeBraceToken = formatToken(mappingBindingPatternNode.closeBrace(), env.trailingWS, env.trailingNL);
        return mappingBindingPatternNode.modify()
                .withOpenBrace(openBraceToken)
                .withFieldBindingPatterns(fieldBindingPatternNodes)
                .withCloseBrace(closeBraceToken)
                .apply();
    }

    @Override
    public FieldBindingPatternFullNode transform(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        SimpleNameReferenceNode variableName = formatNode(fieldBindingPatternFullNode.variableName(), 0, 0);
        Token colon = formatToken(fieldBindingPatternFullNode.colon(), 1, 0);
        BindingPatternNode bindingPatternNode = formatNode(fieldBindingPatternFullNode.bindingPattern(),
                env.trailingWS, env.leadingNL);
        return fieldBindingPatternFullNode.modify()
                .withVariableName(variableName)
                .withColon(colon)
                .withBindingPattern(bindingPatternNode)
                .apply();
    }

    @Override
    public FieldBindingPatternVarnameNode transform(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        SimpleNameReferenceNode variableName = formatNode(fieldBindingPatternVarnameNode.variableName(),
                env.trailingWS, env.leadingNL);
        return fieldBindingPatternVarnameNode.modify()
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public TypeTestExpressionNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        ExpressionNode expression = formatNode(typeTestExpressionNode.expression(), 1, 0);
        Token isToken = formatToken(typeTestExpressionNode.isKeyword(), 1, 0);
        Node typeDescriptor = formatNode(typeTestExpressionNode.typeDescriptor(),
                env.trailingWS, env.trailingNL);
        return typeTestExpressionNode.modify()
                .withExpression(expression)
                .withIsKeyword(isToken)
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public ListenerDeclarationNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        Token visibilityQualifier = formatToken(listenerDeclarationNode.visibilityQualifier().orElse(null), 1, 0);
        MetadataNode metadata = formatNode(listenerDeclarationNode.metadata().orElse(null), 0, 1);
        Token listenerKeyword = formatToken(listenerDeclarationNode.listenerKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptor = formatNode(listenerDeclarationNode.typeDescriptor().orElse(null), 1, 0);
        Token variableName = formatToken(listenerDeclarationNode.variableName(), 1, 0);
        Token equalsToken = formatToken(listenerDeclarationNode.equalsToken(), 1, 0);
        Node initializer = formatNode(listenerDeclarationNode.initializer(), 0, 0);
        Token semicolonToken = formatToken(listenerDeclarationNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return listenerDeclarationNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withListenerKeyword(listenerKeyword)
                .withTypeDescriptor(typeDescriptor)
                .withVariableName(variableName)
                .withEqualsToken(equalsToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public MethodCallExpressionNode transform(MethodCallExpressionNode methodCallExpressionNode) {
        ExpressionNode expression = formatNode(methodCallExpressionNode.expression(), 0, 0);
        Token dotToken = formatToken(methodCallExpressionNode.dotToken(), 0, 0);
        NameReferenceNode methodName = formatNode(methodCallExpressionNode.methodName(), 0, 0);
        Token openParenToken = formatToken(methodCallExpressionNode.openParenToken(), 0, 0);
        SeparatedNodeList<FunctionArgumentNode> arguments = formatSeparatedNodeList(methodCallExpressionNode
                .arguments(), 0, 0, 0, 0);
        Token closeParenToken = formatToken(methodCallExpressionNode.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return methodCallExpressionNode.modify()
                .withExpression(expression)
                .withDotToken(dotToken)
                .withMethodName(methodName)
                .withOpenParenToken(openParenToken)
                .withArguments(arguments)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public NilLiteralNode transform(NilLiteralNode nilLiteralNode) {
        Token openParenToken = formatToken(nilLiteralNode.openParenToken(), 0, 0);
        Token closeParenToken = formatToken(nilLiteralNode.closeParenToken(), env.trailingWS, env.trailingNL);
        return nilLiteralNode.modify()
                .withOpenParenToken(openParenToken)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public XMLNamespaceDeclarationNode transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = formatToken(xMLNamespaceDeclarationNode.xmlnsKeyword(), 1, 0);
        boolean hasPrefix = xMLNamespaceDeclarationNode.asKeyword().isPresent();
        ExpressionNode namespaceUri = formatNode(xMLNamespaceDeclarationNode.namespaceuri(), hasPrefix ? 1 : 0, 0);
        Token asKeyword = formatToken(xMLNamespaceDeclarationNode.asKeyword().orElse(null), 1, 0);
        IdentifierToken namespacePrefix = formatNode(xMLNamespaceDeclarationNode.namespacePrefix().orElse(null), 0, 0);
        Token semicolonToken = formatToken(xMLNamespaceDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return xMLNamespaceDeclarationNode.modify()
                .withXmlnsKeyword(xmlnsKeyword)
                .withNamespaceuri(namespaceUri)
                .withAsKeyword(asKeyword)
                .withNamespacePrefix(namespacePrefix)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = formatToken(moduleXMLNamespaceDeclarationNode.xmlnsKeyword(), 1, 0);
        boolean hasPrefix = moduleXMLNamespaceDeclarationNode.asKeyword().isPresent();
        ExpressionNode namespaceUri = formatNode(moduleXMLNamespaceDeclarationNode.namespaceuri(), hasPrefix ? 1 : 0,
                0);
        Token asKeyword = formatToken(moduleXMLNamespaceDeclarationNode.asKeyword().orElse(null), 1, 0);
        IdentifierToken namespacePrefix =
                formatNode(moduleXMLNamespaceDeclarationNode.namespacePrefix().orElse(null), 0, 0);
        Token semicolonToken = formatToken(moduleXMLNamespaceDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);
        return moduleXMLNamespaceDeclarationNode.modify()
                .withAsKeyword(asKeyword)
                .withNamespacePrefix(namespacePrefix)
                .withXmlnsKeyword(xmlnsKeyword)
                .withNamespaceuri(namespaceUri)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public XMLSimpleNameNode transform(XMLSimpleNameNode xMLSimpleNameNode) {
        Token name = formatToken(xMLSimpleNameNode.name(), env.trailingWS, env.trailingNL);
        return xMLSimpleNameNode.modify()
                .withName(name)
                .apply();
    }

    @Override
    public XMLQualifiedNameNode transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        XMLSimpleNameNode prefix = formatNode(xMLQualifiedNameNode.prefix(), 0, 0);
        Token colon = formatToken(xMLQualifiedNameNode.colon(), 0, 0);
        XMLSimpleNameNode name = formatNode(xMLQualifiedNameNode.name(), env.trailingWS, env.trailingNL);

        return xMLQualifiedNameNode.modify()
                .withPrefix(prefix)
                .withName(name)
                .withColon(colon)
                .apply();
    }

    @Override
    public XMLEmptyElementNode transform(XMLEmptyElementNode xMLEmptyElementNode) {
        Token ltToken = formatToken(xMLEmptyElementNode.ltToken(), 0, 0);
        XMLNameNode name;
        if (xMLEmptyElementNode.attributes().isEmpty()) {
            name = formatNode(xMLEmptyElementNode.name(), 0, 0);
        } else {
            name = formatNode(xMLEmptyElementNode.name(), 1, 0);
        }

        NodeList<XMLAttributeNode> attributes = formatNodeList(xMLEmptyElementNode.attributes(), 1, 0, 0, 0);
        Token slashToken = formatToken(xMLEmptyElementNode.slashToken(), 0, 0);
        Token getToken = formatToken(xMLEmptyElementNode.getToken(), env.trailingWS, env.trailingNL);

        return xMLEmptyElementNode.modify()
                .withLtToken(ltToken)
                .withName(name)
                .withAttributes(attributes)
                .withSlashToken(slashToken)
                .withGetToken(getToken)
                .apply();
    }

    @Override
    public XMLTextNode transform(XMLTextNode xMLTextNode) {
        Token content = formatToken(xMLTextNode.content(), env.trailingWS, env.trailingNL);
        for (char c : content.text().toCharArray()) {
            if (c == '\n') {
                env.lineLength = 0;
            } else {
                env.lineLength++;
            }
        }

        return xMLTextNode.modify()
                .withContent(content)
                .apply();
    }

    @Override
    public XMLComment transform(XMLComment xMLComment) {
        Token commentStart = formatToken(xMLComment.commentStart(), 0, 0);
        NodeList<Node> content = formatNodeList(xMLComment.content(), 0, 0, 0, 0);
        Token commentEnd = formatToken(xMLComment.commentEnd(), env.trailingWS, env.trailingNL);

        return xMLComment.modify()
                .withCommentStart(commentStart)
                .withContent(content)
                .withCommentEnd(commentEnd)
                .apply();
    }

    @Override
    public XMLCDATANode transform(XMLCDATANode xmlCdataNode) {
        Token cdataStart = formatToken(xmlCdataNode.cdataStart(), 0, 0);
        NodeList<Node> content = formatNodeList(xmlCdataNode.content(), 0, 0, 0, 0);
        Token cdataEnd = formatToken(xmlCdataNode.cdataEnd(), env.trailingWS, env.trailingNL);

        return xmlCdataNode.modify()
                .withCdataStart(cdataStart)
                .withContent(content)
                .withCdataEnd(cdataEnd)
                .apply();
    }

    @Override
    public XMLProcessingInstruction transform(XMLProcessingInstruction xMLProcessingInstruction) {
        Token piStart = formatToken(xMLProcessingInstruction.piStart(), 0, 0);
        XMLNameNode target;

        if (xMLProcessingInstruction.data().isEmpty()) {
            target = formatNode(xMLProcessingInstruction.target(), 0, 0);
        } else {
            target = formatNode(xMLProcessingInstruction.target(), 1, 0);
        }

        NodeList<Node> data = formatNodeList(xMLProcessingInstruction.data(), 0, 0, 0, 0);
        Token piEnd = formatToken(xMLProcessingInstruction.piEnd(), env.trailingWS, env.trailingNL);
        return xMLProcessingInstruction.modify()
                .withTarget(target)
                .withPiStart(piStart)
                .withData(data)
                .withPiEnd(piEnd)
                .apply();
    }

    @Override
    public XMLFilterExpressionNode transform(XMLFilterExpressionNode xMLFilterExpressionNode) {
        ExpressionNode expression = formatNode(xMLFilterExpressionNode.expression(), 0, 0);
        XMLNamePatternChainingNode xmlPatternChain = formatNode(xMLFilterExpressionNode.xmlPatternChain(),
                env.trailingWS, env.trailingNL);

        return xMLFilterExpressionNode.modify()
                .withExpression(expression)
                .withXmlPatternChain(xmlPatternChain)
                .apply();
    }

    @Override
    public XMLStepExpressionNode transform(XMLStepExpressionNode xMLStepExpressionNode) {
        ExpressionNode expression = formatNode(xMLStepExpressionNode.expression(), 0, 0);
        Node xmlStepStart = formatNode(xMLStepExpressionNode.xmlStepStart(), env.trailingWS, env.trailingNL);

        return xMLStepExpressionNode.modify()
                .withExpression(expression)
                .withXmlStepStart(xmlStepStart)
                .apply();
    }

    @Override
    public XMLNamePatternChainingNode transform(XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        Token startToken = formatToken(xMLNamePatternChainingNode.startToken(), 0, 0);
        SeparatedNodeList<Node> xmlNamePattern = formatSeparatedNodeList(xMLNamePatternChainingNode.xmlNamePattern(),
                0, 0, 0, 0, 0, 0);
        Token gtToken = formatToken(xMLNamePatternChainingNode.gtToken(), env.trailingWS, env.trailingNL);

        return xMLNamePatternChainingNode.modify()
                .withStartToken(startToken)
                .withXmlNamePattern(xmlNamePattern)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public XMLAtomicNamePatternNode transform(XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        Token prefix = formatToken(xMLAtomicNamePatternNode.prefix(), 0, 0);
        Token colon = formatToken(xMLAtomicNamePatternNode.colon(), 0, 0);
        Token name = formatToken(xMLAtomicNamePatternNode.name(), env.trailingWS, env.trailingNL);

        return xMLAtomicNamePatternNode.modify()
                .withPrefix(prefix)
                .withColon(colon)
                .withName(name)
                .apply();
    }

    @Override
    public TemplateExpressionNode transform(TemplateExpressionNode templateExpressionNode) {
        Token type = formatToken(templateExpressionNode.type().orElse(null), 1, 0);
        Token startBacktick = formatToken(templateExpressionNode.startBacktick(), 0, 0);
        NodeList<Node> content = formatNodeList(templateExpressionNode.content(), 0, 0, 0, 0);
        Token endBacktick = formatToken(templateExpressionNode.endBacktick(), env.trailingWS, env.trailingNL);

        return templateExpressionNode.modify()
                .withType(type)
                .withStartBacktick(startBacktick)
                .withContent(content)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public ByteArrayLiteralNode transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        Token type = formatToken(byteArrayLiteralNode.type(), 1, 0);
        Token startBacktick = formatToken(byteArrayLiteralNode.startBacktick(), 0, 0);
        Token content = formatToken(byteArrayLiteralNode.content().orElse(null), 0, 0);
        Token endBacktick = formatToken(byteArrayLiteralNode.endBacktick(), env.trailingWS, env.trailingNL);
        return byteArrayLiteralNode.modify()
                .withType(type)
                .withStartBacktick(startBacktick)
                .withContent(content)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public TypeReferenceNode transform(TypeReferenceNode typeReferenceNode) {
        Token asteriskToken = formatToken(typeReferenceNode.asteriskToken(), 0, 0);
        Node typeName = formatNode(typeReferenceNode.typeName(), 0, 0);
        Token semicolonToken = formatToken(typeReferenceNode.semicolonToken(), env.trailingWS, env.trailingNL);
        return typeReferenceNode.modify()
                .withAsteriskToken(asteriskToken)
                .withTypeName(typeName)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public MappingMatchPatternNode transform(MappingMatchPatternNode mappingMatchPatternNode) {
        Token openBraceToken = formatToken(mappingMatchPatternNode.openBraceToken(), 0, 0);
        SeparatedNodeList<Node> fieldMatchPatterns =
                formatSeparatedNodeList(mappingMatchPatternNode.fieldMatchPatterns(), 0, 0, 0, 0);
        Token closeBraceToken =
                formatToken(mappingMatchPatternNode.closeBraceToken(), env.trailingWS, env.trailingNL);

        return mappingMatchPatternNode.modify()
                .withOpenBraceToken(openBraceToken)
                .withFieldMatchPatterns(fieldMatchPatterns)
                .withCloseBraceToken(closeBraceToken)
                .apply();
    }

    @Override
    public StartActionNode transform(StartActionNode startActionNode) {
        NodeList<AnnotationNode> annotations;
        if (env.inlineAnnotation) {
            annotations = formatNodeList(startActionNode.annotations(), 1, 0, 1, 0);
        } else {
            annotations = formatNodeList(startActionNode.annotations(), 0, 1, 0, 1);
        }

        Token startKeyword = formatToken(startActionNode.startKeyword(), 1, 0);
        ExpressionNode expression = formatNode(startActionNode.expression(), env.trailingWS, env.trailingNL);
        return startActionNode.modify()
                .withAnnotations(annotations)
                .withStartKeyword(startKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public FlushActionNode transform(FlushActionNode flushActionNode) {
        Token flushKeyword;
        if (flushActionNode.peerWorker().isPresent()) {
            flushKeyword = formatToken(flushActionNode.flushKeyword(), 1, 0);
        } else {
            flushKeyword = formatToken(flushActionNode.flushKeyword(), env.trailingWS, env.trailingNL);
        }

        NameReferenceNode peerWorker = formatNode(flushActionNode.peerWorker().orElse(null),
                env.trailingWS, env.trailingNL);
        return flushActionNode.modify()
                .withFlushKeyword(flushKeyword)
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public FailStatementNode transform(FailStatementNode failStatementNode) {
        Token failKeyword = formatToken(failStatementNode.failKeyword(), 1, 0);
        ExpressionNode expression = formatNode(failStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(failStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return failStatementNode.modify()
                .withFailKeyword(failKeyword)
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ContinueStatementNode transform(ContinueStatementNode continueStatementNode) {
        Token continueToken = formatToken(continueStatementNode.continueToken(), 0, 0);
        Token semicolonToken = formatToken(continueStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return continueStatementNode.modify()
                .withContinueToken(continueToken)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public LocalTypeDefinitionStatementNode transform(
            LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(localTypeDefinitionStatementNode.annotations(),
                1, 0, 1, 0);
        Token typeKeyword = formatToken(localTypeDefinitionStatementNode.typeKeyword(), 1, 0);
        Node typeName = formatNode(localTypeDefinitionStatementNode.typeName(), 1, 0);
        Node typeDescriptor = formatNode(localTypeDefinitionStatementNode.typeDescriptor(), 1, 0);
        Token semicolonToken = formatToken(localTypeDefinitionStatementNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return localTypeDefinitionStatementNode.modify()
                .withAnnotations(annotations)
                .withTypeKeyword(typeKeyword)
                .withTypeName(typeName)
                .withTypeDescriptor(typeDescriptor)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ForkStatementNode transform(ForkStatementNode forkStatementNode) {
        Token forkKeyword = formatToken(forkStatementNode.forkKeyword(), 1, 0);
        Token openBraceToken = formatToken(forkStatementNode.openBraceToken(), 0, 1);
        indent();
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                formatNodeList(forkStatementNode.namedWorkerDeclarations(), 0, 1, 0, 1);
        unindent();
        Token closeBraceToken = formatToken(forkStatementNode.closeBraceToken(), env.trailingWS, env.trailingNL);

        return forkStatementNode.modify()
                .withForkKeyword(forkKeyword)
                .withOpenBraceToken(openBraceToken)
                .withNamedWorkerDeclarations(namedWorkerDeclarations)
                .withCloseBraceToken(closeBraceToken)
                .apply();
    }

    @Override
    public TypeofExpressionNode transform(TypeofExpressionNode typeofExpressionNode) {
        Token typeofKeyword = formatToken(typeofExpressionNode.typeofKeyword(), 1, 0);
        ExpressionNode expression = formatNode(typeofExpressionNode.expression(), env.trailingWS, env.trailingNL);
        return typeofExpressionNode.modify()
                .withTypeofKeyword(typeofKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public UnaryExpressionNode transform(UnaryExpressionNode unaryExpressionNode) {
        Token unaryOperator = formatToken(unaryExpressionNode.unaryOperator(), 0, 0);
        ExpressionNode expression = formatNode(unaryExpressionNode.expression(), env.trailingWS, env.trailingNL);

        return unaryExpressionNode.modify()
                .withUnaryOperator(unaryOperator)
                .withExpression(expression)
                .apply();
    }

    @Override
    public DefaultableParameterNode transform(DefaultableParameterNode defaultableParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(defaultableParameterNode.annotations(), 1, 0, 1, 0);
        Node typeName = formatNode(defaultableParameterNode.typeName(), 1, 0);
        Token paramName = formatToken(defaultableParameterNode.paramName().orElse(null), 1, 0);
        Token equalsToken = formatToken(defaultableParameterNode.equalsToken(), 1, 0);
        Node expression = formatNode(defaultableParameterNode.expression(), env.trailingWS, env.trailingNL);

        return defaultableParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .withParamName(paramName)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .apply();
    }

    @Override
    public RestParameterNode transform(RestParameterNode restParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(restParameterNode.annotations(), 0, 1, 0, 1);
        Node typeName = formatNode(restParameterNode.typeName(), 0, 0);
        Token ellipsisToken;
        if (restParameterNode.paramName().isPresent()) {
            ellipsisToken = formatToken(restParameterNode.ellipsisToken(), 1, 0);
        } else {
            ellipsisToken = formatToken(restParameterNode.ellipsisToken(), env.trailingWS, env.trailingNL);
        }

        Token paramName = formatToken(restParameterNode.paramName().orElse(null), env.trailingWS, env.trailingNL);
        return restParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .withParamName(paramName)
                .withEllipsisToken(ellipsisToken)
                .apply();
    }

    @Override
    public SpreadFieldNode transform(SpreadFieldNode spreadFieldNode) {
        Token ellipsis = formatToken(spreadFieldNode.ellipsis(), 0, 0);
        ExpressionNode valueExpr = formatNode(spreadFieldNode.valueExpr(), env.trailingWS, env.trailingNL);
        return spreadFieldNode.modify()
                .withEllipsis(ellipsis)
                .withValueExpr(valueExpr)
                .apply();
    }

    @Override
    public SpreadMemberNode transform(SpreadMemberNode spreadMemberNode) {
        Token ellipsis = formatToken(spreadMemberNode.ellipsis(), 0, 0);
        ExpressionNode valueExpr = formatNode(spreadMemberNode.expression(), env.trailingWS, env.trailingNL);
        return spreadMemberNode.modify()
                .withEllipsis(ellipsis)
                .withExpression(valueExpr)
                .apply();
    }

    @Override
    public NamedArgumentNode transform(NamedArgumentNode namedArgumentNode) {
        SimpleNameReferenceNode argumentName = formatNode(namedArgumentNode.argumentName(), 1, 0);
        Token equalsToken = formatToken(namedArgumentNode.equalsToken(), 1, 0);
        ExpressionNode expression = formatNode(namedArgumentNode.expression(), env.trailingWS, env.trailingNL);
        return namedArgumentNode.modify()
                .withArgumentName(argumentName)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .apply();
    }

    @Override
    public RestArgumentNode transform(RestArgumentNode restArgumentNode) {
        Token ellipsis = formatToken(restArgumentNode.ellipsis(), 0, 0);
        ExpressionNode expression = formatNode(restArgumentNode.expression(), env.trailingWS, env.trailingNL);
        return restArgumentNode.modify()
                .withEllipsis(ellipsis)
                .withExpression(expression)
                .apply();
    }

    @Override
    public ObjectTypeDescriptorNode transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        NodeList<Token> objectTypeQualifiers = formatNodeList(objectTypeDescriptorNode.objectTypeQualifiers(),
                1, 0, 1, 0);
        Token objectKeyword = formatToken(objectTypeDescriptorNode.objectKeyword(), 1, 0);

        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(objectTypeDescriptorNode)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        Token openBrace = formatToken(objectTypeDescriptorNode.openBrace(), 0, fieldTrailingNL);
        indent();
        NodeList<Node> members = formatNodeList(objectTypeDescriptorNode.members(), fieldTrailingWS, fieldTrailingNL,
                0, fieldTrailingNL);
        unindent();
        Token closeBrace = formatToken(objectTypeDescriptorNode.closeBrace(), env.trailingWS, env.trailingNL);

        return objectTypeDescriptorNode.modify()
                .withObjectTypeQualifiers(objectTypeQualifiers)
                .withObjectKeyword(objectKeyword)
                .withOpenBrace(openBrace)
                .withMembers(members)
                .withCloseBrace(closeBrace)
                .apply();
    }

    @Override
    public ObjectConstructorExpressionNode transform(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(objectConstructorExpressionNode.annotations(),
                1, 0, 1, 0);
        NodeList<Token> objectTypeQualifiers = formatNodeList(objectConstructorExpressionNode.objectTypeQualifiers(),
                1, 0, 1, 0);
        Token objectKeyword = formatToken(objectConstructorExpressionNode.objectKeyword(), 1, 0);

        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(objectConstructorExpressionNode)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        TypeDescriptorNode typeReference = formatNode(objectConstructorExpressionNode.typeReference().orElse(null),
                1, 0);
        Token openBraceToken = formatToken(objectConstructorExpressionNode.openBraceToken(), 0, fieldTrailingNL);
        indent();
        NodeList<Node> members = formatNodeList(objectConstructorExpressionNode.members(),
                fieldTrailingWS, fieldTrailingNL, 0, fieldTrailingNL);
        unindent();
        Token closeBraceToken = formatToken(objectConstructorExpressionNode.closeBraceToken(),
                env.trailingWS, env.trailingNL);

        return objectConstructorExpressionNode.modify()
                .withAnnotations(annotations)
                .withObjectTypeQualifiers(objectTypeQualifiers)
                .withObjectKeyword(objectKeyword)
                .withTypeReference(typeReference)
                .withOpenBraceToken(openBraceToken)
                .withMembers(members)
                .withCloseBraceToken(closeBraceToken)
                .apply();
    }

    @Override
    public ObjectFieldNode transform(ObjectFieldNode objectFieldNode) {
        MetadataNode metadata = formatNode(objectFieldNode.metadata().orElse(null), 0, 1);
        Token visibilityQualifier = formatToken(objectFieldNode.visibilityQualifier().orElse(null), 1, 0);
        NodeList<Token> qualifierList = formatNodeList(objectFieldNode.qualifierList(), 1, 0, 1, 0);
        Node typeName = formatNode(objectFieldNode.typeName(), 1, 0);
        Token fieldName;
        if (objectFieldNode.equalsToken().isPresent() && objectFieldNode.expression().isPresent()) {
            fieldName = formatToken(objectFieldNode.fieldName(), 1, 0);
        } else {
            fieldName = formatToken(objectFieldNode.fieldName(), 0, 0);
        }
        Token equalsToken = formatToken(objectFieldNode.equalsToken().orElse(null), 1, 0);
        ExpressionNode expression = formatNode(objectFieldNode.expression().orElse(null), 0, 0);
        Token semicolonToken = formatToken(objectFieldNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return objectFieldNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withQualifierList(qualifierList)
                .withTypeName(typeName)
                .withFieldName(fieldName)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public RecordRestDescriptorNode transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        Node typeName = formatNode(recordRestDescriptorNode.typeName(), 0, 0);
        Token ellipsisToken = formatToken(recordRestDescriptorNode.ellipsisToken(), 0, 0);
        Token semicolonToken = formatToken(recordRestDescriptorNode.semicolonToken(), env.trailingWS, env.trailingNL);
        return recordRestDescriptorNode.modify()
                .withTypeName(typeName)
                .withEllipsisToken(ellipsisToken)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public AnnotationDeclarationNode transform(AnnotationDeclarationNode annotationDeclarationNode) {
        MetadataNode metadata = formatNode(annotationDeclarationNode.metadata().orElse(null), 0, 1);
        Token visibilityQualifier = formatToken(annotationDeclarationNode.visibilityQualifier().orElse(null), 1, 0);
        Token constKeyword = formatToken(annotationDeclarationNode.constKeyword().orElse(null), 1, 0);
        Token annotationKeyword = formatToken(annotationDeclarationNode.annotationKeyword(), 1, 0);
        Node typeDescriptor = formatNode(annotationDeclarationNode.typeDescriptor().orElse(null), 1, 0);
        Token annotationTag;
        if (annotationDeclarationNode.onKeyword().isPresent()) {
            annotationTag = formatToken(annotationDeclarationNode.annotationTag(), 1, 0);
        } else {
            annotationTag = formatToken(annotationDeclarationNode.annotationTag(), 0, 0);
        }

        Token onKeyword = formatToken(annotationDeclarationNode.onKeyword().orElse(null), 1, 0);
        int currentIndentation = env.currentIndentation;
        setIndentation(env.lineLength);
        SeparatedNodeList<Node> attachPoints = formatSeparatedNodeList(annotationDeclarationNode.attachPoints(),
                0, 0, 0, 0);
        setIndentation(currentIndentation);
        Token semicolonToken = formatToken(annotationDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);
        return annotationDeclarationNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withConstKeyword(constKeyword)
                .withAnnotationKeyword(annotationKeyword)
                .withTypeDescriptor(typeDescriptor)
                .withAnnotationTag(annotationTag)
                .withOnKeyword(onKeyword)
                .withAttachPoints(attachPoints)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public AnnotationAttachPointNode transform(AnnotationAttachPointNode annotationAttachPointNode) {
        Token sourceKeyword = formatToken(annotationAttachPointNode.sourceKeyword().orElse(null), 1, 0);
        NodeList<Token> identifiers = formatNodeList(annotationAttachPointNode.identifiers(), 1, 0, env.trailingWS,
                env.trailingNL);
        return annotationAttachPointNode.modify()
                .withSourceKeyword(sourceKeyword)
                .withIdentifiers(identifiers)
                .apply();
    }

    @Override
    public TrapExpressionNode transform(TrapExpressionNode trapExpressionNode) {
        Token trapKeyword = formatToken(trapExpressionNode.trapKeyword(), 1, 0);
        ExpressionNode expression = formatNode(trapExpressionNode.expression(), env.trailingWS, env.trailingNL);
        return trapExpressionNode.modify()
                .withTrapKeyword(trapKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public TableConstructorExpressionNode transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        Token tableKeyword = formatToken(tableConstructorExpressionNode.tableKeyword(), 1, 0);
        KeySpecifierNode keySpecifier = formatNode(tableConstructorExpressionNode.keySpecifier().orElse(null), 1, 0);
        int rowTrailingWS = 0, rowTrailingNL = 0;
        if (shouldExpand(tableConstructorExpressionNode)) {
            rowTrailingNL++;
        } else {
            rowTrailingWS++;
        }

        Token openBracket = formatToken(tableConstructorExpressionNode.openBracket(), 0, rowTrailingNL);
        indent();
        SeparatedNodeList<Node> mappingConstructors =
                formatSeparatedNodeList(tableConstructorExpressionNode.rows(), 0, 0, rowTrailingWS, rowTrailingNL, 0,
                        rowTrailingNL);
        unindent();
        Token closeBracket =
                formatToken(tableConstructorExpressionNode.closeBracket(), env.trailingWS, env.trailingNL);

        return tableConstructorExpressionNode.modify()
                .withTableKeyword(tableKeyword)
                .withKeySpecifier(keySpecifier)
                .withOpenBracket(openBracket)
                .withRows(mappingConstructors)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public KeySpecifierNode transform(KeySpecifierNode keySpecifierNode) {
        Token keyKeyword = formatToken(keySpecifierNode.keyKeyword(), 0, 0);
        Token openParenToken = formatToken(keySpecifierNode.openParenToken(), 0, 0);
        SeparatedNodeList<IdentifierToken> fieldNames = formatSeparatedNodeList(keySpecifierNode.fieldNames(),
                0, 0, 0, 0);
        Token closeParenToken = formatToken(keySpecifierNode.closeParenToken(), env.trailingWS, env.trailingNL);

        return keySpecifierNode.modify()
                .withKeyKeyword(keyKeyword)
                .withOpenParenToken(openParenToken)
                .withFieldNames(fieldNames)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public StreamTypeDescriptorNode transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        Token streamKeywordToken;
        if (streamTypeDescriptorNode.streamTypeParamsNode().isPresent()) {
            streamKeywordToken = formatToken(streamTypeDescriptorNode.streamKeywordToken(), 0, 0);
        } else {
            streamKeywordToken = formatToken(streamTypeDescriptorNode.streamKeywordToken(),
                    env.trailingWS, env.trailingNL);
        }

        Node streamTypeParamsNode = formatNode(streamTypeDescriptorNode.streamTypeParamsNode().orElse(null),
                env.trailingWS, env.trailingNL);
        return streamTypeDescriptorNode.modify()
                .withStreamKeywordToken(streamKeywordToken)
                .withStreamTypeParamsNode(streamTypeParamsNode)
                .apply();
    }

    @Override
    public StreamTypeParamsNode transform(StreamTypeParamsNode streamTypeParamsNode) {
        Token ltToken = formatToken(streamTypeParamsNode.ltToken(), 0, 0);
        Node leftTypeDescNode = formatNode(streamTypeParamsNode.leftTypeDescNode(), 0, 0);
        Token commaToken = formatToken(streamTypeParamsNode.commaToken().orElse(null), 1, 0);
        Node rightTypeDescNode = formatNode(streamTypeParamsNode.rightTypeDescNode().orElse(null), 0, 0);
        Token gtToken = formatToken(streamTypeParamsNode.gtToken(), env.trailingWS, env.trailingNL);

        return streamTypeParamsNode.modify()
                .withLtToken(ltToken)
                .withLeftTypeDescNode(leftTypeDescNode)
                .withCommaToken(commaToken)
                .withRightTypeDescNode(rightTypeDescNode)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public LetExpressionNode transform(LetExpressionNode letExpressionNode) {
        Token letKeyword = formatToken(letExpressionNode.letKeyword(), 1, 0);
        int listTrailingNL = 0;
        int listTrailingWS = 0;
        if (shouldExpand(letExpressionNode)) {
            listTrailingNL++;
        } else {
            listTrailingWS++;
        }

        indent();
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                formatSeparatedNodeList(letExpressionNode.letVarDeclarations(), 0, 0, listTrailingWS, listTrailingNL);
        Token inKeyword = formatToken(letExpressionNode.inKeyword(), 1, 0);
        ExpressionNode expression = formatNode(letExpressionNode.expression(), env.trailingWS, env.trailingNL);
        unindent();

        return letExpressionNode.modify()
                .withLetKeyword(letKeyword)
                .withLetVarDeclarations(letVarDeclarations)
                .withInKeyword(inKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public LetVariableDeclarationNode transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(letVariableDeclarationNode.annotations(), 0, 1, 0, 1);
        TypedBindingPatternNode typedBindingPattern = formatNode(letVariableDeclarationNode.typedBindingPattern(),
                1, 0);
        Token equalsToken = formatToken(letVariableDeclarationNode.equalsToken(), 1, 0);
        ExpressionNode expression = formatNode(letVariableDeclarationNode.expression(),
                env.trailingWS, env.trailingNL);

        return letVariableDeclarationNode.modify()
                .withAnnotations(annotations)
                .withTypedBindingPattern(typedBindingPattern)
                .withEqualsToken(equalsToken)
                .withExpression(expression)
                .apply();
    }

    @Override
    public InterpolationNode transform(InterpolationNode interpolationNode) {
        Token interpolationStartToken = formatToken(interpolationNode.interpolationStartToken(), 0, 0);
        ExpressionNode expression = formatNode(interpolationNode.expression(), 0, 0);
        Token interpolationEndToken = formatToken(interpolationNode.interpolationEndToken(),
                env.trailingWS, env.trailingNL);

        return interpolationNode.modify()
                .withInterpolationStartToken(interpolationStartToken)
                .withExpression(expression)
                .withInterpolationEndToken(interpolationEndToken)
                .apply();
    }

    @Override
    public ExplicitAnonymousFunctionExpressionNode transform(
            ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        NodeList<AnnotationNode> annotations =
                formatNodeList(explicitAnonymousFunctionExpressionNode.annotations(), 0, 1, 0, 1);

        NodeList<Token> qualifierList = formatNodeList(explicitAnonymousFunctionExpressionNode.qualifierList(),
                1, 0, 1, 0);
        Token functionKeyword = formatToken(explicitAnonymousFunctionExpressionNode.functionKeyword(), 0, 0);
        FunctionSignatureNode functionSignature =
                formatNode(explicitAnonymousFunctionExpressionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBody = formatNode(explicitAnonymousFunctionExpressionNode.functionBody(),
                env.trailingWS, env.trailingNL);

        return explicitAnonymousFunctionExpressionNode.modify()
                .withQualifierList(qualifierList)
                .withAnnotations(annotations)
                .withFunctionKeyword(functionKeyword)
                .withFunctionSignature(functionSignature)
                .withFunctionBody(functionBody)
                .apply();
    }

    @Override
    public ImplicitNewExpressionNode transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        Token newKeyword;
        if (implicitNewExpressionNode.parenthesizedArgList().isPresent()) {
            newKeyword = formatToken(implicitNewExpressionNode.newKeyword(), 1, 0);
        } else {
            newKeyword = formatToken(implicitNewExpressionNode.newKeyword(), env.trailingWS,
                    env.trailingNL);
        }

        ParenthesizedArgList parenthesizedArgList =
                formatNode(implicitNewExpressionNode.parenthesizedArgList().orElse(null), env.trailingWS,
                        env.trailingNL);
        return implicitNewExpressionNode.modify()
                .withNewKeyword(newKeyword)
                .withParenthesizedArgList(parenthesizedArgList)
                .apply();
    }

    @Override
    public QueryConstructTypeNode transform(QueryConstructTypeNode queryConstructTypeNode) {
        Token keyword;
        if (queryConstructTypeNode.keySpecifier().isPresent()) {
            keyword = formatToken(queryConstructTypeNode.keyword(), 1, 0);
        } else {
            keyword = formatToken(queryConstructTypeNode.keyword(), env.trailingWS,
                    env.trailingNL);
        }

        KeySpecifierNode keySpecifier =
                formatNode(queryConstructTypeNode.keySpecifier().orElse(null), env.trailingWS,
                        env.trailingNL);
        return queryConstructTypeNode.modify()
                .withKeyword(keyword)
                .withKeySpecifier(keySpecifier)
                .apply();
    }

    @Override
    public FromClauseNode transform(FromClauseNode fromClauseNode) {
        Token fromKeyword = formatToken(fromClauseNode.fromKeyword(), 1, 0);
        TypedBindingPatternNode typedBindingPattern = formatNode(fromClauseNode.typedBindingPattern(), 1, 0);
        Token inKeyword = formatToken(fromClauseNode.inKeyword(), 1, 0);
        ExpressionNode expression = formatNode(fromClauseNode.expression(), env.trailingWS, env.trailingNL);

        return fromClauseNode.modify()
                .withFromKeyword(fromKeyword)
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(inKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public WhereClauseNode transform(WhereClauseNode whereClauseNode) {
        Token whereKeyword = formatToken(whereClauseNode.whereKeyword(), 1, 0);
        ExpressionNode expression = formatNode(whereClauseNode.expression(), env.trailingWS, env.trailingNL);
        return whereClauseNode.modify()
                .withWhereKeyword(whereKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public LetClauseNode transform(LetClauseNode letClauseNode) {
        Token letKeyword = formatToken(letClauseNode.letKeyword(), 1, 0);
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                formatSeparatedNodeList(letClauseNode.letVarDeclarations(), 0, 0, env.trailingWS, env.trailingNL);
        return letClauseNode.modify()
                .withLetKeyword(letKeyword)
                .withLetVarDeclarations(letVarDeclarations)
                .apply();
    }

    @Override
    public QueryPipelineNode transform(QueryPipelineNode queryPipelineNode) {
        FromClauseNode fromClause = formatNode(queryPipelineNode.fromClause(), 0, 1);
        NodeList<IntermediateClauseNode> intermediateClauses = formatNodeList(queryPipelineNode.intermediateClauses(),
                0, 1, env.trailingWS, env.trailingNL);
        return queryPipelineNode.modify()
                .withFromClause(fromClause)
                .withIntermediateClauses(intermediateClauses)
                .apply();
    }

    @Override
    public SelectClauseNode transform(SelectClauseNode selectClauseNode) {
        Token selectKeyword = formatToken(selectClauseNode.selectKeyword(), 1, 0);
        ExpressionNode expression = formatNode(selectClauseNode.expression(), env.trailingWS, env.trailingNL);
        return selectClauseNode.modify()
                .withSelectKeyword(selectKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public QueryExpressionNode transform(QueryExpressionNode queryExpressionNode) {
        int lineLength = env.lineLength;
        if (lineLength != 0) {
            indent();
        }

        QueryConstructTypeNode queryConstructType =
                formatNode(queryExpressionNode.queryConstructType().orElse(null), 1, 0);
        QueryPipelineNode queryPipeline = formatNode(queryExpressionNode.queryPipeline(), 0, 1);

        SelectClauseNode selectClause;
        if (queryExpressionNode.onConflictClause().isPresent()) {
            selectClause = formatNode(queryExpressionNode.selectClause(), 0, 1);
        } else {
            selectClause = formatNode(queryExpressionNode.selectClause(), env.trailingWS, env.trailingNL);
        }

        OnConflictClauseNode onConflictClause = formatNode(queryExpressionNode.onConflictClause().orElse(null),
                env.trailingWS, env.trailingNL);
        if (lineLength != 0) {
            unindent();
        }

        return queryExpressionNode.modify()
                .withQueryConstructType(queryConstructType)
                .withQueryPipeline(queryPipeline)
                .withSelectClause(selectClause)
                .withOnConflictClause(onConflictClause)
                .apply();
    }

    @Override
    public ImplicitAnonymousFunctionParameters transform(
            ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        Token openParenToken = formatToken(implicitAnonymousFunctionParameters.openParenToken(), 0, 0);
        SeparatedNodeList<SimpleNameReferenceNode> parameters =
                formatSeparatedNodeList(implicitAnonymousFunctionParameters.parameters(), 0, 0, 0, 0);
        Token closeParenToken = formatToken(implicitAnonymousFunctionParameters.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return implicitAnonymousFunctionParameters.modify()
                .withOpenParenToken(openParenToken)
                .withParameters(parameters)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public ImplicitAnonymousFunctionExpressionNode transform(
            ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        Node params = formatNode(implicitAnonymousFunctionExpressionNode.params(), 1, 0);
        Token rightDoubleArrow = formatToken(implicitAnonymousFunctionExpressionNode.rightDoubleArrow(), 1, 0);
        ExpressionNode expression = formatNode(implicitAnonymousFunctionExpressionNode.expression(),
                env.trailingWS, env.trailingNL);

        return implicitAnonymousFunctionExpressionNode.modify()
                .withParams(params)
                .withRightDoubleArrow(rightDoubleArrow)
                .withExpression(expression)
                .apply();
    }

    @Override
    public MethodDeclarationNode transform(MethodDeclarationNode methodDeclarationNode) {
        MetadataNode metadata = formatNode(methodDeclarationNode.metadata().orElse(null), 0, 1);
        NodeList<Token> qualifierList = formatNodeList(methodDeclarationNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword = formatToken(methodDeclarationNode.functionKeyword(), 1, 0);
        IdentifierToken methodName;
        if (methodDeclarationNode.relativeResourcePath().isEmpty()) {
            methodName = formatNode(methodDeclarationNode.methodName(), 0, 0);
        } else {
            methodName = formatNode(methodDeclarationNode.methodName(), 1, 0);
        }
        NodeList<Node> relativeResourcePath = formatNodeList(methodDeclarationNode.relativeResourcePath(), 0, 0, 0, 0);
        FunctionSignatureNode methodSignature = formatNode(methodDeclarationNode.methodSignature(), 0, 0);
        Token semicolon = formatToken(methodDeclarationNode.semicolon(), env.trailingWS, env.trailingNL);

        return methodDeclarationNode.modify()
                .withMetadata(metadata)
                .withQualifierList(qualifierList)
                .withFunctionKeyword(functionKeyword)
                .withMethodName(methodName)
                .withRelativeResourcePath(relativeResourcePath)
                .withMethodSignature(methodSignature)
                .withSemicolon(semicolon)
                .apply();
    }

    @Override
    public WildcardBindingPatternNode transform(WildcardBindingPatternNode wildcardBindingPatternNode) {
        Token underscoreToken = formatToken(wildcardBindingPatternNode.underscoreToken(),
                env.trailingWS, env.trailingNL);
        return wildcardBindingPatternNode.modify()
                .withUnderscoreToken(underscoreToken)
                .apply();
    }

    @Override
    public ErrorBindingPatternNode transform(ErrorBindingPatternNode errorBindingPatternNode) {
        Token errorKeyword;
        if (errorBindingPatternNode.typeReference().isPresent()) {
            errorKeyword = formatToken(errorBindingPatternNode.errorKeyword(), 1, 0);
        } else {
            errorKeyword = formatToken(errorBindingPatternNode.errorKeyword(), 0, 0);
        }

        Node typeReference = formatNode(errorBindingPatternNode.typeReference().orElse(null), 0, 0);
        Token openParenthesis = formatToken(errorBindingPatternNode.openParenthesis(), 0, 0);
        SeparatedNodeList<BindingPatternNode> argListBindingPatterns =
                formatSeparatedNodeList(errorBindingPatternNode.argListBindingPatterns(), 0, 0, 0, 0);
        Token closeParenthesis = formatToken(errorBindingPatternNode.closeParenthesis(),
                env.trailingWS, env.trailingNL);

        return errorBindingPatternNode.modify()
                .withErrorKeyword(errorKeyword)
                .withTypeReference(typeReference)
                .withOpenParenthesis(openParenthesis)
                .withArgListBindingPatterns(argListBindingPatterns)
                .withCloseParenthesis(closeParenthesis)
                .apply();
    }

    @Override
    public NamedArgBindingPatternNode transform(NamedArgBindingPatternNode namedArgBindingPatternNode) {
        IdentifierToken argName = formatToken(namedArgBindingPatternNode.argName(), 1, 0);
        Token equalsToken = formatToken(namedArgBindingPatternNode.equalsToken(), 1, 0);
        BindingPatternNode bindingPattern = formatNode(namedArgBindingPatternNode.bindingPattern(), env.trailingWS,
                env.leadingNL);
        return namedArgBindingPatternNode.modify()
                .withArgName(argName)
                .withEqualsToken(equalsToken)
                .withBindingPattern(bindingPattern)
                .apply();
    }

    @Override
    public AsyncSendActionNode transform(AsyncSendActionNode asyncSendActionNode) {
        ExpressionNode expression = formatNode(asyncSendActionNode.expression(), 1, 0);
        Token rightArrowToken = formatToken(asyncSendActionNode.rightArrowToken(), 1, 0);
        SimpleNameReferenceNode peerWorker = formatNode(asyncSendActionNode.peerWorker(),
                env.trailingWS, env.trailingNL);

        return asyncSendActionNode.modify()
                .withExpression(expression)
                .withRightArrowToken(rightArrowToken)
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public SyncSendActionNode transform(SyncSendActionNode syncSendActionNode) {
        ExpressionNode expression = formatNode(syncSendActionNode.expression(), 1, 0);
        Token syncSendToken = formatToken(syncSendActionNode.syncSendToken(), 1, 0);
        SimpleNameReferenceNode peerWorker = formatNode(syncSendActionNode.peerWorker(),
                env.trailingWS, env.trailingNL);
        return syncSendActionNode.modify()
                .withExpression(expression)
                .withSyncSendToken(syncSendToken)
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public ReceiveActionNode transform(ReceiveActionNode receiveActionNode) {
        Token leftArrow = formatToken(receiveActionNode.leftArrow(), 1, 0);
        Node receiveWorkers = formatNode(receiveActionNode.receiveWorkers(), env.trailingWS, env.trailingNL);
        return receiveActionNode.modify()
                .withLeftArrow(leftArrow)
                .withReceiveWorkers(receiveWorkers)
                .apply();
    }

    @Override
    public ReceiveFieldsNode transform(ReceiveFieldsNode receiveFieldsNode) {
        Token openBrace = formatToken(receiveFieldsNode.openBrace(), 0, 1);
        indent();
        SeparatedNodeList<NameReferenceNode> receiveFields = formatSeparatedNodeList(receiveFieldsNode.receiveFields(),
                0, 1, 0, 1);
        Token closeBrace = formatToken(receiveFieldsNode.closeBrace(), 0, 1);
        unindent();
        return receiveFieldsNode.modify()
                .withOpenBrace(openBrace)
                .withReceiveFields(receiveFields)
                .withCloseBrace(closeBrace)
                .apply();
    }

    @Override
    public RestDescriptorNode transform(RestDescriptorNode restDescriptorNode) {
        TypeDescriptorNode typeDescriptor = formatNode(restDescriptorNode.typeDescriptor(), 0, 0);
        Token ellipsisToken = formatToken(restDescriptorNode.ellipsisToken(), env.trailingWS, env.trailingNL);
        return restDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withEllipsisToken(ellipsisToken)
                .apply();
    }

    @Override
    public DoubleGTTokenNode transform(DoubleGTTokenNode doubleGTTokenNode) {
        Token openGTToken = formatToken(doubleGTTokenNode.openGTToken(), 0, 0);
        Token endGTToken = formatToken(doubleGTTokenNode.endGTToken(), env.trailingWS, env.trailingNL);
        return doubleGTTokenNode.modify()
                .withOpenGTToken(openGTToken)
                .withEndGTToken(endGTToken)
                .apply();
    }

    @Override
    public TrippleGTTokenNode transform(TrippleGTTokenNode trippleGTTokenNode) {
        Token openGTToken = formatToken(trippleGTTokenNode.openGTToken(), 0, 0);
        Token middleGTToken = formatToken(trippleGTTokenNode.middleGTToken(), 0, 0);
        Token endGTToken = formatToken(trippleGTTokenNode.endGTToken(), env.trailingWS, env.trailingNL);
        return trippleGTTokenNode.modify()
                .withOpenGTToken(openGTToken)
                .withMiddleGTToken(middleGTToken)
                .withEndGTToken(endGTToken)
                .apply();
    }

    @Override
    public WaitActionNode transform(WaitActionNode waitActionNode) {
        Token waitKeyword = formatToken(waitActionNode.waitKeyword(), 1, 0);
        Node waitFutureExpr = formatNode(waitActionNode.waitFutureExpr(), env.trailingWS, env.trailingNL);
        return waitActionNode.modify()
                .withWaitKeyword(waitKeyword)
                .withWaitFutureExpr(waitFutureExpr)
                .apply();
    }

    @Override
    public WaitFieldsListNode transform(WaitFieldsListNode waitFieldsListNode) {
        Token openBrace = formatToken(waitFieldsListNode.openBrace(), 0, 0);
        SeparatedNodeList<Node> waitFields = formatSeparatedNodeList(waitFieldsListNode.waitFields(), 0, 0, 0, 0);
        Token closeBrace = formatToken(waitFieldsListNode.closeBrace(), env.trailingWS, env.trailingNL);
        return waitFieldsListNode.modify()
                .withOpenBrace(openBrace)
                .withWaitFields(waitFields)
                .withCloseBrace(closeBrace)
                .apply();
    }

    @Override
    public WaitFieldNode transform(WaitFieldNode waitFieldNode) {
        SimpleNameReferenceNode fieldName = formatNode(waitFieldNode.fieldName(), 0, 0);
        Token colon = formatToken(waitFieldNode.colon(), 1, 0);
        ExpressionNode waitFutureExpr = formatNode(waitFieldNode.waitFutureExpr(), env.trailingWS, env.trailingNL);
        return waitFieldNode.modify()
                .withFieldName(fieldName)
                .withColon(colon)
                .withWaitFutureExpr(waitFutureExpr)
                .apply();
    }

    @Override
    public AnnotAccessExpressionNode transform(AnnotAccessExpressionNode annotAccessExpressionNode) {
        ExpressionNode expression = formatNode(annotAccessExpressionNode.expression(), 0, 0);
        Token annotChainingToken = formatToken(annotAccessExpressionNode.annotChainingToken(), 0, 0);
        NameReferenceNode annotTagReference =
                formatNode(annotAccessExpressionNode.annotTagReference(), env.trailingWS, env.trailingNL);
        return annotAccessExpressionNode.modify()
                .withExpression(expression)
                .withAnnotChainingToken(annotChainingToken)
                .withAnnotTagReference(annotTagReference)
                .apply();
    }

    @Override
    public QueryActionNode transform(QueryActionNode queryActionNode) {
        int lineLength = env.lineLength;
        if (lineLength != 0) {
            // Set the indentation for statements starting with query expression nodes.
            indent();
        }

        QueryPipelineNode queryPipeline = formatNode(queryActionNode.queryPipeline(), 0, 1);
        Token doKeyword = formatToken(queryActionNode.doKeyword(), 1, 0);
        BlockStatementNode blockStatement = formatNode(queryActionNode.blockStatement(),
                env.trailingWS, env.trailingNL);
        if (lineLength != 0) {
            // Revert the indentation for statements starting with query expression nodes.
            unindent();
        }

        return queryActionNode.modify()
                .withQueryPipeline(queryPipeline)
                .withDoKeyword(doKeyword)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public OptionalFieldAccessExpressionNode transform(
            OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        ExpressionNode expression = formatNode(optionalFieldAccessExpressionNode.expression(), 0, 0);
        Token optionalChainingToken = formatToken(optionalFieldAccessExpressionNode.optionalChainingToken(), 0, 0);
        NameReferenceNode fieldName = formatNode(optionalFieldAccessExpressionNode.fieldName(),
                env.trailingWS, env.trailingNL);
        return optionalFieldAccessExpressionNode.modify()
                .withExpression(expression)
                .withOptionalChainingToken(optionalChainingToken)
                .withFieldName(fieldName)
                .apply();
    }

    @Override
    public ConditionalExpressionNode transform(ConditionalExpressionNode conditionalExpressionNode) {
        indent();
        ExpressionNode lhsExpression = formatNode(conditionalExpressionNode.lhsExpression(), 1, 0, !env.hasNewline);
        Token questionMarkToken = formatToken(conditionalExpressionNode.questionMarkToken(), 1, 0, !env.hasNewline);
        ExpressionNode middleExpression = formatNode(conditionalExpressionNode.middleExpression(),
                1, 0, !env.hasNewline);
        Token colonToken = formatToken(conditionalExpressionNode.colonToken(), 1, 0, !env.hasNewline);
        ExpressionNode endExpression = formatNode(conditionalExpressionNode.endExpression(),
                env.trailingWS, env.trailingNL, !env.hasNewline);
        unindent();

        return conditionalExpressionNode.modify()
                .withLhsExpression(lhsExpression)
                .withQuestionMarkToken(questionMarkToken)
                .withMiddleExpression(middleExpression)
                .withColonToken(colonToken)
                .withEndExpression(endExpression)
                .apply();
    }

    @Override
    public TransactionStatementNode transform(TransactionStatementNode transactionStatementNode) {
        Token transactionKeyword = formatToken(transactionStatementNode.transactionKeyword(), 1, 0);
        BlockStatementNode blockStatement;
        if (transactionStatementNode.onFailClause().isPresent()) {
            blockStatement = formatNode(transactionStatementNode.blockStatement(), 1, 0);
        } else {
            blockStatement = formatNode(transactionStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        OnFailClauseNode onFailClause = formatNode(transactionStatementNode.onFailClause().orElse(null),
                env.trailingWS, env.trailingNL);
        return transactionStatementNode.modify()
                .withTransactionKeyword(transactionKeyword)
                .withBlockStatement(blockStatement)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public RollbackStatementNode transform(RollbackStatementNode rollbackStatementNode) {
        Token rollbackKeyword;
        if (rollbackStatementNode.expression().isPresent()) {
            rollbackKeyword = formatToken(rollbackStatementNode.rollbackKeyword(), 1, 0);
        } else {
            rollbackKeyword = formatToken(rollbackStatementNode.rollbackKeyword(), 0, 0);
        }

        ExpressionNode expression = formatNode(rollbackStatementNode.expression().orElse(null), 0, 0);
        Token semicolon = formatToken(rollbackStatementNode.semicolon(), env.trailingWS, env.trailingNL);

        return rollbackStatementNode.modify()
                .withRollbackKeyword(rollbackKeyword)
                .withExpression(expression)
                .withSemicolon(semicolon)
                .apply();
    }

    @Override
    public RetryStatementNode transform(RetryStatementNode retryStatementNode) {
        Token retryKeyword;
        if (retryStatementNode.typeParameter().isPresent() || retryStatementNode.arguments().isPresent()) {
            retryKeyword = formatToken(retryStatementNode.retryKeyword(), 0, 0);
        } else {
            retryKeyword = formatToken(retryStatementNode.retryKeyword(), 1, 0);
        }

        TypeParameterNode typeParameter = formatNode(retryStatementNode.typeParameter().orElse(null), 1, 0);
        ParenthesizedArgList arguments = formatNode(retryStatementNode.arguments().orElse(null), 1, 0);
        StatementNode retryBody;
        if (retryStatementNode.onFailClause().isPresent()) {
            retryBody = formatNode(retryStatementNode.retryBody(), 1, 0);
        } else {
            retryBody = formatNode(retryStatementNode.retryBody(), env.trailingWS, env.trailingNL);
        }

        OnFailClauseNode onFailClause = formatNode(retryStatementNode.onFailClause().orElse(null),
                env.trailingWS, env.trailingNL);
        return retryStatementNode.modify()
                .withRetryKeyword(retryKeyword)
                .withTypeParameter(typeParameter)
                .withArguments(arguments)
                .withRetryBody(retryBody)
                .withOnFailClause(onFailClause)
                .apply();
    }

    @Override
    public CommitActionNode transform(CommitActionNode commitActionNode) {
        Token commitKeyword = formatToken(commitActionNode.commitKeyword(), env.trailingWS, env.trailingNL);
        return commitActionNode.modify()
                .withCommitKeyword(commitKeyword)
                .apply();
    }

    @Override
    public TransactionalExpressionNode transform(TransactionalExpressionNode transactionalExpressionNode) {
        Token transactionalKeyword = formatToken(transactionalExpressionNode.transactionalKeyword(),
                env.trailingWS, env.trailingNL);
        return transactionalExpressionNode.modify()
                .withTransactionalKeyword(transactionalKeyword)
                .apply();
    }

    @Override
    public TypeReferenceTypeDescNode transform(TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        NameReferenceNode typeRef = formatNode(typeReferenceTypeDescNode.typeRef(), env.trailingWS, env.trailingNL);
        return typeReferenceTypeDescNode.modify()
                .withTypeRef(typeRef)
                .apply();
    }

    @Override
    public DistinctTypeDescriptorNode transform(DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        Token distinctKeyword = formatToken(distinctTypeDescriptorNode.distinctKeyword(), 1, 0);
        TypeDescriptorNode typeDescriptor = formatNode(distinctTypeDescriptorNode.typeDescriptor(), env.trailingWS,
                env.trailingNL);
        return distinctTypeDescriptorNode.modify()
                .withDistinctKeyword(distinctKeyword)
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public OnConflictClauseNode transform(OnConflictClauseNode onConflictClauseNode) {
        Token onKeyword = formatToken(onConflictClauseNode.onKeyword(), 1, 0);
        Token conflictKeyword = formatToken(onConflictClauseNode.conflictKeyword(), 1, 0);
        ExpressionNode expression = formatNode(onConflictClauseNode.expression(), env.trailingWS, env.trailingNL);
        return onConflictClauseNode.modify()
                .withOnKeyword(onKeyword)
                .withConflictKeyword(conflictKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public LimitClauseNode transform(LimitClauseNode limitClauseNode) {
        Token limitKeyword = formatToken(limitClauseNode.limitKeyword(), 1, 0);
        ExpressionNode expression = formatNode(limitClauseNode.expression(), env.trailingWS, env.trailingNL);
        return limitClauseNode.modify()
                .withLimitKeyword(limitKeyword)
                .withExpression(expression)
                .apply();
    }

    @Override
    public JoinClauseNode transform(JoinClauseNode joinClauseNode) {
        Token outerKeyword = formatToken(joinClauseNode.outerKeyword().orElse(null), 1, 0);
        Token joinKeyword = formatToken(joinClauseNode.joinKeyword(), 1, 0);
        TypedBindingPatternNode typedBindingPattern = formatNode(joinClauseNode.typedBindingPattern(), 1, 0);
        Token inKeyword = formatToken(joinClauseNode.inKeyword(), 1, 0);
        ExpressionNode expression = formatNode(joinClauseNode.expression(), 1, 0);
        OnClauseNode joinOnCondition = formatNode(joinClauseNode.joinOnCondition(), env.trailingWS, env.trailingNL);

        return joinClauseNode.modify()
                .withOuterKeyword(outerKeyword)
                .withJoinKeyword(joinKeyword)
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(inKeyword)
                .withExpression(expression)
                .withJoinOnCondition(joinOnCondition)
                .apply();
    }

    @Override
    public OnClauseNode transform(OnClauseNode onClauseNode) {
        Token onKeyword = formatToken(onClauseNode.onKeyword(), 1, 0);
        ExpressionNode lhsExpr = formatNode(onClauseNode.lhsExpression(), 1, 0);
        Token equalsKeyword = formatToken(onClauseNode.equalsKeyword(), 1, 0);
        ExpressionNode rhsExpr = formatNode(onClauseNode.rhsExpression(), env.trailingWS, env.trailingNL);

        return onClauseNode.modify()
                .withOnKeyword(onKeyword)
                .withLhsExpression(lhsExpr)
                .withEqualsKeyword(equalsKeyword)
                .withRhsExpression(rhsExpr)
                .apply();
    }

    @Override
    public ListMatchPatternNode transform(ListMatchPatternNode listMatchPatternNode) {
        Token openBracket = formatToken(listMatchPatternNode.openBracket(), 0, 0);
        SeparatedNodeList<Node> matchPatterns = formatSeparatedNodeList(listMatchPatternNode.matchPatterns(), 0,
                0, 0, 0);
        Token closeBracket = formatToken(listMatchPatternNode.closeBracket(), env.trailingWS, env.trailingNL);

        return listMatchPatternNode.modify()
                .withOpenBracket(openBracket)
                .withMatchPatterns(matchPatterns)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public RestMatchPatternNode transform(RestMatchPatternNode restMatchPatternNode) {
        Token ellipsisToken = formatToken(restMatchPatternNode.ellipsisToken(), 0, 0);
        Token varKeywordToken = formatToken(restMatchPatternNode.varKeywordToken(), 1, 0);
        SimpleNameReferenceNode variableName = formatNode(restMatchPatternNode.variableName(),
                env.trailingWS, env.trailingNL);

        return restMatchPatternNode.modify()
                .withEllipsisToken(ellipsisToken)
                .withVarKeywordToken(varKeywordToken)
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public FieldMatchPatternNode transform(FieldMatchPatternNode fieldMatchPatternNode) {
        IdentifierToken fieldNameNode = formatNode(fieldMatchPatternNode.fieldNameNode(), 0, 0);
        Token colonToken = formatToken(fieldMatchPatternNode.colonToken(), 1, 0);
        Node matchPattern = formatNode(fieldMatchPatternNode.matchPattern(), env.trailingWS, env.trailingNL);

        return fieldMatchPatternNode.modify()
                .withFieldNameNode(fieldNameNode)
                .withColonToken(colonToken)
                .withMatchPattern(matchPattern)
                .apply();
    }

    @Override
    public ErrorMatchPatternNode transform(ErrorMatchPatternNode errorMatchPatternNode) {
        boolean hasTypeRef = errorMatchPatternNode.typeReference().isPresent();
        Token errorKeywordToken = formatToken(errorMatchPatternNode.errorKeyword(), hasTypeRef ? 1 : 0, 0);
        NameReferenceNode typeRef = formatNode(errorMatchPatternNode.typeReference().orElse(null), 0, 0);
        Token openParenthesisToken = formatToken(errorMatchPatternNode.openParenthesisToken(), 0, 0);
        SeparatedNodeList<Node> argListMatchPatternNode =
                formatSeparatedNodeList(errorMatchPatternNode.argListMatchPatternNode(), 0, 0, 0, 0);
        Token closeParenthesisToken = formatToken(errorMatchPatternNode.closeParenthesisToken(),
                env.trailingWS, env.trailingNL);

        return errorMatchPatternNode.modify()
                .withErrorKeyword(errorKeywordToken)
                .withTypeReference(typeRef)
                .withOpenParenthesisToken(openParenthesisToken)
                .withArgListMatchPatternNode(argListMatchPatternNode)
                .withCloseParenthesisToken(closeParenthesisToken)
                .apply();
    }

    @Override
    public NamedArgMatchPatternNode transform(NamedArgMatchPatternNode namedArgMatchPatternNode) {
        IdentifierToken identifier = formatToken(namedArgMatchPatternNode.identifier(), 1, 0);
        Token equalToken = formatToken(namedArgMatchPatternNode.equalToken(), 1, 0);
        Node matchPattern = formatNode(namedArgMatchPatternNode.matchPattern(), env.trailingWS, env.trailingNL);

        return namedArgMatchPatternNode.modify()
                .withIdentifier(identifier)
                .withEqualToken(equalToken)
                .withMatchPattern(matchPattern)
                .apply();
    }

    @Override
    public OrderByClauseNode transform(OrderByClauseNode orderByClauseNode) {
        Token orderKeyword = formatToken(orderByClauseNode.orderKeyword(), 1, 0);
        Token byKeyword = formatToken(orderByClauseNode.byKeyword(), 1, 0);
        SeparatedNodeList<OrderKeyNode> orderKey = formatSeparatedNodeList(orderByClauseNode.orderKey(),
                0, 0, env.trailingWS, env.trailingNL);

        return orderByClauseNode.modify()
                .withOrderKeyword(orderKeyword)
                .withByKeyword(byKeyword)
                .withOrderKey(orderKey)
                .apply();
    }

    @Override
    public OrderKeyNode transform(OrderKeyNode orderKeyNode) {
        ExpressionNode expression;
        if (orderKeyNode.orderDirection().isPresent()) {
            expression = formatNode(orderKeyNode.expression(), 1, 0);
        } else {
            expression = formatNode(orderKeyNode.expression(), env.trailingWS, env.trailingNL);
        }

        Token orderDirection =
                formatToken(orderKeyNode.orderDirection().orElse(null), env.trailingWS, env.trailingNL);
        return orderKeyNode.modify()
                .withExpression(expression)
                .withOrderDirection(orderDirection)
                .apply();
    }

    @Override
    public ClassDefinitionNode transform(ClassDefinitionNode classDefinitionNode) {
        MetadataNode metadata = formatNode(classDefinitionNode.metadata().orElse(null), 0, 1);
        Token visibilityQualifier = formatToken(classDefinitionNode.visibilityQualifier().orElse(null), 1, 0);
        NodeList<Token> classTypeQualifiers = formatNodeList(classDefinitionNode.classTypeQualifiers(), 1, 0, 1, 0);
        Token classKeyword = formatToken(classDefinitionNode.classKeyword(), 1, 0);
        Token className = formatToken(classDefinitionNode.className(), 1, 0);
        Token openBrace = formatToken(classDefinitionNode.openBrace(), 0, 1);

        indent();
        NodeList<Node> members = formatNodeList(classDefinitionNode.members(), 0, 1, 0, 1);
        unindent();
        Optional<Token> optSemicolon = classDefinitionNode.semicolonToken();
        Token closeBrace = optSemicolon.isPresent() ?
                formatToken(classDefinitionNode.closeBrace(), 0, 0) :
                formatToken(classDefinitionNode.closeBrace(), env.trailingWS, env.trailingNL);
        Token semicolon = formatToken(optSemicolon.orElse(null), env.trailingWS, env.trailingNL);

        return classDefinitionNode.modify()
                .withMetadata(metadata)
                .withVisibilityQualifier(visibilityQualifier)
                .withClassTypeQualifiers(classTypeQualifiers)
                .withClassKeyword(classKeyword)
                .withClassName(className)
                .withOpenBrace(openBrace)
                .withMembers(members)
                .withCloseBrace(closeBrace)
                .withSemicolonToken(semicolon)
                .apply();
    }

    @Override
    public BreakStatementNode transform(BreakStatementNode breakStatementNode) {
        Token breakToken = formatToken(breakStatementNode.breakToken(), 0, 0);
        Token semicolonToken = formatToken(breakStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);
        return breakStatementNode.modify()
                .withBreakToken(breakToken)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public NamedWorkerDeclarationNode transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(namedWorkerDeclarationNode.annotations(), 0, 1, 0, 1);
        Token transactionalKeyword =
                formatToken(namedWorkerDeclarationNode.transactionalKeyword().orElse(null), 1, 0);
        Token workerKeyword = formatToken(namedWorkerDeclarationNode.workerKeyword(), 1, 0);
        IdentifierToken workerName = formatToken(namedWorkerDeclarationNode.workerName(), 1, 0);
        Node returnTypeDesc = formatNode(namedWorkerDeclarationNode.returnTypeDesc().orElse(null), 1, 0);
        BlockStatementNode workerBody = formatNode(namedWorkerDeclarationNode.workerBody(), env.trailingWS,
                env.trailingNL);

        return namedWorkerDeclarationNode.modify()
                .withAnnotations(annotations)
                .withTransactionalKeyword(transactionalKeyword)
                .withWorkerKeyword(workerKeyword)
                .withWorkerName(workerName)
                .withReturnTypeDesc(returnTypeDesc)
                .withWorkerBody(workerBody)
                .apply();
    }

    @Override
    public NamedWorkerDeclarator transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        NodeList<StatementNode> workerInitStatements = formatNodeList(namedWorkerDeclarator.workerInitStatements(), 0,
                1, 0, 1);
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                formatNodeList(namedWorkerDeclarator.namedWorkerDeclarations(), 0, 1, 0, 1);
        return namedWorkerDeclarator.modify()
                .withWorkerInitStatements(workerInitStatements)
                .withNamedWorkerDeclarations(namedWorkerDeclarations)
                .apply();
    }

    @Override
    public ArrayTypeDescriptorNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        TypeDescriptorNode memberTypeDesc = formatNode(arrayTypeDescriptorNode.memberTypeDesc(), 0, 0);
        NodeList<ArrayDimensionNode> dimensions = formatNodeList(arrayTypeDescriptorNode.dimensions(), 0, 
                0, env.trailingWS, env.trailingNL);
        return arrayTypeDescriptorNode.modify()
                .withMemberTypeDesc(memberTypeDesc)
                .withDimensions(dimensions)
                .apply();
    }
    
    @Override
    public ArrayDimensionNode transform(ArrayDimensionNode arrayDimensionNode) {
        Token openBracket = formatToken(arrayDimensionNode.openBracket(), 0, 0);
        Node arrayLength = formatNode(arrayDimensionNode.arrayLength().orElse(null), 0, 0);
        Token closeBracket = formatToken(arrayDimensionNode.closeBracket(), env.trailingWS, env.trailingNL);
        return arrayDimensionNode.modify()
                .withOpenBracket(openBracket)
                .withArrayLength(arrayLength)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public XMLElementNode transform(XMLElementNode xMLElementNode) {
        XMLStartTagNode startTagNode = formatNode(xMLElementNode.startTag(), 0, 0);
        NodeList<XMLItemNode> content = formatNodeList(xMLElementNode.content(), 0, 0, 0, 0);
        XMLEndTagNode endTagNode = formatNode(xMLElementNode.endTag(), env.trailingWS, env.trailingNL);
        return xMLElementNode.modify()
                .withStartTag(startTagNode)
                .withContent(content)
                .withEndTag(endTagNode)
                .apply();
    }

    @Override
    public XMLStartTagNode transform(XMLStartTagNode xMLStartTagNode) {
        Token ltToken = formatToken(xMLStartTagNode.ltToken(), 0, 0);
        int nameTrailingWS = xMLStartTagNode.attributes().isEmpty() ? 0 : 1;
        XMLNameNode name = formatNode(xMLStartTagNode.name(), nameTrailingWS, 0);
        NodeList<XMLAttributeNode> attributes = formatNodeList(xMLStartTagNode.attributes(), 1, 0, 0, 0);
        Token getToken = formatToken(xMLStartTagNode.getToken(), env.trailingWS, env.trailingNL);

        return xMLStartTagNode.modify()
                .withLtToken(ltToken)
                .withName(name)
                .withAttributes(attributes)
                .withGetToken(getToken)
                .apply();
    }

    @Override
    public XMLEndTagNode transform(XMLEndTagNode xMLEndTagNode) {
        Token ltToken = formatToken(xMLEndTagNode.ltToken(), 0, 0);
        Token slashToken = formatToken(xMLEndTagNode.slashToken(), 0, 0);
        XMLNameNode name = formatNode(xMLEndTagNode.name(), 0, 0);
        Token getToken = formatToken(xMLEndTagNode.getToken(), env.trailingWS, env.trailingNL);

        return xMLEndTagNode.modify()
                .withLtToken(ltToken)
                .withSlashToken(slashToken)
                .withName(name)
                .withGetToken(getToken)
                .apply();
    }

    @Override
    public XMLAttributeNode transform(XMLAttributeNode xMLAttributeNode) {
        XMLNameNode attributeName = formatNode(xMLAttributeNode.attributeName(), 0, 0);
        Token equalToken = formatToken(xMLAttributeNode.equalToken(), 0, 0);
        XMLAttributeValue value = formatNode(xMLAttributeNode.value(), env.trailingWS, env.trailingNL);
        return xMLAttributeNode.modify()
                .withAttributeName(attributeName)
                .withEqualToken(equalToken)
                .withValue(value)
                .apply();
    }

    @Override
    public XMLAttributeValue transform(XMLAttributeValue xMLAttributeValue) {
        Token startQuote = formatToken(xMLAttributeValue.startQuote(), 0, 0);
        NodeList<Node> value = formatNodeList(xMLAttributeValue.value(), 0, 0, 0, 0);
        Token endQuote = formatToken(xMLAttributeValue.endQuote(), env.trailingWS, env.trailingNL);
        return xMLAttributeValue.modify()
                .withStartQuote(startQuote)
                .withValue(value)
                .withEndQuote(endQuote)
                .apply();
    }

    @Override
    public ClientResourceAccessActionNode transform(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        ExpressionNode expressionNode = formatNode(clientResourceAccessActionNode.expression(), 0, 0);
        Token rightArrowToken  = formatNode(clientResourceAccessActionNode.rightArrowToken(), 0, 0);
        Token slashToken;
        if (clientResourceAccessActionNode.resourceAccessPath().isEmpty() &&
                clientResourceAccessActionNode.methodName().isEmpty() &&
                clientResourceAccessActionNode.arguments().isEmpty()) {
            slashToken = formatNode(clientResourceAccessActionNode.slashToken(), env.trailingWS, env.trailingNL);
        } else {
            slashToken = formatNode(clientResourceAccessActionNode.slashToken(), 0, 0);
        }

        SeparatedNodeList<Node> resourceAccessPath;
        if (clientResourceAccessActionNode.methodName().isEmpty() &&
                clientResourceAccessActionNode.arguments().isEmpty()) {
            resourceAccessPath = formatSeparatedNodeList(clientResourceAccessActionNode.resourceAccessPath(), 0,
                    0, 0, 0, env.trailingWS, env.trailingNL);
        } else {
            resourceAccessPath = formatSeparatedNodeList(clientResourceAccessActionNode.resourceAccessPath(), 0,
                    0, 0, 0, 0, 0);
        }

        SimpleNameReferenceNode methodName;
        if (clientResourceAccessActionNode.arguments().isEmpty()) {
            methodName = formatNode(clientResourceAccessActionNode.methodName().orElse(null),
                    env.trailingWS, env.trailingNL);
        } else {
            methodName = formatNode(clientResourceAccessActionNode.methodName().orElse(null), 0, 0);
        }

        Token dotToken = formatToken(clientResourceAccessActionNode.dotToken().orElse(null), 0, 0);
        ParenthesizedArgList argumentNode =
                formatNode(clientResourceAccessActionNode.arguments().orElse(null), env.trailingWS, env.trailingNL);

        return clientResourceAccessActionNode.modify()
                .withExpression(expressionNode)
                .withRightArrowToken(rightArrowToken)
                .withSlashToken(slashToken)
                .withResourceAccessPath(resourceAccessPath)
                .withDotToken(dotToken)
                .withMethodName(methodName)
                .withArguments(argumentNode)
                .apply();
    }

    @Override
    public ComputedResourceAccessSegmentNode transform(
            ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
        Token openBracket = formatToken(computedResourceAccessSegmentNode.openBracketToken(), 0, 0);
        ExpressionNode expressionNode = formatNode(computedResourceAccessSegmentNode.expression(), 0, 0);
        Token closeBracket = formatToken(computedResourceAccessSegmentNode.closeBracketToken(), env.trailingWS,
                env.trailingNL);

        return computedResourceAccessSegmentNode.modify()
                .withOpenBracketToken(openBracket)
                .withExpression(expressionNode)
                .withCloseBracketToken(closeBracket)
                .apply();
    }

    @Override
    public ResourceAccessRestSegmentNode transform(ResourceAccessRestSegmentNode resourceAccessRestSegmentNode) {
        Token openBracket = formatToken(resourceAccessRestSegmentNode.openBracketToken(), 0, 0);
        Token ellipsis = formatToken(resourceAccessRestSegmentNode.ellipsisToken(), 0, 0);
        ExpressionNode expressionNode = formatNode(resourceAccessRestSegmentNode.expression(), 0, 0);
        Token closeBracket = formatToken(resourceAccessRestSegmentNode.closeBracketToken(), env.trailingWS,
                env.trailingNL);

        return resourceAccessRestSegmentNode.modify()
                .withOpenBracketToken(openBracket)
                .withEllipsisToken(ellipsis)
                .withExpression(expressionNode)
                .withCloseBracketToken(closeBracket)
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        return formatToken(identifier, env.trailingWS, env.trailingNL);
    }

    @Override
    public Token transform(Token token) {
        return formatToken(token, env.trailingWS, env.trailingNL);
    }

    // ------------------------------------- Set of private helper methods -------------------------------------

    /**
     * Format a node.
     *
     * @param <T> Type of the node
     * @param node Node to be formatted
     * @param trailingWS Number of single-length spaces to be added after the node
     * @param trailingNL Number of newlines to be added after the node
     * @param preserveIndentation Preserve user-defined indentation
     * @return Formatted node
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T formatNode(T node, int trailingWS, int trailingNL, Boolean preserveIndentation) {
        try {
            if (node == null) {
                return node;
            }

            if (!isInlineRange(node, lineRange)) {
                checkForNewline(node);
                return node;
            }

            int prevTrailingNL = env.trailingNL;
            int prevTrailingWS = env.trailingWS;
            env.trailingNL = trailingNL;
            env.trailingWS = trailingWS;
            if (preserveIndentation != null) {
                env.preserveIndentation = preserveIndentation;
            }

            // Cache the current node and parent before format.
            // Because reference to the nodes will change after modifying.
            T oldNode = node;
            Node parent = node.parent();

            node = (T) node.apply(this);

            if (options.getLineWrapping() && shouldWrapLine(oldNode, parent)) {
                node = wrapLine(oldNode, parent);
            }

            env.trailingNL = prevTrailingNL;
            env.trailingWS = prevTrailingWS;
        } catch (Exception e) {
            checkForNewline(node);
            LOGGER.error(String.format("Error while formatting [kind: %s] [line: %s] [column:%s]: %s",
                    node.kind().name(), node.lineRange().startLine().line() + 1,
                    node.lineRange().startLine().offset(), e));
        }

        return node;
    }

    /**
     * Format a node.
     *
     * @param <T> Type of the node
     * @param node Node to be formatted
     * @param trailingWS Number of single-length spaces to be added after the node
     * @param trailingNL Number of newlines to be added after the node
     * @return Formatted node
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T formatNode(T node, int trailingWS, int trailingNL) {
        return formatNode(node, trailingWS, trailingNL, null);
    }

    /**
     * Format a token.
     *
     * @param <T> Type of the token
     * @param token Token to be formatted
     * @param trailingWS Number of single-length spaces to be added after the token
     * @param trailingNL Number of newlines to be added after the token
     * @param preserveIndentation Preserve user-defined indentation
     * @return Formatted token
     */
    private <T extends Token> T formatToken(T token, int trailingWS, int trailingNL, Boolean preserveIndentation) {
        try {
            if (token == null) {
                return token;
            }

            if (!isInlineRange(token, lineRange)) {
                checkForNewline(token);
                return token;
            }

            int prevTrailingNL = env.trailingNL;
            int prevTrailingWS = env.trailingWS;

            // Trailing newlines can be at-most 1. Rest will go as newlines for the next token
            env.trailingNL = trailingNL > 0 ? 1 : 0;
            env.trailingWS = trailingWS;
            if (preserveIndentation != null) {
                env.preserveIndentation = preserveIndentation;
            }

            token = formatTokenInternal(token);

            // Set the leading newlines for the next token
            env.leadingNL = trailingNL > 0 ? trailingNL - 1 : 0;

            // If this node has a trailing new line, then the next immediate token
            // will become the first token the next line
            env.hasNewline = trailingNL > 0 || hasTrailingNL(token);
            env.trailingNL = prevTrailingNL;
            env.trailingWS = prevTrailingWS;
            env.prevTokensTrailingWS = trailingWS;
        } catch (Exception e) {
            checkForNewline(token);
            LOGGER.error(String.format("Error while formatting [kind: %s] [line: %s] [column:%s]: %s",
                    token.kind().name(), token.lineRange().startLine().line() + 1,
                    token.lineRange().startLine().offset(), e));
        }

        return token;
    }

    /**
     * Format a token.
     *
     * @param <T> Type of the token
     * @param token Token to be formatted
     * @param trailingWS Number of single-length spaces to be added after the token
     * @param trailingNL Number of newlines to be added after the token
     * @return Formatted token
     */
    private <T extends Token> T formatToken(T token, int trailingWS, int trailingNL) {
        return formatToken(token, trailingWS, trailingNL, null);
    }

    protected <T extends Node> NodeList<T> formatModuleMembers(NodeList<T> members) {
        if (members.isEmpty()) {
            return members;
        }

        boolean nodeModified = false;
        int size = members.size();
        Node[] newNodes = new Node[size];
        for (int index = 0; index < size; index++) {
            T currentMember = members.get(index);
            Node nextMember = null;
            if (index < size - 1) {
                nextMember = members.get(index + 1);
            }

            // We need to do this check, because different kinds of children needs
            // different number of newlines in-between.
            int itemTrailingNL = 1;
            if (isMultilineModuleMember(currentMember) || isMultilineModuleMember(nextMember)) {
                itemTrailingNL++;
            }

            T newMember = formatListItem(0, itemTrailingNL, 0, 1, size, index,
                    currentMember);
            if (currentMember != newMember) {
                nodeModified = true;
            }
            newNodes[index] = newMember;
        }

        if (!nodeModified) {
            return members;
        }

        return (NodeList<T>) NodeFactory.createNodeList(newNodes);
    }

    private <T extends Node> boolean isMultilineModuleMember(T node) {
        if (node == null) {
            return false;
        }

        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case CLASS_DEFINITION:
            case SERVICE_DECLARATION:
            case TYPE_DEFINITION:
            case ENUM_DECLARATION:
            case ANNOTATION_DECLARATION:
                return true;
            case MODULE_VAR_DECL:
            case MODULE_XML_NAMESPACE_DECLARATION:
            case CONST_DECLARATION:
            case LISTENER_DECLARATION:
            default:
                return false;
        }
    }

    /**
     * Format a list of nodes.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item of the list
     * @param itemTrailingNL Number of newlines to be added after each item of the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item of the list
     * @param listTrailingNL Number of newlines to be added after the last item of the list
     * @return Formatted node list
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> NodeList<T> formatNodeList(NodeList<T> nodeList,
                                                          int itemTrailingWS,
                                                          int itemTrailingNL,
                                                          int listTrailingWS,
                                                          int listTrailingNL) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        int size = nodeList.size();
        Node[] newNodes = new Node[size];
        for (int index = 0; index < size; index++) {
            T oldNode = nodeList.get(index);
            T newNode;
            newNode = formatListItem(itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, size, index,
                    oldNode);

            if (oldNode != newNode) {
                nodeModified = true;
            }
            newNodes[index] = newNode;
        }

        if (!nodeModified) {
            return nodeList;
        }

        return (NodeList<T>) NodeFactory.createNodeList(newNodes);
    }

    private <T extends Node> T formatListItem(int itemTrailingWS, int itemTrailingNL, int listTrailingWS,
                                              int listTrailingNL, int size, int index, T oldNode) {
        T newNode;
        if (index == size - 1) {
            // This is the last item of the list. Trailing WS and NL for the last item on the list
            // should be the WS and NL of the entire list
            newNode = formatNode(oldNode, listTrailingWS, listTrailingNL);
        } else {
            newNode = formatNode(oldNode, itemTrailingWS, itemTrailingNL);
        }
        return newNode;
    }

    /**
     * Format a delimited list of nodes. This method assumes the delimiters are followed by a single whitespace
     * character only.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item in the list
     * @param itemTrailingNL Number of newlines to be added after each item in the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item in the list
     * @param listTrailingNL Number of newlines to be added after the last item in the list
     * @return Formatted node list
     */
    protected <T extends Node> SeparatedNodeList<T> formatSeparatedNodeList(SeparatedNodeList<T> nodeList,
                                                                            int itemTrailingWS,
                                                                            int itemTrailingNL,
                                                                            int listTrailingWS,
                                                                            int listTrailingNL) {
        return formatSeparatedNodeList(nodeList, itemTrailingWS, itemTrailingNL, 1, 0, listTrailingWS, listTrailingNL);
    }

    /**
     * Format a delimited list of nodes. This method assumes the delimiters are followed by a single whitespace
     * character only.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item in the list
     * @param itemTrailingNL Number of newlines to be added after each item in the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item in the list
     * @param listTrailingNL Number of newlines to be added after the last item in the list
     * @param allowMultiLineParams Supports the indentation of multiline parenthesized arguments
     * @return Formatted node list
     */
    protected <T extends Node> SeparatedNodeList<T> formatSeparatedNodeList(SeparatedNodeList<T> nodeList,
                                                                            int itemTrailingWS,
                                                                            int itemTrailingNL,
                                                                            int listTrailingWS,
                                                                            int listTrailingNL,
                                                                            boolean allowMultiLineParams) {
        return formatSeparatedNodeList(nodeList, itemTrailingWS, itemTrailingNL, 1, 0, listTrailingWS,
                listTrailingNL, allowMultiLineParams);
    }

    /**
     * Format a delimited list of nodes.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item in the list
     * @param itemTrailingNL Number of newlines to be added after each item in the list
     * @param separatorTrailingWS Number of single-length spaces to be added after each separator in the list
     * @param separatorTrailingNL Number of newlines to be added after each separator in the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item in the list
     * @param listTrailingNL Number of newlines to be added after the last item in the list
     * @return Formatted node list
     */
    protected <T extends Node> SeparatedNodeList<T> formatSeparatedNodeList(SeparatedNodeList<T> nodeList,
                                                                            int itemTrailingWS,
                                                                            int itemTrailingNL,
                                                                            int separatorTrailingWS,
                                                                            int separatorTrailingNL,
                                                                            int listTrailingWS,
                                                                            int listTrailingNL) {
        return formatSeparatedNodeList(nodeList,
                itemTrailingWS,
                itemTrailingNL,
                separatorTrailingWS,
                separatorTrailingNL,
                listTrailingWS,
                listTrailingNL,
                false);
    }

    /**
     * Format a delimited list of nodes.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item in the list
     * @param itemTrailingNL Number of newlines to be added after each item in the list
     * @param separatorTrailingWS Number of single-length spaces to be added after each separator in the list
     * @param separatorTrailingNL Number of newlines to be added after each separator in the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item in the list
     * @param listTrailingNL Number of newlines to be added after the last item in the list
     * @param allowInAndMultiLine Allow both inline and multiline formatting at the same time
     * @return Formatted node list
     */
    protected <T extends Node> SeparatedNodeList<T> formatSeparatedNodeList(SeparatedNodeList<T> nodeList,
                                                                            int itemTrailingWS,
                                                                            int itemTrailingNL,
                                                                            int separatorTrailingWS,
                                                                            int separatorTrailingNL,
                                                                            int listTrailingWS,
                                                                            int listTrailingNL,
                                                                            boolean allowInAndMultiLine) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        int size = nodeList.size();
        Node[] newNodes = new Node[size * 2 - 1];

        for (int index = 0; index < size; index++) {
            T oldNode = nodeList.get(index);
            if (allowInAndMultiLine) {
                if (hasNonWSMinutiae(oldNode.trailingMinutiae())) {
                    indent();
                }
            }
            T newNode = formatListItem(itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, size,
                    index, oldNode);
            if (allowInAndMultiLine) {
                if (hasNonWSMinutiae(oldNode.trailingMinutiae())) {
                    unindent();
                }
            }
            newNodes[2 * index] = newNode;
            if (oldNode != newNode) {
                nodeModified = true;
            }

            if (index == nodeList.size() - 1) {
                break;
            }

            Token oldSeparator = nodeList.getSeparator(index);
            if (allowInAndMultiLine) {
                separatorTrailingWS = 0;
                separatorTrailingNL = 0;
                if (hasNonWSMinutiae(oldSeparator.trailingMinutiae())) {
                    separatorTrailingNL++;
                } else {
                    separatorTrailingWS++;
                }
            }
            Token newSeparator = formatToken(oldSeparator, separatorTrailingWS, separatorTrailingNL);
            newNodes[(2 * index) + 1] = newSeparator;

            if (oldSeparator != newSeparator) {
                nodeModified = true;
            }

        }

        if (!nodeModified) {
            return nodeList;
        }

        return NodeFactory.createSeparatedNodeList(newNodes);
    }

    /**
     * Format a token.
     *
     * @param <T> Type of the token
     * @param token Token to be formatted
     * @return Formatted token
     */
    @SuppressWarnings("unchecked")
    private <T extends Token> T formatTokenInternal(T token) {
        MinutiaeList newLeadingMinutiaeList = getLeadingMinutiae(token);
        env.lineLength += token.text().length();
        MinutiaeList newTrailingMinutiaeList = getTrailingMinutiae(token);

        if (token.isMissing()) {
            return (T) NodeFactory.createMissingToken(token.kind(), newLeadingMinutiaeList, newTrailingMinutiaeList);
        }

        return (T) token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private <T extends Node> void checkForNewline(T node) {
        env.hasNewline = false;
        for (Minutiae minutiae : node.trailingMinutiae()) {
            if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                env.hasNewline = true;
                break;
            }
        }

        // Set the line length for the next line.
        if (env.hasNewline) {
            env.lineLength = 0;
        } else {
            env.lineLength = node.location().lineRange().endLine().offset();
        }
    }

    /**
     * Check whether the current line should be wrapped.
     *
     * @param node Node that is being formatted
     * @param parent
     * @return Flag indicating whether to wrap the current line or not
     */
    private boolean shouldWrapLine(Node node, Node parent) {
        boolean exceedsColumnLimit = env.lineLength > options.getColumnLimit();
        boolean descendantNeedWrapping = env.nodeToWrap == node;
        if (!exceedsColumnLimit && !descendantNeedWrapping) {
            return false;
        }

        // Currently wrapping a line is supported at following levels:
        SyntaxKind kind = node.kind();
        switch (kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                if (node.parent().kind() == SyntaxKind.ANNOTATION) {
                    break;
                }
                return true;

            // Parameters
            case DEFAULTABLE_PARAM:
            case REQUIRED_PARAM:
            case REST_PARAM:

                // Func-call arguments
            case POSITIONAL_ARG:
            case NAMED_ARG:
            case REST_ARG:

            case RETURN_TYPE_DESCRIPTOR:
            case ANNOTATION_ATTACH_POINT:
                return true;

            // Template literals are multi line tokens, and newline are
            // part of the content. Hence we cannot wrap those.
            case XML_TEMPLATE_EXPRESSION:
            case STRING_TEMPLATE_EXPRESSION:
            case TEMPLATE_STRING:
                break;
            default:
                // Expressions
                if (SyntaxKind.BINARY_EXPRESSION.compareTo(kind) <= 0 &&
                        SyntaxKind.OBJECT_CONSTRUCTOR.compareTo(kind) >= 0) {
                    return true;
                }

                // Everything else is not supported
                break;
        }

        // We reach here, if the current node exceeds the limit, but it is
        // not a wrapping-point. Then we ask the parent to wrap itself.
        env.nodeToWrap = parent;

        return false;
    }

    /**
     * Wrap the node. This is equivalent to adding a newline before the node and re-formatting the node. Wrapped content
     * will start from the current level of indentation.
     *
     * @param <T> Node type
     * @param node Node to be wrapped
     * @param parent
     * @return Wrapped node
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T wrapLine(T node, Node parent) {
        env.leadingNL += 1;
        env.lineLength = 0;
        env.hasNewline = true;
        node = (T) node.apply(this);

        // Sometimes wrapping the current node wouldn't be enough. Therefore, if the column
        // length exceeds even after wrapping current node, then ask the parent node to warp.
        if (env.lineLength > options.getColumnLimit()) {
            env.nodeToWrap = parent;
        } else {
            env.nodeToWrap = null;
        }

        return node;
    }

    /**
     * Get leading minutiae.
     *
     * @return Leading minutiae list
     */
    private MinutiaeList getLeadingMinutiae(Token token) {
        List<Minutiae> leadingMinutiae = new ArrayList<>();

        int consecutiveNewlines = 0;
        Minutiae prevMinutiae = null;
        if (env.hasNewline) {
            // 'hasNewlines == true' means a newline has already been added.
            // Therefore, increase the 'consecutiveNewlines' count
            consecutiveNewlines++;

            for (int i = 0; i < env.leadingNL; i++) {
                prevMinutiae = getNewline();
                leadingMinutiae.add(prevMinutiae);
                consecutiveNewlines++;
            }
        }

        // Preserve the necessary leading minutiae coming from the original token
        for (Minutiae minutiae : token.leadingMinutiae()) {
            switch (minutiae.kind()) {
                case END_OF_LINE_MINUTIAE:
                    if (consecutiveNewlines <= 1 && env.hasPreservedNewline) {
                        consecutiveNewlines++;
                        leadingMinutiae.add(getNewline());
                        break;
                    }

                    continue;
                case WHITESPACE_MINUTIAE:
                    if (!shouldAddWS(prevMinutiae) && !env.preserveIndentation) {
                        // Shouldn't update the prevMinutiae
                        continue;
                    }
                    if (env.preserveIndentation) {
                        addWhitespace(getPreservedIndentation(token), leadingMinutiae);
                    } else {
                        addWhitespace(1, leadingMinutiae);
                    }
                    break;
                case COMMENT_MINUTIAE:
                    if (consecutiveNewlines == 0) {
                        // A comment without a leading newline is only possible if there is an explicit newline added
                        // by the user. So, it is being honored here.
                        leadingMinutiae.add(getNewline());
                    }
                    if (!env.preserveIndentation) {
                        int indentation = env.currentIndentation;
                        if (isClosingTypeToken(token)) {
                            indentation += options.getTabSize();
                        }
                        // Then add padding to match the current indentation level
                        addWhitespace(indentation, leadingMinutiae);
                    }

                    leadingMinutiae.add(minutiae);
                    consecutiveNewlines = 0;
                    break;
                case INVALID_TOKEN_MINUTIAE_NODE:
                case INVALID_NODE_MINUTIAE:
                default:
                    consecutiveNewlines = 0;
                    leadingMinutiae.add(minutiae);
                    break;
            }

            prevMinutiae = minutiae;
        }

        // token.isMission() issue has to be discussed.
        if (consecutiveNewlines > 0 && !env.preserveIndentation) {
            addWhitespace(env.currentIndentation, leadingMinutiae);
        }

        MinutiaeList newLeadingMinutiaeList = NodeFactory.createMinutiaeList(leadingMinutiae);
        preserveIndentation(false);
        return newLeadingMinutiaeList;
    }

    /**
     * Add whitespace of a given length to a minutiae list.
     *
     * @param wsLength Length of the whitespace
     * @param minutiaeList List of minutiae to add the whitespace
     */
    private void addWhitespace(int wsLength, List<Minutiae> minutiaeList) {
        if (wsLength <= 0) {
            return;
        }

        String wsContent = getWSContent(wsLength);
        minutiaeList.add(NodeFactory.createWhitespaceMinutiae(wsContent));
    }

    /**
     * Check whether a whitespace needs to be added.
     *
     * @param prevMinutiae Minutiae that precedes the current token
     * @return <code>true</code> if a whitespace needs to be added. <code>false</code> otherwise
     */
    private boolean shouldAddWS(Minutiae prevMinutiae) {
        if (prevMinutiae == null) {
            return false;
        }

        return prevMinutiae.kind() == SyntaxKind.INVALID_TOKEN_MINUTIAE_NODE ||
                prevMinutiae.kind() == SyntaxKind.INVALID_NODE_MINUTIAE;
    }

    /**
     * Get trailing minutiae.
     *
     * @return Trailing minutiae list
     */
    private MinutiaeList getTrailingMinutiae(Token token) {
        List<Minutiae> trailingMinutiae = new ArrayList<>();
        Minutiae prevMinutiae = null;

        // If the token is a missing token and if the previous token has trailing whitespaces or new lines,
        // new whitespaces are not added.
        if (env.trailingWS > 0 && !(token.isMissing() && (env.prevTokensTrailingWS > 0 ||
                env.prevTokensTrailingNL > 0))) {
            addWhitespace(env.trailingWS, trailingMinutiae);
        }

        // Preserve the necessary trailing minutiae coming from the original token
        int consecutiveNewlines = 0;
        for (Minutiae minutiae : token.trailingMinutiae()) {
            switch (minutiae.kind()) {
                case END_OF_LINE_MINUTIAE:
                    preserveIndentation(true);
                    removeTrailingWS(trailingMinutiae);
                    trailingMinutiae.add(getNewline());
                    consecutiveNewlines++;
                    break;
                case WHITESPACE_MINUTIAE:
                    if (!shouldAddWS(prevMinutiae)) {
                        // Shouldn't update the prevMinutiae
                        continue;
                    }

                    addWhitespace(env.trailingWS, trailingMinutiae);
                    break;
                case COMMENT_MINUTIAE:
                    addWhitespace(1, trailingMinutiae);
                    trailingMinutiae.add(minutiae);
                    consecutiveNewlines = 0;
                    break;
                case INVALID_TOKEN_MINUTIAE_NODE:
                case INVALID_NODE_MINUTIAE:
                default:
                    trailingMinutiae.add(minutiae);
                    consecutiveNewlines = 0;
                    break;
            }

            prevMinutiae = minutiae;
        }

        if (consecutiveNewlines == 0 && env.trailingNL > 0 && !token.isMissing() && env.hasPreservedNewline) {
            trailingMinutiae.add(getNewline());
        }
        env.prevTokensTrailingNL = consecutiveNewlines;
        MinutiaeList newTrailingMinutiaeList = NodeFactory.createMinutiaeList(trailingMinutiae);
        return newTrailingMinutiaeList;
    }

    private Minutiae getNewline() {
        // reset the line length
        env.lineLength = 0;
        return NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL);
    }

    private List<Minutiae> removeTrailingWS(List<Minutiae> trailingMinutiae) {
        int minutiaeCount = trailingMinutiae.size();
        for (int i = minutiaeCount - 1; i > -1; i--) {
            if (trailingMinutiae.get(i).kind() == SyntaxKind.WHITESPACE_MINUTIAE) {
                trailingMinutiae.remove(i);
            } else {
                return trailingMinutiae;
            }
        }
        return trailingMinutiae;
    }

    /**
     * Indent the code by the number of white-spaces defined by tab-size.
     */
    private void indent() {
        indent(1);
    }

    /**
     * Indent the code by the number of white-spaces defined by tab-size.
     *
     * @param step Number of tabs.
     */
    private void indent(int step) {
        env.currentIndentation += (options.getTabSize() * step);
    }

    /**
     * Undo the indentation of the code by the number of white-spaces defined by tab-size.
     */
    private void unindent() {
        unindent(1);
    }

    /**
     * Undo the indentation of the code by the number of white-spaces defined by tab-size.
     *
     * @param step Number of tabs.
     */
    private void unindent(int step) {
        if (env.currentIndentation < (options.getTabSize() * step)) {
            env.currentIndentation = 0;
            return;
        }

        env.currentIndentation -= (options.getTabSize() * step);
    }

    /**
     * Set the indentation for the code to follow.
     *
     * @param value Number of characters to set the indentation from the start of the line.
     */
    private void setIndentation(int value) {
        env.currentIndentation = value;
    }

    /**
     * Set the flag for setting preserve indentations.
     *
     * @param value boolean true for setting preserve indentations.
     */
    private void preserveIndentation(boolean value) {
        if (value) {
            if (env.trailingNL < 1) {
                env.preserveIndentation = value;
            }
        } else {
            env.preserveIndentation = value;
        }
    }

    /**
     * Get the user defined indentation of a position aligned to the closest tab.
     *
     * @param token token of which the indentation is required.
     */
    private int getPreservedIndentation(Token token) {
        int position = token.lineRange().startLine().offset();
        int offset = position % 4;
        if (offset != 0) {
            if (offset > 2) {
                position = position + 4 - offset;
            } else {
                position = position - offset;
            }
        }
        return position;
    }

    /**
     * Set the flag for setting preserve new line for currently formatting token.
     *
     * @param value boolean true for setting new line.
     */
    private void setPreserveNewline(boolean value) {
        env.hasPreservedNewline = value;
    }

    /**
     * Set the flag for setting inline annotations.
     *
     * @param value boolean true for setting inline annotations.
     */
    private void setInlineAnnotation(boolean value) {
        env.inlineAnnotation = value;
    }

    private String getWSContent(int count) {
        // for all whitespaces, increase the line length
        env.lineLength += count;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(options.getWSCharacter());
        }

        return sb.toString();
    }

    /**
     * Check whether the node needs to be expanded in to multiple lines.
     *
     * @param node Node
     * @return <code>true</code> If the node needs to be expanded in to multiple lines.
     *         <code>false</code> otherwise
     */
    private boolean shouldExpand(Node node) {
        switch (node.kind()) {
            case OBJECT_TYPE_DESC:
                ObjectTypeDescriptorNode objectTypeDesc = (ObjectTypeDescriptorNode) node;
                if (objectTypeDesc.parent().kind() == SyntaxKind.TYPE_DEFINITION) {
                    return true;
                }

                if (hasNonWSMinutiae(objectTypeDesc.openBrace().trailingMinutiae())
                        || hasNonWSMinutiae(objectTypeDesc.closeBrace().leadingMinutiae())) {
                    return true;
                }

                NodeList<Node> objectTypeDescMembers = objectTypeDesc.members();
                return shouldExpandObjectMembers(objectTypeDescMembers);
            case OBJECT_CONSTRUCTOR:
                ObjectConstructorExpressionNode objectConstructor = (ObjectConstructorExpressionNode) node;
                if (hasNonWSMinutiae(objectConstructor.openBraceToken().trailingMinutiae())
                        || hasNonWSMinutiae(objectConstructor.closeBraceToken().leadingMinutiae())) {
                    return true;
                }

                NodeList<Node> objectConstructorMembers = objectConstructor.members();
                return shouldExpandObjectMembers(objectConstructorMembers);
            case RECORD_TYPE_DESC:
                if (options.getForceFormattingOptions().getForceFormatRecordFields()) {
                    return true;
                }

                RecordTypeDescriptorNode recordTypeDesc = (RecordTypeDescriptorNode) node;
                if (recordTypeDesc.parent().kind() == SyntaxKind.TYPE_DEFINITION) {
                    return true;
                }

                if (hasNonWSMinutiae(recordTypeDesc.bodyStartDelimiter().trailingMinutiae())
                        || hasNonWSMinutiae(recordTypeDesc.bodyEndDelimiter().leadingMinutiae())) {
                    return true;
                }

                for (Node field : recordTypeDesc.fields()) {
                    if (hasNonWSMinutiae(field.leadingMinutiae()) || hasNonWSMinutiae(field.trailingMinutiae())
                            || field.toSourceCode().contains(System.lineSeparator())) {
                        return true;
                    }
                }
                return false;
            case LET_EXPRESSION:
                LetExpressionNode letExpressionNode = (LetExpressionNode) node;
                SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                        letExpressionNode.letVarDeclarations();
                LetVariableDeclarationNode lastLetVarDeclarationNode =
                        letVarDeclarations.get(letVarDeclarations.size() - 1);

                return hasNonWSMinutiae(lastLetVarDeclarationNode.trailingMinutiae())
                        || lastLetVarDeclarationNode.toSourceCode().contains(System.lineSeparator());
            case ENUM_DECLARATION:
                EnumDeclarationNode enumDeclarationNode = (EnumDeclarationNode) node;
                SeparatedNodeList<Node> enumMemberList = enumDeclarationNode.enumMemberList();
                for (int index = 0; index < enumMemberList.size() - 1; index++) {
                    Token separator = enumMemberList.getSeparator(index);
                    if (hasNonWSMinutiae(separator.leadingMinutiae()) || hasNonWSMinutiae(separator.trailingMinutiae())
                            || separator.toSourceCode().contains(System.lineSeparator())) {
                        return true;
                    }
                }
                return false;
            case TABLE_CONSTRUCTOR:
                TableConstructorExpressionNode tableConstructor = (TableConstructorExpressionNode) node;
                SeparatedNodeList<Node> rows = tableConstructor.rows();
                return (rows.size() > 1 || (rows.size() == 1) && shouldExpand(rows.get(0)));
            case MAPPING_CONSTRUCTOR:
                MappingConstructorExpressionNode mappingConstructorExpressionNode =
                        (MappingConstructorExpressionNode) node;
                return mappingConstructorExpressionNode.toSourceCode().trim().contains(System.lineSeparator());
            case LIST_CONSTRUCTOR:
                ListConstructorExpressionNode listConstructorExpressionNode = (ListConstructorExpressionNode) node;
                return listConstructorExpressionNode.toSourceCode().trim().contains(System.lineSeparator());
            default:
                return false;
        }
    }

    private boolean shouldExpandObjectMembers(NodeList<Node> members) {
        for (Node member : members) {
            if (member.kind() == SyntaxKind.METHOD_DECLARATION
                    || hasNonWSMinutiae(member.leadingMinutiae())
                    || hasNonWSMinutiae(member.trailingMinutiae())
                    || member.toSourceCode().contains(System.lineSeparator())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a minutiae list contains any minutiae other than whitespaces.
     *
     * @param minutiaeList List of minutiae to check.
     * @return <code>true</code> If the list contains any minutiae other than whitespaces. <code>false</code> otherwise
     */
    private boolean hasNonWSMinutiae(MinutiaeList minutiaeList) {
        for (Minutiae minutiae : minutiaeList) {
            switch (minutiae.kind()) {
                case WHITESPACE_MINUTIAE:
                    continue;
                case COMMENT_MINUTIAE:
                case INVALID_TOKEN_MINUTIAE_NODE:
                case INVALID_NODE_MINUTIAE:
                case END_OF_LINE_MINUTIAE:
                default:
                    return true;
            }
        }

        return false;
    }

    private boolean isClosingTypeToken(Token token) {
        switch (token.kind()) {
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether a token has trailing newlines.
     *
     * @param token The token
     * @return <code>true</code> if a trailing newline is present. <code>false</code> otherwise
     */
    private boolean hasTrailingNL(Token token) {
        for (Minutiae minutiae : token.trailingMinutiae()) {
            if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                return true;
            }
        }
        return false;
    }
}
