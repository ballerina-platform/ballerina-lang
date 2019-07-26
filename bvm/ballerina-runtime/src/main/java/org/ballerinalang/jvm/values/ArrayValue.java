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
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;
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
import java.util.ArrayList;
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
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            this.elementType = ((BArrayType) type).getElementType();
        }
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
        if (type.getTag() == TypeTags.INT_TAG) {
            intValues = (long[]) newArrayInstance(Long.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.BOOLEAN_TAG) {
            booleanValues = (boolean[]) newArrayInstance(Boolean.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.BYTE_TAG) {
            byteValues = (byte[]) newArrayInstance(Byte.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.STRING_TAG) {
            stringValues = (String[]) newArrayInstance(String.class);
            setArrayElementType(type);
        } else {
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
            } else if (type.getTag() == TypeTags.UNION_TAG) {
                BUnionType unionType = (BUnionType) type;
                this.size = maxArraySize = unionType.getMemberTypes().size();
                unionType.getMemberTypes().forEach(this::initArrayValues);
            } else {
                refValues = (Object[]) newArrayInstance(Object.class);
            }
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
                break;
            case TypeTags.BOOLEAN_TAG:
                booleanValues = (boolean[]) newArrayInstance(Boolean.TYPE);
                break;
            case TypeTags.BYTE_TAG:
                byteValues = (byte[]) newArrayInstance(Byte.TYPE);
                break;
            case TypeTags.XML_TAG:
                refValues = (Object[]) newArrayInstance(Object.class);
                break;
            default:
                refValues = (Object[]) newArrayInstance(Object.class);
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
        }
    }

    // -----------------------  get methods ----------------------------------------------------

    public Object getValue(long index) {
        if (elementType != null) {
            if (elementType.getTag() == TypeTags.INT_TAG) {
                return getInt(index);
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                return getBoolean(index);
            } else if (elementType.getTag() == TypeTags.BYTE_TAG) {
                return getByte(index);
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                return getFloat(index);
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                return getString(index);
            } else {
                return getRefValue(index);
            }
        }
        return getRefValue(index);
    }

    public Object getRefValue(long index) {
        rangeCheckForGet(index, size);
        if (refValues == null) {
            return getValue(index);
        }
        return refValues[(int) index];
    }

    public long getInt(long index) {
        rangeCheckForGet(index, size);
        if (elementType.getTag() == TypeTags.INT_TAG) {
            return intValues[(int) index];
        } else {
            return (Long) refValues[(int) index];
        }
    }

    public boolean getBoolean(long index) {
        rangeCheckForGet(index, size);
        if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
            return booleanValues[(int) index];
        } else {
            return (Boolean) refValues[(int) index];
        }
    }

    public byte getByte(long index) {
        rangeCheckForGet(index, size);
        if (elementType.getTag() == TypeTags.BYTE_TAG) {
            return byteValues[(int) index];
        } else {
            return (Byte) refValues[(int) index];
        }
    }

    public double getFloat(long index) {
        rangeCheckForGet(index, size);
        if (elementType.getTag() == TypeTags.FLOAT_TAG) {
            return floatValues[(int) index];
        } else {
            return (Double) refValues[(int) index];
        }
    }

    public String getString(long index) {
        rangeCheckForGet(index, size);
        if (elementType.getTag() == TypeTags.STRING_TAG) {
            return stringValues[(int) index];
        } else {
            return (String) refValues[(int) index];
        }
    }

    public Object get(long index) {
        rangeCheckForGet(index, size);
        switch (this.elementType.getTag()) {
            case TypeTags.INT_TAG:
                return intValues[(int) index];
            case TypeTags.BOOLEAN_TAG:
                return booleanValues[(int) index];
            case TypeTags.BYTE_TAG:
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

    public Object shift(long index) {
        handleFrozenArrayValue();
        Object val = get(index);
        shiftArray((int) index, getArrayFromType(elementType.getTag()));
        return val;
    }

    private void shiftArray(int index, Object arr) {
        int nElemsToBeMoved = this.size - 1 - index;
        if (nElemsToBeMoved >= 0) {
            System.arraycopy(arr, index + 1, arr, index, nElemsToBeMoved);
        }
        this.size--;
    }

    public void unshift(long index, ArrayValue vals) {
        handleFrozenArrayValue();

        Object valArr = getArrayFromType(elementType.getTag());
        unshiftArray(index, vals.size, valArr, getCurrentArrayLength());

        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
                addToIntArray(vals, (int) index);
                break;
            case TypeTags.BOOLEAN_TAG:
                addToBooleanArray(vals, (int) index);
                break;
            case TypeTags.BYTE_TAG:
                addToByteArray(vals, (int) index);
                break;
            case TypeTags.FLOAT_TAG:
                addToFloatArray(vals, (int) index);
                break;
            case TypeTags.STRING_TAG:
                addToStringArray(vals, (int) index);
                break;
            default:
                addToRefArray(vals, (int) index);
        }
    }

    private void addToIntArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getInt(j));
        }
    }

    private void addToFloatArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getFloat(j));
        }
    }

    private void addToStringArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getString(j));
        }
    }

    private void addToByteArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        byte[] bytes = vals.getBytes();
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            this.byteValues[i] = bytes[j];
        }
    }

    private void addToBooleanArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getBoolean(j));
        }
    }

    private void addToRefArray(ArrayValue vals, int startIndex) {
        int endIndex = startIndex + vals.size;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals.getRefValue(j));
        }
    }

    private void unshiftArray(long index, int unshiftByN, Object arr, int arrLength) {
        int lastIndex = arrLength + unshiftByN - 1;
        prepareForAdd(lastIndex, arrLength);

        if (index > lastIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        int i = (int) index;
        System.arraycopy(arr, i, arr, i + unshiftByN, this.size - i);

        this.size += unshiftByN;
    }

    private Object getArrayFromType(int typeTag) {
        switch (typeTag) {
            case TypeTags.INT_TAG:
                return intValues;
            case TypeTags.BOOLEAN_TAG:
                return booleanValues;
            case TypeTags.BYTE_TAG:
                return byteValues;
            case TypeTags.FLOAT_TAG:
                return floatValues;
            case TypeTags.STRING_TAG:
                return stringValues;
            default:
                return refValues;
        }
    }

    private int getCurrentArrayLength() {
        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
                return intValues.length;
            case TypeTags.BOOLEAN_TAG:
                return booleanValues.length;
            case TypeTags.BYTE_TAG:
                return byteValues.length;
            case TypeTags.FLOAT_TAG:
                return floatValues.length;
            case TypeTags.STRING_TAG:
                return stringValues.length;
            default:
                return refValues.length;
        }
    }

    @Override
    public String stringValue() {
        if (elementType != null) {
            StringJoiner sj = new StringJoiner(" ");
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
                    sj.add(Long.toString(Byte.toUnsignedLong(byteValues[i])));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Double.toString(floatValues[i]));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(stringValues[i]);
                }
                return sj.toString();
            }
        }

        if (getElementType(arrayType).getTag() == TypeTags.JSON_TAG) {
            return getJSONString();
        }

        StringJoiner sj;
        if (arrayType != null && (arrayType.getTag() == TypeTags.TUPLE_TAG)) {
            sj = new StringJoiner(" ");
        } else {
            sj = new StringJoiner(" ");
        }

        for (int i = 0; i < size; i++) {
            if (refValues[i] != null) {
                sj.add((refValues[i] instanceof RefValue) ? ((RefValue) refValues[i]).stringValue() :
                        (refValues[i] instanceof String) ? (String) refValues[i] :  refValues[i].toString());
            } else {
                sj.add("()");
            }
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
        if (type.getTag() == TypeTags.TUPLE_TAG) {

            if (elementType != null && isBasicType(elementType)) {
                moveBasicTypeArrayToRefValueArray();
            }
            Object[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] instanceof RefValue) {
                    BType memberType = ((BTupleType) type).getTupleTypes().get(i);
                    if (memberType.getTag() == TypeTags.ANYDATA_TAG || memberType.getTag() == TypeTags.JSON_TAG) {
                        memberType = TypeConverter.resolveMatchingTypeForUnion(arrayValues[i], memberType);
                        ((BTupleType) type).getTupleTypes().set(i, memberType);
                    }
                    ((RefValue) arrayValues[i]).stamp(memberType, unresolvedValues);
                }
            }
        } else if (type.getTag() == TypeTags.JSON_TAG) {

            if (elementType != null && isBasicType(elementType) && !isBasicType(type)) {
                moveBasicTypeArrayToRefValueArray();
                this.arrayType = new BArrayType(type);
                return;
            }

            Object[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] instanceof RefValue) {
                    ((RefValue) arrayValues[i]).stamp(TypeConverter.resolveMatchingTypeForUnion(arrayValues[i], type),
                                                      unresolvedValues);
                }
            }
            type = new BArrayType(type);
        } else if (type.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (TypeChecker.checkIsLikeType(this, memberType, new ArrayList<>())) {
                    this.stamp(memberType, unresolvedValues);
                    type = memberType;
                    break;
                }
            }
        } else if (type.getTag() == TypeTags.ANYDATA_TAG) {
            type = TypeConverter.resolveMatchingTypeForUnion(this, type);
            this.stamp(type, unresolvedValues);
        } else {
            BType arrayElementType = ((BArrayType) type).getElementType();

            if (elementType != null && isBasicType(elementType)) {
                if (isBasicType(arrayElementType)) {
                    this.arrayType = type;
                    return;
                }

                moveBasicTypeArrayToRefValueArray();
                this.arrayType = type;
                return;
            }

            if (isBasicType(arrayElementType) &&
                    (arrayType.getTag() == TypeTags.TUPLE_TAG || !isBasicType(elementType))) {
                moveRefValueArrayToBasicTypeArray(type, arrayElementType);
                return;
            }

            Object[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] instanceof RefValue) {
                    ((RefValue) arrayValues[i]).stamp(arrayElementType, unresolvedValues);
                }
            }
        }

        this.arrayType = type;
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

    public Object[] getValues() {
        return refValues;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[this.size];
        System.arraycopy(byteValues, 0, bytes, 0, this.size);
        return bytes;
    }

    public String[] getStringArray() {
        return Arrays.copyOf(stringValues, size);
    }

    public long[] getLongArray() {
        return Arrays.copyOf(intValues, size);
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

    private void fillValues(int index) {
        if (index <= size) {
            return;
        }

        int typeTag = elementType.getTag();

        if (typeTag == TypeTags.STRING_TAG) {
            Arrays.fill(stringValues, size, index, BLangConstants.STRING_EMPTY_VALUE);
            return;
        }

        if (typeTag == TypeTags.INT_TAG || typeTag == TypeTags.BYTE_TAG || typeTag == TypeTags.FLOAT_TAG ||
                typeTag == TypeTags.BOOLEAN_TAG) {
            return;
        }

        Arrays.fill(refValues, size, index, elementType.getZeroValue());
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
            if (this.arrayType != null && this.arrayType.getTag() == TypeTags.TUPLE_TAG) {
                throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                        RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
            }
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    private void fillerValueCheck(int index, int size) {
        // if the elementType doesn't have an implicit initial value & if the insertion is not a consecutive append
        // to the array, then an exception will be thrown.
        if (!TypeChecker.hasFillerValue(elementType) && (index > size)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_ARRAY_INSERTION_ERROR,
                    RuntimeErrors.ILLEGAL_ARRAY_INSERTION, size, index + 1);
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
            try {
                if (this.freezeStatus.getState() != State.UNFROZEN) {
                    FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
                }
            } catch (BLangFreezeException e) {
                throw BallerinaErrors.createError(e.getMessage(), e.getDetail());
            }
        }
    }

    protected void prepareForAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        fillerValueCheck(intIndex, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        fillValues(intIndex);
        resetSize(intIndex);
    }

    private void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if ((requestedCapacity) - currentArraySize > 0 && this.arrayType.getTag() == TypeTags.ARRAY_TAG &&
                ((BArrayType) this.arrayType).getState() == ArrayState.UNSEALED) {
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
                Object value = this.getRefValue(i);
                if (value instanceof RefValue) {
                    ((RefValue) value).attemptFreeze(freezeStatus);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (isFrozen()) {
            return;
        }
        this.freezeStatus.setFrozen();
        if (elementType == null || elementType.getTag() > TypeTags.BOOLEAN_TAG) {
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
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
    }

    private boolean isBasicType(BType type) {
        return type.getTag() <= TypeTags.BOOLEAN_TAG && type.getTag() != TypeTags.DECIMAL_TAG;
    }

    private void moveBasicTypeArrayToRefValueArray() {
        refValues = new Object[this.size];
        if (elementType == BTypes.typeBoolean) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = booleanValues[i];
            }
            booleanValues = null;
        }

        if (elementType == BTypes.typeInt) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = intValues[i];
            }
            intValues = null;
        }

        if (elementType == BTypes.typeString) {
            System.arraycopy(stringValues, 0, refValues, 0, this.size());
            stringValues = null;
        }

        if (elementType == BTypes.typeFloat) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = floatValues[i];
            }
            floatValues = null;
        }

        if (elementType == BTypes.typeByte) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = (byteValues[i]);
            }
            byteValues = null;
        }

        elementType = null;
    }

    private void moveRefValueArrayToBasicTypeArray(BType type, BType arrayElementType) {
        Object[] arrayValues = this.getValues();

        if (arrayElementType.getTag() == TypeTags.INT_TAG) {
            intValues = (long[]) newArrayInstance(Long.TYPE);
            for (int i = 0; i < this.size(); i++) {
                intValues[i] = ((long) arrayValues[i]);
            }
        }

        if (arrayElementType.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
            for (int i = 0; i < this.size(); i++) {
                floatValues[i] = ((float) arrayValues[i]);
            }
        }

        if (arrayElementType.getTag() == TypeTags.BOOLEAN_TAG) {
            booleanValues = new boolean[this.size()];
            for (int i = 0; i < this.size(); i++) {
                booleanValues[i] = ((boolean) arrayValues[i]);
            }
        }

        if (arrayElementType.getTag() == TypeTags.STRING_TAG) {
            stringValues = (String[]) newArrayInstance(String.class);
            for (int i = 0; i < this.size(); i++) {
                stringValues[i] = (String) arrayValues[i];
            }
        }

        if (arrayElementType.getTag() == TypeTags.BYTE_TAG) {
            byteValues = (byte[]) newArrayInstance(Byte.TYPE);
            for (int i = 0; i < this.size(); i++) {
                byteValues[i] = (byte) arrayValues[i];
            }
        }

        this.elementType = arrayElementType;
        this.arrayType = type;
        refValues = null;
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
            return array.getValue(cursor);
        }

        @Override
        public boolean hasNext() {
            return cursor < length;
        }
    }
}
