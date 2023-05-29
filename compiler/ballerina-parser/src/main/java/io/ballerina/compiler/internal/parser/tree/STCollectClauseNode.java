/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import io.ballerina.compiler.syntax.tree.CollectClauseNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.7.0
 */
public class STCollectClauseNode extends STClauseNode {
    public final STNode collectKeyword;
    public final STNode expression;

    STCollectClauseNode(
            STNode collectKeyword,
            STNode expression) {
        this(
                collectKeyword,
                expression,
                Collections.emptyList());
    }

    STCollectClauseNode(
            STNode collectKeyword,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.COLLECT_CLAUSE, diagnostics);
        this.collectKeyword = collectKeyword;
        this.expression = expression;

        addChildren(
                collectKeyword,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STCollectClauseNode(
                this.collectKeyword,
                this.expression,
                diagnostics);
    }

    public STCollectClauseNode modify(
            STNode collectKeyword,
            STNode expression) {
        if (checkForReferenceEquality(
                collectKeyword,
                expression)) {
            return this;
        }

        return new STCollectClauseNode(
                collectKeyword,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new CollectClauseNode(this, position, parent);
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
