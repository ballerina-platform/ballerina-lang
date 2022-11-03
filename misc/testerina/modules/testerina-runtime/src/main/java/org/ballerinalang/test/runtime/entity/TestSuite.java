/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.runtime.entity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java class to store and get data from a json (for a test run).
 */
public class TestSuite {

    private String orgName;
    private String version;
    private String packageName;
    private String moduleName;
    private String packageId;
    private String testPackageId;
    private String executeFilePath;

    private String initFunctionName;
    private String startFunctionName;
    private String stopFunctionName;

    private String testInitFunctionName;
    private String testStartFunctionName;
    private String testStopFunctionName;

    private String sourceRootPath;
    private String sourceFileName;

    private Map<String, String> testUtilityFunctions = new HashMap<>();
    private List<String> beforeSuiteFunctionNames = new ArrayList<>();
    private Map<String, AtomicBoolean> afterSuiteFunctionNames = new TreeMap<>();
    private List<String> beforeEachFunctionNames = new ArrayList<>();
    private List<String> afterEachFunctionNames = new ArrayList<>();
    private List<Test> tests = new ArrayList<>();
    private Map<String, TestGroup> groups = new TreeMap<>();
    private List<String> testExecutionDependencies = new ArrayList<>();

    private boolean isReportRequired;
    private boolean isSingleDDTExecution;
    private Map<String, List<String>> dataKeyValues;

    /**
     * Key - unique identifier for the function to be mocked.
     * Value - name of the mock function
     */
    private Map<String, String> mockFunctionNamesMap = new HashMap<>();

    public TestSuite(String packageId, String testPackageId, String packageName,
                     String orgName, String version, String executeFilePath) {
        this.packageId = packageId;
        this.testPackageId = testPackageId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.orgName = orgName;
        this.version = version;
        this.executeFilePath = executeFilePath;
    }

    public String getPackageID() {
        return packageId;
    }

    public String getTestPackageID() {
        return testPackageId;
    }

    public String getSourceRootPath() {
        return sourceRootPath;
    }

    public void setSourceRootPath(String sourceRootPath) {
        this.sourceRootPath = sourceRootPath;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setExecuteFilePath(String executeFilePath) {
        this.executeFilePath = executeFilePath;
    }

    public String getExecuteFilePath() {
        return this.executeFilePath;
    }

    public String getInitFunctionName() {
        return initFunctionName;
    }

    public void setInitFunctionName(String initFunctionName) {
        this.initFunctionName = initFunctionName;
    }

    public String getStartFunctionName() {
        return startFunctionName;
    }

    public void setStartFunctionName(String startFunctionName) {
        this.startFunctionName = startFunctionName;
    }

    public String getStopFunctionName() {
        return stopFunctionName;
    }

    public void setStopFunctionName(String stopFunctionName) {
        this.stopFunctionName = stopFunctionName;
    }

    public String getTestInitFunctionName() {
        return testInitFunctionName;
    }

    public void setTestInitFunctionName(String testInitFunctionName) {
        this.testInitFunctionName = testInitFunctionName;
    }

    public String getTestStartFunctionName() {
        return testStartFunctionName;
    }

    public void setTestStartFunctionName(String testStartFunctionName) {
        this.testStartFunctionName = testStartFunctionName;
    }

    public String getTestStopFunctionName() {
        return testStopFunctionName;
    }

    public void setTestStopFunctionName(String testStopFunctionName) {
        this.testStopFunctionName = testStopFunctionName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Map<String, String> getTestUtilityFunctions() {
        return testUtilityFunctions;
    }

    public List<String> getBeforeSuiteFunctionNames() {
        return beforeSuiteFunctionNames;
    }

    public Map<String, AtomicBoolean> getAfterSuiteFunctionNames() {
        return afterSuiteFunctionNames;
    }

    public List<String> getBeforeEachFunctionNames() {
        return beforeEachFunctionNames;
    }

    public List<String> getAfterEachFunctionNames() {
        return afterEachFunctionNames;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public void addBeforeSuiteFunction(String function) {
        this.beforeSuiteFunctionNames.add(function);
    }

    public void addAfterSuiteFunction(String function, AtomicBoolean alwaysRun) {
        this.afterSuiteFunctionNames.put(function, alwaysRun);
    }

    public void addBeforeEachFunction(String function) {
        this.beforeEachFunctionNames.add(function);
    }

    public void addAfterEachFunction(String function) {
        this.afterEachFunctionNames.add(function);
    }

    public void addMockFunction(String id, String function) {
        this.mockFunctionNamesMap.put(id, function);
    }

    public Map<String, String> getMockFunctionNamesMap() {
        return this.mockFunctionNamesMap;
    }

    public void addTestUtilityFunction(String functionName, String functionClassName) {
        this.testUtilityFunctions.put(functionName, functionClassName);
    }

    public void addTests(Test test) {
        this.tests.add(test);
    }

    public boolean isReportRequired() {
        return isReportRequired;
    }

    public void setReportRequired(boolean reportRequired) {
        isReportRequired = reportRequired;
    }

    public Map<String, TestGroup> getGroups() {
        return groups;
    }

    /**
     * Adds a provided @AfterGroups function to the test suite.
     *
     * @param afterGroupFunc name of the function
     * @param groups         groups to which the function belongs
     */
    public void addAfterGroupFunction(String afterGroupFunc, List<String> groups, AtomicBoolean alwaysRun) {
        for (String groupName : groups) {
            if (this.groups.get(groupName) == null) {
                this.groups.put(groupName, new TestGroup());
            }
            this.groups.get(groupName).addAfterGroupsFunction(afterGroupFunc, alwaysRun);
        }
    }

    /**
     * Adds a provided @BeforeGroups function to the test suite.
     *
     * @param beforeGroupsFunc name of the function
     * @param groups           groups to which the function belongs
     */
    public void addBeforeGroupsFunction(String beforeGroupsFunc, List<String> groups) {
        for (String groupName : groups) {
            if (this.groups.get(groupName) == null) {
                this.groups.put(groupName, new TestGroup());
            }
            this.groups.get(groupName).addBeforeGroupsFunction(beforeGroupsFunc);
        }
    }

    /**
     * Adds a groups to the test suite using the provided Test object.
     *
     * @param test Test object to filter groups from
     */
    public void addTestToGroups(Test test) {
        TestGroup testGroup;
        for (String groupName : test.getGroups()) {
            if (this.getGroups().get(groupName) != null) {
                testGroup = this.getGroups().get(groupName);
            } else {
                testGroup = new TestGroup();
            }
            testGroup.incrementTestCount();
            this.groups.put(groupName, testGroup);
        }
    }

    /**
     * Sort all the lists in Test suite in alphabetical order.
     */
    public void sort() {
        Collections.sort(beforeEachFunctionNames);
        Collections.sort(afterEachFunctionNames);
        Collections.sort(beforeSuiteFunctionNames);
        Collections.sort(tests, new Comparator<Test>() {
            @Override
            public int compare(Test t1, Test t2) {
                return t1.getTestName().compareTo(t2.getTestName());
            }
        });
        for (TestGroup testGroup:groups.values()) {
            testGroup.sort();
        }
    }

    public void addTestExecutionDependencies(Collection<Path> dependencies) {
        dependencies.forEach((path) -> {
            this.testExecutionDependencies.add(path.toString());
        });
    }

    public List<String> getTestExecutionDependencies() {
        return this.testExecutionDependencies;
    }

    public boolean isSingleDDTExecution() {
        return this.isSingleDDTExecution;
    }

    public void setSingleDDTExecution(boolean isSingleDDTExecution) {
        this.isSingleDDTExecution = isSingleDDTExecution;
    }

    public Map<String, List<String>> getDataKeyValues() {
        return dataKeyValues;
    }

    public void setDataKeyValues(Map<String, List<String>> dataKeyValues) {
        this.dataKeyValues = dataKeyValues;
    }
}
