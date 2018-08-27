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
package org.ballerinalang.model.util.serializer;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;

/**
 * Factory methods to create BRefValueArray from Java arrays.
 *
 * @since 0.98.1
 */
class BRefValueArrays {
    private static final BArrayType B_INT_ARRAY_TYPE = new BArrayType(BTypes.typeInt);
    private static final BArrayType B_FLOAT_ARRAY_TYPE = new BArrayType(BTypes.typeFloat);
    private static final BArrayType B_STRING_ARRAY_TYPE = new BArrayType(BTypes.typeString);
    private static final BArrayType B_BYTE_ARRAY_TYPE = new BArrayType(BTypes.typeByte);
    private final BValueSerializer serializer;

    BRefValueArrays(BValueSerializer serializer) {
        this.serializer = serializer;
    }

    BRefValueArray from(int[] array) {
        if (array == null) {
            return null;
        }
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }

        return new BRefValueArray(backing, B_INT_ARRAY_TYPE);
    }

    BRefValueArray from(long[] array) {
        if (array == null) {
            return null;
        }
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BRefValueArray(backing, B_INT_ARRAY_TYPE);
    }

    BRefValueArray from(double[] array) {
        if (array == null) {
            return null;
        }
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BRefValueArray(backing, B_FLOAT_ARRAY_TYPE);
    }

    BRefValueArray from(String[] array) {
        if (array == null) {
            return null;
        }
        BRefType[] backing = getBRefArray(array);
        return new BRefValueArray(backing, B_STRING_ARRAY_TYPE);
    }

    private <T> BRefType[] getBRefArray(T[] array) {
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return backing;
    }

    BRefValueArray from(Byte[] array) {
        if (array == null) {
            return null;
        }
        BRefType[] backing = new BRefType[array.length];

        for (int i = 0; i < array.length; i++) {
            backing[i] = (BRefType) serializer.toBValue(array[i], null);
        }
        return new BRefValueArray(backing, B_BYTE_ARRAY_TYPE);
    }
}
