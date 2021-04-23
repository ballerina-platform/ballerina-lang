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

import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrorType;

/**
 * Extern function lang.string:getCodePoint(string, int).
 *
 * @since 1.0
 */
public class GetCodePoint {

    public static long getCodePoint(BString str, long i) {
        try {
            return str.getCodePoint((int) i);
        } catch (IndexOutOfBoundsException e) {
            throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_STRING_PKG_ID,
                    RuntimeErrorType.CODE_POINT_INDEX_OUT_OF_RANGE, i);
        }
    }
}
