/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.core;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {

    private static final String PARTITION_REGEX = "(?<=\\G.{80})";
    private static final int MAX_ARG_LENGTH = 80;

    /**
     * Get a list of String values by separating the lines from a String value.
     * @param value String
     * @return List<String>
     */
    private static List<String> getValueList(String value) {
        List<String> valueList = new ArrayList<>();
        if (value.contains("\n")) {
            String[] valueArray = value.split("\n");
            for (int i = 0; i < valueArray.length; i++) {
                if (valueArray[i].length() > MAX_ARG_LENGTH) {
                    String[] partitions = valueArray[i].split(PARTITION_REGEX);
                    valueList.addAll(Arrays.asList(partitions));
                } else {
                    valueList.add(valueArray[i]);
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
     * @param actual BString
     * @return BString
     */
    public static BString getStringDiff(BString actual, BString expected) {
        List<String> actualValueList = getValueList(actual.toString());
        List<String> difference = null;
        String output = "\n";
        try {
            Patch<String> patch = DiffUtils.diff(actualValueList, getValueList(expected.toString()));
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
        return StringUtils.fromString(output);
    }

    /**
     * Get the diff between two sets of keys of a Ballerina map as a Ballerina string.
     * @param actualKeys BArray
     * @param expectedKeys BArray
     * @return BString
     */
    public static BString getKeysDiff(BArray actualKeys, BArray expectedKeys) {
        String keyDiff = "";
        List<String> actualKeyList = convertToList(actualKeys);
        List<String> expectedKeyList = convertToList(expectedKeys);
        List<String> actualKeyDiff = comparisonDiffList(actualKeyList, expectedKeyList);
        List<String> expectedKeyDiff = comparisonDiffList(expectedKeyList, actualKeyList);
        if (expectedKeyDiff.size() > 0) {
            keyDiff = keyDiff.concat("\nexpected keys\t:" + convertToString(expectedKeyDiff) + "\n");
        }
        if (actualKeyDiff.size() > 0) {
            keyDiff = keyDiff.concat("actual keys\t:" + convertToString(actualKeyDiff));
        }
        return StringUtils.fromString(keyDiff);
    }

    /**
     * Convert a list of String to a String value.
     * @param keyDiffList List<String>
     * @return String
     */
    private static String convertToString(List<String> keyDiffList) {
        String keyDiff = "";
        for (int i = 0; i < keyDiffList.size(); i++) {
            keyDiff = keyDiff.concat(" " + keyDiffList.get(i));
            if (i != (keyDiffList.size() - 1)) {
                keyDiff = keyDiff.concat(",");
            }
        }
        return keyDiff;
    }

    /**
     * Convert a Ballerina String array to a List of Java Strings.
     *
     * @param keys BArray
     * @return List<String>
     */
    private static List<String> convertToList(BArray keys) {
        List<String> keyList = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            keyList.add(keys.getBString(i).toString());
        }
        return keyList;
    }

    /**
     * Compare whether each key in keyList is available in compareToKeyList.
     * Return a list of keys that are not available.
     * @param keyList List<String>
     * @param compareToKeyList List<String>
     * @return List<String>
     */
    private static List<String> comparisonDiffList(List<String> keyList, List<String> compareToKeyList) {
        List<String> diffList = new ArrayList<>();
        for (String key : keyList) {
            if (!compareToKeyList.contains(key)) {
                diffList.add(key);
            }
        }
        return diffList;
    }

}

