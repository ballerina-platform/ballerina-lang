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
import io.ballerina.runtime.api.StringUtils;
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

    private static List<String> getValueList(String value) {
        List<String> valueList = new ArrayList<>();
        if (value.contains("\n")) {
            String[] valueArray = value.split("\n");
            for (int i = 0; i < valueArray.length; i++) {
                if (valueArray[i].length() > MAX_ARG_LENGTH) {
                    String[] partitions = valueArray[i].split(PARTITION_REGEX);
                    valueList.addAll(Arrays.asList(partitions));
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

    public static BString getStringDiff(BString expected, BString actual) {
        List<String> expectedValueList = getValueList(expected.toString());
        List<String> difference = null;
        String output = "\n";
        try {
            Patch<String> patch = DiffUtils.diff(expectedValueList, getValueList(actual.toString()));
            difference = UnifiedDiffUtils.generateUnifiedDiff("expected", "actual",
                    expectedValueList, patch, MAX_ARG_LENGTH);
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
                    output = output.concat(line + "\n\n");
                } else {
                    output = output.concat(line + "\n");
                }
            }
        }
        return StringUtils.fromString(output);
    }

}

