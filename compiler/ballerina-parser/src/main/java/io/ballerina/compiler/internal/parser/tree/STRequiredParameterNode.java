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
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRequiredParameterNode extends STParameterNode {
    public final STNode annotations;
    public final STNode typeName;
    public final STNode paramName;

    STRequiredParameterNode(
            STNode annotations,
            STNode typeName,
            STNode paramName) {
        this(
                annotations,
                typeName,
                paramName,
                Collections.emptyList());
    }

    STRequiredParameterNode(
            STNode annotations,
            STNode typeName,
            STNode paramName,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.REQUIRED_PARAM, diagnostics);
        this.annotations = annotations;
        this.typeName = typeName;
        this.paramName = paramName;

        addChildren(
                annotations,
                typeName,
                paramName);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRequiredParameterNode(
                this.annotations,
                this.typeName,
                this.paramName,
                diagnostics);
    }

    public STRequiredParameterNode modify(
            STNode annotations,
            STNode typeName,
            STNode paramName) {
        if (checkForReferenceEquality(
                annotations,
                typeName,
                paramName)) {
            return this;
        }

        return new STRequiredParameterNode(
                annotations,
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
