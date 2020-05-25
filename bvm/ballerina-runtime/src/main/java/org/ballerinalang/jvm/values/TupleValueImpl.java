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
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import static org.ballerinalang.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * <p>
 * Represent an array in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 0.995.0
 */
public class TupleValueImpl extends AbstractArrayValue {

    protected BTupleType tupleType;
    Object[] refValues;
    private int minSize = 0;
    private boolean hasRestElement; // cached value for ease of access
    // ------------------------ Constructors -------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TupleValueImpl that = (TupleValueImpl) o;
        return minSize == that.minSize &&
                hasRestElement == that.hasRestElement &&
                tupleType.equals(that.tupleType) &&
                Arrays.equals(refValues, that.refValues);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tupleType, minSize, hasRestElement);
        result = 31 * result + Arrays.hashCode(refValues);
        return result;
    }

    @Deprecated
    public TupleValueImpl(Object[] values, BTupleType type) {
        this.refValues = values;
        this.tupleType = type;
        this.hasRestElement = this.tupleType.getRestType() != null;

        List<BType> memTypes = type.getTupleTypes();
        int memCount = memTypes.size();

        if (values.length < memCount) {
            this.refValues = Arrays.copyOf(refValues, memCount);
            for (int i = values.length; i < memCount; i++) {
                refValues[i] = memTypes.get(i).getZeroValue();
            }
        }
        this.minSize = memTypes.size();
        this.size = refValues.length;
    }

    @Deprecated
    public TupleValueImpl(BTupleType type) {
        this.tupleType = type;

        List<BType> memTypes = this.tupleType.getTupleTypes();
        int memTypeCount = memTypes.size();

        this.minSize = this.size = memTypeCount;
        this.hasRestElement = this.tupleType.getRestType() != null;

        if (type.getRestType() == null) {
            this.maxSize = this.size;
            this.refValues = new Object[this.size];
        } else {
            this.refValues = new Object[DEFAULT_ARRAY_SIZE];
        }

        for (int i = 0; i < memTypeCount; i++) {
            BType memType = memTypes.get(i);
            if (!TypeChecker.hasFillerValue(memType)) {
                continue;
            }
            this.refValues[i] = memType.getZeroValue();
        }
    }

    @Deprecated
    public TupleValueImpl(BTupleType type, long size, ListInitialValueEntry[] initialValues) {
        this.tupleType = type;

        List<BType> memTypes = this.tupleType.getTupleTypes();
        int memCount = memTypes.size();

        this.size = size < memCount ? memCount : (int) size;
        this.minSize = memCount;
        this.hasRestElement = this.tupleType.getRestType() != null;

        if (type.getRestType() == null) {
            this.maxSize = this.size;
            this.refValues = new Object[this.size];
        } else {
            this.refValues = new Object[DEFAULT_ARRAY_SIZE];
        }

        for (int index = 0; index < initialValues.length; index++) {
            addRefValue(index, ((ListInitialValueEntry.ExpressionEntry) initialValues[index]).value);
        }

        if (size >= memCount) {
            return;
        }

        for (int i = (int) size; i < memCount; i++) {
            BType memType = memTypes.get(i);
            if (!TypeChecker.hasFillerValue(memType)) {
                continue;
            }

            this.refValues[i] = memType.getZeroValue();
        }
    }

    // ----------------------- get methods ----------------------------------------------------

    /**
     * Get value in the given array index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public Object get(long index) {
        rangeCheckForGet(index, this.size);
        return this.refValues[(int) index];
    }

    /**
     * Get ref value in the given index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public Object getRefValue(long index) {
        return get(index);
    }

    @Override
    public Object fillAndGetRefValue(long index) {
        // Need do a filling-read if index >= size
        if (index >= this.size && this.hasRestElement) {
            handleImmutableArrayValue();
            fillRead(index, refValues.length);
            return this.refValues[(int) index];
        }
        return get(index);
    }

    /**
     * Get int value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public long getInt(long index) {
        return (Long) get(index);
    }

    /**
     * Get boolean value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    public boolean getBoolean(long index) {
        return (Boolean) get(index);
    }

    /**
     * Get byte value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public byte getByte(long index) {
        return (Byte) get(index);
    }

    /**
     * Get float value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public double getFloat(long index) {
        return (Double) get(index);
    }

    /**
     * Get string value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    @Deprecated
    public String getString(long index) {
        return get(index).toString();
    }

    /**
     * Get string value in the given index.
     *
     * @param index array index
     * @return array element
     */
    @Override
    public BString getBString(long index) {
        return (BString) get(index);
    }

    // ---------------------------- add methods --------------------------------------------------

    /**
     * Add ref value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, Object value) {
        handleImmutableArrayValue();
        addRefValue(index, value);
    }

    private void addRefValue(long index, Object value) {
        prepareForAdd(index, value, refValues.length);
        refValues[(int) index] = value;
    }

    /**
     * Add int value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, long value) {
        add(index, Long.valueOf(value));
    }

    /**
     * Add boolean value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, boolean value) {
        add(index, Boolean.valueOf(value));
    }

    /**
     * Add byte value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, byte value) {
        add(index, Byte.valueOf(value));
    }

    /**
     * Add double value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, double value) {
        add(index, Double.valueOf(value));
    }

    /**
     * Add string value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    @Deprecated
    public void add(long index, String value) {
        add(index, (Object) value);
    }

    /**
     * Add string value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, BString value) {
        add(index, (Object) value);
    }

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

    @Override
    public Object shift(long index) {
        handleImmutableArrayValue();
        Object val = get(index);
        shiftArray((int) index);
        return val;
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
    public void unshift(BArray values) {
        unshift(0, (TupleValueImpl) values);
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(" ");
        for (int i = 0; i < this.size; i++) {
            sj.add(StringUtils.getStringValue(this.refValues[i]));
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return this.tupleType;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        Object[] values = new Object[this.size];
        TupleValueImpl refValueArray = new TupleValueImpl(values, this.tupleType);
        refs.put(this, refValueArray);
        IntStream.range(0, this.size).forEach(i -> {
            Object value = this.refValues[i];
            if (value instanceof RefValue) {
                values[i] = ((RefValue) value).copy(refs);
            } else {
                values[i] = value;
            }
        });

        return refValueArray;
    }

    /**
     * Get ref values array.
     * 
     * @return ref value array
     */
    @Override
    public Object[] getValues() {
        return refValues;
    }

    /**
     * Get a copy of byte array.
     * 
     * @return byte array
     */
    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a copy of string array.
     * 
     * @return string array
     */
    @Override
    public String[] getStringArray() {
        throw new UnsupportedOperationException();
    }

    public long[] getLongArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a copy of int array.
     * 
     * @return int array
     */
    @Override
    public long[] getIntArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            outputStream.write(this.toString().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("error occurred while serializing data", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (tupleType.isReadOnly()) {
            return;
        }

        this.tupleType = (BTupleType) ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.tupleType);
        for (int i = 0; i < this.size; i++) {
            Object value = this.get(i);
            if (value instanceof RefValue) {
                ((RefValue) value).freezeDirect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

    /**
     * Get {@code BType} of the array elements.
     * 
     * @return element type
     */
    @Override
    public BType getElementType() {
        throw new UnsupportedOperationException();
    }

    // Protected methods

    @Override
    protected void resizeInternalArray(int newLength) {
        refValues = Arrays.copyOf(refValues, newLength);
    }

    @Override
    protected void fillValues(int index) {
        if (index <= size) {
            return;
        }

        BType restType = this.tupleType.getRestType();
        if (restType != null) {
            for (int i = size; i < index; i++) {
                this.refValues[i] = restType.getZeroValue();
            }
        }
    }

    @Override
    protected void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index < 0 || index >= size) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if ((this.tupleType.getRestType() == null && index >= this.maxSize) || (int) index < 0) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void fillerValueCheck(int index, int size) {
        // if there has been values added beyond the current index, that means filler values
        // has already been checked. Therefore no need to check again.
        if (this.size >= index) {
            return;
        }

        // if the elementType doesn't have an implicit initial value & if the insertion is not a consecutive append
        // to the array, then an exception will be thrown.
        if (!TypeChecker.hasFillerValue(this.tupleType.getRestType()) && (index > size)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                    RuntimeErrors.ILLEGAL_TUPLE_INSERTION, size, index + 1);
        }
    }

    /**
     * Same as {@code prepareForAdd}, except fillerValueCheck is not performed as we are guaranteed to add
     * elements to consecutive positions.
     *
     * @param index last index after add operation completes
     * @param currentArraySize current array size
     */
    @Override
    protected void prepareForConsecutiveMultiAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    @Override
    protected void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if (requestedCapacity <= currentArraySize) {
            return;
        }

        // Here the growth rate is 1.5. This value has been used by many other languages
        int newArraySize = currentArraySize + (currentArraySize >> 1);

        // Now get the maximum value of the calculate new array size and request capacity
        newArraySize = Math.max(newArraySize, requestedCapacity);

        // Now get the minimum value of new array size and maximum array size
        newArraySize = Math.min(newArraySize, this.maxSize);
        resizeInternalArray(newArraySize);
    }

    @Override
    protected void checkFixedLength(long length) {
        if (this.tupleType.getRestType() == null) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_TUPLE_SIZE, size, length);
        } else if (this.tupleType.getTupleTypes().size() > length) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_TUPLE_WITH_REST_TYPE_SIZE, this.tupleType.getTupleTypes().size(), length);
        }
    }

    @Override
    protected void unshift(long index, ArrayValue vals) {
        handleImmutableArrayValue();
        unshiftArray(index, vals.size(), getCurrentArrayLength());
        addToRefArray(vals, (int) index);
    }

    // private methods

    private void prepareForAdd(long index, Object value, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);

        // check types
        BType elemType;
        if (index >= this.minSize) {
            elemType = this.tupleType.getRestType();
        } else {
            elemType = this.tupleType.getTupleTypes().get((int) index);
        }

        if (!TypeChecker.checkIsType(value, elemType)) {
            throw BallerinaErrors.createError(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_TYPE, elemType,
                                                         TypeChecker.getType(value)));
        }

        fillerValueCheck(intIndex, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        fillValues(intIndex);
        resetSize(intIndex);
    }

    private void fillRead(long index, int currentArraySize) {
        BType restType = this.tupleType.getRestType();
        if (!TypeChecker.hasFillerValue(restType)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                                                           RuntimeErrors.ILLEGAL_TUPLE_INSERTION, size, index + 1);
        }

        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);

        for (int i = size; i <= index; i++) {
            this.refValues[i] = restType.getZeroValue();
        }

        resetSize(intIndex);
    }

    private void shiftArray(int index) {
        int nElemsToBeMoved = this.size - 1 - index;
        if (nElemsToBeMoved >= 0) {
            System.arraycopy(this.refValues, index + 1, this.refValues, index, nElemsToBeMoved);
        }
        this.size--;
    }

    private void addToRefArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size();
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getRefValue(j));
        }
    }

    private void unshiftArray(long index, int unshiftByN, int arrLength) {
        int lastIndex = size() + unshiftByN - 1;
        prepareForConsecutiveMultiAdd(lastIndex, arrLength);
        if (index > lastIndex) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        int i = (int) index;
        System.arraycopy(this.refValues, i, this.refValues, i + unshiftByN, this.size - i);
    }

    private int getCurrentArrayLength() {
        return this.refValues.length;
    }

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }
}
