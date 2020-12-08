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
 * Represents the parent node of a {@code Token} attached to an invalid {@code Minutiae} node.
 * <p>
 * This node doesn't have a parent, because it is a detached node from the tree.
 *
 * @since 2.0.0
 */
public class InvalidTokenMinutiaeNode extends NonTerminalNode {
    private final Minutiae parentMinutiae;

    public InvalidTokenMinutiaeNode(STNode internalNode,
                                    int position,
                                    Minutiae parentMinutiae,
                                    SyntaxTree syntaxTree) {
        // This is a detached node, therefore the parent has to be null
        super(internalNode, position, null);
        // We need to syntax tree to calculate the node location etc.
        this.syntaxTree = syntaxTree;
        this.parentMinutiae = parentMinutiae;
    }

    public Token invalidToken() {
        return childInBucket(0);
    }

    public Minutiae parentMinutiae() {
        return parentMinutiae;
    }

    @Override
    protected String[] childNames() {
        return new String[]{"invalidToken"};
    }

    @Override
    public void accept(NodeVisitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T apply(NodeTransformer<T> transformer) {
        throw new UnsupportedOperationException();
    }
}
