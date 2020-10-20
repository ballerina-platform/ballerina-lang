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

package org.ballerinalang.langlib.decimal;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;

import java.math.BigDecimal;

/**
 * Native implementation of lang.decimal:fromString(string).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.decimal", functionName = "fromString",
//        args = {@Argument(name = "s", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.UNION)},
//        isPublic = true
//)
public class FromString {

    public static Object fromString(BString s) {
        try {
            return ValueCreator.createDecimalValue(new BigDecimal(s.getValue()));
        } catch (NumberFormatException e) {
            // TODO: 6/21/19 Improve this error value
            return ErrorCreator.createError(StringUtils.fromString(e.getMessage()), e);
        }
    }
}
