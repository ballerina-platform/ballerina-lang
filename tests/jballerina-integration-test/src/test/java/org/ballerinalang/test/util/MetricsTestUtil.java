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

package org.ballerinalang.test.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util class Observability Metrics.
 */
public class MetricsTestUtil {

    public static String getMetricName(String key) {
        int index = key.lastIndexOf("{");
        return key.substring(0, index);
    }

    public static String getTag(String key, String tag) {
        Pattern connectionIDPattern = Pattern.compile(tag + "=\"[^\"]*\",");
        Matcher connectionIDMatcher = connectionIDPattern.matcher(key);
        if (connectionIDMatcher.find()) {
            return connectionIDMatcher.group(0);
        }
        return "";
    }

    /**
     * Used to re-organize the tags of a given metric into a standardized order.
     * @param metric
     * @param tags
     * @return
     */
    public static String generateNewKey(String metric, String[] tags) {
        String key = metric + "{";
        for (String tag: tags) {
            key = key + tag;
        }
        key = key + "}";
        return key;
    }
}
