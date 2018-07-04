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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * @since 0.87
 */
public class BBooleanArray extends BNewArray {

    private static BType arrayType = new BArrayType(BTypes.typeBoolean);

    private int[] values;

    public BBooleanArray(int[] values) {
        this.values = values;
        this.size = values.length;
    }

    public BBooleanArray() {
        values = (int[]) newArrayInstance(Integer.TYPE);
    }

    public void add(long index, int value) {
        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    public int get(long index) {
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
        BBooleanArray booleanArray = new BBooleanArray(Arrays.copyOf(values, values.length));
        booleanArray.size = this.size;
        return booleanArray;
    }
    
    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) {
            sj.add(Boolean.toString(values[i] == 1));
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        return new BBoolean(get(index) == 1);
    }
}
