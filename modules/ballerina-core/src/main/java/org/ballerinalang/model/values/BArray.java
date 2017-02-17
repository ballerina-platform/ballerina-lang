/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.values;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * {@code BArray} represents an arrays in Ballerina.
 *
 * @param <V> Ballerina value stored in this arrays value
 * @since 0.8.0
 */
public final class BArray<V extends BValue> implements BRefType {

    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_ARRAY_SIZE = 100;
    private static final int DEFAULT_ARRAY_BUCKET_SIZE = 10;

    private BValue[][] arrayBucket = new BValue[DEFAULT_ARRAY_BUCKET_SIZE][];
    private Class<V> valueClass;

    private int lastBucketIndex = -1;
    private int size = 0;

    public BArray(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    public <V extends BValue> void add(int index, V value) {
        ensureCapacity(index);

        int bucketIndex = index / DEFAULT_ARRAY_SIZE;
        int slot = index % DEFAULT_ARRAY_SIZE;
        arrayBucket[bucketIndex][slot] = value;

        if (index >= size) {
            size = index + 1;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(int index) {
        rangeCheck(index);

        int bucketIndex = index / DEFAULT_ARRAY_SIZE;
        int slot = index % DEFAULT_ARRAY_SIZE;

        return (V) arrayBucket[bucketIndex][slot];
    }

    public int size() {
        return size;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public V value() {
        return null;
    }


    // Private methods

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.
     */

    @SuppressWarnings("unchecked")
    private <V extends BValue> V[] createArray() {
        return (V[]) Array.newInstance(this.valueClass, DEFAULT_ARRAY_SIZE);
    }

    private void rangeCheck(int index) {
        if (index >= size) {
            throw new BallerinaException("arrays index out of range: " + outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void ensureCapacity(int capacityRequired) {
        int bucketIndex = capacityRequired / DEFAULT_ARRAY_SIZE;

        if (bucketIndex - lastBucketIndex > 0) {
            grow(capacityRequired, bucketIndex);
        }
    }

    private void grow(int capacityRequired, int bucketIndex) {
        if (capacityRequired > MAX_ARRAY_SIZE) {
            throw new BallerinaException("Requested arrays size " + capacityRequired +
                    " exceeds limit: " + MAX_ARRAY_SIZE);
        }

        if (bucketIndex >= arrayBucket.length) {
            // We have to create new arrayBucket
            arrayBucket = Arrays.copyOf(arrayBucket, arrayBucket.length + DEFAULT_ARRAY_BUCKET_SIZE);

        }

        if (bucketIndex > lastBucketIndex) {
            for (int i = lastBucketIndex + 1; bucketIndex > lastBucketIndex; i++) {
                arrayBucket[i] = createArray();
                lastBucketIndex++;
            }
        }
    }
}
