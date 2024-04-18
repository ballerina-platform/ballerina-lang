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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.langlib.string.utils.Constants;

/**
 * Extern function lang.string:fromCodePointInts(int[]).
 *
 * @since 1.0
 */
public class FromCodePointInts {

    public static Object fromCodePointInts(BArray codePoints) {
        int codePoint = 0;
        try {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < codePoints.size(); i++) {
                codePoint = (int) codePoints.getInt(i);

                if (codePoint <= Constants.MIDDLE_LIMIT_UNICODE && codePoint >= Constants.MIN_UNICODE) {
                    return ErrorCreator.createError(StringUtils.fromString("Invalid codepoint: " + codePoint));
                }

                builder.appendCodePoint(codePoint);
            }
            return StringUtils.fromString(builder.toString());
        } catch (IllegalArgumentException e) {
            return ErrorCreator.createError(StringUtils.fromString("Invalid codepoint: " + codePoint));
        }
    }
}
