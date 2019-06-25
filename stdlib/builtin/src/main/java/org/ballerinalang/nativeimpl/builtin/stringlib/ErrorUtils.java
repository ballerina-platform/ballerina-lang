/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.string;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;

/**
 * Util class for creating error messages.
 *
 * @since 2019R2
 */
public class ErrorUtils {

    private ErrorUtils() {

    }

    public static BError createStringError(Context context, String errMsg) {

        return createError(context, errMsg, BallerinaErrorReasons.STRING_OPERATION_ERROR);
    }

    /**
     * Get builtin conversion error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @param reason  The reason of the error
     * @return conversion error
     */
    public static BError createError(Context context, String errMsg, String reason) {

        BMap<String, BValue> errorMap = new BMap<>();
        errorMap.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, reason, errorMap);
    }

}
