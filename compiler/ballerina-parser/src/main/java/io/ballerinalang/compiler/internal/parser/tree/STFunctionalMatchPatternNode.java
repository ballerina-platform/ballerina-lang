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

import io.ballerinalang.compiler.syntax.tree.FunctionalMatchPatternNode;
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
public class STFunctionalMatchPatternNode extends STNode {
    public final STNode typeRef;
    public final STNode openParenthesisToken;
    public final STNode argListMatchPatternNode;
    public final STNode closeParenthesisToken;

    STFunctionalMatchPatternNode(
            STNode typeRef,
            STNode openParenthesisToken,
            STNode argListMatchPatternNode,
            STNode closeParenthesisToken) {
        this(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken,
                Collections.emptyList());
    }

    STFunctionalMatchPatternNode(
            STNode typeRef,
            STNode openParenthesisToken,
            STNode argListMatchPatternNode,
            STNode closeParenthesisToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FUNCTIONAL_MATCH_PATTERN, diagnostics);
        this.typeRef = typeRef;
        this.openParenthesisToken = openParenthesisToken;
        this.argListMatchPatternNode = argListMatchPatternNode;
        this.closeParenthesisToken = closeParenthesisToken;

        addChildren(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFunctionalMatchPatternNode(
                this.typeRef,
                this.openParenthesisToken,
                this.argListMatchPatternNode,
                this.closeParenthesisToken,
                diagnostics);
    }

    public STFunctionalMatchPatternNode modify(
            STNode typeRef,
            STNode openParenthesisToken,
            STNode argListMatchPatternNode,
            STNode closeParenthesisToken) {
        if (checkForReferenceEquality(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken)) {
            return this;
        }

        return new STFunctionalMatchPatternNode(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionalMatchPatternNode(this, position, parent);
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
