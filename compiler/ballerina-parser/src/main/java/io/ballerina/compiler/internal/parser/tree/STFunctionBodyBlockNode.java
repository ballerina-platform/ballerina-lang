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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STFunctionBodyBlockNode extends STFunctionBodyNode {
    public final STNode openBraceToken;
    public final STNode namedWorkerDeclarator;
    public final STNode statements;
    public final STNode closeBraceToken;
    public final STNode semicolonToken;

    STFunctionBodyBlockNode(
            STNode openBraceToken,
            STNode namedWorkerDeclarator,
            STNode statements,
            STNode closeBraceToken,
            STNode semicolonToken) {
        this(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken,
                Collections.emptyList());
    }

    STFunctionBodyBlockNode(
            STNode openBraceToken,
            STNode namedWorkerDeclarator,
            STNode statements,
            STNode closeBraceToken,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FUNCTION_BODY_BLOCK, diagnostics);
        this.openBraceToken = openBraceToken;
        this.namedWorkerDeclarator = namedWorkerDeclarator;
        this.statements = statements;
        this.closeBraceToken = closeBraceToken;
        this.semicolonToken = semicolonToken;

        addChildren(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFunctionBodyBlockNode(
                this.openBraceToken,
                this.namedWorkerDeclarator,
                this.statements,
                this.closeBraceToken,
                this.semicolonToken,
                diagnostics);
    }

    public STFunctionBodyBlockNode modify(
            STNode openBraceToken,
            STNode namedWorkerDeclarator,
            STNode statements,
            STNode closeBraceToken,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken)) {
            return this;
        }

        return new STFunctionBodyBlockNode(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionBodyBlockNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
