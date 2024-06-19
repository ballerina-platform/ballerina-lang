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

import io.ballerina.toml.syntax.tree.LiteralStringLiteralNode;
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
public class STLiteralStringLiteralNode extends STValueNode {
    public final STNode startSingleQuote;
    public final STNode content;
    public final STNode endSingleQuote;

    STLiteralStringLiteralNode(
            STNode startSingleQuote,
            STNode content,
            STNode endSingleQuote) {
        this(
                startSingleQuote,
                content,
                endSingleQuote,
                Collections.emptyList());
    }

    STLiteralStringLiteralNode(
            STNode startSingleQuote,
            STNode content,
            STNode endSingleQuote,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LITERAL_STRING, diagnostics);
        this.startSingleQuote = startSingleQuote;
        this.content = content;
        this.endSingleQuote = endSingleQuote;

        addChildren(
                startSingleQuote,
                content,
                endSingleQuote);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STLiteralStringLiteralNode(
                this.startSingleQuote,
                this.content,
                this.endSingleQuote,
                diagnostics);
    }

    public STLiteralStringLiteralNode modify(
            STNode startSingleQuote,
            STNode content,
            STNode endSingleQuote) {
        if (checkForReferenceEquality(
                startSingleQuote,
                content,
                endSingleQuote)) {
            return this;
        }

        return new STLiteralStringLiteralNode(
                startSingleQuote,
                content,
                endSingleQuote,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new LiteralStringLiteralNode(this, position, parent);
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
