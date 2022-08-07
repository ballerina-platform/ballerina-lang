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

public function registerTest(string name, function f) returns error? {
    if options.tests.length() == 0 || options.tests.indexOf(name) is int {
        check processAnnotation(name, f);
    }
}

type TestFunction record {|
    string name;
    function testFunction;
    DataProviderReturnType? params = ();
    function? before = ();
    function? after = ();
|};

class TestRegistry {
    private TestFunction[] registry;

    function init() {
        self.registry = [];
    }

    function addFunction(*TestFunction functionDetails) {
        self.registry.push(functionDetails);
    }

    function getFunctions() returns TestFunction[] => self.registry;
}
