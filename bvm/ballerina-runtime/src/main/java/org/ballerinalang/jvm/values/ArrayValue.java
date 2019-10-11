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
import org.ballerinalang.jvm.scheduling.Strand;
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
import org.ballerinalang.jvm.values.utils.StringUtils;

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

import static org.ballerinalang.jvm.TypeChecker.anyToByte;
import static org.ballerinalang.jvm.TypeChecker.anyToDecimal;
import static org.ballerinalang.jvm.TypeChecker.anyToFloat;
import static org.ballerinalang.jvm.TypeChecker.anyToInt;
import static org.ballerinalang.jvm.TypeConverter.getConvertibleTypes;
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
public class ArrayValue implements RefValue, CollectionValue {

    static final int SYSTEM_ARRAY_MAX = Integer.MAX_VALUE - 8;
    protected BType arrayType;
    private volatile Status freezeStatus = new Status(State.UNFROZEN);

    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    protected int maxArraySize = SYSTEM_ARRAY_MAX;
    private static final int DEFAULT_ARRAY_SIZE = 100;
    protected int size = 0;

    Object[] refValues;
    private long[] intValues;
    private boolean[] booleanValues;
    private byte[] byteValues;
    private double[] floatValues;
    private String[] stringValues;

    public BType elementType;
    private BType tupleRestType;

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
                tupleRestType = tupleType.getRestType();
                size = tupleType.getTupleTypes().size();
                maxArraySize = (tupleRestType != null) ? maxArraySize : size;
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
        this.arrayType = type;
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            elementType = ((BArrayType) type).getElementType();
            if (size != -1) {
                this.size = maxArraySize = (int) size;
            }
            initArrayValues(elementType);
        } else if (type.getTag() == TypeTags.TUPLE_TAG) {
            tupleRestType = ((BTupleType) type).getRestType();
            if (size != -1) {
                this.size = (int) size;
                maxArraySize = (tupleRestType != null) ? maxArraySize : (int) size;
            }
            refValues = (Object[]) newArrayInstance(Object.class);
        } else {
            if (size != -1) {
                this.size = maxArraySize = (int) size;
            }
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

        unshiftArray(index, vals.size, getCurrentArrayLength());

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

    private void unshiftArray(long index, int unshiftByN, int arrLength) {
        int lastIndex = size() + unshiftByN - 1;
        prepareForConsecutiveMultiAdd(lastIndex, arrLength);
        Object arr = getArrayFromType(elementType.getTag());

        if (index > lastIndex) {
            throw BLangExceptionHelper.getRuntimeException(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                                   INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        int i = (int) index;
        System.arraycopy(arr, i, arr, i + unshiftByN, this.size - i);
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
        return stringValue(null);
    }

    @Override
    public String stringValue(Strand strand) {
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

        StringJoiner sj;
        if (arrayType != null && (arrayType.getTag() == TypeTags.TUPLE_TAG)) {
            sj = new StringJoiner(" ");
        } else {
            sj = new StringJoiner(" ");
        }

        for (int i = 0; i < size; i++) {
            sj.add(StringUtils.getStringValue(strand, refValues[i]));
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
                BType memberType = ((BTupleType) type).getTupleTypes().get(i);
                if (arrayValues[i] instanceof RefValue) {
                    if (memberType.getTag() == TypeTags.ANYDATA_TAG || memberType.getTag() == TypeTags.JSON_TAG) {
                        memberType = TypeConverter.resolveMatchingTypeForUnion(arrayValues[i], memberType);
                        ((BTupleType) type).getTupleTypes().set(i, memberType);
                    }
                    ((RefValue) arrayValues[i]).stamp(memberType, unresolvedValues);
                } else if (!TypeChecker.checkIsType(arrayValues[i], memberType)) {
                    // Has to be a numeric conversion.
                    arrayValues[i] = TypeConverter.convertValues(getConvertibleTypes(arrayValues[i], memberType).get(0),
                                                                 arrayValues[i]);
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
            type = getConvertibleTypes(this, type).get(0);
            this.stamp(type, unresolvedValues);
        } else if (type.getTag() == TypeTags.ANYDATA_TAG) {
            type = TypeConverter.resolveMatchingTypeForUnion(this, type);
            this.stamp(type, unresolvedValues);
        } else {
            BType arrayElementType = ((BArrayType) type).getElementType();

            if (elementType != null && isBasicType(elementType)) {
                if (isBasicType(arrayElementType)) {
                    if (TypeChecker.isNumericType(elementType) && TypeChecker.isNumericType(arrayElementType)) {
                        convertNumericTypeArray(type, arrayElementType);
                        return;
                    }
                    this.arrayType = type;
                    return;
                }

                moveBasicTypeArrayToRefValueArray();
                for (int i = 0; i < this.size(); i++) {
                    Object value = refValues[i];
                    if (!(value instanceof RefValue) && !TypeChecker.checkIsType(value, arrayElementType)) {
                        // Has to be a numeric conversion.
                        refValues[i] = TypeConverter.convertValues(getConvertibleTypes(value, arrayElementType).get(0),
                                                                   value);
                    }
                }

                this.elementType = arrayElementType;
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
                } else if (!TypeChecker.checkIsType(arrayValues[i], arrayElementType)) {
                    // Has to be a numeric conversion.
                    arrayValues[i] =
                            TypeConverter.convertValues(getConvertibleTypes(arrayValues[i], arrayElementType).get(0),
                                                        arrayValues[i]);
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

    public void resizeInternalArray(int newLength) {
        if (arrayType.getTag() == TypeTags.TUPLE_TAG) {
            refValues = Arrays.copyOf(refValues, newLength);
        } else {
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
    }

    private void fillValues(int index) {
        if (index <= size) {
            return;
        }

        if (arrayType.getTag() == TypeTags.TUPLE_TAG) {
            if (tupleRestType != null) {
                Arrays.fill(refValues, size, index, tupleRestType.getZeroValue());
            }
        } else {
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
    }

    public BType getArrayType() {
        return arrayType;
    }

    private void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index < 0 || index >= size) {
            if (arrayType != null && arrayType.getTag() == TypeTags.TUPLE_TAG) {
                throw BLangExceptionHelper.getRuntimeException(
                        getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                        RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
            }
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    private void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                                   INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }
        if (arrayType != null && arrayType.getTag() == TypeTags.TUPLE_TAG) {
            if ((((BTupleType) arrayType).getRestType() == null && index >= maxArraySize) || (int) index < 0) {
                throw BLangExceptionHelper.getRuntimeException(
                        getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                        RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
            }
        } else {
            if ((int) index < 0 || index >= maxArraySize) {
                throw BLangExceptionHelper.getRuntimeException(
                        getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                        RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
            }
        }
    }

    private void fillerValueCheck(int index, int size) {
        // if the elementType doesn't have an implicit initial value & if the insertion is not a consecutive append
        // to the array, then an exception will be thrown.
        if (arrayType != null && arrayType.getTag() == TypeTags.TUPLE_TAG) {
            if (!TypeChecker.hasFillerValue(tupleRestType) && (index > size)) {
                throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                        RuntimeErrors.ILLEGAL_TUPLE_INSERTION, size, index + 1);
            }
        } else {
            if (!TypeChecker.hasFillerValue(elementType) && (index > size)) {
                throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                        RuntimeErrors.ILLEGAL_ARRAY_INSERTION, size, index + 1);
            }
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
                    FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), ARRAY_LANG_LIB);
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

    /**
     * Same as {@code prepareForAdd}, except fillerValueCheck is not performed as we are guaranteed to add
     * elements to consecutive positions.
     *
     * @param index last index after add operation completes
     * @param currentArraySize current array size
     */
    void prepareForConsecutiveMultiAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    private void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if ((requestedCapacity) - currentArraySize > 0) {
            if ((this.arrayType.getTag() == TypeTags.ARRAY_TAG
                    && ((BArrayType) this.arrayType).getState() == ArrayState.UNSEALED)
                    || this.arrayType.getTag() == TypeTags.TUPLE_TAG) {
                // Here the growth rate is 1.5. This value has been used by many other languages
                int newArraySize = currentArraySize + (currentArraySize >> 1);

                // Now get the maximum value of the calculate new array size and request capacity
                newArraySize = Math.max(newArraySize, requestedCapacity);

                // Now get the minimum value of new array size and maximum array size
                newArraySize = Math.min(newArraySize, maxArraySize);
                resizeInternalArray(newArraySize);
            }
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
        } else if (elementType == BTypes.typeInt) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = intValues[i];
            }
            intValues = null;
        } else if (elementType == BTypes.typeString) {
            System.arraycopy(stringValues, 0, refValues, 0, this.size());
            stringValues = null;
        } else if (elementType == BTypes.typeFloat) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = floatValues[i];
            }
            floatValues = null;
        } else if (elementType == BTypes.typeByte) {
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
                Object value = arrayValues[i];
                if (TypeChecker.checkIsType(value, arrayElementType)) {
                    intValues[i] = ((long) value);
                } else {
                    // Has to be a numeric conversion.
                    intValues[i] = (long) TypeConverter.convertValues(arrayElementType, value);
                }
            }
        }

        if (arrayElementType.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
            for (int i = 0; i < this.size(); i++) {
                Object value = arrayValues[i];
                if (TypeChecker.checkIsType(value, arrayElementType)) {
                    floatValues[i] = ((float) value);
                } else {
                    // Has to be a numeric conversion.
                    floatValues[i] = (float) TypeConverter.convertValues(arrayElementType, value);
                }
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
                Object value = arrayValues[i];
                if (TypeChecker.checkIsType(value, arrayElementType)) {
                    byteValues[i] = ((byte) value);
                } else {
                    // Has to be a numeric conversion.
                    byteValues[i] = (byte) TypeConverter.convertValues(arrayElementType, value);
                }
            }
        }

        this.elementType = arrayElementType;
        this.arrayType = type;
        refValues = null;
    }

    private void convertNumericTypeArray(BType type, BType arrayElementType) {
        if (arrayElementType.getTag() == this.elementType.getTag()) {
            return;
        }

        switch (arrayElementType.getTag()) {
            case TypeTags.BYTE_TAG:
                byteValues = (byte[]) newArrayInstance(Byte.TYPE);
                for (int i = 0; i < this.size(); i++) {
                    byteValues[i] = (byte) anyToByte(this.get(i));
                }
                break;
            case TypeTags.INT_TAG:
                intValues = (long[]) newArrayInstance(Long.TYPE);
                for (int i = 0; i < this.size(); i++) {
                    intValues[i] = anyToInt(this.get(i));
                }
                break;
            case TypeTags.FLOAT_TAG:
                floatValues = (double[]) newArrayInstance(Double.TYPE);
                for (int i = 0; i < this.size(); i++) {
                    floatValues[i] = anyToFloat(this.get(i));
                }
                break;
            case TypeTags.DECIMAL_TAG:
                for (int i = 0; i < this.size(); i++) {
                    refValues[i] = anyToDecimal(this.get(i));
                }

                switch (this.elementType.getTag()) {
                    case TypeTags.BYTE_TAG:
                        byteValues = null;
                        break;
                    case TypeTags.INT_TAG:
                        intValues = null;
                        break;
                    case TypeTags.FLOAT_TAG:
                        floatValues = null;
                        break;
                }
                this.elementType = arrayElementType;
                this.arrayType = type;
                return;
        }

        this.elementType = arrayElementType;
        this.arrayType = type;
        refValues = null;
    }
    
    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

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

    private void checkFixedLength(long length) {
        if (arrayType == null) {
            return;
        }
        if (arrayType.getTag() == TypeTags.TUPLE_TAG) {
            BTupleType tupleType = (BTupleType) this.arrayType;
            if (tupleType.getRestType() == null) {
                throw BLangExceptionHelper.getRuntimeException(
                        getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        RuntimeErrors.ILLEGAL_TUPLE_SIZE, size, length);
            } else if (tupleType.getTupleTypes().size() > length) {
                throw BLangExceptionHelper.getRuntimeException(
                        getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        RuntimeErrors.ILLEGAL_TUPLE_WITH_REST_TYPE_SIZE, tupleType.getTupleTypes().size(), length);
            }
        } else if (((BArrayType) this.arrayType).getState() == ArrayState.CLOSED_SEALED) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_ARRAY_SIZE, size, length);
        }
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
