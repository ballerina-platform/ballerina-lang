/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

/**
 * Represents the scope of the platform-specific library.
 *
 * @since 2.0.0
 */
public enum PlatformLibraryScope {
    /**
     * Libraries marked with no scope in Ballerina.toml, get this value.
     * Included phases: compilation, run tests, executable, included in the bala.
     */
    DEFAULT(""),

    /**
     * Libraries marked with scope="testOnly" in Ballerina.toml, get this value.
     * Included phases: run tests.
     */
    TEST_ONLY("testOnly"),

    /**
     * Libraries marked with scope="provided" in Ballerina.toml, get this value.
     * Included phases: compilation, run tests, executable, not included in the bala.
     */
    PROVIDED("provided");

    private final String strValue;

    PlatformLibraryScope(String strValue) {
        this.strValue = strValue;
    }

    /**
     * get the string value of the scope.
     *
     * @return scope as a string
     */
    public String getStringValue() {
        return strValue;
    }
}
