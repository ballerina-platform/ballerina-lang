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

import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.CheckExpressionNode;
import io.ballerinalang.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.DoStatementNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.ForEachStatementNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.MetadataNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.OnFailClauseNode;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.ServiceBodyNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeParameterNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.formatter.core.FormatterUtils.isInLineRange;

/**
 * A formatter implementation that updates the minutiae of a given tree
 * according to the ballerina formatting guidelines.
 *
 * @since 2.0.0
 */
public class NewFormattingTreeModifier extends FormattingTreeModifier {

    /**
     * Number of of whitespace characters to be used as the indentation for the current line.
     */
    private int indentation = 0;

    /**
     * Number of leading newlines to be added to the currently processing node.
     */
    private int leadingNL = 0;

    /**
     * Number of trailing newlines to be added to the currently processing node.
     */
    private int trailingNL = 0;

    /**
     * Number of trailing whitespace characters to be added to the currently processing node.
     */
    private int trailingWS = 0;

    /**
     * Flag indicating whether the currently formatting token is the first token of the current line.
     */
    private boolean hasNewline = true;

    /**
     * Number of of whitespace characters to be used for a single indentation.
     */
    private static final int DEFAULT_INDENTATION = 4;

    /**
     * Maximum length of a line. Any line that goes pass this limit will be wrapped.
     */
    private static final int COLUMN_LIMIT = 80;

    /**
     * Length of the currently formatting line.
     */
    private int lineLength = 0;

    /**
     * Flag indicating whether to preserve the user added newlines. Preserves up to
     * two newlines per each line-of-code.
     */
    private boolean preserveNewlines = false;

    /**
     * Flag indicating whether to line-wrap the currently processing node.
     */
    private boolean wrapLine = false;

    public NewFormattingTreeModifier(FormattingOptions options, LineRange lineRange) {
        super(options, lineRange);
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = formatNodeList(modulePartNode.imports(), 0, 1, 0, 2);
        NodeList<ModuleMemberDeclarationNode> members = formatNodeList(modulePartNode.members(), 0, 2, 0, 1, true);
        Token eofToken = formatToken(modulePartNode.eofToken(), 0, 0);
        return modulePartNode.modify(imports, members, eofToken);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(functionDefinitionNode.metadata().get(), 1, 0);
            functionDefinitionNode = functionDefinitionNode.modify().withMetadata(metadata).apply();
        }

        NodeList<Token> qualifierList = formatNodeList(functionDefinitionNode.qualifierList(), 1, 0, 1, 0);
        Token functionKeyword = formatToken(functionDefinitionNode.functionKeyword(), 1, 0);
        IdentifierToken functionName = formatToken(functionDefinitionNode.functionName(), 0, 0);
        FunctionSignatureNode functionSignatureNode = formatNode(functionDefinitionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBodyNode = formatNode(functionDefinitionNode.functionBody(),
                this.trailingWS, this.trailingNL);

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
        int currentIndentation = this.indentation;
        setIndentation(this.lineLength);
        SeparatedNodeList<ParameterNode> parameters =
                formatSeparatedNodeList(functionSignatureNode.parameters(), 0, 0, 0, 0);
        setIndentation(currentIndentation);

        Token closePara;
        if (functionSignatureNode.returnTypeDesc().isPresent()) {
            closePara = formatToken(functionSignatureNode.closeParenToken(), 1, 0);
            ReturnTypeDescriptorNode returnTypeDesc =
                    formatNode(functionSignatureNode.returnTypeDesc().get(), this.trailingWS, this.trailingNL);
            functionSignatureNode = functionSignatureNode.modify().withReturnTypeDesc(returnTypeDesc).apply();
        } else {
            closePara = formatToken(functionSignatureNode.closeParenToken(), this.trailingWS, this.trailingNL);
        }

        return functionSignatureNode.modify()
                .withOpenParenToken(openPara)
                .withCloseParenToken(closePara)
                .withParameters(parameters)
                .apply();
    }

    @Override
    public RequiredParameterNode transform(RequiredParameterNode requiredParameterNode) {
        NodeList<AnnotationNode> annotations = formatNodeList(requiredParameterNode.annotations(), 0, 1, 0, 0);
        Node typeName;
        if (requiredParameterNode.paramName().isPresent()) {
            typeName = formatNode(requiredParameterNode.typeName(), 1, 0);
            Token paramName = formatToken(requiredParameterNode.paramName().get(), this.trailingWS, this.trailingNL);
            return requiredParameterNode.modify()
                    .withAnnotations(annotations)
                    .withTypeName(typeName)
                    .withParamName(paramName)
                    .apply();
        } else {
            typeName = formatNode(requiredParameterNode.typeName(), this.trailingWS, this.trailingNL);
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
        unindent(); // reset the indentation
        Token closeBrace = formatToken(functionBodyBlockNode.closeBraceToken(), this.trailingWS, this.trailingNL);

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
            ExpressionNode initializer = formatNode(variableDeclarationNode.initializer().get(), 0, 0);
            Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(),
                    this.trailingWS, this.trailingNL);
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
                    this.trailingWS, this.trailingNL);
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
                formatNode(typedBindingPatternNode.bindingPattern(), this.trailingWS, this.trailingNL);
        return typedBindingPatternNode.modify()
                .withTypeDescriptor(typeDescriptorNode)
                .withBindingPattern(bindingPatternNode)
                .apply();
    }

    @Override
    public BuiltinSimpleNameReferenceNode transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        Token name = formatToken(builtinSimpleNameReferenceNode.name(), this.trailingWS, this.trailingNL);
        return builtinSimpleNameReferenceNode.modify().withName(name).apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        Token literalToken = formatToken(basicLiteralNode.literalToken(), this.trailingWS, this.trailingNL);
        return basicLiteralNode.modify().withLiteralToken(literalToken).apply();
    }

    @Override
    public CaptureBindingPatternNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        Token variableName = formatToken(captureBindingPatternNode.variableName(), this.trailingWS, this.trailingNL);
        return captureBindingPatternNode.modify().withVariableName(variableName).apply();
    }

    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {
        Token ifKeyword = formatToken(ifElseStatementNode.ifKeyword(), 1, 0);
        ExpressionNode condition = formatNode(ifElseStatementNode.condition(), 1, 0);
        BlockStatementNode ifBody;
        if (ifElseStatementNode.elseBody().isPresent()) {
            ifBody = formatNode(ifElseStatementNode.ifBody(), 1, 0);
            Node elseBody = formatNode(ifElseStatementNode.elseBody().get(), this.trailingWS, this.trailingNL);
            ifElseStatementNode = ifElseStatementNode.modify().withElseBody(elseBody).apply();
        } else {
            ifBody = formatNode(ifElseStatementNode.ifBody(), this.trailingWS, this.trailingNL);
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
        StatementNode elseBody = formatNode(elseBlockNode.elseBody(), this.trailingWS, this.trailingNL);
        return elseBlockNode.modify()
                .withElseKeyword(elseKeyword)
                .withElseBody(elseBody)
                .apply();
    }

    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {
        Token openBrace = formatToken(blockStatementNode.openBraceToken(), 0, 1);
        indent(); // start an indentation
        NodeList<StatementNode> statements = formatNodeList(blockStatementNode.statements(), 0, 1, 0, 1, true);
        unindent(); // end the indentation
        Token closeBrace = formatToken(blockStatementNode.closeBraceToken(), this.trailingWS, this.trailingNL);

        return blockStatementNode.modify()
                .withOpenBraceToken(openBrace)
                .withStatements(statements)
                .withCloseBraceToken(closeBrace)
                .apply();
    }

    @Override
    public RecordTypeDescriptorNode transform(RecordTypeDescriptorNode recordTypeDesc) {
        final int recorKeywordTrailingWS = 1;
        Token recordKeyword = formatNode(recordTypeDesc.recordKeyword(), recorKeywordTrailingWS, 0);
        int fieldTrailingWS = 0;
        int fieldTrailingNL = 0;
        if (shouldExpand(recordTypeDesc)) {
            fieldTrailingNL++;
        } else {
            fieldTrailingWS++;
        }

        // Set indentation for record body
        int recordKeywordStart = this.lineLength - recordKeyword.text().length() - recorKeywordTrailingWS;
        int prevIndentation = this.indentation;
        setIndentation(recordKeywordStart);
        Token bodyStartDelimiter = formatToken(recordTypeDesc.bodyStartDelimiter(), fieldTrailingWS, fieldTrailingNL);

        // Set indentation for record fields
        indent();
        NodeList<Node> fields = formatNodeList(recordTypeDesc.fields(), fieldTrailingWS, fieldTrailingNL,
                fieldTrailingWS, fieldTrailingNL, true);

        if (recordTypeDesc.recordRestDescriptor().isPresent()) {
            RecordRestDescriptorNode recordRestDescriptor =
                    formatNode(recordTypeDesc.recordRestDescriptor().get(), fieldTrailingWS, fieldTrailingNL);
            recordTypeDesc = recordTypeDesc.modify().withRecordRestDescriptor(recordRestDescriptor).apply();
        }

        // Revert indentation for record fields
        unindent();

        Token bodyEndDelimiter = formatToken(recordTypeDesc.bodyEndDelimiter(), this.trailingWS, this.trailingNL);

        // Revert indentation for record body
        setIndentation(prevIndentation);

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
            Token questionMarkToken = formatToken(recordField.questionMarkToken().get(), 0, 1);
            recordField = recordField.modify().withQuestionMarkToken(questionMarkToken).apply();
        }

        Token semicolonToken = formatToken(recordField.semicolonToken(), this.trailingWS, this.trailingNL);
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
        Token semicolonToken = formatToken(recordField.semicolonToken(), this.trailingWS, this.trailingNL);

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
        Token semicolon = formatToken(importDeclarationNode.semicolon(), this.trailingWS, this.trailingNL);

        return importDeclarationNode.modify()
                .withImportKeyword(importKeyword)
                .withModuleName(moduleNames)
                .withSemicolon(semicolon)
                .apply();
    }

    @Override
    public ImportOrgNameNode transform(ImportOrgNameNode importOrgNameNode) {
        Token orgName = formatToken(importOrgNameNode.orgName(), 0, 0);
        Token slashToken = formatToken(importOrgNameNode.slashToken(), this.trailingWS, this.trailingNL);

        return importOrgNameNode.modify()
                .withOrgName(orgName)
                .withSlashToken(slashToken)
                .apply();
    }

    @Override
    public ImportPrefixNode transform(ImportPrefixNode importPrefixNode) {
        Token asKeyword = formatToken(importPrefixNode.asKeyword(), 1, 0);
        Token prefix = formatToken(importPrefixNode.prefix(), this.trailingWS, this.trailingNL);

        return importPrefixNode.modify()
                .withAsKeyword(asKeyword)
                .withPrefix(prefix)
                .apply();
    }

    @Override
    public ImportVersionNode transform(ImportVersionNode importVersionNode) {
        Token versionKeyword = formatToken(importVersionNode.versionKeyword(), 1, 0);
        SeparatedNodeList<Token> versionNumber = formatSeparatedNodeList(importVersionNode.versionNumber(),
                0, 0, 0, 0, this.trailingWS, this.trailingNL);

        return importVersionNode.modify()
                .withVersionKeyword(versionKeyword)
                .withVersionNumber(versionNumber)
                .apply();
    }

    @Override
    public ServiceDeclarationNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        if (serviceDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(serviceDeclarationNode.metadata().get(), 1, 0);
            serviceDeclarationNode = serviceDeclarationNode.modify().withMetadata(metadata).apply();
        }

        Token serviceKeyword = formatToken(serviceDeclarationNode.serviceKeyword(), 1, 0);
        IdentifierToken serviceName = formatToken(serviceDeclarationNode.serviceName(), 1, 0);
        Token onKeyword = formatToken(serviceDeclarationNode.onKeyword(), 1, 0);
        SeparatedNodeList<ExpressionNode> expressions =
                formatSeparatedNodeList(serviceDeclarationNode.expressions(), 0, 0, 1, 0);
        Node serviceBody = formatNode(serviceDeclarationNode.serviceBody(), this.trailingWS, this.trailingNL);

        return serviceDeclarationNode.modify()
                .withServiceKeyword(serviceKeyword)
                .withServiceName(serviceName)
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
                this.trailingWS, this.trailingNL);

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
        Token closeParenToken = formatToken(parenthesizedArgList.closeParenToken(), this.trailingWS, this.trailingNL);

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
        Token closeBraceToken = formatToken(serviceBodyNode.closeBraceToken(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

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
        Node type = formatNode(returnTypeDescriptorNode.type(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

        return optionalTypeDescriptorNode.modify()
                .withTypeDescriptor(typeDescriptor)
                .withQuestionMarkToken(questionMarkToken)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression = formatNode(expressionStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(expressionStatementNode.semicolonToken(), this.trailingWS, this.trailingNL);

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(semicolonToken)
                .apply();
    }

    @Override
    public CheckExpressionNode transform(CheckExpressionNode checkExpressionNode) {
        Token checkKeyword = formatToken(checkExpressionNode.checkKeyword(), 1, 0);
        ExpressionNode expressionNode = formatNode(checkExpressionNode.expression(), this.trailingWS, this.trailingNL);

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
                .arguments(), 1, 0, 0, 0);
        Token closeParenToken = formatToken(remoteMethodCallActionNode.closeParenToken(),
                this.trailingWS, this.trailingNL);

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
        Token name = formatToken(simpleNameReferenceNode.name(), this.trailingWS, this.trailingNL);

        return simpleNameReferenceNode.modify()
                .withName(name)
                .apply();
    }

    @Override
    public TypeDefinitionNode transform(TypeDefinitionNode typeDefinitionNode) {
        if (typeDefinitionNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(typeDefinitionNode.metadata().get(), 1, 0);
            typeDefinitionNode = typeDefinitionNode.modify().withMetadata(metadata).apply();
        }
        if (typeDefinitionNode.visibilityQualifier().isPresent()) {
            Token visibilityQualifier = formatToken(typeDefinitionNode.visibilityQualifier().get(), 1, 0);
            typeDefinitionNode = typeDefinitionNode.modify().withVisibilityQualifier(visibilityQualifier).apply();
        }

        Token typeKeyword = formatToken(typeDefinitionNode.typeKeyword(), 1, 0);
        Token typeName = formatToken(typeDefinitionNode.typeName(), 1, 0);
        Node typeDescriptor = formatNode(typeDefinitionNode.typeDescriptor(), 1, 0);
        Token semicolonToken = formatToken(typeDefinitionNode.semicolonToken(), this.trailingWS, this.trailingNL);

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
                formatNode(singletonTypeDescriptorNode.simpleContExprNode(), this.trailingWS, this.trailingNL);
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
                    this.trailingWS, this.trailingNL);
            whileStatementNode = whileStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            whileBody = formatNode(whileStatementNode.whileBody(), this.trailingWS, this.trailingNL);
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
        Token closeParen = formatToken(bracedExpressionNode.closeParen(), this.trailingWS, this.trailingNL);

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
        ExpressionNode expression = formatNode(assignmentStatementNode.expression(), 0, 0);
        Token semicolonToken = formatToken(assignmentStatementNode.semicolonToken(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

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
                    this.trailingWS, this.trailingNL);
            doStatementNode = doStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(doStatementNode.blockStatement(), this.trailingWS, this.trailingNL);
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
                    this.trailingWS, this.trailingNL);
            forEachStatementNode = forEachStatementNode.modify().withOnFailClause(onFailClause).apply();
        } else {
            blockStatement = formatNode(forEachStatementNode.blockStatement(), this.trailingWS, this.trailingNL);
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
        Node rhsExpr = formatNode(binaryExpressionNode.rhsExpr(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

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
        Token semicolonToken = formatToken(returnStatementNode.semicolonToken(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

        return unionTypeDescriptorNode.modify()
                .withLeftTypeDesc(leftTypeDesc)
                .withPipeToken(pipeToken)
                .withRightTypeDesc(rightTypeDesc)
                .apply();
    }

    @Override
    public NilTypeDescriptorNode transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        Token openParenToken = formatToken(nilTypeDescriptorNode.openParenToken(), 0, 0);
        Token closeParenToken = formatToken(nilTypeDescriptorNode.closeParenToken(), this.trailingWS, this.trailingNL);

        return nilTypeDescriptorNode.modify()
                .withOpenParenToken(openParenToken)
                .withCloseParenToken(closeParenToken)
                .apply();
    }

    @Override
    public ConstantDeclarationNode transform(ConstantDeclarationNode constantDeclarationNode) {
        if (constantDeclarationNode.metadata().isPresent()) {
            MetadataNode metadata = formatNode(constantDeclarationNode.metadata().get(), 1, 0);
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
        Token semicolonToken = formatToken(constantDeclarationNode.semicolonToken(), this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

        return parameterizedTypeDescriptorNode.modify()
                .withParameterizedType(parameterizedType)
                .withTypeParameter(typeParameter)
                .apply();
    }

    @Override
    public TypeParameterNode transform(TypeParameterNode typeParameterNode) {
        Token ltToken = formatToken(typeParameterNode.ltToken(), 0, 0);
        TypeDescriptorNode typeNode = formatNode(typeParameterNode.typeNode(), 0, 0);
        Token gtToken = formatToken(typeParameterNode.gtToken(), this.trailingWS, this.trailingNL);

        return typeParameterNode.modify()
                .withTypeNode(typeNode)
                .withLtToken(ltToken)
                .withGtToken(gtToken)
                .apply();
    }

    @Override
    public FunctionTypeDescriptorNode transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        NodeList<Token> qualifierList = formatNodeList(functionTypeDescriptorNode.qualifierList(), 1, 0, 0, 1);
        Token functionKeyword = formatToken(functionTypeDescriptorNode.functionKeyword(), 1, 0);
        FunctionSignatureNode functionSignature = formatNode(functionTypeDescriptorNode.functionSignature(),
                this.trailingWS, this.trailingNL);

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
                this.trailingWS, this.trailingNL);

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
        Token semicolonToken = formatToken(externalFunctionBodyNode.semicolonToken(), this.trailingWS, this.trailingNL);

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
        Node annotReference = formatNode(annotationNode.annotReference(), 1, 0);

        if (annotationNode.annotValue().isPresent()) {
            MappingConstructorExpressionNode annotValue = formatNode(annotationNode.annotValue().get(),
                    this.trailingWS, this.trailingNL);
            annotationNode = annotationNode.modify().withAnnotValue(annotValue).apply();
        }

        return annotationNode.modify()
                .withAtToken(atToken)
                .withAnnotReference(annotReference)
                .apply();
    }

    @Override
    public MappingConstructorExpressionNode transform(
            MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        Token openBrace = formatToken(mappingConstructorExpressionNode.openBrace(), 0, 1);
        indent();
        SeparatedNodeList<MappingFieldNode> fields = formatSeparatedNodeList(
                mappingConstructorExpressionNode.fields(), 0, 0, 0, 1, 0, 1);
        unindent();
        Token closeBrace = formatToken(mappingConstructorExpressionNode.closeBrace(), this.trailingWS, this.trailingNL);

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
            fieldName = formatToken(fieldName, 1, 0);
            Token colon = formatToken(specificFieldNode.colon().get(), 1, 0);
            ExpressionNode expressionNode = formatNode(specificFieldNode.valueExpr().get(),
                    this.trailingWS, this.trailingNL);
            return specificFieldNode.modify()
                    .withFieldName(fieldName)
                    .withColon(colon)
                    .withValueExpr(expressionNode)
                    .apply();
        } else {
            fieldName = formatToken(fieldName, this.trailingWS, this.trailingNL);
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
                this.trailingWS, this.trailingNL);

        return listConstructorExpressionNode.modify()
                .withOpenBracket(openBracket)
                .withExpressions(expressions)
                .withCloseBracket(closeBracket)
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        return formatToken(identifier, this.trailingWS, this.trailingNL);
    }

    @Override
    public Token transform(Token token) {
        return formatToken(token, this.trailingWS, this.trailingNL);
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
        if (node == null) {
            return node;
        }

        if (!isInLineRange(node, lineRange)) {
            checkForNewline(node);
            return node;
        }

        int prevTrailingNL = this.trailingNL;
        int prevTrailingWS = this.trailingWS;
        this.trailingNL = trailingNL;
        this.trailingWS = trailingWS;

        node = (T) node.apply(this);
        if (this.lineLength > COLUMN_LIMIT) {
            // If at-least one child node exceeds the column-limit,
            // then the parent should be wrapped. Therefore we only
            // set the flag, but do not un-set it.
            this.wrapLine = true;
        }

        if (shouldWrapLine(node)) {
            node = wrapLine(node);
        }

        this.trailingNL = prevTrailingNL;
        this.trailingWS = prevTrailingWS;
        return node;
    }

    private <T extends Node> void checkForNewline(T node) {
        for (Minutiae minutiae : node.trailingMinutiae()) {
            if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                this.hasNewline = true;
                return;
            }
        }
    }

    /**
     * Check whether the current line should be wrapped.
     * 
     * @param node Node that is being formatted
     * @return Flag indicating whether to wrap the current line or not
     */
    private boolean shouldWrapLine(Node node) {
        if (!this.wrapLine) {
            return false;
        }

        // Currently wrapping a line is supported at following levels:
        SyntaxKind kind = node.kind();
        switch (kind) {
            // Parameters
            case DEFAULTABLE_PARAM:
            case REQUIRED_PARAM:
            case REST_PARAM:

                // Func-call arguments
            case POSITIONAL_ARG:
            case NAMED_ARG:
            case REST_ARG:

            case RETURN_TYPE_DESCRIPTOR:
                return true;
            default:
                // Expressions
                if (SyntaxKind.BINARY_EXPRESSION.compareTo(kind) <= 0 &&
                        SyntaxKind.OBJECT_CONSTRUCTOR.compareTo(kind) >= 0) {
                    return true;
                }

                // Everything else is not supported
                return false;
        }
    }

    /**
     * Wrap the node. This is equivalent to adding a newline before the node and
     * re-formatting the node. Wrapped content will start from the current level
     * of indentation.
     * 
     * @param <T> Node type
     * @param node Node to be wrapped
     * @return Wrapped node
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T wrapLine(T node) {
        this.leadingNL += 1;
        this.lineLength = 0;
        this.hasNewline = true;
        node = (T) node.apply(this);

        // Sometimes wrapping the current node wouldn't be enough.
        // Therefore set the flag so that the parent node will also
        // get wrapped, if needed.
        this.wrapLine = this.lineLength > COLUMN_LIMIT;

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
        if (token == null) {
            return token;
        }

        if (!isInLineRange(token, lineRange)) {
            checkForNewline(token);
            return token;
        }

        int prevTrailingNL = this.trailingNL;
        int prevTrailingWS = this.trailingWS;

        // Trailing newlines can be at-most 1. Rest will go as newlines for the next token
        this.trailingNL = trailingNL > 0 ? 1 : 0;
        this.trailingWS = trailingWS;

        token = formatTokenInternal(token);

        // Set the leading newlines for the next token
        this.leadingNL = trailingNL > 0 ? trailingNL - 1 : 0;

        // If this node has a trailing new line, then the next immediate token
        // will become the first token the the next line
        this.hasNewline = trailingNL > 0;
        this.trailingNL = prevTrailingNL;
        this.trailingWS = prevTrailingWS;
        return token;
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
            boolean prevPreserveNL = this.preserveNewlines;
            this.preserveNewlines = preserveNL;
            if (index == size - 1) {
                // This is the last item of the list. Trailing WS and NL for the last item on the list
                // should be the WS and NL of the entire list
                newNode = formatNode(oldNode, listTrailingWS, listTrailingNL);
            } else {
                newNode = formatNode(oldNode, itemTrailingWS, itemTrailingNL);
            }
            this.preserveNewlines = prevPreserveNL;

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

    /**
     * Format a delimited list of nodes. This method assumes the delimiters are followed by a
     * single whitespace character only.
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
            T newNode;
            if (index == size - 1) {
                // This is the last item of the list. Trailing WS and NL for the last item on the list
                // should be the WS and NL of the entire list
                newNode = formatNode(oldNode, listTrailingWS, listTrailingNL);
            } else {
                newNode = formatNode(oldNode, itemTrailingWS, itemTrailingNL);
            }
            newNodes[2 * index] = newNode;
            if (oldNode != newNode) {
                nodeModified = true;
            }

            if (index == nodeList.size() - 1) {
                break;
            }

            Token oldSeperator = nodeList.getSeparator(index);
            Token newSeperator = formatToken(oldSeperator, separatorTrailingWS, separatorTrailingNL);
            newNodes[(2 * index) + 1] = newSeperator;

            if (oldSeperator != newSeperator) {
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
        this.lineLength += token.text().length();
        MinutiaeList newTrailingMinutiaeList = getTrailingMinutiae(token);

        if (this.preserveNewlines) {
            // We reach here for the first token in a list item (i.e: first token
            // after making 'this.preserveNewlines = true').
            // However, rest of the token in the same item don't need to preserve the newlines.
            this.preserveNewlines = false;
        }

        return (T) token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
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
        if (this.hasNewline) {
            // 'hasNewlines == true' means a newline has already been added.
            // Therefore increase the 'consecutiveNewlines' count
            consecutiveNewlines++;

            for (int i = 0; i < this.leadingNL; i++) {
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
                    if (consecutiveNewlines > 0) {
                        // If there's a newline before this, then add padding to
                        // match the current indentation level
                        addWhitespace(this.indentation, leadingMinutiae);
                    } else {
                        // Else, add a single whitespace
                        addWhitespace(1, leadingMinutiae);
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

        if (consecutiveNewlines > 0) {
            addWhitespace(this.indentation, leadingMinutiae);
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
     * @param prevMinutiae Mintiae that precedes the current token
     * @return <code>true</code> if a leading newline needs to be added. <code>false</code> otherwise
     */
    private boolean shouldAddLeadingNewline(Minutiae prevMinutiae) {
        if (this.preserveNewlines) {
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
     * @param prevMinutiae Mintiae that precedes the current token
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
        if (this.trailingWS > 0) {
            addWhitespace(this.trailingWS, trailingMinutiae);
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

                    addWhitespace(this.trailingWS, trailingMinutiae);
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

        if (consecutiveNewlines == 0 && this.trailingNL > 0) {
            trailingMinutiae.add(getNewline());
        }
        MinutiaeList newTrailingMinutiaeList = NodeFactory.createMinutiaeList(trailingMinutiae);
        return newTrailingMinutiaeList;
    }

    /**
     * Check whether a trailing newline needs to be added.
     * 
     * @param prevMinutiae Mintiae that precedes the current token
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
        this.lineLength = 0;
        return NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL);
    }

    /**
     * Indent the code by the 4-whitespace characters.
     */
    private void indent() {
        this.indentation += DEFAULT_INDENTATION;
    }

    /**
     * Undo the indentation of the code by the 4-whitespace characters.
     */
    private void unindent() {
        if (this.indentation < DEFAULT_INDENTATION) {
            this.indentation = 0;
            return;
        }

        this.indentation -= DEFAULT_INDENTATION;
    }

    /**
     * Set the indentation for the code to follow.
     *
     * @param value Number of characters to set the indentation from the start of the line.
     */
    private void setIndentation(int value) {
        this.indentation = value;
    }

    private String getWSContent(int count) {
        // for all whitespaces, increase the line length
        this.lineLength += count;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Check whether a record type descriptor needs to be expanded in to multiple lines.
     * 
     * @param recordTypeDesc Record type descriptor
     * @return <code>true</code> If the record type descriptor needs to be expanded in to multiple lines.
     *         <code>false</code> otherwise
     */
    private boolean shouldExpand(RecordTypeDescriptorNode recordTypeDesc) {
        int fieldCount = recordTypeDesc.fields().size();
        fieldCount += recordTypeDesc.recordRestDescriptor().isPresent() ? 1 : 0;

        if (fieldCount <= 1) {
            return false;
        }

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
     * @param minutaieList List of minutiae to check.
     * @return <code>true</code> If the list contains any minutiae other than whitespaces. <code>false</code> otherwise
     */
    private boolean hasNonWSMinutiae(MinutiaeList minutaieList) {
        for (Minutiae minutiae : minutaieList) {
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
