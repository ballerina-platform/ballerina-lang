/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrorType;
import org.ballerinalang.langlib.string.utils.StringUtils;

/**
 * Extern function ballerina.model.strings:lastIndexOf.
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.string",
//        functionName = "lastIndexOf",
//        args = {@Argument(name = "s", type = TypeKind.STRING),
//                @Argument(name = "substring", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.UNION)},
//        isPublic = true
//)
public class LastIndexOf {
    public static Object lastIndexOf(BString value, BString subString, long startIndex) {
        StringUtils.checkForNull(value, subString);
        if (startIndex > Integer.MAX_VALUE) {
            throw ErrorUtils.getRuntimeError(RuntimeConstants.BALLERINA_LANG_STRING_PKG_ID,
                    RuntimeErrorType.STRING_INDEX_TOO_LARGE, startIndex);
        }
        return value.lastIndexOf(subString, (int) startIndex);
    }
}
