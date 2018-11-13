/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.model.util;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;

import java.math.BigDecimal;

/**
 * Common utility methods used for List manipulation.
 *
 * @since 0.982.0
 */
public class ListUtils {

    public static BRefType<?> execListGetOperation(BNewArray array, long index) {
        if (array.getType().getTag() != TypeTags.ARRAY_TAG) {
            BRefValueArray bRefValueArray = (BRefValueArray) array;
            return bRefValueArray.get(index);
        }

        switch (((BArrayType) array.getType()).getElementType().getTag()) {
            case TypeTags.BOOLEAN_TAG:
                BBooleanArray bBooleanArray = (BBooleanArray) array;
                int i = bBooleanArray.get(index);
                return i == 0 ? new BBoolean(false) : new BBoolean(true);
            case TypeTags.BYTE_TAG:
                BByteArray bByteArray = (BByteArray) array;
                return new BByte(bByteArray.get(index));
            case TypeTags.FLOAT_TAG:
                BFloatArray bFloatArray = (BFloatArray) array;
                return new BFloat(bFloatArray.get(index));
            case TypeTags.DECIMAL_TAG:
                BRefValueArray bDecimalArray = (BRefValueArray) array;
                return new BDecimal(new BigDecimal(bDecimalArray.get(index).stringValue()));
            case TypeTags.INT_TAG:
                BIntArray bIntArray = (BIntArray) array;
                return new BInteger(bIntArray.get(index));
            case TypeTags.STRING_TAG:
                BStringArray bStringArray = (BStringArray) array;
                return new BString(bStringArray.get(index));
            default:
                BRefValueArray bRefValueArray = (BRefValueArray) array;
                return bRefValueArray.get(index);
        }
    }

    public static void execListAddOperation(BNewArray array, long index, BRefType refType) {
        if (array.getType().getTag() != TypeTags.ARRAY_TAG) {
            BRefValueArray bRefValueArray = (BRefValueArray) array;
            bRefValueArray.add(index, refType);
            return;
        }

        switch (((BArrayType) array.getType()).getElementType().getTag()) {
            case TypeTags.BOOLEAN_TAG:
                BBooleanArray bBooleanArray = (BBooleanArray) array;
                bBooleanArray.add(index, ((BBoolean) refType).value() ? 1 : 0);
                return;
            case TypeTags.BYTE_TAG:
                BByteArray bByteArray = (BByteArray) array;
                bByteArray.add(index, (byte) refType.value());
                return;
            case TypeTags.FLOAT_TAG:
                BFloatArray bFloatArray = (BFloatArray) array;
                bFloatArray.add(index, (double) refType.value());
                return;
            case TypeTags.INT_TAG:
                BIntArray bIntArray = (BIntArray) array;
                bIntArray.add(index, (long) refType.value());
                return;
            case TypeTags.STRING_TAG:
                BStringArray bStringArray = (BStringArray) array;
                bStringArray.add(index, (String) refType.value());
                return;
            default:
                BRefValueArray bRefValueArray = (BRefValueArray) array;
                bRefValueArray.add(index, refType);
        }
    }
}
