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
 * Parses a given input and produces a {@code Node}  / {@code NodeList}.
 *
 * @since 1.3.0
 */
public class NodeParser {

    /**
     * Parses the input as statements.
     *
     * @param text the input
     * @return a {@code NodeList<StatementNode>}
     */
    public static NodeList<StatementNode> parseStatements(String text) {
        BallerinaParser parser = ParserFactory.getParser(text);
        return new NodeList<>(parser.parseAsStatements().createUnlinkedFacade());
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
}
