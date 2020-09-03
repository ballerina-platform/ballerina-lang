/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerinalang.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerinalang.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerinalang.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BreakStatementNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.CheckExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ClauseNode;
import io.ballerinalang.compiler.syntax.tree.CommitActionNode;
import io.ballerinalang.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerinalang.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ContinueStatementNode;
import io.ballerinalang.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerinalang.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.DocumentationReferenceNode;
import io.ballerinalang.compiler.syntax.tree.DoubleGTTokenNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.EnumMemberNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FailExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerinalang.compiler.syntax.tree.FieldMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.FlushActionNode;
import io.ballerinalang.compiler.syntax.tree.ForEachStatementNode;
import io.ballerinalang.compiler.syntax.tree.ForkStatementNode;
import io.ballerinalang.compiler.syntax.tree.FromClauseNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.FunctionalBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.FunctionalMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
import io.ballerinalang.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.InterpolationNode;
import io.ballerinalang.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.JoinClauseNode;
import io.ballerinalang.compiler.syntax.tree.KeySpecifierNode;
import io.ballerinalang.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerinalang.compiler.syntax.tree.LetClauseNode;
import io.ballerinalang.compiler.syntax.tree.LetExpressionNode;
import io.ballerinalang.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.LimitClauseNode;
import io.ballerinalang.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ListMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.LocalTypeDefinitionStatementNode;
import io.ballerinalang.compiler.syntax.tree.LockStatementNode;
import io.ballerinalang.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerinalang.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerinalang.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerinalang.compiler.syntax.tree.MatchClauseNode;
import io.ballerinalang.compiler.syntax.tree.MatchGuardNode;
import io.ballerinalang.compiler.syntax.tree.MatchStatementNode;
import io.ballerinalang.compiler.syntax.tree.MetadataNode;
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerinalang.compiler.syntax.tree.NilLiteralNode;
import io.ballerinalang.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.ObjectFieldNode;
import io.ballerinalang.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.OnClauseNode;
import io.ballerinalang.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerinalang.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.OrderByClauseNode;
import io.ballerinalang.compiler.syntax.tree.OrderKeyNode;
import io.ballerinalang.compiler.syntax.tree.PanicStatementNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.QueryActionNode;
import io.ballerinalang.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerinalang.compiler.syntax.tree.QueryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.QueryPipelineNode;
import io.ballerinalang.compiler.syntax.tree.ReceiveActionNode;
import io.ballerinalang.compiler.syntax.tree.ReceiveFieldsNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.RestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.RetryStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RollbackStatementNode;
import io.ballerinalang.compiler.syntax.tree.SelectClauseNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.ServiceBodyNode;
import io.ballerinalang.compiler.syntax.tree.ServiceConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SpreadFieldNode;
import io.ballerinalang.compiler.syntax.tree.StartActionNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.SyncSendActionNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TemplateMemberNode;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TransactionStatementNode;
import io.ballerinalang.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TrapExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;
import io.ballerinalang.compiler.syntax.tree.TrippleGTTokenNode;
import io.ballerinalang.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypeCastParamNode;
import io.ballerinalang.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeParameterNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceTypeDescNode;
import io.ballerinalang.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.TypedescTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.WaitActionNode;
import io.ballerinalang.compiler.syntax.tree.WaitFieldNode;
import io.ballerinalang.compiler.syntax.tree.WaitFieldsListNode;
import io.ballerinalang.compiler.syntax.tree.WhereClauseNode;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;
import io.ballerinalang.compiler.syntax.tree.WildcardBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.XMLAtomicNamePatternNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeValue;
import io.ballerinalang.compiler.syntax.tree.XMLComment;
import io.ballerinalang.compiler.syntax.tree.XMLElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEmptyElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEndTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerinalang.compiler.syntax.tree.XMLItemNode;
import io.ballerinalang.compiler.syntax.tree.XMLNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLNamePatternChainingNode;
import io.ballerinalang.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerinalang.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLStartTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerinalang.compiler.syntax.tree.XMLTextNode;
import io.ballerinalang.compiler.syntax.tree.XmlTypeDescriptorNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Modifies the given tree to format the nodes.
 *
 * @since 2.0.0
 */
public class FormattingTreeModifier extends TreeModifier {

    private FormattingOptions formattingOptions;
    private LineRange lineRange;

    @Override
    public ImportDeclarationNode transform(ImportDeclarationNode importDeclarationNode) {
        if (!isInLineRange(importDeclarationNode)) {
            return importDeclarationNode;
        }
        Token importKeyword = getToken(importDeclarationNode.importKeyword());
        Token semicolon = getToken(importDeclarationNode.semicolon());
        SeparatedNodeList<IdentifierToken> moduleNames = this.modifySeparatedNodeList(
                importDeclarationNode.moduleName());
        ImportOrgNameNode orgName = this.modifyNode(importDeclarationNode.orgName().orElse(null));
        ImportPrefixNode prefix = this.modifyNode(importDeclarationNode.prefix().orElse(null));
        ImportVersionNode version = this.modifyNode(importDeclarationNode.version().orElse(null));
        if (orgName != null) {
            importDeclarationNode = importDeclarationNode.modify()
                    .withOrgName(orgName).apply();
        }
        if (prefix != null) {
            importDeclarationNode = importDeclarationNode.modify()
                    .withPrefix(prefix).apply();
        }
        if (version != null) {
            importDeclarationNode = importDeclarationNode.modify()
                    .withVersion(version).apply();
        }
        return importDeclarationNode.modify()
                .withImportKeyword(formatToken(importKeyword, 0, 0, 0, 0))
                .withModuleName(moduleNames)
                .withSemicolon(formatToken(semicolon, 0, 0, 0, 1))
                .apply();
    }

    @Override
    public ImportOrgNameNode transform(ImportOrgNameNode importOrgNameNode) {
        if (!isInLineRange(importOrgNameNode)) {
            return importOrgNameNode;
        }
        Token orgName = getToken(importOrgNameNode.orgName());
        Token slashToken = getToken(importOrgNameNode.slashToken());
        return importOrgNameNode.modify()
                .withOrgName(formatToken(orgName, 1, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportPrefixNode transform(ImportPrefixNode importPrefixNode) {
        if (!isInLineRange(importPrefixNode)) {
            return importPrefixNode;
        }
        Token asKeyword = getToken(importPrefixNode.asKeyword());
        Token prefix = getToken(importPrefixNode.prefix());
        return importPrefixNode.modify()
                .withAsKeyword(formatToken(asKeyword, 1, 0, 0, 0))
                .withPrefix(formatToken(prefix, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ImportVersionNode transform(ImportVersionNode importVersionNode) {
        if (!isInLineRange(importVersionNode)) {
            return importVersionNode;
        }
        Token versionKeyword = getToken(importVersionNode.versionKeyword());
        SeparatedNodeList<Token> versionNumber = this.modifySeparatedNodeList(importVersionNode.versionNumber());
        return importVersionNode.modify()
                .withVersionKeyword(formatToken(versionKeyword, 1, 1, 0, 0))
                .withVersionNumber(versionNumber)
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        if (!isInLineRange(identifier)) {
            return identifier;
        }
        Token identifierToken = getToken(identifier);
        return (IdentifierToken) formatToken(identifierToken, 0, 0, 0, 0);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        if (!isInLineRange(functionDefinitionNode)) {
            return functionDefinitionNode;
        }
        MetadataNode metadata = this.modifyNode(functionDefinitionNode.metadata().orElse(null));
        NodeList<Token> qualifierList = this.modifyNodeList(functionDefinitionNode.qualifierList());
        Token functionKeyword = getToken(functionDefinitionNode.functionKeyword());
        Token functionName = getToken(functionDefinitionNode.functionName());
        FunctionSignatureNode functionSignatureNode = this.modifyNode(functionDefinitionNode.functionSignature());
        FunctionBodyNode functionBodyNode = this.modifyNode(functionDefinitionNode.functionBody());
        if (metadata != null) {
            functionDefinitionNode = functionDefinitionNode.modify()
                    .withMetadata(metadata).apply();
        }
        return functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 0, 0, 0))
                .withFunctionName((IdentifierToken) formatToken(functionName, 1, 0, 0, 0))
                .withFunctionSignature(functionSignatureNode)
                .withQualifierList(qualifierList)
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public FunctionSignatureNode transform(FunctionSignatureNode functionSignatureNode) {
        if (!isInLineRange(functionSignatureNode)) {
            return functionSignatureNode;
        }
        Token openPara = getToken(functionSignatureNode.openParenToken());
        Token closePara = getToken(functionSignatureNode.closeParenToken());
        SeparatedNodeList<ParameterNode> parameters = this.modifySeparatedNodeList(functionSignatureNode.parameters());
        ReturnTypeDescriptorNode returnTypeDesc = this.modifyNode(functionSignatureNode.returnTypeDesc().orElse(null));
        if (returnTypeDesc != null) {
            functionSignatureNode = functionSignatureNode.modify()
                    .withReturnTypeDesc(returnTypeDesc).apply();
        }
        return functionSignatureNode.modify()
                .withOpenParenToken(formatToken(openPara, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closePara, 0, 0, 0, 0))
                .withParameters(parameters)
                .apply();
    }

    @Override
    public ReturnTypeDescriptorNode transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        if (!isInLineRange(returnTypeDescriptorNode)) {
            return returnTypeDescriptorNode;
        }
        Token returnsKeyword = getToken(returnTypeDescriptorNode.returnsKeyword());
        NodeList<AnnotationNode> annotations = this.modifyNodeList(returnTypeDescriptorNode.annotations());
        Node type = this.modifyNode(returnTypeDescriptorNode.type());
        return returnTypeDescriptorNode.modify()
                .withAnnotations(annotations)
                .withReturnsKeyword(formatToken(returnsKeyword, 1, 1, 0, 0))
                .withType(type)
                .apply();
    }

    @Override
    public OptionalTypeDescriptorNode transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        if (!isInLineRange(optionalTypeDescriptorNode)) {
            return optionalTypeDescriptorNode;
        }
        Node typeDescriptor = this.modifyNode(optionalTypeDescriptorNode.typeDescriptor());
        Token questionMarkToken = getToken(optionalTypeDescriptorNode.questionMarkToken());
        return optionalTypeDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withQuestionMarkToken(formatToken(questionMarkToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        if (!isInLineRange(requiredParameterNode)) {
            return requiredParameterNode;
        }
        Token paramName = getToken(requiredParameterNode.paramName().orElse(null));
        Token visibilityQualifier = getToken(requiredParameterNode.visibilityQualifier().orElse(null));
        NodeList<AnnotationNode> annotations = this.modifyNodeList(requiredParameterNode.annotations());
        Node typeName = this.modifyNode(requiredParameterNode.typeName());
        if (paramName != null) {
            requiredParameterNode = requiredParameterNode.modify()
                    .withParamName(formatToken(paramName, 1, 0, 0, 0)).apply();
        }
        if (visibilityQualifier != null) {
            requiredParameterNode = requiredParameterNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 0, 0, 0)).apply();
        }
        return requiredParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .apply();
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        if (!isInLineRange(builtinSimpleNameReferenceNode)) {
            return builtinSimpleNameReferenceNode;
        }
        int startCol = getStartColumn(builtinSimpleNameReferenceNode, builtinSimpleNameReferenceNode.kind(), true);
        Token name = getToken(builtinSimpleNameReferenceNode.name());
        return builtinSimpleNameReferenceNode.modify()
                .withName(formatToken(name, startCol, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        if (!isInLineRange(functionBodyBlockNode)) {
            return functionBodyBlockNode;
        }
        int startColumn = getStartColumn(functionBodyBlockNode, functionBodyBlockNode.kind(), false);
        Token functionBodyOpenBrace = getToken(functionBodyBlockNode.openBraceToken());
        Token functionBodyCloseBrace = getToken(functionBodyBlockNode.closeBraceToken());
        NodeList<StatementNode> statements = this.modifyNodeList(functionBodyBlockNode.statements());
        NamedWorkerDeclarator namedWorkerDeclarator =
                this.modifyNode(functionBodyBlockNode.namedWorkerDeclarator().orElse(null));
        if (namedWorkerDeclarator != null) {
            functionBodyBlockNode = functionBodyBlockNode.modify()
                    .withNamedWorkerDeclarator(namedWorkerDeclarator).apply();
        }
        return functionBodyBlockNode.modify()
                .withOpenBraceToken(formatToken(functionBodyOpenBrace, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(functionBodyCloseBrace, startColumn, 0, 0, 1))
                .withStatements(statements)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        if (!isInLineRange(expressionStatementNode)) {
            return expressionStatementNode;
        }
        ExpressionNode expression = this.modifyNode(expressionStatementNode.expression());
        Token semicolonToken = expressionStatementNode.semicolonToken();
        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        if (!isInLineRange(functionCallExpressionNode)) {
            return functionCallExpressionNode;
        }
        NameReferenceNode functionName = this.modifyNode(functionCallExpressionNode.functionName());
        Token functionCallOpenPara = getToken(functionCallExpressionNode.openParenToken());
        Token functionCallClosePara = getToken(functionCallExpressionNode.closeParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(functionCallExpressionNode
                .arguments());
        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .withOpenParenToken(formatToken(functionCallOpenPara, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(functionCallClosePara, 0, 0, 0, 0))
                .withArguments(arguments)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        if (!isInLineRange(qualifiedNameReferenceNode)) {
            return qualifiedNameReferenceNode;
        }
        int startCol = getStartColumn(qualifiedNameReferenceNode, qualifiedNameReferenceNode.kind(), false);
        Token modulePrefix = getToken(qualifiedNameReferenceNode.modulePrefix());
        Token identifier = getToken(qualifiedNameReferenceNode.identifier());
        Token colon = getToken((Token) qualifiedNameReferenceNode.colon());
        return qualifiedNameReferenceNode.modify()
                .withModulePrefix(formatToken(modulePrefix, startCol, 0, 0, 0))
                .withIdentifier((IdentifierToken) formatToken(identifier, 0, 0, 0, 0))
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public PositionalArgumentNode transform(PositionalArgumentNode positionalArgumentNode) {
        if (!isInLineRange(positionalArgumentNode)) {
            return positionalArgumentNode;
        }
        ExpressionNode expression = this.modifyNode(positionalArgumentNode.expression());
        return positionalArgumentNode.modify()
                .withExpression(expression)
                .apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        if (!isInLineRange(basicLiteralNode)) {
            return basicLiteralNode;
        }
        Token literalToken = getToken(basicLiteralNode.literalToken());
        return basicLiteralNode.modify()
                .withLiteralToken(formatToken(literalToken, 0, 0, 0, 0))
                .apply();
    }


    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        if (!isInLineRange(serviceDeclarationNode)) {
            return serviceDeclarationNode;
        }
        Token serviceKeyword = getToken(serviceDeclarationNode.serviceKeyword());
        IdentifierToken serviceName = (IdentifierToken) getToken(serviceDeclarationNode.serviceName());
        Token onKeyword = getToken(serviceDeclarationNode.onKeyword());
        MetadataNode metadata = this.modifyNode(serviceDeclarationNode.metadata().orElse(null));
        SeparatedNodeList<ExpressionNode> expressions =
                this.modifySeparatedNodeList(serviceDeclarationNode.expressions());
        Node serviceBody = this.modifyNode(serviceDeclarationNode.serviceBody());
        if (metadata != null) {
            serviceDeclarationNode = serviceDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return serviceDeclarationNode.modify()
                .withServiceKeyword(formatToken(serviceKeyword, 0, 0, 1, 0))
                .withServiceName((IdentifierToken) formatToken(serviceName, 1, 0, 0, 0))
                .withOnKeyword(formatToken(onKeyword, 1, 0, 0, 0))
                .withExpressions(expressions)
                .withServiceBody(serviceBody)
                .apply();
    }

    @Override
    public ServiceBodyNode transform(ServiceBodyNode serviceBodyNode) {
        if (!isInLineRange(serviceBodyNode)) {
            return serviceBodyNode;
        }
        Token openBraceToken = getToken(serviceBodyNode.openBraceToken());
        Token closeBraceToken = getToken(serviceBodyNode.closeBraceToken());
        NodeList<Node> resources = this.modifyNodeList(serviceBodyNode.resources());
        return serviceBodyNode.modify()
                .withOpenBraceToken(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(closeBraceToken, 0, 0, 0, 1))
                .withResources(resources)
                .apply();
    }

    @Override
    public ExplicitNewExpressionNode transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        if (!isInLineRange(explicitNewExpressionNode)) {
            return explicitNewExpressionNode;
        }
        Token newKeywordToken = getToken(explicitNewExpressionNode.newKeyword());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(explicitNewExpressionNode.typeDescriptor());
        return explicitNewExpressionNode.modify()
                .withNewKeyword(formatToken(newKeywordToken, 1, 1, 0, 0))
                .withParenthesizedArgList(modifyNode(explicitNewExpressionNode.parenthesizedArgList()))
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public ParenthesizedArgList transform(ParenthesizedArgList parenthesizedArgList) {
        if (!isInLineRange(parenthesizedArgList)) {
            return parenthesizedArgList;
        }
        Token openParenToken = getToken(parenthesizedArgList.openParenToken());
        Token closeParenToken = getToken(parenthesizedArgList.closeParenToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(parenthesizedArgList
                .arguments());
        return parenthesizedArgList.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
        if (!isInLineRange(variableDeclarationNode)) {
            return variableDeclarationNode;
        }
        Token semicolonToken = getToken(variableDeclarationNode.semicolonToken());
        Token equalToken = getToken(variableDeclarationNode.equalsToken().orElse(null));
        Token finalToken = getToken(variableDeclarationNode.finalKeyword().orElse(null));
        ExpressionNode initializer = this.modifyNode(variableDeclarationNode.initializer().orElse(null));
        NodeList<AnnotationNode> annotationNodes = this.modifyNodeList(variableDeclarationNode.annotations());
        TypedBindingPatternNode typedBindingPatternNode = this.modifyNode(
                variableDeclarationNode.typedBindingPattern());
        if (equalToken != null) {
            variableDeclarationNode = variableDeclarationNode.modify()
                    .withEqualsToken(formatToken(equalToken, 1, 1, 0, 0)).apply();
        }
        if (finalToken != null) {
            variableDeclarationNode = variableDeclarationNode.modify()
                    .withFinalKeyword(formatToken(finalToken, 0, 0, 0, 0)).apply();
        }
        if (initializer != null) {
            variableDeclarationNode = variableDeclarationNode.modify()
                    .withInitializer(initializer).apply();
        }
        return variableDeclarationNode.modify()
                .withAnnotations(annotationNodes)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .withTypedBindingPattern(typedBindingPatternNode)
                .apply();
    }

    @Override
    public TypedBindingPatternNode transform(TypedBindingPatternNode typedBindingPatternNode) {
        if (!isInLineRange(typedBindingPatternNode)) {
            return typedBindingPatternNode;
        }
        BindingPatternNode bindingPatternNode = this.modifyNode(typedBindingPatternNode.bindingPattern());
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(typedBindingPatternNode.typeDescriptor());
        return typedBindingPatternNode.modify()
                .withBindingPattern(bindingPatternNode)
                .withTypeDescriptor(typeDescriptorNode)
                .apply();
    }

    @Override
    public CaptureBindingPatternNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        if (!isInLineRange(captureBindingPatternNode)) {
            return captureBindingPatternNode;
        }
        Token variableName = getToken(captureBindingPatternNode.variableName());
        return captureBindingPatternNode.modify()
                .withVariableName(formatToken(variableName, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public ListBindingPatternNode transform(ListBindingPatternNode listBindingPatternNode) {
        if (!isInLineRange(listBindingPatternNode)) {
            return listBindingPatternNode;
        }
        SeparatedNodeList<BindingPatternNode> bindingPatternNodes = this.modifySeparatedNodeList(
                listBindingPatternNode.bindingPatterns());
        Token openBracket = getToken(listBindingPatternNode.openBracket());
        Token closeBracket = getToken(listBindingPatternNode.closeBracket());
        RestBindingPatternNode restBindingPattern =
                this.modifyNode(listBindingPatternNode.restBindingPattern().orElse(null));
        if (restBindingPattern != null) {
            listBindingPatternNode = listBindingPatternNode.modify()
                    .withRestBindingPattern(restBindingPattern).apply();
        }
        return listBindingPatternNode.modify()
                .withBindingPatterns(bindingPatternNodes)
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public MappingBindingPatternNode transform(MappingBindingPatternNode mappingBindingPatternNode) {
        if (!isInLineRange(mappingBindingPatternNode)) {
            return mappingBindingPatternNode;
        }
        Token openBraceToken = getToken(mappingBindingPatternNode.openBrace());
        Token closeBraceToken = getToken(mappingBindingPatternNode.closeBrace());
        SeparatedNodeList<FieldBindingPatternNode> fieldBindingPatternNodes =
                this.modifySeparatedNodeList(mappingBindingPatternNode.fieldBindingPatterns());
        RestBindingPatternNode restBindingPattern =
                this.modifyNode(mappingBindingPatternNode.restBindingPattern().orElse(null));
        if (restBindingPattern != null) {
            mappingBindingPatternNode = mappingBindingPatternNode.modify()
                    .withRestBindingPattern(restBindingPattern).apply();
        }
        return mappingBindingPatternNode.modify()
                .withOpenBrace(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBrace(formatToken(closeBraceToken, 0, 0, 1, 0))
                .withFieldBindingPatterns(fieldBindingPatternNodes)
                .apply();
    }

    @Override
    public FieldBindingPatternFullNode transform(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        if (!isInLineRange(fieldBindingPatternFullNode)) {
            return fieldBindingPatternFullNode;
        }
        Token colon = getToken(fieldBindingPatternFullNode.colon());
        BindingPatternNode bindingPatternNode = this.modifyNode(fieldBindingPatternFullNode.bindingPattern());
        SimpleNameReferenceNode variableName = this.modifyNode(fieldBindingPatternFullNode.variableName());
        return fieldBindingPatternFullNode.modify()
                .withBindingPattern(bindingPatternNode)
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public FieldBindingPatternVarnameNode transform(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        if (!isInLineRange(fieldBindingPatternVarnameNode)) {
            return fieldBindingPatternVarnameNode;
        }
        SimpleNameReferenceNode variableName = this.modifyNode(fieldBindingPatternVarnameNode.variableName());
        return fieldBindingPatternVarnameNode.modify()
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RestBindingPatternNode transform(RestBindingPatternNode restBindingPatternNode) {
        if (!isInLineRange(restBindingPatternNode)) {
            return restBindingPatternNode;
        }
        Token ellipsisToken = getToken(restBindingPatternNode.ellipsisToken());
        SimpleNameReferenceNode variableName = restBindingPatternNode.variableName();
        return restBindingPatternNode.modify()
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public RemoteMethodCallActionNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        if (!isInLineRange(remoteMethodCallActionNode)) {
            return remoteMethodCallActionNode;
        }
        Token openParenToken = getToken(remoteMethodCallActionNode.openParenToken());
        Token closeParenToken = getToken(remoteMethodCallActionNode.closeParenToken());
        Token rightArrowToken = getToken(remoteMethodCallActionNode.rightArrowToken());
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(remoteMethodCallActionNode
                .arguments());
        ExpressionNode expression = this.modifyNode(remoteMethodCallActionNode.expression());
        SimpleNameReferenceNode methodName = this.modifyNode(remoteMethodCallActionNode.methodName());
        return remoteMethodCallActionNode.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .withExpression(expression)
                .withMethodName(methodName)
                .withRightArrowToken(formatToken(rightArrowToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public SimpleNameReferenceNode transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        if (!isInLineRange(simpleNameReferenceNode)) {
            return simpleNameReferenceNode;
        }
        Token name = getToken(simpleNameReferenceNode.name());
        return simpleNameReferenceNode.modify()
                .withName(formatToken(name, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {
        if (!isInLineRange(ifElseStatementNode)) {
            return ifElseStatementNode;
        }
        BlockStatementNode ifBody = this.modifyNode(ifElseStatementNode.ifBody());
        ExpressionNode condition = this.modifyNode(ifElseStatementNode.condition());
        Token ifKeyword = getToken(ifElseStatementNode.ifKeyword());
        Node elseBody = this.modifyNode(ifElseStatementNode.elseBody().orElse(null));

        int startColumn = 1;
        if (ifElseStatementNode.parent().kind() != SyntaxKind.ELSE_BLOCK) {
            startColumn = getStartColumn(ifElseStatementNode, ifElseStatementNode.kind(), true);
        }
        if (elseBody != null) {
            ifElseStatementNode = ifElseStatementNode.modify()
                    .withElseBody(elseBody).apply();
        }
        return ifElseStatementNode.modify()
                .withIfKeyword(formatToken(ifKeyword, startColumn, 0, 0, 0))
                .withIfBody(ifBody)
                .withCondition(condition)
                .apply();
    }

    @Override
    public ElseBlockNode transform(ElseBlockNode elseBlockNode) {
        if (!isInLineRange(elseBlockNode)) {
            return elseBlockNode;
        }
        Token elseKeyword = getToken(elseBlockNode.elseKeyword());
        StatementNode elseBody = this.modifyNode(elseBlockNode.elseBody());
        return elseBlockNode.modify()
                .withElseKeyword(formatToken(elseKeyword, 1, 0, 0, 0))
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public BracedExpressionNode transform(BracedExpressionNode bracedExpressionNode) {
        if (!isInLineRange(bracedExpressionNode)) {
            return bracedExpressionNode;
        }
        Token openParen = getToken(bracedExpressionNode.openParen());
        Token closeParen = getToken(bracedExpressionNode.closeParen());
        ExpressionNode expression = this.modifyNode(bracedExpressionNode.expression());
        return bracedExpressionNode.modify()
                .withOpenParen(formatToken(openParen, 1, 0, 0, 0))
                .withCloseParen(formatToken(closeParen, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public TypeTestExpressionNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        if (!isInLineRange(typeTestExpressionNode)) {
            return typeTestExpressionNode;
        }
        ExpressionNode expression = this.modifyNode(typeTestExpressionNode.expression());
        Node typeDescriptor = this.modifyNode(typeTestExpressionNode.typeDescriptor());
        Token isToken = getToken(typeTestExpressionNode.isKeyword());
        return typeTestExpressionNode.modify()
                .withExpression(expression)
                .withIsKeyword(formatToken(isToken, 1, 1, 0, 0))
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public ErrorTypeDescriptorNode transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        if (!isInLineRange(errorTypeDescriptorNode)) {
            return errorTypeDescriptorNode;
        }
        Token errorKeywordToken = getToken(errorTypeDescriptorNode.errorKeywordToken());
        ErrorTypeParamsNode errorTypeParamsNode =
                this.modifyNode(errorTypeDescriptorNode.errorTypeParamsNode().orElse(null));
        if (errorTypeParamsNode != null) {
            errorTypeDescriptorNode = errorTypeDescriptorNode.modify()
                    .withErrorTypeParamsNode(errorTypeParamsNode).apply();
        }
        return errorTypeDescriptorNode.modify()
                .withErrorKeywordToken(formatToken(errorKeywordToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ModuleVariableDeclarationNode transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        if (!isInLineRange(moduleVariableDeclarationNode)) {
            return moduleVariableDeclarationNode;
        }
        Token equalsToken = getToken(moduleVariableDeclarationNode.equalsToken());
        Token semicolonToken = getToken(moduleVariableDeclarationNode.semicolonToken());
        Token finalKeyword = getToken(moduleVariableDeclarationNode.finalKeyword().orElse(null));
        MetadataNode metadata = this.modifyNode(moduleVariableDeclarationNode.metadata().orElse(null));
        ExpressionNode initializer = this.modifyNode(moduleVariableDeclarationNode.initializer());
        if (metadata != null) {
            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        if (finalKeyword != null) {
            moduleVariableDeclarationNode = moduleVariableDeclarationNode.modify()
                    .withFinalKeyword(formatToken(finalKeyword, 0, 1, 0, 0)).apply();
        }
        return moduleVariableDeclarationNode.modify()
                .withTypedBindingPattern(this.modifyNode(moduleVariableDeclarationNode.typedBindingPattern()))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 2))
                .apply();
    }

    @Override
    public ConstantDeclarationNode transform(ConstantDeclarationNode constantDeclarationNode) {
        if (!isInLineRange(constantDeclarationNode)) {
            return constantDeclarationNode;
        }
        Token constKeyword = getToken(constantDeclarationNode.constKeyword());
        Token variableName = getToken(constantDeclarationNode.variableName());
        Token equalsToken = getToken(constantDeclarationNode.equalsToken());
        Token semicolonToken = getToken(constantDeclarationNode.semicolonToken());
        Token visibilityQualifier = getToken(constantDeclarationNode.visibilityQualifier().orElse(null));
        Node initializer = this.modifyNode(constantDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(constantDeclarationNode.metadata().orElse(null));
        TypeDescriptorNode typeDescriptorNode = this.modifyNode(constantDeclarationNode.typeDescriptor().orElse(null));
        if (metadata != null) {
            constantDeclarationNode = constantDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return constantDeclarationNode.modify()
                .withVisibilityQualifier(formatToken(visibilityQualifier, 1, 1, 0, 0))
                .withConstKeyword(formatToken(constKeyword, 1, 1, 0, 0))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withSemicolonToken(formatToken(semicolonToken, 1, 1, 0, 1))
                .withTypeDescriptor(typeDescriptorNode)
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public MetadataNode transform(MetadataNode metadataNode) {
        if (!isInLineRange(metadataNode)) {
            return metadataNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(metadataNode.annotations());
        Node documentationString = metadataNode.documentationString().orElse(null);
        if (documentationString != null) {
            metadataNode = metadataNode.modify()
                    .withDocumentationString(this.modifyNode(documentationString)).apply();
        }
        return metadataNode.modify()
                .withAnnotations(annotations)
                .apply();
    }

    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {
        if (!isInLineRange(blockStatementNode)) {
            return blockStatementNode;
        }
        int startColumn = getStartColumn(blockStatementNode, blockStatementNode.kind(), false);
        Token openBraceToken = getToken(blockStatementNode.openBraceToken());
        Token closeBraceToken = getToken(blockStatementNode.closeBraceToken());
        NodeList<StatementNode> statements = this.modifyNodeList(blockStatementNode.statements());

        int trailingNewLines = 1;
        if (blockStatementNode.parent() != null && blockStatementNode.parent().kind() == SyntaxKind.IF_ELSE_STATEMENT) {
            IfElseStatementNode ifElseStatementNode = (IfElseStatementNode) blockStatementNode.parent();
            if (ifElseStatementNode.elseBody().isPresent()) {
                trailingNewLines = 0;
            }
        }
        return blockStatementNode.modify()
                .withOpenBraceToken(formatToken(openBraceToken, 1, 0, 0, 1))
                .withCloseBraceToken(formatToken(closeBraceToken, startColumn, 0, 0, trailingNewLines))
                .withStatements(statements)
                .apply();
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        if (!isInLineRange(mappingConstructorExpressionNode)) {
            return mappingConstructorExpressionNode;
        }
        int startColumn = getStartColumn(mappingConstructorExpressionNode, mappingConstructorExpressionNode.kind(),
                false);
        Token openBrace = getToken(mappingConstructorExpressionNode.openBrace());
        Token closeBrace = getToken(mappingConstructorExpressionNode.closeBrace());
        SeparatedNodeList<MappingFieldNode> fields = this.modifySeparatedNodeList(
                mappingConstructorExpressionNode.fields());
        return mappingConstructorExpressionNode.modify()
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 1))
                .withCloseBrace(formatToken(closeBrace, startColumn, 0, 1, 0))
                .withFields(fields)
                .apply();
    }

    @Override
    public ListenerDeclarationNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        if (!isInLineRange(listenerDeclarationNode)) {
            return listenerDeclarationNode;
        }
        Token equalsToken = getToken(listenerDeclarationNode.equalsToken());
        Token variableName = getToken(listenerDeclarationNode.variableName());
        Token semicolonToken = getToken(listenerDeclarationNode.semicolonToken());
        Token listenerKeyword = getToken(listenerDeclarationNode.listenerKeyword());
        Token visibilityQualifier = getToken(listenerDeclarationNode.visibilityQualifier().orElse(null));
        Node initializer = this.modifyNode(listenerDeclarationNode.initializer());
        MetadataNode metadata = this.modifyNode(listenerDeclarationNode.metadata().orElse(null));
        Node typeDescriptor = this.modifyNode(listenerDeclarationNode.typeDescriptor());
        if (visibilityQualifier != null) {
            listenerDeclarationNode = listenerDeclarationNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 0, 0, 0)).apply();
        }
        if (metadata != null) {
            listenerDeclarationNode = listenerDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return listenerDeclarationNode.modify()
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withInitializer(initializer)
                .withListenerKeyword(formatToken(listenerKeyword, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .withTypeDescriptor(typeDescriptor)
                .withVariableName(formatToken(variableName, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public SpecificFieldNode transform(SpecificFieldNode specificFieldNode) {
        if (!isInLineRange(specificFieldNode)) {
            return specificFieldNode;
        }
        int startColumn = getStartColumn(specificFieldNode, specificFieldNode.kind(), true);
        Token fieldName = getToken((Token) specificFieldNode.fieldName());
        Token readOnlyKeyword = specificFieldNode.readonlyKeyword().orElse(null);
        Token colon = getToken(specificFieldNode.colon().orElse(null));
        ExpressionNode expressionNode = this.modifyNode(specificFieldNode.valueExpr().orElse(null));
        if (readOnlyKeyword != null) {
            specificFieldNode = specificFieldNode.modify()
                    .withReadonlyKeyword(formatToken(readOnlyKeyword, 0, 0, 0, 0)).apply();
        }
        return specificFieldNode.modify()
                .withFieldName(formatToken(fieldName, startColumn, 0, 0, 0))
                .withColon(formatToken(colon, 0, 1, 0, 0))
                .withValueExpr(expressionNode)
                .apply();
    }

    @Override
    public BinaryExpressionNode transform(BinaryExpressionNode binaryExpressionNode) {
        if (!isInLineRange(binaryExpressionNode)) {
            return binaryExpressionNode;
        }
        Node lhsExpr = this.modifyNode(binaryExpressionNode.lhsExpr());
        Node rhsExpr = this.modifyNode(binaryExpressionNode.rhsExpr());
        Token operator = getToken(binaryExpressionNode.operator());
        return binaryExpressionNode.modify()
                .withLhsExpr(lhsExpr)
                .withRhsExpr(rhsExpr)
                .withOperator(formatToken(operator, 1, 1, 0, 0))
                .apply();
    }

    @Override
    public ArrayTypeDescriptorNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        if (!isInLineRange(arrayTypeDescriptorNode)) {
            return arrayTypeDescriptorNode;
        }
        Node arrayLength = arrayTypeDescriptorNode.arrayLength().orElse(null);
        Token openBracket = getToken(arrayTypeDescriptorNode.openBracket());
        Token closeBracket = getToken(arrayTypeDescriptorNode.closeBracket());
        TypeDescriptorNode memberTypeDesc = this.modifyNode(arrayTypeDescriptorNode.memberTypeDesc());
        if (arrayLength != null) {
            arrayTypeDescriptorNode = arrayTypeDescriptorNode.modify()
                    .withArrayLength(this.modifyNode(arrayLength)).apply();
        }
        return arrayTypeDescriptorNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .withMemberTypeDesc(memberTypeDesc)
                .apply();
    }

    @Override
    public AssignmentStatementNode transform(AssignmentStatementNode assignmentStatementNode) {
        if (!isInLineRange(assignmentStatementNode)) {
            return assignmentStatementNode;
        }
        Node varRef = this.modifyNode(assignmentStatementNode.varRef());
        ExpressionNode expression = this.modifyNode(assignmentStatementNode.expression());
        Token equalsToken = getToken(assignmentStatementNode.equalsToken());
        Token semicolonToken = getToken(assignmentStatementNode.semicolonToken());
        return assignmentStatementNode.modify()
                .withVarRef(varRef)
                .withExpression(expression)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .apply();
    }

    @Override
    public IndexedExpressionNode transform(IndexedExpressionNode indexedExpressionNode) {
        if (!isInLineRange(indexedExpressionNode)) {
            return indexedExpressionNode;
        }
        SeparatedNodeList<ExpressionNode> keyExpression = this.modifySeparatedNodeList(
                indexedExpressionNode.keyExpression());
        ExpressionNode containerExpression = this.modifyNode(indexedExpressionNode.containerExpression());
        Token openBracket = getToken(indexedExpressionNode.openBracket());
        Token closeBracket = getToken(indexedExpressionNode.closeBracket());
        return indexedExpressionNode.modify()
                .withKeyExpression(keyExpression)
                .withContainerExpression(containerExpression)
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public CheckExpressionNode transform(CheckExpressionNode checkExpressionNode) {
        if (!isInLineRange(checkExpressionNode)) {
            return checkExpressionNode;
        }
        int startColumn = getStartColumn(checkExpressionNode, checkExpressionNode.kind(), false);
        Token checkKeyword = getToken(checkExpressionNode.checkKeyword());
        ExpressionNode expressionNode = this.modifyNode(checkExpressionNode.expression());
        return checkExpressionNode.modify()
                .withCheckKeyword(formatToken(checkKeyword, startColumn, 1, 0, 0))
                .withExpression(expressionNode)
                .apply();
    }

    @Override
    public WhileStatementNode transform(WhileStatementNode whileStatementNode) {
        if (!isInLineRange(whileStatementNode)) {
            return whileStatementNode;
        }
        int startColumn = getStartColumn(whileStatementNode, whileStatementNode.kind(), true);
        Token whileKeyword = getToken(whileStatementNode.whileKeyword());
        ExpressionNode condition = this.modifyNode(whileStatementNode.condition());
        BlockStatementNode whileBody = this.modifyNode(whileStatementNode.whileBody());
        return whileStatementNode.modify()
                .withWhileKeyword(formatToken(whileKeyword, startColumn, 0, 0, 0))
                .withCondition(condition)
                .withWhileBody(whileBody)
                .apply();
    }

    @Override
    public ReturnStatementNode transform(ReturnStatementNode returnStatementNode) {
        if (!isInLineRange(returnStatementNode)) {
            return returnStatementNode;
        }
        int startColumn = getStartColumn(returnStatementNode, returnStatementNode.kind(), true);
        Token returnKeyword = getToken(returnStatementNode.returnKeyword());
        ExpressionNode expressionNode = returnStatementNode.expression().orElse(null);
        Token semicolonToken = getToken(returnStatementNode.semicolonToken());
        if (expressionNode != null) {
            returnStatementNode = returnStatementNode.modify()
                    .withExpression(this.modifyNode(expressionNode)).apply();
        }
        return returnStatementNode.modify()
                .withReturnKeyword(formatToken(returnKeyword, startColumn, 1, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 1))
                .apply();
    }

    @Override
    public MethodCallExpressionNode transform(MethodCallExpressionNode methodCallExpressionNode) {
        if (!isInLineRange(methodCallExpressionNode)) {
            return methodCallExpressionNode;
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = this.modifySeparatedNodeList(methodCallExpressionNode
                .arguments());
        Token openParenToken = getToken(methodCallExpressionNode.openParenToken());
        Token closeParenToken = getToken(methodCallExpressionNode.closeParenToken());
        Token dotToken = getToken(methodCallExpressionNode.dotToken());
        ExpressionNode expression = this.modifyNode(methodCallExpressionNode.expression());
        NameReferenceNode methodName = this.modifyNode(methodCallExpressionNode.methodName());
        return methodCallExpressionNode.modify()
                .withArguments(arguments)
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .withDotToken(formatToken(dotToken, 0, 0, 0, 0))
                .withExpression(expression)
                .withMethodName(methodName)
                .apply();
    }

    @Override
    public NilLiteralNode transform(NilLiteralNode nilLiteralNode) {
        Token openParenToken = getToken(nilLiteralNode.openParenToken());
        Token closeParenToken = getToken(nilLiteralNode.closeParenToken());
        return nilLiteralNode.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public NilTypeDescriptorNode transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken = getToken(nilTypeDescriptorNode.openParenToken());
        Token closeParenToken = getToken(nilTypeDescriptorNode.closeParenToken());
        return nilTypeDescriptorNode.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public UnionTypeDescriptorNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        TypeDescriptorNode leftTypeDesc = this.modifyNode(unionTypeDescriptorNode.leftTypeDesc());
        Token pipeToken = getToken(unionTypeDescriptorNode.pipeToken());
        TypeDescriptorNode rightTypeDesc = this.modifyNode(unionTypeDescriptorNode.rightTypeDesc());
        return unionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withPipeToken(pipeToken)
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public XMLNamespaceDeclarationNode transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = getToken(xMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri = this.modifyNode(xMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword = getToken(xMLNamespaceDeclarationNode.asKeyword().orElse(null));
        IdentifierToken namespacePrefix = this.modifyNode(xMLNamespaceDeclarationNode.namespacePrefix().orElse(null));
        Token semicolonToken = getToken(xMLNamespaceDeclarationNode.semicolonToken());
        return xMLNamespaceDeclarationNode.modify()
                .withNamespacePrefix(namespacePrefix)
                .withNamespaceuri(namespaceuri)
                .withXmlnsKeyword(formatToken(xmlnsKeyword, 0, 0, 0, 0))
                .withAsKeyword(formatToken(asKeyword, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ModuleXMLNamespaceDeclarationNode transform(
            ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        Token xmlnsKeyword = getToken(moduleXMLNamespaceDeclarationNode.xmlnsKeyword());
        ExpressionNode namespaceuri = this.modifyNode(moduleXMLNamespaceDeclarationNode.namespaceuri());
        Token asKeyword = getToken(moduleXMLNamespaceDeclarationNode.asKeyword());
        IdentifierToken namespacePrefix = this.modifyNode(moduleXMLNamespaceDeclarationNode.namespacePrefix());
        Token semicolonToken = getToken(moduleXMLNamespaceDeclarationNode.semicolonToken());
        return moduleXMLNamespaceDeclarationNode.modify()
                .withNamespacePrefix(namespacePrefix)
                .withNamespaceuri(namespaceuri)
                .withXmlnsKeyword(formatToken(xmlnsKeyword, 0, 0, 0, 0))
                .withAsKeyword(formatToken(asKeyword, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XmlTypeDescriptorNode transform(XmlTypeDescriptorNode xmlTypeDescriptorNode) {
        int startColumn = getStartColumn(xmlTypeDescriptorNode, xmlTypeDescriptorNode.kind(), true);
        Token xmlKeywordToken = getToken(xmlTypeDescriptorNode.xmlKeywordToken());
        TypeParameterNode xmlTypeParamsNode = this.modifyNode(xmlTypeDescriptorNode.xmlTypeParamsNode().orElse(null));
        if (xmlTypeParamsNode != null) {
            xmlTypeDescriptorNode = xmlTypeDescriptorNode.modify()
                    .withXmlTypeParamsNode(xmlTypeParamsNode).apply();
        }
        return xmlTypeDescriptorNode.modify()
                .withXmlKeywordToken(formatToken(xmlKeywordToken, startColumn, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLElementNode transform(XMLElementNode xMLElementNode) {
        XMLStartTagNode startTag = this.modifyNode(xMLElementNode.startTag());
        NodeList<XMLItemNode> content = modifyNodeList(xMLElementNode.content());
        XMLEndTagNode endTag = this.modifyNode(xMLElementNode.endTag());
        return xMLElementNode.modify()
                .withStartTag(startTag)
                .withEndTag(endTag)
                .withContent(content)
                .apply();
    }

    @Override
    public XMLStartTagNode transform(XMLStartTagNode xMLStartTagNode) {
        Token ltToken = getToken(xMLStartTagNode.ltToken());
        XMLNameNode name = this.modifyNode(xMLStartTagNode.name());
        NodeList<XMLAttributeNode> attributes = modifyNodeList(xMLStartTagNode.attributes());
        Token getToken = getToken(xMLStartTagNode.getToken());
        return xMLStartTagNode.modify()
                .withName(name)
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withAttributes(attributes)
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLEndTagNode transform(XMLEndTagNode xMLEndTagNode) {
        Token ltToken = getToken(xMLEndTagNode.ltToken());
        Token slashToken = getToken(xMLEndTagNode.slashToken());
        XMLNameNode name = this.modifyNode(xMLEndTagNode.name());
        Token getToken = getToken(xMLEndTagNode.getToken());
        return xMLEndTagNode.modify()
                .withName(name)
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLSimpleNameNode transform(XMLSimpleNameNode xMLSimpleNameNode) {
        Token name = getToken(xMLSimpleNameNode.name());
        return xMLSimpleNameNode.modify()
                .withName(name)
                .apply();
    }

    @Override
    public XMLQualifiedNameNode transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        XMLSimpleNameNode prefix = this.modifyNode(xMLQualifiedNameNode.prefix());
        Token colon = getToken(xMLQualifiedNameNode.colon());
        XMLSimpleNameNode name = this.modifyNode(xMLQualifiedNameNode.name());
        return xMLQualifiedNameNode.modify()
                .withPrefix(prefix)
                .withName(name)
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLEmptyElementNode transform(XMLEmptyElementNode xMLEmptyElementNode) {
        Token ltToken = getToken(xMLEmptyElementNode.ltToken());
        XMLNameNode name = this.modifyNode(xMLEmptyElementNode.name());
        NodeList<XMLAttributeNode> attributes = this.modifyNodeList(xMLEmptyElementNode.attributes());
        Token slashToken = getToken(xMLEmptyElementNode.slashToken());
        Token getToken = getToken(xMLEmptyElementNode.getToken());
        return xMLEmptyElementNode.modify()
                .withName(name)
                .withAttributes(attributes)
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withSlashToken(formatToken(slashToken, 0, 0, 0, 0))
                .withGetToken(formatToken(getToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLTextNode transform(XMLTextNode xMLTextNode) {
        Token content = getToken(xMLTextNode.content());
        return xMLTextNode.modify()
                .withContent(formatToken(content, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAttributeNode transform(XMLAttributeNode xMLAttributeNode) {
        XMLNameNode attributeName = this.modifyNode(xMLAttributeNode.attributeName());
        Token equalToken = getToken(xMLAttributeNode.equalToken());
        XMLAttributeValue value = this.modifyNode(xMLAttributeNode.value());
        return xMLAttributeNode.modify()
                .withValue(value)
                .withAttributeName(attributeName)
                .withEqualToken(formatToken(equalToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAttributeValue transform(XMLAttributeValue xMLAttributeValue) {
        Token startQuote = getToken(xMLAttributeValue.startQuote());
        NodeList<Node> value = this.modifyNodeList(xMLAttributeValue.value());
        Token endQuote = getToken(xMLAttributeValue.endQuote());
        return xMLAttributeValue.modify()
                .withStartQuote(formatToken(startQuote, 0, 0, 0, 0))
                .withValue(value)
                .withEndQuote(formatToken(endQuote, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLComment transform(XMLComment xMLComment) {
        Token commentStart = getToken(xMLComment.commentStart());
        NodeList<Node> content = this.modifyNodeList(xMLComment.content());
        Token commentEnd = getToken(xMLComment.commentEnd());
        return xMLComment.modify()
                .withCommentStart(formatToken(commentStart, 0, 0, 0, 0))
                .withContent(content)
                .withCommentEnd(formatToken(commentEnd, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLProcessingInstruction transform(XMLProcessingInstruction xMLProcessingInstruction) {
        Token piStart = getToken(xMLProcessingInstruction.piStart());
        XMLNameNode target = this.modifyNode(xMLProcessingInstruction.target());
        NodeList<Node> data = this.modifyNodeList(xMLProcessingInstruction.data());
        Token piEnd = getToken(xMLProcessingInstruction.piEnd());
        return xMLProcessingInstruction.modify()
                .withTarget(target)
                .withPiStart(formatToken(piStart, 0, 0, 0, 0))
                .withData(data)
                .withPiEnd(formatToken(piEnd, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLFilterExpressionNode transform(XMLFilterExpressionNode xMLFilterExpressionNode) {
        ExpressionNode expression = this.modifyNode(xMLFilterExpressionNode.expression());
        XMLNamePatternChainingNode xmlPatternChain = this.modifyNode(xMLFilterExpressionNode.xmlPatternChain());
        return xMLFilterExpressionNode.modify()
                .withExpression(expression)
                .withXmlPatternChain(xmlPatternChain)
                .apply();
    }

    @Override
    public XMLStepExpressionNode transform(XMLStepExpressionNode xMLStepExpressionNode) {
        ExpressionNode expression = this.modifyNode(xMLStepExpressionNode.expression());
        Node xmlStepStart = this.modifyNode(xMLStepExpressionNode.xmlStepStart());
        return xMLStepExpressionNode.modify()
                .withExpression(expression)
                .withXmlStepStart(xmlStepStart)
                .apply();
    }

    @Override
    public XMLNamePatternChainingNode transform(XMLNamePatternChainingNode xMLNamePatternChainingNode) {
        Token startToken = getToken(xMLNamePatternChainingNode.startToken());
        SeparatedNodeList<Node> xmlNamePattern = modifySeparatedNodeList(xMLNamePatternChainingNode.xmlNamePattern());
        Token gtToken = getToken(xMLNamePatternChainingNode.gtToken());
        return xMLNamePatternChainingNode.modify()
                .withStartToken(formatToken(startToken, 0, 0, 0, 0))
                .withXmlNamePattern(xmlNamePattern)
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public XMLAtomicNamePatternNode transform(XMLAtomicNamePatternNode xMLAtomicNamePatternNode) {
        Token prefix = getToken(xMLAtomicNamePatternNode.prefix());
        Token colon = getToken(xMLAtomicNamePatternNode.colon());
        Token name = getToken(xMLAtomicNamePatternNode.name());
        return xMLAtomicNamePatternNode.modify()
                .withPrefix(formatToken(prefix, 0, 0, 0, 0))
                .withColon(formatToken(colon, 0, 0, 0, 0))
                .withName(formatToken(name, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TemplateExpressionNode transform(TemplateExpressionNode templateExpressionNode) {
        Token type = getToken(templateExpressionNode.type().orElse(null));
        Token startBacktick = getToken(templateExpressionNode.startBacktick());
        NodeList<TemplateMemberNode> content = modifyNodeList(templateExpressionNode.content());
        Token endBacktick = getToken(templateExpressionNode.endBacktick());
        return templateExpressionNode.modify()
                .withStartBacktick(formatToken(startBacktick, 1, 0, 0, 0))
                .withContent(content)
                .withType(formatToken(type, 0, 0, 0, 0))
                .withEndBacktick(formatToken(endBacktick, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ByteArrayLiteralNode transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        Token type = getToken(byteArrayLiteralNode.type());
        Token startBacktick = getToken(byteArrayLiteralNode.startBacktick());
        Token content = getToken(byteArrayLiteralNode.content().orElse(null));
        Token endBacktick = getToken(byteArrayLiteralNode.endBacktick());
        if (content != null) {
            byteArrayLiteralNode = byteArrayLiteralNode.modify()
                    .withContent(formatToken(content, 0, 0, 0, 0)).apply();
        }
        return byteArrayLiteralNode.modify()
                .withType(formatToken(type, 0, 0, 0, 0))
                .withStartBacktick(formatToken(startBacktick, 0, 0, 0, 0))
                .withEndBacktick(formatToken(endBacktick, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ListConstructorExpressionNode transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Token openBracket = getToken(listConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> expressions = this.modifySeparatedNodeList(listConstructorExpressionNode.expressions());
        Token closeBracket = getToken(listConstructorExpressionNode.closeBracket());
        return listConstructorExpressionNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withExpressions(expressions)
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TypeReferenceNode transform(TypeReferenceNode typeReferenceNode) {
        Token asteriskToken = getToken(typeReferenceNode.asteriskToken());
        Node typeName = this.modifyNode(typeReferenceNode.typeName());
        Token semicolonToken = getToken(typeReferenceNode.semicolonToken());
        return typeReferenceNode.modify()
                .withTypeName(typeName)
                .withAsteriskToken(formatToken(asteriskToken, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TupleTypeDescriptorNode transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        int startCol = getStartColumn(tupleTypeDescriptorNode, tupleTypeDescriptorNode.kind(), true);
        Token openBracketToken = getToken(tupleTypeDescriptorNode.openBracketToken());
        SeparatedNodeList<Node> memberTypeDesc = this.modifySeparatedNodeList(tupleTypeDescriptorNode.memberTypeDesc());
        Token closeBracketToken = getToken(tupleTypeDescriptorNode.closeBracketToken());
        return tupleTypeDescriptorNode.modify()
                .withOpenBracketToken(formatToken(openBracketToken, startCol, 0, 0, 0))
                .withMemberTypeDesc(memberTypeDesc)
                .withCloseBracketToken(formatToken(closeBracketToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public MappingMatchPatternNode transform(MappingMatchPatternNode mappingMatchPatternNode) {
        Token openBraceToken = getToken(mappingMatchPatternNode.openBraceToken());
        SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns =
                this.modifySeparatedNodeList(mappingMatchPatternNode.fieldMatchPatterns());
        RestMatchPatternNode restMatchPattern =
                this.modifyNode(mappingMatchPatternNode.restMatchPattern().orElse(null));
        Token closeBraceToken = getToken(mappingMatchPatternNode.closeBraceToken());
        if (restMatchPattern != null) {
            mappingMatchPatternNode = mappingMatchPatternNode.modify()
                    .withRestMatchPattern(restMatchPattern).apply();
        }
        return mappingMatchPatternNode.modify()
                .withOpenBraceToken(formatToken(openBraceToken, 0, 0, 0, 0))
                .withFieldMatchPatterns(fieldMatchPatterns)
                .withCloseBraceToken(formatToken(closeBraceToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ParameterizedTypeDescriptorNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        int startCol = getStartColumn(parameterizedTypeDescriptorNode, parameterizedTypeDescriptorNode.kind(), true);
        Token parameterizedType = getToken(parameterizedTypeDescriptorNode.parameterizedType());
        TypeParameterNode typeParameter = this.modifyNode(parameterizedTypeDescriptorNode.typeParameter());
        return parameterizedTypeDescriptorNode.modify()
                .withParameterizedType(formatToken(parameterizedType, startCol, 0, 0, 0))
                .withTypeParameter(typeParameter)
                .apply();
    }

    @Override
    public TypeParameterNode transform(TypeParameterNode typeParameterNode) {
        Token ltToken = getToken(typeParameterNode.ltToken());
        TypeDescriptorNode typeNode = this.modifyNode(typeParameterNode.typeNode());
        Token gtToken = getToken(typeParameterNode.gtToken());
        return typeParameterNode.modify()
                .withTypeNode(typeNode)
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public StartActionNode transform(StartActionNode startActionNode) {
        if (!isInLineRange(startActionNode)) {
            return startActionNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(startActionNode.annotations());
        Token startKeyword = getToken(startActionNode.startKeyword());
        ExpressionNode expression = this.modifyNode(startActionNode.expression());
        return startActionNode.modify()
                .withAnnotations(annotations)
                .withStartKeyword(formatToken(startKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public FlushActionNode transform(FlushActionNode flushActionNode) {
        if (!isInLineRange(flushActionNode)) {
            return flushActionNode;
        }
        Token flushKeyword = getToken(flushActionNode.flushKeyword());
        NameReferenceNode peerWorker = this.modifyNode(flushActionNode.peerWorker());
        return flushActionNode.modify()
                .withFlushKeyword(formatToken(flushKeyword, 0, 1, 0, 0))
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public NamedWorkerDeclarationNode transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        if (!isInLineRange(namedWorkerDeclarationNode)) {
            return namedWorkerDeclarationNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(namedWorkerDeclarationNode.annotations());
        Token workerKeyword = getToken(namedWorkerDeclarationNode.workerKeyword());
        IdentifierToken workerName = this.modifyNode(namedWorkerDeclarationNode.workerName());
        Node returnTypeDesc =
                this.modifyNode(namedWorkerDeclarationNode.returnTypeDesc().orElse(null));
        BlockStatementNode workerBody = this.modifyNode(namedWorkerDeclarationNode.workerBody());
        if (returnTypeDesc != null) {
            namedWorkerDeclarationNode = namedWorkerDeclarationNode.modify()
                    .withReturnTypeDesc(returnTypeDesc).apply();
        }
        return namedWorkerDeclarationNode.modify()
                .withAnnotations(annotations)
                .withWorkerKeyword(formatToken(workerKeyword, 0, 0, 0, 0))
                .withWorkerName(workerName)
                .withWorkerBody(workerBody)
                .apply();
    }

    @Override
    public TypeDefinitionNode transform(TypeDefinitionNode typeDefinitionNode) {
        if (!isInLineRange(typeDefinitionNode)) {
            return typeDefinitionNode;
        }
        MetadataNode metadata = this.modifyNode(typeDefinitionNode.metadata().orElse(null));
        Token visibilityQualifier = getToken(typeDefinitionNode.visibilityQualifier().orElse(null));
        Token typeKeyword = getToken(typeDefinitionNode.typeKeyword());
        Token typeName = getToken(typeDefinitionNode.typeName());
        Node typeDescriptor = this.modifyNode(typeDefinitionNode.typeDescriptor());
        Token semicolonToken = this.modifyToken(typeDefinitionNode.semicolonToken());
        if (metadata != null) {
            typeDefinitionNode = typeDefinitionNode.modify()
                    .withMetadata(metadata).apply();
        }
        if (visibilityQualifier != null) {
            typeDefinitionNode = typeDefinitionNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 1, 1, 0, 0)).apply();
        }
        return typeDefinitionNode.modify()
                .withTypeKeyword(formatToken(typeKeyword, 1, 1, 0, 0))
                .withTypeName(formatToken(typeName, 1, 1, 0, 0))
                .withTypeDescriptor(typeDescriptor)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public CompoundAssignmentStatementNode transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        if (!isInLineRange(compoundAssignmentStatementNode)) {
            return compoundAssignmentStatementNode;
        }
        ExpressionNode lhsExpression = this.modifyNode(compoundAssignmentStatementNode.lhsExpression());
        Token binaryOperator = getToken(compoundAssignmentStatementNode.binaryOperator());
        Token equalsToken = getToken(compoundAssignmentStatementNode.equalsToken());
        ExpressionNode rhsExpression = this.modifyNode(compoundAssignmentStatementNode.rhsExpression());
        Token semicolonToken = getToken(compoundAssignmentStatementNode.semicolonToken());
        return compoundAssignmentStatementNode.modify()
                .withLhsExpression(lhsExpression)
                .withBinaryOperator(formatToken(binaryOperator, 1, 1, 0, 0))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withRhsExpression(rhsExpression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public BreakStatementNode transform(BreakStatementNode breakStatementNode) {
        if (!isInLineRange(breakStatementNode)) {
            return breakStatementNode;
        }
        Token breakToken = getToken(breakStatementNode.breakToken());
        Token semicolonToken = getToken(breakStatementNode.semicolonToken());
        return breakStatementNode.modify()
                .withBreakToken(formatToken(breakToken, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ContinueStatementNode transform(ContinueStatementNode continueStatementNode) {
        if (!isInLineRange(continueStatementNode)) {
            return continueStatementNode;
        }
        Token continueToken = getToken(continueStatementNode.continueToken());
        Token semicolonToken = getToken(continueStatementNode.semicolonToken());
        return continueStatementNode.modify()
                .withContinueToken(formatToken(continueToken, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ExternalFunctionBodyNode transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        if (!isInLineRange(externalFunctionBodyNode)) {
            return externalFunctionBodyNode;
        }
        Token equalsToken = getToken(externalFunctionBodyNode.equalsToken());
        NodeList<AnnotationNode> annotations = this.modifyNodeList(externalFunctionBodyNode.annotations());
        Token externalKeyword = getToken(externalFunctionBodyNode.externalKeyword());
        Token semicolonToken = getToken(externalFunctionBodyNode.semicolonToken());
        return externalFunctionBodyNode.modify()
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withAnnotations(annotations)
                .withExternalKeyword(formatToken(externalKeyword, 1, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 1, 0, 0, 0))
                .apply();
    }

    @Override
    public PanicStatementNode transform(PanicStatementNode panicStatementNode) {
        if (!isInLineRange(panicStatementNode)) {
            return panicStatementNode;
        }
        Token panicKeyword = getToken(panicStatementNode.panicKeyword());
        ExpressionNode expression = this.modifyNode(panicStatementNode.expression());
        Token semicolonToken = getToken(panicStatementNode.semicolonToken());
        return panicStatementNode.modify()
                .withPanicKeyword(formatToken(panicKeyword, 1, 1, 0, 0))
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public LocalTypeDefinitionStatementNode transform(
            LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        if (!isInLineRange(localTypeDefinitionStatementNode)) {
            return localTypeDefinitionStatementNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(localTypeDefinitionStatementNode.annotations());
        Token typeKeyword = getToken(localTypeDefinitionStatementNode.typeKeyword());
        Node typeName = this.modifyNode(localTypeDefinitionStatementNode.typeName());
        Node typeDescriptor = this.modifyNode(localTypeDefinitionStatementNode.typeDescriptor());
        Token semicolonToken = getToken(localTypeDefinitionStatementNode.semicolonToken());
        return localTypeDefinitionStatementNode.modify()
                .withAnnotations(annotations)
                .withTypeKeyword(formatToken(typeKeyword, 0, 1, 0, 0))
                .withTypeName(typeName)
                .withTypeDescriptor(typeDescriptor)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public LockStatementNode transform(LockStatementNode lockStatementNode) {
        if (!isInLineRange(lockStatementNode)) {
            return lockStatementNode;
        }
        Token lockKeyword = getToken(lockStatementNode.lockKeyword());
        StatementNode blockStatement = this.modifyNode(lockStatementNode.blockStatement());
        return lockStatementNode.modify()
                .withLockKeyword(formatToken(lockKeyword, 0, 1, 0, 0))
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public ForkStatementNode transform(ForkStatementNode forkStatementNode) {
        if (!isInLineRange(forkStatementNode)) {
            return forkStatementNode;
        }
        Token forkKeyword = getToken(forkStatementNode.forkKeyword());
        Token openBraceToken = getToken(forkStatementNode.openBraceToken());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                this.modifyNodeList(forkStatementNode.namedWorkerDeclarations());
        Token closeBraceToken = getToken(forkStatementNode.closeBraceToken());
        return forkStatementNode.modify()
                .withForkKeyword(formatToken(forkKeyword, 1, 1, 0, 0))
                .withOpenBraceToken(formatToken(openBraceToken, 0, 0, 0, 0))
                .withNamedWorkerDeclarations(namedWorkerDeclarations)
                .withCloseBraceToken(formatToken(closeBraceToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ForEachStatementNode transform(ForEachStatementNode forEachStatementNode) {
        if (!isInLineRange(forEachStatementNode)) {
            return forEachStatementNode;
        }
        Token forEachKeyword = getToken(forEachStatementNode.forEachKeyword());
        TypedBindingPatternNode typedBindingPattern = this.modifyNode(forEachStatementNode.typedBindingPattern());
        Token inKeyword = getToken(forEachStatementNode.inKeyword());
        Node actionOrExpressionNode = this.modifyNode(forEachStatementNode.actionOrExpressionNode());
        StatementNode blockStatement = this.modifyNode(forEachStatementNode.blockStatement());
        return forEachStatementNode.modify()
                .withForEachKeyword(formatToken(forEachKeyword, 0, 1, 0, 0))
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(formatToken(inKeyword, 1, 1, 0,  0))
                .withActionOrExpressionNode(actionOrExpressionNode)
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public FailExpressionNode transform(FailExpressionNode failExpressionNode) {
        if (!isInLineRange(failExpressionNode)) {
            return failExpressionNode;
        }
        Token failKeyword = getToken(failExpressionNode.failKeyword());
        ExpressionNode expression = this.modifyNode(failExpressionNode.expression());
        return failExpressionNode.modify()
                .withFailKeyword(formatToken(failKeyword, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public FieldAccessExpressionNode transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        if (!isInLineRange(fieldAccessExpressionNode)) {
            return fieldAccessExpressionNode;
        }
        ExpressionNode expression = this.modifyNode(fieldAccessExpressionNode.expression());
        Token dotToken = getToken(fieldAccessExpressionNode.dotToken());
        NameReferenceNode fieldName = this.modifyNode(fieldAccessExpressionNode.fieldName());
        return fieldAccessExpressionNode.modify()
                .withExpression(expression)
                .withDotToken(formatToken(dotToken, 0, 0, 0, 0))
                .withFieldName(fieldName)
                .apply();
    }

    @Override
    public TypeofExpressionNode transform(TypeofExpressionNode typeofExpressionNode) {
        if (!isInLineRange(typeofExpressionNode)) {
            return typeofExpressionNode;
        }
        Token typeofKeyword = getToken(typeofExpressionNode.typeofKeyword());
        ExpressionNode expression = this.modifyNode(typeofExpressionNode.expression());
        return typeofExpressionNode.modify()
                .withTypeofKeyword(formatToken(typeofKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public UnaryExpressionNode transform(UnaryExpressionNode unaryExpressionNode) {
        if (!isInLineRange(unaryExpressionNode)) {
            return unaryExpressionNode;
        }
        Token unaryOperator = getToken(unaryExpressionNode.unaryOperator());
        ExpressionNode expression = this.modifyNode(unaryExpressionNode.expression());
        return unaryExpressionNode.modify()
                .withUnaryOperator(formatToken(unaryOperator, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public ComputedNameFieldNode transform(ComputedNameFieldNode computedNameFieldNode) {
        if (!isInLineRange(computedNameFieldNode)) {
            return computedNameFieldNode;
        }
        Token openBracket = getToken(computedNameFieldNode.openBracket());
        ExpressionNode fieldNameExpr = this.modifyNode(computedNameFieldNode.fieldNameExpr());
        Token closeBracket = getToken(computedNameFieldNode.closeBracket());
        Token colonToken = getToken(computedNameFieldNode.colonToken());
        ExpressionNode valueExpr = this.modifyNode(computedNameFieldNode.valueExpr());
        return computedNameFieldNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withFieldNameExpr(fieldNameExpr)
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .withColonToken(formatToken(colonToken, 1, 1, 0, 0))
                .withValueExpr(valueExpr)
                .apply();
    }

    @Override
    public DefaultableParameterNode transform(DefaultableParameterNode defaultableParameterNode) {
        if (!isInLineRange(defaultableParameterNode)) {
            return defaultableParameterNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(defaultableParameterNode.annotations());
        Token visibilityQualifier = getToken(defaultableParameterNode.visibilityQualifier().orElse(null));
        Node typeName = this.modifyNode(defaultableParameterNode.typeName());
        Token paramName = getToken(defaultableParameterNode.paramName().orElse(null));
        Token equalsToken = getToken(defaultableParameterNode.equalsToken());
        Node expression = this.modifyNode(defaultableParameterNode.expression());
        if (visibilityQualifier != null) {
            defaultableParameterNode = defaultableParameterNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 1, 0, 0)).apply();
        }
        if (paramName != null) {
            defaultableParameterNode = defaultableParameterNode.modify()
                    .withParamName(formatToken(paramName, 1, 1, 0, 0)).apply();
        }
        return defaultableParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public RestParameterNode transform(RestParameterNode restParameterNode) {
        if (!isInLineRange(restParameterNode)) {
            return restParameterNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(restParameterNode.annotations());
        Node typeName = this.modifyNode(restParameterNode.typeName());
        Token ellipsisToken = getToken(restParameterNode.ellipsisToken());
        Token paramName = getToken(restParameterNode.paramName().orElse(null));
        if (paramName != null) {
            restParameterNode = restParameterNode.modify()
                    .withParamName(formatToken(paramName, 1, 1, 0, 0)).apply();
        }
        return restParameterNode.modify()
                .withAnnotations(annotations)
                .withTypeName(typeName)
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public SpreadFieldNode transform(SpreadFieldNode spreadFieldNode) {
        if (!isInLineRange(spreadFieldNode)) {
            return spreadFieldNode;
        }
        Token ellipsis = getToken(spreadFieldNode.ellipsis());
        ExpressionNode valueExpr = this.modifyNode(spreadFieldNode.valueExpr());
        return spreadFieldNode.modify()
                .withEllipsis(formatToken(ellipsis, 0, 0, 0, 0))
                .withValueExpr(valueExpr)
                .apply();
    }

    @Override
    public NamedArgumentNode transform(NamedArgumentNode namedArgumentNode) {
        if (!isInLineRange(namedArgumentNode)) {
            return namedArgumentNode;
        }
        SimpleNameReferenceNode argumentName = this.modifyNode(namedArgumentNode.argumentName());
        Token equalsToken = getToken(namedArgumentNode.equalsToken());
        ExpressionNode expression = this.modifyNode(namedArgumentNode.expression());
        return namedArgumentNode.modify()
                .withArgumentName(argumentName)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public RestArgumentNode transform(RestArgumentNode restArgumentNode) {
        if (!isInLineRange(restArgumentNode)) {
            return restArgumentNode;
        }
        Token ellipsis = getToken(restArgumentNode.ellipsis());
        ExpressionNode expression = this.modifyNode(restArgumentNode.expression());
        return restArgumentNode.modify()
                .withEllipsis(formatToken(ellipsis, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public ObjectTypeDescriptorNode transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        if (!isInLineRange(objectTypeDescriptorNode)) {
            return objectTypeDescriptorNode;
        }
        NodeList<Token> objectTypeQualifiers = this.modifyNodeList(objectTypeDescriptorNode.objectTypeQualifiers());
        Token objectKeyword = getToken(objectTypeDescriptorNode.objectKeyword());
        Token openBrace = getToken(objectTypeDescriptorNode.openBrace());
        NodeList<Node> members = this.modifyNodeList(objectTypeDescriptorNode.members());
        Token closeBrace = getToken(objectTypeDescriptorNode.closeBrace());
        return objectTypeDescriptorNode.modify()
                .withObjectTypeQualifiers(objectTypeQualifiers)
                .withObjectKeyword(formatToken(objectKeyword, 0, 1, 1, 0))
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 0))
                .withMembers(members)
                .withCloseBrace(formatToken(closeBrace, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RecordTypeDescriptorNode transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        if (!isInLineRange(recordTypeDescriptorNode)) {
            return recordTypeDescriptorNode;
        }
        Token recordKeyword = getToken(recordTypeDescriptorNode.recordKeyword());
        Token bodyStartDelimiter = getToken(recordTypeDescriptorNode.bodyStartDelimiter());
        NodeList<Node> fields = this.modifyNodeList(recordTypeDescriptorNode.fields());
        RecordRestDescriptorNode recordRestDescriptor =
                modifyNode(recordTypeDescriptorNode.recordRestDescriptor().orElse(null));
        Token bodyEndDelimiter = getToken(recordTypeDescriptorNode.bodyEndDelimiter());
        if (recordRestDescriptor != null) {
            recordTypeDescriptorNode = recordTypeDescriptorNode.modify()
                    .withRecordRestDescriptor(recordRestDescriptor).apply();
        }
        return recordTypeDescriptorNode.modify()
                .withRecordKeyword(formatToken(recordKeyword, 0, 1, 0, 0))
                .withBodyStartDelimiter(formatToken(bodyStartDelimiter, 0, 0, 0, 0))
                .withFields(fields)
                .withBodyEndDelimiter(formatToken(bodyEndDelimiter, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ObjectFieldNode transform(ObjectFieldNode objectFieldNode) {
        if (!isInLineRange(objectFieldNode)) {
            return objectFieldNode;
        }
        MetadataNode metadata = this.modifyNode(objectFieldNode.metadata().orElse(null));
        Token visibilityQualifier = getToken(objectFieldNode.visibilityQualifier().orElse(null));
        Token readonlyKeyword = getToken(objectFieldNode.readonlyKeyword().orElse(null));
        Node typeName = this.modifyNode(objectFieldNode.typeName());
        Token fieldName = getToken(objectFieldNode.fieldName());
        Token equalsToken = getToken(objectFieldNode.equalsToken().orElse(null));
        ExpressionNode expression = this.modifyNode(objectFieldNode.expression().orElse(null));
        Token semicolonToken = getToken(objectFieldNode.semicolonToken());
        if (metadata != null) {
            objectFieldNode = objectFieldNode.modify()
                    .withMetadata(metadata).apply();
        }
        if (visibilityQualifier != null) {
            objectFieldNode = objectFieldNode.modify()
                    .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 1, 0, 0)).apply();
        }
        if (readonlyKeyword != null) {
            objectFieldNode = objectFieldNode.modify()
                    .withReadonlyKeyword(formatToken(readonlyKeyword, 0, 1, 0, 0)).apply();
        }
        return objectFieldNode.modify()
                .withTypeName(typeName)
                .withFieldName(formatToken(fieldName, 1, 1, 0, 0))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RecordFieldNode transform(RecordFieldNode recordFieldNode) {
        if (!isInLineRange(recordFieldNode)) {
            return recordFieldNode;
        }
        MetadataNode metadata = this.modifyNode(recordFieldNode.metadata().orElse(null));
        Token readonlyKeyword = getToken(recordFieldNode.readonlyKeyword().orElse(null));
        Node typeName = this.modifyNode(recordFieldNode.typeName());
        Token fieldName = getToken(recordFieldNode.fieldName());
        Token questionMarkToken = getToken(recordFieldNode.questionMarkToken().orElse(null));
        Token semicolonToken = getToken(recordFieldNode.semicolonToken());
        if (metadata != null) {
            recordFieldNode = recordFieldNode.modify()
                    .withMetadata(metadata).apply();
        }
        if (readonlyKeyword != null) {
            recordFieldNode = recordFieldNode.modify()
                    .withReadonlyKeyword(formatToken(readonlyKeyword, 0, 1, 0, 0)).apply();
        }
        if (questionMarkToken != null) {
            recordFieldNode = recordFieldNode.modify()
                    .withQuestionMarkToken(formatToken(questionMarkToken, 1, 1, 0, 0)).apply();
        }
        return recordFieldNode.modify()
                .withTypeName(typeName)
                .withFieldName(formatToken(fieldName, 0, 1, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RecordFieldWithDefaultValueNode transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        if (!isInLineRange(recordFieldWithDefaultValueNode)) {
            return recordFieldWithDefaultValueNode;
        }
        MetadataNode metadata = this.modifyNode(recordFieldWithDefaultValueNode.metadata().orElse(null));
        Token readonlyKeyword = getToken(recordFieldWithDefaultValueNode.readonlyKeyword().orElse(null));
        Node typeName = this.modifyNode(recordFieldWithDefaultValueNode.typeName());
        Token fieldName = getToken(recordFieldWithDefaultValueNode.fieldName());
        Token equalsToken = getToken(recordFieldWithDefaultValueNode.equalsToken());
        ExpressionNode expression = this.modifyNode(recordFieldWithDefaultValueNode.expression());
        Token semicolonToken = getToken(recordFieldWithDefaultValueNode.semicolonToken());
        if (metadata != null) {
            recordFieldWithDefaultValueNode = recordFieldWithDefaultValueNode.modify()
                    .withMetadata(metadata).apply();
        }
        if (readonlyKeyword != null) {
            recordFieldWithDefaultValueNode = recordFieldWithDefaultValueNode.modify()
                    .withReadonlyKeyword(formatToken(readonlyKeyword, 0, 1, 0, 0)).apply();
        }
        return recordFieldWithDefaultValueNode.modify()
                .withTypeName(typeName)
                .withFieldName(formatToken(fieldName, 1, 1, 0, 0))
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RecordRestDescriptorNode transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        if (!isInLineRange(recordRestDescriptorNode)) {
            return recordRestDescriptorNode;
        }
        Node typeName = this.modifyNode(recordRestDescriptorNode.typeName());
        Token ellipsisToken = getToken(recordRestDescriptorNode.ellipsisToken());
        Token semicolonToken = getToken(recordRestDescriptorNode.semicolonToken());
        return recordRestDescriptorNode.modify()
                .withTypeName(typeName)
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public AnnotationNode transform(AnnotationNode annotationNode) {
        if (!isInLineRange(annotationNode)) {
            return annotationNode;
        }
        Token atToken = getToken(annotationNode.atToken());
        Node annotReference = this.modifyNode(annotationNode.annotReference());
        MappingConstructorExpressionNode annotValue = this.modifyNode(annotationNode.annotValue().orElse(null));
        if (annotValue != null) {
            annotationNode = annotationNode.modify()
                    .withAnnotValue(annotValue).apply();
        }
        return annotationNode.modify()
                .withAtToken(formatToken(atToken, 1, 1, 0, 0))
                .withAnnotReference(annotReference)
                .apply();
    }

    @Override
    public AnnotationDeclarationNode transform(AnnotationDeclarationNode annotationDeclarationNode) {
        if (!isInLineRange(annotationDeclarationNode)) {
            return annotationDeclarationNode;
        }
        MetadataNode metadata = this.modifyNode(annotationDeclarationNode.metadata().orElse(null));
        Token visibilityQualifier = getToken(annotationDeclarationNode.visibilityQualifier());
        Token constKeyword = getToken(annotationDeclarationNode.constKeyword());
        Token annotationKeyword = getToken(annotationDeclarationNode.annotationKeyword());
        Node typeDescriptor = this.modifyNode(annotationDeclarationNode.typeDescriptor());
        Token annotationTag = getToken(annotationDeclarationNode.annotationTag());
        Token onKeyword = getToken(annotationDeclarationNode.onKeyword());
        SeparatedNodeList<Node> attachPoints = this.modifySeparatedNodeList(annotationDeclarationNode.attachPoints());
        Token semicolonToken = getToken(annotationDeclarationNode.semicolonToken());
        if (metadata != null) {
            annotationDeclarationNode = annotationDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return annotationDeclarationNode.modify()
                .withVisibilityQualifier(formatToken(visibilityQualifier, 0, 1, 0, 0))
                .withConstKeyword(formatToken(constKeyword, 1, 1, 0, 0))
                .withAnnotationKeyword(formatToken(annotationKeyword, 0, 0, 0, 0))
                .withTypeDescriptor(typeDescriptor)
                .withAnnotationTag(formatToken(annotationTag, 0, 0, 0, 0))
                .withOnKeyword(formatToken(onKeyword, 1, 1, 0, 0))
                .withAttachPoints(attachPoints)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public AnnotationAttachPointNode transform(AnnotationAttachPointNode annotationAttachPointNode) {
        if (!isInLineRange(annotationAttachPointNode)) {
            return annotationAttachPointNode;
        }
        Token sourceKeyword = getToken(annotationAttachPointNode.sourceKeyword());
        Token firstIdent = getToken(annotationAttachPointNode.firstIdent());
        Token secondIdent = getToken(annotationAttachPointNode.secondIdent());
        return annotationAttachPointNode.modify()
                .withSourceKeyword(formatToken(sourceKeyword, 0, 1, 0, 0))
                .withFirstIdent(formatToken(firstIdent, 0, 0, 0, 0))
                .withSecondIdent(formatToken(secondIdent, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public NamedWorkerDeclarator transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        if (!isInLineRange(namedWorkerDeclarator)) {
            return namedWorkerDeclarator;
        }
        NodeList<StatementNode> workerInitStatements =
                this.modifyNodeList(namedWorkerDeclarator.workerInitStatements());
        NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations =
                this.modifyNodeList(namedWorkerDeclarator.namedWorkerDeclarations());
        return namedWorkerDeclarator.modify()
                .withNamedWorkerDeclarations(namedWorkerDeclarations)
                .withWorkerInitStatements(workerInitStatements)
                .apply();
    }

    @Override
    public TrapExpressionNode transform(TrapExpressionNode trapExpressionNode) {
        if (!isInLineRange(trapExpressionNode)) {
            return trapExpressionNode;
        }
        Token trapKeyword = getToken(trapExpressionNode.trapKeyword());
        ExpressionNode expression = this.modifyNode(trapExpressionNode.expression());
        return trapExpressionNode.modify()
                .withTrapKeyword(formatToken(trapKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public TypeCastExpressionNode transform(TypeCastExpressionNode typeCastExpressionNode) {
        if (!isInLineRange(typeCastExpressionNode)) {
            return typeCastExpressionNode;
        }
        Token ltToken = getToken(typeCastExpressionNode.ltToken());
        TypeCastParamNode typeCastParam = this.modifyNode(typeCastExpressionNode.typeCastParam());
        Token gtToken = getToken(typeCastExpressionNode.gtToken());
        ExpressionNode expression = this.modifyNode(typeCastExpressionNode.expression());
        return typeCastExpressionNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withTypeCastParam(typeCastParam)
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public TypeCastParamNode transform(TypeCastParamNode typeCastParamNode) {
        if (!isInLineRange(typeCastParamNode)) {
            return typeCastParamNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(typeCastParamNode.annotations());
        Node type = this.modifyNode(typeCastParamNode.type());
        return typeCastParamNode.modify()
                .withAnnotations(annotations)
                .withType(type)
                .apply();
    }

    @Override
    public TableConstructorExpressionNode transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        if (!isInLineRange(tableConstructorExpressionNode)) {
            return tableConstructorExpressionNode;
        }
        Token tableKeyword = getToken(tableConstructorExpressionNode.tableKeyword());
        KeySpecifierNode keySpecifier = this.modifyNode(tableConstructorExpressionNode.keySpecifier().orElse(null));
        Token openBracket = getToken(tableConstructorExpressionNode.openBracket());
        SeparatedNodeList<Node> mappingConstructors =
                this.modifySeparatedNodeList(tableConstructorExpressionNode.mappingConstructors());
        Token closeBracket = this.modifyToken(tableConstructorExpressionNode.closeBracket());
        return tableConstructorExpressionNode.modify()
                .withTableKeyword(formatToken(tableKeyword, 0, 1, 0, 0))
                .withKeySpecifier(keySpecifier)
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withMappingConstructors(mappingConstructors)
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public KeySpecifierNode transform(KeySpecifierNode keySpecifierNode) {
        if (!isInLineRange(keySpecifierNode)) {
            return keySpecifierNode;
        }
        Token keyKeyword = getToken(keySpecifierNode.keyKeyword());
        Token openParenToken = getToken(keySpecifierNode.openParenToken());
        SeparatedNodeList<IdentifierToken> fieldNames = this.modifySeparatedNodeList(keySpecifierNode.fieldNames());
        Token closeParenToken = getToken(keySpecifierNode.closeParenToken());
        return keySpecifierNode.modify()
                .withKeyKeyword(formatToken(keyKeyword, 0, 1, 0, 0))
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withFieldNames(fieldNames)
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ErrorTypeParamsNode transform(ErrorTypeParamsNode errorTypeParamsNode) {
        if (!isInLineRange(errorTypeParamsNode)) {
            return errorTypeParamsNode;
        }
        Token ltToken = getToken(errorTypeParamsNode.ltToken());
        Node parameter = this.modifyNode(errorTypeParamsNode.parameter());
        Token gtToken = getToken(errorTypeParamsNode.gtToken());
        return errorTypeParamsNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withParameter(parameter)
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public StreamTypeDescriptorNode transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        if (!isInLineRange(streamTypeDescriptorNode)) {
            return streamTypeDescriptorNode;
        }
        Token streamKeywordToken = getToken(streamTypeDescriptorNode.streamKeywordToken());
        Node streamTypeParamsNode = this.modifyNode(streamTypeDescriptorNode.streamTypeParamsNode().orElse(null));
        if (streamTypeParamsNode != null) {
            streamTypeDescriptorNode = streamTypeDescriptorNode.modify()
                    .withStreamTypeParamsNode(streamTypeParamsNode).apply();
        }
        return streamTypeDescriptorNode.modify()
                .withStreamKeywordToken(formatToken(streamKeywordToken, 0, 1, 0, 0))
                .apply();
    }

    @Override
    public StreamTypeParamsNode transform(StreamTypeParamsNode streamTypeParamsNode) {
        if (!isInLineRange(streamTypeParamsNode)) {
            return streamTypeParamsNode;
        }
        Token ltToken = getToken(streamTypeParamsNode.ltToken());
        Node leftTypeDescNode = this.modifyNode(streamTypeParamsNode.leftTypeDescNode());
        Token commaToken = getToken(streamTypeParamsNode.commaToken().orElse(null));
        Node rightTypeDescNode = this.modifyNode(streamTypeParamsNode.rightTypeDescNode().orElse(null));
        Token gtToken = getToken(streamTypeParamsNode.gtToken());
        if (commaToken != null) {
            streamTypeParamsNode = streamTypeParamsNode.modify()
                    .withCommaToken(formatToken(commaToken, 0, 1, 0, 0)).apply();
        }
        if (rightTypeDescNode != null) {
            streamTypeParamsNode = streamTypeParamsNode.modify()
                    .withRightTypeDescNode(rightTypeDescNode).apply();
        }
        return streamTypeParamsNode.modify()
                .withLtToken(formatToken(ltToken, 0, 0, 0, 0))
                .withLeftTypeDescNode(leftTypeDescNode)
                .withGtToken(formatToken(gtToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TypedescTypeDescriptorNode transform(TypedescTypeDescriptorNode typedescTypeDescriptorNode) {
        if (!isInLineRange(typedescTypeDescriptorNode)) {
            return typedescTypeDescriptorNode;
        }
        Token typedescKeywordToken = this.modifyToken(typedescTypeDescriptorNode.typedescKeywordToken());
        TypeParameterNode typedescTypeParamsNode =
                this.modifyNode(typedescTypeDescriptorNode.typedescTypeParamsNode().orElse(null));
        if (typedescTypeParamsNode != null) {
            typedescTypeDescriptorNode = typedescTypeDescriptorNode.modify()
                    .withTypedescTypeParamsNode(typedescTypeParamsNode).apply();
        }
        return typedescTypeDescriptorNode.modify()
                .withTypedescKeywordToken(formatToken(typedescKeywordToken, 0, 1, 0, 0))
                .apply();
    }

    @Override
    public LetExpressionNode transform(LetExpressionNode letExpressionNode) {
        if (!isInLineRange(letExpressionNode)) {
            return letExpressionNode;
        }
        Token letKeyword = getToken(letExpressionNode.letKeyword());
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                this.modifySeparatedNodeList(letExpressionNode.letVarDeclarations());
        Token inKeyword = getToken(letExpressionNode.inKeyword());
        ExpressionNode expression = this.modifyNode(letExpressionNode.expression());
        return letExpressionNode.modify()
                .withLetKeyword(formatToken(letKeyword, 0, 1, 0, 0))
                .withLetVarDeclarations(letVarDeclarations)
                .withInKeyword(formatToken(inKeyword, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public LetVariableDeclarationNode transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        if (!isInLineRange(letVariableDeclarationNode)) {
            return letVariableDeclarationNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(letVariableDeclarationNode.annotations());
        TypedBindingPatternNode typedBindingPattern = this.modifyNode(letVariableDeclarationNode.typedBindingPattern());
        Token equalsToken = getToken(letVariableDeclarationNode.equalsToken());
        ExpressionNode expression = this.modifyNode(letVariableDeclarationNode.expression());
        return letVariableDeclarationNode.modify()
                .withAnnotations(annotations)
                .withTypedBindingPattern(typedBindingPattern)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public InterpolationNode transform(InterpolationNode interpolationNode) {
        if (!isInLineRange(interpolationNode)) {
            return interpolationNode;
        }
        Token interpolationStartToken = getToken(interpolationNode.interpolationStartToken());
        ExpressionNode expression = this.modifyNode(interpolationNode.expression());
        Token interpolationEndToken = getToken(interpolationNode.interpolationEndToken());
        return interpolationNode.modify()
                .withInterpolationStartToken(formatToken(interpolationStartToken, 0, 0, 0, 0))
                .withExpression(expression)
                .withInterpolationEndToken(formatToken(interpolationEndToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TableTypeDescriptorNode transform(TableTypeDescriptorNode tableTypeDescriptorNode) {
        if (!isInLineRange(tableTypeDescriptorNode)) {
            return tableTypeDescriptorNode;
        }
        Token tableKeywordToken = getToken(tableTypeDescriptorNode.tableKeywordToken());
        Node rowTypeParameterNode = this.modifyNode(tableTypeDescriptorNode.rowTypeParameterNode());
        Node keyConstraintNode = this.modifyNode(tableTypeDescriptorNode.keyConstraintNode());
        return tableTypeDescriptorNode.modify()
                .withTableKeywordToken(formatToken(tableKeywordToken, 0, 1, 0, 0))
                .withRowTypeParameterNode(rowTypeParameterNode)
                .withKeyConstraintNode(keyConstraintNode)
                .apply();
    }

    @Override
    public KeyTypeConstraintNode transform(KeyTypeConstraintNode keyTypeConstraintNode) {
        if (!isInLineRange(keyTypeConstraintNode)) {
            return keyTypeConstraintNode;
        }
        Token keyKeywordToken = getToken(keyTypeConstraintNode.keyKeywordToken());
        Node typeParameterNode = this.modifyNode(keyTypeConstraintNode.typeParameterNode());
        return keyTypeConstraintNode.modify()
                .withKeyKeywordToken(formatToken(keyKeywordToken, 0, 1, 0, 0))
                .withTypeParameterNode(typeParameterNode)
                .apply();
    }

    @Override
    public FunctionTypeDescriptorNode transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        if (!isInLineRange(functionTypeDescriptorNode)) {
            return functionTypeDescriptorNode;
        }
        Token functionKeyword = getToken(functionTypeDescriptorNode.functionKeyword());
        FunctionSignatureNode functionSignature = this.modifyNode(functionTypeDescriptorNode.functionSignature());
        return functionTypeDescriptorNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 1, 0, 0))
                .withFunctionSignature(functionSignature)
                .apply();
    }

    @Override
    public ExplicitAnonymousFunctionExpressionNode transform(
            ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        if (!isInLineRange(explicitAnonymousFunctionExpressionNode)) {
            return explicitAnonymousFunctionExpressionNode;
        }
        NodeList<AnnotationNode> annotations =
                this.modifyNodeList(explicitAnonymousFunctionExpressionNode.annotations());
        Token functionKeyword = getToken(explicitAnonymousFunctionExpressionNode.functionKeyword());
        FunctionSignatureNode functionSignature =
                this.modifyNode(explicitAnonymousFunctionExpressionNode.functionSignature());
        FunctionBodyNode functionBody = this.modifyNode(explicitAnonymousFunctionExpressionNode.functionBody());
        return explicitAnonymousFunctionExpressionNode.modify()
                .withAnnotations(annotations)
                .withFunctionKeyword(formatToken(functionKeyword, 0, 1, 0, 0))
                .withFunctionSignature(functionSignature)
                .withFunctionBody(functionBody)
                .apply();
    }

    @Override
    public ExpressionFunctionBodyNode transform(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        if (!isInLineRange(expressionFunctionBodyNode)) {
            return expressionFunctionBodyNode;
        }
        Token rightDoubleArrow = getToken(expressionFunctionBodyNode.rightDoubleArrow());
        ExpressionNode expression = this.modifyNode(expressionFunctionBodyNode.expression());
        Token semicolon = this.modifyToken(expressionFunctionBodyNode.semicolon().orElse(null));
        if (semicolon != null) {
            expressionFunctionBodyNode = expressionFunctionBodyNode.modify()
                    .withSemicolon(formatToken(semicolon, 0, 0, 0, 0)).apply();
        }
        return expressionFunctionBodyNode.modify()
                .withRightDoubleArrow(formatToken(rightDoubleArrow, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public ParenthesisedTypeDescriptorNode transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        if (!isInLineRange(parenthesisedTypeDescriptorNode)) {
            return parenthesisedTypeDescriptorNode;
        }
        Token openParenToken = getToken(parenthesisedTypeDescriptorNode.openParenToken());
        TypeDescriptorNode typedesc = this.modifyNode(parenthesisedTypeDescriptorNode.typedesc());
        Token closeParenToken = getToken(parenthesisedTypeDescriptorNode.closeParenToken());
        return parenthesisedTypeDescriptorNode.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withTypedesc(typedesc)
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ImplicitNewExpressionNode transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        if (!isInLineRange(implicitNewExpressionNode)) {
            return implicitNewExpressionNode;
        }
        Token newKeyword = getToken(implicitNewExpressionNode.newKeyword());
        ParenthesizedArgList parenthesizedArgList =
                this.modifyNode(implicitNewExpressionNode.parenthesizedArgList().orElse(null));
        if (parenthesizedArgList != null) {
            implicitNewExpressionNode = implicitNewExpressionNode.modify()
                    .withParenthesizedArgList(parenthesizedArgList).apply();
        }
        return implicitNewExpressionNode.modify()
                .withNewKeyword(formatToken(newKeyword, 0, 1, 0, 0))
                .apply();
    }

    @Override
    public QueryConstructTypeNode transform(QueryConstructTypeNode queryConstructTypeNode) {
        if (!isInLineRange(queryConstructTypeNode)) {
            return queryConstructTypeNode;
        }
        Token keyword = getToken(queryConstructTypeNode.keyword());
        KeySpecifierNode keySpecifier = this.modifyNode(queryConstructTypeNode.keySpecifier().orElse(null));
        if (keySpecifier != null) {
            queryConstructTypeNode = queryConstructTypeNode.modify()
                    .withKeySpecifier(keySpecifier).apply();
        }
        return queryConstructTypeNode.modify()
                .withKeyword(formatToken(keyword, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public FromClauseNode transform(FromClauseNode fromClauseNode) {
        if (!isInLineRange(fromClauseNode)) {
            return fromClauseNode;
        }
        Token fromKeyword = getToken(fromClauseNode.fromKeyword());
        TypedBindingPatternNode typedBindingPattern = this.modifyNode(fromClauseNode.typedBindingPattern());
        Token inKeyword = getToken(fromClauseNode.inKeyword());
        ExpressionNode expression = this.modifyNode(fromClauseNode.expression());
        return fromClauseNode.modify()
                .withFromKeyword(formatToken(fromKeyword, 1, 1, 0, 0))
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(formatToken(inKeyword, 0, 0, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public WhereClauseNode transform(WhereClauseNode whereClauseNode) {
        if (!isInLineRange(whereClauseNode)) {
            return whereClauseNode;
        }
        Token whereKeyword = getToken(whereClauseNode.whereKeyword());
        ExpressionNode expression = this.modifyNode(whereClauseNode.expression());
        return whereClauseNode.modify()
                .withWhereKeyword(formatToken(whereKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public LetClauseNode transform(LetClauseNode letClauseNode) {
        if (!isInLineRange(letClauseNode)) {
            return letClauseNode;
        }
        Token letKeyword = getToken(letClauseNode.letKeyword());
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations =
                this.modifySeparatedNodeList(letClauseNode.letVarDeclarations());
        return letClauseNode.modify()
                .withLetKeyword(formatToken(letKeyword, 0, 1, 0, 0))
                .withLetVarDeclarations(letVarDeclarations)
                .apply();
    }

    @Override
    public QueryPipelineNode transform(QueryPipelineNode queryPipelineNode) {
        if (!isInLineRange(queryPipelineNode)) {
            return queryPipelineNode;
        }
        FromClauseNode fromClause = this.modifyNode(queryPipelineNode.fromClause());
        NodeList<ClauseNode> intermediateClauses = this.modifyNodeList(queryPipelineNode.intermediateClauses());
        return queryPipelineNode.modify()
                .withFromClause(fromClause)
                .withIntermediateClauses(intermediateClauses)
                .apply();
    }

    @Override
    public SelectClauseNode transform(SelectClauseNode selectClauseNode) {
        if (!isInLineRange(selectClauseNode)) {
            return selectClauseNode;
        }
        Token selectKeyword = getToken(selectClauseNode.selectKeyword());
        ExpressionNode expression = this.modifyNode(selectClauseNode.expression());
        return selectClauseNode.modify()
                .withSelectKeyword(formatToken(selectKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public QueryExpressionNode transform(QueryExpressionNode queryExpressionNode) {
        if (!isInLineRange(queryExpressionNode)) {
            return queryExpressionNode;
        }
        QueryConstructTypeNode queryConstructType =
                this.modifyNode(queryExpressionNode.queryConstructType().orElse(null));
        QueryPipelineNode queryPipeline = this.modifyNode(queryExpressionNode.queryPipeline());
        SelectClauseNode selectClause = this.modifyNode(queryExpressionNode.selectClause());
        OnConflictClauseNode onConflictClause = this.modifyNode(queryExpressionNode.onConflictClause().orElse(null));
        LimitClauseNode limitClause = this.modifyNode(queryExpressionNode.limitClause().orElse(null));
        if (queryConstructType != null) {
            queryExpressionNode = queryExpressionNode.modify()
                    .withQueryConstructType(queryConstructType).apply();
        }
        if (onConflictClause != null) {
            queryExpressionNode = queryExpressionNode.modify()
                    .withOnConflictClause(onConflictClause).apply();
        }
        if (limitClause != null) {
            queryExpressionNode = queryExpressionNode.modify()
                    .withLimitClause(limitClause).apply();
        }
        return queryExpressionNode.modify()
                .withQueryPipeline(queryPipeline)
                .withSelectClause(selectClause)
                .apply();
    }

    @Override
    public IntersectionTypeDescriptorNode transform(IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        if (!isInLineRange(intersectionTypeDescriptorNode)) {
            return intersectionTypeDescriptorNode;
        }
        Node leftTypeDesc = this.modifyNode(intersectionTypeDescriptorNode.leftTypeDesc());
        Token bitwiseAndToken = getToken(intersectionTypeDescriptorNode.bitwiseAndToken());
        Node rightTypeDesc = this.modifyNode(intersectionTypeDescriptorNode.rightTypeDesc());
        return intersectionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withBitwiseAndToken(formatToken(bitwiseAndToken, 1, 1, 0, 0))
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public ImplicitAnonymousFunctionParameters transform(
            ImplicitAnonymousFunctionParameters implicitAnonymousFunctionParameters) {
        if (!isInLineRange(implicitAnonymousFunctionParameters)) {
            return implicitAnonymousFunctionParameters;
        }
        Token openParenToken = getToken(implicitAnonymousFunctionParameters.openParenToken());
        SeparatedNodeList<SimpleNameReferenceNode> parameters =
                this.modifySeparatedNodeList(implicitAnonymousFunctionParameters.parameters());
        Token closeParenToken = getToken(implicitAnonymousFunctionParameters.closeParenToken());
        return implicitAnonymousFunctionParameters.modify()
                .withOpenParenToken(formatToken(openParenToken, 0, 0, 0, 0))
                .withParameters(parameters)
                .withCloseParenToken(formatToken(closeParenToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public ImplicitAnonymousFunctionExpressionNode transform(
            ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        if (!isInLineRange(implicitAnonymousFunctionExpressionNode)) {
            return implicitAnonymousFunctionExpressionNode;
        }
        Node params = this.modifyNode(implicitAnonymousFunctionExpressionNode.params());
        Token rightDoubleArrow = getToken(implicitAnonymousFunctionExpressionNode.rightDoubleArrow());
        ExpressionNode expression = this.modifyNode(implicitAnonymousFunctionExpressionNode.expression());
        return implicitAnonymousFunctionExpressionNode.modify()
                .withParams(params)
                .withRightDoubleArrow(formatToken(rightDoubleArrow, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public SingletonTypeDescriptorNode transform(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        if (!isInLineRange(singletonTypeDescriptorNode)) {
            return singletonTypeDescriptorNode;
        }
        ExpressionNode simpleContExprNode = this.modifyNode(singletonTypeDescriptorNode.simpleContExprNode());
        return singletonTypeDescriptorNode.modify()
                .withSimpleContExprNode(simpleContExprNode)
                .apply();
    }

    @Override
    public MethodDeclarationNode transform(MethodDeclarationNode methodDeclarationNode) {
        if (!isInLineRange(methodDeclarationNode)) {
            return methodDeclarationNode;
        }
        MetadataNode metadata = this.modifyNode(methodDeclarationNode.metadata().orElse(null));
        NodeList<Token> qualifierList = this.modifyNodeList(methodDeclarationNode.qualifierList());
        Token functionKeyword = getToken(methodDeclarationNode.functionKeyword());
        IdentifierToken methodName = this.modifyNode(methodDeclarationNode.methodName());
        FunctionSignatureNode methodSignature = this.modifyNode(methodDeclarationNode.methodSignature());
        Token semicolon = getToken(methodDeclarationNode.semicolon());
        if (metadata != null) {
            methodDeclarationNode = methodDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return methodDeclarationNode.modify()
                .withQualifierList(qualifierList)
                .withFunctionKeyword(formatToken(functionKeyword, 0, 1, 0, 0))
                .withMethodName(methodName)
                .withMethodSignature(methodSignature)
                .withSemicolon(formatToken(semicolon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public WildcardBindingPatternNode transform(WildcardBindingPatternNode wildcardBindingPatternNode) {
        if (!isInLineRange(wildcardBindingPatternNode)) {
            return wildcardBindingPatternNode;
        }
        Token underscoreToken = getToken(wildcardBindingPatternNode.underscoreToken());
        return wildcardBindingPatternNode.modify()
                .withUnderscoreToken(formatToken(underscoreToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionalBindingPatternNode transform(FunctionalBindingPatternNode functionalBindingPatternNode) {
        if (!isInLineRange(functionalBindingPatternNode)) {
            return functionalBindingPatternNode;
        }
        Node typeReference = this.modifyNode(functionalBindingPatternNode.typeReference());
        Token openParenthesis = getToken(functionalBindingPatternNode.openParenthesis());
        SeparatedNodeList<BindingPatternNode> argListBindingPatterns =
                this.modifySeparatedNodeList(functionalBindingPatternNode.argListBindingPatterns());
        Token closeParenthesis = getToken(functionalBindingPatternNode.closeParenthesis());
        return functionalBindingPatternNode.modify()
                .withTypeReference(typeReference)
                .withOpenParenthesis(formatToken(openParenthesis, 0, 0, 0, 0))
                .withArgListBindingPatterns(argListBindingPatterns)
                .withCloseParenthesis(formatToken(closeParenthesis, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public NamedArgBindingPatternNode transform(NamedArgBindingPatternNode namedArgBindingPatternNode) {
        if (!isInLineRange(namedArgBindingPatternNode)) {
            return namedArgBindingPatternNode;
        }
        IdentifierToken argName = this.modifyNode(namedArgBindingPatternNode.argName());
        Token equalsToken = getToken(namedArgBindingPatternNode.equalsToken());
        BindingPatternNode bindingPattern = this.modifyNode(namedArgBindingPatternNode.bindingPattern());
        return namedArgBindingPatternNode.modify()
                .withArgName(argName)
                .withEqualsToken(formatToken(equalsToken, 1, 1, 0, 0))
                .withBindingPattern(bindingPattern)
                .apply();
    }

    @Override
    public AsyncSendActionNode transform(AsyncSendActionNode asyncSendActionNode) {
        if (!isInLineRange(asyncSendActionNode)) {
            return asyncSendActionNode;
        }
        ExpressionNode expression = this.modifyNode(asyncSendActionNode.expression());
        Token rightArrowToken = getToken(asyncSendActionNode.rightArrowToken());
        SimpleNameReferenceNode peerWorker = this.modifyNode(asyncSendActionNode.peerWorker());
        return asyncSendActionNode.modify()
                .withExpression(expression)
                .withRightArrowToken(formatToken(rightArrowToken, 1, 1, 0, 0))
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public SyncSendActionNode transform(SyncSendActionNode syncSendActionNode) {
        if (!isInLineRange(syncSendActionNode)) {
            return syncSendActionNode;
        }
        ExpressionNode expression = this.modifyNode(syncSendActionNode.expression());
        Token syncSendToken = getToken(syncSendActionNode.syncSendToken());
        SimpleNameReferenceNode peerWorker = this.modifyNode(syncSendActionNode.peerWorker());
        return syncSendActionNode.modify()
                .withExpression(expression)
                .withSyncSendToken(formatToken(syncSendToken, 1, 1, 0, 0))
                .withPeerWorker(peerWorker)
                .apply();
    }

    @Override
    public ReceiveActionNode transform(ReceiveActionNode receiveActionNode) {
        if (!isInLineRange(receiveActionNode)) {
            return receiveActionNode;
        }
        Token leftArrow = getToken(receiveActionNode.leftArrow());
        SimpleNameReferenceNode receiveWorkers = this.modifyNode(receiveActionNode.receiveWorkers());
        return receiveActionNode.modify()
                .withLeftArrow(formatToken(leftArrow, 1, 1, 0, 0))
                .withReceiveWorkers(receiveWorkers)
                .apply();
    }

    @Override
    public ReceiveFieldsNode transform(ReceiveFieldsNode receiveFieldsNode) {
        if (!isInLineRange(receiveFieldsNode)) {
            return receiveFieldsNode;
        }
        Token openBrace = getToken(receiveFieldsNode.openBrace());
        SeparatedNodeList<NameReferenceNode> receiveFields =
                this.modifySeparatedNodeList(receiveFieldsNode.receiveFields());
        Token closeBrace = getToken(receiveFieldsNode.closeBrace());
        return receiveFieldsNode.modify()
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 0))
                .withReceiveFields(receiveFields)
                .withCloseBrace(formatToken(closeBrace, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RestDescriptorNode transform(RestDescriptorNode restDescriptorNode) {
        if (!isInLineRange(restDescriptorNode)) {
            return restDescriptorNode;
        }
        TypeDescriptorNode typeDescriptor = this.modifyNode(restDescriptorNode.typeDescriptor());
        Token ellipsisToken = getToken(restDescriptorNode.ellipsisToken());
        return restDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public DoubleGTTokenNode transform(DoubleGTTokenNode doubleGTTokenNode) {
        if (!isInLineRange(doubleGTTokenNode)) {
            return doubleGTTokenNode;
        }
        Token openGTToken = getToken(doubleGTTokenNode.openGTToken());
        Token endGTToken = getToken(doubleGTTokenNode.endGTToken());
        return doubleGTTokenNode.modify()
                .withOpenGTToken(formatToken(openGTToken, 0, 0, 0, 0))
                .withEndGTToken(formatToken(endGTToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public TrippleGTTokenNode transform(TrippleGTTokenNode trippleGTTokenNode) {
        if (!isInLineRange(trippleGTTokenNode)) {
            return trippleGTTokenNode;
        }
        Token openGTToken = getToken(trippleGTTokenNode.openGTToken());
        Token middleGTToken = getToken(trippleGTTokenNode.middleGTToken());
        Token endGTToken = getToken(trippleGTTokenNode.endGTToken());
        return trippleGTTokenNode.modify()
                .withOpenGTToken(formatToken(openGTToken, 0, 0, 0, 0))
                .withMiddleGTToken(formatToken(middleGTToken, 0, 0, 0, 0))
                .withEndGTToken(formatToken(endGTToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public WaitActionNode transform(WaitActionNode waitActionNode) {
        if (!isInLineRange(waitActionNode)) {
            return waitActionNode;
        }
        Token waitKeyword = getToken(waitActionNode.waitKeyword());
        Node waitFutureExpr = this.modifyNode(waitActionNode.waitFutureExpr());
        return waitActionNode.modify()
                .withWaitKeyword(formatToken(waitKeyword, 1, 1, 0, 0))
                .withWaitFutureExpr(waitFutureExpr)
                .apply();
    }

    @Override
    public WaitFieldsListNode transform(WaitFieldsListNode waitFieldsListNode) {
        if (!isInLineRange(waitFieldsListNode)) {
            return waitFieldsListNode;
        }
        Token openBrace = getToken(waitFieldsListNode.openBrace());
        SeparatedNodeList<Node> waitFields = this.modifySeparatedNodeList(waitFieldsListNode.waitFields());
        Token closeBrace = getToken(waitFieldsListNode.closeBrace());
        return waitFieldsListNode.modify()
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 0))
                .withWaitFields(waitFields)
                .withCloseBrace(formatToken(closeBrace, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public WaitFieldNode transform(WaitFieldNode waitFieldNode) {
        if (!isInLineRange(waitFieldNode)) {
            return waitFieldNode;
        }
        SimpleNameReferenceNode fieldName = this.modifyNode(waitFieldNode.fieldName());
        Token colon = getToken(waitFieldNode.colon());
        ExpressionNode waitFutureExpr = this.modifyNode(waitFieldNode.waitFutureExpr());
        return waitFieldNode.modify()
                .withFieldName(fieldName)
                .withColon(formatToken(colon, 1, 1, 0, 0))
                .withWaitFutureExpr(waitFutureExpr)
                .apply();
    }

    @Override
    public AnnotAccessExpressionNode transform(AnnotAccessExpressionNode annotAccessExpressionNode) {
        if (!isInLineRange(annotAccessExpressionNode)) {
            return annotAccessExpressionNode;
        }
        ExpressionNode expression = this.modifyNode(annotAccessExpressionNode.expression());
        Token annotChainingToken = getToken(annotAccessExpressionNode.annotChainingToken());
        NameReferenceNode annotTagReference = this.modifyNode(annotAccessExpressionNode.annotTagReference());
        return annotAccessExpressionNode.modify()
                .withExpression(expression)
                .withAnnotChainingToken(formatToken(annotChainingToken, 0, 0, 0, 0))
                .withAnnotTagReference(annotTagReference)
                .apply();
    }

    @Override
    public QueryActionNode transform(QueryActionNode queryActionNode) {
        if (!isInLineRange(queryActionNode)) {
            return queryActionNode;
        }
        QueryPipelineNode queryPipeline = this.modifyNode(queryActionNode.queryPipeline());
        Token doKeyword = getToken(queryActionNode.doKeyword());
        BlockStatementNode blockStatement = this.modifyNode(queryActionNode.blockStatement());
        LimitClauseNode limitClause = this.modifyNode(queryActionNode.limitClause().orElse(null));
        if (limitClause != null) {
            queryActionNode = queryActionNode.modify()
                    .withLimitClause(limitClause).apply();
        }
        return queryActionNode.modify()
                .withQueryPipeline(queryPipeline)
                .withDoKeyword(formatToken(doKeyword, 1, 1, 0, 0))
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public OptionalFieldAccessExpressionNode transform(
            OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        if (!isInLineRange(optionalFieldAccessExpressionNode)) {
            return optionalFieldAccessExpressionNode;
        }
        ExpressionNode expression = this.modifyNode(optionalFieldAccessExpressionNode.expression());
        Token optionalChainingToken = getToken(optionalFieldAccessExpressionNode.optionalChainingToken());
        NameReferenceNode fieldName = this.modifyNode(optionalFieldAccessExpressionNode.fieldName());
        return optionalFieldAccessExpressionNode.modify()
                .withExpression(expression)
                .withOptionalChainingToken(formatToken(optionalChainingToken, 0, 0, 0, 0))
                .withFieldName(fieldName)
                .apply();
    }

    @Override
    public ConditionalExpressionNode transform(ConditionalExpressionNode conditionalExpressionNode) {
        if (!isInLineRange(conditionalExpressionNode)) {
            return conditionalExpressionNode;
        }
        ExpressionNode lhsExpression = this.modifyNode(conditionalExpressionNode.lhsExpression());
        Token questionMarkToken = getToken(conditionalExpressionNode.questionMarkToken());
        ExpressionNode middleExpression = this.modifyNode(conditionalExpressionNode.middleExpression());
        Token colonToken = getToken(conditionalExpressionNode.colonToken());
        ExpressionNode endExpression = this.modifyNode(conditionalExpressionNode.endExpression());
        return conditionalExpressionNode.modify()
                .withLhsExpression(lhsExpression)
                .withQuestionMarkToken(formatToken(questionMarkToken, 1, 1, 0, 0))
                .withMiddleExpression(middleExpression)
                .withColonToken(formatToken(colonToken, 1, 1, 0, 0))
                .withEndExpression(endExpression)
                .apply();
    }

    @Override
    public EnumDeclarationNode transform(EnumDeclarationNode enumDeclarationNode) {
        if (!isInLineRange(enumDeclarationNode)) {
            return enumDeclarationNode;
        }
        MetadataNode metadata = this.modifyNode(enumDeclarationNode.metadata().orElse(null));
        Token qualifier = getToken(enumDeclarationNode.qualifier());
        Token enumKeywordToken = getToken(enumDeclarationNode.enumKeywordToken());
        IdentifierToken identifier = this.modifyNode(enumDeclarationNode.identifier());
        Token openBraceToken = getToken(enumDeclarationNode.openBraceToken());
        SeparatedNodeList<Node> enumMemberList = this.modifySeparatedNodeList(enumDeclarationNode.enumMemberList());
        Token closeBraceToken = getToken(enumDeclarationNode.closeBraceToken());
        if (metadata != null) {
            enumDeclarationNode = enumDeclarationNode.modify()
                    .withMetadata(metadata).apply();
        }
        return enumDeclarationNode.modify()
                .withQualifier(formatToken(qualifier, 1, 1, 0, 0))
                .withEnumKeywordToken(formatToken(enumKeywordToken, 0, 1, 0, 0))
                .withIdentifier(identifier)
                .withOpenBraceToken(formatToken(openBraceToken, 0, 0, 0, 0))
                .withEnumMemberList(enumMemberList)
                .withCloseBraceToken(formatToken(closeBraceToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public EnumMemberNode transform(EnumMemberNode enumMemberNode) {
        if (!isInLineRange(enumMemberNode)) {
            return enumMemberNode;
        }
        MetadataNode metadata = this.modifyNode(enumMemberNode.metadata().orElse(null));
        IdentifierToken identifier = this.modifyNode(enumMemberNode.identifier());
        Token equalToken = getToken(enumMemberNode.equalToken().orElse(null));
        ExpressionNode constExprNode = this.modifyNode(enumMemberNode.constExprNode().orElse(null));
        if (metadata != null) {
            enumMemberNode = enumMemberNode.modify()
                    .withMetadata(metadata).apply();
        }
        return enumMemberNode.modify()
                .withEqualToken(formatToken(equalToken, 1, 1, 0, 0))
                .withIdentifier(identifier)
                .withConstExprNode(constExprNode)
                .apply();
    }

    @Override
    public TransactionStatementNode transform(TransactionStatementNode transactionStatementNode) {
        if (!isInLineRange(transactionStatementNode)) {
            return transactionStatementNode;
        }
        Token transactionKeyword = getToken(transactionStatementNode.transactionKeyword());
        BlockStatementNode blockStatement = this.modifyNode(transactionStatementNode.blockStatement());
        return transactionStatementNode.modify()
                .withTransactionKeyword(formatToken(transactionKeyword, 1, 1, 0, 0))
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public RollbackStatementNode transform(RollbackStatementNode rollbackStatementNode) {
        if (!isInLineRange(rollbackStatementNode)) {
            return rollbackStatementNode;
        }
        Token rollbackKeyword = getToken(rollbackStatementNode.rollbackKeyword());
        ExpressionNode expression = this.modifyNode(rollbackStatementNode.expression().orElse(null));
        Token semicolon = getToken(rollbackStatementNode.semicolon());
        if (expression != null) {
            rollbackStatementNode = rollbackStatementNode.modify()
                    .withExpression(expression).apply();
        }
        return rollbackStatementNode.modify()
                .withRollbackKeyword(formatToken(rollbackKeyword, 1, 1, 0, 0))
                .withSemicolon(formatToken(semicolon, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RetryStatementNode transform(RetryStatementNode retryStatementNode) {
        if (!isInLineRange(retryStatementNode)) {
            return retryStatementNode;
        }
        Token retryKeyword = getToken(retryStatementNode.retryKeyword());
        TypeParameterNode typeParameter = this.modifyNode(retryStatementNode.typeParameter().orElse(null));
        ParenthesizedArgList arguments = this.modifyNode(retryStatementNode.arguments().orElse(null));
        StatementNode retryBody = this.modifyNode(retryStatementNode.retryBody());
        if (typeParameter != null) {
            retryStatementNode = retryStatementNode.modify()
                    .withTypeParameter(typeParameter).apply();
        }
        if (arguments != null) {
            retryStatementNode = retryStatementNode.modify()
                    .withArguments(arguments).apply();
        }
        return retryStatementNode.modify()
                .withRetryKeyword(formatToken(retryKeyword, 1, 1, 0, 0))
                .withRetryBody(retryBody)
                .apply();
    }

    @Override
    public CommitActionNode transform(CommitActionNode commitActionNode) {
        if (!isInLineRange(commitActionNode)) {
            return commitActionNode;
        }
        Token commitKeyword = getToken(commitActionNode.commitKeyword());
        return commitActionNode.modify()
                .withCommitKeyword(formatToken(commitKeyword, 1, 1, 0, 0))
                .apply();
    }

    @Override
    public TransactionalExpressionNode transform(TransactionalExpressionNode transactionalExpressionNode) {
        if (!isInLineRange(transactionalExpressionNode)) {
            return transactionalExpressionNode;
        }
        Token transactionalKeyword = getToken(transactionalExpressionNode.transactionalKeyword());
        return transactionalExpressionNode.modify()
                .withTransactionalKeyword(formatToken(transactionalKeyword, 1, 1, 0, 0))
                .apply();
    }

    @Override
    public ServiceConstructorExpressionNode transform(
            ServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        if (!isInLineRange(serviceConstructorExpressionNode)) {
            return serviceConstructorExpressionNode;
        }
        NodeList<AnnotationNode> annotations = this.modifyNodeList(serviceConstructorExpressionNode.annotations());
        Token serviceKeyword = getToken(serviceConstructorExpressionNode.serviceKeyword());
        Node serviceBody = this.modifyNode(serviceConstructorExpressionNode.serviceBody());
        return serviceConstructorExpressionNode.modify()
                .withAnnotations(annotations)
                .withServiceKeyword(formatToken(serviceKeyword, 1, 1, 0, 0))
                .withServiceBody(serviceBody)
                .apply();
    }

    @Override
    public TypeReferenceTypeDescNode transform(TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        if (!isInLineRange(typeReferenceTypeDescNode)) {
            return typeReferenceTypeDescNode;
        }
        NameReferenceNode typeRef = this.modifyNode(typeReferenceTypeDescNode.typeRef());
        return typeReferenceTypeDescNode.modify()
                .withTypeRef(typeRef)
                .apply();
    }

    @Override
    public MatchStatementNode transform(MatchStatementNode matchStatementNode) {
        if (!isInLineRange(matchStatementNode)) {
            return matchStatementNode;
        }
        Token matchKeyword = getToken(matchStatementNode.matchKeyword());
        ExpressionNode condition = this.modifyNode(matchStatementNode.condition());
        Token openBrace = getToken(matchStatementNode.openBrace());
        NodeList<MatchClauseNode> matchClauses = this.modifyNodeList(matchStatementNode.matchClauses());
        Token closeBrace = getToken(matchStatementNode.closeBrace());
        return matchStatementNode.modify()
                .withMatchKeyword(formatToken(matchKeyword, 1, 1, 0, 0))
                .withCondition(condition)
                .withOpenBrace(formatToken(openBrace, 0, 0, 0, 0))
                .withMatchClauses(matchClauses)
                .withCloseBrace(formatToken(closeBrace, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public MatchClauseNode transform(MatchClauseNode matchClauseNode) {
        if (!isInLineRange(matchClauseNode)) {
            return matchClauseNode;
        }
        SeparatedNodeList<Node> matchPatterns = this.modifySeparatedNodeList(matchClauseNode.matchPatterns());
        MatchGuardNode matchGuard = this.modifyNode(matchClauseNode.matchGuard().orElse(null));
        Token rightDoubleArrow = getToken(matchClauseNode.rightDoubleArrow());
        BlockStatementNode blockStatement = this.modifyNode(matchClauseNode.blockStatement());
        if (matchGuard != null) {
            matchClauseNode = matchClauseNode.modify()
                    .withMatchGuard(matchGuard).apply();
        }
        return matchClauseNode.modify()
                .withMatchPatterns(matchPatterns)
                .withRightDoubleArrow(formatToken(rightDoubleArrow, 1, 1, 0, 0))
                .withBlockStatement(blockStatement)
                .apply();
    }

    @Override
    public MatchGuardNode transform(MatchGuardNode matchGuardNode) {
        if (!isInLineRange(matchGuardNode)) {
            return matchGuardNode;
        }
        Token ifKeyword = getToken(matchGuardNode.ifKeyword());
        ExpressionNode expression = this.modifyNode(matchGuardNode.expression());
        return matchGuardNode.modify()
                .withIfKeyword(formatToken(ifKeyword, 0, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public DistinctTypeDescriptorNode transform(DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
        if (!isInLineRange(distinctTypeDescriptorNode)) {
            return distinctTypeDescriptorNode;
        }
        Token distinctKeyword = getToken(distinctTypeDescriptorNode.distinctKeyword());
        TypeDescriptorNode typeDescriptor = this.modifyNode(distinctTypeDescriptorNode.typeDescriptor());
        return distinctTypeDescriptorNode.modify()
                .withDistinctKeyword(formatToken(distinctKeyword, 1, 1, 0, 0))
                .withTypeDescriptor(typeDescriptor)
                .apply();
    }

    @Override
    public OnConflictClauseNode transform(OnConflictClauseNode onConflictClauseNode) {
        if (!isInLineRange(onConflictClauseNode)) {
            return onConflictClauseNode;
        }
        Token onKeyword = getToken(onConflictClauseNode.onKeyword());
        Token conflictKeyword = getToken(onConflictClauseNode.conflictKeyword());
        ExpressionNode expression = this.modifyNode(onConflictClauseNode.expression());
        return onConflictClauseNode.modify()
                .withOnKeyword(formatToken(onKeyword, 1, 1, 0, 0))
                .withConflictKeyword(formatToken(conflictKeyword, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public LimitClauseNode transform(LimitClauseNode limitClauseNode) {
        if (!isInLineRange(limitClauseNode)) {
            return limitClauseNode;
        }
        Token limitKeyword = getToken(limitClauseNode.limitKeyword());
        ExpressionNode expression = this.modifyNode(limitClauseNode.expression());
        return limitClauseNode.modify()
                .withLimitKeyword(formatToken(limitKeyword, 1, 1, 0, 0))
                .withExpression(expression)
                .apply();
    }

    @Override
    public JoinClauseNode transform(JoinClauseNode joinClauseNode) {
        if (!isInLineRange(joinClauseNode)) {
            return joinClauseNode;
        }
        Token outerKeyword = getToken(joinClauseNode.outerKeyword().orElse(null));
        Token joinKeyword = getToken(joinClauseNode.joinKeyword());
        TypedBindingPatternNode typedBindingPattern = this.modifyNode(joinClauseNode.typedBindingPattern());
        Token inKeyword = getToken(joinClauseNode.inKeyword());
        ExpressionNode expression = this.modifyNode(joinClauseNode.expression());
        OnClauseNode onCondition = this.modifyNode(joinClauseNode.onCondition());
        if (outerKeyword != null) {
            joinClauseNode = joinClauseNode.modify()
                    .withOuterKeyword(formatToken(outerKeyword, 1, 1, 0, 0)).apply();
        }
        return joinClauseNode.modify()
                .withJoinKeyword(formatToken(joinKeyword, 1, 1, 0, 0))
                .withTypedBindingPattern(typedBindingPattern)
                .withInKeyword(formatToken(inKeyword, 1, 1, 0, 0))
                .withExpression(expression)
                .withOnCondition(onCondition)
                .apply();
    }

    @Override
    public OnClauseNode transform(OnClauseNode onClauseNode) {
        if (!isInLineRange(onClauseNode)) {
            return onClauseNode;
        }
        Token onKeyword = getToken(onClauseNode.onKeyword());
        Token equalsKeyword = getToken(onClauseNode.equalsKeyword());
        ExpressionNode lhsExpr = this.modifyNode(onClauseNode.lhsExpression());
        ExpressionNode rhsExpr = this.modifyNode(onClauseNode.rhsExpression());
        return onClauseNode.modify()
                .withOnKeyword(formatToken(onKeyword, 1, 1, 0, 0))
                .withLhsExpression(lhsExpr)
                .withEqualsKeyword(formatToken(equalsKeyword, 1, 1, 0,  0))
                .withRhsExpression(rhsExpr)
                .apply();
    }

    @Override
    public ListMatchPatternNode transform(ListMatchPatternNode listMatchPatternNode) {
        if (!isInLineRange(listMatchPatternNode)) {
            return listMatchPatternNode;
        }
        Token openBracket = getToken(listMatchPatternNode.openBracket());
        SeparatedNodeList<Node> matchPatterns = this.modifySeparatedNodeList(listMatchPatternNode.matchPatterns());
        RestMatchPatternNode restMatchPattern = this.modifyNode(listMatchPatternNode.restMatchPattern().orElse(null));
        Token closeBracket = getToken(listMatchPatternNode.closeBracket());
        return listMatchPatternNode.modify()
                .withOpenBracket(formatToken(openBracket, 0, 0, 0, 0))
                .withMatchPatterns(matchPatterns)
                .withRestMatchPattern(restMatchPattern)
                .withCloseBracket(formatToken(closeBracket, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public RestMatchPatternNode transform(RestMatchPatternNode restMatchPatternNode) {
        if (!isInLineRange(restMatchPatternNode)) {
            return restMatchPatternNode;
        }
        Token ellipsisToken = getToken(restMatchPatternNode.ellipsisToken());
        Token varKeywordToken = getToken(restMatchPatternNode.varKeywordToken());
        SimpleNameReferenceNode variableName = this.modifyNode(restMatchPatternNode.variableName());
        return restMatchPatternNode.modify()
                .withEllipsisToken(formatToken(ellipsisToken, 0, 0, 0, 0))
                .withVarKeywordToken(formatToken(varKeywordToken, 1, 1, 0, 0))
                .withVariableName(variableName)
                .apply();
    }

    @Override
    public FieldMatchPatternNode transform(FieldMatchPatternNode fieldMatchPatternNode) {
        if (!isInLineRange(fieldMatchPatternNode)) {
            return fieldMatchPatternNode;
        }
        SimpleNameReferenceNode fieldNameNode = this.modifyNode(fieldMatchPatternNode.fieldNameNode());
        Token colonToken = getToken(fieldMatchPatternNode.colonToken());
        Node matchPattern = this.modifyNode(fieldMatchPatternNode.matchPattern());
        return fieldMatchPatternNode.modify()
                .withFieldNameNode(fieldNameNode)
                .withColonToken(formatToken(colonToken, 1, 1, 0, 0))
                .withMatchPattern(matchPattern)
                .apply();
    }

    @Override
    public FunctionalMatchPatternNode transform(FunctionalMatchPatternNode functionalMatchPatternNode) {
        if (!isInLineRange(functionalMatchPatternNode)) {
            return functionalMatchPatternNode;
        }
        Node typeRef = this.modifyNode(functionalMatchPatternNode.typeRef());
        Token openParenthesisToken = getToken(functionalMatchPatternNode.openParenthesisToken());
        SeparatedNodeList<Node> argListMatchPatternNode =
                this.modifySeparatedNodeList(functionalMatchPatternNode.argListMatchPatternNode());
        Token closeParenthesisToken = getToken(functionalMatchPatternNode.closeParenthesisToken());
        return functionalMatchPatternNode.modify()
                .withTypeRef(typeRef)
                .withOpenParenthesisToken(formatToken(openParenthesisToken, 0, 0, 0, 0))
                .withArgListMatchPatternNode(argListMatchPatternNode)
                .withCloseParenthesisToken(formatToken(closeParenthesisToken, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public NamedArgMatchPatternNode transform(NamedArgMatchPatternNode namedArgMatchPatternNode) {
        if (!isInLineRange(namedArgMatchPatternNode)) {
            return namedArgMatchPatternNode;
        }
        IdentifierToken identifier = this.modifyNode(namedArgMatchPatternNode.identifier());
        Token equalToken = getToken(namedArgMatchPatternNode.equalToken());
        Node matchPattern = this.modifyNode(namedArgMatchPatternNode.matchPattern());
        return namedArgMatchPatternNode.modify()
                .withIdentifier(identifier)
                .withEqualToken(formatToken(equalToken, 1, 1, 0, 0))
                .withMatchPattern(matchPattern)
                .apply();
    }

    @Override
    public MarkdownDocumentationNode transform(MarkdownDocumentationNode markdownDocumentationNode) {
        if (!isInLineRange(markdownDocumentationNode)) {
            return markdownDocumentationNode;
        }
        NodeList<Node> documentationLines = this.modifyNodeList(markdownDocumentationNode.documentationLines());
        return markdownDocumentationNode.modify()
                .withDocumentationLines(documentationLines)
                .apply();
    }

    @Override
    public MarkdownDocumentationLineNode transform(MarkdownDocumentationLineNode markdownDocumentationLineNode) {
        if (!isInLineRange(markdownDocumentationLineNode)) {
            return markdownDocumentationLineNode;
        }
        Token hashToken = getToken(markdownDocumentationLineNode.hashToken());
        NodeList<Node> documentElements = this.modifyNodeList(markdownDocumentationLineNode.documentElements());
        return markdownDocumentationLineNode.modify()
                .withDocumentElements(documentElements)
                .withHashToken(formatToken(hashToken, 1, 1, 0, 0))
                .apply();
    }

    @Override
    public MarkdownParameterDocumentationLineNode transform(
            MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        if (!isInLineRange(markdownParameterDocumentationLineNode)) {
            return markdownParameterDocumentationLineNode;
        }
        Token hashToken = getToken(markdownParameterDocumentationLineNode.hashToken());
        Token plusToken = getToken(markdownParameterDocumentationLineNode.plusToken());
        Token parameterName = getToken(markdownParameterDocumentationLineNode.parameterName());
        Token minusToken = getToken(markdownParameterDocumentationLineNode.minusToken());
        NodeList<Node> documentElements =
                this.modifyNodeList(markdownParameterDocumentationLineNode.documentElements());
        return markdownParameterDocumentationLineNode.modify()
                .withHashToken(formatToken(hashToken, 1, 1, 0, 0))
                .withPlusToken(formatToken(plusToken, 1, 1, 0, 0))
                .withParameterName(formatToken(parameterName, 1, 1, 0, 0))
                .withMinusToken(formatToken(minusToken, 1, 1, 0, 0))
                .withDocumentElements(documentElements)
                .apply();
    }

    @Override
    public DocumentationReferenceNode transform(DocumentationReferenceNode documentationReferenceNode) {
        if (!isInLineRange(documentationReferenceNode)) {
            return documentationReferenceNode;
        }
        Token referenceType = getToken(documentationReferenceNode.referenceType().orElse(null));
        Token startBacktick = getToken(documentationReferenceNode.startBacktick());
        Node backtickContent = this.modifyNode(documentationReferenceNode.backtickContent());
        Token endBacktick = getToken(documentationReferenceNode.endBacktick());
        if (referenceType != null) {
            documentationReferenceNode = documentationReferenceNode.modify()
                    .withReferenceType(referenceType).apply();
        }
        return documentationReferenceNode.modify()
                .withStartBacktick(formatToken(startBacktick, 0, 0, 0, 0))
                .withBacktickContent(backtickContent)
                .withEndBacktick(formatToken(endBacktick, 0, 0, 0, 0))
                .apply();
    }

    @Override
    public OrderByClauseNode transform(OrderByClauseNode orderByClauseNode) {
        if (!isInLineRange(orderByClauseNode)) {
            return orderByClauseNode;
        }
        Token orderKeyword = getToken(orderByClauseNode.orderKeyword());
        Token byKeyword = getToken(orderByClauseNode.byKeyword());
        SeparatedNodeList<OrderKeyNode> orderKey = this.modifySeparatedNodeList(orderByClauseNode.orderKey());
        return orderByClauseNode.modify()
                .withOrderKeyword(formatToken(orderKeyword, 1, 1, 0, 0))
                .withByKeyword(formatToken(byKeyword, 1, 1, 0, 0))
                .withOrderKey(orderKey)
                .apply();
    }

    @Override
    public OrderKeyNode transform(OrderKeyNode orderKeyNode) {
        if (!isInLineRange(orderKeyNode)) {
            return orderKeyNode;
        }
        ExpressionNode expression = this.modifyNode(orderKeyNode.expression());
        Token orderDirection = getToken(orderKeyNode.orderDirection().orElse(null));
        if (orderDirection != null) {
            orderKeyNode = orderKeyNode.modify()
                    .withOrderDirection(formatToken(orderDirection, 1, 1, 0, 0)).apply();
        }
        return orderKeyNode.modify()
                .withExpression(expression)
                .apply();
    }

    /**
     * Update the minutiae and return the token.
     *
     * @param token            token
     * @param leadingSpaces    leading spaces
     * @param trailingSpaces   trailing spaces
     * @param leadingNewLines  leading new lines
     * @param trailingNewLines trailing new lines
     * @return updated token
     */
    private Token formatToken(Token token, int leadingSpaces, int trailingSpaces, int leadingNewLines,
                              int trailingNewLines) {
        if (token == null) {
            return token;
        }
        MinutiaeList leadingMinutiaeList = token.leadingMinutiae();
        MinutiaeList trailingMinutiaeList = token.trailingMinutiae();

        MinutiaeList newLeadingMinutiaeList = modifyMinutiaeList(leadingMinutiaeList, leadingSpaces, leadingNewLines);
        MinutiaeList newTrailingMinutiaeList = modifyMinutiaeList(trailingMinutiaeList, trailingSpaces,
                trailingNewLines);

        return token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private MinutiaeList modifyMinutiaeList(MinutiaeList minutiaeList, int spaces, int newLines) {
        Minutiae minutiae = NodeFactory.createWhitespaceMinutiae(getWhiteSpaces(spaces, newLines));
        return minutiaeList.add(minutiae);
    }

    private String getWhiteSpaces(int column, int newLines) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (newLines - 1); i++) {
            whiteSpaces.append("\n");
        }
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    /**
     * Initialize the token with empty minutiae lists.
     *
     * @param node node
     * @return token with empty minutiae
     */
    private <T extends Token> Token getToken(T node) {
        if (node == null) {
            return node;
        }
        MinutiaeList leadingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        MinutiaeList trailingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        if (node.containsLeadingMinutiae()) {
            leadingMinutiaeList = getCommentMinutiae(node.leadingMinutiae(), true);
        }
        if (node.containsTrailingMinutiae()) {
            trailingMinutiaeList = getCommentMinutiae(node.trailingMinutiae(), false);
        }
        return node.modify(leadingMinutiaeList, trailingMinutiaeList);
    }

    private MinutiaeList getCommentMinutiae(MinutiaeList minutiaeList, boolean isLeading) {
        MinutiaeList minutiaes = AbstractNodeFactory.createEmptyMinutiaeList();
        for (int i = 0; i < minutiaeList.size(); i++) {
            if (minutiaeList.get(i).kind().equals(SyntaxKind.COMMENT_MINUTIAE)) {
                if (i > 0) {
                    minutiaes = minutiaes.add(minutiaeList.get(i - 1));
                }
                minutiaes = minutiaes.add(minutiaeList.get(i));
                if ((i + 1) < minutiaeList.size() && isLeading) {
                    minutiaes = minutiaes.add(minutiaeList.get(i + 1));
                }
            }
        }
        return minutiaes;
    }

    // TODO: Use a generic way to get the parent node using querying.
    private <T extends Node> Node getParent(T node, SyntaxKind syntaxKind) {
        Node parent = node.parent();
        if (parent == null) {
            parent = node;
        }
        SyntaxKind parentKind = parent.kind();
        if (parentKind == SyntaxKind.MODULE_VAR_DECL) {
            if (parent.parent() != null && parent.parent().kind() == SyntaxKind.MODULE_PART &&
                    syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        } else if (parentKind == SyntaxKind.FUNCTION_DEFINITION ||
                parentKind == SyntaxKind.IF_ELSE_STATEMENT ||
                parentKind == SyntaxKind.ELSE_BLOCK ||
                parentKind == SyntaxKind.SPECIFIC_FIELD ||
                parentKind == SyntaxKind.WHILE_STATEMENT) {
            return parent;
        } else if (syntaxKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            if (parentKind == SyntaxKind.REQUIRED_PARAM ||
                    parentKind == SyntaxKind.POSITIONAL_ARG ||
                    parentKind == SyntaxKind.BINARY_EXPRESSION ||
                    parentKind == SyntaxKind.RETURN_STATEMENT ||
                    parentKind == SyntaxKind.LOCAL_VAR_DECL ||
                    (parentKind == SyntaxKind.FUNCTION_CALL && parent.parent() != null &&
                            parent.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT)) {
                return null;
            }
            return getParent(parent, syntaxKind);

        } else if (parentKind == SyntaxKind.SERVICE_DECLARATION ||
                parentKind == SyntaxKind.BINARY_EXPRESSION) {
            if (syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        } else if (parentKind == SyntaxKind.REQUIRED_PARAM ||
                parentKind == SyntaxKind.RETURN_TYPE_DESCRIPTOR) {
            return null;
        } else if (parent.parent() != null) {
            return getParent(parent, syntaxKind);
        } else {
            return null;
        }
    }

    /**
     * Get the node position.
     *
     * @param node node
     * @return node position
     */
    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange range = node.lineRange();
        LinePosition startPos = range.startLine();
        LinePosition endPos = range.endLine();
        return new DiagnosticPos(null, startPos.line() + 1, endPos.line() + 1,
                startPos.offset(), endPos.offset());
    }

    /**
     * return the indented start column.
     *
     * @param node       node
     * @param syntaxKind node kind
     * @param addSpaces  add spaces or not
     * @return start position
     */
    private int getStartColumn(Node node, SyntaxKind syntaxKind, boolean addSpaces) {
        Node parent = getParent(node, syntaxKind);
        if (parent != null) {
            return getPosition(parent).sCol + (addSpaces ? 4 : 0);
        }
        return 0;
    }

    private boolean isInLineRange(Node node) {
        if (this.lineRange == null) {
            return true;
        }
        int nodeStartLine = node.lineRange().startLine().line();
        int nodeStartOffset = node.lineRange().startLine().offset();
        int nodeEndLine = node.lineRange().endLine().line();
        int nodeEndOffset = node.lineRange().endLine().offset();

        int startLine = this.lineRange.startLine().line();
        int startOffset = this.lineRange.startLine().offset();
        int endLine = this.lineRange.endLine().line();
        int endOffset = this.lineRange.endLine().offset();

        if (nodeStartLine >= startLine && nodeEndLine <= endLine) {
            if (nodeStartLine == startLine || nodeEndLine == endLine) {
                return nodeStartOffset >= startOffset && nodeEndOffset <= endOffset;
            }
            return true;
        }
        return false;
    }

    public FormattingOptions getFormattingOptions() {
        return formattingOptions;
    }

    void setFormattingOptions(FormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
    }

    void setLineRange(LineRange lineRange) {
        this.lineRange = lineRange;
    }
}
