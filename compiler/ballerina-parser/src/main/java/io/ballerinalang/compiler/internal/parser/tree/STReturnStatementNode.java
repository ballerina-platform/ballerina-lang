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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STReturnStatementNode extends STStatementNode {
    public final STNode returnKeyword;
    public final STNode expression;
    public final STNode semicolonToken;

    STReturnStatementNode(
            STNode returnKeyword,
            STNode expression,
            STNode semicolonToken) {
        this(
                returnKeyword,
                expression,
                semicolonToken,
                Collections.emptyList());
    }

    STReturnStatementNode(
            STNode returnKeyword,
            STNode expression,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RETURN_STATEMENT, diagnostics);
        this.returnKeyword = returnKeyword;
        this.expression = expression;
        this.semicolonToken = semicolonToken;

        addChildren(
                returnKeyword,
                expression,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReturnStatementNode(
                this.returnKeyword,
                this.expression,
                this.semicolonToken,
                diagnostics);
    }

    public STReturnStatementNode modify(
            STNode returnKeyword,
            STNode expression,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                returnKeyword,
                expression,
                semicolonToken)) {
            return this;
        }

        return new STReturnStatementNode(
                returnKeyword,
                expression,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReturnStatementNode(this, position, parent);
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
