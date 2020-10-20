/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.integer;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.util.BLangConstants.INT_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.NUMBER_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.int:fromHexString(string).
 *
 * @since 1.0
 */
public class FromHexString {

    public static Object fromHexString(BString s) {
        try {
            return Long.parseLong(s.getValue(), 16);
        } catch (NumberFormatException e) {
            return ErrorCreator.createError(getModulePrefixedReason(INT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER),
                                            StringUtils.fromString(e.getMessage()));
        }
    }
}
