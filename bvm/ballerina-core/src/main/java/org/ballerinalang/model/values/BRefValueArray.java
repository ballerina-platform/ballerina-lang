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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBRefArray;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 0.87
 */
public class BRefValueArray extends BNewArray implements Serializable {

    private BRefType[] values;

    public BRefValueArray(BRefType[] values, BType type) {
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
            Arrays.fill(values, type.getZeroValue());
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

    public void add(long index, BRefType value) {
        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    public BRefType get(long index) {
        rangeCheckForGet(index, size);
        return values[(int) index];
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void grow(int newLength) {
        values = Arrays.copyOf(values, newLength);
    }

    @Override
    public BValue copy() {
        BRefValueArray refValueArray = new BRefValueArray(Arrays.copyOf(values, values.length), arrayType);
        refValueArray.size = this.size;
        return refValueArray;
    }
    
    @Override
    public String stringValue() {
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

    @Override
    public SerializableRefType serialize(SerializableState state) {
        return new SerializableBRefArray(this, state);
    }
}
