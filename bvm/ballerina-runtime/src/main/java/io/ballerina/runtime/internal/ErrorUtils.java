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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_LANG_ERROR_PKG_ID;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FLOAT_LANG_LIB;
import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.errors.ErrorCodes.INCOMPATIBLE_CONVERT_OPERATION;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;

/**
 * This class contains internal methods used by codegen and runtime classes to handle errors.
 *
 * @since 2.0.0
 */

public class ErrorUtils {

    private static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");
    private static final BString ERROR_CAUSE_FIELD = StringUtils.fromString("cause");

    private ErrorUtils() {}

    /**
     * Create Ballerina error using java exception for interop.
     *
     * @param e java exception
     * @return ballerina error
     */
    public static ErrorValue createInteropError(Throwable e) {
        MappingInitialValueEntry[] initialValues;
        String message = e.getMessage();
        Throwable cause = e.getCause();
        if (message != null && cause != null) {
            initialValues = new MappingInitialValueEntry[2];
            initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD,
                                                                            StringUtils.fromString(message));
            initialValues[1] = new MappingInitialValueEntry.KeyValueEntry(ERROR_CAUSE_FIELD, createError(StringUtils.
                    fromString(cause.getClass().getName()), StringUtils.fromString(cause.getMessage())));

        } else if (message != null || cause != null) {
            initialValues = new MappingInitialValueEntry[1];
            if (message != null) {
                initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD,
                        StringUtils.fromString(message));
            } else {
                initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_CAUSE_FIELD, createError(StringUtils
                                  .fromString(cause.getClass().getName()), StringUtils.fromString(cause.getMessage())));
            }
        } else {
            initialValues = new MappingInitialValueEntry[0];
        }
        BMap<BString, Object> detailMap = new MapValueImpl(PredefinedTypes.TYPE_ERROR_DETAIL, initialValues);

        return (ErrorValue) createError(StringUtils.fromString(e.getClass().getName()), detailMap);
    }

    public static Object handleResourceError(Object returnValue) {
        if (returnValue instanceof BError error) {
            throw error;
        }
        return returnValue;
    }

    public static ErrorValue trapError(Throwable throwable) {
        // Used to trap and create error value for non error value exceptions. At the moment, we can trap
        // stack overflow exceptions in addition to error value.
        // In the future, if we need to trap more exception types, we need to check instance of each exception and
        // handle accordingly.
        BError error = createError(ErrorReasons.STACK_OVERFLOW_ERROR);
        error.setStackTrace(throwable.getStackTrace());
        return (ErrorValue) error;
    }

    public static ErrorValue createCancelledFutureError() {
        return (ErrorValue) createError(ErrorReasons.FUTURE_CANCELLED);
    }

    public static BError createIntOverflowError() {
        throw createError(ErrorReasons.NUMBER_OVERFLOW,
                ErrorHelper.getErrorDetails(ErrorCodes.INT_RANGE_OVERFLOW_ERROR));
    }

    public static BError createIntOverflowError(BString errorMsg) {
        throw createError(errorMsg, ErrorHelper.getErrorDetails(ErrorCodes.INT_RANGE_OVERFLOW_ERROR));
    }

    public static BError createTypeCastError(Object sourceVal, Type targetType) {
        throw createError(ErrorReasons.TYPE_CAST_ERROR,
                          ErrorHelper.getErrorDetails(ErrorCodes.TYPE_CAST_ERROR,
                                                               TypeChecker.getType(sourceVal), targetType));

    }

    public static BError createTypeCastError(Object sourceVal, Type targetType, String detailMessage) {
        return createError(ErrorReasons.TYPE_CAST_ERROR, ErrorHelper.getErrorMessage(
                ErrorCodes.TYPE_CAST_ERROR, TypeChecker.getType(sourceVal), targetType)
                .concat(StringUtils.fromString(": " + detailMessage)));
    }

    public static BError createBToJTypeCastError(Object sourceVal, String targetType) {
        throw createError(ErrorReasons.TYPE_CAST_ERROR,
                          ErrorHelper.getErrorDetails(ErrorCodes.J_TYPE_CAST_ERROR,
                                                               TypeChecker.getType(sourceVal), targetType));
    }

    public static BError createJToBTypeCastError(Object sourceVal, Type targetType) {
        throw createError(ErrorHelper.
                getErrorMessage(ErrorCodes.TYPE_ASSIGNABLE_ERROR, sourceVal, targetType));
    }

    public static BError createJToBTypeCastError(Object value) {
        throw createError(ErrorHelper.
                getErrorMessage(ErrorCodes.J_TYPE_ASSIGNABLE_ERROR, value));
    }

    public static BError createNumericConversionError(Object inputValue, Type targetType) {
        throw createError(ErrorReasons.NUMBER_CONVERSION_ERROR,
                          ErrorHelper.getErrorDetails(
                                  ErrorCodes.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                  TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static BError createNumericConversionError(Object inputValue, Type inputType, Type targetType) {
        throw createError(ErrorReasons.NUMBER_CONVERSION_ERROR,
                ErrorHelper.getErrorDetails(ErrorCodes.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                        inputType, inputValue, targetType));
    }

    public static BError createOperationNotSupportedError(Type lhsType, Type rhsType) {
        throw createError(ErrorReasons.OPERATION_NOT_SUPPORTED_ERROR,
                ErrorHelper.getErrorDetails(ErrorCodes.UNSUPPORTED_COMPARISON_OPERATION, lhsType, rhsType));
    }

    public static BError createUnorderedTypesError(Object lhsValue, Object rhsValue) {
        throw createError(ErrorReasons.UNORDERED_TYPES_ERROR, ErrorHelper.getErrorDetails(
                ErrorCodes.UNORDERED_TYPES_IN_COMPARISON, lhsValue, rhsValue));
    }

    public static BError createConversionError(Object inputValue, Type targetType) {
        return createError(ErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                ErrorHelper.getErrorDetails(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType));
    }

    public static BError createConversionError(Object inputValue, Type targetType, String detailMessage) {
        return createError(ErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                ErrorHelper.getErrorMessage(
                INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                .concat(StringUtils.fromString(": " + detailMessage)));
    }

    public static BError createInvalidDecimalError(String value) {
        throw createError(ErrorReasons.UNSUPPORTED_DECIMAL_ERROR,
                ErrorHelper.getErrorDetails(ErrorCodes.UNSUPPORTED_DECIMAL_VALUE, value));
    }

    public static BError createInvalidFractionDigitsError() {
        throw createError(getModulePrefixedReason(FLOAT_LANG_LIB,
                ErrorReasons.INVALID_FRACTION_DIGITS_ERROR),
                ErrorHelper.getErrorDetails(ErrorCodes.INVALID_FRACTION_DIGITS));
    }

    public static BError createNoMessageError(String chnlName) {
        String[] splitWorkers = chnlName.split(":")[0].split("->");
        return createError(BALLERINA_LANG_ERROR_PKG_ID, "NoMessage", ErrorReasons.NO_MESSAGE_ERROR,
                null, ErrorHelper.getErrorDetails(ErrorCodes.NO_MESSAGE_ERROR,
                        StringUtils.fromString(splitWorkers[0]), StringUtils.fromString(splitWorkers[1])));
    }
}
