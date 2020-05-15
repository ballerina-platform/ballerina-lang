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
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;

import java.util.Iterator;

/**
 * The {@code MinutiaeList} represents a immutable list of {@code Minutiae} values.
 *
 * @since 2.0.0
 */
public class MinutiaeList implements Iterable<Minutiae> {
    private final Token token;
    private final STNode internalMinutia;
    private final int size;
    private final Minutiae[] minutiaeNodes;

    MinutiaeList(Token token, STNode internalMinutiae) {
        this.token = token;
        this.internalMinutia = internalMinutiae;
        this.size = getMinutiaeCount(internalMinutiae);
        this.minutiaeNodes = loadMinutiaeNodes(internalMinutiae, size);
    }

    public Minutiae get(int index) {
        rangeCheck(index);
        return minutiaeNodes[index];
    }

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

    private Minutiae[] loadMinutiaeNodes(STNode internalMinutiae, int size) {
        if (size == 0) {
            return new Minutiae[0];
        }

        if (!SyntaxUtils.isSTNodeList(internalMinutiae)) {
            return new Minutiae[]{createMinutiae(internalMinutiae)};
        }

        int index = 0;
        Minutiae[] minutiaeNodes = new Minutiae[size];
        for (int bucket = 0; bucket < internalMinutiae.bucketCount(); bucket++) {
            STNode node = internalMinutiae.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(node)) {
                continue;
            }
            minutiaeNodes[index] = createMinutiae(node);
            index++;
        }
        return minutiaeNodes;
    }

    private Minutiae createMinutiae(STNode internalMinutiae) {
        return new Minutiae((STMinutiae) internalMinutiae, token, 0);
    }

    private int getMinutiaeCount(STNode internalMinutiae) {
        int count = 0;
        for (int bucket = 0; bucket < internalMinutiae.bucketCount(); bucket++) {
            STNode child = internalMinutiae.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(child)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private void rangeCheck(int childIndex) {
        if (childIndex >= size || childIndex < 0)
            throw new IndexOutOfBoundsException("Index: '" + childIndex + "', Size: '" + size + "'");
    }

    @Override
    public String toString() {
        return internalMinutia.toString();
    }
}
