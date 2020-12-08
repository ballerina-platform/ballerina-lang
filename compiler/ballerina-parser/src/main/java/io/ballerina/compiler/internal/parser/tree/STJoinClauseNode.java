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

import io.ballerina.compiler.syntax.tree.JoinClauseNode;
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
public class STJoinClauseNode extends STIntermediateClauseNode {
    public final STNode outerKeyword;
    public final STNode joinKeyword;
    public final STNode typedBindingPattern;
    public final STNode inKeyword;
    public final STNode expression;
    public final STNode joinOnCondition;

    STJoinClauseNode(
            STNode outerKeyword,
            STNode joinKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression,
            STNode joinOnCondition) {
        this(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition,
                Collections.emptyList());
    }

    STJoinClauseNode(
            STNode outerKeyword,
            STNode joinKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression,
            STNode joinOnCondition,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.JOIN_CLAUSE, diagnostics);
        this.outerKeyword = outerKeyword;
        this.joinKeyword = joinKeyword;
        this.typedBindingPattern = typedBindingPattern;
        this.inKeyword = inKeyword;
        this.expression = expression;
        this.joinOnCondition = joinOnCondition;

        addChildren(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STJoinClauseNode(
                this.outerKeyword,
                this.joinKeyword,
                this.typedBindingPattern,
                this.inKeyword,
                this.expression,
                this.joinOnCondition,
                diagnostics);
    }

    public STJoinClauseNode modify(
            STNode outerKeyword,
            STNode joinKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression,
            STNode joinOnCondition) {
        if (checkForReferenceEquality(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition)) {
            return this;
        }

        return new STJoinClauseNode(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new JoinClauseNode(this, position, parent);
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
