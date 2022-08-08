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

final TestRegistry testRegistry = new ();
final TestRegistry beforeSuiteRegistry = new ();
final TestRegistry afterSuiteRegistry = new ();
final TestRegistry beforeEachRegistry = new ();
final TestRegistry afterEachRegistry = new ();

final GroupRegistry beforeGroupsRegistry = new ();
final GroupRegistry afterGroupsRegistry = new ();
final GroupStatusRegistry groupStatusRegistry = new ();

public function registerTest(string name, function f) returns error? {
    if filterTests.length() == 0 || filterTests.indexOf(name) is int {
        check processAnnotation(name, f);
    }
}

type TestFunction record {|
    string name;
    function executableFunction;
    DataProviderReturnType? params = ();
    function? before = ();
    function? after = ();
    boolean alwaysRun = false;
    string[] groups = [];
    boolean skip = false;
|};

class TestRegistry {
    private final TestFunction[] registry = [];

    function addFunction(*TestFunction functionDetails) {
        self.registry.push(functionDetails);
    }

    function getFunctions() returns TestFunction[] => self.registry;
}

class GroupRegistry {
    private final map<TestFunction[]> registry = {};

    function addFunction(string group, TestFunction testFunction) {
        if self.registry.hasKey(group) {
            self.registry.get(group).push(testFunction);
        } else {
            self.registry[group] = [testFunction];
        }
    }

    function getFunctions(string group) returns TestFunction[]? {
        if self.registry.hasKey(group) {
            return self.registry.get(group);
        }
        return;
    }
}

class GroupStatusRegistry {
    private final map<int> totalTests = {};
    private final map<int> executedTests = {};
    private final map<boolean> skip = {};

    function firstExecuted(string group) returns boolean => self.executedTests.get(group) > 0;

    function lastExecuted(string group) returns boolean => self.executedTests.get(group) == self.totalTests.get(group);

    function incrementTotalTest(string group) {
        self.skip[group] = false;
        if self.totalTests.hasKey(group) {
            self.totalTests[group] = self.totalTests.get(group) + 1;
        } else {
            self.totalTests[group] = 1;
            self.executedTests[group] = 0;
        }
    }

    function incrementExecutedTest(string group) {
        if self.executedTests.hasKey(group) {
            self.executedTests[group] = self.executedTests.get(group) + 1;
        } else {
            self.executedTests[group] = 1;
        }
    }

    function setSkipAfterGroup(string group) {
        self.skip[group] = true;
    }

    function getSkipAfterGroup(string group) returns boolean => self.skip.get(group);
}

