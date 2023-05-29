/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.ValueConverter;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.errors.ErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;

/**
 * Extern function lang.values:fromJsonWithType.
 *
 * @since 2.0
 */
public class FromJsonWithType {

    private FromJsonWithType() {}

    public static Object fromJsonWithType(Object value, BTypedesc t) {
        try {
            return ValueConverter.convert(value, t);
        } catch (BError e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, (BMap<BString, Object>) e.getDetails());
        }
    }

    // TODO: remove this after fixing standard libraries to use the runtime API
    /**
     * @deprecated use {@link ValueUtils#convert(Object, Type)} instead.
     */
    @Deprecated
    public static Object convert(Object value, Type targetType) {
        try {
            return ValueConverter.convert(value, targetType);
        } catch (BError e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, (BMap<BString, Object>) e.getDetails());
        }
    }

    // TODO: remove this after fixing standard libraries to use the runtime API
    /**
     * @deprecated use {@link ValueUtils#convert(Object, Type)} instead.
     */
    @Deprecated
    public static Object convert(Object value, Type targetType, BTypedesc t) {
        try {
            return ValueConverter.convert(value, targetType);
        } catch (BError e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, (BMap<BString, Object>) e.getDetails());
        }
    }
}
