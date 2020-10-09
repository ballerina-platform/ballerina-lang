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
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTupleTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode openBracketToken;
    public final STNode memberTypeDesc;
    public final STNode closeBracketToken;

    STTupleTypeDescriptorNode(
            STNode openBracketToken,
            STNode memberTypeDesc,
            STNode closeBracketToken) {
        this(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken,
                Collections.emptyList());
    }

    STTupleTypeDescriptorNode(
            STNode openBracketToken,
            STNode memberTypeDesc,
            STNode closeBracketToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TUPLE_TYPE_DESC, diagnostics);
        this.openBracketToken = openBracketToken;
        this.memberTypeDesc = memberTypeDesc;
        this.closeBracketToken = closeBracketToken;

        addChildren(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTupleTypeDescriptorNode(
                this.openBracketToken,
                this.memberTypeDesc,
                this.closeBracketToken,
                diagnostics);
    }

    public STTupleTypeDescriptorNode modify(
            STNode openBracketToken,
            STNode memberTypeDesc,
            STNode closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken)) {
            return this;
        }

        return new STTupleTypeDescriptorNode(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TupleTypeDescriptorNode(this, position, parent);
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
