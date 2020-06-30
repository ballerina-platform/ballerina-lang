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
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.parser.AbstractTokenReader;
import io.ballerinalang.compiler.internal.parser.BallerinaParser;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

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

    protected STNode parseTopLevelNode(SyntaxKind tokenKind) {
        STNode modelLevelDecl = getIfReusable(subtreeSupplier.peek(), isModelLevelDeclaration);
        return modelLevelDecl != null ? modelLevelDecl : super.parseTopLevelNode(tokenKind);
    }

    protected STNode parseFunctionBody(SyntaxKind tokenKind) {
        STNode funcBodyNode = getIfReusable(subtreeSupplier.peek(), isFunctionBody);
        // TODO: How to deal with object methods?
        return funcBodyNode != null ? funcBodyNode : super.parseFunctionBody(tokenKind, false);
    }

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

    private Predicate<SyntaxKind> isModelLevelDeclaration = kind -> kind == SyntaxKind.FUNCTION_DEFINITION ||
            kind == SyntaxKind.TYPE_DEFINITION || kind == SyntaxKind.IMPORT_DECLARATION;

    private Predicate<SyntaxKind> isFunctionBody = kind ->
            kind == SyntaxKind.FUNCTION_BODY_BLOCK ||
            kind == SyntaxKind.EXTERNAL_FUNCTION_BODY ||
            kind == SyntaxKind.EXPRESSION_FUNCTION_BODY;

    private Predicate<SyntaxKind> isStatement = kind -> kind == SyntaxKind.BLOCK_STATEMENT ||
            kind == SyntaxKind.IF_ELSE_STATEMENT ||
            kind == SyntaxKind.CALL_STATEMENT ||
            kind == SyntaxKind.LOCAL_VAR_DECL ||
            kind == SyntaxKind.ASSIGNMENT_STATEMENT ||
            kind == SyntaxKind.WHILE_STATEMENT;
}
