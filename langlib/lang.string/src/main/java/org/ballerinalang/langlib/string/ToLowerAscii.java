/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.util.Locale;

import static org.ballerinalang.langlib.string.utils.StringUtils.createNullReferenceError;

/**
 * Extern function ballerina.model.strings:toLower.
 *
 * @since 0.8.0
 */
public class ToLowerAscii {

    public static BString toLowerAscii(BString str) {
        if (str == null) {
            throw createNullReferenceError();
        }
        return StringUtils.fromString(str.getValue().toLowerCase(Locale.getDefault()));
    }
}
