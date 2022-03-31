/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.semver.checker.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import org.ballerinalang.semver.checker.diff.CompatibilityLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class SourceDiffUtils {

    private static final String PARTITION_REGEX = "(?<=\\G.{80})";
    private static final int MAX_ARG_LENGTH = 80;

    /**
     * Get a list of String values by separating the lines from a String value.
     *
     * @param value String
     * @return List<String>
     */
    private static List<String> getValueList(String value) {
        List<String> valueList = new ArrayList<>();
        if (value.contains("\n")) {
            String[] valueArray = value.split("\n");
            for (String s : valueArray) {
                if (s.length() > MAX_ARG_LENGTH) {
                    String[] partitions = s.split(PARTITION_REGEX);
                    valueList.addAll(Arrays.asList(partitions));
                } else {
                    valueList.add(s);
                }
            }
        } else {
            if (value.length() > MAX_ARG_LENGTH) {
                String[] partitions = value.split(PARTITION_REGEX);
                valueList.addAll(Arrays.asList(partitions));
            } else {
                valueList.add(value);
            }
        }
        return valueList;
    }

    /**
     * Get the diff between two String values as a String in unified format.
     *
     * @param expected BString
     * @param actual   BString
     * @return BString
     */
    public static String getStringDiff(String actual, String expected) {
        List<String> actualValueList = getValueList(actual);
        List<String> difference = null;
        String output = "\n";
        try {
            Patch<String> patch = DiffUtils.diff(actualValueList, getValueList(expected));
            difference = UnifiedDiffUtils.generateUnifiedDiff("actual", "expected",
                    actualValueList, patch, MAX_ARG_LENGTH);
        } catch (DiffException e) {
            output = "\nWarning: Could not generate diff.";
        }
        if (difference != null) {
            for (String line : difference) {
                if (line.startsWith("+") || line.startsWith("-")) {
                    if (output.endsWith("\n")) {
                        output = output.concat(line + "\n");
                    } else {
                        output = output.concat("\n" + line + "\n");
                    }
                } else if (line.startsWith("@@ -")) {
                    output = output.concat("\n" + line + "\n\n");
                } else {
                    output = output.concat(line + "\n");
                }
            }
        }
        output = output.replaceAll("\n\n", " \n \n ");
        return output;
    }
}

