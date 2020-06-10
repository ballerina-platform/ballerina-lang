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

import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
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
public class STErrorTypeParamsNode extends STNode {
    public final STNode ltToken;
    public final STNode parameter;
    public final STNode gtToken;

    STErrorTypeParamsNode(
            STNode ltToken,
            STNode parameter,
            STNode gtToken) {
        this(
                ltToken,
                parameter,
                gtToken,
                Collections.emptyList());
    }

    STErrorTypeParamsNode(
            STNode ltToken,
            STNode parameter,
            STNode gtToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ERROR_TYPE_PARAMS, diagnostics);
        this.ltToken = ltToken;
        this.parameter = parameter;
        this.gtToken = gtToken;

        addChildren(
                ltToken,
                parameter,
                gtToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STErrorTypeParamsNode(
                this.ltToken,
                this.parameter,
                this.gtToken,
                diagnostics);
    }

    public STErrorTypeParamsNode modify(
            STNode ltToken,
            STNode parameter,
            STNode gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                parameter,
                gtToken)) {
            return this;
        }

        return new STErrorTypeParamsNode(
                ltToken,
                parameter,
                gtToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ErrorTypeParamsNode(this, position, parent);
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
