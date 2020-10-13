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
package io.ballerina.compiler.internal.syntax;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;

/**
 * Represents a list of {@code Node}s. This class is not exposed with the syntax tre API.
 * <p>
 * It is primarily used to hold a list of syntax tree nodes of the same kind.
 * E.g., a list of statements in a block statement, a list of module-level members in a module part.
 *
 * @since 1.3.0
 */
public class ExternalTreeNodeList extends NonTerminalNode {

    public ExternalTreeNodeList(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    @Override
    public void accept(NodeVisitor visitor) {
    }

    @Override
    public <T> T apply(NodeTransformer<T> transformer) {
        return null;
    }

    protected <T extends Node> T childInBucket(int bucket) {
        T child = (T) childBuckets[bucket];
        if (child != null) {
            return child;
        }

        STNode internalChild = internalNode.childInBucket(bucket);
        child = (T) internalChild.createFacade(getChildPosition(bucket), this.parent);
        childBuckets[bucket] = child;
        return child;
    }

    @Override
    protected String[] childNames() {
        return new String[0];
    }
}
