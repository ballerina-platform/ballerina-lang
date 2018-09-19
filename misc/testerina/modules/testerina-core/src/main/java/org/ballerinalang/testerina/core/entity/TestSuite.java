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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entity class to hold a test suite.
 * Represents a package in ballerina.
 */
public class TestSuite {

    private String suiteName;
    private String sourceFileName;
    private TesterinaFunction initFunction;
    private Set<Test> tests = new LinkedHashSet<>();
    private ProgramFile programFile;
    private Set<String> beforeSuiteFunctionNames = new LinkedHashSet<>();
    private Set<String> afterSuiteFunctionNames = new LinkedHashSet<>();
    private Set<String> beforeEachFunctionNames = new LinkedHashSet<>();
    private Set<String> afterEachFunctionNames = new LinkedHashSet<>();
    private Set<TesterinaFunction> beforeSuiteFunctions = new LinkedHashSet<>();
    private Set<TesterinaFunction> afterSuiteFunctions = new LinkedHashSet<>();
    private Set<TesterinaFunction> testUtilityFunctions = new LinkedHashSet<>();
    private Set<TesterinaFunction> beforeEachFunctions = new LinkedHashSet<>();
    private Set<TesterinaFunction> afterEachFunctions = new LinkedHashSet<>();
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

    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public Set<String> getBeforeSuiteFunctionNames() {
        return beforeSuiteFunctionNames;
    }

    public Set<String> getAfterSuiteFunctionNames() {
        return afterSuiteFunctionNames;
    }

    public Set<String> getBeforeEachFunctionNames() {
        return beforeEachFunctionNames;
    }

    public Set<String> getAfterEachFunctionNames() {
        return afterEachFunctionNames;
    }

    public Map<String, FunctionInfo> getMockedRealFunctionsMap() {
        return mockedRealFunctionsMap;
    }

    public Map<String, String> getMockFunctionNamesMap() {
        return mockFunctionNamesMap;
    }

    public Map<String, TesterinaFunction> getMockFunctionsMap() {
        return mockFunctionsMap;
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    public void setProgramFile(ProgramFile programFile) {
        this.programFile = programFile;
    }

    public TesterinaFunction getInitFunction() {
        return initFunction;
    }

    public void setInitFunction(TesterinaFunction initFunction) {
        this.initFunction = initFunction;
    }

    public Set<TesterinaFunction> getTestUtilityFunctions() {
        return testUtilityFunctions;
    }

    public Set<TesterinaFunction> getBeforeEachFunctions() {
        return beforeEachFunctions;
    }

    public Set<TesterinaFunction> getAfterEachFunctions() {
        return afterEachFunctions;
    }

    public Set<Test> getTests() {
        return tests;
    }

    public void setTests(Set<Test> tests) {
        this.tests = tests;
    }

    public void addTests(Test tests) {
        this.tests.add(tests);
    }

    public Set<TesterinaFunction> getBeforeSuiteFunctions() {
        return beforeSuiteFunctions;
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

    public Set<TesterinaFunction> getAfterSuiteFunctions() {
        return afterSuiteFunctions;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

}
