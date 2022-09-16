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
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;

import java.util.regex.Matcher;

/**
 * Native implementation of lang.regexp:replace(string).
 *
 * @since 2.3.0
 */
public class Replace {
    public static BString replaceFromString(BRegexpValue regExp, BString str, BString replacingStr, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        if (matcher.find(startIndex)) {
            return StringUtils.fromString(matcher.replaceFirst(replacingStr.getValue()));
        }
        return str;
    }

    public static BString replaceAllFromString(BRegexpValue regExp, BString str, BString replacingStr, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        String replacementString = replacingStr.getValue();
        if (matcher.find(startIndex)) {
            return StringUtils.fromString(matcher.replaceAll(replacementString));
        }
        return str;
    }
}
