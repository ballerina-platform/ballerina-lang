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
 * Holds the Test-Suite info.
 *
 * @since 2.0.0
 */

public class CTestSuite {
    static int failedTestCount = 0;
    private String suiteName;
    private String suiteDescription;
    private String testGroupName;
    private List<String> paths = new ArrayList<>();
    private List<CTestGroup> testGroups = new ArrayList<>();

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public void setTestGroupName(String testGroupName) {
        this.testGroupName = testGroupName;
    }

    public void setSuiteDescription(String suiteDescription) {
        this.suiteDescription = suiteDescription;
    }

    public void setPaths(String path) {
        this.paths.add(path);
    }

    public void setTestGroup(CTestGroup cTestGroup) {
        this.testGroups.add(cTestGroup);
    }

    public String getSuiteName() {
        return this.suiteName;
    }

    public String getSuiteDescription() {
        return this.suiteDescription;
    }

    public String getTestGroupName() {
        return this.testGroupName;
    }

    public List<String> getPaths() {
        return this.paths;
    }

    public List<CTestGroup> getTestGroups() {
        return this.testGroups;
    }
}
