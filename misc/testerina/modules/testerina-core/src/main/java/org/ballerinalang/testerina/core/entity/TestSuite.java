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

import org.ballerinalang.jvm.scheduling.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity class to hold a test suite.
 * Represents a module in ballerina.
 */
public class TestSuite {

    private String suiteName;
    private String sourceFileName;
    private TesterinaFunction initFunction;
    private TesterinaFunction startFunction;
    private TesterinaFunction stopFunction;
    private TesterinaFunction testInitFunction;
    private TesterinaFunction testStartFunction;
    private TesterinaFunction testStopFunction;
    private List<Test> tests = new ArrayList<>();
    private ClassLoader programFile;
    private List<String> beforeSuiteFunctionNames = new ArrayList<>();
    private List<String> afterSuiteFunctionNames = new ArrayList<>();

    // We will use one scheduler to start the module and let it spin
    // Then we will use a second scheduler to run the test function
    public final Scheduler scheduler = new Scheduler(4, false);
    public final Scheduler initScheduler = new Scheduler(4, false);

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

    public ClassLoader getProgramFile() {
        return programFile;
    }

    public TesterinaFunction getInitFunction() {
        return initFunction;
    }

    public void setInitFunction(TesterinaFunction initFunction) {
        this.initFunction = initFunction;
        this.initFunction.scheduler = this.scheduler;
    }

    public TesterinaFunction getStartFunction() {
        return startFunction;
    }

    public void setStartFunction(TesterinaFunction startFunction) {
        this.startFunction = startFunction;
        this.startFunction.scheduler = this.scheduler;
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
        function.scheduler = this.scheduler;
        this.testUtilityFunctions.add(function);
    }

    public void addMockFunction(String id, String function) {
        this.mockFunctionNamesMap.put(id, function);
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

    public void setProgramFile(ClassLoader programFile) {
        this.programFile = programFile;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public TesterinaFunction getStopFunction() {
        return stopFunction;
    }


    public TesterinaFunction getTestInitFunction() {
        return testInitFunction;
    }

    public void setTestInitFunction(TesterinaFunction testInitFunction) {
        this.testInitFunction = testInitFunction;
    }

    public TesterinaFunction getTestStartFunction() {
        return testStartFunction;
    }

    public void setTestStartFunction(TesterinaFunction testStartFunction) {
        this.testStartFunction = testStartFunction;
    }

    public TesterinaFunction getTestStopFunction() {
        return testStopFunction;
    }

    public void setTestStopFunction(TesterinaFunction testStopFunction) {
        this.testStopFunction = testStopFunction;
    }

    public void setStopFunction(TesterinaFunction stopFunction) {
        this.stopFunction = stopFunction;
        this.stopFunction.scheduler = this.scheduler;
    }


    public void start() {
        if (initFunction != null && startFunction != null) {
            // As the init function we need to use $moduleInit to initialize all the dependent modules
            // properly.
            initFunction.setName("$moduleInit");
            initFunction.scheduler = initScheduler;
            initFunction.invoke();
            // Now we initialize the init of testable module.
            if (testInitFunction != null) {
                testInitFunction.scheduler = initScheduler;
                testInitFunction.invoke();
            }
            // As the start function we need to use $moduleStart to start all the dependent modules
            // properly.
            startFunction.setName("$moduleStart");
            startFunction.scheduler = initScheduler;
            startFunction.invoke();

            // Invoke start function of the testable module
            if (testStartFunction != null) {
                testStartFunction.scheduler = initScheduler;
                testStartFunction.invoke();
            }

            // Once the start function finish we will re start the scheduler with immortal true
            initScheduler.immortal = true;
            Thread immortalThread = new Thread(() -> {
                initScheduler.start();
            }, "module-start");
            immortalThread.setDaemon(true);
            immortalThread.start();
        }
    }

    public void stop() {
        if (stopFunction != null) {
            // Invoke stop function of the testable module.
            if (testStopFunction != null) {
                testStopFunction.scheduler = scheduler;
                testStopFunction.invoke();
            }

            stopFunction.setName("$moduleStop");
            stopFunction.directInvoke(new Class[]{});
        }
        // this.initScheduler.poison();
    }
}
