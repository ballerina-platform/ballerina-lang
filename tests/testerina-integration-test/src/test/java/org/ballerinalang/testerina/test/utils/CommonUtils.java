/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.test.utils;



/**
 * Common utility functions to handle integration test outputs .
 *
 */
public class CommonUtils {

    private CommonUtils() {
    }

    public static String replaceExecutionTime(String content) {
        return replaceVaryingString("Test execution time :", "s", content);
    }

    public static String replaceVaryingString(String firstString, String endString, String content) {
        String modifiedContent = content;
        int firstPos = modifiedContent.indexOf(firstString);
        int lastPos = -1;
        if (firstPos >= 0) {
            firstPos = firstPos + firstString.length();
            lastPos = modifiedContent.indexOf(endString, firstPos);
        }
        while (firstPos != -1) {
            modifiedContent = modifiedContent.substring(0, firstPos) + "*****" + modifiedContent.substring(lastPos);
            firstPos = modifiedContent.indexOf(firstString, firstPos);
            if (firstPos >= 0) {
                firstPos = firstPos + firstString.length();
                lastPos = modifiedContent.indexOf(endString, firstPos);
            }
        }
        return modifiedContent;
    }
}
