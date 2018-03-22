/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util.parser.helper;

import org.ballerinalang.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for annotation.
 */
public class AnnotationHelper {

    private static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    public static List<String> generateIncludedMetrics(Element metrics) {
        List<String> regexs = new ArrayList<String>();
        if (metrics != null) {
            String[] metricStrings = metrics.getValue().split(",");
            for (String metricString : metricStrings) {
                String metricStringTrim = metricString.trim();
                if (!metricStringTrim.isEmpty()) {
                    regexs.add(createRegexFromGlob(metricStringTrim));
                }

            }
        }
        if (regexs.size() == 0) {
            regexs.add(createRegexFromGlob("*.*"));
        }
        return regexs;

    }
}
