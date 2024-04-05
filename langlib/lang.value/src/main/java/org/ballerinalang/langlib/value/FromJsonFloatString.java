/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.JsonParser;

import java.io.StringReader;

/**
 * Parse a string in JSON format and return the the value that it represents.
 * All numbers in the JSON will be represented as float values.
 */

public class FromJsonFloatString {

    public static Object fromJsonFloatString(BString value) {

        String str = value.getValue();
        if (str.equals("null")) {
            return null;
        }
        try {
            return JsonParser.parse(new StringReader(str),
                    JsonUtils.NonStringValueProcessingMode.FROM_JSON_FLOAT_STRING);
        } catch (BError e) {
            return ErrorCreator.createError(
                    StringUtils.fromString("{ballerina/lang.value}FromJsonFloatStringError"),
                    StringUtils.fromString(e.getMessage()));
        }
    }
}
