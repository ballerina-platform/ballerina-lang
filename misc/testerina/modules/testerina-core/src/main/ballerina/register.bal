// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/lang.array;

final TestRegistry testRegistry = new;
final TestRegistry beforeSuiteRegistry = new;
final TestRegistry afterSuiteRegistry = new;
final TestRegistry beforeEachRegistry = new;
final TestRegistry afterEachRegistry = new;

final GroupRegistry beforeGroupsRegistry = new;
final GroupRegistry afterGroupsRegistry = new;
final GroupStatusRegistry groupStatusRegistry = new;

type TestFunction record {|
    string name;
    function executableFunction;
    function? before = ();
    function? after = ();
    boolean alwaysRun = false;
    readonly & string[] groups = [];
    error? diagnostics = ();
    readonly & function[] dependsOn = [];
    boolean serialExecution = false;
    TestConfig? config = ();
|} & readonly;

type TestFunctionMetaData record {|
    boolean enabled = true;
    boolean skip = false;
    int dependsOnCount = 0;
    TestFunction[] dependents = [];
    boolean visited = false;
    boolean isReadyToExecute = false;
    TestCompletionStatus executionCompletionStatus = YET_TO_COMPLETE;
|};

// This class manages the execution of the tests
isolated class ExecutionManager {
    private final TestFunction[] parallelTestExecutionList = [];
    private final TestFunction[] serialTestExecutionList = [];
    private final TestFunction[] testsInExecution = [];
    private final map<TestFunctionMetaData> testMetaData = {};
    private boolean parallelExecutionEnabled = false;

    isolated function createTestFunctionMetaData(string functionName, int dependsOnCount, boolean enabled) {
        lock {
            self.testMetaData[functionName] = {dependsOnCount: dependsOnCount, enabled: enabled};
        }
    }

    isolated function setExecutionCompleted(string functionName) {
        lock {
            self.testMetaData[functionName].executionCompletionStatus = COMPLETED;
        }
    }

    isolated function setExecutionSuspended(string functionName) {
        lock {
            self.testMetaData[functionName].executionCompletionStatus = SUSPENDED;
        }
    }

    isolated function setDisabled(string functionName) {
        lock {
            self.testMetaData[functionName].enabled = false;
        }
    }

    isolated function setEnabled(string functionName) {
        lock {
            self.testMetaData[functionName].enabled = true;
        }
    }

    isolated function isEnabled(string functionName) returns boolean {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[functionName];
            return testFunctionMetaData is TestFunctionMetaData && testFunctionMetaData.enabled;
        }
    }

    isolated function setVisited(string functionName) {
        lock {
            self.testMetaData[functionName].visited = true;
        }
    }

    isolated function isVisited(string functionName) returns boolean {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[functionName];
            return testFunctionMetaData is TestFunctionMetaData && testFunctionMetaData.visited;
        }
    }

    isolated function isSkip(string functionName) returns boolean {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[functionName];
            return testFunctionMetaData is TestFunctionMetaData && testFunctionMetaData.skip;
        }
    }

    isolated function setSkip(string functionName) {
        lock {
            self.testMetaData[functionName].skip = true;
        }
    }

    isolated function addDependent(string functionName, TestFunction dependent) {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[functionName];
            if testFunctionMetaData is TestFunctionMetaData {
                testFunctionMetaData.dependents.push(dependent);
            }
        }
    }

    isolated function getDependents(string functionName) returns TestFunction[] {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[functionName];
            if testFunctionMetaData is TestFunctionMetaData {
                return testFunctionMetaData.dependents.clone();
            }
            return [];
        }
    }

    isolated function setParallelExecutionStatus(boolean isParalleleExecutionEnabled) {
        lock {
            self.parallelExecutionEnabled = isParalleleExecutionEnabled;
        }
    }

    isolated function isParallelExecutionEnabled() returns boolean {
        lock {
            return self.parallelExecutionEnabled;
        }
    }

    isolated function addInitialParallelTest(TestFunction testFunction) {
        lock {
            self.parallelTestExecutionList.unshift(testFunction);
        }
    }

    isolated function addInitialSerialTest(TestFunction testFunction) {
        lock {
            self.serialTestExecutionList.unshift(testFunction);
        }
    }

    isolated function countTestInExecution() returns int {
        lock {
            return self.testsInExecution.length();
        }
    }

    isolated function addTestInExecution(TestFunction testFunction) {
        lock {
            self.testsInExecution.push(testFunction);
        }
    }

    isolated function getSerialQueueLength() returns int {
        lock {
            return self.serialTestExecutionList.length();
        }
    }

    isolated function getParallelQueueLength() returns int {
        lock {
            return self.parallelTestExecutionList.length();
        }
    }

    isolated function isExecutionDone() returns boolean {
        lock {
            return self.parallelTestExecutionList.length() == 0 &&
                self.serialTestExecutionList.length() == 0 &&
                self.testsInExecution.length() == 0;
        }
    }

    isolated function getParallelTest() returns TestFunction {
        lock {
            return self.parallelTestExecutionList.remove(0);
        }
    }

    isolated function getSerialTest() returns TestFunction {
        lock {
            return self.serialTestExecutionList.pop();
        }
    }

    isolated function populateExecutionQueues() {
        lock {
            int i = 0;
            TestCompletionStatus executionCompletionStatus = YET_TO_COMPLETE;
            while i < self.testsInExecution.length() {
                TestFunction testInProgress = self.testsInExecution[i];
                TestFunctionMetaData? inProgressTestMetaData = self.testMetaData[testInProgress.name];
                if inProgressTestMetaData == () {
                    continue;
                }
                executionCompletionStatus = inProgressTestMetaData.executionCompletionStatus;
                if executionCompletionStatus == COMPLETED {
                    inProgressTestMetaData.dependents.reverse().forEach(
                        dependent => self.checkExecutionReadiness(dependent));
                    _ = self.testsInExecution.remove(i);
                } else if executionCompletionStatus == SUSPENDED {
                    _ = self.testsInExecution.remove(i);
                } else {
                    i += 1;
                }
            }
        }
    }

    private isolated function checkExecutionReadiness(TestFunction testFunction) {
        lock {
            TestFunctionMetaData? testFunctionMetaData = self.testMetaData[testFunction.name];
            if testFunctionMetaData == () {
                return;
            }
            testFunctionMetaData.dependsOnCount -= 1;
            if testFunctionMetaData.dependsOnCount != 0 || testFunctionMetaData.isReadyToExecute {
                return;
            }
            testFunctionMetaData.isReadyToExecute = true;
            if !testFunction.serialExecution {
                self.parallelTestExecutionList.push(testFunction);
            } else {
                self.serialTestExecutionList.push(testFunction);
            }
        }
    }
}

isolated class TestOptions {
    private string moduleName = "";
    private string packageName = "";
    private string targetPath = "";
    private map<string?> filterTestModules = {};
    private boolean hasFilteredTests = false;
    private string[] filterTests = [];
    private map<string[]> filterSubTests = {};

    isolated function isFilterSubTestsContains(string key) returns boolean {
        lock {
            return self.filterSubTests.hasKey(key);
        }
    }

    isolated function getFilterSubTest(string key) returns string[] {
        lock {
            string[]? nullOrSubTests = self.filterSubTests[key];
            if nullOrSubTests is string[] {
                return nullOrSubTests.clone();
            }
            return [];
        }
    }

    isolated function addFilterSubTest(string key, string[] subTests) {
        lock {
            self.filterSubTests[key] = subTests.clone();
        }
    }

    isolated function setFilterSubTests(map<string[]> filterSubTests) {
        lock {
            self.filterSubTests = filterSubTests.clone();
        }
    }

    isolated function setFilterTests(string[] filterTests) {
        lock {
            self.filterTests = filterTests.clone();
        }
    }

    isolated function addFilterTest(string filterTest) {
        lock {
            self.filterTests.push(filterTest);
        }
    }

    isolated function getFilterTestSize() returns int {
        lock {
            return self.filterTests.length();
        }
    }

    isolated function getFilterTestIndex(string testName) returns int? {
        lock {
            return self.filterTests.indexOf(testName);
        }
    }

    isolated function getFilterTests() returns string[] {
        lock {
            return self.filterTests.clone();
        }
    }

    isolated function setModuleName(string moduleName) {
        lock {
            self.moduleName = moduleName;
        }
    }

    isolated function setPackageName(string packageName) {
        lock {
            self.packageName = packageName;
        }
    }

    isolated function getModuleName() returns string {
        lock {
            return self.moduleName;
        }
    }

    isolated function getPackageName() returns string {
        lock {
            return self.packageName;
        }
    }

    isolated function setTargetPath(string targetPath) {
        lock {
            self.targetPath = targetPath;
        }
    }

    isolated function getTargetPath() returns string {
        lock {
            return self.targetPath;
        }
    }

    isolated function setFilterTestModules(map<string?> filterTestModulesMap) {
        lock {
            self.filterTestModules = filterTestModulesMap.clone();
        }
    }

    isolated function getFilterTestModule(string name) returns string? {
        lock {
            return self.filterTestModules[name];
        }
    }

    isolated function setFilterTestModule(string key, string? value) {
        lock {
            self.filterTestModules[key] = value;
        }
    }

    isolated function setHasFilteredTests(boolean hasFilteredTests) {
        lock {
            self.hasFilteredTests = hasFilteredTests;
        }
    }

    isolated function getHasFilteredTests() returns boolean {
        lock {
            return self.hasFilteredTests;
        }
    }
}

isolated class TestRegistry {
    private final TestFunction[] rootRegistry = [];
    private final TestFunction[] dependentRegistry = [];

    isolated function addFunction(*TestFunction functionDetails) {
        if functionDetails.dependsOn == [] {
            lock {
                self.rootRegistry.push(functionDetails);
            }
        } else {
            lock {
                self.dependentRegistry.push(functionDetails);
            }
        }
    }

    isolated function getTestFunction(function f) returns TestFunction|error {
        TestFunction[] filter;
        lock {
            filter = self.rootRegistry.filter(testFunction => f === testFunction.executableFunction).clone();
        }
        if filter.length() == 0 {
            lock {
                filter = self.dependentRegistry.filter(testFunction => f === testFunction.executableFunction).clone();
            }
            if filter.length() == 0 {
                //TODO: need to obtain the function name form the variable
                return error(string `The dependent test function is either disabled or not included.`);
            }
        }
        return filter.pop();
    }

    isolated function getFunctions() returns TestFunction[] {
        lock {
            return self.rootRegistry.sort(array:ASCENDING, (item) => item.name).clone();
        }
    }

    isolated function getDependentFunctions() returns TestFunction[] {
        lock {
            return self.dependentRegistry.clone();
        }
    }
}

isolated class GroupRegistry {
    private final map<TestFunction[]> registry = {};

    function addFunction(string 'group, *TestFunction testFunction) {
        lock {
            if self.registry.hasKey('group) {
                self.registry.get('group).push(testFunction);
            } else {
                self.registry['group] = [testFunction];
            }
        }
    }

    isolated function getFunctions(string 'group) returns TestFunction[]? {
        lock {
            if self.registry.hasKey('group) {
                return self.registry.get('group).clone();
            }
            return;
        }
    }
}

isolated class GroupStatusRegistry {
    private final map<int> enabledTests = {};
    private final map<int> totalTests = {};
    private final map<int> executedTests = {};
    private final map<boolean> skip = {};

    isolated function firstExecuted(string 'group) returns boolean {
        lock {
            return self.executedTests.get('group) > 0;
        }
    }
    isolated function lastExecuted(string 'group) returns boolean {
        lock {
            return self.executedTests.get('group) == self.enabledTests.get('group);
        }
    }

    isolated function incrementTotalTest(string 'group, boolean enabled) {
        lock {
            if self.totalTests.hasKey('group) {
                self.totalTests['group] = self.totalTests.get('group) + 1;
            } else {
                self.totalTests['group] = 1;
            }
            if enabled {
                self.skip['group] = false;
                if self.enabledTests.hasKey('group) {
                    self.enabledTests['group] = self.enabledTests.get('group) + 1;
                } else {
                    self.enabledTests['group] = 1;
                    self.executedTests['group] = 0;
                }
            }
        }
    }

    isolated function incrementExecutedTest(string 'group) {
        lock {
            if self.executedTests.hasKey('group) {
                self.executedTests['group] = self.executedTests.get('group) + 1;
            } else {
                self.executedTests['group] = 1;
            }
        }
    }

    isolated function setSkipAfterGroup(string 'group) {
        lock {
            self.skip['group] = true;
        }
    }

    isolated function getSkipAfterGroup(string 'group) returns boolean {
        lock {
            return self.skip.get('group);
        }
    }

    isolated function getGroupsList() returns string[] {
        lock {
            return self.totalTests.keys().clone();
        }
    }
}
