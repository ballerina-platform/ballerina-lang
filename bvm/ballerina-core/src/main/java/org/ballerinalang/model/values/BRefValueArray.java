/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBRefArray;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.ballerinalang.model.util.FreezeUtils.handleInvalidUpdate;
import static org.ballerinalang.model.util.FreezeUtils.isOpenForFreeze;

/**
 * @since 0.87
 */
public class BRefValueArray extends BNewArray implements Serializable {

    BRefType<?>[] values;

    public BRefValueArray(BRefType<?>[] values, BType type) {
        this.values = values;
        super.arrayType = type;
        this.size = values.length;
    }

    public BRefValueArray(BType type) {
        super.arrayType = type;
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType arrayType = (BArrayType) type;
            if (arrayType.getState() == BArrayState.CLOSED_SEALED) {
                this.size = maxArraySize = arrayType.getSize();
            }
            values = (BRefType[]) newArrayInstance(BRefType.class);
            Arrays.fill(values, arrayType.getElementType().getZeroValue());
        } else if (type.getTag() == TypeTags.TUPLE_TAG) {
            BTupleType tupleType = (BTupleType) type;
            this.size = maxArraySize = tupleType.getTupleTypes().size();
            values = (BRefType[]) newArrayInstance(BRefType.class);
            AtomicInteger counter = new AtomicInteger(0);
            tupleType.getTupleTypes().forEach(memType -> values[counter.getAndIncrement()] = memType.getEmptyValue());
        } else {
            values = (BRefType[]) newArrayInstance(BRefType.class);
            Arrays.fill(values, type.getEmptyValue());
        }
    }

    public BRefValueArray() {
        values = (BRefType[]) newArrayInstance(BRefType.class);
    }

    public void add(long index, BRefType<?> value) {
        synchronized (this) {
            if (freezeStatus.getState() != CPU.FreezeStatus.State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
        }

        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    public void append(BRefType<?> value) {
        add(size, value);
    }

    public BRefType<?> get(long index) {
        rangeCheckForGet(index, size);
        return values[(int) index];
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void stamp(BType type) {
        if (type.getTag() == TypeTags.TUPLE_TAG) {
            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    arrayValues[i].stamp(((BTupleType) type).getTupleTypes().get(i));
                }
            }

        } else if (type.getTag() == TypeTags.JSON_TAG) {
            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    arrayValues[i].stamp(type);
                }
            }
        } else if (type.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (CPU.checkIsLikeType(this, memberType)) {
                    this.stamp(memberType);
                    type = memberType;
                    break;
                }
            }
        } else if (type.getTag() != TypeTags.ANYDATA_TAG) {
            BType arrayElementType = ((BArrayType) type).getElementType();
            BRefType<?>[] arrayValues = this.getValues();
            for (int i = 0; i < this.size(); i++) {
                if (arrayValues[i] != null) {
                    arrayValues[i].stamp(arrayElementType);
                }
            }
        }

        this.arrayType = type;
    }

    @Override
    public void grow(int newLength) {
        values = Arrays.copyOf(values, newLength);
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        BRefType<?>[] values = new BRefType[size];
        BRefValueArray refValueArray = new BRefValueArray(values, arrayType);
        refValueArray.size = this.size;
        refs.put(this, refValueArray);
        int bound = this.size;
        IntStream.range(0, bound)
                .forEach(i -> values[i] = this.values[i] == null ? null : (BRefType<?>) this.values[i].copy(refs));
        return refValueArray;
    }

    @Override
    public String stringValue() {
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
            if (values[i] != null) {
                sj.add((values[i].getType().getTag() == TypeTags.STRING_TAG)
                        ? ("\"" + values[i] + "\"") : values[i].stringValue());
            }
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        return get(index);
    }

    public BRefType<?>[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return stringValue();
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

    @Override
    public SerializableRefType serialize(SerializableState state) {
        return new SerializableBRefArray(this, state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(CPU.FreezeStatus freezeStatus) {
        if (isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            for (int i = 0; i < this.size; i++) {
                if (this.get(i) != null) {
                    this.get(i).attemptFreeze(freezeStatus);
                }
            }
        }
    }
}
