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

import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
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
public class STArrayTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode memberTypeDesc;
    public final STNode openBracket;
    public final STNode arrayLength;
    public final STNode closeBracket;

    STArrayTypeDescriptorNode(
            STNode memberTypeDesc,
            STNode openBracket,
            STNode arrayLength,
            STNode closeBracket) {
        this(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket,
                Collections.emptyList());
    }

    STArrayTypeDescriptorNode(
            STNode memberTypeDesc,
            STNode openBracket,
            STNode arrayLength,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ARRAY_TYPE_DESC, diagnostics);
        this.memberTypeDesc = memberTypeDesc;
        this.openBracket = openBracket;
        this.arrayLength = arrayLength;
        this.closeBracket = closeBracket;

        addChildren(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STArrayTypeDescriptorNode(
                this.memberTypeDesc,
                this.openBracket,
                this.arrayLength,
                this.closeBracket,
                diagnostics);
    }

    public STArrayTypeDescriptorNode modify(
            STNode memberTypeDesc,
            STNode openBracket,
            STNode arrayLength,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket)) {
            return this;
        }

        return new STArrayTypeDescriptorNode(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ArrayTypeDescriptorNode(this, position, parent);
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
