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
package org.ballerinalang.langlib.testutils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the test info.
 *
 * @since 2.0.0
 */

public class CTests {

    private String testName;
    private String description;
    private String path;
    private List<TestFunction> testFunctions = new ArrayList<>();

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setPathName(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public List<TestFunction> getTestFunctions() {
        return testFunctions;
    }

    public void addTestFunctions(TestFunction function) {
        this.testFunctions.add(function);
    }
}
