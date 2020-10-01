/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.string;

import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.internal.ErrorUtils;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Extern function ballerina.model.strings:indexOf.
 *
 * @since 0.8.0
 */
public class IndexOf {

    public static Object indexOf(BString bStr, BString subString, long startIndx) {

        if (bStr == null || subString == null) {
            throw ErrorUtils.createNullReferenceError();
        }
        if (startIndx > Integer.MAX_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(getModulePrefixedReason(STRING_LANG_LIB,
                    INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, startIndx);
        }
        return bStr.indexOf(subString, (int) startIndx);
    }
}
