/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import org.wso2.ballerinalang.compiler.util.Constants;

/**
 * Extern function lang.string:fromCodePointInt(int).
 *
 * @since 1.0
 */
public class FromCodePointInt {

    public static Object fromCodePointInt(long codePoint) {

        if (codePoint <= Constants.MIDDLE_LIMIT_UNICODE && codePoint >= Constants.MIN_UNICODE) {
            return ErrorCreator.createError(StringUtils.fromString("Invalid codepoint: " + codePoint));
        }

        try {
            StringBuilder builder = new StringBuilder();
            builder.appendCodePoint(((Long) codePoint).intValue());
            return StringUtils.fromString(builder.toString());
        } catch (IllegalArgumentException e) {
            return ErrorCreator.createError(StringUtils.fromString("Invalid codepoint: " + codePoint));
        }
    }
}
