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
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TableTypeDescriptorNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTableTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode tableKeywordToken;
    public final STNode rowTypeParameterNode;
    public final STNode keyConstraintNode;

    STTableTypeDescriptorNode(
            STNode tableKeywordToken,
            STNode rowTypeParameterNode,
            STNode keyConstraintNode) {
        this(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode,
                Collections.emptyList());
    }

    STTableTypeDescriptorNode(
            STNode tableKeywordToken,
            STNode rowTypeParameterNode,
            STNode keyConstraintNode,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TABLE_TYPE_DESC, diagnostics);
        this.tableKeywordToken = tableKeywordToken;
        this.rowTypeParameterNode = rowTypeParameterNode;
        this.keyConstraintNode = keyConstraintNode;

        addChildren(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTableTypeDescriptorNode(
                this.tableKeywordToken,
                this.rowTypeParameterNode,
                this.keyConstraintNode,
                diagnostics);
    }

    public STTableTypeDescriptorNode modify(
            STNode tableKeywordToken,
            STNode rowTypeParameterNode,
            STNode keyConstraintNode) {
        if (checkForReferenceEquality(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode)) {
            return this;
        }

        return new STTableTypeDescriptorNode(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TableTypeDescriptorNode(this, position, parent);
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
