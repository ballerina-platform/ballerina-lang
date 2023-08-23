/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STElseBlockNode extends STNode {
    public final STNode elseKeyword;
    public final STNode elseBody;

    STElseBlockNode(
            STNode elseKeyword,
            STNode elseBody) {
        this(
                elseKeyword,
                elseBody,
                Collections.emptyList());
    }

    STElseBlockNode(
            STNode elseKeyword,
            STNode elseBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ELSE_BLOCK, diagnostics);
        this.elseKeyword = elseKeyword;
        this.elseBody = elseBody;

        addChildren(
                elseKeyword,
                elseBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STElseBlockNode(
                this.elseKeyword,
                this.elseBody,
                diagnostics);
    }

    public STElseBlockNode modify(
            STNode elseKeyword,
            STNode elseBody) {
        if (checkForReferenceEquality(
                elseKeyword,
                elseBody)) {
            return this;
        }

        return new STElseBlockNode(
                elseKeyword,
                elseBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ElseBlockNode(this, position, parent);
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
