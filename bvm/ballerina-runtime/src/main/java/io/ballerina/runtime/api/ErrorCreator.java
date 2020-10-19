/*
*   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package io.ballerina.runtime.api;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BErrorType;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.MapValueImpl;

/**
 * Class @{@link ErrorCreator} provides APIs to create ballerina error instances.
 *
 * @since 2.0.0
 */
public class ErrorCreator {

    private static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");

    /**
     * Create an error with given reason.
     *
     * @param message error message
     * @return new error
     */
    public static BError createError(BString message) {
        return new ErrorValue(message, new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    /**
     * Create an error with given message and details.
     *
     * @param message error message
     * @param details error details
     * @return new error.
     */
    public static BError createError(BString message, BString details) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        if (details != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, details);
        }
        return new ErrorValue(message, detailMap);
    }

    /**
     * Create an error with given reason and java @{@link Throwable}.
     *
     * @param message error message
     * @param throwable java throwable
     * @return new error
     */
    public static BError createError(BString message, Throwable throwable) {
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage()),
                              message, createError(StringUtils.fromString(throwable.getMessage())),
                              new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    /**
     * Create an error with given type, message, cause and details.
     *
     * @param type    error type
     * @param message  error message
     * @param cause   cause for the error
     * @param details error details
     * @return new error
     */
    public static BError createError(Type type, BString message, BError cause, BMap<BString, Object> details) {
        return new ErrorValue(type, message, cause, details);
    }

    /**
     * Create an error with given type, reason and details.
     *
     * @param type    error type
     * @param message  error message
     * @param details error details
     * @return new error
     */
    public static BError createError(Type type, BString message, BString details) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        if (details != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, details);
        }
        return new ErrorValue(type, message, null, detailMap);
    }

    /**
     * Create an error with given reason and details.
     *
     * @param message  error message
     * @param details error details
     * @return new error
     */
    public static BError createError(BString message, BMap<BString, Object> details) {
        return new ErrorValue(message, details);
    }

    /**
     * Create an error with given @Throwable.
     *
     * @param error  java @{@link Throwable}
     * @return new error
     */
    public static BError createError(Throwable error) {
        if (error instanceof BError) {
            return (BError) error;
        }
        return createError(StringUtils.fromString(error.getMessage()));
    }

    /**
     * Create an distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message  error message
     * @return new error
     */
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message) {
        return createDistinctError(typeIdName, typeIdPkg, message, new MapValueImpl<>(
                PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    /**
     * Create an distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message  error message
     * @param details  error details
     * @return new error
     */
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message,
                                             BMap<BString, Object> details) {
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(), TypeChecker
                .getType(details)), message, null, details, typeIdName, typeIdPkg);
    }

    /**
     * Create an distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message     error message
     * @param cause      error cause
     * @return new error
     */
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message,
                                             BError cause) {
        MapValueImpl<Object, Object> details = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(),
                                             TypeChecker.getType(details)), message, cause, details,
                              typeIdName, typeIdPkg);
    }
}
