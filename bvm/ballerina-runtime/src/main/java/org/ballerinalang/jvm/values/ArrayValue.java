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

import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Represent an array in ballerina.
 * 
 * @since 0.995.0
 */
public class ArrayValue implements RefValue, CollectionValue {

    protected BType arrayType;
    private volatile Status freezeStatus = new Status(State.UNFROZEN);

    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    protected int maxArraySize = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_ARRAY_SIZE = 100;
    protected int size = 0;

    Object[] refValues;
    private long[] intValues;
    private boolean[] booleanValues;
    private byte[] byteValues;
    private double[] floatValues;
    private String[] stringValues;

    public BType elementType;

    //------------------------ Constructors -------------------------------------------------------------------

    public ArrayValue(Object[] values, BType type) {
        this.refValues = values;
        this.arrayType = type;
        this.size = values.length;
    }

    public ArrayValue(long[] values) {
        this.intValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeInt);
    }

    public ArrayValue(boolean[] values) {
        this.booleanValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeBoolean);
    }

    public ArrayValue(byte[] values) {
        this.byteValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeByte);
    }

    public ArrayValue(double[] values) {
        this.floatValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeFloat);
    }

    public ArrayValue(String[] values) {
        this.stringValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeString);
    }

    public ArrayValue(BType type) {
        this.arrayType = type;
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType arrayType = (BArrayType) type;
            this.elementType = arrayType.getElementType();
            if (arrayType.getState() == ArrayState.CLOSED_SEALED) {
                this.size = maxArraySize = arrayType.getSize();
            }
            initArrayValues(this.elementType);
        } else if (type.getTag() == TypeTags.TUPLE_TAG) {
            BTupleType tupleType = (BTupleType) type;
            this.size = maxArraySize = tupleType.getTupleTypes().size();
            refValues = (Object[]) newArrayInstance(Object.class);
            AtomicInteger counter = new AtomicInteger(0);
            tupleType.getTupleTypes()
                    .forEach(memType -> refValues[counter.getAndIncrement()] = memType.getEmptyValue());
        } else {
            refValues = (Object[]) newArrayInstance(Object.class);
            Arrays.fill(refValues, type.getEmptyValue());
        }
    }

    private void initArrayValues(BType elementType) {
        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
                intValues = (long[]) newArrayInstance(Long.TYPE);
                break;
            case TypeTags.FLOAT_TAG:
                floatValues = (double[]) newArrayInstance(Double.TYPE);
                break;
            case TypeTags.STRING_TAG:
                stringValues = (String[]) newArrayInstance(String.class);
                Arrays.fill(stringValues, BLangConstants.STRING_EMPTY_VALUE);
                break;
            case TypeTags.BOOLEAN_TAG:
                booleanValues = (boolean[]) newArrayInstance(Boolean.TYPE);
                break;
            case TypeTags.BYTE_TAG:
                byteValues = (byte[]) newArrayInstance(Byte.TYPE);
                break;
            default:
                refValues = (Object[]) newArrayInstance(Object.class);
                Arrays.fill(refValues, elementType.getZeroValue());
        }
    }

    public ArrayValue() {
        refValues = (Object[]) newArrayInstance(Object.class);
    }

    public ArrayValue(BType type, long size) {
        if (size != -1) {
            this.size = maxArraySize = (int) size;
        }

        this.arrayType = type;
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            this.elementType = ((BArrayType) type).getElementType();
            initArrayValues(this.elementType);
        } else {
            refValues = (Object[]) newArrayInstance(Object.class);
            Arrays.fill(refValues, type.getEmptyValue());
        }
    }

    // -----------------------  get methods ----------------------------------------------------

    public Object getRefValue(long index) {
        rangeCheckForGet(index, size);
        return refValues[(int) index];
    }

    public long getInt(long index) {
        rangeCheckForGet(index, size);
        return intValues[(int) index];
    }

    public boolean getBoolean(long index) {
        rangeCheckForGet(index, size);
        return booleanValues[(int) index];
    }

    public byte getByte(long index) {
        rangeCheckForGet(index, size);
        return byteValues[(int) index];
    }

    public double getFloat(long index) {
        rangeCheckForGet(index, size);
        return floatValues[(int) index];
    }

    public String getString(long index) {
        rangeCheckForGet(index, size);
        return stringValues[(int) index];
    }

    public Object get(long index) {
        rangeCheckForGet(index, size);
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
                return intValues[(int) index];
            case TypeTags.BOOLEAN_TAG:
                return booleanValues[(int) index];
            case TypeTags.BYTE_ARRAY_TAG:
                return byteValues[(int) index];
            case TypeTags.FLOAT_TAG:
                return floatValues[(int) index];
            case TypeTags.STRING_TAG:
                return stringValues[(int) index];
            default:
                return refValues[(int) index];
        }
    }

    // ----------------------------  add methods --------------------------------------------------

    public void add(long index, Object value) {
        handleFrozenArrayValue();
        prepareForAdd(index, refValues.length);
        refValues[(int) index] = value;
    }

    public void add(long index, long value) {
        handleFrozenArrayValue();
        prepareForAdd(index, intValues.length);
        intValues[(int) index] = value;
    }

    public void add(long index, boolean value) {
        if (elementType.getTag() == TypeTags.INT_TAG) {
            add(index, value);
            return;
        }

        handleFrozenArrayValue();
        prepareForAdd(index, booleanValues.length);
        booleanValues[(int) index] = value;
    }

    public void add(long index, byte value) {
        handleFrozenArrayValue();
        prepareForAdd(index, byteValues.length);
        byteValues[(int) index] = value;
    }

    public void add(long index, double value) {
        handleFrozenArrayValue();
        prepareForAdd(index, floatValues.length);
        floatValues[(int) index] = value;
    }

    public void add(long index, String value) {
        handleFrozenArrayValue();
        prepareForAdd(index, stringValues.length);
        stringValues[(int) index] = value;
    }

    //-------------------------------------------------------------------------------------------------------------

    public void append(Object value) {
        add(size, value);
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void stamp(BType type) {
    }

    public int size() {
        return size;
    }
    
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        if (elementType != null) {
            ArrayValue valueArray = null;

            if (elementType.getTag() == TypeTags.INT_TAG) {
                valueArray = new ArrayValue(Arrays.copyOf(intValues, intValues.length));
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                valueArray = new ArrayValue(Arrays.copyOf(booleanValues, booleanValues.length));
            } else if (elementType.getTag() == TypeTags.BYTE_TAG) {
                valueArray = new ArrayValue(Arrays.copyOf(byteValues, byteValues.length));
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                valueArray = new ArrayValue(Arrays.copyOf(floatValues, floatValues.length));
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                valueArray = new ArrayValue(Arrays.copyOf(stringValues, stringValues.length));
            }

            if (valueArray != null) {
                valueArray.size = this.size;
                refs.put(this, valueArray);
                return valueArray;
            }
        }

        Object[] values = new Object[size];
        ArrayValue refValueArray = new ArrayValue(values, arrayType);
        refValueArray.size = this.size;
        refs.put(this, refValueArray);
        int bound = this.size;
        IntStream.range(0, bound).forEach(i -> {
            Object value = this.refValues[i];
            if (value instanceof RefValue) {
                values[i] = ((RefValue) value).copy(refs);
            } else {
                values[i] = value;
            }
        });

        return refValueArray;
    }
    
    @Override
    public String toString() {
        if (elementType != null) {
            StringJoiner sj = new StringJoiner(", ", "[", "]");
            if (elementType.getTag() == TypeTags.INT_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Long.toString(intValues[i]));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Boolean.toString(booleanValues[i]));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.BYTE_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Integer.toString(Byte.toUnsignedInt(byteValues[i])));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Double.toString(floatValues[i]));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add("\"" + stringValues[i] + "\"");
                }
                return sj.toString();
            }
        }

        if (getElementType(arrayType).getTag() == TypeTags.JSON_TAG) {
            return getJSONString();
        }

        StringJoiner sj;
        if (arrayType != null && (arrayType.getTag() == TypeTags.TUPLE_TAG)) {
            sj = new StringJoiner(", ", "(", ")");
        } else {
            sj = new StringJoiner(", ", "[", "]");
        }

        Object value;
        for (int i = 0; i < size; i++) {
            value = refValues[i];
            if (value != null) {
                sj.add((TypeChecker.getType(value).getTag() == TypeTags.STRING_TAG) ? ("\"" + value + "\"")
                        : value.toString());
            }
        }
        return sj.toString();
    }

    public Object[] getValues() {
        return refValues;
    }

    public byte[] getBytes() {
        return byteValues.clone();
    }

    public String[] getStringArray() {
        return stringValues;
    }

    @Override
    public void serialize(OutputStream outputStream) {
        if (elementType.getTag() == TypeTags.BYTE_TAG) {
            try {
                outputStream.write(byteValues);
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

    public void grow(int newLength) {
        if (elementType != null) {
            switch (elementType.getTag()) {
                case TypeTags.INT_TAG:
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
                    stringValues = Arrays.copyOf(stringValues, newLength);
                    break;
                default:
                    refValues = Arrays.copyOf(refValues, newLength);
                    break;
            }
        } else {
            refValues = Arrays.copyOf(refValues, newLength);
        }
    }

    public BType getArrayType() {
        return arrayType;
    }

    private void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index < 0 || index >= size) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    private void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if ((int) index < 0 || index >= maxArraySize) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    Object newArrayInstance(Class<?> componentType) {
        return (size > 0) ?
                Array.newInstance(componentType, size) : Array.newInstance(componentType, DEFAULT_ARRAY_SIZE);
    }

    private void setArrayElementType(BType type) {
        this.arrayType = new BArrayType(type);
        this.elementType = type;
    }

    private String getJSONString() {
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

    private BType getElementType(BType type) {
        if (type.getTag() != TypeTags.ARRAY_TAG) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    /**
     * Util method to handle frozen array values.
     */
    private void handleFrozenArrayValue() {
        synchronized (this) {
            if (this.freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }
    }

    protected void prepareForAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    private void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if ((requestedCapacity) - currentArraySize >= 0) {
            // Here the growth rate is 1.5. This value has been used by many other languages
            int newArraySize = currentArraySize + (currentArraySize >> 1);

            // Now get the maximum value of the calculate new array size and request capacity
            newArraySize = Math.max(newArraySize, requestedCapacity);

            // Now get the minimum value of new array size and maximum array size
            newArraySize = Math.min(newArraySize, maxArraySize);
            grow(newArraySize);
        }
    }

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (!FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            return;
        }
        this.freezeStatus = freezeStatus;
        if (elementType == null || elementType.getTag() > TypeTags.BOOLEAN_TAG) {
            for (int i = 0; i < this.size; i++) {
                Object refValue = this.getRefValue(i);
                ((RefValue) refValue).attemptFreeze(freezeStatus);
            }
        }
    }

    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

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
