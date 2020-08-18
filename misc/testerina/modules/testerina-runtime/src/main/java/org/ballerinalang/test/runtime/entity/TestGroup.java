/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.runtime.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a test group in the test suite.
 */
public class TestGroup {
    private int testCount;
    private int executedCount;
    private List<String> beforeGroupsFunctions;
    private List<String> afterGroupsFunctions;

    public TestGroup() {
        this.testCount = 0;
        this.executedCount = 0;
        this.beforeGroupsFunctions = new ArrayList<>();
        this.afterGroupsFunctions = new ArrayList<>();
    }

    /**
     * Adds a test to the group.
     */
    public void incrementTestCount() {
        this.testCount++;
    }

    /**
     * Increments the executed test count by one.
     * This should be be called after each test execution.
     */
    public void incrementExecutedCount() {
        this.executedCount++;
    }

    /**
     * Returns the @BeforeGroups functions declared for this group.
     *
     * @return list of function names
     */
    public List<String> getBeforeGroupsFunctions() {
        return beforeGroupsFunctions;
    }

    /**
     * Adds a @BeforeGroups function to the list.
     *
     * @param beforeGroupsFunc name of the function
     */
    public void addBeforeGroupsFunction(String beforeGroupsFunc) {
        this.beforeGroupsFunctions.add(beforeGroupsFunc);
    }

    /**
     * Returns the @AfterGroups functions declared for this group.
     *
     * @return list of function names
     */
    public List<String> getAfterGroupsFunctions() {
        return afterGroupsFunctions;
    }

    /**
     * Adds a @AfterGroups function to the list.
     *
     * @param afterGroupsFunc name of the function
     */
    public void addAfterGroupsFunction(String afterGroupsFunc) {
        this.afterGroupsFunctions.add(afterGroupsFunc);
    }

    /**
     * Returns whether the first test of the group is executed.
     * This is used for executing the @BeforeGroups functions.
     *
     * @return if the first test is executed
     */
    public boolean isFirstTestExecuted() {
        return executedCount > 0;
    }

    /**
     * Returns whether the last test of the group is executed.
     * This is used for executing the @AfterGroups functions.
     *
     * @return  if the last test is executed
     */
    public boolean isLastTestExecuted() {
        return testCount == executedCount;
    }
}
