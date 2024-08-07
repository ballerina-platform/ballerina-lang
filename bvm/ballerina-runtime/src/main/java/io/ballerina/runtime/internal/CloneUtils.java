/*
 * Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BRefValue;

import java.util.HashMap;
import java.util.List;

import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_END;
import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_SEPARATOR;
import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_START;
import static io.ballerina.runtime.internal.TypeConverter.MAX_CONVERSION_ERROR_COUNT;

/**
 * This class contains the functions related to cloning Ballerina values.
 *
 * @since 1.0.0
 */
public class CloneUtils {

    static final String NEWLINE_WITH_TABS = "\n\t\t";
    static final String TWO_SPACES = "  ";

    private CloneUtils() {}

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

        if (!(value instanceof BRefValue refValue)) {
            return value;
        }

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

        if (!(value instanceof BRefValue refValue)) {
            return value;
        }

        return refValue.frozenCopy(new HashMap<>());
    }

    public static BError createConversionError(Object value, Type targetType, List<String> errors) {
        if (errors.isEmpty()) {
            return ErrorUtils.createConversionError(value, targetType);
        }
        return ErrorUtils.createConversionError(value, targetType, getErrorMessage(errors, MAX_CONVERSION_ERROR_COUNT));
    }

    static String getErrorMessage(List<String> errors, int maxErrorCount) {
        StringBuilder errorMsg = new StringBuilder();
        int totalErrorCount = errors.size();
        int tabs = 0;
        for (int i = 0; i < Math.min(totalErrorCount, maxErrorCount); i++) {
            String err = errors.get(i);
            // intentionally comparing whether the two String objects are the same
            if (err == ERROR_MESSAGE_UNION_START) {
                errorMsg.append(NEWLINE_WITH_TABS).append(TWO_SPACES.repeat(tabs++)).append(ERROR_MESSAGE_UNION_START);
            } else if (err == ERROR_MESSAGE_UNION_END) {
                errorMsg.append(NEWLINE_WITH_TABS).append(TWO_SPACES.repeat(--tabs)).append(ERROR_MESSAGE_UNION_END);
            } else if (err == ERROR_MESSAGE_UNION_SEPARATOR) {
                errorMsg.append(NEWLINE_WITH_TABS).append(TWO_SPACES.repeat(tabs - 1))
                        .append(ERROR_MESSAGE_UNION_SEPARATOR);
            } else {
                errorMsg.append(NEWLINE_WITH_TABS).append(TWO_SPACES.repeat(tabs)).append(err);
            }
        }
        if (totalErrorCount > maxErrorCount) {
            errorMsg.append(NEWLINE_WITH_TABS + "...");
        }
        return errorMsg.toString();
    }
}
