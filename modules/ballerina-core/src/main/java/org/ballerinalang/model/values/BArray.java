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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

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
    
    private BType type;
    
    // zeroValue is the value to be returned if the value in this array is null.
    private BValue zeroValue;

    public BArray(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    public <V extends BValue> void add(long index, V value) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }
        int indexVal = (int) index;
        if (indexVal < 0) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
        ensureCapacity(indexVal);

        int bucketIndex = indexVal / DEFAULT_ARRAY_SIZE;
        int slot = indexVal % DEFAULT_ARRAY_SIZE;
        arrayBucket[bucketIndex][slot] = value;

        if (index >= size) {
            size = indexVal + 1;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(long index) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }
        int indexVal = (int) index;
        rangeCheck(indexVal);

        int bucketIndex = indexVal / DEFAULT_ARRAY_SIZE;
        int slot = indexVal % DEFAULT_ARRAY_SIZE;

        BValue value = arrayBucket[bucketIndex][slot];
        
        // When an array is initialized, all values are set to null as the default value, irrespective of the type
        // of the array. But, since the default value for value-types(int/string/float/boolean) cannot be null, here
        // we return the zero-value of the type associated with this array.
        if (value == null) {
            return (V) zeroValue;
        } 
        
        return (V) value;
    }

    public int size() {
        return size;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return type;
    }
    
    public void setType(BType type) {
        this.type = type;
        
        if (type instanceof BArrayType) {
            this.zeroValue = ((BArrayType) type).getElementType().getZeroValue();
        } else {
            this.zeroValue = type.getZeroValue();
        }
    }

    @Override
    public V value() {
        return null;
    }

    @Override
    public BValue copy() {
        BArray array = new BArray<>(this.valueClass);
        for (int i = 0; i < size; i++) {
            BValue value = this.get(i);
            array.add(i, value == null ? null : value.copy());
        }
        return array;
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
        if (index < 0 || index >= size) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    private void ensureCapacity(int capacityRequired) {
        int bucketIndex = capacityRequired / DEFAULT_ARRAY_SIZE;

        if (bucketIndex - lastBucketIndex > 0) {
            grow(capacityRequired, bucketIndex);
        }
    }

    private void grow(int capacityRequired, int bucketIndex) {
        if (capacityRequired > MAX_ARRAY_SIZE) {
            throw new BallerinaException("Requested array size " + capacityRequired +
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
