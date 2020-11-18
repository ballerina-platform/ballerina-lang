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

import java.util.Collection;

import static io.ballerina.compiler.internal.syntax.NodeListUtils.rangeCheck;

/**
 * Represents a list of {@code Node}s separated by a separator.
 *
 * @param <T> the type of the constituent node instance
 */
public class SeparatedNodeList<T extends Node> extends NodeList<T> {

    private final int separatorSize;

    SeparatedNodeList(NonTerminalNode node) {
        // The list size is always an odd number
        // E.g., If the list size is 7, then there are 4 nodes and 3 separators
        super(node, (node.bucketCount() + 1) / 2);
        this.separatorSize = size != 0 ? size - 1 : 0;

        // TODO check whether the given node is a node list with separator
    }

    // Positional access methods

    public T get(int index) {
        rangeCheck(index, size);
        return this.nonTerminalNode.childInBucket(index * 2);
    }

    // Modification methods

    public NodeList<T> add(T node) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> add(int index, T node) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> addAll(Collection<T> c) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> set(int index, T node) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> remove(int index) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> remove(T node) {
        throw new UnsupportedOperationException();
    }

    public NodeList<T> removeAll(Collection<T> c) {
        throw new UnsupportedOperationException();
    }

    // Separator related methods

    public int separatorSize() {
        return this.separatorSize;
    }

    public Token getSeparator(int index) {
        rangeCheck(index, separatorSize);
        return this.nonTerminalNode.childInBucket(index * 2 + 1);
    }
}
