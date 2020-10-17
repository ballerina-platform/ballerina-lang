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
 */

package org.ballerinalang.langlib.value;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;

import java.util.HashMap;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * This class contains the functions related to cloning Ballerina values.
 *
 * @since 1.0.0
 */
public class CloneUtils {

    private static final BString NULL_REF_EXCEPTION = StringUtils.fromString("NullReferenceException");

    /**
     * Returns a clone of `value`. A clone is a deep copy that does not copy immutable subtrees.A clone can therefore
     * safely be used concurrently with the original. It corresponds to the Clone(v) abstract operation, defined in
     * the Ballerina Language Specification.
     * @param value The value on which the function is invoked
     * @return String value of the value
     */
    public static Object cloneValue(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof BRefValue)) {
            return value;
        }

        BRefValue refValue = (BRefValue) value;
        return refValue.copy(new HashMap<>());
    }

    /**
     * Returns a clone of `value` which is immutable. A clone is a deep copy that does not copy immutable subtrees.
     * A clone can therefore safely be used concurrently with the original. It corresponds to the ImmutableClone(v)
     * abstract operation, defined in the Ballerina Language Specification.
     * @param value The value on which the function is invoked
     * @return String value of the value
     */
    public static Object cloneReadOnly(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof BRefValue)) {
            return value;
        }

        BRefValue refValue = (BRefValue) value;
        return refValue.frozenCopy(new HashMap<>());
    }

    public static BError createConversionError(Object inputValue, Type targetType) {
        return createError(BALLERINA_PREFIXED_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType));
    }
}
