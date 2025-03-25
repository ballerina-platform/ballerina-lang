/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.NaturalModelNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.13.0
 */
public class STNaturalModelNode extends STNode {
    public final STNode modelKeyword;
    public final STNode openParenthesis;
    public final STNode expression;
    public final STNode closeParenthesis;

    STNaturalModelNode(
            STNode modelKeyword,
            STNode openParenthesis,
            STNode expression,
            STNode closeParenthesis) {
        this(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis,
                Collections.emptyList());
    }

    STNaturalModelNode(
            STNode modelKeyword,
            STNode openParenthesis,
            STNode expression,
            STNode closeParenthesis,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NATURAL_MODEL, diagnostics);
        this.modelKeyword = modelKeyword;
        this.openParenthesis = openParenthesis;
        this.expression = expression;
        this.closeParenthesis = closeParenthesis;

        addChildren(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNaturalModelNode(
                this.modelKeyword,
                this.openParenthesis,
                this.expression,
                this.closeParenthesis,
                diagnostics);
    }

    public STNaturalModelNode modify(
            STNode modelKeyword,
            STNode openParenthesis,
            STNode expression,
            STNode closeParenthesis) {
        if (checkForReferenceEquality(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis)) {
            return this;
        }

        return new STNaturalModelNode(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new NaturalModelNode(this, position, parent);
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
