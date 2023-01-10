/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package io.ballerina.runtime.api.utils;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.ValueConverter;

/**
 * This class provides APIs needed for the type conversion in Ballerina.
 *
 * @since 2201.4.0
 */
public class ValueUtils {

    private ValueUtils() {}

    /**
     * Converts the given value of any subtype of anydata to any subtype of anydata given by the bTypedesc.
     *
     * @param value     value to be converted
     * @param bTypedesc typedesc of the target type
     * @return          converted value of the type defined by the bTypedesc. BError if the conversion fails.
     */
    public static Object convert(Object value, BTypedesc bTypedesc) {
        return ValueConverter.convert(value, bTypedesc);
    }

    /**
     * Converts the given value of any subtype of anydata to any subtype of anydata given by the target type.
     *
     * @param value         value to be converted
     * @param targetType    target type
     * @return              converted value of the target type. BError if the conversion fails.
     */
    public static Object convert(Object value, Type targetType) {
        return ValueConverter.convert(value, targetType, null);
    }
}
