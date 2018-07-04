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

package org.ballerinalang.testerina.core.entity;

import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity class to hold a test suite.
 * Represents a package in ballerina.
 */
public class TestSuite {

    private String suiteName;
    private TesterinaFunction initFunction;
    private List<Test> tests = new ArrayList<>();
    private ProgramFile programFile;
    private List<String> beforeSuiteFunctionNames = new ArrayList<>();
    private List<String> afterSuiteFunctionNames = new ArrayList<>();

    public List<String> getBeforeSuiteFunctionNames() {
        return beforeSuiteFunctionNames;
    }

    public void setBeforeSuiteFunctionNames(List<String> beforeSuiteFunctionNames) {
        this.beforeSuiteFunctionNames = beforeSuiteFunctionNames;
    }

    public List<String> getAfterSuiteFunctionNames() {
        return afterSuiteFunctionNames;
    }

    public void setAfterSuiteFunctionNames(List<String> afterSuiteFunctionNames) {
        this.afterSuiteFunctionNames = afterSuiteFunctionNames;
    }

    public List<String> getBeforeEachFunctionNames() {
        return beforeEachFunctionNames;
    }

    public void setBeforeEachFunctionNames(List<String> beforeEachFunctionNames) {
        this.beforeEachFunctionNames = beforeEachFunctionNames;
    }

    public List<String> getAfterEachFunctionNames() {
        return afterEachFunctionNames;
    }

    public void setAfterEachFunctionNames(List<String> afterEachFunctionNames) {
        this.afterEachFunctionNames = afterEachFunctionNames;
    }

    private List<String> beforeEachFunctionNames = new ArrayList<>();
    private List<String> afterEachFunctionNames = new ArrayList<>();
    private List<TesterinaFunction> beforeSuiteFunctions = new ArrayList<>();
    private List<TesterinaFunction> afterSuiteFunctions = new ArrayList<>();
    private List<TesterinaFunction> testUtilityFunctions = new ArrayList<>();
    private List<TesterinaFunction> beforeEachFunctions = new ArrayList<>();
    private List<TesterinaFunction> afterEachFunctions = new ArrayList<>();

    /**
     * Key - unique identifier for the function to be mocked.
     * Value - name of the mock function
     */
    private Map<String, String> mockFunctionNamesMap = new HashMap<>();
    /**
     * Key - unique identifier for the function to be mocked.
     * Value - a @{@link TesterinaFunction} mock function.
     */
    private Map<String, TesterinaFunction> mockFunctionsMap = new HashMap<>();

    /**
     * Key - unique identifier for the function to be mocked.
     * Value - real function.
     */
    private Map<String, FunctionInfo> mockedRealFunctionsMap = new HashMap<>();

    public Map<String, FunctionInfo> getMockedRealFunctionsMap() {
        return mockedRealFunctionsMap;
    }
    public Map<String, String> getMockFunctionNamesMap() {
        return mockFunctionNamesMap;
    }

    public void setMockFunctionNamesMap(Map<String, String> mockFunctionNamesMap) {
        this.mockFunctionNamesMap = mockFunctionNamesMap;
    }

    public Map<String, TesterinaFunction> getMockFunctionsMap() {
        return mockFunctionsMap;
    }

    public void setMockFunctionsMap(Map<String, TesterinaFunction> mockFunctionsMap) {
        this.mockFunctionsMap = mockFunctionsMap;
    }

    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    public TesterinaFunction getInitFunction() {
        return initFunction;
    }
    public void setInitFunction(TesterinaFunction initFunction) {
        this.initFunction = initFunction;
    }

    public List<TesterinaFunction> getTestUtilityFunctions() {
        return testUtilityFunctions;
    }

    public void setTestUtilityFunctions(List<TesterinaFunction> testUtilityFunctions) {
        this.testUtilityFunctions = testUtilityFunctions;
    }

    public void setBeforeEachFunctions(List<TesterinaFunction> beforeEachFunctions) {
        this.beforeEachFunctions = beforeEachFunctions;
    }

    public void setAfterEachFunctions(List<TesterinaFunction> afterEachFunctions) {
        this.afterEachFunctions = afterEachFunctions;
    }

    public List<TesterinaFunction> getBeforeEachFunctions() {
        return beforeEachFunctions;
    }

    public List<TesterinaFunction> getAfterEachFunctions() {
        return afterEachFunctions;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    public void addTests(Test tests) {
        this.tests.add(tests);
    }

    public List<TesterinaFunction> getBeforeSuiteFunctions() {
        return beforeSuiteFunctions;
    }

    public void setBeforeSuiteFunctions(List<TesterinaFunction> beforeSuiteFunctions) {
        this.beforeSuiteFunctions = beforeSuiteFunctions;
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

    public void addTestUtilityFunction(TesterinaFunction function) {
        this.testUtilityFunctions.add(function);
    }

    public void addMockFunction(String id, String function) {
        this.mockFunctionNamesMap.put(id, function);
    }

    public void addMockedRealFunction(String id, FunctionInfo function) {
        this.mockedRealFunctionsMap.put(id, function);
    }

    public void addMockFunctionObj(String id, TesterinaFunction function) {
        this.mockFunctionsMap.put(id, function);
    }

    public void addBeforeSuiteFunctionObj(TesterinaFunction function) {
        this.beforeSuiteFunctions.add(function);
    }

    public void addAfterSuiteFunctionObj(TesterinaFunction function) {
        this.afterSuiteFunctions.add(function);
    }

    public void addBeforeEachFunctionObj(TesterinaFunction function) {
        this.beforeEachFunctions.add(function);
    }

    public void addAfterEachFunctionObj(TesterinaFunction function) {
        this.afterEachFunctions.add(function);
    }

    public List<TesterinaFunction> getAfterSuiteFunctions() {
        return afterSuiteFunctions;
    }

    public void setAfterSuiteFunctions(List<TesterinaFunction> afterSuiteFunctions) {
        this.afterSuiteFunctions = afterSuiteFunctions;
    }

    public void setProgramFile(ProgramFile programFile) {
        this.programFile = programFile;
    }

}
