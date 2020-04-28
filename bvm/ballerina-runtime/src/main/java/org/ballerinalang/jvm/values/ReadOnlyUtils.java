/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;

/**
 * Util class for readonly-typed value related operations.
 *
 * @since 1.3.0
 */
class ReadOnlyUtils {

    /**
     * Method to handle an update to a value, that is invalid due to the value being immutable.
     *
     * @param moduleName the name of the langlib module for whose values the error occurred
     */
    static void handleInvalidUpdate(String moduleName) {
        throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE));
    }

    static BType setImmutableType(BType originalType) {
        // TODO: 4/28/20 Return type with readonly flag.
        return originalType.getImmutableType();
    }
}
