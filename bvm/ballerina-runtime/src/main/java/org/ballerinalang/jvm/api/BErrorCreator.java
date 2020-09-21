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
package org.ballerinalang.jvm.api;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * Class @{@link BErrorCreator} provides apis to create ballerina error instances.
 *
 * @since 2.0.0
 */
public class BErrorCreator {

    private static final BString ERROR_MESSAGE_FIELD = BStringUtils.fromString("message");

    /**
     * Create an error with given reason.
     *
     * @param message error message
     * @return new error
     */
    public static BError createError(BString message) {
        return new ErrorValue(message, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    /**
     * Create an error with given message and details.
     *
     * @param message error message
     * @param details error details
     * @return new error.
     */
    public static BError createError(BString message, BString details) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
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
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage()),
                              message, createError(BStringUtils.fromString(throwable.getMessage())),
                              new MapValueImpl<>(BTypes.typeErrorDetail));
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
    public static BError createError(BType type, BString message, BError cause, BMap<BString, Object> details) {
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
    public static BError createError(BType type, BString message, BString details) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
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
        return createError(BStringUtils.fromString(error.getMessage()));
    }

    /**
     * Create an distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message  error message
     * @return new error
     */
    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message) {
        return createDistinctError(typeIdName, typeIdPkg, message, new MapValueImpl<>(BTypes.typeErrorDetail));
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
    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message,
                                             BMap<BString, Object> details) {
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(), TypeChecker
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
    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message,
                                             BError cause) {
        MapValueImpl<Object, Object> details = new MapValueImpl<>(BTypes.typeErrorDetail);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(),
                                             TypeChecker.getType(details)), message, cause, details,
                              typeIdName, typeIdPkg);
    }
}
