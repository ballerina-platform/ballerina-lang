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

import io.ballerinalang.compiler.syntax.tree.FromClauseNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STFromClauseNode extends STClauseNode {
    public final STNode fromKeyword;
    public final STNode typedBindingPattern;
    public final STNode inKeyword;
    public final STNode expression;

    STFromClauseNode(
            STNode fromKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression) {
        this(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                Collections.emptyList());
    }

    STFromClauseNode(
            STNode fromKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FROM_CLAUSE, diagnostics);
        this.fromKeyword = fromKeyword;
        this.typedBindingPattern = typedBindingPattern;
        this.inKeyword = inKeyword;
        this.expression = expression;

        addChildren(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFromClauseNode(
                this.fromKeyword,
                this.typedBindingPattern,
                this.inKeyword,
                this.expression,
                diagnostics);
    }

    public STFromClauseNode modify(
            STNode fromKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression) {
        if (checkForReferenceEquality(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression)) {
            return this;
        }

        return new STFromClauseNode(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FromClauseNode(this, position, parent);
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
