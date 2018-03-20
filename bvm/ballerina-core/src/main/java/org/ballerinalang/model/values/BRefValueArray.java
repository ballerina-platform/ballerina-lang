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

import org.ballerinalang.model.types.BType;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * @since 0.87
 */
public class BRefValueArray extends BNewArray {

    private BType arrayType;

    private BRefType[] values;

    public BRefValueArray(BRefType[] values, BType type) {
        this.values = values;
        this.arrayType = type;
        this.size = values.length;
    }

    public BRefValueArray(BType type) {
        this.arrayType = type;
        values = (BRefType[]) newArrayInstance(BRefType.class);
        Arrays.fill(values, type.getEmptyValue());
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
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) {
            sj.add(values[i] == null ? "null" : values[i].stringValue());
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        return get(index);
    }
}
