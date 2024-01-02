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
import ballerina/lang.runtime;

final TestRegistry testRegistry = new ();
final TestRegistry beforeSuiteRegistry = new ();
final TestRegistry afterSuiteRegistry = new ();
final TestRegistry beforeEachRegistry = new ();
final TestRegistry afterEachRegistry = new ();

final GroupRegistry beforeGroupsRegistry = new ();
final GroupRegistry afterGroupsRegistry = new ();
final GroupStatusRegistry groupStatusRegistry = new ();

type TestFunction record {|
    string name;
    function executableFunction;
    DataProviderReturnType? params = ();
    function? before = ();
    function? after = ();
    boolean alwaysRun = false;
    string[] groups = [];
    error? diagnostics = ();
    function[] dependsOn = [];
    boolean parallelizable = true;
    TestConfig? config = ();
|} & readonly;

type TestFunctionMetaData record {|
    boolean enabled = true;
    boolean skip = false;
    int dependsOnCount = 0;
    TestFunction[] dependents = [];
    boolean visited = false;
    boolean isInExecutionQueue = false;
    boolean isExecutionDone = false;
|};

isolated class ConcurrentExecutionManager {
    private TestFunction[] parallelTestExecutionList = [];
    private TestFunction[] serialTestExecutionList = [];
    private TestFunction[] testsInExecution = [];
    private int unAllocatedTestWorkers = 1;
    private int intialWorkers = 1;
    private final map<TestFunctionMetaData> testMetaData = {};

    isolated function createTestFunctionMetaData(string functionName, *TestFunctionMetaData intialMetaData) {
        lock {
            self.testMetaData[functionName] = intialMetaData.clone();
        }
    }

    isolated function setExecutionDone(string functionName) {
        lock {
            self.testMetaData[functionName].isExecutionDone = true;
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
            TestFunctionMetaData? unionResult = self.testMetaData[functionName];
            if unionResult is TestFunctionMetaData {
                return unionResult.enabled;
            } else {
                return false;
            }

        }
    }

    isolated function setVisited(string functionName) {
        lock {
            self.testMetaData[functionName].visited = true;
        }
    }

    isolated function isVisited(string functionName) returns boolean {
        lock {
            TestFunctionMetaData? unionResult = self.testMetaData[functionName];
            if unionResult is TestFunctionMetaData {
                return unionResult.visited;
            } else {
                return false;
            }

        }
    }

    isolated function isSkip(string functionName) returns boolean {
        lock {
            TestFunctionMetaData? unionResult = self.testMetaData[functionName];
            if unionResult is TestFunctionMetaData {
                return unionResult.skip;
            } else {
                return false;
            }

        }
    }

    isolated function setSkip(string functionName) {
        lock {
            self.testMetaData[functionName].skip = true;
        }
    }

    isolated function addDependent(string functionName, TestFunction dependent) {
        lock {
            TestFunctionMetaData? unionResult = self.testMetaData[functionName];
            if unionResult is TestFunctionMetaData {
                unionResult.dependents.push(dependent);
            }
        }
    }

    isolated function getDependents(string functionName) returns TestFunction[] {
        lock {
            TestFunctionMetaData? unionResult = self.testMetaData[functionName];
            if unionResult is TestFunctionMetaData {
                return unionResult.dependents.clone();
            } else {
                return [];
            }
        }
    }

    isolated function setIntialWorkers(int workers) {
        lock {
            self.intialWorkers = workers;
            self.unAllocatedTestWorkers = workers;
        }
    }

    isolated function addParallelTest(TestFunction testFunction) {
        lock {
            self.parallelTestExecutionList.push(testFunction);
        }
    }

    isolated function addSerialTest(TestFunction testFunction) {
        lock {
            self.serialTestExecutionList.push(testFunction);
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

    isolated function addTestInExecution(TestFunction testFunction) {
        lock {
            self.testsInExecution.push(testFunction);
        }
    }

    isolated function getConfiguredWorkers() returns int {
        lock {
            return self.intialWorkers;
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
                self.getAvailableWorkers() == self.intialWorkers &&
                self.serialTestExecutionList.length() == 0 &&
                self.testsInExecution.length() == 0;
        }
    }

    isolated function allocateWorker() {
        lock {
            self.unAllocatedTestWorkers -= 1;
        }
    }

    isolated function releaseWorker() {
        lock {
            self.unAllocatedTestWorkers += 1;
        }
    }

    isolated function waitForWorkers() {
        while self.getAvailableWorkers() < 1 {
            runtime:sleep(0.0001); // sleep is added to yield the strand
        }
    }

    isolated function getAvailableWorkers() returns int {
        lock {
            return self.unAllocatedTestWorkers;
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

    isolated function waitUntilEmptyQueueFilled() {
        int parallelQueueLength = 0;
        int serialQueueLength = 0;
        self.populateExecutionQueues();
        lock {
            parallelQueueLength = self.parallelTestExecutionList.length();
            serialQueueLength = self.serialTestExecutionList.length();
        }
        while parallelQueueLength == 0 && serialQueueLength == 0 {
            self.populateExecutionQueues();
            runtime:sleep(0.0001); // sleep is added to yield the strand
            if self.isExecutionDone() {
                break;
            }
        }
    }

    isolated function populateExecutionQueues() {
        lock {
            int i = 0;
            boolean isExecutionDone = false;
            while i < self.testsInExecution.length() {
                TestFunction testInProgress = self.testsInExecution[i];
                TestFunctionMetaData? inProgressTestMetaData = self.testMetaData[testInProgress.name];
                if inProgressTestMetaData == () {
                    return;
                }
                isExecutionDone = inProgressTestMetaData.isExecutionDone;
                if isExecutionDone {
                    inProgressTestMetaData.dependents.reverse().forEach(dependent => self.checkExecutionReadiness(dependent));
                    _ = self.testsInExecution.remove(i);
                } else {
                    i = i + 1;
                }
            }
        }
    }

    private isolated function checkExecutionReadiness(TestFunction testFunction) {
        lock {
            TestFunctionMetaData? unionResult = self.testMetaData[testFunction.name];
            if unionResult is TestFunctionMetaData {
                unionResult.dependsOnCount -= 1;
                if unionResult.dependsOnCount == 0 && unionResult.isInExecutionQueue != true {
                    unionResult.isInExecutionQueue = true;
                    _ = testFunction.parallelizable ? self.parallelTestExecutionList.push(testFunction) : self.serialTestExecutionList.push(testFunction);
                }

            }
        }
    }

}

class TestRegistry {
    private final TestFunction[] rootRegistry = [];
    private final TestFunction[] dependentRegistry = [];

    function addFunction(*TestFunction functionDetails) {
        if functionDetails.dependsOn == [] {
            self.rootRegistry.push(functionDetails);
        } else {
            self.dependentRegistry.push(functionDetails);
        }
    }

    function getTestFunction(function f) returns TestFunction|error {
        TestFunction[] filter;
        filter = self.rootRegistry.filter(testFunction => f === testFunction.executableFunction);
        if filter.length() == 0 {
            filter = self.dependentRegistry.filter(testFunction => f === testFunction.executableFunction);
            if filter.length() == 0 {
                //TODO: need to obtain the function name form the variable
                return error(string `The dependent test function is either disabled or not included.`);
            }
        }
        return filter.pop();
    }

    function getFunctions() returns TestFunction[] => self.rootRegistry.sort(key = testFunctionsSort);

    function getDependentFunctions() returns TestFunction[] => self.dependentRegistry;

}

class GroupRegistry {
    private final map<TestFunction[]> registry = {};

    function addFunction(string 'group, *TestFunction testFunction) {
        if self.registry.hasKey('group) {
            self.registry.get('group).push(testFunction);
        } else {
            self.registry['group] = [testFunction];
        }
    }

    function getFunctions(string 'group) returns TestFunction[]? {
        if self.registry.hasKey('group) {
            return self.registry.get('group);
        }
        return;
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

isolated function testFunctionsSort(TestFunction testFunction) returns string => testFunction.name;

function isDataDrivenTest(TestFunction testFunction) returns boolean =>
    testFunction.params is map<AnyOrError[]> || testFunction.params is AnyOrError[][];
