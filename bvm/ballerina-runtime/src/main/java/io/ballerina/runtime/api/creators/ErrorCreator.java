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
package io.ballerina.runtime.api.creators;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.ballerina.runtime.internal.values.ValueCreator;

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
        return new ErrorValue(message);
    }

    /**
     * Create an error with given reason and details.
     *
     * @param message  error message
     * @param details error details
     * @return new error
     */
    public static BError createError(BString message, BMap<BString, Object> details) {
        details = RuntimeUtils.validateErrorDetails(details);
        return new ErrorValue(message, details);
    }

    /**
     * Create an error with given message and details.
     *
     * @param message error message
     * @param details error details
     * @return new error.
     */
    public static BError createError(BString message, BString details) {
        MappingInitialValueEntry[] initialValues;
        if (details != null) {
            initialValues = new MappingInitialValueEntry[1];
            initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD, details);
        } else {
            initialValues = new MappingInitialValueEntry[0];
        }
        MapValueImpl<BString, Object> detailMap = new MapValueImpl(PredefinedTypes.TYPE_ERROR_DETAIL, initialValues);
        return createError(message, detailMap);
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
        details = RuntimeUtils.validateErrorDetails(details);
        ((BErrorType) type).setDetailType(TypeChecker.getType(details));
        return new ErrorValue(type, message, cause, details);
    }

    /**
     * Create an error with given type, reason and details.
     *
     * @param type    error type
     * @param message error message
     * @param details error details
     * @return new error
     */
    public static BError createError(Type type, BString message, BString details) {
        MappingInitialValueEntry[] initialValues;
        if (details != null) {
            initialValues = new MappingInitialValueEntry[1];
            initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD, details);
        } else {
            initialValues = new MappingInitialValueEntry[0];
        }
        MapValueImpl<BString, Object> detailMap = new MapValueImpl(PredefinedTypes.TYPE_ERROR_DETAIL, initialValues);
        return createError(type, message, null, detailMap);
    }

    /**
     * Create an error with given @Throwable.
     *
     * @param error  java @{@link Throwable}
     * @return new error
     */
    public static BError createError(Throwable error) {
        if (error instanceof BError bError) {
            return bError;
        }
        BError bError = createError(StringUtils.fromString(error.toString()));
        bError.setStackTrace(error.getStackTrace());
        return bError;
    }

    /**
     * Create an error value with given error type defined in the given module.
     *
     * @param module        module name
     * @param errorTypeName error type name
     * @param message       error message
     * @param cause         error cause
     * @param details       error details
     * @return error value
     * @throws BError if given error type is not defined in the ballerina module.
     */
    public static BError createError(Module module, String errorTypeName,
                                     BString message, BError cause, BMap<BString, Object> details) throws BError {
        details = RuntimeUtils.validateErrorDetails(details);
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module, false));
        try {
            return valueCreator.createErrorValue(errorTypeName, message, cause, details);
        } catch (BError e) {
            // If error type definition not found, get it from test module.
            String testLookupKey = ValueCreator.getLookupKey(module, true);
            if (ValueCreator.containsValueCreator(testLookupKey)) {
                return ValueCreator.getValueCreator(testLookupKey).createErrorValue(errorTypeName, message,
                        cause, details);
            }
            throw e;
        }
    }

    /**
     * Create a distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message  error message
     * @return new error
     * @deprecated Use {@link #createError(Module, String, BString, BError, BMap)} to create a distinct error.
     */
    @Deprecated
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message) {
        return createDistinctError(typeIdName, typeIdPkg, message, new MapValueImpl<>(
                PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    /**
     * Create a distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message  error message
     * @param details  error details
     * @return new error
     * @deprecated Use {@link #createError(Module, String, BString, BError, BMap)} to create a distinct error.
     */
    @Deprecated
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message,
                                             BMap<BString, Object> details) {
        details = RuntimeUtils.validateErrorDetails(details);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(), TypeChecker
                .getType(details)), message, null, details, typeIdName, typeIdPkg);
    }

    /**
     * Create a distinct error with given typeID, typeIdPkg and reason.
     *
     * @param typeIdName type id
     * @param typeIdPkg  type id module
     * @param message     error message
     * @param cause      error cause
     * @return new error
     * @deprecated Use {@link #createError(Module, String, BString, BError, BMap)} to create a distinct error.
     */
    @Deprecated
    public static BError createDistinctError(String typeIdName, Module typeIdPkg, BString message,
                                             BError cause) {
        MapValueImpl<Object, Object> details = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(),
                                             TypeChecker.getType(details)), message, cause, details,
                              typeIdName, typeIdPkg);
    }

}
