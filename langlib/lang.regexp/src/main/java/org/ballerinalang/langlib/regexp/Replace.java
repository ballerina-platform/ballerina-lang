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
 * @since 2201.3.0
 */
public class Replace {

    public static BString replaceFromString(BRegexpValue regExp, BString str, BString replacingStr, int startIndex) {
        return replaceFromString(regExp, str, replacingStr, startIndex, false);
    }

    public static BString replaceAllFromString(BRegexpValue regExp, BString str, BString replacingStr, int startIndex) {
        return replaceFromString(regExp, str, replacingStr, startIndex, true);
    }

    private static BString replaceFromString(BRegexpValue regExp, BString str, BString replacingStr, int startIndex,
                                             boolean isReplaceAll) {
        String originalStr = str.getValue();
        String replacementString = replacingStr.getValue();
        Matcher matcher = RegexUtil.getMatcher(regExp, originalStr);
        matcher.region(startIndex, originalStr.length());
        if (matcher.find()) {
            String prefixStr = "";
            String updatedSubStr;
            if (startIndex != 0) {
                prefixStr = originalStr.substring(0, startIndex);
                String substr = originalStr.substring(startIndex);
                matcher = RegexUtil.getMatcher(regExp, substr);
            }
            if (isReplaceAll) {
                updatedSubStr = matcher.replaceAll(replacementString);
            } else {
                updatedSubStr = matcher.replaceFirst(replacementString);
            }
            return StringUtils.fromString(prefixStr + updatedSubStr);
        }
        return str;
    }
}
