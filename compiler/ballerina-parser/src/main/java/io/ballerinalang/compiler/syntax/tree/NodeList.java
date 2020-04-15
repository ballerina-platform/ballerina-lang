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

import java.util.Iterator;

/**
 * Represents a list of {@code Node}s.
 *
 * @param <T> the type of the node instance
 */
public class NodeList<T extends Node> implements Iterable<T> {

    private final NonTerminalNode node;
    private final int size;

    public NodeList(NonTerminalNode node) {
        this.node = node;
        this.size = node.bucketCount();
    }

    public T get(int index) {
        return this.node.childInBucket(index);
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    NonTerminalNode underlyingListNode() {
        return this.node;
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeListIterator();
    }

    /**
     * An iterator for this list of nodes.
     *
     * @since 1.3.0
     */
    private class NodeListIterator implements Iterator<T> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return this.currentIndex < size;
        }

        @Override
        public T next() {
            return get(currentIndex++);
        }
    }
}
