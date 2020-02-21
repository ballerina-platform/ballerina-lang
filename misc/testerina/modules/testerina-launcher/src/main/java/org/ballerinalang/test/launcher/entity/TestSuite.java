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
package org.ballerinalang.test.launcher.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java class to store and get data from a json (for a test run).
 */
public class TestSuite {

    private String orgName;
    private String version;
    private String packageName;
    private String packageId;

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
    private List<String> afterSuiteFunctionNames = new ArrayList<>();
    private List<String> beforeEachFunctionNames = new ArrayList<>();
    private List<String> afterEachFunctionNames = new ArrayList<>();
    private List<Test> tests = new ArrayList<>();

    /**
     * Key - unique identifier for the function to be mocked.
     * Value - name of the mock function
     */
    private Map<String, String> mockFunctionNamesMap = new HashMap<>();

    public TestSuite(String packageId, String packageName, String orgName, String version) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.orgName = orgName;
        this.version = version;
    }

    public String getPackageID() {
        return packageId;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public List<String> getAfterSuiteFunctionNames() {
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

    public void addAfterSuiteFunction(String function) {
        this.afterSuiteFunctionNames.add(function);
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

    public void addTestUtilityFunction(String functionName, String functionClassName) {
        this.testUtilityFunctions.put(functionName, functionClassName);
    }

    public void addTests(Test tests) {
        this.tests.add(tests);
    }
}
