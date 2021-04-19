/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Extern function lang.string:includes(string, string, int).
 *
 * @since 2.0.0
 */

public class Includes {
    public static boolean includes(BString str, BString substr, long index) {
        if (index  > Integer.MAX_VALUE) {
            throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_STRING_PKG_ID,
                    RuntimeErrorType.STRING_INDEX_TOO_LARGE, index);
        }
        try {
            return str.indexOf(substr, (int) index) >= 0;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
