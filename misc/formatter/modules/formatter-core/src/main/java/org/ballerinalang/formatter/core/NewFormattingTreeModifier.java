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
import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.MetadataNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.formatter.core.FormatterUtils.isInLineRange;

/**
 *
 */
public class NewFormattingTreeModifier extends FormattingTreeModifier {
    
    private int blockIndentation = 0;
    private int leadingNL = 0;
    private int trailingNL = 0;
    private int trailingWS = 0;
    private boolean isFirstToken = true;

    /**
     * @param options
     * @param lineRange
     */
    public NewFormattingTreeModifier(FormattingOptions options, LineRange lineRange) {
        super(options, lineRange);
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        MetadataNode metadata = formatNode(functionDefinitionNode.metadata().orElse(null), 1, 0);
        NodeList<Token> qualifierList = modifyNodeList(functionDefinitionNode.qualifierList());
        Token functionKeyword = formatToken(functionDefinitionNode.functionKeyword(), 1, 0);
        IdentifierToken functionName = formatToken(functionDefinitionNode.functionName(), 1, 0);
        FunctionSignatureNode functionSignatureNode = formatNode(functionDefinitionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBodyNode = formatNode(functionDefinitionNode.functionBody(), 0, 2);

        // FIXME: metadata cannot be null
        if (metadata != null) {
            functionDefinitionNode = functionDefinitionNode.modify().withMetadata(metadata).apply();
        }

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
        Token closePara = formatToken(functionSignatureNode.closeParenToken(), 1, 0);
        SeparatedNodeList<ParameterNode> parameters = this.modifySeparatedNodeList(functionSignatureNode.parameters());
        ReturnTypeDescriptorNode returnTypeDesc = this.modifyNode(functionSignatureNode.returnTypeDesc().orElse(null));
        if (returnTypeDesc != null) {
            functionSignatureNode = functionSignatureNode.modify()
                    .withReturnTypeDesc(returnTypeDesc).apply();
        }

        return functionSignatureNode.modify()
                .withOpenParenToken(openPara)
                .withCloseParenToken(closePara)
                .withParameters(parameters)
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        Token openBrace = formatToken(functionBodyBlockNode.openBraceToken(), 0, 1);

        // Format statements. Increase indentations.
        indent();
        NodeList<StatementNode> statements = this.modifyNodeList(functionBodyBlockNode.statements());
        unindent();

        Token closeBrace = formatToken(functionBodyBlockNode.closeBraceToken(), this.trailingWS, this.trailingNL);

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(openBrace)
                .withCloseBraceToken(closeBrace)
                .withStatements(statements)
                .apply();
    }

    @Override
    public VariableDeclarationNode transform(VariableDeclarationNode variableDeclarationNode) {
        NodeList<AnnotationNode> annotationNodes = modifyNodeList(variableDeclarationNode.annotations());
        Token finalToken = formatToken(variableDeclarationNode.finalKeyword().orElse(null), 1, 0);

        // FIXME:
        if (finalToken != null) {
            variableDeclarationNode = variableDeclarationNode.modify().withFinalKeyword(finalToken).apply();
        }

        TypedBindingPatternNode typedBindingPatternNode =
                formatNode(variableDeclarationNode.typedBindingPattern(), 1, 0);
        Token equalToken = formatToken(variableDeclarationNode.equalsToken().orElse(null), 1, 0);
        ExpressionNode initializer = formatNode(variableDeclarationNode.initializer().orElse(null), 0, 0);
        Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(), 0, 1);

        return variableDeclarationNode.modify()
                .withAnnotations(annotationNodes)
                .withTypedBindingPattern(typedBindingPatternNode)
                .withEqualsToken(equalToken)
                .withInitializer(initializer)
                .withSemicolonToken(semicolonToken)
                .apply();
    }


    @Override
    public TypedBindingPatternNode transform(TypedBindingPatternNode typedBindingPatternNode) {
        TypeDescriptorNode typeDescriptorNode = formatNode(typedBindingPatternNode.typeDescriptor(), 1, 0);
        BindingPatternNode bindingPatternNode = formatNode(typedBindingPatternNode.bindingPattern(), 1, 0);
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
        Token variableName = formatToken(captureBindingPatternNode.variableName(), 1, 0);
        return captureBindingPatternNode.modify().withVariableName(variableName).apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        return formatToken(identifier);
    }

    @Override
    public Token transform(Token token) {
        return formatToken(token);
    }

    // ------------------------------------- Set of private helper methods -------------------------------------

    @SuppressWarnings("unchecked")
    private <T extends Node> T formatNode(T node, int trailingWS, int trailingNL) {
        if (node == null || !isInLineRange(node, lineRange)) {
            return node;
        }

        int prevTrailingNL = this.trailingNL;
        int prevTrailingWS = this.trailingWS;
        this.trailingNL = trailingNL;
        this.trailingWS = trailingWS;

        node = (T) node.apply(this);

        this.trailingNL = prevTrailingNL;
        this.trailingWS = prevTrailingWS;
        return node;
    }

    private <T extends Token> T formatToken(T token, int trailingWS, int trailingNL) {
        if (token == null || !isInLineRange(token, lineRange)) {
            return token;
        }

        int prevTrailingNL = this.trailingNL;
        int prevTrailingWS = this.trailingWS;

        // Trailing newlines can be at-most 1. Rest will go as newlines for the next token
        this.trailingNL = trailingNL > 0 ? 1 : 0;
        this.trailingWS = trailingWS;

        token = formatToken(token);

        // Set the leading newlines for the next token
        this.leadingNL = trailingNL > 0 ? trailingNL - 1 : 0;

        // If this node has a trailing new line, then the next immediate token
        // will become the first token the the next line
        this.isFirstToken = trailingNL > 0;
        this.trailingNL = prevTrailingNL;
        this.trailingWS = prevTrailingWS;
        return token;
    }

    @SuppressWarnings("unchecked")
    private <T extends Token> T formatToken(T token) {
        // Create leading minutiae
        List<Minutiae> leadingMinutiae = new ArrayList<>();
        if (this.isFirstToken) {
            if (this.blockIndentation > 0) {
                String wsContent = getWSContent(this.blockIndentation);
                leadingMinutiae.add(NodeFactory.createWhitespaceMinutiae(wsContent));
            }

            for (int i = 0; i < this.leadingNL - 1; i++) {
                leadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL));
                String wsContent = getWSContent(this.blockIndentation);
                leadingMinutiae.add(NodeFactory.createWhitespaceMinutiae(wsContent));
            }
        }
        MinutiaeList newLeadingMinutiaeList = NodeFactory.createMinutiaeList(leadingMinutiae);

        // Create trailing minutiae
        List<Minutiae> trailingMinutiae = new ArrayList<>();
        if (this.trailingWS > 0) {
            String wsContent = getWSContent(this.trailingWS);
            trailingMinutiae.add(NodeFactory.createWhitespaceMinutiae(wsContent));
        }

        if (this.trailingNL > 0) {
            trailingMinutiae.add(NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL));
        }
        MinutiaeList newTrailingMinutiaeList = NodeFactory.createMinutiaeList(trailingMinutiae);

        // Update the token
        return (T) token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private void indent() {
        startBlock(4);
    }

    private void unindent() {
        endBlock(4);
    }

    private void startBlock(int value) {
        this.blockIndentation += value;
    }

    private void endBlock(int value) {
        if (this.blockIndentation < value) {
            this.blockIndentation = 0;
            return;
        }

        this.blockIndentation -= value;
    }

    private String getWSContent(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }
}
