/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BValueArray;

/**
 * Factory methods to create BValueArray from Java arrays.
 *
 * @since 0.982.0
 */
class BValueArrays {
    private static final BArrayType B_INT_ARRAY_TYPE = new BArrayType(BTypes.typeInt);
    private static final BArrayType B_FLOAT_ARRAY_TYPE = new BArrayType(BTypes.typeFloat);
    private final BValueSerializer serializer;

    BValueArrays(BValueSerializer serializer) {
        this.serializer = serializer;
    }

    BValueArray from(int[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }

        return new BValueArray(backing, B_INT_ARRAY_TYPE);
    }

    BValueArray from(long[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_INT_ARRAY_TYPE);
    }

    BValueArray from(double[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_FLOAT_ARRAY_TYPE);
    }

    private <T> BRefType[] getBRefArray(T[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return backing;
    }

    BValueArray from(Object[] array) {
        BRefType[] backing = getBRefArray(array);
        return new BValueArray(backing, new BArrayType(BTypes.typeAny));
    }

    public BRefType from(char[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_INT_ARRAY_TYPE);
    }

    public BRefType from(byte[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_INT_ARRAY_TYPE);
    }

    public BRefType from(short[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_INT_ARRAY_TYPE);
    }

    public BRefType from(float[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BValueArray(backing, B_FLOAT_ARRAY_TYPE);
    }
}
