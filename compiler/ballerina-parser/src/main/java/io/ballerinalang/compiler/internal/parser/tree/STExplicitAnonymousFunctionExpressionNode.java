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

import io.ballerinalang.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
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
public class STExplicitAnonymousFunctionExpressionNode extends STAnonymousFunctionExpressionNode {
    public final STNode annotations;
    public final STNode functionKeyword;
    public final STNode functionSignature;
    public final STNode functionBody;

    STExplicitAnonymousFunctionExpressionNode(
            STNode annotations,
            STNode functionKeyword,
            STNode functionSignature,
            STNode functionBody) {
        this(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody,
                Collections.emptyList());
    }

    STExplicitAnonymousFunctionExpressionNode(
            STNode annotations,
            STNode functionKeyword,
            STNode functionSignature,
            STNode functionBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION, diagnostics);
        this.annotations = annotations;
        this.functionKeyword = functionKeyword;
        this.functionSignature = functionSignature;
        this.functionBody = functionBody;

        addChildren(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STExplicitAnonymousFunctionExpressionNode(
                this.annotations,
                this.functionKeyword,
                this.functionSignature,
                this.functionBody,
                diagnostics);
    }

    public STExplicitAnonymousFunctionExpressionNode modify(
            STNode annotations,
            STNode functionKeyword,
            STNode functionSignature,
            STNode functionBody) {
        if (checkForReferenceEquality(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody)) {
            return this;
        }

        return new STExplicitAnonymousFunctionExpressionNode(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ExplicitAnonymousFunctionExpressionNode(this, position, parent);
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
