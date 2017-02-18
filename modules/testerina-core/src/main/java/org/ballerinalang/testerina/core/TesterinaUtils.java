/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.testerina.core;

/**
 * Utility functions for Function Invocations.
 *
 * @since 0.8.0
 */
public class TesterinaUtils {

    public static final String BALLERINA_MOCK_SYSPROPERTY = "ballerina.mock";

    private TesterinaUtils() {
    }

    public static void setMockEnabled(Boolean mock) {
        System.setProperty(BALLERINA_MOCK_SYSPROPERTY, mock.toString());
    }

    public static boolean isMockEnabled() {
        return Boolean.parseBoolean(System.getProperty(BALLERINA_MOCK_SYSPROPERTY));
    }
}
