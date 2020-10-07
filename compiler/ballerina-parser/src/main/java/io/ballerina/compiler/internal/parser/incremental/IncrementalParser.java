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
package io.ballerina.compiler.internal.parser.incremental;

import io.ballerina.compiler.internal.parser.AbstractTokenReader;
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.function.Predicate;

/**
 * An incremental parser for Ballerina.
 * <p>
 * Reuses nodes and tokens from the old tree.
 *
 * @since 1.3.0
 */
public class IncrementalParser extends BallerinaParser {
    private final UnmodifiedSubtreeSupplier subtreeSupplier;

    public IncrementalParser(AbstractTokenReader tokenReader, UnmodifiedSubtreeSupplier subtreeSupplier) {
        super(tokenReader);
        this.subtreeSupplier = subtreeSupplier;
    }

    @Override
    protected STNode parseTopLevelNode() {
        STNode modelLevelDecl = getIfReusable(subtreeSupplier.peek(), isModelLevelDeclaration);
        return modelLevelDecl != null ? modelLevelDecl : super.parseTopLevelNode();
    }

    @Override
    protected STNode parseFunctionBody() {
        STNode funcBodyNode = getIfReusable(subtreeSupplier.peek(), isFunctionBody);
        return funcBodyNode != null ? funcBodyNode : super.parseFunctionBody();
    }

    @Override
    protected STNode parseStatement() {
        STNode stmtNode = getIfReusable(subtreeSupplier.peek(), isStatement);
        return stmtNode != null ? stmtNode : super.parseStatement();
    }

    private STNode getIfReusable(STNode node, Predicate<SyntaxKind> predicate) {
        if (node != null && predicate.test(node.kind)) {
            this.subtreeSupplier.consume();
        }
        return node;
    }

    private Predicate<SyntaxKind> isModelLevelDeclaration =
            kind -> SyntaxKind.IMPORT_DECLARATION.compareTo(kind) <= 0 &&
                    SyntaxKind.ENUM_DECLARATION.compareTo(kind) >= 0;

    private Predicate<SyntaxKind> isFunctionBody = kind ->
            kind == SyntaxKind.FUNCTION_BODY_BLOCK ||
            kind == SyntaxKind.EXTERNAL_FUNCTION_BODY ||
            kind == SyntaxKind.EXPRESSION_FUNCTION_BODY;

    private Predicate<SyntaxKind> isStatement = kind -> SyntaxKind.BLOCK_STATEMENT.compareTo(kind) <= 0 &&
            SyntaxKind.INVALID_EXPRESSION_STATEMENT.compareTo(kind) >= 0;
}
