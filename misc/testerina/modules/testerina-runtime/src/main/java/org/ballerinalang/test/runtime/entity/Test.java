/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.launcher.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a Ballerina test info.
 */
public class Test {

    String testName;
    String beforeTestFunction;
    String afterTestFunction;
    String dataProvider;
    List<String> dependsOnTestFunctions = new ArrayList<>();
    List<String> groups = new ArrayList<>();

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }

    public List<String> getDependsOnTestFunctions() {
        return dependsOnTestFunctions;
    }

    public void setDependsOnTestFunctions(List<String> dependsOnTestFunctions) {
        this.dependsOnTestFunctions = dependsOnTestFunctions;
    }

    public String getBeforeTestFunction() {
        return beforeTestFunction;
    }

    public void setBeforeTestFunction(String beforeTestFunction) {
        this.beforeTestFunction = beforeTestFunction;
    }

    public String getAfterTestFunction() {
        return afterTestFunction;
    }

    public void setAfterTestFunction(String afterTestFunction) {
        this.afterTestFunction = afterTestFunction;
    }

    public void addDependsOnTestFunction(String function) {
        this.dependsOnTestFunctions.add(function);
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return getTestName();
    }
}
