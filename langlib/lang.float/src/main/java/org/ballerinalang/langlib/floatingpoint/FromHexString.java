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

package org.ballerinalang.langlib.floatingpoint;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;

import java.util.regex.Pattern;

import static io.ballerina.runtime.api.constants.RuntimeConstants.FLOAT_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.NUMBER_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.float:fromHexString(string).
 *
 * @since 1.0
 */
public class FromHexString {

    private static final Pattern HEX_FLOAT_LITERAL = Pattern.compile("[-+]?0[xX][\\dA-Fa-f.pP\\-+]+");

    private FromHexString() {
    }

    public static Object fromHexString(BString s) {
        String hexValue = s.getValue();
        try {
            if (isValidHexString(hexValue.toLowerCase())) {
                return Double.parseDouble(hexValue);
            }
            return getNumberFormatError("invalid hex string: '" + hexValue + "'");
        } catch (NumberFormatException e) {
            return getNumberFormatError(e.getMessage());
        }
    }

    private static boolean isValidHexString(String hexValue) {
        switch (hexValue) {
            case "+infinity":
            case "-infinity":
            case "infinity":
            case "nan":
                return true;
            default:
                return HEX_FLOAT_LITERAL.matcher(hexValue).matches();
        }
    }

    private static BError getNumberFormatError(String message) {
        return ErrorCreator.createError(getModulePrefixedReason(FLOAT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER),
                StringUtils.fromString(message));
    }
}
