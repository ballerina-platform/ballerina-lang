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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;

/**
 * This class represents a node in the syntax tree.
 */
public abstract class Node {

    protected final STNode node;
    protected final int position;
    protected final NonTerminalNode parent;

    // Span - starting startOffset and width
    protected final Span span;
    // SpanWithMinutiae - starting startOffset and widthWithMinutiae
    protected final Span spanWithMinutiae;

    public Node(STNode node, int position, NonTerminalNode parent) {
        this.node = node;
        this.position = position;
        this.parent = parent;

        // TODO Set the width excluding the minutiae.
        this.span = new Span(position, node.width());
        this.spanWithMinutiae = new Span(position, node.width());
    }

    public int getPosition() {
        return position;
    }

    public NonTerminalNode getParent() {
        return parent;
    }

    public Span getSpan() {
        return span;
    }

    public Span getSpanWithMinutiae() {
        return spanWithMinutiae;
    }

    public SyntaxKind getKind() {
        return node.kind;
    }

    // TODO Temp method. We need to find a way to get the green node from a red node.
    public STNode getInternalNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
