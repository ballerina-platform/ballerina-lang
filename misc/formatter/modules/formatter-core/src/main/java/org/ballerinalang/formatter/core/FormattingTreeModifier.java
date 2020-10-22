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
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
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
import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.DocumentationReferenceNode;
import io.ballerina.compiler.syntax.tree.DoubleGTTokenNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternNode;
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
import io.ballerina.compiler.syntax.tree.ImportVersionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
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
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
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
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
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
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyncSendActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateMemberNode;
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
import io.ballerina.compiler.syntax.tree.TypedescTypeDescriptorNode;
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
import io.ballerina.compiler.syntax.tree.XmlTypeDescriptorNode;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.formatter.core.FormatterUtils.isInLineRange;

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
        if (functionDefinitionNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(functionDefinitionNode.metadata().get(), 0, 1);
            functionDefinitionNode = functionDefinitionNode.modify().withMetadata(metadata).apply();
        }

        NodeList<Token> qualifierList = formatNodeList(functionDefinitionNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword = formatToken(functionDefinitionNode.functionKeyword(), 1, 0);
        IdentifierToken functionName = formatToken(functionDefinitionNode.functionName(), 0, 0);
        FunctionSignatureNode functionSignatureNode = formatNode(functionDefinitionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBodyNode =
                formatNode(functionDefinitionNode.functionBody(), env.trailingWS, env.trailingNL);

        return functionDefinitionNode.modify()
                .withFunctionKeyword(functionKeyword)
                .withFunctionName(functionName)
                .withFunctionSignature(functionSignatureNode).withQualifierList(qualifierList)
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public FunctionSignatureNode transform(FunctionSignatureNode functionSignatureNode) {
        Token openPara = formatToken(functionSignatureNode.openParenToken(), 0, 0);

        // Start a new indentation for the parameters. So any wrapped parameter will
        // start from the same level as the open parenthesis.
        int currentIndentation = env.currentIndentation;
        setIndentation(env.lineLength);
        SeparatedNodeList<ParameterNode> parameters =
                formatSeparatedNodeList(functionSignatureNode.parameters(), 0, 0, 0, 0);
        setIndentation(currentIndentation);

        Token closePara;
        if (functionSignatureNode.returnTypeDesc().isPresent()) {
            closePara = formatToken(functionSignatureNode.closeParenToken(), 1, 0);
            ReturnTypeDescriptorNode returnTypeDesc =
                    formatNode(functionSignatureNode.returnTypeDesc().get(), env.trailingWS, env.trailingNL);
            functionSignatureNode = functionSignatureNode.modify().withReturnTypeDesc(returnTypeDesc).apply();
        } else {
            closePara = formatToken(functionSignatureNode.closeParenToken(), env.trailingWS, env.trailingNL);
        }

        return functionSignatureNode.modify()
                .withOpenParenToken(openPara)
                .withCloseParenToken(closePara)
                .withParameters(parameters)
                .apply();
    }

    @Override
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(requiredParameterNode.annotations(), 1, 0, 1, 0);
        Node typeName;
        if (requiredParameterNode.paramName().isPresent()) {
            typeName = formatNode(requiredParameterNode.typeName(), 1, 0);
            Token paramName = formatToken(requiredParameterNode.paramName().get(), env.trailingWS, env.trailingNL);
            return requiredParameterNode.modify()
                    .withAnnotations(annotations)
                    .withTypeName(typeName)
                    .withParamName(paramName)
                    .apply();
        } else {
            typeName = formatNode(requiredParameterNode.typeName(), env.trailingWS, env.trailingNL);
            return requiredParameterNode.modify()
                    .withAnnotations(annotations)
                    .withTypeName(typeName)
                    .apply();
        }
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        Token openBrace = formatToken(functionBodyBlockNode.openBraceToken(), 0, 1);
        indent(); // increase indentation for the statements to follow.
        NodeList<StatementNode> statements = formatNodeList(functionBodyBlockNode.statements(), 0, 1, 0, 1, true);
        if (functionBodyBlockNode.namedWorkerDeclarator().isPresent()) {
            NamedWorkerDeclarator namedWorkerDeclarator =
                    formatNode(functionBodyBlockNode.namedWorkerDeclarator().get(), 0, 1);
            functionBodyBlockNode = functionBodyBlockNode.modify()
                    .withNamedWorkerDeclarator(namedWorkerDeclarator)
                    .apply();
        }

        unindent(); // reset the indentation
        if (functionBodyBlockNode.statements().isEmpty() &&
                !functionBodyBlockNode.namedWorkerDeclarator().isPresent()) {
            env.preserveNewlines = true;
        }
        Token closeBrace = formatToken(functionBodyBlockNode.closeBraceToken(), env.trailingWS, env.trailingNL);

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(openBrace)
                .withCloseBraceToken(closeBrace)
                .withStatements(statements)
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
        NodeList<AnnotationNode> annotationNodes = formatNodeList(variableDeclarationNode.annotations(), 0, 1, 0, 1);
        if (variableDeclarationNode.finalKeyword().isPresent()) {
            Token finalToken = formatToken(variableDeclarationNode.finalKeyword().get(), 1, 0);
            variableDeclarationNode = variableDeclarationNode.modify().withFinalKeyword(finalToken).apply();
        }

        TypedBindingPatternNode typedBindingPatternNode;
        if (variableDeclarationNode.equalsToken().isPresent()) {
            typedBindingPatternNode = formatNode(variableDeclarationNode.typedBindingPattern(), 1, 0);
            Token equalToken = formatToken(variableDeclarationNode.equalsToken().get(), 1, 0);

            boolean previousInLineAnnotation = env.inLineAnnotation;
            setInLineAnnotation(true);
            ExpressionNode initializer = formatNode(variableDeclarationNode.initializer().get(), 0, 0);
            setInLineAnnotation(previousInLineAnnotation);

            Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(),
                    env.trailingWS, env.trailingNL);
            return variableDeclarationNode.modify()
                    .withAnnotations(annotationNodes)
                    .withTypedBindingPattern(typedBindingPatternNode)
                    .withEqualsToken(equalToken)
                    .withInitializer(initializer)
                    .withSemicolonToken(semicolonToken)
                    .apply();
        } else {
            typedBindingPatternNode = formatNode(variableDeclarationNode.typedBindingPattern(), 0, 0);
            Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(),
                    env.trailingWS, env.trailingNL);
            return variableDeclarationNode.modify()
                    .withAnnotations(annotationNodes)
                    .withTypedBindingPattern(typedBindingPatternNode)
                    .withSemicolonToken(semicolonToken)
                    .apply();
        }
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
        Token ifKeyword = formatToken(ifElseStatementNode.ifKeyword(), 1, 0);
        ExpressionNode condition = formatNode(ifElseStatementNode.condition(), 1, 0);
        BlockStatementNode ifBody;
        if (ifElseStatementNode.elseBody().isPresent()) {
            ifBody = formatNode(ifElseStatementNode.ifBody(), 1, 0);
            Node elseBody = formatNode(ifElseStatementNode.elseBody().get(), env.trailingWS, env.trailingNL);
            ifElseStatementNode = ifElseStatementNode.modify().withElseBody(elseBody).apply();
        } else {
            ifBody = formatNode(ifElseStatementNode.ifBody(), env.trailingWS, env.trailingNL);
        }

        return ifElseStatementNode.modify()
                .withIfKeyword(ifKeyword)
                .withIfBody(ifBody)
                .withCondition(condition)
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
        Token openBrace = formatToken(blockStatementNode.openBraceToken(), 0, 1);
        env.preserveNewlines = true;
        indent(); // start an indentation
        NodeList<StatementNode> statements = formatNodeList(blockStatementNode.statements(), 0, 1, 0, 1, true);
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

        int prevIndentation = env.currentIndentation;

        // Set indentation for braces.
        // For records inside module-level typ-defs, braces should have the same indentation as the type-keyword.
        // For records in other places, braces should have the same indentation as the record-keyword.
        if (recordTypeDesc.parent().kind() != SyntaxKind.TYPE_DEFINITION) {
            int fieldIndentation = env.lineLength - recordKeyword.text().length() - recordKeywordTrailingWS;
            setIndentation(fieldIndentation);
        }

        Token bodyStartDelimiter = formatToken(recordTypeDesc.bodyStartDelimiter(), fieldTrailingWS, fieldTrailingNL);
        indent(); // Set indentation for record fields
        NodeList<Node> fields = formatNodeList(recordTypeDesc.fields(), fieldTrailingWS, fieldTrailingNL,
                fieldTrailingWS, fieldTrailingNL, true);

        if (recordTypeDesc.recordRestDescriptor().isPresent()) {
            RecordRestDescriptorNode recordRestDescriptor =
                    formatNode(recordTypeDesc.recordRestDescriptor().get(), fieldTrailingWS, fieldTrailingNL);
            recordTypeDesc = recordTypeDesc.modify().withRecordRestDescriptor(recordRestDescriptor).apply();
        }

        unindent(); // Revert indentation for record fields
        Token bodyEndDelimiter = formatToken(recordTypeDesc.bodyEndDelimiter(), env.trailingWS, env.trailingNL);
        setIndentation(prevIndentation);  // Revert indentation for braces
        return recordTypeDesc.modify()
                .withRecordKeyword(recordKeyword)
                .withBodyStartDelimiter(bodyStartDelimiter)
                .withFields(fields)
                .withBodyEndDelimiter(bodyEndDelimiter)
                .apply();
    }

    @Override
    public RecordFieldNode transform(RecordFieldNode recordField) {
        if (recordField.metadata().isPresent()) {
            MetadataNode metadata = formatNode(recordField.metadata().get(), 0, 1);
            recordField = recordField.modify().withMetadata(metadata).apply();
        }

        if (recordField.readonlyKeyword().isPresent()) {
            Token readonlyKeyword = formatNode(recordField.readonlyKeyword().get(), 1, 0);
            recordField = recordField.modify().withReadonlyKeyword(readonlyKeyword).apply();
        }

        Node typeName = formatNode(recordField.typeName(), 1, 0);
        Token fieldName = formatToken(recordField.fieldName(), 0, 0);

        if (recordField.questionMarkToken().isPresent()) {
            Token questionMarkToken = formatToken(recordField.questionMarkToken().get(), 0, 0);
            recordField = recordField.modify().withQuestionMarkToken(questionMarkToken).apply();
        }

        Token semicolonToken = formatToken(recordField.semicolonToken(), env.trailingWS, env.trailingNL);
        return recordField.modify()
                .withTypeName(typeName)
                .withFieldName(fieldName)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public RecordFieldWithDefaultValueNode transform(RecordFieldWithDefaultValueNode recordField) {
        if (recordField.metadata().isPresent()) {
            MetadataNode metadata = formatNode(recordField.metadata().get(), 0, 1);
            recordField = recordField.modify().withMetadata(metadata).apply();
        }

        if (recordField.readonlyKeyword().isPresent()) {
            Token readonlyKeyword = formatNode(recordField.readonlyKeyword().get(), 1, 0);
            recordField = recordField.modify().withReadonlyKeyword(readonlyKeyword).apply();
        }

        Node typeName = formatNode(recordField.typeName(), 1, 0);
        Token fieldName = formatToken(recordField.fieldName(), 1, 0);
        Token equalsToken = formatToken(recordField.equalsToken(), 1, 0);
        ExpressionNode expression = formatNode(recordField.expression(), 0, 0);
        Token semicolonToken = formatToken(recordField.semicolonToken(), env.trailingWS, env.trailingNL);

        return recordField.modify()
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

        boolean hasOrgName = importDeclarationNode.orgName().isPresent();
        boolean hasVersion = importDeclarationNode.version().isPresent();
        boolean hasPrefix = importDeclarationNode.prefix().isPresent();

        if (hasOrgName) {
            ImportOrgNameNode orgName = formatNode(importDeclarationNode.orgName().get(), 0, 0);
            importDeclarationNode = importDeclarationNode.modify().withOrgName(orgName).apply();
        }
        SeparatedNodeList<IdentifierToken> moduleNames = formatSeparatedNodeList(importDeclarationNode.moduleName(),
                0, 0, 0, 0, (hasVersion || hasPrefix) ? 1 : 0, 0);

        if (hasVersion) {
            ImportVersionNode version = formatNode(importDeclarationNode.version().get(), hasPrefix ? 1 : 0, 0);
            importDeclarationNode = importDeclarationNode.modify().withVersion(version).apply();
        }
        if (hasPrefix) {
            ImportPrefixNode prefix = formatNode(importDeclarationNode.prefix().get(), 0, 0);
            importDeclarationNode = importDeclarationNode.modify().withPrefix(prefix).apply();
        }
        Token semicolon = formatToken(importDeclarationNode.semicolon(), env.trailingWS, env.trailingNL);

        return importDeclarationNode.modify()
                .withImportKeyword(importKeyword)
                .withModuleName(moduleNames)
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
    public ImportVersionNode transform(ImportVersionNode importVersionNode) {
        Token versionKeyword = formatToken(importVersionNode.versionKeyword(), 1, 0);
        SeparatedNodeList<Token> versionNumber = formatSeparatedNodeList(importVersionNode.versionNumber(),
                0, 0, 0, 0, env.trailingWS, env.trailingNL);

        return importVersionNode.modify()
                .withVersionKeyword(versionKeyword)
                .withVersionNumber(versionNumber)
                .apply();
    }

    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        if (serviceDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(serviceDeclarationNode.metadata().get(), 0, 1);
            serviceDeclarationNode = serviceDeclarationNode.modify().withMetadata(metadata).apply();
        }
        Token serviceKeyword = formatToken(serviceDeclarationNode.serviceKeyword(), 1, 0);

        if (serviceDeclarationNode.serviceName().isPresent()) {
            IdentifierToken serviceName = formatToken(serviceDeclarationNode.serviceName().get(), 1, 0);
            serviceDeclarationNode = serviceDeclarationNode.modify().withServiceName(serviceName).apply();
        }

        Token onKeyword = formatToken(serviceDeclarationNode.onKeyword(), 1, 0);
        SeparatedNodeList<ExpressionNode> expressions =
                formatSeparatedNodeList(serviceDeclarationNode.expressions(), 0, 0, 1, 0);
        Node serviceBody = formatNode(serviceDeclarationNode.serviceBody(), env.trailingWS, env.trailingNL);

        return serviceDeclarationNode.modify()
                .withServiceKeyword(serviceKeyword)
                .withOnKeyword(onKeyword)
                .withExpressions(expressions)
                .withServiceBody(serviceBody)
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
                .arguments(), 0, 0, 0, 0);
        Token closeParenToken = formatToken(parenthesizedArgList.closeParenToken(), env.trailingWS, env.trailingNL);

        return parenthesizedArgList.modify()
                .withOpenParenToken(openParenToken)
                .withArguments(arguments)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public ServiceBodyNode transform(ServiceBodyNode serviceBodyNode) {
        Token openBraceToken = formatToken(serviceBodyNode.openBraceToken(), 0, 1);
        indent(); // increase indentation for the statements to follow.
        NodeList<Node> resources = formatNodeList(serviceBodyNode.resources(), 0, 1, 0, 1, true);
        unindent(); // reset the indentation

        if (serviceBodyNode.resources().isEmpty()) {
            env.preserveNewlines = true;
        }
        Token closeBraceToken = formatToken(serviceBodyNode.closeBraceToken(), env.trailingWS, env.trailingNL);
        return serviceBodyNode.modify()
                .withOpenBraceToken(openBraceToken)
                .withResources(resources)
                .withCloseBraceToken(closeBraceToken)
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
        if (typeDefinitionNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(typeDefinitionNode.metadata().get(), 0, 1);
            typeDefinitionNode = typeDefinitionNode.modify().withMetadata(metadata).apply();
        }
        if (typeDefinitionNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(typeDefinitionNode.visibilityQualifier().get(), 1, 0);
            typeDefinitionNode = typeDefinitionNode.modify().withVisibilityQualifier(visibilityQualifier).apply();
        }

        Token typeKeyword = formatToken(typeDefinitionNode.typeKeyword(), 1, 0);
        Token typeName = formatToken(typeDefinitionNode.typeName(), 1, 0);
        Node typeDescriptor = formatNode(typeDefinitionNode.typeDescriptor(), 0, 0);
        Token semicolonToken = formatToken(typeDefinitionNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return typeDefinitionNode.modify()
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

        if (hasOnFailClause) {
            whileBody = formatNode(whileStatementNode.whileBody(), 1, 0);
            OnFailClauseNode onFailClause = formatNode(whileStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            whileStatementNode = whileStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            whileBody = formatNode(whileStatementNode.whileBody(), env.trailingWS, env.trailingNL);
        }

        return whileStatementNode.modify()
                .withWhileKeyword(whileKeyword)
                .withCondition(condition)
                .withWhileBody(whileBody)
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

        boolean previousinLineAnnotation = env.inLineAnnotation;
        setInLineAnnotation(true);

        ExpressionNode expression = formatNode(assignmentStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(assignmentStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        setInLineAnnotation(previousinLineAnnotation);

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

        if (hasOnFailClause) {
            blockStatement = formatNode(doStatementNode.blockStatement(), 1, 0);
            OnFailClauseNode onFailClause = formatNode(doStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            doStatementNode = doStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(doStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return doStatementNode.modify()
                .withDoKeyword(doKeyword)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public ForEachStatementNode transform(ForEachStatementNode forEachStatementNode) {
        boolean hasOnFailClause = forEachStatementNode.onFailClause().isPresent();
        Token forEachKeyword = formatToken(forEachStatementNode.forEachKeyword(), 1, 0);
        TypedBindingPatternNode typedBindingPattern = formatNode(forEachStatementNode.typedBindingPattern(), 1, 0);
        Token inKeyword = formatToken(forEachStatementNode.inKeyword(), 1, 0);
        Node actionOrExpressionNode = formatNode(forEachStatementNode.actionOrExpressionNode(), 1, 0);
        StatementNode blockStatement;

        if (hasOnFailClause) {
            blockStatement = formatNode(forEachStatementNode.blockStatement(), 1, 0);
            OnFailClauseNode onFailClause = formatNode(forEachStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            forEachStatementNode = forEachStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(forEachStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return forEachStatementNode.modify()
                .withForEachKeyword(forEachKeyword)
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(inKeyword)
                .withActionOrExpressionNode(actionOrExpressionNode)
                .withBlockStatement(blockStatement)
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
        TypeDescriptorNode typeDescriptor = formatNode(onFailClauseNode.typeDescriptor(), 1, 0);
        IdentifierToken failErrorName = formatToken(onFailClauseNode.failErrorName(), 1, 0);
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

        if (returnStatementNode.expression().isPresent()) {
            ExpressionNode expressionNode = formatNode(returnStatementNode.expression().get(), 0, 0);
            returnStatementNode = returnStatementNode.modify().withExpression(expressionNode).apply();
        }
        Token semicolonToken = formatToken(returnStatementNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return returnStatementNode.modify()
                .withReturnKeyword(returnKeyword)
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
        if (constantDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(constantDeclarationNode.metadata().get(), 0, 1);
            constantDeclarationNode = constantDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }

        if (constantDeclarationNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(constantDeclarationNode.visibilityQualifier().get(), 1, 0);
            constantDeclarationNode = constantDeclarationNode.modify()
                    .withVisibilityQualifier(visibilityQualifier).apply();
        }
        Token constKeyword = formatToken(constantDeclarationNode.constKeyword(), 1, 0);

        if (constantDeclarationNode.typeDescriptor().isPresent()) {
            TypeDescriptorNode typeDescriptorNode = formatNode(constantDeclarationNode.typeDescriptor().get(), 1, 0);
            constantDeclarationNode = constantDeclarationNode.modify().withTypeDescriptor(typeDescriptorNode).apply();
        }

        Token variableName = formatToken(constantDeclarationNode.variableName(), 1, 0);
        Token equalsToken = formatToken(constantDeclarationNode.equalsToken(), 1, 0);
        Node initializer = formatNode(constantDeclarationNode.initializer(), 0, 0);
        Token semicolonToken = formatToken(constantDeclarationNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return constantDeclarationNode.modify()
                .withConstKeyword(constKeyword)
                .withEqualsToken(equalsToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        Token parameterizedType = formatToken(parameterizedTypeDescriptorNode.parameterizedType(), 0, 0);
        TypeParameterNode typeParameter = formatNode(parameterizedTypeDescriptorNode.typeParameter(),
                env.trailingWS, env.trailingNL);

        return parameterizedTypeDescriptorNode.modify()
                .withParameterizedType(parameterizedType)
                .withTypeParameter(typeParameter)
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
        Token functionKeyword = formatToken(functionTypeDescriptorNode.functionKeyword(), 0, 0);
        FunctionSignatureNode functionSignature = formatNode(functionTypeDescriptorNode.functionSignature(),
                env.trailingWS, env.trailingNL);

        return functionTypeDescriptorNode.modify()
                .withQualifierList(qualifierList)
                .withFunctionKeyword(functionKeyword)
                .withFunctionSignature(functionSignature)
                .apply();
    }

    @Override
    public ParenthesisedTypeDescriptorNode transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        Token openParenToken = formatToken(parenthesisedTypeDescriptorNode.openParenToken(), 0, 0);
        TypeDescriptorNode typedesc = formatNode(parenthesisedTypeDescriptorNode.typedesc(), 0, 0);
        Token closeParenToken = formatToken(parenthesisedTypeDescriptorNode.closeParenToken(),
                env.trailingWS, env.trailingNL);

        return parenthesisedTypeDescriptorNode.modify()
                .withOpenParenToken(openParenToken)
                .withTypedesc(typedesc)
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
            MappingConstructorExpressionNode annotValue = formatNode(annotationNode.annotValue().get(),
                    env.trailingWS, env.trailingNL);
            annotationNode = annotationNode.modify().withAnnotValue(annotValue).apply();
        } else {
            annotReference = formatNode(annotationNode.annotReference(), env.trailingWS, env.trailingNL);
        }

        return annotationNode.modify()
                .withAtToken(atToken)
                .withAnnotReference(annotReference)
                .apply();
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(mappingConstructorExpressionNode.fields())) {
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
        if (specificFieldNode.readonlyKeyword().isPresent()) {
            Token readOnlyKeyword = formatToken(specificFieldNode.readonlyKeyword().get(), 1, 0);
            specificFieldNode = specificFieldNode.modify()
                    .withReadonlyKeyword(readOnlyKeyword).apply();
        }

        Token fieldName;
        if (specificFieldNode.fieldName() instanceof BasicLiteralNode) {
            fieldName = ((BasicLiteralNode) specificFieldNode.fieldName()).literalToken();
        } else {
            fieldName = (Token) specificFieldNode.fieldName();
        }

        if (specificFieldNode.colon().isPresent()) {
            fieldName = formatToken(fieldName, 0, 0);
            Token colon = formatToken(specificFieldNode.colon().get(), 1, 0);
            ExpressionNode expressionNode = formatNode(specificFieldNode.valueExpr().get(),
                    env.trailingWS, env.trailingNL);
            return specificFieldNode.modify()
                    .withFieldName(fieldName)
                    .withColon(colon)
                    .withValueExpr(expressionNode)
                    .apply();
        } else {
            fieldName = formatToken(fieldName, env.trailingWS, env.trailingNL);
            return specificFieldNode.modify()
                    .withFieldName(fieldName)
                    .apply();
        }
    }

    @Override
    public ListConstructorExpressionNode transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Token openBracket = formatToken(listConstructorExpressionNode.openBracket(), 0, 0);
        SeparatedNodeList<Node> expressions = formatSeparatedNodeList(listConstructorExpressionNode.expressions(),
                0, 0, 0, 0);
        Token closeBracket = formatToken(listConstructorExpressionNode.closeBracket(),
                env.trailingWS, env.trailingNL);

        return listConstructorExpressionNode.modify()
                .withOpenBracket(openBracket)
                .withExpressions(expressions)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public ErrorTypeDescriptorNode transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        if (errorTypeDescriptorNode.errorTypeParamsNode().isPresent()) {
            Token errorKeywordToken = formatToken(errorTypeDescriptorNode.errorKeywordToken(), 0, 0);
            ErrorTypeParamsNode errorTypeParamsNode = formatNode(errorTypeDescriptorNode.errorTypeParamsNode().get(),
                    env.trailingWS, env.trailingNL);

            return errorTypeDescriptorNode.modify()
                    .withErrorKeywordToken(errorKeywordToken)
                    .withErrorTypeParamsNode(errorTypeParamsNode)
                    .apply();
        } else {
            Token errorKeywordToken = formatToken(errorTypeDescriptorNode.errorKeywordToken(),
                    env.trailingWS, env.trailingNL);

            return errorTypeDescriptorNode.modify()
                    .withErrorKeywordToken(errorKeywordToken)
                    .apply();
        }
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
        if (moduleVariableDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(moduleVariableDeclarationNode.metadata().get(), 0, 1);
            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify().withMetadata(metadata).apply();
        }

        if (moduleVariableDeclarationNode.finalKeyword().isPresent()) {
            Token finalKeyword = formatToken(moduleVariableDeclarationNode.finalKeyword().get(), 1, 0);
            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                    .withFinalKeyword(finalKeyword).apply();
        }

        TypedBindingPatternNode typedBindingPatternNode;

        if (moduleVariableDeclarationNode.equalsToken().isPresent()
                && moduleVariableDeclarationNode.initializer().isPresent()) {
            typedBindingPatternNode = formatNode(moduleVariableDeclarationNode.typedBindingPattern(), 1, 0);
            Token equalsToken = formatToken(moduleVariableDeclarationNode.equalsToken().get(), 1, 0);
            ExpressionNode initializer = formatNode(moduleVariableDeclarationNode.initializer().get(), 0, 0);

            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                    .withEqualsToken(equalsToken)
                    .withInitializer(initializer)
                    .apply();
        } else {
            typedBindingPatternNode = formatNode(moduleVariableDeclarationNode.typedBindingPattern(), 0, 0);
        }

        Token semicolonToken = formatToken(moduleVariableDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return moduleVariableDeclarationNode.modify()
                .withTypedBindingPattern(typedBindingPatternNode)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ExpressionFunctionBodyNode transform(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        Token rightDoubleArrow = formatToken(expressionFunctionBodyNode.rightDoubleArrow(), 1, 0);

        if (expressionFunctionBodyNode.semicolon().isPresent()) {
            ExpressionNode expression = formatNode(expressionFunctionBodyNode.expression(), 0, 0);
            Token semicolon = formatToken(expressionFunctionBodyNode.semicolon().get(),
                    env.trailingWS, env.trailingNL);

            return expressionFunctionBodyNode.modify()
                    .withRightDoubleArrow(rightDoubleArrow)
                    .withExpression(expression)
                    .withSemicolon(semicolon)
                    .apply();
        } else {
            ExpressionNode expression = formatNode(expressionFunctionBodyNode.expression(),
                    env.trailingWS, env.trailingNL);

            return expressionFunctionBodyNode.modify()
                    .withRightDoubleArrow(rightDoubleArrow)
                    .withExpression(expression)
                    .apply();
        }
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
        NodeList<AnnotationNode> annotations = formatNodeList(typeCastParamNode.annotations(), 0, 1, 1, 0);
        Node type = formatNode(typeCastParamNode.type(), env.trailingWS, env.trailingNL);

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
        SeparatedNodeList<BindingPatternNode> bindingPatternNodes;

        if (listBindingPatternNode.restBindingPattern().isPresent()) {
            bindingPatternNodes =
                    formatSeparatedNodeList(listBindingPatternNode.bindingPatterns(), 0, 0, 1, 0);
            RestBindingPatternNode restBindingPattern = formatNode(listBindingPatternNode.restBindingPattern().get(),
                    0, 0);
            listBindingPatternNode = listBindingPatternNode.modify().withRestBindingPattern(restBindingPattern).apply();
        } else {
            bindingPatternNodes =
                    formatSeparatedNodeList(listBindingPatternNode.bindingPatterns(), 0, 0, 0, 0);
        }

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
        if (tableTypeDescriptorNode.keyConstraintNode().isPresent()) {
            Node rowTypeParameterNode = formatNode(tableTypeDescriptorNode.rowTypeParameterNode(), 1, 0);
            Node keyConstraintNode = formatNode(tableTypeDescriptorNode.keyConstraintNode().get(),
                    env.trailingWS, env.trailingNL);
            return tableTypeDescriptorNode.modify()
                    .withTableKeywordToken(tableKeywordToken)
                    .withRowTypeParameterNode(rowTypeParameterNode)
                    .withKeyConstraintNode(keyConstraintNode)
                    .apply();
        } else {
            Node rowTypeParameterNode =
                    formatNode(tableTypeDescriptorNode.rowTypeParameterNode(), env.trailingWS, env.trailingNL);
            return tableTypeDescriptorNode.modify()
                    .withTableKeywordToken(tableKeywordToken)
                    .withRowTypeParameterNode(rowTypeParameterNode)
                    .apply();
        }
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
            OnFailClauseNode onFailClause = formatNode(matchStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            matchStatementNode = matchStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            closeBrace = formatToken(matchStatementNode.closeBrace(), env.trailingWS, env.trailingNL);
        }

        return matchStatementNode.modify()
                .withMatchKeyword(matchKeyword)
                .withCondition(condition)
                .withOpenBrace(openBrace)
                .withMatchClauses(matchClauses)
                .withCloseBrace(closeBrace)
                .apply();
    }

    @Override
    public MatchClauseNode transform(MatchClauseNode matchClauseNode) {
        SeparatedNodeList<Node> matchPatterns = formatSeparatedNodeList(matchClauseNode.matchPatterns(),
                0, 0, 0, 0, 1, 0);

        if (matchClauseNode.matchGuard().isPresent()) {
            MatchGuardNode matchGuard = formatNode(matchClauseNode.matchGuard().get(), 1, 0);
            matchClauseNode = matchClauseNode.modify().withMatchGuard(matchGuard).apply();
        }

        Token rightDoubleArrow = formatToken(matchClauseNode.rightDoubleArrow(), 1, 0);
        BlockStatementNode blockStatement = formatNode(matchClauseNode.blockStatement(),
                env.trailingWS, env.trailingNL);

        return matchClauseNode.modify()
                .withMatchPatterns(matchPatterns)
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
        StatementNode blockStatement;

        if (lockStatementNode.onFailClause().isPresent()) {
            blockStatement = formatNode(lockStatementNode.blockStatement(), 1, 0);
            OnFailClauseNode onFailClause = formatNode(lockStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            lockStatementNode = lockStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(lockStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return lockStatementNode.modify()
                .withLockKeyword(lockKeyword)
                .withBlockStatement(blockStatement)
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
            Node documentationString = formatNode(metadataNode.documentationString().get(), 0, 1);
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
        if (enumDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(enumDeclarationNode.metadata().get(), 0, 1);
            enumDeclarationNode = enumDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }

        if (enumDeclarationNode.qualifier().isPresent()) {
            Token qualifier = formatToken(enumDeclarationNode.qualifier().get(), 1, 0);
            enumDeclarationNode = enumDeclarationNode.modify()
                    .withQualifier(qualifier).apply();
        }

        Token enumKeywordToken = formatToken(enumDeclarationNode.enumKeywordToken(), 1, 0);
        IdentifierToken identifier = formatNode(enumDeclarationNode.identifier(), 1, 0);
        Token openBraceToken = formatToken(enumDeclarationNode.openBraceToken(), 0, 1);
        indent();
        SeparatedNodeList<Node> enumMemberList = formatSeparatedNodeList(enumDeclarationNode.enumMemberList(),
                0, 0, 0, 1, 0, 1);
        unindent();
        Token closeBraceToken = formatToken(enumDeclarationNode.closeBraceToken(), env.trailingWS, env.trailingNL);

        return enumDeclarationNode.modify()
                .withEnumKeywordToken(enumKeywordToken)
                .withIdentifier(identifier)
                .withOpenBraceToken(openBraceToken)
                .withEnumMemberList(enumMemberList)
                .withCloseBraceToken(closeBraceToken)
                .apply();
    }

    @Override
    public EnumMemberNode transform(EnumMemberNode enumMemberNode) {
        if (enumMemberNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(enumMemberNode.metadata().get(), 0, 1);
            enumMemberNode = enumMemberNode.modify().withMetadata(metadata).apply();
        }
        IdentifierToken identifier;

        if (enumMemberNode.equalToken().isPresent()) {
            identifier = formatNode(enumMemberNode.identifier(), 1, 0);
            Token equalToken = formatToken(enumMemberNode.equalToken().get(), 1, 0);
            ExpressionNode constExprNode = formatNode(enumMemberNode.constExprNode().get(),
                    env.trailingWS, env.trailingNL);

            return enumMemberNode.modify()
                    .withIdentifier(identifier)
                    .withEqualToken(equalToken)
                    .withConstExprNode(constExprNode)
                    .apply();
        } else {
            identifier = formatNode(enumMemberNode.identifier(), env.trailingWS, env.trailingNL);

            return enumMemberNode.modify()
                    .withIdentifier(identifier)
                    .apply();
        }
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
            if (markdownDocumentationLineNode.documentElements().get(0).kind() == SyntaxKind.DEPRECATION_LITERAL) {
                hashToken = formatToken(markdownDocumentationLineNode.hashToken(), 1, 0);
            } else {
                hashToken = formatToken(markdownDocumentationLineNode.hashToken(), 0, 0);
            }
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
        Token plusToken = formatToken(markdownParameterDocumentationLineNode.plusToken(), 1, 0);
        Token parameterName = formatToken(markdownParameterDocumentationLineNode.parameterName(), 1, 0);
        Token minusToken = formatToken(markdownParameterDocumentationLineNode.minusToken(), 0, 0);
        NodeList<Node> documentElements = formatNodeList(markdownParameterDocumentationLineNode.documentElements(),
                0, 0, env.trailingWS, env.trailingNL);

        return markdownParameterDocumentationLineNode.modify()
                .withHashToken(hashToken)
                .withPlusToken(plusToken)
                .withParameterName(parameterName)
                .withMinusToken(minusToken)
                .withDocumentElements(documentElements)
                .apply();
    }

    @Override
    public DocumentationReferenceNode transform(DocumentationReferenceNode documentationReferenceNode) {
        if (documentationReferenceNode.referenceType().isPresent()) {
            Token referenceType = formatToken(documentationReferenceNode.referenceType().get(), 1, 0);
            documentationReferenceNode = documentationReferenceNode.modify().withReferenceType(referenceType).apply();
        }

        Token startBacktick = formatToken(documentationReferenceNode.startBacktick(), 0, 0);
        Node backtickContent = formatNode(documentationReferenceNode.backtickContent(), 0, 0);
        Token endBacktick = formatToken(documentationReferenceNode.endBacktick(), env.trailingWS, env.trailingNL);

        return documentationReferenceNode.modify()
                .withStartBacktick(startBacktick)
                .withBacktickContent(backtickContent)
                .withEndBacktick(endBacktick)
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
        SeparatedNodeList<FieldBindingPatternNode> fieldBindingPatternNodes =
                formatSeparatedNodeList(mappingBindingPatternNode.fieldBindingPatterns(), 0, 0, 0, 0);
        if (mappingBindingPatternNode.restBindingPattern().isPresent()) {
            RestBindingPatternNode restBindingPattern =
                    formatNode(mappingBindingPatternNode.restBindingPattern().get(), 1, 0);
            mappingBindingPatternNode = mappingBindingPatternNode.modify()
                    .withRestBindingPattern(restBindingPattern).apply();
        }

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
        Token colon = formatToken(fieldBindingPatternFullNode.colon(), 0, 0);
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
                env.trailingWS, env.leadingNL);
        return typeTestExpressionNode.modify()
                .withExpression(expression)
                .withIsKeyword(isToken)
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public ListenerDeclarationNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        if (listenerDeclarationNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(listenerDeclarationNode.visibilityQualifier().get(), 1, 0);
            listenerDeclarationNode = listenerDeclarationNode.modify()
                    .withVisibilityQualifier(visibilityQualifier).apply();
        }

        if (listenerDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(listenerDeclarationNode.metadata().get(), 0, 1);
            listenerDeclarationNode = listenerDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }

        Token listenerKeyword = formatToken(listenerDeclarationNode.listenerKeyword(), 1, 0);
        Node typeDescriptor = formatNode(listenerDeclarationNode.typeDescriptor(), 1, 0);
        Token variableName = formatToken(listenerDeclarationNode.variableName(), 1, 0);
        Token equalsToken = formatToken(listenerDeclarationNode.equalsToken(), 1, 0);
        Node initializer = formatNode(listenerDeclarationNode.initializer(), 0, 0);
        Token semicolonToken = formatToken(listenerDeclarationNode.semicolonToken(), env.trailingWS, env.trailingNL);
        return listenerDeclarationNode.modify()
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
        ExpressionNode namespaceUri;

        if (xMLNamespaceDeclarationNode.asKeyword().isPresent()) {
            namespaceUri = formatNode(xMLNamespaceDeclarationNode.namespaceuri(), 1, 0);
            Token asKeyword = formatToken(xMLNamespaceDeclarationNode.asKeyword().get(), 1, 0);
            IdentifierToken namespacePrefix = formatNode(xMLNamespaceDeclarationNode.namespacePrefix().get(), 0, 0);

            xMLNamespaceDeclarationNode = xMLNamespaceDeclarationNode.modify()
                    .withAsKeyword(asKeyword)
                    .withNamespacePrefix(namespacePrefix)
                    .apply();
        } else {
            namespaceUri = formatNode(xMLNamespaceDeclarationNode.namespaceuri(), 0, 0);
        }

        Token semicolonToken = formatToken(xMLNamespaceDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);

        return xMLNamespaceDeclarationNode.modify()
                .withXmlnsKeyword(xmlnsKeyword)
                .withNamespaceuri(namespaceUri)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = formatToken(moduleXMLNamespaceDeclarationNode.xmlnsKeyword(), 1, 0);
        ExpressionNode namespaceUri;

        if (moduleXMLNamespaceDeclarationNode.asKeyword().isPresent()) {
            namespaceUri = formatNode(moduleXMLNamespaceDeclarationNode.namespaceuri(), 1, 0);
            Token asKeyword = formatToken(moduleXMLNamespaceDeclarationNode.asKeyword().get(), 1, 0);
            IdentifierToken namespacePrefix =
                    formatNode(moduleXMLNamespaceDeclarationNode.namespacePrefix().get(), 0, 0);
            moduleXMLNamespaceDeclarationNode = moduleXMLNamespaceDeclarationNode.modify()
                    .withAsKeyword(asKeyword)
                    .withNamespacePrefix(namespacePrefix)
                    .apply();
        } else {
            namespaceUri = formatNode(moduleXMLNamespaceDeclarationNode.namespaceuri(), 0, 0);
        }

        Token semicolonToken = formatToken(moduleXMLNamespaceDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);
        return moduleXMLNamespaceDeclarationNode.modify()
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
        if (templateExpressionNode.type().isPresent()) {
            Token type = formatToken(templateExpressionNode.type().get(), 1, 0);
            templateExpressionNode = templateExpressionNode.modify()
                    .withType(type).apply();
        }

        Token startBacktick = formatToken(templateExpressionNode.startBacktick(), 0, 0);
        NodeList<TemplateMemberNode> content = formatNodeList(templateExpressionNode.content(), 0, 0, 0, 0);
        Token endBacktick = formatToken(templateExpressionNode.endBacktick(), env.trailingWS, env.trailingNL);

        return templateExpressionNode.modify()
                .withStartBacktick(startBacktick)
                .withContent(content)
                .withEndBacktick(endBacktick)
                .apply();
    }

    @Override
    public ByteArrayLiteralNode transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        Token type = formatToken(byteArrayLiteralNode.type(), 1, 0);
        Token startBacktick = formatToken(byteArrayLiteralNode.startBacktick(), 0, 0);
        if (byteArrayLiteralNode.content().isPresent()) {
            Token content = formatToken(byteArrayLiteralNode.content().get(), 0, 0);
            byteArrayLiteralNode = byteArrayLiteralNode.modify()
                    .withContent(content).apply();
        }

        Token endBacktick = formatToken(byteArrayLiteralNode.endBacktick(), env.trailingWS, env.trailingNL);
        return byteArrayLiteralNode.modify()
                .withType(type)
                .withStartBacktick(startBacktick)
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
        Token openBraceToken = formatToken(mappingMatchPatternNode.openBraceToken(), 1, 0);
        SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns =
                formatSeparatedNodeList(mappingMatchPatternNode.fieldMatchPatterns(), 0, 0, 1, 0);

        if (mappingMatchPatternNode.restMatchPattern().isPresent()) {
            RestMatchPatternNode restMatchPattern =
                    formatNode(mappingMatchPatternNode.restMatchPattern().orElse(null), 1, 0);
            mappingMatchPatternNode = mappingMatchPatternNode.modify()
                    .withRestMatchPattern(restMatchPattern).apply();
        }

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
        if (env.inLineAnnotation) {
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
            NameReferenceNode peerWorker = formatNode(flushActionNode.peerWorker().get(),
                    env.trailingWS, env.trailingNL);

            return flushActionNode.modify()
                    .withFlushKeyword(flushKeyword)
                    .withPeerWorker(peerWorker)
                    .apply();
        }
        flushKeyword = formatToken(flushActionNode.flushKeyword(), env.trailingWS, env.trailingNL);

        return flushActionNode.modify()
                .withFlushKeyword(flushKeyword)
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
                formatNodeList(forkStatementNode.namedWorkerDeclarations(), 0, 1, 0, 1, true);
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

        if (defaultableParameterNode.paramName().isPresent()) {
            Token paramName = formatToken(defaultableParameterNode.paramName().get(), 1, 0);
            defaultableParameterNode = defaultableParameterNode.modify().withParamName(paramName).apply();
        }

        Token equalsToken = formatToken(defaultableParameterNode.equalsToken(), 1, 0);
        Node expression = formatNode(defaultableParameterNode.expression(), env.trailingWS, env.trailingNL);

        return defaultableParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
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
            Token paramName = formatToken(restParameterNode.paramName().get(), env.trailingWS, env.trailingNL);
            restParameterNode = restParameterNode.modify().withParamName(paramName).apply();
        } else {
            ellipsisToken = formatToken(restParameterNode.ellipsisToken(), env.trailingWS, env.trailingNL);
        }

        return restParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
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
        int prevIndentation = env.currentIndentation;

        // Set indentation for braces.
        if (objectTypeDescriptorNode.parent().kind() != SyntaxKind.TYPE_DEFINITION) {
            // Set indentation for braces.
            if (env.lineLength == 0) {
                // Set the indentation for statements starting with query expression nodes.
                setIndentation(env.lineLength + prevIndentation);
            } else {
                setIndentation(env.lineLength);
            }
        }

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
                0, fieldTrailingNL, true);
        unindent();
        Token closeBrace = formatToken(objectTypeDescriptorNode.closeBrace(), env.trailingWS, env.trailingNL);
        setIndentation(prevIndentation);  // Revert indentation for braces

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
        if (shouldExpandObjectMembers(objectConstructorExpressionNode.members())) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        int prevIndentation = env.currentIndentation;

        // Set indentation for braces.
        int fieldIndentation = env.lineLength - objectKeyword.text().length() - 1;
        setIndentation(fieldIndentation);

        if (objectConstructorExpressionNode.typeReference().isPresent()) {
            TypeDescriptorNode typeReference = formatNode(objectConstructorExpressionNode.typeReference().get(),
                    1, 0);
            objectConstructorExpressionNode = objectConstructorExpressionNode.modify()
                    .withTypeReference(typeReference).apply();
        }

        Token openBraceToken = formatToken(objectConstructorExpressionNode.openBraceToken(), 0, fieldTrailingNL);
        indent();
        NodeList<Node> members = formatNodeList(objectConstructorExpressionNode.members(),
                fieldTrailingWS, fieldTrailingNL, 0, fieldTrailingNL, true);
        unindent();
        Token closeBraceToken = formatToken(objectConstructorExpressionNode.closeBraceToken(),
                env.trailingWS, env.trailingNL);
        setIndentation(prevIndentation);  // Revert indentation for braces

        return objectConstructorExpressionNode.modify()
                .withAnnotations(annotations)
                .withObjectTypeQualifiers(objectTypeQualifiers)
                .withObjectKeyword(objectKeyword)
                .withOpenBraceToken(openBraceToken)
                .withMembers(members)
                .withCloseBraceToken(closeBraceToken)
                .apply();
    }

    @Override
    public ObjectFieldNode transform(ObjectFieldNode objectFieldNode) {
        if (objectFieldNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(objectFieldNode.metadata().get(), 0, 1);
            objectFieldNode = objectFieldNode.modify().withMetadata(metadata).apply();
        }

        if (objectFieldNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(objectFieldNode.visibilityQualifier().get(), 1, 0);
            objectFieldNode = objectFieldNode.modify().withVisibilityQualifier(visibilityQualifier).apply();
        }

        if (objectFieldNode.finalKeyword().isPresent()) {
            Token finalKeyword = formatToken(objectFieldNode.finalKeyword().get(), 1, 0);
            objectFieldNode = objectFieldNode.modify().withFinalKeyword(finalKeyword).apply();
        }

        Node typeName = formatNode(objectFieldNode.typeName(), 1, 0);
        Token fieldName;

        if (objectFieldNode.equalsToken().isPresent() && objectFieldNode.expression().isPresent()) {
            fieldName = formatToken(objectFieldNode.fieldName(), 1, 0);
            Token equalsToken = formatToken(objectFieldNode.equalsToken().get(), 1, 0);
            ExpressionNode expression = formatNode(objectFieldNode.expression().get(), 0, 0);
            objectFieldNode = objectFieldNode.modify()
                    .withEqualsToken(equalsToken)
                    .withExpression(expression)
                    .apply();
        } else {
            fieldName = formatToken(objectFieldNode.fieldName(), 0, 0);
        }
        Token semicolonToken = formatToken(objectFieldNode.semicolonToken(), env.trailingWS, env.trailingNL);

        return objectFieldNode.modify()
                .withTypeName(typeName)
                .withFieldName(fieldName)
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
        if (annotationDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(annotationDeclarationNode.metadata().get(), 0, 1);
            annotationDeclarationNode = annotationDeclarationNode.modify().withMetadata(metadata).apply();
        }

        if (annotationDeclarationNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(annotationDeclarationNode.visibilityQualifier().get(), 1, 0);
            annotationDeclarationNode =
                    annotationDeclarationNode.modify().withVisibilityQualifier(visibilityQualifier).apply();
        }

        if (annotationDeclarationNode.constKeyword().isPresent()) {
            Token constKeyword = formatToken(annotationDeclarationNode.constKeyword().get(), 1, 0);
            annotationDeclarationNode =
                    annotationDeclarationNode.modify().withConstKeyword(constKeyword).apply();
        }

        Token annotationKeyword = formatToken(annotationDeclarationNode.annotationKeyword(), 1, 0);

        if (annotationDeclarationNode.typeDescriptor().isPresent()) {
            Node typeDescriptor = formatNode(annotationDeclarationNode.typeDescriptor().get(), 1, 0);
            annotationDeclarationNode =
                    annotationDeclarationNode.modify().withTypeDescriptor(typeDescriptor).apply();
        }

        Token annotationTag;

        if (annotationDeclarationNode.onKeyword().isPresent()) {
            annotationTag = formatToken(annotationDeclarationNode.annotationTag(), 1, 0);
            Token onKeyword = formatToken(annotationDeclarationNode.onKeyword().get(), 1, 0);
            int currentIndentation = env.currentIndentation;
            setIndentation(env.lineLength);
            SeparatedNodeList<Node> attachPoints = formatSeparatedNodeList(annotationDeclarationNode.attachPoints(),
                    0, 0, 0, 0);
            setIndentation(currentIndentation);
            annotationDeclarationNode = annotationDeclarationNode.modify()
                    .withOnKeyword(onKeyword)
                    .withAttachPoints(attachPoints)
                    .apply();
        } else {
            annotationTag = formatToken(annotationDeclarationNode.annotationTag(), 0, 0);
        }

        Token semicolonToken = formatToken(annotationDeclarationNode.semicolonToken(),
                env.trailingWS, env.trailingNL);
        return annotationDeclarationNode.modify()
                .withAnnotationKeyword(annotationKeyword)
                .withAnnotationTag(annotationTag)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public AnnotationAttachPointNode transform(AnnotationAttachPointNode annotationAttachPointNode) {
        if (annotationAttachPointNode.sourceKeyword().isPresent()) {
            Token sourceKeyword = formatToken(annotationAttachPointNode.sourceKeyword().get(), 1, 0);
            annotationAttachPointNode = annotationAttachPointNode.modify()
                    .withSourceKeyword(sourceKeyword)
                    .apply();
        }

        if (annotationAttachPointNode.secondIdent().isPresent()) {
            Token firstIdent = formatToken(annotationAttachPointNode.firstIdent(), 1, 0);
            Token secondIdent = formatToken(annotationAttachPointNode.secondIdent().get(), env.trailingWS,
                    env.trailingNL);
            return annotationAttachPointNode.modify()
                    .withFirstIdent(firstIdent)
                    .withSecondIdent(secondIdent)
                    .apply();
        } else {
            Token firstIdent = formatToken(annotationAttachPointNode.firstIdent(), env.trailingWS, env.trailingNL);
            return annotationAttachPointNode.modify()
                    .withFirstIdent(firstIdent)
                    .apply();
        }
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
        if (tableConstructorExpressionNode.keySpecifier().isPresent()) {
            KeySpecifierNode keySpecifier = formatNode(tableConstructorExpressionNode.keySpecifier().get(), 1, 0);
            tableConstructorExpressionNode = tableConstructorExpressionNode.modify()
                    .withKeySpecifier(keySpecifier).apply();
        }

        SeparatedNodeList<Node> rows = tableConstructorExpressionNode.rows();
        int rowTrailingWS = 0, rowTrailingNL = 0;
        if (rows.size() > 1) {
            rowTrailingNL++;
        } else {
            rowTrailingWS++;
        }

        indent();
        Token openBracket = formatToken(tableConstructorExpressionNode.openBracket(), 0, rowTrailingNL);
        indent();
        SeparatedNodeList<Node> mappingConstructors =
                formatSeparatedNodeList(tableConstructorExpressionNode.rows(), 0, 0, rowTrailingWS, rowTrailingNL, 0,
                        rowTrailingNL);
        unindent();
        Token closeBracket =
                formatToken(tableConstructorExpressionNode.closeBracket(), env.trailingWS, env.trailingNL);
        unindent();

        return tableConstructorExpressionNode.modify()
                .withTableKeyword(tableKeyword)
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
    public ErrorTypeParamsNode transform(ErrorTypeParamsNode errorTypeParamsNode) {
        Token ltToken = formatToken(errorTypeParamsNode.ltToken(), 0, 0);
        Node parameter = formatNode(errorTypeParamsNode.parameter(), 0, 0);
        Token gtToken = formatToken(errorTypeParamsNode.gtToken(), env.trailingWS, env.trailingNL);

        return errorTypeParamsNode.modify()
                .withLtToken(ltToken)
                .withParameter(parameter)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public StreamTypeDescriptorNode transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        Token streamKeywordToken;

        if (streamTypeDescriptorNode.streamTypeParamsNode().isPresent()) {
            streamKeywordToken = formatToken(streamTypeDescriptorNode.streamKeywordToken(), 0, 0);
            Node streamTypeParamsNode = formatNode(streamTypeDescriptorNode.streamTypeParamsNode().get(),
                    env.trailingWS, env.trailingNL);
            streamTypeDescriptorNode = streamTypeDescriptorNode.modify()
                    .withStreamTypeParamsNode(streamTypeParamsNode).apply();
        } else {
            streamKeywordToken = formatToken(streamTypeDescriptorNode.streamKeywordToken(),
                    env.trailingWS, env.trailingNL);
        }

        return streamTypeDescriptorNode.modify()
                .withStreamKeywordToken(streamKeywordToken)
                .apply();
    }

    @Override
    public StreamTypeParamsNode transform(StreamTypeParamsNode streamTypeParamsNode) {
        Token ltToken = formatToken(streamTypeParamsNode.ltToken(), 0, 0);
        Node leftTypeDescNode = formatNode(streamTypeParamsNode.leftTypeDescNode(), 0, 0);
        Token gtToken = formatToken(streamTypeParamsNode.gtToken(), env.trailingWS, env.trailingNL);

        if (streamTypeParamsNode.commaToken().isPresent() && streamTypeParamsNode.rightTypeDescNode().isPresent()) {
            Token commaToken = formatToken(streamTypeParamsNode.commaToken().get(), 1, 0);
            Node rightTypeDescNode = formatNode(streamTypeParamsNode.rightTypeDescNode().get(), 0, 0);
            streamTypeParamsNode = streamTypeParamsNode.modify()
                    .withCommaToken(commaToken)
                    .withRightTypeDescNode(rightTypeDescNode)
                    .apply();
        }

        return streamTypeParamsNode.modify()
                .withLtToken(ltToken)
                .withLeftTypeDescNode(leftTypeDescNode)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public TypedescTypeDescriptorNode transform(TypedescTypeDescriptorNode typedescTypeDescriptorNode) {
        if (typedescTypeDescriptorNode.typedescTypeParamsNode().isPresent()) {
            Token typedescKeyword = formatToken(typedescTypeDescriptorNode.typedescKeywordToken(), 0, 0);
            TypeParameterNode typedescTypeParamsNode =
                    formatNode(typedescTypeDescriptorNode.typedescTypeParamsNode().get(), env.trailingWS,
                            env.trailingNL);
            return typedescTypeDescriptorNode.modify()
                    .withTypedescKeywordToken(typedescKeyword)
                    .withTypedescTypeParamsNode(typedescTypeParamsNode)
                    .apply();
        }

        Token typedescKeyword =
                formatToken(typedescTypeDescriptorNode.typedescKeywordToken(), env.trailingWS, env.trailingNL);
        return typedescTypeDescriptorNode.modify()
                .withTypedescKeywordToken(typedescKeyword)
                .apply();
    }

    @Override
    public LetExpressionNode transform(LetExpressionNode letExpressionNode) {
        Token letKeyword = formatToken(letExpressionNode.letKeyword(), 1, 0);

        int prevIndentation = env.currentIndentation;
        int fieldIndentation = env.lineLength - letKeyword.text().length() - 1;
        setIndentation(fieldIndentation); // Set indentation for braces

        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                formatSeparatedNodeList(letExpressionNode.letVarDeclarations(), 0, 0, 0, 1);
        Token inKeyword = formatToken(letExpressionNode.inKeyword(), 1, 0);
        ExpressionNode expression = formatNode(letExpressionNode.expression(), env.trailingWS, env.trailingNL);

        setIndentation(prevIndentation);  // Revert indentation for braces

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

        int prevIndentation = env.currentIndentation;
        // Set indentation for braces.
        if (env.lineLength == 0) {
            // Set the indentation for statements starting with explicit anonymous function expression nodes.
            setIndentation(env.lineLength + prevIndentation);
        } else {
            setIndentation(env.lineLength);
        }

        NodeList<Token> qualifierList = formatNodeList(explicitAnonymousFunctionExpressionNode.qualifierList(),
                1, 0, 1, 0);
        Token functionKeyword = formatToken(explicitAnonymousFunctionExpressionNode.functionKeyword(), 0, 0);
        FunctionSignatureNode functionSignature =
                formatNode(explicitAnonymousFunctionExpressionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBody = formatNode(explicitAnonymousFunctionExpressionNode.functionBody(),
                env.trailingWS, env.trailingNL);

        setIndentation(prevIndentation);

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
        if (implicitNewExpressionNode.parenthesizedArgList().isPresent()) {
            Token newKeyword = formatToken(implicitNewExpressionNode.newKeyword(), 1, 0);
            ParenthesizedArgList parenthesizedArgList =
                    formatNode(implicitNewExpressionNode.parenthesizedArgList().get(), env.trailingWS,
                            env.trailingNL);
            return implicitNewExpressionNode.modify()
                    .withNewKeyword(newKeyword)
                    .withParenthesizedArgList(parenthesizedArgList)
                    .apply();
        }

        Token newKeyword = formatToken(implicitNewExpressionNode.newKeyword(), env.trailingWS,
                env.trailingNL);
        return implicitNewExpressionNode.modify()
                .withNewKeyword(newKeyword)
                .apply();
    }

    @Override
    public QueryConstructTypeNode transform(QueryConstructTypeNode queryConstructTypeNode) {
        if (queryConstructTypeNode.keySpecifier().isPresent()) {
            Token keyword = formatToken(queryConstructTypeNode.keyword(), 1, 0);
            KeySpecifierNode keySpecifier = formatNode(queryConstructTypeNode.keySpecifier().get(), env.trailingWS,
                    env.trailingNL);
            return queryConstructTypeNode.modify()
                    .withKeyword(keyword)
                    .withKeySpecifier(keySpecifier)
                    .apply();
        } else {
            Token keyword = formatToken(queryConstructTypeNode.keyword(), env.trailingWS,
                    env.trailingNL);
            return queryConstructTypeNode.modify()
                    .withKeyword(keyword)
                    .apply();
        }
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
        int prevIndentation = env.currentIndentation;
        // Set indentation for braces.
        if (env.lineLength == 0) {
            // Set the indentation for statements starting with query expression nodes.
            setIndentation(env.lineLength + prevIndentation);
        } else {
            setIndentation(env.lineLength);
        }

        if (queryExpressionNode.queryConstructType().isPresent()) {
            QueryConstructTypeNode queryConstructType = formatNode(queryExpressionNode.queryConstructType().get(),
                    1, 0);
            queryExpressionNode = queryExpressionNode.modify().withQueryConstructType(queryConstructType).apply();
        }

        QueryPipelineNode queryPipeline = formatNode(queryExpressionNode.queryPipeline(), 0, 1);
        SelectClauseNode selectClause;

        if (queryExpressionNode.onConflictClause().isPresent()) {
            selectClause = formatNode(queryExpressionNode.selectClause(), 0, 1);
            OnConflictClauseNode onConflictClause = formatNode(queryExpressionNode.onConflictClause().get(),
                    env.trailingWS, env.trailingNL);
            queryExpressionNode = queryExpressionNode.modify().withOnConflictClause(onConflictClause).apply();
        } else {
            selectClause = formatNode(queryExpressionNode.selectClause(), env.trailingWS, env.trailingNL);
        }
        setIndentation(prevIndentation);  // Revert indentation for braces

        return queryExpressionNode.modify()
                .withQueryPipeline(queryPipeline)
                .withSelectClause(selectClause)
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
        if (methodDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(methodDeclarationNode.metadata().get(), 0, 1);
            methodDeclarationNode = methodDeclarationNode.modify().withMetadata(metadata).apply();
        }

        NodeList<Token> qualifierList = formatNodeList(methodDeclarationNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword = formatToken(methodDeclarationNode.functionKeyword(), 1, 0);
        IdentifierToken methodName = formatNode(methodDeclarationNode.methodName(), 0, 0);
        FunctionSignatureNode methodSignature = formatNode(methodDeclarationNode.methodSignature(), 0, 0);
        Token semicolon = formatToken(methodDeclarationNode.semicolon(), env.trailingWS, env.trailingNL);

        return methodDeclarationNode.modify()
                .withQualifierList(qualifierList)
                .withFunctionKeyword(functionKeyword)
                .withMethodName(methodName)
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
            Node typeReference = formatNode(errorBindingPatternNode.typeReference().get(), 0, 0);
            errorBindingPatternNode = errorBindingPatternNode.modify().withTypeReference(typeReference).apply();
        } else {
            errorKeyword = formatToken(errorBindingPatternNode.errorKeyword(), 0, 0);
        }

        Token openParenthesis = formatToken(errorBindingPatternNode.openParenthesis(), 0, 0);
        SeparatedNodeList<BindingPatternNode> argListBindingPatterns =
                formatSeparatedNodeList(errorBindingPatternNode.argListBindingPatterns(), 0, 0, 0, 0);
        Token closeParenthesis = formatToken(errorBindingPatternNode.closeParenthesis(),
                env.trailingWS, env.trailingNL);

        return errorBindingPatternNode.modify()
                .withErrorKeyword(errorKeyword)

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
        SimpleNameReferenceNode receiveWorkers = formatNode(receiveActionNode.receiveWorkers(),
                env.trailingWS, env.trailingNL);

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
        int prevIndentation = env.currentIndentation;
        // Set indentation for braces.
        if (env.lineLength == 0) {
            // Set the indentation for statements starting with query expression nodes.
            setIndentation(env.lineLength + prevIndentation);
        } else {
            setIndentation(env.lineLength);
        }

        QueryPipelineNode queryPipeline = formatNode(queryActionNode.queryPipeline(), 0, 1);
        Token doKeyword = formatToken(queryActionNode.doKeyword(), 1, 0);
        BlockStatementNode blockStatement = formatNode(queryActionNode.blockStatement(),
                env.trailingWS, env.trailingNL);

        setIndentation(prevIndentation);  // Revert indentation for braces

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
        ExpressionNode lhsExpression = formatNode(conditionalExpressionNode.lhsExpression(), 1, 0);
        Token questionMarkToken = formatToken(conditionalExpressionNode.questionMarkToken(), 1, 0);
        ExpressionNode middleExpression = formatNode(conditionalExpressionNode.middleExpression(), 1, 0);
        Token colonToken = formatToken(conditionalExpressionNode.colonToken(), 1, 0);
        ExpressionNode endExpression = formatNode(conditionalExpressionNode.endExpression(),
                env.trailingWS, env.trailingNL);

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
            OnFailClauseNode onFailClause = formatNode(transactionStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            transactionStatementNode = transactionStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(transactionStatementNode.blockStatement(), env.trailingWS, env.trailingNL);
        }

        return transactionStatementNode.modify()
                .withTransactionKeyword(transactionKeyword)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public RollbackStatementNode transform(RollbackStatementNode rollbackStatementNode) {
        Token rollbackKeyword;

        if (rollbackStatementNode.expression().isPresent()) {
            rollbackKeyword = formatToken(rollbackStatementNode.rollbackKeyword(), 1, 0);
            ExpressionNode expression = formatNode(rollbackStatementNode.expression().get(), 0, 0);
            rollbackStatementNode = rollbackStatementNode.modify().withExpression(expression).apply();
        } else {
            rollbackKeyword = formatToken(rollbackStatementNode.rollbackKeyword(), 0, 0);
        }

        Token semicolon = formatToken(rollbackStatementNode.semicolon(), env.trailingWS, env.trailingNL);

        return rollbackStatementNode.modify()
                .withRollbackKeyword(rollbackKeyword)
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

        if (retryStatementNode.typeParameter().isPresent()) {
            TypeParameterNode typeParameter = formatNode(retryStatementNode.typeParameter().get(), 1, 0);
            retryStatementNode = retryStatementNode.modify().withTypeParameter(typeParameter).apply();
        }

        if (retryStatementNode.arguments().isPresent()) {
            ParenthesizedArgList arguments = formatNode(retryStatementNode.arguments().get(), 1, 0);
            retryStatementNode = retryStatementNode.modify().withArguments(arguments).apply();
        }
        StatementNode retryBody;

        if (retryStatementNode.onFailClause().isPresent()) {
            retryBody = formatNode(retryStatementNode.retryBody(), 1, 0);
            OnFailClauseNode onFailClause = formatNode(retryStatementNode.onFailClause().get(),
                    env.trailingWS, env.trailingNL);
            retryStatementNode = retryStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            retryBody = formatNode(retryStatementNode.retryBody(), env.trailingWS, env.trailingNL);
        }

        return retryStatementNode.modify()
                .withRetryKeyword(retryKeyword)
                .withRetryBody(retryBody)
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
    public ServiceConstructorExpressionNode transform(
            ServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        NodeList<AnnotationNode> annots = serviceConstructorExpressionNode.annotations();
        NodeList<AnnotationNode> annotations;
        if (annots.size() <= 1) {
            annotations = formatNodeList(serviceConstructorExpressionNode.annotations(), 1, 0, 1,
                    0);
        } else {
            annotations = formatNodeList(serviceConstructorExpressionNode.annotations(), 0, 1, 0,
                    1);
        }

        Token serviceKeyword = formatToken(serviceConstructorExpressionNode.serviceKeyword(), 1, 0);

        int prevIndentation = env.currentIndentation;
        // Set indentation for braces.
        int fieldIndentation = env.lineLength - serviceKeyword.text().length() - 1;
        setIndentation(fieldIndentation);

        Node serviceBody = formatNode(serviceConstructorExpressionNode.serviceBody(), env.trailingWS, env.trailingNL);

        setIndentation(prevIndentation);

        return serviceConstructorExpressionNode.modify()
                .withAnnotations(annotations)
                .withServiceKeyword(serviceKeyword)
                .withServiceBody(serviceBody)
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
        if (joinClauseNode.outerKeyword().isPresent()) {
            Token outerKeyword = formatToken(joinClauseNode.outerKeyword().get(), 1, 0);
            joinClauseNode = joinClauseNode.modify()
                    .withOuterKeyword(outerKeyword).apply();
        }

        Token joinKeyword = formatToken(joinClauseNode.joinKeyword(), 1, 0);
        TypedBindingPatternNode typedBindingPattern = formatNode(joinClauseNode.typedBindingPattern(), 1, 0);
        Token inKeyword = formatToken(joinClauseNode.inKeyword(), 1, 0);
        ExpressionNode expression = formatNode(joinClauseNode.expression(), 1, 0);
        OnClauseNode joinOnCondition = formatNode(joinClauseNode.joinOnCondition(), env.trailingWS, env.trailingNL);

        return joinClauseNode.modify()
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
        SeparatedNodeList<Node> matchPatterns;

        if (listMatchPatternNode.restMatchPattern().isPresent()) {
            matchPatterns = formatSeparatedNodeList(listMatchPatternNode.matchPatterns(), 0, 0, 1, 0);
            RestMatchPatternNode restMatchPattern = formatNode(listMatchPatternNode.restMatchPattern().get(), 0, 0);
            listMatchPatternNode = listMatchPatternNode.modify()
                    .withRestMatchPattern(restMatchPattern)
                    .apply();
        } else {
            matchPatterns = formatSeparatedNodeList(listMatchPatternNode.matchPatterns(), 0, 0, 0, 0);
        }

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
        SimpleNameReferenceNode fieldNameNode = formatNode(fieldMatchPatternNode.fieldNameNode(), 0, 0);
        Token colonToken = formatToken(fieldMatchPatternNode.colonToken(), 0, 0);
        Node matchPattern = formatNode(fieldMatchPatternNode.matchPattern(), env.trailingWS, env.trailingNL);

        return fieldMatchPatternNode.modify()
                .withFieldNameNode(fieldNameNode)
                .withColonToken(colonToken)
                .withMatchPattern(matchPattern)
                .apply();
    }

    @Override
    public ErrorMatchPatternNode transform(ErrorMatchPatternNode errorMatchPatternNode) {
        Token errorKeywordToken;

        if (errorMatchPatternNode.typeReference().isPresent()) {
            errorKeywordToken = formatToken(errorMatchPatternNode.errorKeyword(), 1, 0);
            NameReferenceNode typeRef = formatNode(errorMatchPatternNode.typeReference().get(), 0, 0);
            errorMatchPatternNode = errorMatchPatternNode.modify()
                    .withTypeReference(typeRef).apply();
        } else {
            errorKeywordToken = formatToken(errorMatchPatternNode.errorKeyword(), 0, 0);
        }

        Token openParenthesisToken = formatToken(errorMatchPatternNode.openParenthesisToken(), 0, 0);
        SeparatedNodeList<Node> argListMatchPatternNode =
                formatSeparatedNodeList(errorMatchPatternNode.argListMatchPatternNode(), 0, 0, 0, 0);
        Token closeParenthesisToken = formatToken(errorMatchPatternNode.closeParenthesisToken(),
                env.trailingWS, env.trailingNL);

        return errorMatchPatternNode.modify()
                .withErrorKeyword(errorKeywordToken)
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
            Token orderDirection = formatToken(orderKeyNode.orderDirection().get(), env.trailingWS, env.trailingNL);
            orderKeyNode = orderKeyNode.modify().withOrderDirection(orderDirection).apply();
        } else {
            expression = formatNode(orderKeyNode.expression(), env.trailingWS, env.trailingNL);
        }

        return orderKeyNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public ClassDefinitionNode transform(ClassDefinitionNode classDefinitionNode) {
        if (classDefinitionNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(classDefinitionNode.metadata().get(), 0, 1);
            classDefinitionNode = classDefinitionNode.modify().withMetadata(metadata).apply();
        }

        if (classDefinitionNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(classDefinitionNode.visibilityQualifier().get(), 1, 0);
            classDefinitionNode = classDefinitionNode.modify().withVisibilityQualifier(visibilityQualifier).apply();
        }

        NodeList<Token> classTypeQualifiers = formatNodeList(classDefinitionNode.classTypeQualifiers(), 1, 0, 1, 0);
        Token classKeyword = formatToken(classDefinitionNode.classKeyword(), 1, 0);
        Token className = formatToken(classDefinitionNode.className(), 1, 0);
        Token openBrace = formatToken(classDefinitionNode.openBrace(), 0, 1);

        indent();
        NodeList<Node> members = formatNodeList(classDefinitionNode.members(), 0, 1, 0, 1, true);
        unindent();
        Token closeBrace = formatToken(classDefinitionNode.closeBrace(), env.trailingWS, env.trailingNL);

        return classDefinitionNode.modify()
                .withClassTypeQualifiers(classTypeQualifiers)
                .withClassKeyword(classKeyword)
                .withClassName(className)
                .withOpenBrace(openBrace)
                .withMembers(members)
                .withCloseBrace(closeBrace)
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

        if (namedWorkerDeclarationNode.transactionalKeyword().isPresent()) {
            Token transactionalKeyword = formatToken(namedWorkerDeclarationNode.transactionalKeyword().get(), 1, 0);
            namedWorkerDeclarationNode = namedWorkerDeclarationNode.modify()
                    .withTransactionalKeyword(transactionalKeyword).apply();
        }

        Token workerKeyword = formatToken(namedWorkerDeclarationNode.workerKeyword(), 1, 0);
        IdentifierToken workerName = formatToken(namedWorkerDeclarationNode.workerName(), 1, 0);
        if (namedWorkerDeclarationNode.returnTypeDesc().isPresent()) {
            Node returnTypeDesc = formatNode(namedWorkerDeclarationNode.returnTypeDesc().get(), 1, 0);
            namedWorkerDeclarationNode = namedWorkerDeclarationNode.modify()
                    .withReturnTypeDesc(returnTypeDesc)
                    .apply();
        }

        BlockStatementNode workerBody = formatNode(namedWorkerDeclarationNode.workerBody(), env.trailingWS,
                env.trailingNL);

        return namedWorkerDeclarationNode.modify()
                .withAnnotations(annotations)
                .withWorkerKeyword(workerKeyword)
                .withWorkerName(workerName)
                .withWorkerBody(workerBody)
                .apply();
    }

    @Override
    public NamedWorkerDeclarator transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        NodeList<StatementNode> workerInitStatements = formatNodeList(namedWorkerDeclarator.workerInitStatements(), 0,
                1, 0, 1, true);
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                formatNodeList(namedWorkerDeclarator.namedWorkerDeclarations(), 0, 1, 0, 1, true);
        return namedWorkerDeclarator.modify()
                .withWorkerInitStatements(workerInitStatements)
                .withNamedWorkerDeclarations(namedWorkerDeclarations)
                .apply();
    }

    @Override
    public ArrayTypeDescriptorNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        TypeDescriptorNode memberTypeDesc = formatNode(arrayTypeDescriptorNode.memberTypeDesc(), 0, 0);
        Token openBracket = formatToken(arrayTypeDescriptorNode.openBracket(), 0, 0);
        if (arrayTypeDescriptorNode.arrayLength().isPresent()) {
            Node arrayLength = formatNode(arrayTypeDescriptorNode.arrayLength().get(), 0, 0);
            arrayTypeDescriptorNode = arrayTypeDescriptorNode.modify()
                    .withArrayLength(arrayLength)
                    .apply();
        }

        Token closeBracket = formatToken(arrayTypeDescriptorNode.closeBracket(), env.trailingWS, env.trailingNL);
        return arrayTypeDescriptorNode.modify()
                .withOpenBracket(openBracket)
                .withCloseBracket(closeBracket)
                .withMemberTypeDesc(memberTypeDesc)
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
    public XmlTypeDescriptorNode transform(XmlTypeDescriptorNode xmlTypeDescriptorNode) {
        Token xmlKeywordToken;
        if (xmlTypeDescriptorNode.xmlTypeParamsNode().isPresent()) {
            xmlKeywordToken = formatToken(xmlTypeDescriptorNode.xmlKeywordToken(), 0, 0);
            TypeParameterNode xmlTypeParamsNode = formatNode(xmlTypeDescriptorNode.xmlTypeParamsNode().get(),
                    env.trailingWS, env.trailingNL);
            xmlTypeDescriptorNode = xmlTypeDescriptorNode.modify()
                    .withXmlTypeParamsNode(xmlTypeParamsNode)
                    .apply();
        } else {
            xmlKeywordToken = formatToken(xmlTypeDescriptorNode.xmlKeywordToken(), env.trailingWS, env.trailingNL);
        }

        return xmlTypeDescriptorNode.modify()
                .withXmlKeywordToken(xmlKeywordToken)
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
     * @return Formatted node
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T formatNode(T node, int trailingWS, int trailingNL) {
        if (node == null || node.isMissing()) {
            return node;
        }

        if (!isInLineRange(node, lineRange)) {
            checkForNewline(node);
            return node;
        }

        int prevTrailingNL = env.trailingNL;
        int prevTrailingWS = env.trailingWS;
        env.trailingNL = trailingNL;
        env.trailingWS = trailingWS;

        // Cache the current node and parent before format.
        // Because reference to the nodes will change after modifying.
        T oldNode = node;
        Node parent = node.parent();

        node = (T) node.apply(this);
        if (shouldWrapLine(oldNode, parent)) {
            node = wrapLine(oldNode, parent);
        }

        env.trailingNL = prevTrailingNL;
        env.trailingWS = prevTrailingWS;
        return node;
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
        if (token == null || token.isMissing()) {
            return token;
        }

        if (!isInLineRange(token, lineRange)) {
            checkForNewline(token);
            return token;
        }

        int prevTrailingNL = env.trailingNL;
        int prevTrailingWS = env.trailingWS;

        // Trailing newlines can be at-most 1. Rest will go as newlines for the next token
        env.trailingNL = trailingNL > 0 ? 1 : 0;
        env.trailingWS = trailingWS;

        token = formatTokenInternal(token);

        // Set the leading newlines for the next token
        env.leadingNL = trailingNL > 0 ? trailingNL - 1 : 0;

        // If this node has a trailing new line, then the next immediate token
        // will become the first token the the next line
        env.hasNewline = trailingNL > 0;
        env.trailingNL = prevTrailingNL;
        env.trailingWS = prevTrailingWS;
        return token;
    }

    protected <T extends Node> NodeList<T> formatModuleMembers(NodeList<T> members) {
        if (members.isEmpty()) {
            return members;
        }

        boolean prevPreserveNL = env.preserveNewlines;
        boolean nodeModified = false;
        int size = members.size();
        Node[] newNodes = new Node[size];
        for (int index = 0; index < size; index++) {
            T currentMember = members.get(index);
            Node nextMember = null;
            if (index < size - 1) {
                nextMember = members.get(index + 1);
            }

            env.preserveNewlines = true;

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

        env.preserveNewlines = prevPreserveNL;
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
    protected <T extends Node> NodeList<T> formatNodeList(NodeList<T> nodeList,
                                                          int itemTrailingWS,
                                                          int itemTrailingNL,
                                                          int listTrailingWS,
                                                          int listTrailingNL) {
        return formatNodeList(nodeList, itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, false);
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
     * @param preserveNL Flag indicating whether to preserve the user added newlines
     * @return Formatted node list
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> NodeList<T> formatNodeList(NodeList<T> nodeList,
                                                          int itemTrailingWS,
                                                          int itemTrailingNL,
                                                          int listTrailingWS,
                                                          int listTrailingNL,
                                                          boolean preserveNL) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        int size = nodeList.size();
        Node[] newNodes = new Node[size];
        for (int index = 0; index < size; index++) {
            T oldNode = nodeList.get(index);
            T newNode;
            if (preserveNL) {
                boolean prevPreserveNL = env.preserveNewlines;
                env.preserveNewlines = preserveNL;
                newNode = formatListItem(itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, size, index,
                        oldNode);
                env.preserveNewlines = prevPreserveNL;
            } else {
                // If preserve newlines is false, then honour what is coming from the environment.
                newNode = formatListItem(itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, size, index,
                        oldNode);
            }

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
     * Format a delimited list of nodes.
     *
     * @param <T> Type of the list item
     * @param nodeList Node list to be formatted
     * @param itemTrailingWS Number of single-length spaces to be added after each item in the list
     * @param itemTrailingNL Number of newlines to be added after each item in the list
     * @param separatorTrailingWS Number of single-length spaces to be added after each separator in the list
     * @param separatorTrailingNL Number of newlines to be added after each each separator in the list
     * @param listTrailingWS Number of single-length spaces to be added after the last item in the list
     * @param listTrailingNL Number of newlines to be added after the last item in the list
     * @return Formatted node list
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> SeparatedNodeList<T> formatSeparatedNodeList(SeparatedNodeList<T> nodeList,
                                                                            int itemTrailingWS,
                                                                            int itemTrailingNL,
                                                                            int separatorTrailingWS,
                                                                            int separatorTrailingNL,
                                                                            int listTrailingWS,
                                                                            int listTrailingNL) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        int size = nodeList.size();
        Node[] newNodes = new Node[size * 2 - 1];

        for (int index = 0; index < size; index++) {
            T oldNode = nodeList.get(index);
            T newNode = formatListItem(itemTrailingWS, itemTrailingNL, listTrailingWS, listTrailingNL, size, index,
                    oldNode);
            newNodes[2 * index] = newNode;
            if (oldNode != newNode) {
                nodeModified = true;
            }

            if (index == nodeList.size() - 1) {
                break;
            }

            Token oldSeparator = nodeList.getSeparator(index);
            Token newSeparator = formatToken(oldSeparator, separatorTrailingWS, separatorTrailingNL);
            newNodes[(2 * index) + 1] = newSeparator;

            if (oldSeparator != newSeparator) {
                nodeModified = true;
            }

        }

        if (!nodeModified) {
            return nodeList;
        }

        return (SeparatedNodeList<T>) NodeFactory.createSeparatedNodeList(newNodes);
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

        if (env.preserveNewlines) {
            // We reach here for the first token in a list item (i.e: first token
            // after making 'env.preserveNewlines = true').
            // However, rest of the token in the same item don't need to preserve the newlines.
            env.preserveNewlines = false;
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
            // Therefore increase the 'consecutiveNewlines' count
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
                    if (!shouldAddLeadingNewline(prevMinutiae)) {
                        // Shouldn't update the prevMinutiae
                        continue;
                    }

                    if (consecutiveNewlines <= 1) {
                        consecutiveNewlines++;
                        leadingMinutiae.add(getNewline());
                        break;
                    }

                    continue;
                case WHITESPACE_MINUTIAE:
                    if (!shouldAddWS(prevMinutiae)) {
                        // Shouldn't update the prevMinutiae
                        continue;
                    }

                    addWhitespace(1, leadingMinutiae);
                    break;
                case COMMENT_MINUTIAE:
                    if (consecutiveNewlines == 0) {
                        // A comment without a leading newline is only possible if there is a explicit newline added
                        // by the user. So, it is being honored here.
                        leadingMinutiae.add(getNewline());
                    }
                    // Then add padding to match the current indentation level
                    addWhitespace(env.currentIndentation, leadingMinutiae);

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

        if (consecutiveNewlines > 0) {
            addWhitespace(env.currentIndentation, leadingMinutiae);
        }

        MinutiaeList newLeadingMinutiaeList = NodeFactory.createMinutiaeList(leadingMinutiae);
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
     * Check whether a leading newline needs to be added.
     *
     * @param prevMinutiae Minutiae that precedes the current token
     * @return <code>true</code> if a leading newline needs to be added. <code>false</code> otherwise
     */
    private boolean shouldAddLeadingNewline(Minutiae prevMinutiae) {
        if (env.preserveNewlines) {
            return true;
        }

        if (prevMinutiae == null) {
            return false;
        }

        switch (prevMinutiae.kind()) {
            case COMMENT_MINUTIAE:
            case INVALID_TOKEN_MINUTIAE_NODE:
            case INVALID_NODE_MINUTIAE:
                return true;
            default:
                return false;
        }
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
        if (env.trailingWS > 0) {
            addWhitespace(env.trailingWS, trailingMinutiae);
        }

        // Preserve the necessary trailing minutiae coming from the original token
        int consecutiveNewlines = 0;
        for (Minutiae minutiae : token.trailingMinutiae()) {
            switch (minutiae.kind()) {
                case END_OF_LINE_MINUTIAE:
                    if (!shouldAddTrailingNewline(prevMinutiae)) {
                        // Shouldn't update the prevMinutiae
                        continue;
                    }

                    trailingMinutiae.add(minutiae);
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

        if (consecutiveNewlines == 0 && env.trailingNL > 0) {
            trailingMinutiae.add(getNewline());
        }
        MinutiaeList newTrailingMinutiaeList = NodeFactory.createMinutiaeList(trailingMinutiae);
        return newTrailingMinutiaeList;
    }

    /**
     * Check whether a trailing newline needs to be added.
     *
     * @param prevMinutiae Minutiae that precedes the current token
     * @return <code>true</code> if a trailing newline needs to be added. <code>false</code> otherwise
     */
    private boolean shouldAddTrailingNewline(Minutiae prevMinutiae) {
        if (prevMinutiae == null) {
            return false;
        }

        switch (prevMinutiae.kind()) {
            case COMMENT_MINUTIAE:
            case INVALID_TOKEN_MINUTIAE_NODE:
            case INVALID_NODE_MINUTIAE:
                return true;
            default:
                return false;
        }
    }

    private Minutiae getNewline() {
        // reset the line length
        env.lineLength = 0;
        return NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL);
    }

    /**
     * Indent the code by the 4-whitespace characters.
     */
    private void indent() {
        env.currentIndentation += options.getTabSize();
    }

    /**
     * Undo the indentation of the code by the 4-whitespace characters.
     */
    private void unindent() {
        if (env.currentIndentation < options.getTabSize()) {
            env.currentIndentation = 0;
            return;
        }

        env.currentIndentation -= options.getTabSize();
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
     * Set the flag for setting inline annotations.
     *
     * @param value boolean true for setting inline annotations.
     */
    private void setInLineAnnotation(boolean value) {
        env.inLineAnnotation = value;
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
     * Check whether a node list needs to be expanded into multiple lines.
     *
     * @param nodeList node list
     * @return <code>true</code> If the node list needs to be expanded into multiple lines.
     *         <code>false</code> otherwise
     */
    private <T extends Node> boolean shouldExpand(NodeList<T> nodeList) {
        int fieldCount = nodeList.size();
        if (fieldCount <= 1) {
            return false;
        }

        if (fieldCount > 3) {
            return true;
        }

        for (Node field : nodeList) {
            TextRange textRange = field.textRange();
            if ((textRange.endOffset() - textRange.startOffset()) > 15) {
                return true;
            }

            if (hasNonWSMinutiae(field.leadingMinutiae()) || hasNonWSMinutiae(field.trailingMinutiae())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a object type descriptor needs to be expanded in to multiple lines.
     *
     * @param objectTypeDesc Object type descriptor
     * @return <code>true</code> If the object type descriptor needs to be expanded in to multiple lines.
     *         <code>false</code> otherwise
     */
    private boolean shouldExpand(ObjectTypeDescriptorNode objectTypeDesc) {
        if (objectTypeDesc.parent().kind() == SyntaxKind.TYPE_DEFINITION) {
            return true;
        }

        NodeList<Node> members = objectTypeDesc.members();
        return shouldExpandObjectMembers(members);
    }

    private boolean shouldExpandObjectMembers(NodeList<Node> members) {
        int fieldCount = members.size();
        if (fieldCount > 3) {
            return true;
        }

        for (Node member : members) {
            if (member.kind() == SyntaxKind.METHOD_DECLARATION) {
                return true;
            }

            TextRange textRange = member.textRange();
            if ((textRange.endOffset() - textRange.startOffset()) > 15) {
                return true;
            }

            if (hasNonWSMinutiae(member.leadingMinutiae()) || hasNonWSMinutiae(member.trailingMinutiae())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether a record type descriptor needs to be expanded in to multiple lines.
     *
     * @param recordTypeDesc Record type descriptor
     * @return <code>true</code> If the record type descriptor needs to be expanded in to multiple lines.
     *         <code>false</code> otherwise
     */
    private boolean shouldExpand(RecordTypeDescriptorNode recordTypeDesc) {
        if (recordTypeDesc.parent().kind() == SyntaxKind.TYPE_DEFINITION) {
            return true;
        }

        int fieldCount = recordTypeDesc.fields().size();
        fieldCount += recordTypeDesc.recordRestDescriptor().isPresent() ? 1 : 0;
        if (fieldCount > 3) {
            return true;
        }

        for (Node field : recordTypeDesc.fields()) {
            TextRange textRange = field.textRange();
            if ((textRange.endOffset() - textRange.startOffset()) > 15) {
                return true;
            }

            if (hasNonWSMinutiae(field.leadingMinutiae()) || hasNonWSMinutiae(field.trailingMinutiae())) {
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
}
