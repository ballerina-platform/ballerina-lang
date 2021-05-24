/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CONVERSION_ERROR;

/**
 * Create a record value from the provided value map.
 *
 * @since 2.0
 */
public class CreateRecordValue {
    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object createRecordFromMap(BMap<?, ?> value, BTypedesc t) {
        Type describingType = t.getDescribingType();
        if (value == null) {
            if (describingType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, describingType));
        }
        List<Type> convertibleTypes = TypeConverter.getConvertibleTypes(value, describingType);
        if (convertibleTypes.isEmpty()) {
            throw createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION,
                            TypeChecker.getType(value), describingType));
        } else if (convertibleTypes.size() > 1) {
            throw createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION,
                            TypeChecker.getType(value), describingType)
                            .concat(StringUtils.fromString(": ".concat(AMBIGUOUS_TARGET))));
        }

        BMap<BString, BObject> valMap = (BMap<BString, BObject>) value;
        Map<String, Object> recVals = new HashMap<>();
        valMap.entrySet().stream().forEach(entry -> recVals.put(entry.getKey().getValue(), entry.getValue()));

        return ValueCreator.createRecordValue(describingType.getPackage(), describingType.getName(), recVals);
    }
}
