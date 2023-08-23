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

import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
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
public class STByteArrayLiteralNode extends STExpressionNode {
    public final STNode type;
    public final STNode startBacktick;
    public final STNode content;
    public final STNode endBacktick;

    STByteArrayLiteralNode(
            STNode type,
            STNode startBacktick,
            STNode content,
            STNode endBacktick) {
        this(
                type,
                startBacktick,
                content,
                endBacktick,
                Collections.emptyList());
    }

    STByteArrayLiteralNode(
            STNode type,
            STNode startBacktick,
            STNode content,
            STNode endBacktick,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.BYTE_ARRAY_LITERAL, diagnostics);
        this.type = type;
        this.startBacktick = startBacktick;
        this.content = content;
        this.endBacktick = endBacktick;

        addChildren(
                type,
                startBacktick,
                content,
                endBacktick);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STByteArrayLiteralNode(
                this.type,
                this.startBacktick,
                this.content,
                this.endBacktick,
                diagnostics);
    }

    public STByteArrayLiteralNode modify(
            STNode type,
            STNode startBacktick,
            STNode content,
            STNode endBacktick) {
        if (checkForReferenceEquality(
                type,
                startBacktick,
                content,
                endBacktick)) {
            return this;
        }

        return new STByteArrayLiteralNode(
                type,
                startBacktick,
                content,
                endBacktick,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ByteArrayLiteralNode(this, position, parent);
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
