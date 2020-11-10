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

package org.ballerinalang.core.model.util;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BNewArray;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValueArray;

import java.math.BigDecimal;

/**
 * Common utility methods used for List manipulation.
 *
 * @since 0.982.0
 */
public class ListUtils {

    public static BRefType<?> execListGetOperation(BNewArray array, long index) {
        if (array.getType().getTag() != TypeTags.ARRAY_TAG) {
            BValueArray bRefValueArray = (BValueArray) array;
            return bRefValueArray.getRefValue(index);
        }

        switch (((BArrayType) array.getType()).getElementType().getTag()) {
            case TypeTags.BOOLEAN_TAG:
                BValueArray bBooleanArray = (BValueArray) array;
                int i = bBooleanArray.getBoolean(index);
                return i == 0 ? new BBoolean(false) : new BBoolean(true);
            case TypeTags.BYTE_TAG:
                BValueArray bByteArray = (BValueArray) array;
                return new BByte(bByteArray.getByte(index));
            case TypeTags.FLOAT_TAG:
                BValueArray bFloatArray = (BValueArray) array;
                return new BFloat(bFloatArray.getFloat(index));
            case TypeTags.DECIMAL_TAG:
                BValueArray bDecimalArray = (BValueArray) array;
                return new BDecimal(new BigDecimal(bDecimalArray.getRefValue(index).stringValue()));
            case TypeTags.INT_TAG:
                BValueArray bIntArray = (BValueArray) array;
                return new BInteger(bIntArray.getInt(index));
            case TypeTags.STRING_TAG:
                BValueArray bStringArray = (BValueArray) array;
                return new BString(bStringArray.getString(index));
            default:
                BValueArray bRefValueArray = (BValueArray) array;
                return bRefValueArray.getRefValue(index);
        }
    }

    public static void execListAddOperation(BNewArray array, long index, BRefType refType) {
        if (array.getType().getTag() != TypeTags.ARRAY_TAG) {
            BValueArray bRefValueArray = (BValueArray) array;
            bRefValueArray.add(index, refType);
            return;
        }

        BValueArray bValueArray = (BValueArray) array;
        switch (((BArrayType) array.getType()).getElementType().getTag()) {
            case TypeTags.BOOLEAN_TAG:
                bValueArray.add(index, ((BBoolean) refType).value() ? 1 : 0);
                return;
            case TypeTags.BYTE_TAG:
                bValueArray.add(index, (byte) refType.value());
                return;
            case TypeTags.FLOAT_TAG:
                bValueArray.add(index, (double) refType.value());
                return;
            case TypeTags.INT_TAG:
                bValueArray.add(index, (long) refType.value());
                return;
            case TypeTags.STRING_TAG:
                bValueArray.add(index, (String) refType.value());
                return;
            default:
                bValueArray.add(index, refType);
        }
    }
}
