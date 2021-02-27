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
package io.ballerina.toml.internal.parser.tree;

import io.ballerina.toml.syntax.tree.IdentifierLiteralNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STIdentifierLiteralNode extends STValueNode {
    public final STNode value;

    STIdentifierLiteralNode(
            STNode value) {
        this(
                value,
                Collections.emptyList());
    }

    STIdentifierLiteralNode(
            STNode value,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IDENTIFIER_LITERAL, diagnostics);
        this.value = value;

        addChildren(
                value);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STIdentifierLiteralNode(
                this.value,
                diagnostics);
    }

    public STIdentifierLiteralNode modify(
            STNode value) {
        if (checkForReferenceEquality(
                value)) {
            return this;
        }

        return new STIdentifierLiteralNode(
                value,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new IdentifierLiteralNode(this, position, parent);
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
