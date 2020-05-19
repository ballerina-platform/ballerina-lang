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

import io.ballerinalang.compiler.internal.parser.tree.STMinutiae;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The {@code MinutiaeList} represents a immutable list of {@code Minutiae} values.
 *
 * @since 2.0.0
 */
public final class MinutiaeList implements Iterable<Minutiae> {
    private static final MinutiaeList EMPTY_LIST = new MinutiaeList(null, STNodeList.emptyList(), -1);

    private final Token token;
    private final STNodeList internalNode;
    private final int position;
    private final int size;
    private final Minutiae[] minutiaeNodes;

    MinutiaeList(Token token, STNode internalMinutiae, int position) {
        if (!SyntaxUtils.isSTNodeList(internalMinutiae)) {
            throw new IllegalStateException("An STNodeList instance expected here");
        }
        this.internalNode = (STNodeList) internalMinutiae;
        this.token = token;
        this.position = position;
        this.size = getMinutiaeCount(internalMinutiae);

        // The following method eagerly create all the minutiae nodes at the moment
        // Do we need to create them on-demand?
        // TODO Update the implementation with an lazy loading algorithm.
        this.minutiaeNodes = loadMinutiaeNodes(internalMinutiae, size, position);
    }

    public static MinutiaeList emptyList() {
        return EMPTY_LIST;
    }

    // Positional access methods

    public Minutiae get(int index) {
        rangeCheck(index);
        return minutiaeNodes[index];
    }

    // Modification Operations

    public MinutiaeList add(Minutiae minutiae) {
        return new MinutiaeList(token, internalNode.add(minutiae.internalNode()), position);
    }

    public MinutiaeList add(int index, Minutiae minutiae) {
        rangeCheck(index);
        return new MinutiaeList(token, internalNode.add(index, minutiae.internalNode()), position);
    }

    public MinutiaeList addAll(Collection<Minutiae> c) {
        return new MinutiaeList(token,
                internalNode.addAll(c.stream()
                        .map(Minutiae::internalNode)
                        .collect(Collectors.toList())),
                position);
    }

    public MinutiaeList set(int index, Minutiae minutiae) {
        rangeCheck(index);
        return new MinutiaeList(token, internalNode.set(index, minutiae.internalNode()), position);
    }

    public MinutiaeList remove(int index) {
        rangeCheck(index);
        return new MinutiaeList(token, internalNode.remove(index), position);
    }

    public MinutiaeList remove(Minutiae minutiae) {
        Objects.requireNonNull(minutiae, "minutiae cannot be null");
        for (int i = 0; i < minutiaeNodes.length; i++) {
            if (minutiae.equals(minutiaeNodes[i])) {
                return remove(i);
            }
        }
        return this;
    }

    public MinutiaeList removeAll(Collection<Minutiae> c) {
        List<STNode> internalNodeList = new ArrayList<>();
        for (Minutiae minutiae : minutiaeNodes) {
            if (c.contains(minutiae)) {
                internalNodeList.add(minutiae.internalNode());
            }
        }

        return new MinutiaeList(token, internalNode.removeAll(internalNodeList), position);
    }

    //query methods

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Minutiae> iterator() {
        return new Iterator<Minutiae>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Minutiae next() {
                return get(currentIndex++);
            }
        };
    }

    protected STNode internalNode() {
        return internalNode;
    }

    private Minutiae[] loadMinutiaeNodes(STNode internalMinutiae, int size, int position) {
        if (size == 0) {
            return new Minutiae[0];
        }

        int index = 0;
        int minutiaeStartPos = position;
        Minutiae[] minutiaeNodes = new Minutiae[size];
        for (int bucket = 0; bucket < internalMinutiae.bucketCount(); bucket++) {
            STNode node = internalMinutiae.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(node)) {
                continue;
            }
            minutiaeNodes[index] = createMinutiae(node, minutiaeStartPos);
            index++;
            minutiaeStartPos += node.widthWithMinutiae();
        }
        return minutiaeNodes;
    }

    private Minutiae createMinutiae(STNode internalMinutiae, int position) {
        return new Minutiae((STMinutiae) internalMinutiae, token, position);
    }

    private int getMinutiaeCount(STNode internalMinutiae) {
        for (int bucket = 0; bucket < internalMinutiae.bucketCount(); bucket++) {
            STNode child = internalMinutiae.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(child)) {
                throw new IllegalStateException("No minutia nodes with 'null' values are allowed in MinutiaeList");
            }
        }
        return internalMinutiae.bucketCount();
    }

    private void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException("Index: '" + index + "', Size: '" + size + "'");
    }

    @Override
    public String toString() {
        return internalNode.toString();
    }
}
