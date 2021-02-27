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
import io.ballerina.compiler.syntax.tree.TypeCastParamNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTypeCastParamNode extends STNode {
    public final STNode annotations;
    public final STNode type;

    STTypeCastParamNode(
            STNode annotations,
            STNode type) {
        this(
                annotations,
                type,
                Collections.emptyList());
    }

    STTypeCastParamNode(
            STNode annotations,
            STNode type,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TYPE_CAST_PARAM, diagnostics);
        this.annotations = annotations;
        this.type = type;

        addChildren(
                annotations,
                type);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTypeCastParamNode(
                this.annotations,
                this.type,
                diagnostics);
    }

    public STTypeCastParamNode modify(
            STNode annotations,
            STNode type) {
        if (checkForReferenceEquality(
                annotations,
                type)) {
            return this;
        }

        return new STTypeCastParamNode(
                annotations,
                type,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TypeCastParamNode(this, position, parent);
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
