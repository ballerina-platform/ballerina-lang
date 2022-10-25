/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ReCharSetRangeNoDashNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 22201.3.0
 */
public class STReCharSetRangeNoDashNode extends STNode {
    public final STNode reCharSetAtomNoDash;
    public final STNode minusToken;
    public final STNode reCharSetAtom;

    STReCharSetRangeNoDashNode(
            STNode reCharSetAtomNoDash,
            STNode minusToken,
            STNode reCharSetAtom) {
        this(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom,
                Collections.emptyList());
    }

    STReCharSetRangeNoDashNode(
            STNode reCharSetAtomNoDash,
            STNode minusToken,
            STNode reCharSetAtom,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_CHAR_SET_RANGE_NO_DASH, diagnostics);
        this.reCharSetAtomNoDash = reCharSetAtomNoDash;
        this.minusToken = minusToken;
        this.reCharSetAtom = reCharSetAtom;

        addChildren(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReCharSetRangeNoDashNode(
                this.reCharSetAtomNoDash,
                this.minusToken,
                this.reCharSetAtom,
                diagnostics);
    }

    public STReCharSetRangeNoDashNode modify(
            STNode reCharSetAtomNoDash,
            STNode minusToken,
            STNode reCharSetAtom) {
        if (checkForReferenceEquality(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom)) {
            return this;
        }

        return new STReCharSetRangeNoDashNode(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReCharSetRangeNoDashNode(this, position, parent);
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
