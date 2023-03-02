/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeConverter;

import static io.ballerina.runtime.api.constants.RuntimeConstants.REGEXP_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.REG_EXP_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Converts a string to the corresponding regular expression representation.
 *
 * @since 2201.3.0
 */
public class FromString {

    private static final BString ERROR_REASON = getModulePrefixedReason(REGEXP_LANG_LIB,
            REG_EXP_PARSING_ERROR_IDENTIFIER);

    public static Object fromString(BString string) {
        try {
            return TypeConverter.stringToRegExp(string.getValue());
        } catch (BError bError) {
            return ErrorCreator.createError(ERROR_REASON, bError.getErrorMessage());
        }
    }
}
