/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.LENGTH_GREATER_THAT_2147483647_NOT_YET_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Extern function ballerina.model.strings:padEnd.
 *
 * @since 2201.1.0
 */

public class PadEnd {

    public static BString padEnd(BString str, long len, BString padChar) {
        int strLength = str.length();
        if (len <= strLength) {
            return str;
        }

        if (len > Integer.MAX_VALUE) {
            throw ErrorCreator.createError(getModulePrefixedReason(STRING_LANG_LIB,
                    LENGTH_GREATER_THAT_2147483647_NOT_YET_SUPPORTED));
        }

        int targetLen = (int) len;
        String pad = padChar.toString().repeat(targetLen - strLength);
        return StringUtils.fromString(str.toString().concat(pad));
    }
}
