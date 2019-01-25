/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;

/**
 * @since 0.94.1
 */
public class BuiltInUtils {

    private static final int READABLE_BUFFER_SIZE = 8192; //8KB

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link BValue} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static BValue getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return BTypes.typeString.getZeroValue();
        }
        return new BString(value);
    }

    /**
     * Get builtin conversion error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description (detail)
     * @param reason  Error reason
     * @return conversion error
     */
    public static BError createConversionError(Context context, String errMsg, String reason) {
        return createError(context, errMsg, reason);
    }

    /**
     * Get builtin string error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @return conversion error
     */
    public static BError createStringError(Context context, String errMsg) {
        return createError(context, errMsg, BallerinaErrorReasons.STRING_OPERATION_ERROR);
    }

    /**
     * Get builtin conversion error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @param reason The reason of the error
     * @return conversion error
     */
    public static BError createError(Context context, String errMsg, String reason) {
        BMap<String, BValue> errorMap = new BMap<>();
        errorMap.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, reason, errorMap);
    }

    private BuiltInUtils() {
    }
}
