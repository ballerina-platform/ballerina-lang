/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.JsonParser;

import static io.ballerina.runtime.internal.errors.ErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;

/**
 * Extern function lang.values:fromJsonWithType.
 * Converts a string in JSON format to a user-specified type.
 *
 * @since 2.0
 */
public class FromJsonStringWithType {

    private FromJsonStringWithType() {}

    public static Object fromJsonStringWithType(BString value, BTypedesc t) {
        try {
            return JsonParser.parse(value.getValue(), t.getDescribingType());
        } catch (BError e) {
            return ErrorCreator.createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                                            StringUtils.fromString(e.getMessage()));
        }
    }
}
