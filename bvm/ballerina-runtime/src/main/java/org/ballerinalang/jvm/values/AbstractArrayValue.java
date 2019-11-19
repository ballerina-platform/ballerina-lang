/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.ballerinalang.jvm.util.BLangConstants.ARRAY_LANG_LIB;

/**
 * <p>
 * Abstract implementation of an array value in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 1.1.0
 */
public abstract class AbstractArrayValue implements ArrayValue {

    static final int SYSTEM_ARRAY_MAX = Integer.MAX_VALUE - 8;
    protected volatile Status freezeStatus = new Status(State.UNFROZEN);

    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    protected int maxSize = SYSTEM_ARRAY_MAX;
    protected static final int DEFAULT_ARRAY_SIZE = 100;
    protected int size = 0;

    // ----------------------- get methods ----------------------------------------------------

    /**
     * Get value in the given array index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public abstract Object get(long index);

    /**
     * Get ref value in the given index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public abstract Object getRefValue(long index);

    /**
     * Get int value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public abstract long getInt(long index);

    /**
     * Get boolean value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public abstract boolean getBoolean(long index);

    /**
     * Get byte value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public abstract byte getByte(long index);

    /**
     * Get float value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public abstract double getFloat(long index);

    /**
     * Get string value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public abstract String getString(long index);

    // ---------------------------- add methods --------------------------------------------------

    /**
     * Add ref value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, Object value);

    /**
     * Add int value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, long value);

    /**
     * Add boolean value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, boolean value);

    /**
     * Add byte value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, byte value);

    /**
     * Add double value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, double value);

    /**
     * Add string value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public abstract void add(long index, String value);

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Append value to the existing array.
     * 
     * @param value value to be appended
     */
    @Override
    public void append(Object value) {
        add(size, value);
    }

    /**
     * Removes and returns first member of an array.
     * 
     * @return the value that was the first member of the array
     */
    @Override
    public Object shift() {
        return shift(0);
    }

    @Override
    public abstract Object shift(long index);

    /**
     * Adds values to the start of an array.
     * 
     * @param values values to add to the start of the array
     */
    public void unshift(ArrayValue values) {
        unshift(0, values);
    }

    @Override
    public void unshift(BArray values) {
        unshift(0, (ArrayValue) values);
    }

    @Override
    public String stringValue() {
        return stringValue(null);
    }

    @Override
    public abstract String stringValue(Strand strand);

    @Override
    public abstract BType getType();

    @Override
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public abstract Object copy(Map<Object, Object> refs);

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        ArrayValue copy = (ArrayValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    @Override
    public String toString() {
        return stringValue();
    }

    @Override
    public abstract void serialize(OutputStream outputStream);

    @Override
    public String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JSONGenerator gen = new JSONGenerator(byteOut);
        try {
            gen.serialize(this);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error in converting JSON to a string: " + e.getMessage(), e);
        }
        return new String(byteOut.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void attemptFreeze(Status freezeStatus);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void freezeDirect();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

    @Override
    public void setLength(long length) {
        if (length == size) {
            return;
        }
        handleFrozenArrayValue();
        int newLength = (int) length;
        checkFixedLength(length);
        rangeCheck(length, size);
        fillerValueCheck(newLength, size);
        resizeInternalArray(newLength);
        fillValues(newLength);
        size = newLength;
    }

    /*
     * helper methods that are visible to the implementation classes.
     */

    protected abstract void fillValues(int newLength);

    protected abstract void fillerValueCheck(int newLength, int size2);

    protected abstract void resizeInternalArray(int newLength);

    protected abstract void rangeCheckForGet(long index, int size);

    protected abstract void rangeCheck(long index, int size);

    /**
     * Util method to handle frozen array values.
     */
    protected void handleFrozenArrayValue() {
        synchronized (this) {
            try {
                if (this.freezeStatus.getState() != State.UNFROZEN) {
                    FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), ARRAY_LANG_LIB);
                }
            } catch (BLangFreezeException e) {
                throw BallerinaErrors.createError(e.getMessage(), e.getDetail());
            }
        }
    }

    protected abstract void prepareForAdd(long index, Object value, int currentArraySize);

    /**
     * Same as {@code prepareForAdd}, except fillerValueCheck is not performed as we are guaranteed to add
     * elements to consecutive positions.
     *
     * @param index last index after add operation completes
     * @param currentArraySize current array size
     */
    protected void prepareForConsecutiveMultiAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    protected abstract void ensureCapacity(int requestedCapacity, int currentArraySize);

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    protected abstract void unshift(long index, ArrayValue vals);

    protected abstract void checkFixedLength(long length);

    /**
     * {@code {@link ArrayIterator}} provides iterator implementation for Ballerina array values.
     *
     * @since 0.995.0
     */
    static class ArrayIterator implements IteratorValue {
        ArrayValue array;
        long cursor = 0;
        long length;

        ArrayIterator(ArrayValue value) {
            this.array = value;
            this.length = value.size();
        }

        @Override
        public Object next() {
            long cursor = this.cursor++;
            if (cursor == length) {
                return null;
            }
            return array.get(cursor);
        }

        @Override
        public boolean hasNext() {
            return cursor < length;
        }
    }
}
