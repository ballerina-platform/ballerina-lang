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

import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;

/**
 * Parses a given input and produces a {@code Node}.
 *
 * @since 1.3.0
 */
public class NodeParser {

    /**
     * Parses the input as an action or expression.
     *
     * @param text the input
     * @return an {@code ExpressionNode}
     */
    public static ExpressionNode parseActionOrExpression(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsActionOrExpression().createUnlinkedFacade();
    }

    /**
     * Parses the input as a binding pattern.
     *
     * @param text the input
     * @return a {@code BindingPatternNode}
     */
    public static BindingPatternNode parseBindingPattern(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsBindingPattern().createUnlinkedFacade();
    }

    /**
     * Parses the input as a block statement.
     *
     * @param text the input
     * @return a {@code BlockStatementNode}
     */
    public static BlockStatementNode parseBlockStatement(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsBlockStatement().createUnlinkedFacade();
    }

    /**
     * Parses the input as an expression.
     *
     * @param text the input
     * @return an {@code ExpressionNode}
     */
    public static ExpressionNode parseExpression(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsExpression().createUnlinkedFacade();
    }

    /**
     * Parses the input as a function body block.
     *
     * @param text the input
     * @return a {@code FunctionBodyBlockNode}
     */
    public static FunctionBodyBlockNode parseFunctionBodyBlock(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsFunctionBodyBlock().createUnlinkedFacade();
    }

    /**
     * Parses the input an import declaration.
     *
     * @param text the input
     * @return a {@code ImportDeclarationNode}
     */
    public static ImportDeclarationNode parseImportDeclaration(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsImportDeclaration().createUnlinkedFacade();
    }

    /**
     * Parses the input a module member declaration.
     *
     * @param text the input
     * @return a {@code ModuleMemberDeclarationNode}
     */
    public static ModuleMemberDeclarationNode parseModuleMemberDeclaration(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsModuleMemberDeclaration().createUnlinkedFacade();
    }

    /**
     * Parses the input a statement.
     *
     * @param text the input
     * @return a {@code StatementNode}
     */
    public static StatementNode parseStatement(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsStatement().createUnlinkedFacade();
    }

    /**
     * Parses the input as a type descriptor.
     *
     * @param text the input
     * @return a {@code TypeDescriptorNode}
     */
    public static TypeDescriptorNode parseTypeDescriptor(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsTypeDescriptor().createUnlinkedFacade();
    }

    /**
     * Parses the input as an object member.
     *
     * @param text the input
     * @return a {@code Node}
     */
    public static Node parseObjectMember(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsObjectMember().createUnlinkedFacade();
    }

    /**
     * Parses the input as a module part.
     *
     * @param text the input
     * @return a {@code ModulePartNode}
     */
    public static ModulePartNode parseModulePart(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parse().createUnlinkedFacade();
    }

    /**
     * Parses the input as an intermediate clause.
     *
     * @param text the input
     * @param allowActions Allow actions
     * @return a {@code IntermediateClauseNode}
     */
    public static IntermediateClauseNode parseIntermediateClause(String text, boolean allowActions) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsIntermediateClause(allowActions).createUnlinkedFacade();
    }

    /**
     * Parses the input as a let variable declaration.
     *
     * @param text the input
     * @return a {@code LetVariableDeclarationNode}
     */
    public static LetVariableDeclarationNode parseLetVarDeclaration(String text, boolean allowActions) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return parser.parseAsLetVarDeclaration(allowActions).createUnlinkedFacade();
    }
}
