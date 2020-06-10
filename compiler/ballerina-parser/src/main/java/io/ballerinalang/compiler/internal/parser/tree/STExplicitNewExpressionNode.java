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

import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
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
public class STExplicitNewExpressionNode extends STNewExpressionNode {
    public final STNode newKeyword;
    public final STNode typeDescriptor;
    public final STNode parenthesizedArgList;

    STExplicitNewExpressionNode(
            STNode newKeyword,
            STNode typeDescriptor,
            STNode parenthesizedArgList) {
        this(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList,
                Collections.emptyList());
    }

    STExplicitNewExpressionNode(
            STNode newKeyword,
            STNode typeDescriptor,
            STNode parenthesizedArgList,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.EXPLICIT_NEW_EXPRESSION, diagnostics);
        this.newKeyword = newKeyword;
        this.typeDescriptor = typeDescriptor;
        this.parenthesizedArgList = parenthesizedArgList;

        addChildren(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STExplicitNewExpressionNode(
                this.newKeyword,
                this.typeDescriptor,
                this.parenthesizedArgList,
                diagnostics);
    }

    public STExplicitNewExpressionNode modify(
            STNode newKeyword,
            STNode typeDescriptor,
            STNode parenthesizedArgList) {
        if (checkForReferenceEquality(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList)) {
            return this;
        }

        return new STExplicitNewExpressionNode(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ExplicitNewExpressionNode(this, position, parent);
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
