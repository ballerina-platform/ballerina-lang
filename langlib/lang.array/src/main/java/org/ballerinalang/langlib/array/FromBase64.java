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

package org.ballerinalang.langlib.array;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;

import java.util.Base64;

/**
 * Native implementation of lang.array:fromBase64(string).
 *
 * @since 1.0
 */
public class FromBase64 {

    public static Object fromBase64(BString str) {
        try {
            byte[] decodedArr = Base64.getDecoder().decode(str.getValue());
            return ValueCreator.createArrayValue(decodedArr);
        } catch (IllegalArgumentException e) {
            return ErrorCreator.createError(StringUtils.fromString("Invalid base64 string"),
                                            StringUtils.fromString(e.getMessage()));
        }
    }
}
