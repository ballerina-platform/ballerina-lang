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

import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class to hold a test suite.
 * Represents a package in ballerina.
 */
public class TestSuite {

    private String suiteName;
    private List<Test> tests = new ArrayList<>();

    public List<ProgramFile> getProgramFiles() {
        return programFiles;
    }

    private List<ProgramFile> programFiles = new ArrayList<>();
    private List<TesterinaFunction> beforeSuiteFunctions = new ArrayList<>();
    private List<TesterinaFunction> afterSuiteFunctions = new ArrayList<>();

    public TesterinaFunction getInitFunction() {
        return initFunction;
    }

    public void setInitFunction(TesterinaFunction initFunction) {
        this.initFunction = initFunction;
    }

    private TesterinaFunction initFunction;

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

    private List<TesterinaFunction> testUtilityFunctions = new ArrayList<>();

    public List<TesterinaFunction> getBeforeEachFunctions() {
        return beforeEachFunctions;
    }

    public List<TesterinaFunction> getAfterEachFunctions() {
        return afterEachFunctions;
    }

    private List<TesterinaFunction> beforeEachFunctions = new ArrayList<>();
    private List<TesterinaFunction> afterEachFunctions = new ArrayList<>();

    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
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

    public void setBeforeSuiteFunctions(
            List<TesterinaFunction> beforeSuiteFunctions) {
        this.beforeSuiteFunctions = beforeSuiteFunctions;
    }

    public void addBeforeSuiteFunction(TesterinaFunction function) {
        this.beforeSuiteFunctions.add(function);
    }

    public void addAfterSuiteFunction(TesterinaFunction function) {
        this.afterSuiteFunctions.add(function);
    }

    public void addBeforeEachFunction(TesterinaFunction function) {
        this.beforeEachFunctions.add(function);
    }

    public void addAfterEachFunction(TesterinaFunction function) {
        this.afterEachFunctions.add(function);
    }

    public void addTestUtilityFunction(TesterinaFunction function) {
        this.testUtilityFunctions.add(function);
    }

    public List<TesterinaFunction> getAfterSuiteFunctions() {
        return afterSuiteFunctions;
    }

    public void setAfterSuiteFunctions(
            List<TesterinaFunction> afterSuiteFunctions) {
        this.afterSuiteFunctions = afterSuiteFunctions;
    }

    public void addProgramFile (ProgramFile programFile) {
        this.programFiles.add(programFile);
    }

}
