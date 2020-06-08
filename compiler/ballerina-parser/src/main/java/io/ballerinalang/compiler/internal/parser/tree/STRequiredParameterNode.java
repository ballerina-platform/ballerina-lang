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
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRequiredParameterNode extends STParameterNode {
    public final STNode leadingComma;
    public final STNode annotations;
    public final STNode visibilityQualifier;
    public final STNode typeName;
    public final STNode paramName;

    STRequiredParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode typeName,
            STNode paramName) {
        this(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName,
                Collections.emptyList());
    }

    STRequiredParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode typeName,
            STNode paramName,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.REQUIRED_PARAM, diagnostics);
        this.leadingComma = leadingComma;
        this.annotations = annotations;
        this.visibilityQualifier = visibilityQualifier;
        this.typeName = typeName;
        this.paramName = paramName;

        addChildren(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRequiredParameterNode(
                this.leadingComma,
                this.annotations,
                this.visibilityQualifier,
                this.typeName,
                this.paramName,
                diagnostics);
    }

    public STRequiredParameterNode modify(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode typeName,
            STNode paramName) {
        if (checkForReferenceEquality(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName)) {
            return this;
        }

        return new STRequiredParameterNode(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RequiredParameterNode(this, position, parent);
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
