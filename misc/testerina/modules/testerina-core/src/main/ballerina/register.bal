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
    boolean enabled = true;
    DataProviderReturnType? params = ();
    function? before = ();
    function? after = ();
    boolean alwaysRun = false;
    string[] groups = [];
    boolean skip = false;
    error? diagnostics = ();
    function[] dependsOn = [];
    int dependsOnCount = 0;
    TestFunction[] dependents = [];
    boolean visited = false;
    boolean isInExecutionQueue = false;
    boolean parallelizable = true;
    TestConfig? config = ();
    boolean isExecutionDone = false;
|};

class ConcurrentExecutionManager {
    private TestFunction[] parallelTestExecutionList = [];
    private TestFunction[] serialTestExecutionList = [];
    private TestFunction[] testsInExecution = [];
    private int unAllocatedTestWorkers = 1;
    private final int intialWorkers;

    function init(int workers) {
        self.intialWorkers = workers;
        self.unAllocatedTestWorkers = workers;
    }

    function addParallelTest(TestFunction testFunction) {
        self.parallelTestExecutionList.push(testFunction);
    }

    function addSerialTest(TestFunction testFunction) {
        self.serialTestExecutionList.push(testFunction);
    }

    function addTestInExecution(TestFunction testFunction) {
        self.testsInExecution.push(testFunction);
    }

    function getConfiguredWorkers() returns int {
        return self.intialWorkers;
    }

    function getSerialQueueLength() returns int {
        return self.serialTestExecutionList.length();
    }

    function getParallelQueueLength() returns int {
        return self.parallelTestExecutionList.length();
    }

    function isExecutionDone() returns boolean {
        return self.parallelTestExecutionList.length() == 0 &&
                self.getAvailableWorkers() == testWorkers &&
                self.serialTestExecutionList.length() == 0 &&
                self.testsInExecution.length() == 0;

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

    function getParallelTest() returns TestFunction {
        return self.parallelTestExecutionList.remove(0);
    }

    function getSerialTest() returns TestFunction {
        return self.serialTestExecutionList.pop();

    }

    function waitUntilEmptyQueueFilled() {
        while self.parallelTestExecutionList.length() == 0 && self.serialTestExecutionList.length() == 0 {
            self.populateExecutionQueues();
            runtime:sleep(0.0001); // sleep is added to yield the strand
            if self.isExecutionDone() {
                break;
            }
        }
    }

    function populateExecutionQueues() {
        int i = 0;
        boolean isExecutionDone = false;
        while i < self.testsInExecution.length() {
            TestFunction testInProgress = self.testsInExecution[i];
            lock {
                isExecutionDone = testInProgress.isExecutionDone;
            }
            if isExecutionDone {
                testInProgress.dependents.forEach(dependent => self.checkExecutionReadiness(dependent));
                _ = self.testsInExecution.remove(i);
            } else {
                i = i + 1;
            }
        }
    }

    private function checkExecutionReadiness(TestFunction testFunction) {
        testFunction.dependsOnCount -= 1;
        if testFunction.dependsOnCount == 0 && testFunction.isInExecutionQueue != true {
            testFunction.isInExecutionQueue = true;
            _ = testFunction.parallelizable ? self.parallelTestExecutionList.push(testFunction) : self.serialTestExecutionList.push(testFunction);
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
