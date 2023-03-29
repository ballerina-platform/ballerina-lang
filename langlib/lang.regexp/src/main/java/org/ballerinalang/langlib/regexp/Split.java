/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.values.RegExpValue;

/**
 * Native implementation of lang.regexp:split(string).
 *
 * @since 2201.3.0
 */
public class Split {

    public static BArray split(BRegexpValue regExp, BString str) {
        String originalString = str.getValue();
        RegExpValue translatedRegExpVal = RegExpFactory.translateRegExpConstructs((RegExpValue) regExp);
        String regex = StringUtils.getStringValue(translatedRegExpVal, null);
        String[] splitStrArr = originalString.split(regex, -1);
        return StringUtils.fromStringArray(splitStrArr);
    }
}
