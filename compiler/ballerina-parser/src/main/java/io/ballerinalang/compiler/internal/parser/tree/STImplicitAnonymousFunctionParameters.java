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

import io.ballerinalang.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
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
public class STImplicitAnonymousFunctionParameters extends STNode {
    public final STNode openParenToken;
    public final STNode parameters;
    public final STNode closeParenToken;

    STImplicitAnonymousFunctionParameters(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken) {
        this(
                openParenToken,
                parameters,
                closeParenToken,
                Collections.emptyList());
    }

    STImplicitAnonymousFunctionParameters(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.INFER_PARAM_LIST, diagnostics);
        this.openParenToken = openParenToken;
        this.parameters = parameters;
        this.closeParenToken = closeParenToken;

        addChildren(
                openParenToken,
                parameters,
                closeParenToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STImplicitAnonymousFunctionParameters(
                this.openParenToken,
                this.parameters,
                this.closeParenToken,
                diagnostics);
    }

    public STImplicitAnonymousFunctionParameters modify(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken) {
        if (checkForReferenceEquality(
                openParenToken,
                parameters,
                closeParenToken)) {
            return this;
        }

        return new STImplicitAnonymousFunctionParameters(
                openParenToken,
                parameters,
                closeParenToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ImplicitAnonymousFunctionParameters(this, position, parent);
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
