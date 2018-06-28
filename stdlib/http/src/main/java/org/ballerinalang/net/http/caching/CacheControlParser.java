/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple parser for parsing the Cache-Control header and separating the control directives.
 *
 * @since 0.965.0
 */
public class CacheControlParser {

    // Taken from org.apache.abdera.protocol.util.CacheControlUtil
    private static final String DIRECTIVE_FORMAT =
            "\\s*([\\w\\-]+)\\s*(=)?\\s*(\\d+|\\\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)+\\\")?\\s*";

    private static final Pattern pattern = Pattern.compile(DIRECTIVE_FORMAT);

    public static Map<CacheControlDirective, String> parse(String cacheControlHeader) {
        Map<CacheControlDirective, String> directivesMap = new HashMap<>();
        Matcher matcher = pattern.matcher(cacheControlHeader);

        while (matcher.find()) {
            String directiveString = matcher.group(1);
            CacheControlDirective directive = CacheControlDirective.parseValue(directiveString);
            if (directive != CacheControlDirective.INVALID) {
                directivesMap.put(directive, matcher.group(3));
            }
        }

        return directivesMap;
    }
}
