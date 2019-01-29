/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBRefArray;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.ballerinalang.model.util.FreezeUtils.handleInvalidUpdate;
import static org.ballerinalang.model.util.FreezeUtils.isOpenForFreeze;

/**
 * @since 0.985.0
 */
public class BValueArray extends BNewArray implements Serializable {

    BRefType<?>[] refValues;
    private long[] intValues;
    private int[] booleanValues;
    private byte[] byteValues;
    private double[] floatValues;
    private String[] stringValues;

    public BType elementType;

    //------------------------ Constructors -------------------------------------------------------------------

    public BValueArray(BRefType<?>[] values, BType type) {
        this.refValues = values;
        super.arrayType = type;
        this.size = values.length;
    }

    public BValueArray(long[] values) {
        this.intValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeInt);
    }

    public BValueArray(int[] values) {
        this.booleanValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeBoolean);
    }

    public BValueArray(byte[] values) {
        this.byteValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeByte);
    }

    public BValueArray(double[] values) {
        this.floatValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeFloat);
    }

    public BValueArray(String[] values) {
        this.stringValues = values;
        this.size = values.length;
        setArrayElementType(BTypes.typeString);
    }

    public BValueArray(BType type) {
        if (type.getTag() == TypeTags.INT_TAG) {
            intValues = (long[]) newArrayInstance(Long.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.BOOLEAN_TAG) {
            booleanValues = (int[]) newArrayInstance(Integer.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.BYTE_TAG) {
            byteValues = (byte[]) newArrayInstance(Byte.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
            setArrayElementType(type);
        } else if (type.getTag() == TypeTags.STRING_TAG) {
            stringValues = (String[]) newArrayInstance(String.class);
            Arrays.fill(stringValues, BLangConstants.STRING_EMPTY_VALUE);
            setArrayElementType(type);
        } else {
            super.arrayType = type;
            if (type.getTag() == TypeTags.ARRAY_TAG) {
                BArrayType arrayType = (BArrayType) type;
                this.elementType = arrayType.getElementType();
                if (arrayType.getState() == BArrayState.CLOSED_SEALED) {
                    this.size = maxArraySize = arrayType.getSize();
                }
                refValues = (BRefType[]) newArrayInstance(BRefType.class);
                Arrays.fill(refValues, arrayType.getElementType().getZeroValue());
            } else if (type.getTag() == TypeTags.TUPLE_TAG) {
                BTupleType tupleType = (BTupleType) type;
                this.size = maxArraySize = tupleType.getTupleTypes().size();
                refValues = (BRefType[]) newArrayInstance(BRefType.class);
                AtomicInteger counter = new AtomicInteger(0);
                tupleType.getTupleTypes().forEach(memType ->
                        refValues[counter.getAndIncrement()] = memType.getEmptyValue());
            } else {
                refValues = (BRefType[]) newArrayInstance(BRefType.class);
                Arrays.fill(refValues, type.getEmptyValue());
            }
        }
    }

    public BValueArray() {
        refValues = (BRefType[]) newArrayInstance(BRefType.class);
    }

    public BValueArray(BType type, int size) {
        if (size != -1) {
            this.size = maxArraySize = size;
        }

        if (type.getTag() == TypeTags.INT_TAG) {
            intValues = (long[]) newArrayInstance(Long.TYPE);
        } else if (type.getTag() == TypeTags.BOOLEAN_TAG) {
            booleanValues = (int[]) newArrayInstance(Integer.TYPE);
        } else if (type.getTag() == TypeTags.BYTE_TAG) {
            byteValues = (byte[]) newArrayInstance(Byte.TYPE);
        } else if (type.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
        } else if (type.getTag() == TypeTags.STRING_TAG) {
            stringValues = (String[]) newArrayInstance(String.class);
            Arrays.fill(stringValues, BLangConstants.STRING_EMPTY_VALUE);
        }

        super.arrayType = new BArrayType(type, size);
        this.elementType = type;
    }

    // -----------------------  get methods ----------------------------------------------------

    public BRefType<?> getRefValue(long index) {
        rangeCheckForGet(index, size);
        return refValues[(int) index];
    }

    public long getInt(long index) {
        rangeCheckForGet(index, size);
        return intValues[(int) index];
    }

    public int getBoolean(long index) {
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

    // ----------------------------  add methods --------------------------------------------------

    public void add(long index, BRefType<?> value) {
        handleFrozenArrayValue();
        prepareForAdd(index, refValues.length);
        refValues[(int) index] = value;
    }

    public void add(long index, long value) {
        handleFrozenArrayValue();
        prepareForAdd(index, intValues.length);
        intValues[(int) index] = value;
    }

    public void add(long index, int value) {
        if (elementType.getTag() == TypeTags.INT_TAG) {
            add(index, (long) value);
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

    public void append(BRefType<?> value) {
        add(size, value);
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {
        if (type.getTag() == TypeTags.TUPLE_TAG) {

            if (elementType != null && isBasicType(elementType)) {
                moveBasicTypeArrayToRefValueArray();
            }

            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    BType memberType = ((BTupleType) type).getTupleTypes().get(i);
                    if (memberType.getTag() == TypeTags.ANYDATA_TAG || memberType.getTag() == TypeTags.JSON_TAG) {
                        memberType = BVM.resolveMatchingTypeForUnion(arrayValues[i], memberType);
                        ((BTupleType) type).getTupleTypes().set(i, memberType);
                    }
                    arrayValues[i].stamp(memberType, unresolvedValues);
                }
            }
        } else if (type.getTag() == TypeTags.JSON_TAG) {

            if (elementType != null && isBasicType(elementType) && !isBasicType(type)) {
                moveBasicTypeArrayToRefValueArray();
                this.arrayType = new BArrayType(type);
                return;
            }

            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    arrayValues[i].stamp(BVM.resolveMatchingTypeForUnion(arrayValues[i], type), unresolvedValues);
                }
            }
            type = new BArrayType(type);
        } else if (type.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (BVM.checkIsLikeType(this, memberType, new ArrayList<>())) {
                    this.stamp(memberType, unresolvedValues);
                    type = memberType;
                    break;
                }
            }
        } else if (type.getTag() == TypeTags.ANYDATA_TAG) {
            type = BVM.resolveMatchingTypeForUnion(this, type);
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

            if (isBasicType(arrayElementType) && !isBasicType(elementType)) {
                moveRefValueArrayToBasicTypeArray(type, arrayElementType);
                return;
            }

            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    arrayValues[i].stamp(arrayElementType, unresolvedValues);
                }
            }
        }

        this.arrayType = type;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        if (elementType != null) {
            BValueArray valueArray = null;

            if (elementType.getTag() == TypeTags.INT_TAG) {
                valueArray = new BValueArray(Arrays.copyOf(intValues, intValues.length));
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                valueArray = new BValueArray(Arrays.copyOf(booleanValues, booleanValues.length));
            } else if (elementType.getTag() == TypeTags.BYTE_TAG) {
                valueArray = new BValueArray(Arrays.copyOf(byteValues, byteValues.length));
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                valueArray = new BValueArray(Arrays.copyOf(floatValues, floatValues.length));
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                valueArray = new BValueArray(Arrays.copyOf(stringValues, stringValues.length));
            }

            if (valueArray != null) {
                valueArray.size = this.size;
                refs.put(this, valueArray);
                return valueArray;
            }
        }

        BRefType<?>[] values = new BRefType[size];
        BValueArray refValueArray = new BValueArray(values, arrayType);
        refValueArray.size = this.size;
        refs.put(this, refValueArray);
        int bound = this.size;
        IntStream.range(0, bound)
                .forEach(i -> values[i] = this.refValues[i] == null ? null :
                        (BRefType<?>) this.refValues[i].copy(refs));
        return refValueArray;

    }

    @Override
    public String stringValue() {
        if (elementType != null) {
            StringJoiner sj = new StringJoiner(", ", "[", "]");
            if (elementType.getTag() == TypeTags.INT_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Long.toString(intValues[i]));
                }
                return sj.toString();
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                for (int i = 0; i < size; i++) {
                    sj.add(Boolean.toString(booleanValues[i] == 1));
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

        for (int i = 0; i < size; i++) {
            if (refValues[i] != null) {
                sj.add((refValues[i].getType().getTag() == TypeTags.STRING_TAG)
                        ? ("\"" + refValues[i] + "\"") : refValues[i].stringValue());
            }
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        if (elementType != null) {
            if (elementType.getTag() == TypeTags.INT_TAG) {
                return new BInteger(getInt(index));
            } else if (elementType.getTag() == TypeTags.BOOLEAN_TAG) {
                return new BBoolean(getBoolean(index) == 1);
            } else if (elementType.getTag() == TypeTags.BYTE_TAG) {
                return new BByte(getByte(index));
            } else if (elementType.getTag() == TypeTags.FLOAT_TAG) {
                return new BFloat(getFloat(index));
            } else if (elementType.getTag() == TypeTags.STRING_TAG) {
                return new BString(getString(index));
            } else {
                return getRefValue(index);
            }
        }
        return getRefValue(index);
    }

    public BRefType<?>[] getValues() {
        return refValues;
    }

    public byte[] getBytes() {
        return byteValues.clone();
    }

    @SuppressWarnings("unchecked")
    public String[] getStringArray() {
        return stringValues;
    }

    @Override
    public String toString() {
        return stringValue();
    }

    @Override
    public SerializableRefType serialize(SerializableState state) {

        return new SerializableBRefArray(this, state);
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
                outputStream.write(this.stringValue().getBytes(Charset.defaultCharset()));
            } catch (IOException e) {
                throw new BallerinaException("error occurred while serializing data", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(BVM.FreezeStatus freezeStatus) {
        if (!isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            return;
        }

        this.freezeStatus = freezeStatus;

        if (elementType == null || !(elementType.getTag() == TypeTags.INT_TAG ||
                elementType.getTag() == TypeTags.STRING_TAG || elementType.getTag() == TypeTags.BOOLEAN_TAG ||
                elementType.getTag() == TypeTags.FLOAT_TAG || elementType.getTag() == TypeTags.BYTE_TAG)) {
            for (int i = 0; i < this.size; i++) {
                if (this.getRefValue(i) != null) {
                    this.getRefValue(i).attemptFreeze(freezeStatus);
                }
            }
        }
    }

    @Override
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

    private void setArrayElementType(BType type) {
        super.arrayType = new BArrayType(type);
        this.elementType = type;
    }

    private String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
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
            if (freezeStatus.getState() != BVM.FreezeStatus.State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
        }
    }

    private boolean isBasicType(BType type) {
        return type == BTypes.typeString || type == BTypes.typeInt || type == BTypes.typeFloat ||
                type == BTypes.typeBoolean || type == BTypes.typeByte;
    }

    private void moveBasicTypeArrayToRefValueArray() {
        refValues = (BRefType[]) newArrayInstance(BRefType.class);
        if (elementType == BTypes.typeBoolean) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = new BBoolean(booleanValues[i] == 1);
            }
            booleanValues = null;
        }

        if (elementType == BTypes.typeInt) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = new BInteger(intValues[i]);
            }
            intValues = null;
        }

        if (elementType == BTypes.typeString) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = new BString(stringValues[i]);
            }
            stringValues = null;
        }

        if (elementType == BTypes.typeFloat) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = new BFloat(floatValues[i]);
            }
            floatValues = null;
        }

        if (elementType == BTypes.typeByte) {
            for (int i = 0; i < this.size(); i++) {
                refValues[i] = new BByte(byteValues[i]);
            }
            byteValues = null;
        }

        elementType = null;
    }

    private void moveRefValueArrayToBasicTypeArray(BType type, BType arrayElementType) {
        BRefType<?>[] arrayValues = this.getValues();

        if (arrayElementType.getTag() == TypeTags.INT_TAG) {
            intValues = (long[]) newArrayInstance(Long.TYPE);
            for (int i = 0; i < this.size(); i++) {
                intValues[i] = ((BInteger) arrayValues[i]).value();
            }
        }

        if (arrayElementType.getTag() == TypeTags.FLOAT_TAG) {
            floatValues = (double[]) newArrayInstance(Double.TYPE);
            for (int i = 0; i < this.size(); i++) {
                floatValues[i] = ((BFloat) arrayValues[i]).value();
            }
        }

        if (arrayElementType.getTag() == TypeTags.BOOLEAN_TAG) {
            booleanValues = (int[]) newArrayInstance(Integer.TYPE);
            for (int i = 0; i < this.size(); i++) {
                booleanValues[i] = ((BBoolean) arrayValues[i]).value() ? 1 : 0;
            }
        }

        if (arrayElementType.getTag() == TypeTags.STRING_TAG) {
            stringValues = (String[]) newArrayInstance(String.class);
            for (int i = 0; i < this.size(); i++) {
                stringValues[i] = arrayValues[i].stringValue();
            }
        }

        if (arrayElementType.getTag() == TypeTags.BYTE_TAG) {
            byteValues = (byte[]) newArrayInstance(Byte.TYPE);
            for (int i = 0; i < this.size(); i++) {
                byteValues[i] = ((BByte) arrayValues[i]).value();
            }
        }

        this.elementType = arrayElementType;
        this.arrayType = type;
        refValues = null;
    }
}
