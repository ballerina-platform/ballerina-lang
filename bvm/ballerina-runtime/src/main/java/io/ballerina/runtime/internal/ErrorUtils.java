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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrorType;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ERROR_DETAIL;
import static io.ballerina.runtime.api.creators.ErrorCreator.createError;

/**
 * This class contains internal methods used by codegen and runtime classes to handle errors.
 *
 * @since 2.0.0
 */

public class ErrorUtils {

    private static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");
    private static final BString ERROR_CAUSE_FIELD = StringUtils.fromString("cause");
    private static final BString NULL_REF_EXCEPTION = StringUtils.fromString("NullReferenceException");

    private static final String BALLERINA_ORG_PREFIX = "{ballerina/";
    private static final String CLOSING_CURLY_BRACE = "}";
    private static final String RECORD_TYPE_NAME = "Detail";

    private static ResourceBundle messageBundle = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());

    /**
     * Create balleria error using java exception for interop.
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
        BMap<BString, Object> detailMap = new MapValueImpl(TYPE_ERROR_DETAIL, initialValues);

        return (ErrorValue) createError(StringUtils.fromString(e.getClass().getName()), detailMap);
    }

    public static Object handleResourceError(Object returnValue) {
        if (returnValue instanceof BError) {
            throw (BError) returnValue;
        }
        return returnValue;
    }

    public static ErrorValue trapError(Throwable throwable) {
        // Used to trap and create error value for non error value exceptions. At the moment, we can trap
        // stack overflow exceptions in addition to error value.
        // In the future, if we need to trap more exception types, we need to check instance of each exception and
        // handle accordingly.
        BError error = createError(BallerinaErrorReasons.STACK_OVERFLOW_ERROR);
        error.setStackTrace(throwable.getStackTrace());
        return (ErrorValue) error;
    }

    public static ErrorValue createCancelledFutureError() {
        return (ErrorValue) createError(BallerinaErrorReasons.FUTURE_CANCELLED);
    }

    public static BError createRuntimeError(Module module, RuntimeErrorType errorType, Object... params) {
        BString errorMessage = getErrorMessage(errorType, params);
        BString modulePrefixedErrorName = getModulePrefixedErrorName(module, errorType);
        BError errorCause = getErrorCause(errorMessage);
        BMap<BString, Object> detail = getErrorDetail(errorMessage);
        try {
            return createError(module, errorType.getErrorName(), modulePrefixedErrorName, null, detail);
        } catch (Exception e) {
            // This should never happen unless ErrorCreator itself has a bug
            e.addSuppressed(errorCause);
            throw e;
        }
    }

    public static BString getErrorMessage(RuntimeErrorType runtimeErrors, Object... params) {
        return StringUtils.fromString(MessageFormat
                .format(messageBundle.getString(runtimeErrors.getErrorMsgKey()), params));
    }

    private static BError getErrorCause(BString errorMessage) {
        return ErrorCreator.createError(errorMessage);
    }

    public static BMap<BString, Object> getErrorDetail(BString errMessage) {
        BMap<BString, Object> errDetail = ValueCreator.createRecordValue(RuntimeConstants.BALLERINA_LANG_ERROR_PKG_ID,
                RECORD_TYPE_NAME);
        errDetail.put(ERROR_MESSAGE_FIELD, errMessage);
        return errDetail;
    }

    public static BString getModulePrefixedErrorName(Module module, RuntimeErrorType errorType) {
        return StringUtils.fromString(BALLERINA_ORG_PREFIX.concat(module.getName())
                .concat(CLOSING_CURLY_BRACE).concat(errorType.getErrorName()));
    }
}

