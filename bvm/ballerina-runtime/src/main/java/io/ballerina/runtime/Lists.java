/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.values.ArrayValue;

/**
 * Common utility methods used for List manipulation.
 *
 * @since 0.995.0
 */
public class Lists {

    public static Object get(ArrayValue array, long index) {
        if (array.getType().getTag() != TypeTags.ARRAY_TAG) {
            return array.getRefValue(index);
        }

        switch (((BArrayType) array.getType()).getElementType().getTag()) {
            case TypeTags.BOOLEAN_TAG:
                return Boolean.valueOf(array.getBoolean(index));
            case TypeTags.BYTE_TAG:
                return new Long(array.getByte(index));
            case TypeTags.FLOAT_TAG:
                return new Double(array.getFloat(index));
            case TypeTags.DECIMAL_TAG:
                return array.getRefValue(index);
            case TypeTags.INT_TAG:
                return new Long((int) array.getInt(index));
            case TypeTags.STRING_TAG:
                return new String(array.getString(index));
            default:
                return array.getRefValue(index);
        }
    }
}
