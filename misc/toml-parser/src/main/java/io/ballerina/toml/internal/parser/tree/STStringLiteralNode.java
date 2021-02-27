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

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STStringLiteralNode extends STValueNode {
    public final STNode startDoubleQuote;
    public final STNode content;
    public final STNode endDoubleQuote;

    STStringLiteralNode(
            STNode startDoubleQuote,
            STNode content,
            STNode endDoubleQuote) {
        this(
                startDoubleQuote,
                content,
                endDoubleQuote,
                Collections.emptyList());
    }

    STStringLiteralNode(
            STNode startDoubleQuote,
            STNode content,
            STNode endDoubleQuote,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.STRING_LITERAL, diagnostics);
        this.startDoubleQuote = startDoubleQuote;
        this.content = content;
        this.endDoubleQuote = endDoubleQuote;

        addChildren(
                startDoubleQuote,
                content,
                endDoubleQuote);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STStringLiteralNode(
                this.startDoubleQuote,
                this.content,
                this.endDoubleQuote,
                diagnostics);
    }

    public STStringLiteralNode modify(
            STNode startDoubleQuote,
            STNode content,
            STNode endDoubleQuote) {
        if (checkForReferenceEquality(
                startDoubleQuote,
                content,
                endDoubleQuote)) {
            return this;
        }

        return new STStringLiteralNode(
                startDoubleQuote,
                content,
                endDoubleQuote,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new StringLiteralNode(this, position, parent);
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
