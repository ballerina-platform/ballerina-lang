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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.values.ArrayValue;

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

        return switch (TypeUtils.getImpliedType(((BArrayType) array.getType()).getElementType()).getTag()) {
            case TypeTags.BOOLEAN_TAG -> array.getBoolean(index);
            case TypeTags.BYTE_TAG -> (long) array.getByte(index);
            case TypeTags.FLOAT_TAG -> array.getFloat(index);
            case TypeTags.DECIMAL_TAG -> array.getRefValue(index);
            case TypeTags.INT_TAG -> array.getInt(index);
            case TypeTags.STRING_TAG -> array.getString(index);
            default -> array.getRefValue(index);
        };
    }
}
