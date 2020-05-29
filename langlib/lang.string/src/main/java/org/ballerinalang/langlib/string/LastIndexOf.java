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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.langlib.string.utils.StringUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.util.BLangCompilerConstants.STRING_VERSION;

/**
 * Extern function ballerina.model.strings:lastIndexOf.
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string", version = STRING_VERSION,
        functionName = "lastIndexOf",
        args = {@Argument(name = "s", type = TypeKind.STRING),
                @Argument(name = "substring", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class LastIndexOf {
    public static Object lastIndexOf(Strand strand, BString value, BString subString, long startIndex) {
        StringUtils.checkForNull(value, subString);
        if (startIndex > Integer.MAX_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(getModulePrefixedReason(STRING_LANG_LIB,
                    INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, startIndex);
        }
        return value.lastIndexOf(subString, (int) startIndex);
    }
}
