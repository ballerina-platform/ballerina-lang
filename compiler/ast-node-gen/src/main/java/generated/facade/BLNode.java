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
package generated.facade;


import generated.internal.SyntaxNode;

/**
 * This class represents a node in the syntax tree.
 */
public abstract class BLNode {

    protected final SyntaxNode node;
    protected final int position;
    protected final BLNonTerminalNode parent;

    // Span - starting position and width
    protected final BLSpan span;
    // SpanWithMinutiae - starting position and widthWithMinutiae
    protected final BLSpan spanWithMinutiae;

    public BLNode(SyntaxNode node, int position, BLNonTerminalNode parent) {
        this.node = node;
        this.position = position;
        this.parent = parent;

        // TODO Set the width excluding the minutiae.
        this.span = new BLSpan(position, node.width());
        this.spanWithMinutiae = new BLSpan(position, node.width());
    }

    public int getPosition() {
        return position;
    }

    public BLNonTerminalNode getParent() {
        return parent;
    }

    public BLSpan getSpan() {
        return span;
    }

    public BLSpan getSpanWithMinutiae() {
        return spanWithMinutiae;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
