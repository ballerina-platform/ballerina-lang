/*
*  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.util;

/**
 * Test utils for running performance tests.
 */
public class PerformanceTestUtils {

    private static final String RES_TIME_THRESHOLD_SYSTEM_PROPERTY = "responseTimeThreshold";
    /**
     * Get response time threshold for Completion Performance Test.
     */
    public static int getCompletionResponseTimeThreshold() {
        return Integer.parseInt(System.getProperty(RES_TIME_THRESHOLD_SYSTEM_PROPERTY)) / 2;
    }

    /**
     * Get response time threshold for Code Action Performance Test.
     */
    public static int getCodeActionResponseTimeThreshold() {
        return Integer.parseInt(System.getProperty(RES_TIME_THRESHOLD_SYSTEM_PROPERTY)) / 2;
    }

    /**
     * Get response time threshold for Hover Performance Test.
     */
    public static int getHoverResponseTimeThreshold() {
        return Integer.parseInt(System.getProperty(RES_TIME_THRESHOLD_SYSTEM_PROPERTY)) / 2;
    }

    /**
     * Get response time threshold for Open Document Performance Test.
     */
    public static int getOpenDocumentResponseTimeThreshold() {
        return Integer.parseInt(System.getProperty(RES_TIME_THRESHOLD_SYSTEM_PROPERTY));
    }
}
