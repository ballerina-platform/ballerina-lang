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
package internal.parser.tree;

import syntax.tree.BasicValueNode;
import syntax.tree.Node;
import syntax.tree.NonTerminalNode;
import syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STBasicValueNode extends STValueNode {
    public final STNode value;

    STBasicValueNode(
            SyntaxKind kind,
            STNode value) {
        this(
                kind,
                value,
                Collections.emptyList());
    }

    STBasicValueNode(
            SyntaxKind kind,
            STNode value,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.value = value;

        addChildren(
                value);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STBasicValueNode(
                this.kind,
                this.value,
                diagnostics);
    }

    public STBasicValueNode modify(
            SyntaxKind kind,
            STNode value) {
        if (checkForReferenceEquality(
                value)) {
            return this;
        }

        return new STBasicValueNode(
                kind,
                value,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new BasicValueNode(this, position, parent);
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
