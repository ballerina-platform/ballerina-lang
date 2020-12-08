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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

/**
 * Represents a literal value in the syntax tree.
 *
 * @since 2.0.0
 */
public class LiteralValueToken extends Token {

    public LiteralValueToken(STNode token, int position, NonTerminalNode parent) {
        super(token, position, parent);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    public LiteralValueToken modify(String text) {
        return modify(text, this.leadingMinutiae(), this.trailingMinutiae());
    }

    public LiteralValueToken modify(MinutiaeList leadingMinutiae, MinutiaeList trailingMinutiae) {
        if (internalNode.leadingMinutiae() == leadingMinutiae.internalNode() &&
                internalNode.trailingMinutiae() == trailingMinutiae.internalNode()) {
            return this;
        } else {
            return NodeFactory.createLiteralValueToken(this.kind(), this.text(), leadingMinutiae, trailingMinutiae);
        }
    }

    public LiteralValueToken modify(String text, MinutiaeList leadingMinutiae, MinutiaeList trailingMinutiae) {
        if (text().equals(text) &&
                internalNode.leadingMinutiae() == leadingMinutiae.internalNode() &&
                internalNode.trailingMinutiae() == trailingMinutiae.internalNode()) {
            return this;
        } else {
            return NodeFactory.createLiteralValueToken(this.kind(), text, leadingMinutiae, trailingMinutiae);
        }
    }
}
