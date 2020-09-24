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
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.MetadataNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
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
    private boolean isFirstToken = true;

    /**
     * Number of of whitespace characters to be used for a single indentation.
     */
    private static final int DEFAULT_INDENTATION = 4;

    public NewFormattingTreeModifier(FormattingOptions options, LineRange lineRange) {
        super(options, lineRange);
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = formatNodeList(modulePartNode.imports(), 0, 1, 0, 2);
        NodeList<ModuleMemberDeclarationNode> members = formatNodeList(modulePartNode.members(), 0, 2, 0, 1);
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
        IdentifierToken functionName = formatToken(functionDefinitionNode.functionName(), 1, 0);
        FunctionSignatureNode functionSignatureNode = formatNode(functionDefinitionNode.functionSignature(), 1, 0);
        FunctionBodyNode functionBodyNode = formatNode(functionDefinitionNode.functionBody(), this.trailingWS, this.trailingNL);

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
        SeparatedNodeList<ParameterNode> parameters =
                formatSeparatedNodeList(functionSignatureNode.parameters(), 0, 0, 0, 0);
        if (functionSignatureNode.returnTypeDesc().isPresent()) {
            ReturnTypeDescriptorNode returnTypeDesc =
                    formatNode(functionSignatureNode.returnTypeDesc().get(), this.trailingWS, this.trailingNL);
            functionSignatureNode = functionSignatureNode.modify().withReturnTypeDesc(returnTypeDesc).apply();
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
        indent(); // increase indentation for the statements to follow.
        NodeList<StatementNode> statements = formatNodeList(functionBodyBlockNode.statements(), 0, 1, 0, 1);
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
            variableDeclarationNode = variableDeclarationNode.modify()
                    .withEqualsToken(equalToken)
                    .withInitializer(initializer)
                    .apply();
        } else {
            typedBindingPatternNode = formatNode(variableDeclarationNode.typedBindingPattern(), 0, 0);
        }

        Token semicolonToken = formatToken(variableDeclarationNode.semicolonToken(), this.trailingWS, this.trailingNL);
        return variableDeclarationNode.modify()
                .withAnnotations(annotationNodes)
                .withTypedBindingPattern(typedBindingPatternNode)
                .withSemicolonToken(semicolonToken)
                .apply();
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
        NodeList<StatementNode> statements = formatNodeList(blockStatementNode.statements(), 0, 1, 0, 1);
        unindent(); // end the indentation
        Token closeBrace = formatToken(blockStatementNode.closeBraceToken(), this.trailingWS, this.trailingNL);

        return blockStatementNode.modify()
                .withOpenBraceToken(openBrace)
                .withStatements(statements)
                .withCloseBraceToken(closeBrace)
                .apply();
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        // ideally should never reach here
        return formatToken(identifier, this.trailingWS, this.trailingNL);
    }

    @Override
    public Token transform(Token token) {
        // ideally should never reach here
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
        if (token == null || !isInLineRange(token, lineRange)) {
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
        this.isFirstToken = trailingNL > 0;
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
            if (index == size - 1) {
                // This is the last item of the list. Trailing WS and NL for the last item on the list
                // should be the WS and NL of the entire list
                newNode = formatNode(oldNode, listTrailingWS, listTrailingNL);
            } else {
                newNode = formatNode(oldNode, itemTrailingWS, itemTrailingNL);
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
        return formatSeparatedNodeList(nodeList, itemTrailingWS, itemTrailingNL, 0, 1, listTrailingWS, listTrailingNL);
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
        Node[] newNodes = new Node[size];

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
        MinutiaeList newLeadingMinutiaeList = getLeadingMinutiae();
        MinutiaeList newTrailingMinutiaeList = getTrailingMinutiae();
        return (T) token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    /**
     * Get leading minutiae.
     * 
     * @return Leading minutiae list
     */
    private MinutiaeList getLeadingMinutiae() {
        List<Minutiae> leadingMinutiae = new ArrayList<>();
        if (this.isFirstToken) {
            for (int i = 0; i < this.leadingNL; i++) {
                leadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL));
            }

            if (this.indentation > 0) {
                String wsContent = getWSContent(this.indentation);
                leadingMinutiae.add(NodeFactory.createWhitespaceMinutiae(wsContent));
            }
        }
        MinutiaeList newLeadingMinutiaeList = NodeFactory.createMinutiaeList(leadingMinutiae);
        return newLeadingMinutiaeList;
    }

    /**
     * Get trailing minutiae.
     * 
     * @return Trailing minutiae list
     */
    private MinutiaeList getTrailingMinutiae() {
        List<Minutiae> trailingMinutiae = new ArrayList<>();
        if (this.trailingWS > 0) {
            String wsContent = getWSContent(this.trailingWS);
            trailingMinutiae.add(NodeFactory.createWhitespaceMinutiae(wsContent));
        }

        if (this.trailingNL > 0) {
            trailingMinutiae.add(NodeFactory.createEndOfLineMinutiae(FormatterUtils.NEWLINE_SYMBOL));
        }
        MinutiaeList newTrailingMinutiaeList = NodeFactory.createMinutiaeList(trailingMinutiae);
        return newTrailingMinutiaeList;
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }
}
