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
import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.BLangConstants;
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
public class ArrayValueImpl extends AbstractArrayValue {

    protected BArrayType arrayType;
    protected BType elementType;

    protected Object[] refValues;
    private long[] intValues;
    private boolean[] booleanValues;
    private byte[] byteValues;
    private double[] floatValues;
    private BString[] bStringValues;
    // ------------------------ Constructors -------------------------------------------------------------------

    @Deprecated
    public ArrayValueImpl(Object[] values, BArrayType type) {
        this.refValues = values;
        this.arrayType = type;
        this.size = values.length;
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            this.elementType = type.getElementType();
        }
    }

    @Deprecated
    public ArrayValueImpl(long[] values) {
        this.intValues = values;
        this.size = values.length;
        setArrayType(BTypes.typeInt);
    }

    @Deprecated
    public ArrayValueImpl(boolean[] values) {
        this.booleanValues = values;
        this.size = values.length;
        setArrayType(BTypes.typeBoolean);
    }

    @Deprecated
    public ArrayValueImpl(byte[] values) {
        this.byteValues = values;
        this.size = values.length;
        setArrayType(BTypes.typeByte);
    }

    @Deprecated
    public ArrayValueImpl(double[] values) {
        this.floatValues = values;
        this.size = values.length;
        setArrayType(BTypes.typeFloat);
    }

    @Deprecated
    public ArrayValueImpl(String[] values) {
        this.size = values.length;
        bStringValues = new BString[size];
        for (int i = 0; i < size; i++) {
            bStringValues[i] = org.ballerinalang.jvm.StringUtils.fromString(values[i]);
        }
        setArrayType(BTypes.typeString);
    }

    @Deprecated
    public ArrayValueImpl(BString[] values) {
        this.bStringValues = values;
        this.size = values.length;
        setArrayType(BTypes.typeString);
    }

    @Deprecated
    public ArrayValueImpl(BArrayType type) {
        this.arrayType = type;
        BArrayType arrayType = type;
        this.elementType = arrayType.getElementType();
        initArrayValues(elementType);
        if (arrayType.getState() == ArrayState.CLOSED_SEALED) {
            this.size = maxSize = arrayType.getSize();
        }
    }

    private void initArrayValues(BType elementType) {
        int initialArraySize = (arrayType.getSize() != -1) ? arrayType.getSize() : DEFAULT_ARRAY_SIZE;
        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                this.intValues = new long[initialArraySize];
                break;
            case TypeTags.FLOAT_TAG:
                this.floatValues = new double[initialArraySize];
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                this.bStringValues = new BString[initialArraySize];
                break;
            case TypeTags.BOOLEAN_TAG:
                this.booleanValues = new boolean[initialArraySize];
                break;
            case TypeTags.BYTE_TAG:
                this.byteValues = new byte[initialArraySize];
                break;
            default:
                this.refValues = new Object[initialArraySize];
                if (arrayType.getState() == ArrayState.CLOSED_SEALED) {
                    fillerValueCheck(initialArraySize, initialArraySize);
                    fillValues(initialArraySize);
                }
        }
    }

    @Deprecated
    public ArrayValueImpl(BArrayType type, long size) {
        this.arrayType = type;
        this.elementType = type.getElementType();
        initArrayValues(this.elementType);
        if (size != -1) {
            this.size = this.maxSize = (int) size;
        }
    }

    @Deprecated
    public ArrayValueImpl(BArrayType type, long size, ListInitialValueEntry[] initialValues) {
        this.arrayType = type;
        this.elementType = type.getElementType();
        initArrayValues(this.elementType);
        if (size != -1) {
            this.size = this.maxSize = (int) size;
        }

        for (int index = 0; index < initialValues.length; index++) {
            addRefValue(index, ((ListInitialValueEntry.ExpressionEntry) initialValues[index]).value);
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
        rangeCheckForGet(index, size);
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return intValues[(int) index];
            case TypeTags.BOOLEAN_TAG:
                return booleanValues[(int) index];
            case TypeTags.BYTE_TAG:
                return Byte.toUnsignedInt(byteValues[(int) index]);
            case TypeTags.FLOAT_TAG:
                return floatValues[(int) index];
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                    return bStringValues[(int) index];
            default:
                return refValues[(int) index];
        }
    }

    /**
     * Get ref value in the given index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public Object getRefValue(long index) {
        rangeCheckForGet(index, size);
        if (refValues != null) {
            return refValues[(int) index];
        }
        return get(index);
    }

    @Override
    public Object fillAndGetRefValue(long index) {
        if (refValues != null) {
            // Need do a filling-read if index >= size
            if (index >= this.size) {
                handleImmutableArrayValue();
                fillRead(index, refValues.length);
            }
            return refValues[(int) index];
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
        rangeCheckForGet(index, size);
        if (intValues != null) {
            return intValues[(int) index];
        } else if (refValues != null) {
            return (Long) refValues[(int) index];
        }
        return Byte.toUnsignedInt(byteValues[(int) index]);
    }

    /**
     * Get boolean value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public boolean getBoolean(long index) {
        rangeCheckForGet(index, size);
        if (booleanValues != null) {
            return booleanValues[(int) index];
        }
        return (Boolean) refValues[(int) index];
    }

    /**
     * Get byte value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public byte getByte(long index) {
        rangeCheckForGet(index, size);
        if (byteValues != null) {
            return byteValues[(int) index];
        }
        return ((Integer) refValues[(int) index]).byteValue();
    }

    /**
     * Get float value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public double getFloat(long index) {
        rangeCheckForGet(index, size);
        if (floatValues != null) {
            return floatValues[(int) index];
        }
        return (Double) refValues[(int) index];
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
        rangeCheckForGet(index, size);
        if (bStringValues != null) {
            return bStringValues[(int) index].getValue();
        }
        return (String) refValues[(int) index];
    }

    /**
     * Get string value in the given index.
     *
     * @param index array index
     * @return array element
     */
    @Override
    public BString getBString(long index) {
        rangeCheckForGet(index, size);
        if (bStringValues != null) {
            return bStringValues[(int) index];
        }
        return (BString) refValues[(int) index];
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

    /**
     * Add int value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, long value) {
        handleImmutableArrayValue();
        addInt(index, value);
    }

    /**
     * Add boolean value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, boolean value) {
        handleImmutableArrayValue();
        addBoolean(index, value);
    }

    /**
     * Add byte value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, byte value) {
        handleImmutableArrayValue();
        addByte(index, value);
    }

    /**
     * Add double value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, double value) {
        handleImmutableArrayValue();
        addFloat(index, value);
    }

    /**
     * Add string value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Deprecated
    @Override
    public void add(long index, String value) {
        handleImmutableArrayValue();
        addString(index, value);
    }

    /**
     * Add string value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, BString value) {
        handleImmutableArrayValue();
        addBString(index, value);
    }

    public void addRefValue(long index, Object value) {
        BType type = TypeChecker.getType(value);
        switch (this.elementType.getTag()) {
            case TypeTags.BOOLEAN_TAG:
                prepareForAdd(index, value, type, booleanValues.length);
                this.booleanValues[(int) index] = (Boolean) value;
                return;
            case TypeTags.FLOAT_TAG:
                prepareForAdd(index, value, type, floatValues.length);
                this.floatValues[(int) index] = (Double) value;
                return;
            case TypeTags.BYTE_TAG:
                prepareForAdd(index, value, type, byteValues.length);
                this.byteValues[(int) index] = ((Number) value).byteValue();
                return;
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                prepareForAdd(index, value, type, intValues.length);
                this.intValues[(int) index] = (Long) value;
                return;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                prepareForAdd(index, value, type, bStringValues.length);
                this.bStringValues[(int) index] = (BString) value;
                return;
            default:
                prepareForAdd(index, value, type, refValues.length);
                this.refValues[(int) index] = value;
        }
    }

    public void addInt(long index, long value) {
        if (intValues != null) {
            prepareForAdd(index, value, BTypes.typeInt, intValues.length);
            intValues[(int) index] = value;
            return;
        }

        prepareForAdd(index, value, TypeChecker.getType(value), byteValues.length);
        byteValues[(int) index] = (byte) ((Long) value).intValue();
    }

    private void addBoolean(long index, boolean value) {
        prepareForAdd(index, value, BTypes.typeBoolean, booleanValues.length);
        booleanValues[(int) index] = value;
    }

    private void addByte(long index, byte value) {
        prepareForAdd(index, value, BTypes.typeByte, byteValues.length);
        byteValues[(int) index] = value;
    }

    private void addFloat(long index, double value) {
        prepareForAdd(index, value, BTypes.typeFloat, floatValues.length);
        floatValues[(int) index] = value;
    }

    @Deprecated
    private void addString(long index, String value) {
        addBString(index, org.ballerinalang.jvm.StringUtils.fromString(value));
    }

    private void addBString(long index, BString value) {
        prepareForAdd(index, value, BTypes.typeString, bStringValues.length);
        bStringValues[(int) index] = value;
    }

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Append value to the existing array.
     * 
     * @param value value to be appended
     */
    @Override
    public void append(Object value) {
        add(this.size, value);
    }

    @Override
    public Object shift(long index) {
        handleImmutableArrayValue();
        Object val = get(index);
        shiftArray((int) index, getArrayFromType(this.elementType.getTag()));
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
        unshift(0, (ArrayValue) values);
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(" ");
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                for (int i = 0; i < size; i++) {
                    sj.add(Long.toString(intValues[i]));
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                for (int i = 0; i < size; i++) {
                    sj.add(Boolean.toString(booleanValues[i]));
                }
                break;
            case TypeTags.BYTE_TAG:
                for (int i = 0; i < size; i++) {
                    sj.add(Long.toString(Byte.toUnsignedLong(byteValues[i])));
                }
                break;
            case TypeTags.FLOAT_TAG:
                for (int i = 0; i < size; i++) {
                    sj.add(Double.toString(floatValues[i]));
                }
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                for (int i = 0; i < size; i++) {
                    sj.add(bStringValues[i].getValue());
                }
                break;
            default:
                for (int i = 0; i < size; i++) {
                    sj.add(StringUtils.getStringValue(refValues[i]));
                }
                break;
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return this.arrayType;
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

        ArrayValue valueArray = null;
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                valueArray = new ArrayValueImpl(Arrays.copyOf(intValues, this.size));
                break;
            case TypeTags.BOOLEAN_TAG:
                valueArray = new ArrayValueImpl(Arrays.copyOf(booleanValues, this.size));
                break;
            case TypeTags.BYTE_TAG:
                valueArray = new ArrayValueImpl(Arrays.copyOf(byteValues, this.size));
                break;
            case TypeTags.FLOAT_TAG:
                valueArray = new ArrayValueImpl(Arrays.copyOf(floatValues, this.size));
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                valueArray = new ArrayValueImpl(Arrays.copyOf(bStringValues, this.size));
                break;
            default:
                Object[] values = new Object[this.size];
                valueArray = new ArrayValueImpl(values, arrayType);
                IntStream.range(0, this.size).forEach(i -> {
                    Object value = this.refValues[i];
                    if (value instanceof RefValue) {
                        values[i] = ((RefValue) value).copy(refs);
                    } else {
                        values[i] = value;
                    }
                });
                break;
        }

        refs.put(this, valueArray);
        return valueArray;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        ArrayValue copy = (ArrayValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    /**
     * Return a subarray starting from `startIndex` (inclusive) to `endIndex` (exclusive).
     *
     * @param startIndex index of first member to include in the slice
     * @param endIndex index of first member not to include in the slice
     * @return array slice within specified range
     */
    @Deprecated
    public ArrayValueImpl slice(long startIndex, long endIndex) {
        ArrayValueImpl slicedArray;
        int slicedSize = (int) (endIndex - startIndex);
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                slicedArray = new ArrayValueImpl(new long[slicedSize]);
                System.arraycopy(intValues, (int) startIndex, slicedArray.intValues, 0, slicedSize);
                break;
            case TypeTags.BOOLEAN_TAG:
                slicedArray = new ArrayValueImpl(new boolean[slicedSize]);
                System.arraycopy(booleanValues, (int) startIndex, slicedArray.booleanValues, 0, slicedSize);
                break;
            case TypeTags.BYTE_TAG:
                slicedArray = new ArrayValueImpl(new byte[slicedSize]);
                System.arraycopy(byteValues, (int) startIndex, slicedArray.byteValues, 0, slicedSize);
                break;
            case TypeTags.FLOAT_TAG:
                slicedArray = new ArrayValueImpl(new double[slicedSize]);
                System.arraycopy(floatValues, (int) startIndex, slicedArray.floatValues, 0, slicedSize);
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                slicedArray = new ArrayValueImpl(new BString[slicedSize]);
                System.arraycopy(bStringValues, (int) startIndex, slicedArray.bStringValues, 0, slicedSize);
                break;
            default:
                slicedArray = new ArrayValueImpl(new Object[slicedSize], new BArrayType(this.elementType));
                System.arraycopy(refValues, (int) startIndex, slicedArray.refValues, 0, slicedSize);
                break;
        }
        return slicedArray;
    }

    @Override
    public String toString() {
        return stringValue();
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
        byte[] bytes = new byte[this.size];
        System.arraycopy(byteValues, 0, bytes, 0, this.size);
        return bytes;
    }

    /**
     * Get a copy of string array.
     * 
     * @return string array
     */
    @Override
    public String[] getStringArray() {
        String[] arr = new String[size];
        for (int i = 0; i < size; i++) {
            arr[i] = bStringValues[i].getValue();
        }
        return arr;
    }

    public long[] getLongArray() {
        return Arrays.copyOf(intValues, size);
    }

    /**
     * Get a copy of int array.
     * 
     * @return int array
     */
    @Override
    public long[] getIntArray() {
        return Arrays.copyOf(intValues, size);
    }

    @Override
    public void serialize(OutputStream outputStream) {
        if (this.elementType.getTag() == TypeTags.BYTE_TAG) {
            try {
                for (int i = 0; i < this.size; i++) {
                    outputStream.write(this.byteValues[i]);
                }
            } catch (IOException e) {
                throw new BallerinaException("error occurred while writing the binary content to the output stream", e);
            }
        } else {
            try {
                outputStream.write(this.toString().getBytes(Charset.defaultCharset()));
            } catch (IOException e) {
                throw new BallerinaException("error occurred while serializing data", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (arrayType.isReadOnly()) {
            return;
        }

        this.arrayType = (BArrayType) ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.arrayType);
        if (this.elementType == null || this.elementType.getTag() > TypeTags.BOOLEAN_TAG) {
            for (int i = 0; i < this.size; i++) {
                Object value = this.getRefValue(i);
                if (value instanceof RefValue) {
                    ((RefValue) value).freezeDirect();
                }
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
        return this.elementType;
    }

    // Protected methods

    @Override
    protected void resizeInternalArray(int newLength) {
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                intValues = Arrays.copyOf(intValues, newLength);
                break;
            case TypeTags.BOOLEAN_TAG:
                booleanValues = Arrays.copyOf(booleanValues, newLength);
                break;
            case TypeTags.BYTE_TAG:
                byteValues = Arrays.copyOf(byteValues, newLength);
                break;
            case TypeTags.FLOAT_TAG:
                floatValues = Arrays.copyOf(floatValues, newLength);
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                bStringValues = Arrays.copyOf(bStringValues, newLength);
                break;
            default:
                refValues = Arrays.copyOf(refValues, newLength);
                break;
        }
    }

    @Override
    protected void fillValues(int index) {
        if (index <= this.size) {
            return;
        }

        switch (this.elementType.getTag()) {
            case TypeTags.STRING_TAG:
                Arrays.fill(bStringValues, size, index, BLangConstants.STRING_EMPTY_VALUE);
                return;
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
                return;
            default:
                if (arrayType.hasFillerValue()) {
                    for (int i = size; i < index; i++) {
                        this.refValues[i] = this.elementType.getZeroValue();
                    }
                }
        }
    }

    @Override
    protected void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index >= size) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if ((int) index < 0 || index >= maxSize) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void fillerValueCheck(int index, int size) {
        // if the elementType doesn't have an implicit initial value & if the insertion is not a consecutive append
        // to the array, then an exception will be thrown.
        if (arrayType.hasFillerValue()) {
            return;
        }
        if (index > size) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                                                           RuntimeErrors.ILLEGAL_ARRAY_INSERTION, size, index + 1);
        }
    }

    @Override
    protected void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if (requestedCapacity <= currentArraySize) {
            return;
        }

        if (this.arrayType.getState() != ArrayState.UNSEALED) {
            return;
        }

        // Here the growth rate is 1.5. This value has been used by many other languages
        int newArraySize = currentArraySize + (currentArraySize >> 1);

        // Now get the maximum value of the calculate new array size and request capacity
        newArraySize = Math.max(newArraySize, requestedCapacity);

        // Now get the minimum value of new array size and maximum array size
        newArraySize = Math.min(newArraySize, maxSize);
        resizeInternalArray(newArraySize);
    }

    @Override
    protected void checkFixedLength(long length) {
        if (this.arrayType.getState() == ArrayState.CLOSED_SEALED) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_ARRAY_SIZE, size, length);
        }
    }

    @Override
    protected void unshift(long index, ArrayValue vals) {
        handleImmutableArrayValue();
        unshiftArray(index, vals.size(), getCurrentArrayLength());

        int startIndex = (int) index;
        int endIndex = startIndex + vals.size();

        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.get(j));
        }
    }

    // Private methods

    private void prepareForAdd(long index, Object value, BType sourceType, int currentArraySize) {
        // check types
        if (!TypeChecker.checkIsType(value, sourceType, this.elementType)) {
            String reason = getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER);
            String detail = BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_TYPE, this.elementType,
                    sourceType);
            throw BallerinaErrors.createError(reason, detail);
        }

        int intIndex = (int) index;
        rangeCheck(index, size);
        fillerValueCheck(intIndex, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        fillValues(intIndex);
        resetSize(intIndex);
    }

    private void fillRead(long index, int currentArraySize) {
        if (!arrayType.hasFillerValue()) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                                                           RuntimeErrors.ILLEGAL_ARRAY_INSERTION, size, index + 1);
        }

        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);

        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
                break;
            case TypeTags.STRING_TAG:
                Arrays.fill(bStringValues, size, intIndex, BLangConstants.STRING_EMPTY_VALUE);
                break;
            default:
                for (int i = size; i <= index; i++) {
                    this.refValues[i] = this.elementType.getZeroValue();
                }
        }

        resetSize(intIndex);
    }

    private void setArrayType(BType elementType) {
        this.arrayType = new BArrayType(elementType);
        this.elementType = elementType;
    }

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    private void shiftArray(int index, Object arr) {
        int nElemsToBeMoved = this.size - 1 - index;
        if (nElemsToBeMoved >= 0) {
            System.arraycopy(arr, index + 1, arr, index, nElemsToBeMoved);
        }
        this.size--;
    }

    private void unshiftArray(long index, int unshiftByN, int arrLength) {
        int lastIndex = size() + unshiftByN - 1;
        prepareForConsecutiveMultiAdd(lastIndex, arrLength);
        Object arr = getArrayFromType(elementType.getTag());

        if (index > lastIndex) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        int i = (int) index;
        System.arraycopy(arr, i, arr, i + unshiftByN, this.size - i);
    }

    private Object getArrayFromType(int typeTag) {
        switch (typeTag) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return intValues;
            case TypeTags.BOOLEAN_TAG:
                return booleanValues;
            case TypeTags.BYTE_TAG:
                return byteValues;
            case TypeTags.FLOAT_TAG:
                return floatValues;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                return bStringValues;
            default:
                return refValues;
        }
    }

    private int getCurrentArrayLength() {
        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return intValues.length;
            case TypeTags.BOOLEAN_TAG:
                return booleanValues.length;
            case TypeTags.BYTE_TAG:
                return byteValues.length;
            case TypeTags.FLOAT_TAG:
                return floatValues.length;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                return bStringValues.length;
            default:
                return refValues.length;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArrayValueImpl that = (ArrayValueImpl) o;
        return arrayType.equals(that.arrayType) &&
                elementType.equals(that.elementType) &&
                Arrays.equals(refValues, that.refValues) &&
                Arrays.equals(intValues, that.intValues) &&
                Arrays.equals(booleanValues, that.booleanValues) &&
                Arrays.equals(byteValues, that.byteValues) &&
                Arrays.equals(floatValues, that.floatValues) &&
                Arrays.equals(bStringValues, that.bStringValues);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(arrayType, elementType);
        result = 31 * result + Arrays.hashCode(refValues);
        result = 31 * result + Arrays.hashCode(intValues);
        result = 31 * result + Arrays.hashCode(booleanValues);
        result = 31 * result + Arrays.hashCode(byteValues);
        result = 31 * result + Arrays.hashCode(floatValues);
        result = 31 * result + Arrays.hashCode(bStringValues);
        return result;
    }
}
