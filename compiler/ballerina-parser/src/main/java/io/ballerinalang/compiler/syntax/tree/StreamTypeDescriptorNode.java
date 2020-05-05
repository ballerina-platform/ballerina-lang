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

import io.ballerinalang.compiler.internal.parser.tree.STNode;

/**
 * This is a generated syntax tree node.
 *
 * @since 1.3.0
 */
public class StreamTypeDescriptorNode extends NonTerminalNode {

    public StreamTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token streamKeywordToken() {
        return childInBucket(0);
    }

    public Token ltToken() {
        return childInBucket(1);
    }

    public Node leftTypeDescNode() {
        return childInBucket(2);
    }

    public Token commaToken() {
        return childInBucket(3);
    }

    public Node rightTypeDescNode() {
        return childInBucket(4);
    }

    public Token gtToken() {
        return childInBucket(5);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "streamKeywordToken",
                "ltToken",
                "leftTypeDescNode",
                "commaToken",
                "rightTypeDescNode",
                "gtToken"};
    }

    public StreamTypeDescriptorNode modify(
            Token streamKeywordToken,
            Token ltToken,
            Node leftTypeDescNode,
            Token commaToken,
            Node rightTypeDescNode,
            Token gtToken) {
        if (checkForReferenceEquality(
                streamKeywordToken,
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken)) {
            return this;
        }

        return NodeFactory.createStreamTypeDescriptorNode(
                streamKeywordToken,
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }
}
