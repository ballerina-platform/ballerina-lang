/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.jvm.util.exceptions;

import org.ballerinalang.jvm.api.BErrorCreator;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.values.BString;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for handler error messages.
 */
public class BLangExceptionHelper {
    private static ResourceBundle messageBundle = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());

    public static BError getRuntimeException(RuntimeErrors runtimeErrors, Object... params) {
        BString errorMsg = BStringUtils
                .fromString(MessageFormat.format(messageBundle.getString(runtimeErrors.getErrorMsgKey()), params));
        return BErrorCreator.createError(errorMsg);
    }

    public static BError getRuntimeException(BString reason, RuntimeErrors runtimeErrors, Object... params) {
        BString errorDetail = BStringUtils
                .fromString(MessageFormat.format(messageBundle.getString(runtimeErrors.getErrorMsgKey()), params));
        return BErrorCreator.createError(reason, errorDetail);
    }

    public static BString getErrorMessage(RuntimeErrors runtimeErrors, Object... params) {
        return BStringUtils.fromString(MessageFormat
                                              .format(messageBundle.getString(runtimeErrors.getErrorMsgKey()), params));
    }

    /**
     * Handle any json related exception.
     *
     * @param reason The reason to set as error reason
     * @param operation Operation that executed
     * @param e Throwable to handle
     * @return Error value
     */
    public static BError getJsonError(String reason, String operation, Throwable e) {
        // here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (e instanceof BallerinaException && ((BallerinaException) e).getDetail() != null) {
            return BErrorCreator.createError(BStringUtils.fromString(reason),
                                             BStringUtils.fromString("Failed to " + operation + ": " +
                                                                            ((BallerinaException) e).getDetail()));
        } else if (e instanceof BLangFreezeException) {
            return BErrorCreator.createError(BStringUtils.fromString(reason),
                                             BStringUtils.fromString("Failed to " + operation + ": " +
                                                                            ((BLangFreezeException) e).getDetail()));
        } else if (e.getCause() != null) {
            return BErrorCreator.createError(BStringUtils.fromString(reason),
                                             BStringUtils.fromString(
                                                     "Failed to " + operation + ": " + e.getCause().getMessage()));
        } else {
            return BErrorCreator.createError(BStringUtils.fromString(reason),
                                             BStringUtils
                                                     .fromString("Failed to " + operation + ": " + e.getMessage()));
        }
    }

    /*
     * XML error handling methods.
     */

    /**
     * Handle invalid/malformed xpath exceptions.
     *
     * @param operation Operation that executed
     * @param e Exception to handle
     */
    public static void handleInvalidXPath(String operation, Exception e) {
        throw BErrorCreator
                .createError(BStringUtils.fromString("Failed to " + operation + ". Invalid xpath: " + e.getMessage()));
    }

    /**
     * Handle any xpath related exception.
     *
     * @param operation Operation that executed
     * @param e Throwable to handle
     */
    public static void handleXMLException(String operation, Throwable e) {
        // here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (e instanceof BallerinaException && ((BallerinaException) e).getDetail() != null) {
            throw BErrorCreator.createError(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                            BStringUtils.fromString("Failed to " + operation + ": " +
                                                                           ((BallerinaException) e).getDetail()));
        } else if (e instanceof BLangFreezeException) {
            throw BErrorCreator.createError(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                            BStringUtils.fromString("Failed to " + operation + ": " +
                                                                           ((BLangFreezeException) e).getDetail()));
        } else if (e.getCause() != null) {
            throw BErrorCreator.createError(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                            BStringUtils.fromString(
                                                    "Failed to " + operation + ": " + e.getCause().getMessage()));
        } else {
            throw BErrorCreator.createError(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                            BStringUtils.fromString("Failed to " + operation + ": " + e.getMessage()));
        }
    }
}
