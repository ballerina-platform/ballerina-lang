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
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRestParameterNode extends STParameterNode {
    public final STNode leadingComma;
    public final STNode annotations;
    public final STNode typeName;
    public final STNode ellipsisToken;
    public final STNode paramName;

    STRestParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode typeName,
            STNode ellipsisToken,
            STNode paramName) {
        this(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName,
                Collections.emptyList());
    }

    STRestParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode typeName,
            STNode ellipsisToken,
            STNode paramName,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.REST_PARAM, diagnostics);
        this.leadingComma = leadingComma;
        this.annotations = annotations;
        this.typeName = typeName;
        this.ellipsisToken = ellipsisToken;
        this.paramName = paramName;

        addChildren(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRestParameterNode(
                this.leadingComma,
                this.annotations,
                this.typeName,
                this.ellipsisToken,
                this.paramName,
                diagnostics);
    }

    public STRestParameterNode modify(
            STNode leadingComma,
            STNode annotations,
            STNode typeName,
            STNode ellipsisToken,
            STNode paramName) {
        if (checkForReferenceEquality(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName)) {
            return this;
        }

        return new STRestParameterNode(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RestParameterNode(this, position, parent);
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
