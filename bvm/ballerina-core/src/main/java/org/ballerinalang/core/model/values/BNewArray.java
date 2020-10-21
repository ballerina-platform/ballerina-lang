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
package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.core.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.core.util.exceptions.RuntimeErrors;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.lang.reflect.Array;

/**
 * {@code BArray} represents an arrays in Ballerina.
 *
 * @since 0.87
 */
// TODO Change this class name
public abstract class BNewArray implements BRefType, BCollection {

    protected BType arrayType;
    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    protected int maxArraySize = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_ARRAY_SIZE = 100;
    protected int size = 0;

    public abstract void grow(int newLength);

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public BRefType<?> value() {
        return null;
    }

    // Private methods

    protected Object newArrayInstance(Class<?> componentType) {
        return (size > 0) ?
                Array.newInstance(componentType, size) : Array.newInstance(componentType, DEFAULT_ARRAY_SIZE);
    }

    protected void prepareForAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    protected void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    protected void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }
        if ((int) index < 0 || index >= maxArraySize) {
            if (this.arrayType != null && this.arrayType.getTag() == TypeTags.TUPLE_TAG) {
                throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                        RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
            }
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    protected void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index < 0 || index >= size) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    protected void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if ((requestedCapacity - currentArraySize) >= 0 && this.arrayType.getTag() == TypeTags.ARRAY_TAG &&
                ((BArrayType) this.arrayType).getState() == BArrayState.OPEN) {
            // Here the growth rate is 1.5. This value has been used by many other languages
            int newArraySize = currentArraySize + (currentArraySize >> 1);

            // Now get the maximum value of the calculate new array size and request capacity
            newArraySize = Math.max(newArraySize, requestedCapacity);

            // Now get the minimum value of new array size and maximum array size
            newArraySize = Math.min(newArraySize, maxArraySize);
            grow(newArraySize);
        }
    }

    public long size() {
        return size;
    }

    public abstract BValue getBValue(long index);

    @Override
    public BIterator newIterator() {
        return new BArrayIterator(this);
    }

    /**
     * {@code {@link BArrayIterator}} provides iterator implementation for Ballerina array values.
     *
     * @since 0.96.0
     */
    static class BArrayIterator implements BIterator {
        BNewArray array;
        long cursor = 0;
        long length;

        BArrayIterator(BNewArray value) {
            this.array = value;
            this.length = value.size();
        }

        @Override
        public BValue getNext() {
            long cursor = this.cursor++;
            if (cursor == length) {
                return null;
            }
            return array.getBValue(cursor);
        }

        @Override
        public boolean hasNext() {
            return cursor < length;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return true;
    }
}
