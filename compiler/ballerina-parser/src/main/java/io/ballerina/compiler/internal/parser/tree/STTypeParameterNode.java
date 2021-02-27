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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTypeParameterNode extends STNode {
    public final STNode ltToken;
    public final STNode typeNode;
    public final STNode gtToken;

    STTypeParameterNode(
            STNode ltToken,
            STNode typeNode,
            STNode gtToken) {
        this(
                ltToken,
                typeNode,
                gtToken,
                Collections.emptyList());
    }

    STTypeParameterNode(
            STNode ltToken,
            STNode typeNode,
            STNode gtToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TYPE_PARAMETER, diagnostics);
        this.ltToken = ltToken;
        this.typeNode = typeNode;
        this.gtToken = gtToken;

        addChildren(
                ltToken,
                typeNode,
                gtToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTypeParameterNode(
                this.ltToken,
                this.typeNode,
                this.gtToken,
                diagnostics);
    }

    public STTypeParameterNode modify(
            STNode ltToken,
            STNode typeNode,
            STNode gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                typeNode,
                gtToken)) {
            return this;
        }

        return new STTypeParameterNode(
                ltToken,
                typeNode,
                gtToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TypeParameterNode(this, position, parent);
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
